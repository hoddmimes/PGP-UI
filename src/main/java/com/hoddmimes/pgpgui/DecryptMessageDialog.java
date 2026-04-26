package com.hoddmimes.pgpgui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.hoddmimes.pgpgui.GPGAdapter.DecryptInterface;

public class DecryptMessageDialog extends JDialog implements DecryptInterface {

	private static final long   serialVersionUID   = 1L;
	private static final String STATIC_MSG_HEADER  = "-----BEGIN STATIC ENCRYPTED MESSAGE-----";
	private static final String PGP_MSG_HEADER     = "-----BEGIN PGP MESSAGE-----";
	private static final String PBKDF2_SALT        = "MANDELBROTIAN";
	private static final int    PBKDF2_ITERATIONS  = 65431;

	private final JButton        mDecryptBtn;
	private final JButton        mCancelBtn;
	private final JTextArea      mText;
	private final JTextField     mSignUsers;
	private final JPanel         mSignPanel;
	private final JPanel         mSouthPanel;
	private final JPanel         mPasswordPanel;
	private final JPasswordField mPasswordField;

	public DecryptMessageDialog() {
		setTitle("Decrypt Message");
		GPGAdapter.setAppIcon(this, this);
		getContentPane().setLayout(new BorderLayout(0, 0));

		// ── Text area ────────────────────────────────────────────────────────

		mText = new JTextArea();
		mText.setRows(15);
		mText.setColumns(80);
		addClipboardPopup(mText);
		getContentPane().add(new JScrollPane(mText), BorderLayout.CENTER);

		// ── South panel ──────────────────────────────────────────────────────

		mSouthPanel = new JPanel(new BorderLayout());
		getContentPane().add(mSouthPanel, BorderLayout.SOUTH);

		// Signing-user row (shown after decrypting a signed PGP message)
		mSignPanel = new JPanel();
		FlowLayout tSignFlow = new FlowLayout(FlowLayout.LEFT);
		tSignFlow.setHgap(10);
		mSignPanel.setLayout(tSignFlow);
		mSignPanel.setBorder(new EmptyBorder(10, 10, 0, 0));
		mSignPanel.add(new JLabel("Signing user"));
		mSignUsers = new JTextField();
		mSignUsers.setColumns(40);
		mSignUsers.setEditable(false);
		mSignPanel.add(mSignUsers);

		// Password row (shown when a static-encrypted message is detected)
		mPasswordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		mPasswordPanel.setBorder(new EmptyBorder(5, 10, 0, 0));
		mPasswordPanel.add(new JLabel("Password:"));
		mPasswordField = new JPasswordField(30);
		mPasswordPanel.add(mPasswordField);
		mPasswordPanel.setVisible(false);
		mSouthPanel.add(mPasswordPanel, BorderLayout.CENTER);

		// Button row
		JPanel tButtonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		mSouthPanel.add(tButtonPane, BorderLayout.SOUTH);

		mDecryptBtn = new JButton("Decrypt");
		mDecryptBtn.addActionListener(event -> decryptMessage());
		tButtonPane.add(mDecryptBtn);
		getRootPane().setDefaultButton(mDecryptBtn);

		mCancelBtn = new JButton("Cancel");
		mCancelBtn.addActionListener(event -> dispose());
		tButtonPane.add(mCancelBtn);

		// ── Listeners ────────────────────────────────────────────────────────

		DocumentListener tTextListener = new DocumentListener() {
			@Override public void insertUpdate(DocumentEvent e)  { onTextChanged(); }
			@Override public void removeUpdate(DocumentEvent e)  { onTextChanged(); }
			@Override public void changedUpdate(DocumentEvent e) { onTextChanged(); }
		};
		mText.getDocument().addDocumentListener(tTextListener);

		DocumentListener tPasswordListener = new DocumentListener() {
			@Override public void insertUpdate(DocumentEvent e)  { updateDecryptButton(); }
			@Override public void removeUpdate(DocumentEvent e)  { updateDecryptButton(); }
			@Override public void changedUpdate(DocumentEvent e) { updateDecryptButton(); }
		};
		mPasswordField.getDocument().addDocumentListener(tPasswordListener);

		this.pack();
	}

	// ── UI state helpers ──────────────────────────────────────────────────────

	private void onTextChanged() {
		String tText = mText.getText();
		boolean tIsStatic = tText.contains(STATIC_MSG_HEADER);
		mPasswordPanel.setVisible(tIsStatic);
		updateDecryptButton();
		pack();
	}

	private void updateDecryptButton() {
		if (mPasswordPanel.isVisible()) {
			mDecryptBtn.setEnabled(mPasswordField.getPassword().length > 0);
		} else {
			mDecryptBtn.setEnabled(true);
		}
	}

	// ── Decryption ────────────────────────────────────────────────────────────

	private void addClipboardPopup(JTextArea pTextArea) {
		JPopupMenu tPopup = new JPopupMenu();
		JMenuItem tCopyItem = new JMenuItem("Copy to Clipboard");
		tCopyItem.addActionListener(e -> Toolkit.getDefaultToolkit().getSystemClipboard()
				.setContents(new StringSelection(pTextArea.getText()), null));
		tPopup.add(tCopyItem);
		JMenuItem tPasteItem = new JMenuItem("Paste from Clipboard");
		tPasteItem.addActionListener(e -> pasteFromClipboard());
		tPopup.add(tPasteItem);
		pTextArea.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e)  { if (e.isPopupTrigger()) tPopup.show(pTextArea, e.getX(), e.getY()); }
			@Override public void mouseReleased(MouseEvent e) { if (e.isPopupTrigger()) tPopup.show(pTextArea, e.getX(), e.getY()); }
		});
	}

	private void pasteFromClipboard() {
		try {
			Transferable tContents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
			if (tContents != null && tContents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				String tText = (String) tContents.getTransferData(DataFlavor.stringFlavor);
				if (tText != null && !tText.isEmpty()) {
					mText.setText(tText.replace("\r\n", "\n").replace("\r", "\n"));
					return;
				}
			}
			AlertMessage.showMessage(this, "Nothing to paste from clipboard");
		} catch (Exception e) {
			AlertMessage.showMessage(this, "Failed to paste from clipboard: " + e.getMessage());
		}
	}

	private void decryptMessage() {
		String tText = mText.getText();

		if (tText.contains(STATIC_MSG_HEADER)) {
			decryptStaticMessage(tText);
		} else if (tText.contains(PGP_MSG_HEADER)) {
			decryptPgpMessage(tText);
		} else {
			AlertMessage.showMessage(this, "Does not seem to be a valid encrypted message");
		}
	}

	private void decryptPgpMessage(String tMessage) {
		try {
			mText.setText("");
			GPGAdapter.getInstance().decryptMessage(tMessage.getBytes(), this);
		} catch (Exception e) {
			AlertMessage.showMessage(this, "Failed to decode message: " + e.getMessage());
			mText.setText(tMessage);
		}
	}

	private void decryptStaticMessage(String tCipherText) {
		char[] tPassword = mPasswordField.getPassword();
		try {
			// Extract Base64 payload between the header/footer lines
			String tHeader = STATIC_MSG_HEADER;
			String tFooter = "-----END STATIC ENCRYPTED MESSAGE-----";
			int tStart = tCipherText.indexOf(tHeader) + tHeader.length();
			int tEnd   = tCipherText.indexOf(tFooter);
			if (tStart < 0 || tEnd < 0 || tEnd <= tStart) {
				AlertMessage.showMessage(this, "Malformed static encrypted message");
				return;
			}
			byte[] tCombined = Base64.getMimeDecoder().decode(tCipherText.substring(tStart, tEnd).trim());
			if (tCombined.length < 17) {
				AlertMessage.showMessage(this, "Malformed static encrypted message (too short)");
				return;
			}

			// Derive key
			byte[] tSalt = PBKDF2_SALT.getBytes(StandardCharsets.UTF_8);
			SecretKeyFactory tFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			PBEKeySpec tSpec = new PBEKeySpec(tPassword, tSalt, PBKDF2_ITERATIONS, 256);
			SecretKey tTmp = tFactory.generateSecret(tSpec);
			SecretKeySpec tSecretKey = new SecretKeySpec(tTmp.getEncoded(), "AES");
			tSpec.clearPassword();

			// Split IV + ciphertext
			byte[] tIv         = new byte[16];
			byte[] tEncrypted  = new byte[tCombined.length - 16];
			System.arraycopy(tCombined, 0,  tIv,        0, 16);
			System.arraycopy(tCombined, 16, tEncrypted, 0, tEncrypted.length);

			Cipher tCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			tCipher.init(Cipher.DECRYPT_MODE, tSecretKey, new IvParameterSpec(tIv));
			byte[] tDecrypted = tCipher.doFinal(tEncrypted);

			mText.setText(new String(tDecrypted, StandardCharsets.UTF_8));
			mPasswordPanel.setVisible(false);
			updateDecryptButton();
			pack();
		} catch (Exception e) {
			AlertMessage.showMessage(this, "Failed to decrypt message (wrong password?): " + e.getMessage());
		}
	}

	// ── DecryptInterface callbacks ────────────────────────────────────────────

	@Override
	public char[] getPasswordForDecrypt(long pKeyId, String pUserId) {
		PasswordDialog tPasswDialog = new PasswordDialog(this, "Decrypt Private Password", pUserId, pKeyId);
		tPasswDialog.setVisible(true);
		char[] tPassword = tPasswDialog.getPassword();
		tPasswDialog.dispose();
		return tPassword;
	}

	@Override
	public void decryptedMessage(byte[] pMessageBytes) {
		String tCurrentText = mText.getText();
		String tDecrypted   = new String(pMessageBytes, StandardCharsets.UTF_8);
		if (tCurrentText.isEmpty()) {
			mText.setText(tDecrypted);
		} else {
			mText.setText(tCurrentText + "\n-----------------------------------------\n" + tDecrypted);
		}
	}

	@Override
	public void encryptSignOnePassUsers(String pUserList) {
		mSignUsers.setText(pUserList);
		mSouthPanel.add(mSignPanel, BorderLayout.NORTH);
		revalidate();
		repaint();
	}

	@Override
	public void encryptSignUsers(String pUserList) {
		System.out.println("Signature User: " + pUserList);
	}
}

package com.hoddmimes.pgpgui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.bouncycastle.openpgp.PGPSecretKeyRing;

@SuppressWarnings("serial")
public class EncryptMessageDialog extends JDialog implements GPGAdapter.GetPasswordInterface, TableModelListener, ActionListener {

	private static final String  PBKDF2_SALT       = "MANDELBROTIAN";
	private static final int     PBKDF2_ITERATIONS  = 65431;

	// PGP tab
	private final JTextArea          mPgpText;
	private final JButton            mPgpEncryptBtn;
	private final JButton            mPgpRestoreBtn;
	private String                   mPgpOriginalText;
	private KeyContainer             mKeyContainer;
	private JPanel                   mAESKeyStrengthPanel;
	private JPanel                   mSignPanel;
	private String[]                 mAESKeyStrength = {"256", "512", "1024", "2048", "4096"};
	private JComboBox<String>        mAESStrengthComboBox;
	private JComboBox<SigningUser>   mSignComboBox;
	private JComboBox<String>        mEncryptAlgoComboBox;

	// Static password tab
	private final JTextArea          mStaticText;
	private final JPasswordField     mPasswordField;
	private final JButton            mStaticEncryptBtn;
	private final JButton            mStaticRestoreBtn;
	private String                   mStaticOriginalText;

	public EncryptMessageDialog() {
		setTitle("Encrypt Message");
		GPGAdapter.setAppIcon(this, this);
		this.setSize(800, 600);
		mPgpOriginalText   = "";
		mStaticOriginalText = "";

		getContentPane().setLayout(new BorderLayout(0, 0));

		JTabbedPane tTabbedPane = new JTabbedPane();
		getContentPane().add(tTabbedPane, BorderLayout.CENTER);

		// ── PGP Encryption tab ───────────────────────────────────────────────

		mPgpText = new JTextArea();
		mPgpText.setRows(15);
		mPgpText.setColumns(72);
		addClipboardPopup(mPgpText);
		JScrollPane tPgpScrollPane = new JScrollPane(mPgpText);

		JPanel tPgpSouthPanel = new JPanel(new BorderLayout(0, 0));

		JPanel tEncryptAlgoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		if (PGPGUI.USE_EXTENSION) {
			tEncryptAlgoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		} else {
			tEncryptAlgoPanel.setBorder(new EmptyBorder(10, 10, 10, 40));
		}

		mSignPanel = new JPanel();
		mSignPanel.add(new JLabel("Signing user"));
		mSignComboBox = new JComboBox<>(getSignUsers());
		mSignComboBox.addActionListener(this);
		mSignPanel.add(mSignComboBox);
		tEncryptAlgoPanel.add(mSignPanel);

		tEncryptAlgoPanel.add(new JLabel("Encrypt Algo"));
		mEncryptAlgoComboBox = new JComboBox<>(getEncryptionAlgorithms());
		mEncryptAlgoComboBox.addActionListener(this);
		tEncryptAlgoPanel.add(mEncryptAlgoComboBox);

		mAESKeyStrengthPanel = new JPanel(new FlowLayout());
		mAESKeyStrengthPanel.setBorder(new EmptyBorder(0, 15, 0, 0));
		mAESKeyStrengthPanel.add(new JLabel("AES Encryption"));
		mAESStrengthComboBox = new JComboBox<>(mAESKeyStrength);
		mAESKeyStrengthPanel.add(mAESStrengthComboBox);
		mAESKeyStrengthPanel.add(new JLabel("bits"));
		if (PGPGUI.USE_EXTENSION) {
			tEncryptAlgoPanel.add(mAESKeyStrengthPanel);
		}

		tPgpSouthPanel.add(tEncryptAlgoPanel, BorderLayout.NORTH);

		JPanel tKeyPanel = new JPanel(new FlowLayout());
		tKeyPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		mKeyContainer = new KeyContainer(KeyType.PUBLIC, 775, 100, true, true);
		mKeyContainer.addTableModelListener(this);
		tKeyPanel.add(mKeyContainer);
		tPgpSouthPanel.add(tKeyPanel, BorderLayout.CENTER);

		JPanel tPgpButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		tPgpButtonPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

		mPgpEncryptBtn = new JButton("Encrypt");
		mPgpEncryptBtn.setEnabled(false);
		mPgpEncryptBtn.addActionListener(e -> encryptPgpMessage());
		tPgpButtonPanel.add(mPgpEncryptBtn);

		mPgpRestoreBtn = new JButton("Restore Text");
		mPgpRestoreBtn.setEnabled(false);
		mPgpRestoreBtn.addActionListener(e -> mPgpText.setText(mPgpOriginalText));
		tPgpButtonPanel.add(mPgpRestoreBtn);

		JButton tPgpCancelBtn = new JButton("Cancel");
		tPgpCancelBtn.addActionListener(e -> dispose());
		tPgpButtonPanel.add(tPgpCancelBtn);
		getRootPane().setDefaultButton(tPgpCancelBtn);

		tPgpSouthPanel.add(tPgpButtonPanel, BorderLayout.SOUTH);

		JPanel tPgpTab = new JPanel(new BorderLayout());
		tPgpTab.add(tPgpScrollPane, BorderLayout.CENTER);
		tPgpTab.add(tPgpSouthPanel, BorderLayout.SOUTH);

		// ── Static Password Encryption tab ───────────────────────────────────

		mStaticText = new JTextArea();
		mStaticText.setRows(15);
		mStaticText.setColumns(72);
		addClipboardPopup(mStaticText);
		JScrollPane tStaticScrollPane = new JScrollPane(mStaticText);

		mPasswordField = new JPasswordField(30);

		mStaticEncryptBtn = new JButton("Encrypt");
		mStaticEncryptBtn.setEnabled(false);
		mStaticEncryptBtn.addActionListener(e -> encryptStaticMessage());

		mStaticRestoreBtn = new JButton("Restore Text");
		mStaticRestoreBtn.setEnabled(false);
		mStaticRestoreBtn.addActionListener(e -> mStaticText.setText(mStaticOriginalText));

		JButton tStaticCancelBtn = new JButton("Cancel");
		tStaticCancelBtn.addActionListener(e -> dispose());

		JPanel tPasswordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tPasswordPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		tPasswordPanel.add(new JLabel("Password:"));
		tPasswordPanel.add(mPasswordField);

		JPanel tStaticButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		tStaticButtonPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
		tStaticButtonPanel.add(mStaticEncryptBtn);
		tStaticButtonPanel.add(mStaticRestoreBtn);
		tStaticButtonPanel.add(tStaticCancelBtn);

		JPanel tInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tInfoPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
				new EmptyBorder(5, 8, 5, 8),
				javax.swing.BorderFactory.createTitledBorder("Encryption Details")));
		tInfoPanel.add(new JLabel("Cipher: AES-256 (CBC/PKCS5)   |   Key derivation: PBKDF2-HMAC-SHA256   |   Iterations: " + PBKDF2_ITERATIONS + "   |   Salt: " + PBKDF2_SALT));

		JPanel tStaticSouthPanel = new JPanel(new BorderLayout());
		tStaticSouthPanel.add(tPasswordPanel, BorderLayout.NORTH);
		tStaticSouthPanel.add(tInfoPanel, BorderLayout.CENTER);
		tStaticSouthPanel.add(tStaticButtonPanel, BorderLayout.SOUTH);

		JPanel tStaticTab = new JPanel(new BorderLayout());
		tStaticTab.add(tStaticScrollPane, BorderLayout.CENTER);
		tStaticTab.add(tStaticSouthPanel, BorderLayout.SOUTH);

		DocumentListener tStaticDocListener = new DocumentListener() {
			@Override public void insertUpdate(DocumentEvent e)  { updateStaticButtons(); }
			@Override public void removeUpdate(DocumentEvent e)  { updateStaticButtons(); }
			@Override public void changedUpdate(DocumentEvent e) { updateStaticButtons(); }
		};
		mStaticText.getDocument().addDocumentListener(tStaticDocListener);
		mPasswordField.getDocument().addDocumentListener(tStaticDocListener);

		// ── Add tabs ─────────────────────────────────────────────────────────

		tTabbedPane.addTab("PGP Encryption", tPgpTab);
		tTabbedPane.addTab("Static Password Encryption", tStaticTab);
	}

	private void addClipboardPopup(JTextArea pTextArea) {
		JPopupMenu tPopup = new JPopupMenu();
		JMenuItem tCopyItem = new JMenuItem("Copy to Clipboard");
		tCopyItem.addActionListener(e -> copyToClipboard(pTextArea.getText()));
		tPopup.add(tCopyItem);
		JMenuItem tPasteItem = new JMenuItem("Paste from Clipboard");
		tPasteItem.addActionListener(e -> pasteFromClipboard(pTextArea));
		tPopup.add(tPasteItem);
		pTextArea.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e)  { if (e.isPopupTrigger()) tPopup.show(pTextArea, e.getX(), e.getY()); }
			@Override public void mouseReleased(MouseEvent e) { if (e.isPopupTrigger()) tPopup.show(pTextArea, e.getX(), e.getY()); }
		});
	}

	private void copyToClipboard(String pText) {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(pText), null);
	}

	private void pasteFromClipboard(JTextArea pTarget) {
		try {
			Transferable tContents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
			if (tContents != null && tContents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				String tText = (String) tContents.getTransferData(DataFlavor.stringFlavor);
				if (tText != null && !tText.isEmpty()) {
					pTarget.setText(tText.replace("\r\n", "\n").replace("\r", "\n"));
					return;
				}
			}
			AlertMessage.showMessage(this, "Nothing to paste from clipboard");
		} catch (Exception e) {
			AlertMessage.showMessage(this, "Failed to paste from clipboard: " + e.getMessage());
		}
	}

	private void updateStaticButtons() {
		boolean tHasText     = !mStaticText.getText().isEmpty();
		boolean tHasPassword = mPasswordField.getPassword().length > 0;
		mStaticEncryptBtn.setEnabled(tHasText && tHasPassword);
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		if (e.getType() == TableModelEvent.UPDATE) {
			mPgpEncryptBtn.setEnabled(mKeyContainer.anyUserSelected());
		}
	}

	private SigningUser[] getSignUsers() {
		try {
			List<KeyRingInterface> tKeyRings = GPGAdapter.getInstance().getSecretKeyRings();
			SigningUser[] tArr = new SigningUser[tKeyRings.size() + 1];
			tArr[0] = new SigningUser(null);
			for (int i = 0; i < tKeyRings.size(); i++) {
				tArr[i + 1] = new SigningUser((SecKeyRing) tKeyRings.get(i));
			}
			return tArr;
		} catch (Exception e) {
			AlertMessage.showMessage(this, "Failed to retrieve secret keys: " + e.getMessage());
			return new SigningUser[]{new SigningUser(null)};
		}
	}

	private String[] getEncryptionAlgorithms() {
		ArrayList<String> tAlgos = new ArrayList<>();
		tAlgos.add(PGPGUI.ENCRYPT_ALGO_AES);
		tAlgos.add(PGPGUI.ENCRYPT_ALGO_CAST5);
		tAlgos.add(PGPGUI.ENCRYPT_ALGO_BLOWFISH);
		tAlgos.add(PGPGUI.ENCRYPT_ALGO_CAMELLIA_256);
		tAlgos.add(PGPGUI.ENCRYPT_ALGO_IDEA);
		tAlgos.add(PGPGUI.ENCRYPT_ALGO_TWOFISH);
		if (PGPGUI.USE_EXTENSION) {
			tAlgos.add(PGPGUI.ENCRYPT_ALGO_SNOWFLAKE);
		}
		return tAlgos.toArray(new String[0]);
	}

	private void encryptPgpMessage() {
		if (mPgpText.getText().isEmpty()) {
			AlertMessage.showMessage(this, "Nothing to encrypt");
			return;
		}

		PubKeyRingInterface tEncKeyRing = (PubKeyRingInterface) mKeyContainer.getKeyAtRow(mKeyContainer.getSelectedRow());
		if (tEncKeyRing == null) {
			AlertMessage.showMessage(this, "PGP key not found for selected user");
			return;
		}

		int tKeyStrength = Integer.parseInt((String) mAESStrengthComboBox.getSelectedItem());
		String tAlgo = (String) mEncryptAlgoComboBox.getSelectedItem();

		try {
			byte[] tInBytes = mPgpText.getText().getBytes(StandardCharsets.UTF_8);
			PGPSecretKeyRing tSecSignKeyRing = (((SigningUser) mSignComboBox.getSelectedItem()).mKeyRing == null)
					? null : ((SigningUser) mSignComboBox.getSelectedItem()).mKeyRing.getSecretKeyRing();
			ByteArrayOutputStream tOut = GPGAdapter.getInstance().encryptMessage(
					tInBytes, tEncKeyRing.getPublicEncryptionKey(), tSecSignKeyRing, this, tAlgo, tKeyStrength);
			mPgpOriginalText = mPgpText.getText();
			mPgpRestoreBtn.setEnabled(true);
			mPgpText.setText(tOut.toString());
		} catch (Exception e) {
			AlertMessage.showMessage(this, "Failed to encrypt message: " + e.getMessage());
		}
	}

	private void encryptStaticMessage() {
		String tPlainText = mStaticText.getText();
		char[] tPassword  = mPasswordField.getPassword();

		try {
			byte[] tSalt = PBKDF2_SALT.getBytes(StandardCharsets.UTF_8);
			SecretKeyFactory tFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			PBEKeySpec tSpec = new PBEKeySpec(tPassword, tSalt, PBKDF2_ITERATIONS, 256);
			SecretKey tTmp = tFactory.generateSecret(tSpec);
			SecretKeySpec tSecretKey = new SecretKeySpec(tTmp.getEncoded(), "AES");
			tSpec.clearPassword();

			byte[] tIv = new byte[16];
			new SecureRandom().nextBytes(tIv);
			Cipher tCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			tCipher.init(Cipher.ENCRYPT_MODE, tSecretKey, new IvParameterSpec(tIv));
			byte[] tEncrypted = tCipher.doFinal(tPlainText.getBytes(StandardCharsets.UTF_8));

			byte[] tCombined = new byte[tIv.length + tEncrypted.length];
			System.arraycopy(tIv, 0, tCombined, 0, tIv.length);
			System.arraycopy(tEncrypted, 0, tCombined, tIv.length, tEncrypted.length);

			String tEncoded = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(tCombined);
			String tResult  = "-----BEGIN STATIC ENCRYPTED MESSAGE-----\n"
					+ tEncoded
					+ "\n-----END STATIC ENCRYPTED MESSAGE-----";

			mStaticOriginalText = tPlainText;
			mStaticRestoreBtn.setEnabled(true);
			mStaticText.setText(tResult);
		} catch (Exception e) {
			AlertMessage.showMessage(this, "Failed to encrypt message: " + e.getMessage());
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == mEncryptAlgoComboBox) {
			mAESStrengthComboBox.setEnabled("AES".equals(mEncryptAlgoComboBox.getSelectedItem()));
		}
	}

	@Override
	public char[] getPasswordForDecrypt(long pKeyId, String pUserId) {
		PasswordDialog tPasswDialog = new PasswordDialog(this, "Signing Private Password", pUserId, pKeyId);
		tPasswDialog.setVisible(true);
		char[] tPassword = tPasswDialog.getPassword();
		tPasswDialog.dispose();
		return tPassword;
	}

	@Override
	public Component getComponent() {
		return this;
	}

	class SigningUser {
		SecKeyRing mKeyRing;

		SigningUser(SecKeyRing pKeyRing) {
			mKeyRing = pKeyRing;
		}

		@Override
		public String toString() {
			return mKeyRing == null ? "None" : mKeyRing.getFirstUserId();
		}
	}
}

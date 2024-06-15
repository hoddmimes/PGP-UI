package com.hoddmimes.gpgui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.hoddmimes.gpgui.GPGAdapter.DecryptInterface;

public class DecryptMessageDialog extends JDialog implements DecryptInterface {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JButton mCancelBtn;
	private final JButton mDecryptBtn;
	private final JTextArea   mText;
	private final JTextField  mSignUsers;
	private final JPanel 	  mSignPanel;
	private final JPanel 	  mSouthPanel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			DecryptMessageDialog dialog = new DecryptMessageDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.pack();
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public DecryptMessageDialog() {
		//setBounds(100, 100, 616, 528);
		setTitle("Decrypt Message");
		GPGAdapter.setAppIcon( this, this );
		getContentPane().setLayout(new BorderLayout(0, 0));
		JPanel tTextPanel = new JPanel();
		tTextPanel.setLayout(new FlowLayout());
		tTextPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		//getContentPane().add(tTextPanel, BorderLayout.CENTER);
		
		JScrollPane mTextScrollPane = new JScrollPane();
		tTextPanel.add(mTextScrollPane);
		
		mText = new JTextArea();
		mText.setRows(15);
		mText.setColumns(80);
		mTextScrollPane.setViewportView(mText);
		getContentPane().add(mTextScrollPane, BorderLayout.CENTER);
		
		/**
		 * South Panel
		 */
		mSouthPanel = new JPanel(new BorderLayout());
		getContentPane().add(mSouthPanel, BorderLayout.SOUTH);
		

		
		mSignPanel = new JPanel();
		FlowLayout tSignFlowLayout = new FlowLayout(FlowLayout.LEFT);
		tSignFlowLayout.setHgap(10);
		mSignPanel.setLayout(tSignFlowLayout);
		mSignPanel.setBorder( new EmptyBorder(10, 10, 0, 0));
		
		mSignPanel.add( new JLabel("Signing user"));
		
		mSignUsers = new JTextField();
		mSignUsers.setColumns(40);
		mSignUsers.setEditable(false);
		mSignPanel.add(mSignUsers);
		mSignPanel.setEnabled(false);
		//mSouthPanel.add(mSignPanel, BorderLayout.NORTH);
		

		JPanel tButtonPane = new JPanel();
		tButtonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		mSouthPanel.add(tButtonPane, BorderLayout.SOUTH);
		
		mDecryptBtn = new JButton("Decrypt");
		mDecryptBtn.setActionCommand("DECRYPT");
		mDecryptBtn.addActionListener( event-> { decryptMessage(); });
		tButtonPane.add(mDecryptBtn);
		getRootPane().setDefaultButton(mDecryptBtn);
		
	
		mCancelBtn = new JButton("Cancel");
		mCancelBtn.setActionCommand("CANCEL");
		mCancelBtn.addActionListener(event-> {this.dispose();});
		tButtonPane.add(mCancelBtn);	
		this.pack();
	}
	
	@Override
	public char[] getPasswordForDecrypt( long pKeyId, String pUserId )
	{
		 PasswordDialog tPasswDialog = new PasswordDialog(this, "Decrypt Private Password", pUserId, pKeyId);
		 tPasswDialog.setVisible(true);
		 char[] tPassword = tPasswDialog.getPassword();
		 tPasswDialog.dispose();
		 return tPassword;
	}
	

	private void decryptMessage() {
		String tMessage = null;
		if (mText.getText().indexOf("BEGIN PGP MESSAGE") < 0) {
			AlertMessage.showMessage(this, "Does not seams to be a valid PGP encrypted message");
			return;
		}

		try {
			tMessage = mText.getText();
			mText.setText("");
			GPGAdapter.getInstance().decryptMessage(tMessage.getBytes(), this);
		}
		catch( Exception e) {
			AlertMessage.showMessage(this, "Failed to decode message: " + e.getMessage());
			mText.setText(tMessage);
			return;
		}
	}


	@Override
	public void decryptedMessage(byte[] pMessageBytes) {
		String tCurrentText = mText.getText();
		if (tCurrentText.isEmpty()) {
			try {mText.setText( new String(pMessageBytes, StandardCharsets.UTF_8));}
			catch( Exception e) {
				e.printStackTrace();
			}
		} else {
			try {mText.setText( tCurrentText + 
								"\n-----------------------------------------\n" +
								new String(pMessageBytes, StandardCharsets.UTF_8));}
			catch( Exception e) {
				e.printStackTrace();
			}
		}
	}
	

	@Override
	public void encryptSignOnePassUsers(String pUserList) {
		mSignUsers.setText(pUserList);
		mSouthPanel.add( mSignPanel, BorderLayout.NORTH);
		this.revalidate();
		this.repaint();
	}

	@Override
	public void encryptSignUsers(String pUserList) {
		System.out.println("Signature User: " + pUserList);
	}
}

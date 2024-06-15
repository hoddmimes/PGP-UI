package com.hoddmimes.gpgui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.bouncycastle.openpgp.PGPSecretKeyRing;

@SuppressWarnings("serial")
public class EncryptMessageDialog extends JDialog implements  GPGAdapter.GetPasswordInterface, TableModelListener, ActionListener {

	
	
	private final JTextArea 		mText;
	private final JButton 			mEncryptBtn;
	private final JButton 			mRestoreBtn;
	private String					mOrginalText;
	private KeyContainer			mKeyContainer;
	private JPanel					mAESKeyStrengthPanel;
	private JPanel					mSignPanel;
	
	private String[] mAESKeyStrength = new String[] {"256","512","1024","2048","4096"};
	private  JComboBox<String>  	 mAESStrengthComboBox;
	private  JComboBox<SigningUser>  mSignComboBox;
	
	private  JComboBox<String>  mEncryptAlgoComboBox;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			EncryptMessageDialog dialog = new EncryptMessageDialog();
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
	public EncryptMessageDialog() {
		setTitle("Encrypt Message");
		GPGAdapter.setAppIcon( this, this );
		this.setSize(800, 500);
		mOrginalText = "";
		
		
		getContentPane().setLayout(new BorderLayout(0, 0));
		JPanel tTextPanel = new JPanel();
		tTextPanel.setLayout(new FlowLayout());
		tTextPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		//getContentPane().add(tTextPanel, BorderLayout.CENTER);
		
		JScrollPane mTextScrollPane = new JScrollPane();
		tTextPanel.add(mTextScrollPane);
		
		mText = new JTextArea();
		mText.setRows(15);
		mText.setColumns(72);
		mTextScrollPane.setViewportView(mText);
		getContentPane().add(mTextScrollPane, BorderLayout.CENTER);
		
				
		/**
		 * Create South panel Key-filter Panel + Button Panel
		 */
		
		JPanel mSouthPanel = new JPanel();
		
		getContentPane().add(mSouthPanel, BorderLayout.SOUTH);
		mSouthPanel.setLayout(new BorderLayout(0, 0));
		
		
		
		JPanel tEncryptAlgoPanel = new JPanel();
		tEncryptAlgoPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		if (PGPGUI.USE_EXTENTION) {
			tEncryptAlgoPanel.setBorder(new EmptyBorder(10,10,10,10));
		} else {
			tEncryptAlgoPanel.setBorder(new EmptyBorder(10,10,10,40));
		}
		
		mSignPanel = new JPanel();
		mSignPanel.add( new JLabel("Signing user"));
		mSignComboBox = new JComboBox<SigningUser>( getSignUsers() );	
		mSignComboBox.addActionListener(this);
		mSignPanel.add( mSignComboBox );
		tEncryptAlgoPanel.add(mSignPanel);
		
		tEncryptAlgoPanel.add( new JLabel("Encrypt Algo"));
		mEncryptAlgoComboBox = new JComboBox<String>( getEncryptionAlgorithms() );	
		mEncryptAlgoComboBox.addActionListener(this);
		tEncryptAlgoPanel.add( mEncryptAlgoComboBox );
		
		
		mAESKeyStrengthPanel = new JPanel( new FlowLayout());
		mAESKeyStrengthPanel.setBorder(new EmptyBorder(0, 15, 0, 0));
		mAESKeyStrengthPanel.add( new JLabel("AES Encryption"));
		mAESStrengthComboBox = new JComboBox<String>( mAESKeyStrength );	
		mAESKeyStrengthPanel.add( mAESStrengthComboBox );
		mAESKeyStrengthPanel.add( new JLabel("bits"));
		if (PGPGUI.USE_EXTENTION) {
			tEncryptAlgoPanel.add(mAESKeyStrengthPanel);
		}
		
		mSouthPanel.add( tEncryptAlgoPanel, BorderLayout.NORTH);
		
		
		JPanel mKeyPanel = new JPanel();
		mKeyPanel.setLayout(new FlowLayout());
		mKeyPanel.setBorder(new EmptyBorder(10,10,10,10));
		
		mKeyContainer = new KeyContainer(KeyType.PUBLIC, 775, 100, true, true );
		mKeyContainer.addTableModelListener(this);
		mKeyPanel.add(mKeyContainer);
		mSouthPanel.add( mKeyPanel, BorderLayout.CENTER );
		

			
		// Create Button Panel
		JPanel mButtonPanel = new JPanel();
		mSouthPanel.add(mButtonPanel, BorderLayout.SOUTH);
		
		
		mButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		{
			mEncryptBtn = new JButton("Encrypt");
			mEncryptBtn.setActionCommand("ENCRYPT");
			mEncryptBtn.setEnabled(false);
			mEncryptBtn.addActionListener(event -> { encryptMessage();}); 
			mButtonPanel.add(mEncryptBtn);
		}
		{
			mRestoreBtn = new JButton("Restore Text");
			mRestoreBtn.setActionCommand("RESTORE");
			mRestoreBtn.setEnabled(false);
			mRestoreBtn.addActionListener(event -> { restoreOrginalText();}); 
			mButtonPanel.add(mRestoreBtn);
		}
		{
			JButton tCancelButton = new JButton("Cancel");
			tCancelButton.setActionCommand("Cancel");
			tCancelButton.addActionListener( event-> { this.dispose();});
			mButtonPanel.add(tCancelButton);
			getRootPane().setDefaultButton(tCancelButton);
		}
		mButtonPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
	}

	@SuppressWarnings("static-access")
	@Override
	public void tableChanged(TableModelEvent e) {
		if (e.getType() == e.UPDATE) {
			if (mKeyContainer.anyUserSelected()) {
				mEncryptBtn.setEnabled(true);
			} else {
				mEncryptBtn.setEnabled(false);
			}
		}
	}
	
	private SigningUser[] getSignUsers() {
		try {
			List<KeyRingInterface> tKeyRings = GPGAdapter.getInstance().getSecretKeyRings();
			SigningUser[] tArr = new SigningUser[tKeyRings.size() + 1];
			tArr[0] = new SigningUser(null);
			for( int i = 0; i < tKeyRings.size(); i++ ) {
				tArr[i+1] =  new SigningUser((SecKeyRing) tKeyRings.get(i));
			}
			return tArr;
		}
		catch( Exception e ) {
			AlertMessage.showMessage(this, "Failed to retrieve secret keys: " + e.getMessage());
			SigningUser[] tArr = new SigningUser[1];
			tArr[0] = new SigningUser(null);
			return tArr;
		}
	}

	
		private String[] getEncryptionAlgorithms() {
			ArrayList<String> tAlgos = new ArrayList<String>();
			tAlgos.add(PGPGUI.ENCRYPT_ALGO_AES );
			tAlgos.add(PGPGUI.ENCRYPT_ALGO_CAST5 );
			tAlgos.add(PGPGUI.ENCRYPT_ALGO_BLOWFISH );
			tAlgos.add(PGPGUI.ENCRYPT_ALGO_CAMELLIA_256 );
			tAlgos.add(PGPGUI.ENCRYPT_ALGO_IDEA );
			tAlgos.add(PGPGUI.ENCRYPT_ALGO_TWOFISH );
			if (PGPGUI.USE_EXTENTION) {
				tAlgos.add(PGPGUI.ENCRYPT_ALGO_SNOWFLAKE );
			}
			return tAlgos.toArray(new String[0]);
		}
		

	private void restoreOrginalText() {
		mText.setText( mOrginalText );
	}
	
	private void encryptMessage() {
		ByteArrayOutputStream tEncryptedByteArrayOutputStream = null;
		 
		if (mText.getText().isEmpty()) {
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
			byte[] tInBytes = mText.getText().getBytes(StandardCharsets.UTF_8);
			PGPSecretKeyRing tSecretSignKeyRing = ( ((SigningUser)mSignComboBox.getSelectedItem()).mKeyRing == null) ? null : ((SigningUser) mSignComboBox.getSelectedItem()).mKeyRing.getSecretKeyRing();
			tEncryptedByteArrayOutputStream = GPGAdapter.getInstance().encryptMessage(tInBytes, tEncKeyRing.getPublicEncryptionKey(), tSecretSignKeyRing, this,  tAlgo, tKeyStrength);
		    mOrginalText = mText.getText();
		    mRestoreBtn.setEnabled(true);
		    mText.setText(tEncryptedByteArrayOutputStream.toString());
		} catch( Exception e) {
			AlertMessage.showMessage(this, "Failed to encrypt message: " + e.getMessage());
		}
	   
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == mEncryptAlgoComboBox) {
			String tAlgo = (String) mEncryptAlgoComboBox.getSelectedItem();
			if (tAlgo.compareTo("AES") != 0) {
				mAESStrengthComboBox.setEnabled(false);
			} else {
				mAESStrengthComboBox.setEnabled(true);
			}
		}	
	}
	
	@Override
	public char[] getPasswordForDecrypt( long pKeyId, String pUserId )
	{
		 PasswordDialog tPasswDialog = new PasswordDialog(this, "Signing Private Password", pUserId, pKeyId);
		 tPasswDialog.setVisible(true);
		 char[] tPassword = tPasswDialog.getPassword();
		 tPasswDialog.dispose();
		 return tPassword;
	}
	
	
	class SigningUser {
		SecKeyRing	mKeyRing;
		
		public SigningUser( SecKeyRing  pKeyRing) {
			mKeyRing = pKeyRing;
		}
		
		@Override
		public String toString() {
			if (mKeyRing == null) {
			  return "None";
			}
			
			return mKeyRing.getFirstUserId();
		}
	}


	@Override
	public Component getComponent() {
		return this;
	}
}

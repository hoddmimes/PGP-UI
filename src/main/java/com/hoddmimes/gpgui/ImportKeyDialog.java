package com.hoddmimes.gpgui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.TextField;
import java.io.ByteArrayInputStream;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;

import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;



public class ImportKeyDialog extends JDialog {

	private Object mPGPKeyObject = null;
	private final JTextArea mTextArea;
	private final JPanel mContentPanel = new JPanel();
	private final KeyAttributePanel mKeyAttrPanel;
	private final  JPanel mSouthPanel;
	private final JButton mImportButton;
	private final JButton mParseButton;
	



	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ImportKeyDialog dialog = new ImportKeyDialog();
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
	public ImportKeyDialog() {
		//setBounds(100, 100, 650, 400);
		GPGAdapter.setAppIcon( this, this );
		setTitle("Import Key");
		
		getContentPane().setLayout(new BorderLayout());
		mContentPanel.setLayout(new FlowLayout());
		mContentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(mContentPanel, BorderLayout.CENTER);
		
		mTextArea = new JTextArea();
		mTextArea.setColumns(55);
		mTextArea.setRows(18);
		mTextArea.setFont( new Font("Arial", Font.PLAIN, 12));
		mTextArea.setEditable(true);
		
		JScrollPane tScrollPane = new JScrollPane(mTextArea);
		mContentPanel.add(tScrollPane);
		

		
		/**
		 * Create Key Attribute panel
		 */
		mKeyAttrPanel = new KeyAttributePanel();
		
		

		 
		
	    /**
	     * Create button panel
	     */
		JPanel tButtonPane = new JPanel();
		tButtonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
			
		mParseButton = new JButton("Parse");
		mParseButton.addActionListener( event-> parseKey());
		tButtonPane.add(mParseButton);
		getRootPane().setDefaultButton(mParseButton);

		mImportButton = new JButton("Import");
		mImportButton.setActionCommand("OK");
		tButtonPane.add(mImportButton);
		mImportButton.setEnabled(false);
		mImportButton.addActionListener(event -> addAndSaveKeyRing());

		JButton tCancelButton = new JButton("Cancel");
		tCancelButton.setActionCommand("Cancel");
		tButtonPane.add(tCancelButton);
		tCancelButton.addActionListener(event-> this.dispose());
		
		/**
		 * Create south panel
		 */
		mSouthPanel = new JPanel();
		mSouthPanel.setLayout(new BorderLayout());

		
		/**
		 * Add Pannels to Windows
		 */
		mSouthPanel.add(tButtonPane, BorderLayout.SOUTH);
		
		getContentPane().add(mSouthPanel, BorderLayout.SOUTH);
	}
	
	

	
	private void resetImport() {
		mKeyAttrPanel.reset();
		mImportButton.setEnabled(false);
		mSouthPanel.remove(mKeyAttrPanel);
		this.revalidate();
	}
	
	private void addAndSaveKeyRing() {
		
		try {
			if (mPGPKeyObject instanceof PGPPublicKeyRing) {
				Auxx.addAndSavePublicKeys((PGPPublicKeyRing) mPGPKeyObject, mKeyAttrPanel.getRespositoryType());
				AlertMessage.showMessage(this, "Key successfully imported to Gnu_PG key ring file");
			} else {
				Auxx.addAndSaveSecretKeys((PGPSecretKeyRing) mPGPKeyObject, mKeyAttrPanel.getRespositoryType() );
				AlertMessage.showMessage(this, "Key successfully imported to PGPGUI key ring file");
			}
			resetImport();
		}
		catch( Exception e) {
			String tPrefixString = (mPGPKeyObject instanceof PGPPublicKeyRing) ? new String("failed to save public key; ") : new String("failed to save secret key; ");
			AlertMessage.showMessage(this, tPrefixString + e.getMessage());
			resetImport();
		}
	}
	
	private void parseSecretKeyRing(PGPSecretKeyRing pKeyRing)
	{

		Iterator<PGPPublicKey> tKeyItr = pKeyRing.getPublicKeys();
		while ( tKeyItr.hasNext()) {
			PGPPublicKey tKey = tKeyItr.next();
			parsePublicKey( tKey );
		}
		mKeyAttrPanel.addKeyRingSelection( );
	}
	
	private void parsePublicKeyRing(PGPPublicKeyRing pKeyRing)
	{
		Iterator<PGPPublicKey> tPubKeyItr = pKeyRing.getPublicKeys();
		while ( tPubKeyItr.hasNext()) {
			PGPPublicKey tKey = tPubKeyItr.next();
			parsePublicKey( tKey );
		}
		mKeyAttrPanel.addKeyRingSelection( );
	}
	
	
	
	
	private void parsePublicKey(PGPPublicKey pPubKey) {
		String tUserId = null;

		if (pPubKey.isMasterKey()) {
			Iterator<byte[]> tUserIdItr = pPubKey.getRawUserIDs();
			while (tUserIdItr.hasNext()) {
				byte[] tByteVector = tUserIdItr.next();
				try {
					tUserId = new String(tByteVector, StandardCharsets.UTF_8);
					mKeyAttrPanel.addUserId(tUserId);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			String tKeyAlgo = GPGAdapter.getKeyAlgorithm(pPubKey.getAlgorithm());
			mKeyAttrPanel.addKey(true, tKeyAlgo, pPubKey.getBitStrength(), pPubKey.getKeyID());
		} else {
			String tKeyAlgo = GPGAdapter.getKeyAlgorithm(pPubKey.getAlgorithm());
			mKeyAttrPanel.addKey(false, tKeyAlgo, pPubKey.getBitStrength(), pPubKey.getKeyID());
		}
		
	}

	
	
	
	
	
	private void parseKey() {
		this.resetImport();
		ByteArrayInputStream tByteArrayInputStream = new ByteArrayInputStream( mTextArea.getText().getBytes());
		try {
			
			InputStream tInStream = PGPUtil.getDecoderStream(tByteArrayInputStream);
			PGPObjectFactory pgpF = new PGPObjectFactory(tInStream, new JcaKeyFingerprintCalculator());
		    mPGPKeyObject = pgpF.nextObject();
		    //System.out.println("KeyClass: " + mPGPKeyObject.getClass().getSimpleName());
		    
		    if (mPGPKeyObject instanceof PGPPublicKeyRing) {
		    	parsePublicKeyRing((PGPPublicKeyRing) mPGPKeyObject);  	
		    } else if (mPGPKeyObject instanceof PGPSecretKeyRing) {
		    	parseSecretKeyRing((PGPSecretKeyRing) mPGPKeyObject);  
		    } else {
		    	AlertMessage.showMessage(this, "Invalid Key object (" + mPGPKeyObject.getClass().getSimpleName() + "), can not be imported.", 3000);
		    	return;
		    }
		    mSouthPanel.add(mKeyAttrPanel,  BorderLayout.CENTER);
	    	this.mImportButton.setEnabled(true);
	    	this.mParseButton.setEnabled(false);
	    	this.getRootPane().setDefaultButton(mImportButton);
	    	this.mTextArea.setRows(9);
	    	this.revalidate();
		}	
		catch( Exception e ) {
			AlertMessage.showMessage(this, "Failed to parse key \"" + e.getMessage() + "\"");
			mTextArea.setText("");
			resetImport();
		}
	}
	
	
	class KeyAttributePanel extends JPanel {
		 int mRowIndex;
		 JRadioButton mGnuPgSelection;
		 JRadioButton mGPGUISelection;

		KeyAttributePanel() {
			super();
			 mGnuPgSelection = new  JRadioButton("Add Key to GnuPG");
			 mGnuPgSelection.setToolTipText("Add key to GnuPG key ring files");
			 mGPGUISelection = new  JRadioButton("Add Key to PGPGUI");
			 mGPGUISelection.setToolTipText("Add key to PGPGUI key ring files");
			 mGPGUISelection.setSelected(true);
			 ButtonGroup tButtonGroup = new ButtonGroup();
			tButtonGroup.add(mGnuPgSelection);
			tButtonGroup.add(mGPGUISelection);
			 
			this.setBorder(new EmptyBorder(10, 0, 5, 0));
			GridBagLayout tGridBagLayout = new GridBagLayout();
			this.setLayout(tGridBagLayout);
			mRowIndex = 0;
			
			
		}

		void addUserId(String pUserId) {
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = mRowIndex;
			this.add(new JLabel("User Id"), c);
			c.gridx = 1;
			c.gridy = mRowIndex;
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.anchor = GridBagConstraints.WEST;
			TextField tUserId = createTextField(pUserId, 40);
			this.add(tUserId, c);
			mRowIndex++;
		}
		
		void addKeyRingSelection( ) {
			FlowLayout tFlowLayout = new FlowLayout();
			tFlowLayout.setAlignment(FlowLayout.CENTER);
			JPanel tPanel = new JPanel(tFlowLayout);
			 
			//tPanel.setBorder(BorderFactory.createCompoundBorder(new EtchedBorder(EtchedBorder.RAISED, Color.GRAY, Color.LIGHT_GRAY), new EmptyBorder(5, 10, 5, 10)));
			tPanel.add(mGnuPgSelection);
			tPanel.add(mGPGUISelection);
			
			if (!Auxx.ifKeyRingFileExists(Settings.getInstance().getPGPPublicKeyRingFilename())) {
				mGnuPgSelection.setEnabled(false);
			}
			
			GridBagConstraints c = new GridBagConstraints();
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.insets = new Insets(10, 15, 0, 0);
			c.gridx = 1;
			c.gridy = mRowIndex++;
			c.anchor = GridBagConstraints.WEST;
			this.add(tPanel, c);
		}

		void reset() {
			this.removeAll();
			mRowIndex = 0;
		}

		void addKey(boolean pMasterKey, String pKeyAlgo, int pKeyStrength, long pKeyId) {
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = mRowIndex;
			JLabel tKeyTypeLabel = (pMasterKey) ? new JLabel("MasterKey")
					: new JLabel("SubKey");
			this.add(tKeyTypeLabel, c);

			
			c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = mRowIndex;
			c.anchor = GridBagConstraints.WEST;
			c.insets = new Insets(0, 5, 0, 0);
			this.add(new JLabel("Key Algo"), c);
			
			c = new GridBagConstraints();
			c.gridx = 2;
			c.gridy = mRowIndex;
			c.anchor = GridBagConstraints.WEST;
			c.insets = new Insets(0, 5, 0, 0);
			this.add(createTextField(pKeyAlgo, 10), c);
			
			if (pKeyStrength > 0) {
				c = new GridBagConstraints();
				c.gridx = 3;
				c.gridy = mRowIndex;
				c.anchor = GridBagConstraints.WEST;
				c.insets = new Insets(0, 5, 0, 0);
				this.add(new JLabel("Key Size"), c);

				c = new GridBagConstraints();
				c.gridx = 4;
				c.gridy = mRowIndex;
				c.anchor = GridBagConstraints.WEST;
				c.insets = new Insets(0, 5, 0, 0);
				this.add(createTextField(String.valueOf(pKeyStrength), 5), c);
			}
			c = new GridBagConstraints();
			c.gridx = 5;
			c.gridy = mRowIndex;
			c.anchor = GridBagConstraints.WEST;
			c.insets = new Insets(0, 5, 0, 0);
			this.add(new JLabel("Key Id"), c);

			c = new GridBagConstraints();
			c.gridx = 6;
			c.gridy = mRowIndex;
			c.anchor = GridBagConstraints.WEST;
			c.insets = new Insets(0, 5, 0, 0);
			this.add(createTextField(Long.toHexString(pKeyId), 14), c);
			mRowIndex++;
		}

		int getRespositoryType() {
			if (mGnuPgSelection.isSelected()) {
				return GPGAdapter.GNUPG_REPOSITORY_INT;
			} else {
				return GPGAdapter.GPGUI_REPOSITORY_INT;
			}
		}
		private TextField createTextField(String pText, int pColumns) {
			TextField tf = new TextField();
			tf.setText(pText);
			tf.setEditable(false);
			tf.setColumns(pColumns);
			return tf;
		}
	}
}

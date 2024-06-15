package com.hoddmimes.gpgui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.bcpg.sig.Features;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.crypto.generators.DSAKeyPairGenerator;
import org.bouncycastle.crypto.generators.DSAParametersGenerator;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.DSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ElGamalParameterSpec;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPKeyRingGenerator;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.operator.PBESecretKeyEncryptor;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.bc.BcPGPKeyPair;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyPair;

public class CreateKeyDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;

	private static final String ALGO_RSA = "RSA";
    private static final String ALGO_DSA_ELGAM = "DSA/ELGAM";
    
	private String[] mKeyAlgos = new String[] {ALGO_RSA, ALGO_DSA_ELGAM };
	private String[] mKeyStrength = new String[] {"128","256","512","1024","2048","3070", "4096","4608","6144","8192"};
	
	private final JPanel contentPanel = new JPanel();
	private JTextField mMailAddrTextField;
	private JComboBox<String> mAlgoComboBox;
	private JPasswordField mPasswordTextField1;
	private JPasswordField mPasswordTextField2;
	private  JComboBox<String>  mStrengthComboBox;
	
	private JRadioButton mGnuPgSelection;
	private JRadioButton mGPGUISelection;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			CreateKeyDialog dialog = new CreateKeyDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public CreateKeyDialog() {
		GPGAdapter.setAppIcon( this, this );
		setBounds(100, 100, 500, 250);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		setTitle("Create (Encryption) Key Pair");
		
		JPanel tParameterPanel = new JPanel();
		Border tBorder = tParameterPanel.getBorder();
		Border tMargin = new EmptyBorder(10, 10, 10, 10);
		tParameterPanel.setBorder(new CompoundBorder(tBorder, tMargin));
		
		GridBagLayout tPanelGridBagLayout = new GridBagLayout();
		tPanelGridBagLayout.columnWidths = new int[] { 86, 86, 0 };
		tPanelGridBagLayout.rowHeights = new int[] { 20, 20, 20, 20, 20, 0 };
		tPanelGridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		tPanelGridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		tParameterPanel.setLayout(tPanelGridBagLayout);
		

		
		mMailAddrTextField = new JTextField();
		mMailAddrTextField.setColumns(30);
		addKeyParameterToPanel( tParameterPanel, "User Id", mMailAddrTextField, 0);
		
		mAlgoComboBox = new JComboBox<String>( mKeyAlgos );
		mAlgoComboBox.setSelectedIndex(0);
		//mAlgoComboBox.addActionListener( this );
		addKeyParameterToPanel( tParameterPanel, "Key Algorithm", mAlgoComboBox, 1);
		
		mStrengthComboBox = new JComboBox<String>( mKeyStrength );
		mStrengthComboBox.setSelectedIndex(4);
		//mStrengthComboBox.addActionListener( this );
		addKeyParameterToPanel( tParameterPanel, "Key Strength", mStrengthComboBox, 2);
		
		mPasswordTextField1 = new JPasswordField();
		mPasswordTextField1.setColumns(30);
		addKeyParameterToPanel( tParameterPanel, "Password", mPasswordTextField1, 3 );
		
		mPasswordTextField2 = new JPasswordField();
		mPasswordTextField2.setColumns(30);
		addKeyParameterToPanel( tParameterPanel, "Verify Password", mPasswordTextField2, 4);
		
		JCheckBox tVisiblePasswordCheckBox = new JCheckBox("Visible password");
		tVisiblePasswordCheckBox.addChangeListener( new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (tVisiblePasswordCheckBox.isSelected()) {
					mPasswordTextField1.setEchoChar('\u0000');
					mPasswordTextField2.setEchoChar('\u0000');
				} else {
					mPasswordTextField1.setEchoChar('*');
					mPasswordTextField2.setEchoChar('*');
				}
			}
		});
		
		 mGnuPgSelection = new  JRadioButton("Add Key to GnuPG");
		 mGnuPgSelection.setToolTipText("Add key to GnuPG key ring files");
		 mGPGUISelection = new  JRadioButton("Add Key to PGPGUI");
		 mGPGUISelection.setToolTipText("Add key to PGPGUI key ring files");
		 mGPGUISelection.setSelected(true);
		 
		 addOptionsToPanel( tParameterPanel, tVisiblePasswordCheckBox, mGnuPgSelection, mGPGUISelection, 5 );
		 
		
		getContentPane().add(tParameterPanel, BorderLayout.WEST);
		
		
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
			
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
		cancelButton.addActionListener( event-> {  dispose(); } );
		
		JButton okButton = new JButton("Create Key");
		okButton.setActionCommand("Create Key");
		buttonPane.add(okButton);
		okButton.addActionListener( event-> {  createKey();  } );
		
		getRootPane().setDefaultButton(okButton);
			
		}
	
	private void addOptionsToPanel(  JPanel pParameterWrapper, JCheckBox pVisiblePasswordCheckBox, 
			                                                        JRadioButton pGnuPgSelection, JRadioButton pGPGUISelection, int pRow  )
	{
		ButtonGroup tButtonGroup = new ButtonGroup();
		tButtonGroup.add(pGnuPgSelection);
		tButtonGroup.add(pGPGUISelection);
		
		FlowLayout tFlowLayout = new FlowLayout();
		tFlowLayout.setAlignment(FlowLayout.CENTER);
		JPanel tPanel = new JPanel(tFlowLayout);
		//tPanel.setBorder(BorderFactory.createCompoundBorder(new EtchedBorder(EtchedBorder.RAISED, Color.GRAY, Color.LIGHT_GRAY), new EmptyBorder(5, 10, 5, 10)));
		tPanel.add(pGnuPgSelection);
		tPanel.add(pGPGUISelection);
		
		if (!Auxx.ifKeyRingFileExists(Settings.getInstance().getPGPPublicKeyRingFilename())) {
			pGnuPgSelection.setEnabled(false);
		}
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridx = 1;
		c.gridy = pRow;
		c.anchor = GridBagConstraints.WEST;
		pParameterWrapper.add(pVisiblePasswordCheckBox, c);
		
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(10, 15, 0, 0);
		c.gridx = 1;
		c.gridy = pRow+1;
		c.anchor = GridBagConstraints.WEST;
		pParameterWrapper.add(tPanel, c);
	}
		
	private void addKeyParameterToPanel(JPanel pParameterWrapper, String pLabel, Component pInputParameter, int pRow) {
	
		GridBagConstraints c = null;
		
		JLabel tLabel = new JLabel(pLabel);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = pRow;
		pParameterWrapper.add(tLabel, c);
		
		
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = pRow;
		c.anchor = GridBagConstraints.WEST;
		pParameterWrapper.add(pInputParameter, c);
	}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource().equals(mAlgoComboBox)) {
			   mStrengthComboBox.setModel(new javax.swing.DefaultComboBoxModel( mKeyStrength));
			   mStrengthComboBox.setSelectedIndex(4);
			}
		}
		
		@SuppressWarnings("unused")
		private boolean validateMailAddress( String pAddress ) {
			Matcher tMatcher = null;
			String[] tArr = pAddress.split(" ");
			String regex = "^[<\\[A-Za-z0-9+_.-]+@[>\\]A-Za-z0-9+_.-]+$";
			 
			Pattern tPattern = Pattern.compile(regex);
			
			for( int i = 0;  i < tArr.length; i++) {
				tMatcher = tPattern.matcher(tArr[i]);
				if (tMatcher.matches()) {
					return true;
				}
			}
			
			AlertMessage.showMessage(this, "Incomplete or Invalid mail address");
			return false;
		}
		
		private boolean validatePasswords() {
			char[] p1 = mPasswordTextField1.getPassword();
			char [] p2 = mPasswordTextField2.getPassword();
			
			if ((p1 == null) || (p2 == null)) {
				 AlertMessage.showMessage(this,"Password too short, must be at least 8 characters");
				return false;
			}
			
			if ((p1.length < 8) || (p2.length < 8)) {
				 AlertMessage.showMessage(this,"Password too short, must be at least 8 characters");
				  return false;
			}
			
			if (p1.length != p2.length)  {
				AlertMessage.showMessage(this, "password are not the same");
				  return false;
			}
			
			for( int i = 0; i < p1.length; i++ ) {
				if (p1[i] != p2[i]) {
					AlertMessage.showMessage(this, "password are not the same");
					 return false;
				}
			}
			
			return true;
		}
		
	private void createKey() {
		char[] tPassword = mPasswordTextField1.getPassword();
		String tMailAddress = mMailAddrTextField.getText();
		String tAlgo = (String) mAlgoComboBox.getSelectedItem();
		String tStrenthValue = (String) mStrengthComboBox.getSelectedItem();

		if (tMailAddress.isEmpty()) {
			AlertMessage.showMessage(this, "User id must not be empty");
			return;
		}
		
		if (!validatePasswords()) {
			return;
		}

		PGPKeyRingGenerator tKeyRing = null;
		try {
			if (tAlgo.compareTo(ALGO_RSA) == 0) {
				tKeyRing = createRSAKey(tMailAddress,  tPassword, Integer.parseInt(tStrenthValue));
			} else if (tAlgo.compareTo(ALGO_DSA_ELGAM) == 0) {
				tKeyRing = createDSAELGAMKey(tMailAddress,  tPassword, Integer.parseInt(tStrenthValue));
			} else {
				AlertMessage.showMessage(this, "Implementation for key algorithm \"" + tAlgo + " \" is missing.");
				return;
			}
		} catch (Exception e) {
			AlertMessage.showMessage(this, "Failed  to create " + tAlgo + " key pair \n " + e.getMessage());
			return;
		}
	
		try {
			int tRespType = (mGnuPgSelection.isSelected()) ? GPGAdapter.GNUPG_REPOSITORY_INT : GPGAdapter.GPGUI_REPOSITORY_INT;
			Auxx.addAndSaveSecretKeys(tKeyRing, tRespType);
			Auxx.addAndSavePublicKeys(tKeyRing, tRespType);
			AlertMessage.showMessage(this,  "Successfully created " + tAlgo + " key for " + tMailAddress);
			this.dispose();
		}
		catch(Exception se) {
			AlertMessage.showMessage(this, "Failed to save keys \n" + se.getMessage());
		}
	}
	
	
	public  PGPKeyRingGenerator createECDHKey( String pMailAddress, char[] pPassword , String pCurveName) throws Exception
	{
		SecureRandomSHA256 tSecureRandom = new SecureRandomSHA256();

		

		// Create signing key
		ECParameterSpec tECParameters = ECNamedCurveTable.getParameterSpec(pCurveName);
		KeyPairGenerator tKeyGenerator = KeyPairGenerator.getInstance("ECDH", "BC");
		tKeyGenerator.initialize(tECParameters, tSecureRandom);
	
		
		PGPKeyPair kp_sign = new JcaPGPKeyPair(PGPPublicKey.ECDH, tKeyGenerator.generateKeyPair(), new Date());
		PGPKeyPair kp_enc = new JcaPGPKeyPair(PGPPublicKey.ECDH, tKeyGenerator.generateKeyPair(), new Date());
		
		
		return createKeyRingGenerator( kp_sign, kp_enc, pMailAddress, pPassword );
		
	}
	
	
	public  PGPKeyRingGenerator createDSAELGAMKey( String pMailAddress, char[] pPassword , int tKeyStrength) throws Exception
	{

		SecureRandomSHA256 tSecureRandom = new SecureRandomSHA256();
		
		BigInteger g = BigInteger.probablePrime(tKeyStrength, tSecureRandom);
		BigInteger p = BigInteger.probablePrime(tKeyStrength, tSecureRandom);
		
		/*
		 * Create a DSA keypair for signing the king ring with 
		 */
		
		DSAParametersGenerator tDsaParamGenerator = new DSAParametersGenerator();
		tDsaParamGenerator.init(tKeyStrength, 80, tSecureRandom);
		DSAKeyGenerationParameters tDsaKeyParamGenerator = new DSAKeyGenerationParameters( tSecureRandom, tDsaParamGenerator.generateParameters());
		DSAKeyPairGenerator  tDsaKeyPairGenerator = new DSAKeyPairGenerator();
		tDsaKeyPairGenerator.init(tDsaKeyParamGenerator);
		PGPKeyPair tDsaKeyPair = new BcPGPKeyPair (PGPPublicKey.DSA, tDsaKeyPairGenerator.generateKeyPair(), new Date());
		
		/*
		 * Create a ELGAMAL key pair for encryption
		 */
		
		KeyPairGenerator tElGamKeyPairGenerator = KeyPairGenerator.getInstance("ElGamal", "BC");
		ElGamalParameterSpec tElGamParams = new ElGamalParameterSpec(p, g);
		tElGamKeyPairGenerator.initialize(tElGamParams);
        KeyPair tKeyPair = tElGamKeyPairGenerator.generateKeyPair();
		PGPKeyPair tElGamKeyPair = new JcaPGPKeyPair(PGPPublicKey.ELGAMAL_ENCRYPT, tKeyPair, new Date());
		

		return createKeyRingGenerator( tDsaKeyPair, tElGamKeyPair, pMailAddress, pPassword );
	}
	
	
	
	
		public  PGPKeyRingGenerator createRSAKey( String pMailAddress, char[] pPassword , int tKeyStrength) throws Exception
		{
			// This object generates individual key-pairs.
			RSAKeyPairGenerator  tKeyPairGenerator = new RSAKeyPairGenerator();

			// Boilerplate RSA parameters, no need to change anything
			// except for the RSA key-size (2048). You can use whatever
			// key-size makes sense for you -- 4096, etc.
			SecureRandomSHA256 tSecureRandom = new SecureRandomSHA256();
			RSAKeyGenerationParameters tKeyGenParameters = new RSAKeyGenerationParameters( BigInteger.valueOf(87381), tSecureRandom, tKeyStrength, 144);
			tKeyPairGenerator.init (tKeyGenParameters);

			// First create the master (signing) key with the generator.
			PGPKeyPair rsakp_sign = new BcPGPKeyPair (PGPPublicKey.RSA_SIGN, tKeyPairGenerator.generateKeyPair(), new Date());
	    
			// Then an encryption subkey.
			PGPKeyPair rsakp_enc = new BcPGPKeyPair(PGPPublicKey.RSA_ENCRYPT, tKeyPairGenerator.generateKeyPair(), new Date());

			 return createKeyRingGenerator( rsakp_sign, rsakp_enc, pMailAddress, pPassword );
		}
		 
		
		
		private PGPKeyRingGenerator createKeyRingGenerator(  PGPKeyPair pSignKey, PGPKeyPair pEncKey,  String pMailAddress, char[] pPassword )  throws Exception
		{
			
			int tS2kcount = 0xc7;
			// Add a self-signature on the id
			PGPSignatureSubpacketGenerator signhashgen = new PGPSignatureSubpacketGenerator();
	    
			// Add signed metadata on the signature.
			// 1) Declare its purpose
			signhashgen.setKeyFlags (false, KeyFlags.SIGN_DATA|KeyFlags.CERTIFY_OTHER);
			// 2) Set preferences for secondary crypto algorithms to use
			//    when sending messages to this key.
			signhashgen.setPreferredSymmetricAlgorithms (false, new int[] {
	            SymmetricKeyAlgorithmTags.AES_256,
	            SymmetricKeyAlgorithmTags.AES_192,
	            SymmetricKeyAlgorithmTags.AES_128
	        });
	    
			signhashgen.setPreferredHashAlgorithms
	        (false, new int[] {
	            HashAlgorithmTags.SHA256,
	            HashAlgorithmTags.SHA1,
	            HashAlgorithmTags.SHA384,
	            HashAlgorithmTags.SHA512,
	            HashAlgorithmTags.SHA224,
	        });
			
			// 3) Request senders add additional checksums to the
			//    message (useful when verifying unsigned messages.)
			signhashgen.setFeature (false, Features.FEATURE_MODIFICATION_DETECTION);

			// Create a signature on the encryption subkey.
			PGPSignatureSubpacketGenerator enchashgen = new PGPSignatureSubpacketGenerator();
	    
			// Add metadata to declare its purpose
			enchashgen.setKeyFlags(true, KeyFlags.ENCRYPT_COMMS|KeyFlags.ENCRYPT_STORAGE);
		

			// Objects used to encrypt the secret key.
			PGPDigestCalculator sha1Calc = new BcPGPDigestCalculatorProvider().get(HashAlgorithmTags.SHA1);
			PGPDigestCalculator sha256Calc = new BcPGPDigestCalculatorProvider().get(HashAlgorithmTags.SHA256);

			// bcpg 1.48 exposes this API that includes s2kcount. Earlier
			// versions use a default of 0x60.

			PBESecretKeyEncryptor pske = (new BcPBESecretKeyEncryptorBuilder (PGPEncryptedData.AES_256, sha256Calc, tS2kcount)).build(pPassword);
	    
		    // Finally, create the keyring itself. The constructor
		    // takes parameters that allow it to generate the self
		    // signature.
			PGPKeyRingGenerator tKeyRingGenerator = new PGPKeyRingGenerator(PGPSignature.POSITIVE_CERTIFICATION, pSignKey, pMailAddress, sha1Calc, signhashgen.generate(), null,
	         new BcPGPContentSignerBuilder(pSignKey.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA1), pske);

			// Add our encryption subkey, together with its signature.
			tKeyRingGenerator.addSubKey(pEncKey, enchashgen.generate(), null);
			return tKeyRingGenerator;
		}

}

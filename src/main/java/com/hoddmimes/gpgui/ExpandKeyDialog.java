package com.hoddmimes.gpgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;

import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class ExpandKeyDialog extends JDialog {

	private final JPanel mContentPanel = new JPanel();
	private int mRow;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ExpandKeyDialog dialog = new ExpandKeyDialog(null, 100, 100);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ExpandKeyDialog(KeyRingInterface pKeyRingInterface, int xPos,int yPos) {
		setBounds(xPos, yPos, 350, 400);
		GPGAdapter.setAppIcon( this, this );

		getContentPane().setLayout(new BorderLayout());
		mContentPanel.setLayout(new FlowLayout());
		mContentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		JScrollPane mScrollPane = new JScrollPane();
		


		mContentPanel.add(mScrollPane);
		JPanel mKeyPanel = new JPanel();
		GridBagLayout tGridBagLayout = new GridBagLayout();

		mKeyPanel.setLayout(tGridBagLayout);
		mKeyPanel.setBackground(Color.white);
		mScrollPane.setViewportView(mKeyPanel);
		mScrollPane.getViewport().setBackground(Color.white);

		if (pKeyRingInterface instanceof PubKeyRingInterface) {
			layoutPubKey((PubKeyRingInterface) pKeyRingInterface, mKeyPanel);
		} else if (pKeyRingInterface instanceof SecKeyRingInterface) {
			layoutSecKey((SecKeyRingInterface) pKeyRingInterface, mKeyPanel);
		}
		
		mScrollPane.getViewport().setPreferredSize(new Dimension(500, 450));
		getContentPane().add(mScrollPane, BorderLayout.CENTER);

		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				okButton.addActionListener(event -> this.dispose());
				buttonPane.add(okButton);
			}
		}
		this.pack();
	}
	
	
	
	
	private void layoutPubKey(PubKeyRingInterface pKeyRingInterface, JPanel pKeyPanel) {
		
		mRow = 0;
		Iterator<PGPPublicKey> tKeyItr = pKeyRingInterface.getPublicKeyRing().getPublicKeys();
	
		this.setTitle("Public Key (" + Long.toHexString(pKeyRingInterface.getPublicKeyRing().getPublicKey().getKeyID()) + ")");
		
		// Add Key Repository 
		JLabel tLabel = new JLabel("Key Repository");
		tLabel.setFont(new Font("Arial", Font.BOLD, 14));
		addParameterLabel(mRow, 0, pKeyPanel, tLabel, new Insets(20, 5, 20, 10));
		
		JTextField tTextField = createTextField(pKeyRingInterface.getKeyRingRepositoryName(), SwingConstants.CENTER);
		tTextField.setFont(new Font("Arial", Font.BOLD, 14));
		tTextField.setHorizontalAlignment(SwingConstants.CENTER);
		
		addParameterValue(mRow, 1, pKeyPanel, tTextField, new Insets(20, 10, 20, 10));
		mRow++;
		
		while(tKeyItr.hasNext()) {
			addPublicKey( pKeyPanel, tKeyItr.next(), true);
		}
	}

	private void layoutSecKey(SecKeyRingInterface pKeyRingInterface, JPanel pKeyPanel) {
		mRow = 0;
		Iterator<PGPSecretKey> tKeyItr = pKeyRingInterface.getSecretKeyRing().getSecretKeys();
	
		this.setTitle("Secret Key (" + Long.toHexString(pKeyRingInterface.getSecretKeyRing().getSecretKey().getKeyID()) + ")");
		
		// Add Key Repository 
		JLabel tLabel = new JLabel("Key Repository");
		tLabel.setFont(new Font("Arial", Font.BOLD, 14));
		addParameterLabel(mRow, 0, pKeyPanel, tLabel, new Insets(20, 5, 20, 10));
		
		JTextField tTextField = createTextField(pKeyRingInterface.getKeyRingRepositoryName(), SwingConstants.CENTER);
		tTextField.setFont(new Font("Arial", Font.BOLD, 14));
		tTextField.setHorizontalAlignment(SwingConstants.CENTER);
		
		addParameterValue(mRow, 1, pKeyPanel, tTextField, new Insets(20, 10, 20, 10));
		mRow++;
		
		while(tKeyItr.hasNext()) {
			addSecretKey( pKeyPanel, tKeyItr.next());
		}
	}
	
	
private void addSecretKey( JPanel pPanel, PGPSecretKey pKey ) {
		
		Insets tDefLabelInsets = (pKey.isMasterKey()) ? new Insets(2, 10, 2, 10) : new Insets(2, 40, 2, 10); //top, left, bottom, right
		Insets tDefValueInsets = new Insets(2, 10, 2, 10); //top, left, bottom, right
		
	
		
		// Is Master Key
		addParameterLabel(mRow, 0, pPanel, new JLabel("Key"), tDefLabelInsets);  
		String tKeyType = (pKey.isMasterKey()) ? "MasterKey" : "SubKey";
		addParameterValue(mRow, 1, pPanel, createTextField(tKeyType, SwingConstants.CENTER), tDefValueInsets);
		mRow++;
		// User Is's
		Iterator<String> tUserItr = pKey.getUserIDs();
		while( tUserItr.hasNext()) {
			addParameterLabel(mRow, 0, pPanel, new JLabel("User Id"), tDefLabelInsets);
			addParameterValue(mRow, 1, pPanel, createTextField(tUserItr.next(), SwingConstants.CENTER), tDefValueInsets);
			mRow++;
		}
		// Key Id 
		addParameterLabel(mRow, 0, pPanel, new JLabel("Key Id"), tDefLabelInsets );
		addParameterValue(mRow, 1, pPanel, createTextField(Long.toHexString(pKey.getKeyID()), SwingConstants.RIGHT), tDefValueInsets);
		mRow++;
		
		// Is Signing Key
		addParameterLabel(mRow, 0, pPanel, new JLabel("Signing Key"), tDefLabelInsets);
		addParameterValue(mRow, 1, pPanel, createTextField(Boolean.toString(pKey.isSigningKey()), SwingConstants.CENTER), tDefValueInsets);
		mRow++;
		
		// Key Algorithm
		addParameterLabel(mRow, 0, pPanel, new JLabel("Key Algorithm"), tDefLabelInsets);
		addParameterValue(mRow, 1, pPanel, createTextField(GPGAdapter.getKeyAlgorithm(pKey.getKeyEncryptionAlgorithm()), SwingConstants.CENTER), tDefValueInsets);
		mRow++;
		
		if (pKey.getPublicKey() != null) {
			addPublicKey( pPanel, pKey.getPublicKey(), false);
		}
		
		
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = mRow++;
		c.insets = new Insets(10, 0, 10, 0);
		pPanel.add( new JLabel(" "), c);
	
		
		
		
	}
	
	
	
	private void addPublicKey( JPanel pPanel, PGPPublicKey pKey, boolean pIsTruePublicKey ) {
		
		Insets tDefLabelInsets = (pKey.isMasterKey() && (pIsTruePublicKey)) ? new Insets(2, 10, 2, 10) : new Insets(2, 40, 2, 10); //top, left, bottom, right
		Insets tDefValueInsets = (pKey.isMasterKey() && (pIsTruePublicKey)) ? new Insets(2, 10, 2, 10) :  new Insets(2, 30, 2, 10); //top, left, bottom, right
		
	
		
		// Is Master Key
		String tKeyLabelStr = (pIsTruePublicKey) ? "Key" : "Public Key";
		addParameterLabel(mRow, 0, pPanel, new JLabel(tKeyLabelStr), tDefLabelInsets);  
		String tKeyType = (pKey.isMasterKey()) ? "MasterKey" : "SubKey";
		addParameterValue(mRow, 1, pPanel, createTextField(tKeyType, SwingConstants.CENTER), tDefValueInsets);
		mRow++;
		// User Is's
		Iterator<String> tUserItr = pKey.getUserIDs();
		while( tUserItr.hasNext()) {
			addParameterLabel(mRow, 0, pPanel, new JLabel("User Id"), tDefLabelInsets);
			addParameterValue(mRow, 1, pPanel, createTextField(tUserItr.next(), SwingConstants.CENTER), tDefValueInsets);
			mRow++;
		}
		// Key Id 
		addParameterLabel(mRow, 0, pPanel, new JLabel("Key Id"), tDefLabelInsets );
		addParameterValue(mRow, 1, pPanel, createTextField(Long.toHexString(pKey.getKeyID()), SwingConstants.RIGHT), tDefValueInsets);
		mRow++;
		
		// Is Encryption Key
		addParameterLabel(mRow, 0, pPanel, new JLabel("Encryption Key"), tDefLabelInsets);
		addParameterValue(mRow, 1, pPanel, createTextField(Boolean.toString(pKey.isEncryptionKey()), SwingConstants.CENTER), tDefValueInsets);
		mRow++;
		
		// Key Algorithm
		addParameterLabel(mRow, 0, pPanel, new JLabel("Key Algorithm"), tDefLabelInsets);
		addParameterValue(mRow, 1, pPanel, createTextField(GPGAdapter.getKeyAlgorithm(pKey.getAlgorithm()), SwingConstants.CENTER), tDefValueInsets);
		mRow++;
		
		// Key Bit Strength
		addParameterLabel(mRow, 0, pPanel, new JLabel("Key Strength"), tDefLabelInsets);
		addParameterValue(mRow, 1, pPanel, createTextField(String.valueOf(pKey.getBitStrength()), SwingConstants.RIGHT),tDefValueInsets);
		mRow++;
		
		// Valid Days
		addParameterLabel(mRow, 0, pPanel, new JLabel("Valid Days"), tDefLabelInsets);
		addParameterValue(mRow, 1, pPanel, createTextField( GPGAdapter.getValidKeyTime(pKey.getValidSeconds()), SwingConstants.RIGHT), tDefValueInsets);
		mRow++;
		
		// Creation Date
		SimpleDateFormat tSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		addParameterLabel(mRow, 0, pPanel, new JLabel("Creation Time"), tDefLabelInsets);
		addParameterValue(mRow, 1, pPanel, createTextField(tSDF.format(pKey.getCreationTime()), SwingConstants.RIGHT), tDefValueInsets);
		mRow++;
		
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = mRow++;
		c.insets = new Insets(10, 0, 10, 0);
		pPanel.add( new JLabel(" "), c);
	
		
		
		
	}
	
	private JTextField createTextField( String pValue, int pAlign ) {
		JTextField tf = new JTextField( pValue );
		tf.setHorizontalAlignment(pAlign);
		tf.setEditable(false);
		tf.setMargin(new Insets(0, 5, 0, 5));
		return tf;
	}
	
	private void addParameterLabel( int pRow, int pCol, JPanel pPanel, JLabel pLabel, Insets pInsets) {
		JLabel tLabel = pLabel;
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = pCol;
		c.gridy = pRow;
		c.anchor = GridBagConstraints.WEST;
		if (pInsets != null) {
			c.insets = pInsets; //new Insets(top, left, bottom, right)
		}
		pPanel.add(tLabel, c);	
	}
	
	
	private void addParameterValue( int pRow, int pCol, JPanel pPanel, JTextField pTextField, Insets pInsets ) {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = pCol;
		c.gridy = pRow;
		if (pInsets != null) {
			c.insets = pInsets; //new Insets(top, left, bottom, right)
		}
		c.anchor = GridBagConstraints.WEST;
		
		pPanel.add(pTextField, c);	
	}	
	

}

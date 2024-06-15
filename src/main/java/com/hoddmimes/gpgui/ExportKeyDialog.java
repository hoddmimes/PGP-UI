package com.hoddmimes.gpgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRing;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ExportKeyDialog extends JDialog {

	
	private final JTextArea mTextArea;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ExportKeyDialog dialog = new ExportKeyDialog(null,100,100);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ExportKeyDialog(KeyRingInterface pKeyRingInterface, int pX, int pY) {
		setBounds(pX, pY, 550, 480);
		GPGAdapter.setAppIcon(this,this);
		getContentPane().setLayout(new BorderLayout());

		{
			JScrollPane tScrollPane = new JScrollPane();
			
			
			mTextArea = new JTextArea();
			mTextArea.setFont(new Font("Arial", Font.PLAIN, 12));
			mTextArea.setBackground(Color.white);
			mTextArea.setEditable(false);
			mTextArea.setMargin(new Insets(5,5,5,5));
			mTextArea.setBorder(new EmptyBorder(5, 5, 5, 5));
			
			try {encodeKey( pKeyRingInterface );}
			catch( Exception e) {
				AlertMessage.showMessage(this, "Failed to encode key: " + e.getMessage());
			}
			
			tScrollPane.setViewportView(mTextArea);
			tScrollPane.getViewport().setBackground(Color.white);
			
			getContentPane().add(tScrollPane, BorderLayout.CENTER);
		
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				okButton.addActionListener(event-> this.dispose());
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
		this.pack();
	}
	
	private void encodeKey( KeyRingInterface pKeyRing ) throws Exception {
		ByteArrayOutputStream tByteArrayOutStream = new ByteArrayOutputStream();
	    ArmoredOutputStream tArmoredOutputStream = new ArmoredOutputStream(tByteArrayOutStream);
	
		
		if (pKeyRing instanceof SecKeyRing) {
			SecKeyRingInterface skri = (SecKeyRingInterface) pKeyRing;
			PGPSecretKeyRing skr = skri.getSecretKeyRing();
			skr.encode(tArmoredOutputStream);
			if (skr.getSecretKey().getUserIDs().hasNext()) {
				setTitle("Secret Key ( " +skr.getSecretKey().getUserIDs().next() + ")");
			} else {
				setTitle("Secret Key ( " + Long.toHexString(skr.getSecretKey().getKeyID()) + ")");
			}
		} else if (pKeyRing instanceof PubKeyRing) {
			PubKeyRingInterface pkri = (PubKeyRingInterface) pKeyRing;
			PGPPublicKeyRing pkr = pkri.getPublicKeyRing();
			pkr.encode(tArmoredOutputStream);
			if (pkr.getPublicKey().getUserIDs().hasNext()) {
				setTitle("Public Key ( " + pkr.getPublicKey().getUserIDs().next() + ")");
			} else {
				setTitle("Public Key ( " + Long.toHexString(pkr.getPublicKey().getKeyID()) + ")");
			}
		} else {
			throw new Exception("unknown key type (not Public nor Secret)");
		}

		tArmoredOutputStream.flush();
		tArmoredOutputStream.close();
	    String tString = new String( tByteArrayOutStream.toByteArray());
	    mTextArea.setText(tString);
	
	}

}

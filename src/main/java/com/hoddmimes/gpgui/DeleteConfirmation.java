package com.hoddmimes.gpgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class DeleteConfirmation extends JDialog {

	private boolean mShouldDeleteKey;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			DeleteConfirmation dialog = new DeleteConfirmation( KeyType.PUBLIC, "par.bertilsson@hoddmimes.com", 0x5e32b31af863L, 100, 100);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public DeleteConfirmation( KeyType pKeyType, String pUserId, long pKeyId, int xPos, int yPos ) {
		GPGAdapter.setAppIcon( this, this );
		Color tColor = new Color(248,248,248);
		this.setLocation(xPos, yPos);
        GridBagConstraints c;
		setBounds(100, 100, 500, 183);
		this.setModal(true);
		
		if (pKeyType == KeyType.PUBLIC) {
			setTitle("Delete Public Key");
		} else {
			setTitle("Delete Secret Key");
		}
		
		GridBagLayout tGridBagLayoutLayout = new GridBagLayout();
		JPanel tPanel = new JPanel( tGridBagLayoutLayout );
		tPanel.setBorder(new EmptyBorder(10, 50, 3, 50));
			
		
		JLabel tUserIdLabel = new JLabel("User Id");
		c = new GridBagConstraints();
		c.insets = new Insets(0, 20, 0, 0);
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		tPanel.add(tUserIdLabel, c);
			
		JTextField pUserIdTextField = new JTextField(pUserId);
		pUserIdTextField.setFont(new Font("Arial", Font.ITALIC, 12));
		pUserIdTextField.setBackground(tColor);
		pUserIdTextField.setMargin(new Insets(1,1,5,5));
		pUserIdTextField.setEditable(false);
			
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		tPanel.add(pUserIdTextField, c);

		JLabel tKeyIdLabel = new JLabel("Key Id");
		c = new GridBagConstraints();
		c.insets = new Insets(0, 20, 0, 0);
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		tPanel.add(tKeyIdLabel,c);

		JTextField tKeyIdtextField = new JTextField("0x" + Long.toHexString(pKeyId));
		tKeyIdtextField.setMargin(new Insets(1,1,5,5));
		tKeyIdtextField.setEditable(false);
		tKeyIdtextField.setFont(new Font("Arial", Font.ITALIC, 12));
		tKeyIdtextField.setBackground(tColor);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		tPanel.add(tKeyIdtextField,c);
		

		JLabel tConfTextLabel = new JLabel("Du you really whant to delete the key ?");
		tConfTextLabel.setFont(new Font(tConfTextLabel.getFont().getFamily(), (Font.BOLD| Font.ITALIC), tConfTextLabel.getFont().getSize()));
		c = new GridBagConstraints();
		c.insets = new Insets(25, 0, 5, 0);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.CENTER;
		tPanel.add(tConfTextLabel,c);
	

		
		FlowLayout tFlowLayout = new FlowLayout(FlowLayout.CENTER);
		JPanel tButtonPanel = new JPanel( tFlowLayout );
		
		JButton tYesButton = new JButton("Yes");
		tYesButton.setActionCommand("YES");
		tButtonPanel.add(tYesButton);
		tYesButton.addActionListener(event->{ mShouldDeleteKey = true; this.setVisible(false);});
		
		JButton tNoButton = new JButton("No");
		tNoButton.setActionCommand("NO");
		tButtonPanel.add(tNoButton);
		tNoButton.addActionListener(event->{ mShouldDeleteKey = false; this.setVisible(false);});
				
	
				
		c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridx = 0;
		c.gridy = 3;
		c.anchor = GridBagConstraints.EAST;
		tPanel.add(tButtonPanel,c);
    	getRootPane().setDefaultButton(tNoButton);
			
		this.getContentPane().add(tPanel);
		this.pack();
	}
	
	public boolean shouldDeleteKey() {
		return mShouldDeleteKey;
	}

}

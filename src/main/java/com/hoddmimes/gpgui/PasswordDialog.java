package com.hoddmimes.gpgui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JCheckBox;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;

public class PasswordDialog extends JDialog {

	private final JPanel passwordPanel = new JPanel();
	private JPasswordField mPasswordTextField;
	private JCheckBox mVisiblePasswordChkBox;
	private JTextField mUserIdTextField;
	private JTextField mKeyIdtextField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			PasswordDialog dialog = new PasswordDialog(null, "Test Dialog", "par.bertilsson@hoddmimes.com", 0x652435367L);
			dialog.setVisible(true);
			try {
				String tStr = new String( dialog.getPassword());
				//System.out.println("Password: " + tStr );
				dialog.dispose();
			}
			catch( Exception se ) {
				se.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public PasswordDialog(Component pComponent, String pTitle, String pUserId, long pKeyId) {
		GPGAdapter.setAppIcon( this, this );
		Color tColor = new Color(248,248,248);
        GridBagConstraints c;
        if (pComponent == null) {
        	setBounds(100, 100, 500, 183);
        } else {
        	setBounds(pComponent.getX() + 50, pComponent.getY() + 50, 500, 183);
        }
		this.setModal(true);
		setTitle(pTitle);
        
		GridBagLayout tGridBagLayoutLayout = new GridBagLayout();
		JPanel tPanel = new JPanel( tGridBagLayoutLayout );
		tPanel.setBorder(new EmptyBorder(10, 5, 10, 5));
			
		JLabel tUserIdLabel = new JLabel("User Id");
		c = new GridBagConstraints();
		c.insets = new Insets(0, 20, 0, 0);
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		tPanel.add(tUserIdLabel, c);
			
		mUserIdTextField = new JTextField(pUserId);
		mUserIdTextField.setFont(new Font("Arial", Font.ITALIC, 12));
		mUserIdTextField.setBackground(tColor);
		mUserIdTextField.setMargin(new Insets(1,1,5,5));
		mUserIdTextField.setEditable(false);
			
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		tPanel.add(mUserIdTextField, c);

		JLabel tKeyIdLabel = new JLabel("Key Id");
		c = new GridBagConstraints();
		c.insets = new Insets(0, 20, 0, 0);
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		tPanel.add(tKeyIdLabel,c);

		mKeyIdtextField = new JTextField("0x" + Long.toHexString(pKeyId));
		mKeyIdtextField.setMargin(new Insets(1,1,5,5));
		mKeyIdtextField.setEditable(false);
		mKeyIdtextField.setFont(new Font("Arial", Font.ITALIC, 12));
		mKeyIdtextField.setBackground(tColor);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		tPanel.add(mKeyIdtextField,c);
		
		JLabel tPasswLbl = new JLabel("Password");
		c = new GridBagConstraints();
		c.insets = new Insets(10, 0, 0, 0);
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.WEST;
		tPanel.add(tPasswLbl,c);

		mPasswordTextField = new JPasswordField();
		mPasswordTextField.setColumns(32);
		c = new GridBagConstraints();
		c.insets = new Insets(10, 0, 0, 0);
		c.gridx = 1;
		c.gridy = 2;
		c.anchor = GridBagConstraints.WEST;
		tPanel.add(mPasswordTextField,c);

		mVisiblePasswordChkBox = new JCheckBox("Visible password");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 3;
		c.anchor = GridBagConstraints.WEST;
		tPanel.add(mVisiblePasswordChkBox,c);
	
		mVisiblePasswordChkBox.addActionListener(event-> {
					if (mVisiblePasswordChkBox.isSelected()) {
						mPasswordTextField.setEchoChar('\u0000');
					} else {
						mPasswordTextField.setEchoChar('*');
					}
				});


		JButton okButton = new JButton("OK");
		okButton.setActionCommand("OK");
		c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridx = 0;
		c.gridy = 4;
		c.anchor = GridBagConstraints.EAST;
		tPanel.add(okButton,c);
    	getRootPane().setDefaultButton(okButton);
				okButton.addActionListener(event-> this.setVisible(false));
	
		this.getContentPane().add(tPanel);
		this.pack();
	}
	
	public char[] getPassword() {
		return mPasswordTextField.getPassword();
	}

}

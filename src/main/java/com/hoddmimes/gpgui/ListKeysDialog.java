package com.hoddmimes.gpgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import java.awt.Font;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;

public class ListKeysDialog extends JDialog implements TableModelListener {
    

	private JButton mOhneKorrekturenButton;
	private KeyContainer mKeyContainer;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ListKeysDialog dialog = new ListKeysDialog(KeyType.PUBLIC);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ListKeysDialog( KeyType pKeyType ) {
		GPGAdapter.setAppIcon( this, this );
		
		if (pKeyType == KeyType.PUBLIC) {
			setTitle("List Public Keys");
		}  else {
			setTitle("List Secret Keys");
		}
		setFont(new Font("Arial", Font.PLAIN, 14));
		setBounds(100, 100, 800, 260);
		getContentPane().setLayout(new BorderLayout());

		
		
		mKeyContainer = new KeyContainer(pKeyType, 620, 150, true, false);
		mKeyContainer.addTableModelListener(this);
		getContentPane().add(mKeyContainer, BorderLayout.CENTER);
		
		
		
		
		
		/**
		 * Add buttons
		 */
		JPanel tButtonPane = new JPanel();
		tButtonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(tButtonPane, BorderLayout.SOUTH);
		
		mOhneKorrekturenButton = new JButton("OK");
		mOhneKorrekturenButton.setActionCommand("OK");
		mOhneKorrekturenButton.addActionListener( event-> { this.dispose();});
		tButtonPane.add(mOhneKorrekturenButton);
		getRootPane().setDefaultButton(mOhneKorrekturenButton);
		
		

		
		
	}

	
	@Override
	public void tableChanged(TableModelEvent e) {
	}

}

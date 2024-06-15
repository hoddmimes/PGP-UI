package com.hoddmimes.gpgui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Scanner;


import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


public class HelpContentsDialog extends JDialog {

	//private final JPanel mContentPanel = new JPanel();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			HelpContentsDialog dialog = new HelpContentsDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setPreferredSize(new Dimension(650,450));
			dialog.pack();
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public HelpContentsDialog() {
		setTitle("PGPGUI Help");
/*		 try {
			 Enumeration<URL> tList = getClass().getClassLoader().getResources("");
	     while (tList.hasMoreElements())
	        {
	            System.out.println("ClassLoader Resource: " + tList.nextElement());
	        }
	        System.out.println("Class Resource: " + HelpContentsDialog.class.getResource("/"));
 		 }
		 catch( Exception ee ) {
			 ee.printStackTrace();
		 }*/
	        
	        
		//setBounds(100, 100, 600, 450);
		getContentPane().setLayout(new BorderLayout());
	

		JEditorPane tTextPane = new JEditorPane();
		tTextPane.setContentType("text/html");
		tTextPane.setEditable(false);
		tTextPane.setMargin(new Insets(10, 10, 10, 10));


		setHelpText( tTextPane );
		tTextPane.setSize(600, 450);

		// tTextPane.setText("..."); // Document text is provided below.

		JScrollPane mScrollPanel = new JScrollPane();
		mScrollPanel.setViewportView(tTextPane);
		mScrollPanel.getViewport().setSize(500, 400);
		//mContentPanel.add(mScrollPanel);
		this.getContentPane().add(mScrollPanel, BorderLayout.CENTER);
		
		

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		JButton okButton = new JButton("OK");
		okButton.setActionCommand("OK");
		okButton.addActionListener(even-> this.dispose());
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
		
	
	}

	private String fileToString( File pFile ) throws IOException {
		String tString = new Scanner(Files.newInputStream(pFile.toPath()), StandardCharsets.UTF_8).useDelimiter("\\Z").next();
		return tString;
	}



	private void setHelpText( JEditorPane pPane) {
		URL tURL = null;

		try {
			if (PGPGUI.USE_EXTENTION) {
				tURL = getClass().getClassLoader().getResource("helpext.html");
				if (tURL == null) {
					tURL =  getClass().getResource("./resources/helpext.html");
				}
				if (tURL == null) {
					tURL = getClass().getResource("./resources/helpext.html");
				}
				if (tURL != null) {
					pPane.setPage(tURL);
					return;
				}

				File tFile = new File("./resources/helpext.html");
				if (!tFile.exists()) {
					tFile = new File("./gpgui/resources/helpext.html");
				}
				if (tFile.exists()) {
					pPane.setText(fileToString(tFile));
					return;
				}
			} else {
				tURL = getClass().getClassLoader().getResource("help.html");
				if (tURL == null) {
					tURL = getClass().getResource("./resources/help.html");
				}
				if (tURL == null) {
					tURL = getClass().getResource("./resources/help.html");
				}
				if (tURL != null) {
					pPane.setPage(tURL);
					return;
				}

				File tFile = new File("./resources/help.html");
				if (!tFile.exists()) {
					tFile = new File("./gpgui/resources/help.html");
				}
				if (tFile.exists()) {
					pPane.setText(fileToString(tFile));
					return;
				}
			}
		}
		catch( IOException e ) {
			e.printStackTrace();
		}
	}
}

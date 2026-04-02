package com.hoddmimes.gpgui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.Security;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.UIManager;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;



public class PGPGUI implements ActionListener {

	// {"AES","CAST5","BLOWFISH","CAMELLIA_256","IDEA","TWOFISH","SNOWFLAKE"};
	public static final String ENCRYPT_ALGO_AES = "AES";
	public static final String ENCRYPT_ALGO_CAST5 = "CAST5";
	public static final String ENCRYPT_ALGO_BLOWFISH = "BLOWFISH";
	public static final String ENCRYPT_ALGO_CAMELLIA_256 = "CAMELLIA_256";
	public static final String ENCRYPT_ALGO_IDEA = "IDEA";
	public static final String ENCRYPT_ALGO_TWOFISH = "TWOFISH";
	public static final String ENCRYPT_ALGO_SNOWFLAKE = "SNOWFLAKE";
	
	
	
	private static String MENU_CMD_ENCRYPT_MSG = "EncryptMessage";
	private static String MENU_CMD_DECRYPT_MSG = "DecryptMessage";
	private static String MENU_CMD_ENCRYPT_FILE = "EncryptFile";
	private static String MENU_CMD_DECRYPT_FILE = "DecryptFile";
	
	private static String MENU_CMD_LIST_PUBLIC_KEYS = "ListPublicKeys";
	private static String MENU_CMD_LIST_SECRET_KEYS = "ListSecretKeys";
	private static String MENU_CMD_IMPORT_KEY = "ImportKey";
	private static String MENU_CMD_CREATE_KEY = "CreateKey";
	
	private static String MENU_CMD_HELP_ABOUT = "About";
	private static String MENU_CMD_HELP_CONTENTS = "Contents";
	private static String MENU_CMD_SETTINGS = "Settings";
	private static String MENU_CMD_EXIT = "Exit";
	
	private Font mMenuFont;
	private Font mMenuBoldFont;
	
	
	static boolean USE_EXTENSION = false;
	
	
	private JFrame mFrame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] pArgs) {
			parseArguments( pArgs );
			EventQueue.invokeLater(() -> {
                try {
                    // Deep Navy accent — drives tab underlines, focus rings, selection, scrollbar hover
                    FlatLaf.setGlobalExtraDefaults(java.util.Collections.singletonMap("@accentColor", "#1A237E"));
                    FlatLightLaf.setup();
                    UIManager.put("Button.arc",          8);
                    UIManager.put("Component.arc",       6);
                    UIManager.put("TextComponent.arc",   5);
                    UIManager.put("ScrollBar.thumbArc",  999);
                    UIManager.put("ScrollBar.width",     10);
                    UIManager.put("Panel.background",              new java.awt.Color(0xF0F1FA));
                    UIManager.put("TabbedPane.selectedBackground", new java.awt.Color(0xD9DCF0));
                    UIManager.put("TabbedPane.hoverColor",         new java.awt.Color(0xBFC4E8));
                    PGPGUI mgpgui = new PGPGUI();
                    mgpgui.mFrame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
		}
	
	
	private static void parseArguments(String[] pArgs )
	{
		int i = 0;
		while( i  < pArgs.length ) {
			if (pArgs[i].equalsIgnoreCase("-enable_extension")) {
				USE_EXTENSION = Boolean.parseBoolean( pArgs[++i] );
			}
			i++;
		}
	}
	
	private static boolean isWindows() 
	{
		String OS = System.getProperty("os.name").toLowerCase();
        return OS.contains("win");
	}
	
	
	/**
	 * Create the application.
	 */
	public PGPGUI() {
		Security.addProvider(new BouncyCastleProvider());
		mMenuFont = new Font("Arial", Font.PLAIN, 16 );
		mMenuBoldFont = new Font("Arial", Font.BOLD, 16 );
		
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		mFrame = new JFrame();
		mFrame.setBounds(100, 100, 650, 80);
		mFrame.setResizable(false);
		mFrame.setTitle("PGPGUI V1.5");
		GPGAdapter.setAppIcon( this, mFrame );
		mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		
		JMenuBar tMenuBar = new JMenuBar();
		// Add Crypto options 
		JMenu tCryptoMenu = new JMenu("Crypto");
		tCryptoMenu.setFont(mMenuBoldFont);
		createMenuItem(tCryptoMenu, "Encrypt Message", MENU_CMD_ENCRYPT_MSG);
		createMenuItem(tCryptoMenu, "Decrypt Message", MENU_CMD_DECRYPT_MSG);
		createMenuItem(tCryptoMenu, "Encrypt File", MENU_CMD_ENCRYPT_FILE);
		createMenuItem(tCryptoMenu, "Decrypt File", MENU_CMD_DECRYPT_FILE);
		createMenuItem(tCryptoMenu, "Settings", MENU_CMD_SETTINGS);
		createMenuItem(tCryptoMenu, "Exit", MENU_CMD_EXIT);
		tMenuBar.add( tCryptoMenu );
		
		
		
		JMenu tKeyMenu = new JMenu("Keys");
		tKeyMenu.setFont(mMenuBoldFont);
		createMenuItem(tKeyMenu, "List Public Keys", MENU_CMD_LIST_PUBLIC_KEYS);
		createMenuItem(tKeyMenu, "List Secret Keys", MENU_CMD_LIST_SECRET_KEYS);
		createMenuItem(tKeyMenu, "Import Key", MENU_CMD_IMPORT_KEY);
		createMenuItem(tKeyMenu, "Create Key", MENU_CMD_CREATE_KEY);
		tMenuBar.add( tKeyMenu );
		
		
		
		JMenu tHelpMenu = new JMenu("Help");
		tHelpMenu.setFont(mMenuBoldFont);
		//createMenuItem(tHelpMenu, "About", MENU_CMD_HELP_ABOUT);
		createMenuItem(tHelpMenu, "Contents", MENU_CMD_HELP_CONTENTS);
		tMenuBar.add( tHelpMenu );
			
		mFrame.getContentPane().add(tMenuBar, BorderLayout.NORTH);
	}
	
	private JMenuItem createMenuItem(JMenu pMenu, String pLabel, String pCommand) {
		JMenuItem tMenuItem = new JMenuItem(pLabel);
		tMenuItem.setFont(mMenuFont);
		tMenuItem.addActionListener(this);
		tMenuItem.setActionCommand(pCommand);
		pMenu.add(tMenuItem);
		return tMenuItem;
	}
	
	private Point getPosWhereToShowWindow() {
		Point tPoint = mFrame.getLocation();
		Dimension tSize = mFrame.getSize();
		
		Point tNewPos = new Point( (int) (tPoint.x + (tSize.getWidth() / 2.0)), (int) ((double) tPoint.y + (tSize.getHeight() * 0.8)));
		return tNewPos;
	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		String tCommandString = e.getActionCommand();
		if (e.getActionCommand().equals(MENU_CMD_LIST_PUBLIC_KEYS)) {
		    ListKeysDialog tDialog = new ListKeysDialog(KeyType.PUBLIC);
		    tDialog.pack();
		    tDialog.setLocation( getPosWhereToShowWindow());
			tDialog.setVisible(true);
		}
		if (e.getActionCommand().equals(MENU_CMD_LIST_SECRET_KEYS)) {
			   ListKeysDialog tDialog = new ListKeysDialog(KeyType.SECRET);
			   tDialog.pack();
			   tDialog.setLocation( getPosWhereToShowWindow());
			   tDialog.setVisible(true);
			}
		if (e.getActionCommand().equals(MENU_CMD_CREATE_KEY)) {
			   CreateKeyDialog tDialog = new CreateKeyDialog();
			   tDialog.pack();
			   tDialog.setLocation( getPosWhereToShowWindow());
			   tDialog.setVisible(true);
		}
		if (e.getActionCommand().equals(MENU_CMD_IMPORT_KEY)) {
			   ImportKeyDialog tDialog = new ImportKeyDialog();
			   tDialog.pack();
			   tDialog.setLocation( getPosWhereToShowWindow());
			   tDialog.setVisible(true);
		}
		if (e.getActionCommand().equals(MENU_CMD_ENCRYPT_FILE)) {
			   EncryptFileDialog tDialog = new EncryptFileDialog();
			   tDialog.pack();
			   tDialog.setLocation( getPosWhereToShowWindow());
			   tDialog.setVisible(true);
		}
		if (e.getActionCommand().equals(MENU_CMD_ENCRYPT_MSG)) {
			   EncryptMessageDialog tDialog = new EncryptMessageDialog();
			   tDialog.pack();
			   tDialog.setLocation( getPosWhereToShowWindow());
			   tDialog.setVisible(true);
		}
		if (e.getActionCommand().equals(MENU_CMD_DECRYPT_MSG)) {
			   DecryptMessageDialog tDialog = new DecryptMessageDialog();
			   tDialog.pack();
			   tDialog.setLocation( getPosWhereToShowWindow());
			   tDialog.setVisible(true);
		}
		if (e.getActionCommand().equals(MENU_CMD_DECRYPT_FILE)) {
			   DecryptFileDialog tDialog = new DecryptFileDialog();
			   tDialog.pack();
			   tDialog.setVisible(true);
		}
		if (e.getActionCommand().equals(MENU_CMD_IMPORT_KEY)) {
			   ImportKeyDialog tDialog = new ImportKeyDialog();
			   tDialog.pack();
			   tDialog.setLocation( getPosWhereToShowWindow());
			   tDialog.setVisible(true);
		}
		if (e.getActionCommand().equals(MENU_CMD_SETTINGS)) {
			   SettingsDialog tDialog = new SettingsDialog();
			   tDialog.pack();
			   tDialog.setLocation( getPosWhereToShowWindow());
			   tDialog.setVisible(true);
		}
//		if (e.getActionCommand().equals(MENU_CMD_HELP_ABOUT)) {
//			throw new RuntimeException("Not YET IMPLEMENTED FIX!!!");
//		}
		if (e.getActionCommand().equals(MENU_CMD_HELP_CONTENTS)) {
			 HelpContentsDialog tDialog = new HelpContentsDialog();
			 tDialog.setLocation( getPosWhereToShowWindow());
			 tDialog.setPreferredSize(new Dimension(650,450));
			 tDialog.pack();
			 tDialog.setVisible(true);
		}
		if (e.getActionCommand().equals(MENU_CMD_EXIT)) {
			 System.exit(0);
		}
	}

}

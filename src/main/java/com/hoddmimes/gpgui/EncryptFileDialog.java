package com.hoddmimes.gpgui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.text.JTextComponent;

import org.bouncycastle.openpgp.PGPSecretKeyRing;

public class EncryptFileDialog extends JDialog implements TableModelListener, ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private enum  SrcDestType		{ Source, Dest };
	
	private final JPanel 			mContentPanel = new JPanel();
	private final JTextField 		mInFilenameTextFile;
	private final JTextField 		mOutFilenameTextFile;
	private final JButton 			mEncryptBtn;
	private final JCheckBox 		mArmorChkBox;
	private final JCheckBox 		mDeleteSrcFileChkBox;
	private final JComboBox<SigningUser>  mSignComboBox;
	private final KeyContainer		mKeyContainer;
	
	
	
	private JPanel					mAESKeyStrengthPanel;
	
	private String[] mAESKeyStrength = new String[] {"256","512","1024","2048"};
	private  JComboBox<String>  mAESStrengthComboBox;
	
	private String[] mEncryptAlgos = new String[] { PGPGUI.ENCRYPT_ALGO_AES,
			PGPGUI.ENCRYPT_ALGO_CAST5,
			PGPGUI.ENCRYPT_ALGO_BLOWFISH,
		    PGPGUI.ENCRYPT_ALGO_CAMELLIA_256,
		    PGPGUI.ENCRYPT_ALGO_IDEA,
		    PGPGUI.ENCRYPT_ALGO_TWOFISH,
		    PGPGUI.ENCRYPT_ALGO_SNOWFLAKE};
	
	private  JComboBox<String>  mEncryptAlgoComboBox;
	
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			EncryptFileDialog dialog = new EncryptFileDialog();
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
	public EncryptFileDialog() {
		setBounds(100, 100, 450, 163);
		setTitle("Encrypt File");
		GPGAdapter.setAppIcon( this, this );
		getContentPane().setLayout(new BorderLayout());
		mContentPanel.setBorder(new EmptyBorder(10, 10, 10, 5));
		getContentPane().add(mContentPanel, BorderLayout.WEST);
		GridBagLayout tGridBagLayout = new GridBagLayout();
		tGridBagLayout.columnWidths = new int[]{0};
		tGridBagLayout.rowHeights = new int[]{ 0};
		tGridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		tGridBagLayout.rowWeights = new double[]{0.0,0.0, 0,0, Double.MIN_VALUE};
		mContentPanel.setLayout(tGridBagLayout);
		

		
		JPanel mSouthPanel = new JPanel();
		getContentPane().add(mSouthPanel, BorderLayout.SOUTH);
		mSouthPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel mKeyPanel = new JPanel();
		mKeyPanel.setLayout(new BorderLayout());
		mKeyPanel.setBorder(new EmptyBorder(10,10,10,10));
		
		
		mKeyContainer = new KeyContainer(KeyType.PUBLIC, 850, 100, true, true );
		mKeyContainer.addTableModelListener(this);
		mKeyPanel.add(mKeyContainer);
		mSouthPanel.add( mKeyPanel, BorderLayout.NORTH );
		
		
	
		int tRow = 0;
		
		JLabel tInFilenameLabel = new JLabel("In File ");
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, 0, 5);
		c.gridx = 0;
		c.gridy = tRow;
		mContentPanel.add(tInFilenameLabel, c);
		
		mOutFilenameTextFile = new JTextField();
		
		mInFilenameTextFile = new JTextField();
		mInFilenameTextFile.setDragEnabled(true);
		mInFilenameTextFile.setTransferHandler(new FileTransferHandler(SrcDestType.Source, mOutFilenameTextFile));
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, 0, 5);
		c.gridx = 1;
		c.gridy = tRow;
		mContentPanel.add(mInFilenameTextFile, c);
		mInFilenameTextFile.setColumns(52);
		
		
		JButton tInBrowseBtn = new JButton("...");
		tInBrowseBtn.setFont(new Font("Tahoma", Font.BOLD, 14));
		tInBrowseBtn.setPreferredSize(new Dimension(30, 20));
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridx = 2;
		c.gridy = tRow;
		mContentPanel.add(tInBrowseBtn, c);
		tInBrowseBtn.addActionListener(event->{ browseInFiles(); });
		
		
		
		
		JLabel tOutFilenameLabel = new JLabel("Output File ");
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(5, 0, 0, 5);
		c.gridx = 0;
		c.gridy = ++tRow;
		mContentPanel.add(tOutFilenameLabel, c);
		
		
		
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(5, 0, 0, 5);
		c.gridx = 1;
		c.gridy = tRow;
		mContentPanel.add(mOutFilenameTextFile, c);
		mOutFilenameTextFile.setColumns(52);
		
		
		JButton tOutBrowseBtn = new JButton("...");
		tOutBrowseBtn.setFont(new Font("Tahoma", Font.BOLD, 14));
		tOutBrowseBtn.setPreferredSize(new Dimension(30, 20));
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(5, 0, 0, 5);
		c.gridx = 2;
		c.gridy = tRow;
		mContentPanel.add(tOutBrowseBtn, c);
		tOutBrowseBtn.addActionListener(event->{ browseOutFiles(); });
		
		
		/**
		 * Setup parameters with a GridBag
		 */
		GridBagLayout tParamGridBaglayout = new GridBagLayout();
		JPanel tParameterPanel = new JPanel( tParamGridBaglayout );
		
		// Row 1 -------------------------
		
		mArmorChkBox = new JCheckBox("Armored Wrapper");
		tParameterPanel.add(mArmorChkBox);
		mArmorChkBox.addActionListener( event -> { updateOutFile(); });
		
		JPanel tPanel = new JPanel( new FlowLayout( FlowLayout.LEFT));
		tPanel.add(mArmorChkBox);
		
		mDeleteSrcFileChkBox = new JCheckBox("Delete Source file as encryption");
		tPanel.add(mDeleteSrcFileChkBox);
		
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, 0, 0);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridx = 0;
		c.gridy = 0;
		tParameterPanel.add(tPanel, c);
		
		
		// Row 2 -------------------------
		
		JLabel tSignUserLabel = new JLabel("Signing user");
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(10, 0, 0, 0);
		//c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridx = 0;
		c.gridy = 1;
		tParameterPanel.add(tSignUserLabel, c);
		
		
		mSignComboBox = new JComboBox<SigningUser>( getSignUsers() );	
		mSignComboBox.addActionListener(this);
		mSignComboBox.setPreferredSize(new Dimension(550, 23));
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(10, 10, 0, 0);
		c.gridx = 1;
		c.gridy = 1;
		tParameterPanel.add(mSignComboBox, c);
		
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 10, 0, 0);
		c.gridx = 3;
		c.gridy = 1;
		tParameterPanel.add(new JLabel("Encrypt Algo"), c);
		
		mEncryptAlgoComboBox = new JComboBox<String>( mEncryptAlgos );	
		mEncryptAlgoComboBox.addActionListener(this);
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 10, 0, 0);
		c.gridx = 4;
		c.gridy = 1;
		tParameterPanel.add(mEncryptAlgoComboBox, c);
		
		mAESKeyStrengthPanel = new JPanel( new FlowLayout());
		mAESKeyStrengthPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
		mAESKeyStrengthPanel.add( new JLabel("AES Encryption"));
		mAESStrengthComboBox = new JComboBox<String>( mAESKeyStrength );	
		mAESKeyStrengthPanel.add( mAESStrengthComboBox );
		mAESKeyStrengthPanel.add( new JLabel("bits"));
		if (PGPGUI.USE_EXTENTION) {
			c = new GridBagConstraints();
			c.anchor = GridBagConstraints.WEST;
			c.insets = new Insets(0, 10, 0, 0);
			c.gridx = 5;
			c.gridy = 1;
			tParameterPanel.add(mAESKeyStrengthPanel, c);
		}
		
		// Add Parameter Panel
		
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(3, 0, 0, 5);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridx = 0;
		c.gridy = ++tRow;
		mContentPanel.add(tParameterPanel, c);
		


	    /**
	     * -------------------------
	     * Add Key Panel
	     * -------------------------
	     */
	
		
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(30, 0, 0, 0);
		c.gridx = 0;
		c.gridy = ++tRow;
		mContentPanel.add(mKeyPanel, c);
		
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				mEncryptBtn = new JButton("Encrypt");
				mEncryptBtn.setActionCommand("ENCRYPT");
				buttonPane.add(mEncryptBtn);
				getRootPane().setDefaultButton(mEncryptBtn);
				mEncryptBtn.addActionListener(event-> { encryptFile(); } );
				mEncryptBtn.setEnabled(false);
			}
			{
				JButton tCancelButton = new JButton("Cancel");
				tCancelButton.setActionCommand("Cancel");
				buttonPane.add(tCancelButton);
				tCancelButton.addActionListener( event-> { this.dispose(); });
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
			AlertMessage.showMessage(this, "Failed to retreive secret keys: " + e.getMessage());
			SigningUser[] tArr = new SigningUser[1];
			tArr[0] = new SigningUser(null);
			return tArr;
		}
	}

	
	private void encryptFile() {
		
		OutputStream tOutStream;
		InputStream  tInStream;
		
		if (mInFilenameTextFile.getText().isEmpty()) {
			return;
		}
		
		File tOutFile = new File(mOutFilenameTextFile.getText());
		
		File tInFile = new File(mInFilenameTextFile.getText());
		if ((!tInFile.exists()) || (!tInFile.canRead())) {
			AlertMessage.showMessage(this, "File is does no exits or is not readable");
			return;
		} 
	
		
		PubKeyRingInterface tEncKeyRing = (PubKeyRingInterface) mKeyContainer.getKeyAtRow(mKeyContainer.getSelectedRow());
		if (tEncKeyRing == null) {
			AlertMessage.showMessage(this, "PGP key not found for selected user");
			return;
		}
		
		try {
			int tKeyStrength = Integer.parseInt((String) mAESStrengthComboBox.getSelectedItem());
			String tAlgo = (String) mEncryptAlgoComboBox.getSelectedItem();
			PGPSecretKeyRing tSecretSignKeyRing = (mSignComboBox.getSelectedItem() == null) ? null : ((SigningUser) mSignComboBox.getSelectedItem()).mKeyRing.getSecretKeyRing();
			double tProcessingTime = GPGAdapter.getInstance().encryptFile(tInFile, tOutFile, tEncKeyRing.getPublicEncryptionKey(), tSecretSignKeyRing, mArmorChkBox.isSelected(), tAlgo, tKeyStrength);
	    	
			if (mDeleteSrcFileChkBox.isSelected()) {
				if (!tInFile.delete()) {
					AlertMessage.showMessage(this, "Failed to delete source file \"" + mInFilenameTextFile.getText() + "\"" );
				}
			}
	    	
	    	
			AlertMessage.showMessage(this, "Succefully encrypted \"" + mInFilenameTextFile.getText() + "\" (" + tProcessingTime + " sec)");
			this.dispose();
			return;
	    }
	    catch(Exception e) {
	    	AlertMessage.showMessage(this, "failed to encrypt; " + e.getMessage(), 3000); 
	    	return;
	    }	
	}
	
	private void browseInFiles() {
		  JFileChooser tFileChooser = new JFileChooser();
		  tFileChooser.setCurrentDirectory(new File( Settings.getInstance().getCurrentDir()));
		           
		  int tSts = tFileChooser.showOpenDialog(this);

		  if (tSts == JFileChooser.APPROVE_OPTION) {
			  File tSelectedFile = tFileChooser.getSelectedFile();
			  Settings.getInstance().setCurrentDir(tSelectedFile.getParent());
			  mInFilenameTextFile.setText(tSelectedFile.getAbsolutePath());
			  if (mArmorChkBox.isSelected()) {
				  mOutFilenameTextFile.setText(tSelectedFile.getAbsolutePath() + ".pgp.asc");
			  } else {
				  mOutFilenameTextFile.setText(tSelectedFile.getAbsolutePath() + ".pgp");
			  }
		  }
	}
	
	private void browseOutFiles() {
		  JFileChooser tFileChooser = new JFileChooser();
		  tFileChooser.setCurrentDirectory(new File( Settings.getInstance().getCurrentDir()));
		           
		  int tSts = tFileChooser.showOpenDialog(this);

		  if (tSts == JFileChooser.APPROVE_OPTION) {
			  File tSelectedFile = tFileChooser.getSelectedFile();
			  Settings.getInstance().setCurrentDir(tSelectedFile.getParent());
			  mOutFilenameTextFile.setText(tSelectedFile.getAbsolutePath());
		  }
	}
	
	private void updateOutFile() {
		if (mOutFilenameTextFile.getText().isEmpty()) {
			return;
		}
		String tName = mOutFilenameTextFile.getText();
		if (mArmorChkBox.isSelected()) {
			if (!tName.endsWith(".asc")) {
			  tName = tName + ".asc";
			  mOutFilenameTextFile.setText( tName );
			} else if (!tName.endsWith(".pgp")) {
				tName = tName.substring(0, tName.length() - ".pgp".length()) + ".asc";
				mOutFilenameTextFile.setText( tName );
			}
		} else if (tName.endsWith(".asc")) {
			tName = tName.substring(0, tName.length() - ".asc".length());
			 mOutFilenameTextFile.setText( tName );
		}
	}
	
	

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

	
	
	class FileTransferHandler extends TransferHandler {
	    SrcDestType mSrcDestType;
		JTextField mOpositeTextField;
		
		public FileTransferHandler( SrcDestType pType, JTextField pOpositeTextField) {
			mSrcDestType = pType;
			mOpositeTextField = pOpositeTextField;
		}
		
		
		
		public int getSourceActions(JComponent c) {
	        return COPY_OR_MOVE;
	    }

	    public Transferable createTransferable(JComponent c) {
	        return new StringSelection(((JTextComponent) c).getSelectedText());
	    }

	    public void exportDone(JComponent c, Transferable t, int action) {
	        if(action == MOVE)
	            ((JTextComponent) c).replaceSelection("");
	    }

	    public boolean canImport(TransferSupport ts) {
	        return ts.getComponent() instanceof JTextComponent;
	    }

	    
	    private String getReversedFilename( SrcDestType pSrcDestType, String pFilename, String pDefaultName) {
	    	if (!pDefaultName.isEmpty()) {
	    		return pDefaultName;
	    	}
	    	    	
	    	if (pFilename.isEmpty()) {
	    		return pDefaultName;
	    	}
	    	
	    	if (pSrcDestType == SrcDestType.Source) {
	    		if (mArmorChkBox.isSelected()) {
	    			if (pFilename.endsWith(".pgp")) {
	    				return pFilename.substring(0, pFilename.length() - ".pgp".length() - 1) + ".asc";
	    			} else {
	    				return pFilename + ".asc";
	    			}
	    		} else {
	    			if (pFilename.endsWith(".asc")) {
	    				return pFilename.substring(0, pFilename.length() - ".asc".length() - 1) + ".pgp";
	    			} else {
	    				return pFilename + ".pgp";
	    			}
	    		}
	    	}
	    	
	    	return pDefaultName;
	    }
	    
	    public boolean importData(TransferSupport ts) {
	        try {
	        	List<File> tFileList = (List<File>) ts.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
	        	if ((tFileList == null) || (tFileList.size() != 1)) {
	        		return false;
	        	}
	        	File tFile = tFileList.get(0);
	        	((JTextComponent) ts.getComponent()).setText(tFile.getAbsolutePath());
	        	mOpositeTextField.setText(getReversedFilename( mSrcDestType, tFile.getAbsolutePath(), mOpositeTextField.getText()));
	            return true;
	        } catch(UnsupportedFlavorException e) {
	            return false;
	        } catch(IOException e) {
	            return false;
	        }
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

}

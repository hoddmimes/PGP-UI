package com.hoddmimes.gpgui;

import java.awt.BorderLayout;
import java.awt.Component;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

import org.bouncycastle.crypto.RuntimeCryptoException;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;

public class DecryptFileDialog extends JDialog implements GPGAdapter.DecryptInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private enum  SrcDestType		{ Source, Dest };
	private final JPanel 			mContentPanel = new JPanel();
	private final JTextField 		mInFilenameTextFile;
	private final JTextField 		mOutFilenameTextFile;
	private final JButton 			mDecryptBtn;
	
	private final JTextField  mSignUsers;
	private final JPanel 	  mSignPanel;
	private final JPanel 	  mSouthPanel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			DecryptFileDialog dialog = new DecryptFileDialog();
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
	public DecryptFileDialog() {
		setTitle("Decrypt File");
		GPGAdapter.setAppIcon( this, this );
		//setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		mContentPanel.setLayout(new FlowLayout());
		mContentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagLayout tGridBagLayout = new GridBagLayout();
		tGridBagLayout.columnWidths = new int[]{0};
		tGridBagLayout.rowHeights = new int[]{ 0};
		tGridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		tGridBagLayout.rowWeights = new double[]{0.0,0.0, Double.MIN_VALUE};
		mContentPanel.setLayout(tGridBagLayout);
		getContentPane().add(mContentPanel, BorderLayout.CENTER);
		
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
		mInFilenameTextFile.setColumns(42);
		
		
		JButton tInBrowseBtn = new JButton("...");
		tInBrowseBtn.setFont(new Font("Tahoma", Font.BOLD, 14));
		tInBrowseBtn.setPreferredSize(new Dimension(30, 20));
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
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
		mOutFilenameTextFile.setColumns(42);
		
		
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
		 * Add South Panel
		 */
		{
			mSouthPanel = new JPanel(new BorderLayout());
			getContentPane().add(mSouthPanel, BorderLayout.SOUTH);

			
			mSignPanel = new JPanel();
			FlowLayout tSignFlowLayout = new FlowLayout(FlowLayout.LEFT);
			tSignFlowLayout.setHgap(10);
			mSignPanel.setLayout(tSignFlowLayout);
			mSignPanel.setBorder( new EmptyBorder(10, 10, 0, 0));
			
			mSignPanel.add( new JLabel("Signing user"));
			
			mSignUsers = new JTextField();
			mSignUsers.setColumns(40);
			mSignUsers.setEditable(false);
			mSignPanel.add(mSignUsers);
			mSignPanel.setEnabled(false);
			//mSouthPanel.add(mSignPanel, BorderLayout.NORTH);
		
			
			
			JPanel tButtonPanel = new JPanel();
			tButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			mSouthPanel.add(tButtonPanel, BorderLayout.SOUTH);
			{
				mDecryptBtn = new JButton("Decrypt");
				mDecryptBtn.setActionCommand("DECRYPT");
				tButtonPanel.add(mDecryptBtn);
				getRootPane().setDefaultButton(mDecryptBtn);
				mDecryptBtn.addActionListener(event-> { decryptFile(); });
			}
			{
				JButton tCancelBtn = new JButton("Cancel");
				tCancelBtn.setActionCommand("Cancel");
				tButtonPanel.add(tCancelBtn);
				tCancelBtn.addActionListener( event -> { this.dispose(); });
			}
		}
	}
	
	private void browseInFiles() {
		JFileChooser tFileChooser = new JFileChooser();
		tFileChooser.setCurrentDirectory(new File(Settings.getInstance().getCurrentDir()));

		int tSts = tFileChooser.showOpenDialog(this);

		if (tSts == JFileChooser.APPROVE_OPTION) {
			File tSelectedFile = tFileChooser.getSelectedFile();
			Settings.getInstance().setCurrentDir(tSelectedFile.getParent());
			mInFilenameTextFile.setText(tSelectedFile.getAbsolutePath());
			Settings.getInstance().setCurrentDir(tSelectedFile.getParent());
			if (mOutFilenameTextFile.getText().isEmpty()) {
				String tName = new String(tSelectedFile.getAbsolutePath());
				if (tName.endsWith(".asc")) {
					tName = tName.substring(0, tName.length() - ".asc".length());
				}
				if (tName.endsWith(".pgp")) {
					tName = tName.substring(0, tName.length() - ".pgp".length());
				}
				mOutFilenameTextFile.setText(tName);
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
	
	
	private boolean isArmoredFile( File pInFile ) {
		InputStream tInStream = null;
		
		try {
		  byte[] tBuffer = new byte[ 2048 ];
		  tInStream = Files.newInputStream(pInFile.toPath());
		  int tLen = tInStream.read( tBuffer, 0, tBuffer.length);
		  tInStream.close();
		  String tString = new String( tBuffer, 0, tLen );
		  if (tString.contains("BEGIN PGP MESSAGE")) {
			  return true;
		  } else {
			  return false;
		  }
		}
		catch( Exception e) {
			if (tInStream != null) {
				try {tInStream.close();}
				catch( IOException ie) {}
			}
			return false;
		}
	}
	
	private String getUserIdFromSecretKey( PGPSecretKey pSeckey ) {
		Iterator<byte[]> tUsrIdItr = pSeckey.getPublicKey().getRawUserIDs();
		String tUserId = "<unknown>";
		if (tUsrIdItr.hasNext()) {
			try {tUserId = new String( tUserId.getBytes(), StandardCharsets.UTF_8);}
			catch( Exception e) {
				e.printStackTrace();
			}
		}
		return tUserId;
	}
	
	private PGPPrivateKey getPrivateKey( PGPSecretKey pSecretKey, char[] pPassword ) throws PGPException {
	    PBESecretKeyDecryptor tDecryptor = new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider()).build(pPassword);
	    return pSecretKey.extractPrivateKey(tDecryptor);
	}
	
	
	private void decryptFile() {
		PGPEncryptedDataList tEncDataList 	= null;
		SecKeyRingInterface tSecKeyRing	    = null;		
		
		File tInFile = new File( mInFilenameTextFile.getText());
		if ((!tInFile.exists()) || (!tInFile.canRead())) {
			AlertMessage.showMessage(this, "In file does not exists or cannot be read");
			return;
		}
		
		try 
		{
			GPGAdapter.getInstance().decryptFile(tInFile, new File( mOutFilenameTextFile.getText()), this);
		}
		catch( Exception e) {
			AlertMessage.showMessage(this, "failed to decrypt file: " + e.getMessage());
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
	    	   if ((pFilename.endsWith(".asc")) || (pFilename.endsWith(".pgp"))) {
	    		   return pFilename.substring(0, pFilename.length() - ".asc".length());
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
	public char[] getPasswordForDecrypt(long pKeyId, String pUserId) {
		PasswordDialog tPasswDialog = new PasswordDialog(this, "Decrypt Private Password", pUserId, pKeyId);
		tPasswDialog.setVisible(true);
		char[] tPassword = tPasswDialog.getPassword();
		tPasswDialog.dispose();
		return tPassword;
	}


	@Override
	public void decryptedMessage(byte[] pMessageBytes) {
		// Should never be called
		throw new RuntimeCryptoException("Inavlid callback in DecryptFileDialog");
	}

	@Override
	public void encryptSignOnePassUsers(String pUserList) {
		mSignUsers.setText(pUserList);
		mSouthPanel.add( mSignPanel, BorderLayout.NORTH);
		this.revalidate();
		this.repaint();
	}

	@Override
	public void encryptSignUsers(String pUserList) {
		System.out.println("Signing user: " + pUserList);
		
	}

}

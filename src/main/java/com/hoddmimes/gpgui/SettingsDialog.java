package com.hoddmimes.gpgui;

import java.awt.Color;
import java.awt.Container;
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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;


public class SettingsDialog extends JDialog {

	private final PgpKeyRingParameter mPGPPubFileName;
	private final PgpKeyRingParameter mPGPUIPubFileName;
	private final PgpKeyRingParameter mPGPSecFileName;
	private final PgpKeyRingParameter mPGPUISecFileName;
	
	private final PlainParameterInteger mCacheLiveTime;
	
	
	private final Container mContentPanel;
	private final JPanel mPanel;
	private Color mNoEditColor = new Color(248,248,248);
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			SettingsDialog dialog = new SettingsDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public SettingsDialog() {
		int tRow = 0;
		Settings tSettings = Settings.getInstance();
		Color tColor = new Color(248,248,248);
		GPGAdapter.setAppIcon( this, this );

        mContentPanel = this.getContentPane();
		//setBounds(100, 100, 500, 183);
		this.setModal(true);
		setTitle("PGPUI Parameters");
		
		GridBagLayout tGridBagLayoutLayout = new GridBagLayout();
		mPanel = new JPanel( tGridBagLayoutLayout );
		mPanel.setBorder(new EmptyBorder(20, 20, 3, 20));
		
		
		mPGPSecFileName = new PgpKeyRingParameter( "PGP Secure Keys", tSettings.getPGPSecretKeyRingFilename());
		mPGPSecFileName.addParameterToPanel(mPanel, 0, tRow++);
		mPGPPubFileName = new PgpKeyRingParameter( "PGP Public Keys", tSettings.getPGPPublicKeyRingFilename());
		mPGPPubFileName.addParameterToPanel(mPanel, 0, tRow++);
		mPGPUISecFileName = new PgpKeyRingParameter( "PGPUI Secure Keys", tSettings.getPGPUISecretKeyRingFilename());
		mPGPUISecFileName.addParameterToPanel(mPanel, 10, tRow++);
		mPGPUIPubFileName = new PgpKeyRingParameter( "PGPUI Public Keys", tSettings.getPGPUIPublicKeyRingFilename());
		mPGPUIPubFileName.addParameterToPanel(mPanel, 0, tRow++);
			
		
		mCacheLiveTime = new PlainParameterInteger("Password cache TTL", tSettings.getPasswordTTL(), "min");
		mCacheLiveTime.addParameterToPanel(mPanel, 10, tRow++);
		mContentPanel.add(mPanel);
		
		FlowLayout tFlowLayout = new FlowLayout( SwingConstants.RIGHT);
		tFlowLayout.setHgap(10);
		JPanel tButtonPanel = new JPanel( tFlowLayout );
		
		JButton tSaveButton = new JButton("Save");
		tSaveButton.addActionListener(event-> saveParameters());
		JButton tCancelButton = new JButton("Cancel");
		tCancelButton.addActionListener(event-> this.dispose());
		
		tButtonPanel.add(tCancelButton);
		tButtonPanel.add(tSaveButton);
		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridx = 0;
		c.gridy = tRow++;
		c.insets = new Insets(10, 5, 15, 0);
		mPanel.add(tButtonPanel, c );
		
	    this.pack();
	}
	
	private void saveParameters() {
		Settings tSettings = Settings.getInstance();
		try {
			int tValue = mCacheLiveTime.getValue();
			if (tValue < 0) {
				throw new Exception("Password cache time must not be negative");
			}
			tSettings.setPasswordTTL(tValue);
		}
		catch( Exception e) {
			AlertMessage.showMessage(this, "Invalid password cache live time: " + e.getMessage());
			return;
		}
		tSettings.setPGPPublicKeyRingFilename(mPGPPubFileName.getFilename());
		tSettings.setPGPSecretKeyRingFilename(mPGPPubFileName.getFilename());
		tSettings.setPGPPublicKeyRingFilename(mPGPPubFileName.getFilename());
		tSettings.setPGPUISecretKeyRingFilename(mPGPPubFileName.getFilename());
		
		tSettings.save();
	}

	private boolean checkIfFileExits( String pFilename ) {
		File tFile = new File( pFilename );
		return tFile.exists();
	}
	
	private JTextField createTextField( String pInitValue, int pColumns, boolean pIsEditable ) {
		JTextField tTextField = new JTextField( pInitValue );
		tTextField.setColumns( pColumns );
		tTextField.setMargin(new Insets(0,5,0,5));
		if ( !pIsEditable ) {
		
			tTextField.setEditable(false);
			tTextField.setFont(new Font(tTextField.getFont().getFamily(), Font.ITALIC, tTextField.getFont().getSize()));
			tTextField.setBackground(mNoEditColor);
		} 
		return tTextField;
	}
	

	
	
	private class PlainParameterInteger 
	{
		JTextField 		mParameter;
		JLabel			    mLabel;
		JLabel				mPostLabel;
		
		
		
		private PlainParameterInteger( String pLabel, int pValue, String pPostLabel) {
			NumberFormat tFormat = NumberFormat.getIntegerInstance();
			tFormat.setGroupingUsed(false);
			mLabel = new JLabel( pLabel );			
			mParameter = new JFormattedTextField(tFormat);
			mParameter.setColumns( 5 );
			mParameter.setMargin(new Insets(0,5,0,5));
			mParameter.setText(Integer.toString(pValue));
			mParameter.setHorizontalAlignment(SwingConstants.RIGHT);
			if (pPostLabel != null) {
				mPostLabel = new JLabel(pPostLabel);
			}
		}
		
		private void addParameterToPanel( JPanel pJPanel, int pExtraVspace, int pRow) {
			GridBagConstraints c = null;
			
			// Add Label 
			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = pRow;
			c.insets = new Insets(pExtraVspace, 5, 0, 0);
			c.anchor = GridBagConstraints.WEST;
			mPanel.add( mLabel, c);
						
			c = new GridBagConstraints();
			c.gridx = 1;
			
			c.gridy = pRow;
			c.insets = new Insets(pExtraVspace, 15, 0, 0);
			c.anchor = GridBagConstraints.WEST;
			mPanel.add( mParameter, c);	
			
			if (mPostLabel != null) {
				c = new GridBagConstraints();
				c.gridx = 1;
				c.gridy = pRow;
				c.insets = new Insets(pExtraVspace, 95, 0, 0);
				c.anchor = GridBagConstraints.WEST;
				mPanel.add( mPostLabel, c);	
			}
		}
		
		
		private int getValue() throws NumberFormatException {
			return Integer.parseInt(mParameter.getText());
		}
	}
	
	private class PgpKeyRingParameter implements FocusListener
	{
		JTextField 	mParameter;
		JLabel		mLabel;
		JCheckBox	mCheckBox;
		JButton		mButton;
		
		
		PgpKeyRingParameter( String pLabel, String pInitFilename ) {
			mLabel = new JLabel( pLabel );
			mCheckBox = new JCheckBox("exists");
			mCheckBox.setEnabled(false);
			
			mButton = new JButton("...");
			mButton.setFont(new Font( mCheckBox.getFont().getFamily(), Font.BOLD, (mCheckBox.getFont().getSize()+2)));
			mButton.setPreferredSize(new Dimension(45, 18));
			mButton.addActionListener(event-> browsekeyRingFile());
			
			mParameter = createTextField(pInitFilename, 32, true);
			mParameter.addFocusListener(this);
			mParameter.setDragEnabled(true);
			mParameter.setTransferHandler(new FileTransferHandler());
			
			validateFile();
		}
		
		private void addParameterToPanel( JPanel pJPanel, int pExtraVspace, int pRow) {
			GridBagConstraints c = null;
			
			// Add Label 
			c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = pRow;
			c.insets = new Insets(pExtraVspace, 5, 0, 0);
			c.anchor = GridBagConstraints.WEST;
			mPanel.add( mLabel, c);
			
			c = new GridBagConstraints();
			c.gridx = 1;
			c.gridy = pRow;
			c.insets = new Insets(pExtraVspace, 15, 0, 0);
			c.anchor = GridBagConstraints.WEST;
			mPanel.add( mParameter, c);
			
			c = new GridBagConstraints();
			c.gridx = 2;
			c.gridy = pRow;
			c.insets = new Insets(pExtraVspace, 15, 0, 0);
			c.anchor = GridBagConstraints.WEST;
			mPanel.add( mButton, c);

			c = new GridBagConstraints();
			c.gridx = 3;
			c.gridy = pRow;
			c.insets = new Insets(pExtraVspace, 15, 0, 0);
			c.anchor = GridBagConstraints.WEST;
			mPanel.add( mCheckBox, c);
		}
		
		private String getFilename() {
			return mParameter.getText();
		}
		
		
		private void validateFile() {
			File tFile = new File( mParameter.getText());
			mCheckBox.setSelected(tFile.exists());
		}
		
		private void browsekeyRingFile() {
			JFileChooser tFileChooser = new JFileChooser();
			tFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if (!mParameter.getText().isEmpty()) {
				int tIdx = mParameter.getText().lastIndexOf(File.separatorChar);
				String tDir = (tIdx >= 0) ? mParameter.getText().substring(0, tIdx) : new String("./");
				tFileChooser.setCurrentDirectory(new File(tDir));
			} else {
				tFileChooser.setCurrentDirectory(new File("./"));
			}

			int tSts = tFileChooser.showOpenDialog(SettingsDialog.this);

			if (tSts == JFileChooser.APPROVE_OPTION) {
				File tSelectedFile = tFileChooser.getSelectedFile();
				Settings.getInstance().setCurrentDir(tSelectedFile.getParent());
				mParameter.setText(tSelectedFile.getAbsolutePath());
				validateFile();		
			}
		}
		
		@Override
		public void focusLost(FocusEvent e) {
			validateFile();
		}
		
		@Override
		public void focusGained(FocusEvent e) {
			// Nothing by design
			
		}
	}
	
	
	class FileTransferHandler extends TransferHandler {
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

	    public boolean importData(TransferSupport ts) {
	        try {
	        	List<File> tFileList = (List<File>) ts.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
	        	if ((tFileList == null) || (tFileList.size() != 1)) {
	        		return false;
	        	}
	        	File tFile = tFileList.get(0);
	        	((JTextComponent) ts.getComponent()).setText(tFile.getAbsolutePath());
	            return true;
	        } catch(UnsupportedFlavorException e) {
	            return false;
	        } catch(IOException e) {
	            return false;
	        }
	    }
	}
	
}

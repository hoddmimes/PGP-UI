package com.hoddmimes.gpgui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class KeyContainer extends JPanel implements TableModelListener, MouseListener,ListSelectionListener, ActionListener
{
	private final String POPUP_EXPAND_KEY = "Expand Key";
	private final String POPUP_EXPORT_PUBLIC_KEY = "Export Public Key";
	private final String POPUP_EXPORT_PRIVATE_KEY = "Export Private Key";
	private final String POPUP_DELETE_KEY = "Delete Key";
	
	
	private final boolean		mEnableUserSelections;
	private final JTable 		mTable;
	private final KeyTableModel mTableModel;

	
	private JTextField 	mUserIdFilterTextField;
	private KeyType		mKeyType;
	private Dimension 	mPrefViewSize;
	private PopupMenu	mPopupMenu;
	
	private ArrayList<TableModelListener> mTableModelListener;

	public KeyContainer(KeyType pKeyType, int pWidth, int pHeight, boolean pUserIdFilter, boolean pEnableUserSeletions ) {
		this.mKeyType = pKeyType;
		this.mEnableUserSelections = pEnableUserSeletions;
		this.mPrefViewSize = new Dimension(pWidth, pHeight);
		this.setLayout(new BorderLayout());
		
		mPopupMenu = new PopupMenu();
		mPopupMenu.addMenuItem(POPUP_DELETE_KEY, this);
		mPopupMenu.addMenuItem(POPUP_EXPAND_KEY, this);
		
		if (pKeyType == KeyType.SECRET) {
			mPopupMenu.addMenuItem(POPUP_EXPORT_PRIVATE_KEY, this);
		} else {
			mPopupMenu.addMenuItem(POPUP_EXPORT_PUBLIC_KEY, this);
		}
		
		mTable = new JTable();
		mTableModel = new KeyTableModel(pEnableUserSeletions);
		
		mTableModelListener = new ArrayList<>();
	
		setupFilterPanel(pUserIdFilter);
		setupKeyTable();
	}
	
	private void setupFilterPanel( boolean pUserIdFilter ) {
		JPanel tFilterPanel = new JPanel();
		GridBagLayout tGridBagLayout = new GridBagLayout();
		tFilterPanel.setLayout(tGridBagLayout);
		
		if (!pUserIdFilter) {
			return;
		}
		/**
		 * Add User Id filter
		 */
		if (pUserIdFilter)
		{
			JPanel tUserIdFilterPanel = new JPanel();
			FlowLayout tFlowLayout = new FlowLayout();
			tFlowLayout.setAlignment(FlowLayout.CENTER);
			tFlowLayout.setHgap(10);
			tUserIdFilterPanel.setLayout(tFlowLayout);
			
			// Add Label
			tUserIdFilterPanel.add( new JLabel("User Id"));
			
			// Text filter Input
			mUserIdFilterTextField = new JTextField();
			mUserIdFilterTextField.setColumns(18);
			tUserIdFilterPanel.add( new JLabel("User Id"));
			tUserIdFilterPanel.add( mUserIdFilterTextField );
			
			// Add apply button 
			JButton tFilterApplyBtn = new JButton("Apply");
			tFilterApplyBtn.setPreferredSize(new Dimension(65, 20));
			tFilterApplyBtn.addActionListener( event-> applyFilter());
			tUserIdFilterPanel.add(tFilterApplyBtn);
			
			GridBagConstraints  c  = new GridBagConstraints();
			c.gridx = 0; c.gridy = GridBagConstraints.REMAINDER;
			//c.anchor = GridBagConstraints.CENTER;
			tFilterPanel.add( tUserIdFilterPanel, c);

		}
		
		this.add( tFilterPanel, BorderLayout.NORTH );
	}
		
	
	
	private void setupKeyTable() {
		/**
		 * Initiate key table
		 */
		
		List<KeyRingInterface> tKeyRings;
		try {
			if (mKeyType == KeyType.PUBLIC) {
				tKeyRings = GPGAdapter.getInstance().getPublicKeyRings();
			}   else {
				tKeyRings = GPGAdapter.getInstance().getSecretKeyRings();
			}
			for( KeyRingInterface kr : tKeyRings ) {
				mTableModel.addKey(kr);
			}
		}
		catch( Exception  e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
			
		mTable.setModel(mTableModel);
		mTableModel.initiateColumnData( mTable.getColumnModel() );
		mTable.getModel().addTableModelListener(this);
		mTable.setFont(new java.awt.Font("Calibri",0,14));
		mTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mTable.setSelectionBackground(new Color( 242,242,242 ));
		mTable.setSelectionForeground(Color.BLUE);
		mTable.setDefaultRenderer(String.class, mTableModel.getRenderer());
		mTable.getTableHeader();
		
		
		mTable.addMouseListener(this);
		mTable.setRowSelectionAllowed(true);
		mTable.getSelectionModel().addListSelectionListener(this);
		
		mTable.getTableHeader().setDefaultRenderer(mTableModel.getHeaderRender());
		mTable.getTableHeader().setBackground( new Color( 248, 248,248));
		
		JScrollPane tScrollPane = new JScrollPane();
		tScrollPane.setBackground(Color.WHITE);
		tScrollPane.getViewport().setBackground(Color.WHITE);
		tScrollPane.getViewport().setPreferredSize(mPrefViewSize);
		tScrollPane.setViewportView(mTable);
		this.add(tScrollPane, BorderLayout.CENTER);
	}

	public KeyRingInterface getKeyAtRow(int pRow) {
		return mTableModel.getKeyAtRow(pRow);
	}
	
	public int getSelectedRow() {
		return mTableModel.getSelectedRow();
	}
	
	public boolean anyUserSelected() {
		return mTableModel.anyUserSelected();
	}
	
	public void addTableModelListener( TableModelListener pListener ) {
		mTableModelListener.add( pListener );
	}
	
	private void fixSelectionRow( int pRow ) {
		mTable.clearSelection();
		int tSelectedRow = mTableModel.getSelectedRow();
		if (tSelectedRow >= 0) {
			mTable.addRowSelectionInterval(tSelectedRow, tSelectedRow);
		}
	}
	
	@Override
	public void tableChanged(TableModelEvent e) {
		
		if (e.getType() == e.UPDATE) {
			//System.out.println("Table Update (row selected: " + mTableModel.getSelectedRow() + " )");
			fixSelectionRow(e.getFirstRow() );
		}
		for( TableModelListener l : mTableModelListener) {
			l.tableChanged(e);
		}
	}


	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			int tFirstRow = e.getFirstIndex();
			int tLastRow = e.getLastIndex();
			//	System.out.println("First Select: " + tFirstRow + " Last Select: " + tLastRow + " table select row: " + mTableModel.getSelectedRow());
			if ((tFirstRow != mTableModel.getSelectedRow()) || (tLastRow != mTableModel.getSelectedRow())) {
				fixSelectionRow( mTableModel.getSelectedRow());
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			int tRow = mTable.rowAtPoint(e.getPoint());
		    int tCol = mTable.columnAtPoint(e.getPoint());
		    
//		    System.out.println("double clicked row: " + tRow + " col: " + tCol);
//		    System.out.println( e.toString() );
		    
		    if ((!mEnableUserSelections) || (tCol > 0)) {
		      mPopupMenu.setSelectedKeyRing( mTableModel.getKeyAtRow(tRow));
		      mPopupMenu.show(this, e.getX(), e.getY());
		    }
		  }
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	private void applyFilter() {
		if (mUserIdFilterTextField.getText().isEmpty()) {
			mTableModel.applyUserIdFilter(null);
			return;
		}
		mTableModel.applyUserIdFilter( mUserIdFilterTextField.getText());
	}

	@Override
	public void actionPerformed(ActionEvent pActionEvent) {

		if (pActionEvent.getActionCommand().equals(POPUP_EXPAND_KEY)) {
			ExpandKeyDialog ekd = new ExpandKeyDialog( mPopupMenu.getSelectedKeyRing(), mPopupMenu.getXPos(), mPopupMenu.getYPos());
			ekd.setVisible(true);
		}
		
		if ((pActionEvent.getActionCommand().equals(POPUP_EXPORT_PUBLIC_KEY)) || (pActionEvent.getActionCommand().equals(POPUP_EXPORT_PUBLIC_KEY)) ){
			ExportKeyDialog ekd = new ExportKeyDialog( mPopupMenu.getSelectedKeyRing(), mPopupMenu.getXPos(), mPopupMenu.getYPos());
			ekd.setVisible(true);
		}
		if ((pActionEvent.getActionCommand().equals(POPUP_EXPORT_PRIVATE_KEY)) || (pActionEvent.getActionCommand().equals(POPUP_EXPORT_PUBLIC_KEY)) ){
			ExportKeyDialog ekd = new ExportKeyDialog( mPopupMenu.getSelectedKeyRing(), mPopupMenu.getXPos(), mPopupMenu.getYPos());
			ekd.setVisible(true);
		}
		
		
		if (pActionEvent.getActionCommand().equals(POPUP_DELETE_KEY)) {
			KeyRingInterface kri = mPopupMenu.getSelectedKeyRing();
			KeyType tKeyType = (kri instanceof PubKeyRing) ? KeyType.PUBLIC : KeyType.SECRET;
			DeleteConfirmation dc = new DeleteConfirmation( tKeyType, kri.getFirstUserId(), kri.getMasterKeyId(), mPopupMenu.getXPos() + 100, mPopupMenu.getYPos() + 100);
			dc.setVisible(true);
			dc.setModal(true);
			boolean tDelConf = dc.shouldDeleteKey();
			if (!tDelConf) {
				return;
			}
			try {
				
				if (tKeyType == KeyType.PUBLIC) {
					Auxx.deleteAndSavePublicKeys((PubKeyRing) kri, kri.getKeyRingRepositoryId());
				} else {
					Auxx.deleteAndSaveSecretKeys((SecKeyRing) kri, kri.getKeyRingRepositoryId());
				}
				mTableModel.removeKeyRing( kri );
				if (kri.getKeyRingRepositoryId() == GPGAdapter.GNUPG_REPOSITORY_INT){
					AlertMessage.showMessage(this, "Key successfully delete from Gnu_PG key ring file");
				} else {
					AlertMessage.showMessage(this, "Key successfully delete from PGPGUI key ring file");
				}
			}
			catch( Exception err) {
				AlertMessage.showMessage(this, "Failed to delete key: " + err.getMessage());
			}

		}
	}



}

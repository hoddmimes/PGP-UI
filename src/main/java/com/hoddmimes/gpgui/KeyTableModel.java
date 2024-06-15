package com.hoddmimes.gpgui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPPublicKey;



public class KeyTableModel extends AbstractTableModel
{

	private final int COL_SELECTED = 0;
	private final int COL_USER_ID = 1;
	private final int COL_KEY_ALGO = 2;
	private final int COL_KEY_ID = 3;
	private final int COL_KEY_SIZE = 4;
	private final int COL_KEY_RING = 5;
	
	
	private ColumnData[] mColumnData = null;
	
	
	ArrayList<KeyRingInterface>	mActiveUserKeyRings;
	ArrayList<KeyRingInterface>	mUserKeyRings;
	JTable						mTable;
	KeyRenderer 				mKeyRenderer;
	KeyHeaderRender 			mKeyHeaderRender;
	boolean                     mEnableUserSelections;
		
	KeyTableModel(boolean pEnableUserSelections) {
		mActiveUserKeyRings = new ArrayList<>();
		mUserKeyRings = new ArrayList<>();
		mKeyRenderer = new KeyRenderer();
		mKeyHeaderRender = new KeyHeaderRender();
		mEnableUserSelections = pEnableUserSelections;
		setupColumnData();
	}
	
	private void setupColumnData() {
		if (mEnableUserSelections) {
			mColumnData = new ColumnData[6];
			mColumnData[0] = new ColumnData("Selected",55, JLabel.CENTER, Boolean.class);
			mColumnData[1] = new ColumnData("User Id",350, JLabel.LEFT, String.class);
			mColumnData[2] = new ColumnData("Key Algo",80, JLabel.CENTER, String.class);
			mColumnData[3] = new ColumnData("Key Id",60, JLabel.RIGHT, String.class);
			mColumnData[4] = new ColumnData("Key Size",50, JLabel.RIGHT, String.class);
			mColumnData[5] = new ColumnData("Key Ring",50, JLabel.CENTER, String.class);
		} else {
			mColumnData = new ColumnData[5];
			mColumnData[0] = new ColumnData("User Id",400, JLabel.LEFT, String.class);
			mColumnData[1] = new ColumnData("Key Algo",80, JLabel.CENTER, String.class);
			mColumnData[2] = new ColumnData("Key Id",60, JLabel.RIGHT, String.class);
			mColumnData[3] = new ColumnData("Key Size",50, JLabel.RIGHT, String.class);
			mColumnData[4] = new ColumnData("Key Ring",50, JLabel.CENTER, String.class);
		}
	}
	
	
	public TableCellRenderer getHeaderRender() {
		return mKeyHeaderRender;
	}
	
	
	@Override
	public Class getColumnClass(int pCol) {
		return mColumnData[pCol].mClass;
	}
		
	
	@Override
	public int getRowCount() {
		return mActiveUserKeyRings.size();
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return mColumnData.length;
	}
	
	@Override
	public boolean isCellEditable(int pRowIndex, int pColumnIndex) {
		int tColumnIndex = (mEnableUserSelections) ? pColumnIndex : (pColumnIndex + 1);
        return tColumnIndex == 0;
    }

	@Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (mEnableUserSelections) {
			if (((Boolean) aValue)) {
				for(int i = 0; i < mActiveUserKeyRings.size(); i++) {
					super.setValueAt(false, i, columnIndex); 
					mActiveUserKeyRings.get(i).setIsSelected(false);
				}
			}
			super.setValueAt(aValue, rowIndex, columnIndex);
			KeyRingInterface tUserKey = mActiveUserKeyRings.get(rowIndex);
			tUserKey.setIsSelected((Boolean) aValue);
			fireTableRowsUpdated(0, mActiveUserKeyRings.size() - 1);
		}
    }
	
	
	private KeyRingInterface getKeyRingWithMasterKeyId( long pKeyId ) {
		for( KeyRingInterface k : mActiveUserKeyRings) {
			if (k.getMasterKeyId() == pKeyId) {
				return k;
			}
		}
		return null;
	}
	
	@Override
	public Object getValueAt(int pRowIndex, int pColumnIndex) {
		if (pRowIndex < 0 || pRowIndex > mActiveUserKeyRings.size()) {
			return null;
		}
		KeyRingInterface tUserKeyRing = mActiveUserKeyRings.get(pRowIndex);
		
		int tColumnIndex = (mEnableUserSelections) ? pColumnIndex : (pColumnIndex + 1);
		
		switch( tColumnIndex ) 
		{
			case COL_SELECTED:
				return tUserKeyRing.isSelected();
			case COL_USER_ID:
				return tUserKeyRing.getFirstUserId();
			case COL_KEY_ID:
				return Integer.toHexString((int)(tUserKeyRing.getMasterKeyId() &0xffffffff));
			case COL_KEY_SIZE:
				return toString().valueOf(tUserKeyRing.getBitStrength());
			case COL_KEY_RING:
				return tUserKeyRing.getKeyRingRepositoryName();
			case COL_KEY_ALGO:
				return GPGAdapter.getKeyAlgorithm(tUserKeyRing.getMasterKeyAlgorithm());
			default:
				return null;
		}
	}
	
	public void initiateColumnData( TableColumnModel pColumModel ) {
		for( int i = 0; i < mColumnData.length; i++) {
			TableColumn tc = pColumModel.getColumn(i);
			tc.setHeaderRenderer(mKeyHeaderRender);
			tc.setHeaderValue(mColumnData[i].mHeader);
			tc.setPreferredWidth(mColumnData[i].mPreferredWidth);
		}
	}
	
	public boolean anyUserSelected() {
		return mActiveUserKeyRings.stream().anyMatch(k -> k.isSelected());
	}
	
	public KeyRingInterface getKeyAtRow( int pRow ) {
		if ((pRow >= mActiveUserKeyRings.size()) || (pRow < 0)) {
			return null;
		}
		return mActiveUserKeyRings.get(pRow);
	}
	
	public int getSelectedRow() {
	  for( int i = 0; i < mActiveUserKeyRings.size(); i++) {
		 if (mActiveUserKeyRings.get(i).isSelected()) {
			 return i;
		 }
	  }
	  return -1;
	}
	
	
	public void addKey( KeyRingInterface pUserKey) {
		mActiveUserKeyRings.add( pUserKey );
		mUserKeyRings.add(pUserKey);
	}
		
	public TableCellRenderer getRenderer() {
	  	return mKeyRenderer;
	}
	
	public void applyUserIdFilter( String pUserIdFilter ) {
		mActiveUserKeyRings.clear();
		if (pUserIdFilter == null) {
			mActiveUserKeyRings.addAll(  mUserKeyRings );
		}  else {
			for( KeyRingInterface kri : mUserKeyRings ) {
				if (kri.getFirstUserId().contains(pUserIdFilter)) {
					mActiveUserKeyRings.add(  kri );
				}
			}
		}
		fireTableDataChanged();
	}
	
	class KeyHeaderRender extends DefaultTableCellRenderer
	{
		public Component getTableCellRendererComponent(
                JTable pJTable, Object pValue,
                boolean pIsSelected, boolean pHasFocus,
                int pRow, int pCol) {
			
			JLabel tLabel = new JLabel(pValue.toString());
			tLabel.setHorizontalAlignment(JLabel.CENTER);
			tLabel.setFont( new Font("Arial", Font.PLAIN, 14));
			tLabel.setBackground(Color.black);
			tLabel.setBorder(BorderFactory.createCompoundBorder(tLabel.getBorder(), BorderFactory.createLineBorder(Color.black)));
			return tLabel;
		}
	}
	
	
	class KeyRenderer extends DefaultTableCellRenderer 
	{
		
		
		
		public Component getTableCellRendererComponent(
                JTable pJTable, Object pValue,
                boolean pIsSelected, boolean pHasFocus,
                int pRow, int pCol) {
			
			JLabel tLabel = new JLabel(pValue.toString());
			tLabel.setHorizontalAlignment(mColumnData[pCol].mJustify);
			tLabel.setFont( new Font("Arial", Font.PLAIN, 12));
			tLabel.setBackground(Color.black);
			tLabel.setBorder(BorderFactory.createCompoundBorder(tLabel.getBorder(), BorderFactory.createEmptyBorder(0,5,0,5)));
			return tLabel;

		}
	}

	
	public void removeKeyRing( KeyRingInterface pKeyRingInterface ) {
		for( int i = 0; i < mActiveUserKeyRings.size(); i++ ) {
		  if (mActiveUserKeyRings.get(i).getMasterKeyId() == pKeyRingInterface.getMasterKeyId()) {
			  mActiveUserKeyRings.remove(i);
			  this.fireTableDataChanged();
			  return;
		  }
		}
	}
	
	static class ColumnData {
		String mHeader;
		int mPreferredWidth;
		int mJustify;
		Class mClass;

		public ColumnData(String pHeader, int pWidth, int pJustify, Class pClass) {
			mHeader = pHeader;
			mPreferredWidth = pWidth;
			mJustify = pJustify;
			mClass = pClass;
		}
	}
}

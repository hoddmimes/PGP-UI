package com.hoddmimes.gpgui;



import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

public class PopupMenu extends JPopupMenu 
{
	KeyRingInterface mKeyRingSelected;
	int mX,mY;
    PopupMenu() 
    {	
    	this.setBorder(new EtchedBorder(Color.blue, Color.gray ));
    }
    
    @Override
    public void show( Component pComponent, int pX, int pY) {
    	super.show(pComponent, pX, pY);
    	mY = pX;
    	mX = pX;
    }
    
    public KeyRingInterface getSelectedKeyRing() {
    	return mKeyRingSelected;
    }
    
    public int getXPos() {
    	return mX;
    }
    
    public int getYPos() {
    	return mY;
    }

    public void setSelectedKeyRing( KeyRingInterface pKeyRingInterface ) {
    	mKeyRingSelected = pKeyRingInterface;
    }
    
    public void addMenuItem( String pText, ActionListener pActionListener ) {
    	JMenuItem tItem = new JMenuItem(pText);
    	tItem.setFont(new Font( tItem.getFont().getFamily(), Font.BOLD, 12));
    	tItem.addActionListener(pActionListener);
    	tItem.setHorizontalTextPosition(SwingConstants.RIGHT);
    	this.add(tItem);
    }
}

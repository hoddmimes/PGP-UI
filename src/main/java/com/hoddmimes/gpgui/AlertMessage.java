package com.hoddmimes.gpgui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

public class AlertMessage 
{
	private static Color mColor = new Color( 253,252,244);
	
	private static void display(Component pParentComponent, String pText, int pDisplayTime) {
		JLabel tTextLabel = new JLabel( pText );
		tTextLabel.setHorizontalAlignment(JLabel.CENTER);
		tTextLabel.setFont(new Font(tTextLabel.getFont().getFamily(), Font.BOLD, tTextLabel.getFont().getSize()));
		tTextLabel.setBackground(mColor);
		
		
		
		JFrame tFrame = new JFrame();
		tFrame.setUndecorated(true);
		tFrame.setBackground(mColor);
		tFrame.setMinimumSize(new Dimension(150, 35));
		((JComponent) tFrame.getContentPane()).setBorder(new EtchedBorder());
		tFrame.add(tTextLabel, BorderLayout.CENTER);
		
		Timer tTime = new Timer(pDisplayTime, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tFrame.setVisible(false);
				tFrame.dispose();
			}
		});
		tTime.setRepeats(false);
		tTime.start();
		tTextLabel.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(5, 10, 5, 10), new EtchedBorder()));
	
		tFrame.setLocation(calcLocation(pParentComponent));
		tFrame.pack();
		tFrame.setVisible(true);
	}

	private static Point calcLocation(Component pComponent) {
		if (pComponent == null) {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			Point p = ge.getCenterPoint();
			return p;
		}
		Rectangle r = pComponent.getBounds();
		Point p = pComponent.getLocation();
		Point rp = new Point(p.x + r.width / 2, p.y + r.height / 2);
		return rp;
	}

	public static  void showMessage(Component pParentComponent, String pText, int pDisplayTime)
	{
		display(pParentComponent, pText,  pDisplayTime);
	}
	
	
	
	private static void displayDialog( Component pParentComponent, String pText) {
	
		JLabel tTextLabel = new JLabel( pText );
		tTextLabel.setHorizontalAlignment(JLabel.CENTER);
		tTextLabel.setFont(new Font(tTextLabel.getFont().getFamily(), Font.BOLD, tTextLabel.getFont().getSize()));
		tTextLabel.setBackground(mColor);
		
		JDialog mDialog = new JDialog( );
		mDialog.setModal(true);
		mDialog.setBackground(mColor);
		mDialog.setMinimumSize(new Dimension(150,35));
		
		
		mDialog.setUndecorated(true);
		((JComponent) mDialog.getContentPane()).setBackground(mColor);
		((JComponent) mDialog.getContentPane()).setBorder(BorderFactory.createCompoundBorder(new EtchedBorder(EtchedBorder.RAISED, Color.DARK_GRAY, Color.BLACK), new EmptyBorder(5, 10, 5, 10)));
		mDialog.add(tTextLabel, BorderLayout.CENTER);
		
		JPanel tButtonPanel = new JPanel(new FlowLayout());
		tButtonPanel.setBackground(mColor);
		tButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		Button tOkButton = new Button("OK");
		tOkButton.addActionListener(event -> {mDialog.dispose();});
		tButtonPanel.add(tOkButton);
		mDialog.add(tButtonPanel, BorderLayout.SOUTH);
		mDialog.setLocation(calcLocation(pParentComponent));
		mDialog.pack();
		mDialog.setVisible(true);
	}
	
	public static  void showMessage(Component pParentComponent, String pText)
	{
		displayDialog(pParentComponent, pText);
	}
	
	public static void main(String[] args) {
		try {
			JFrame tFrame = new JFrame("AlterMessage");
			tFrame.setBounds(100, 100, 100, 100);
			
			AlertMessage.showMessage(tFrame, "Kalle Kulla");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

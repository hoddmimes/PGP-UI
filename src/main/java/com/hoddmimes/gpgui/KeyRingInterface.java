package com.hoddmimes.gpgui;


public interface KeyRingInterface 
{
	public long 	getMasterKeyId();
	public String	getFirstUserId();
	public int		getSubKeysElements();
	public int		getMasterKeyAlgorithm();
	public String	getKeyRingRepositoryName();
	public int		getKeyRingRepositoryId();
	
	public void setIsSelected( boolean tFlag );
	public boolean isSelected();
	
	public int getBitStrength();
	
}

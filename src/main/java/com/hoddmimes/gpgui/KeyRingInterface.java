package com.hoddmimes.gpgui;


public interface KeyRingInterface 
{
	long 	getMasterKeyId();
	String	getFirstUserId();
	int		getSubKeysElements();
	int		getMasterKeyAlgorithm();
	String	getKeyRingRepositoryName();
	int		getKeyRingRepositoryId();
	
	void setIsSelected(boolean tFlag);
	boolean isSelected();
	
	int getBitStrength();
	
}

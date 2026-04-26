package com.hoddmimes.pgpgui;

import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;


public interface SecKeyRingInterface extends KeyRingInterface {

	boolean isSecretKeyInRing(long pKeyId);
	
	PGPSecretKey getSecretKey(long pKeyId);
	
	String getSecretKeyUserId();
	
	PGPSecretKeyRing getSecretKeyRing();
	
	
}

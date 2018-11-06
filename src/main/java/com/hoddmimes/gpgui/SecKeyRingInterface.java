package com.hoddmimes.gpgui;

import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;


public interface SecKeyRingInterface extends KeyRingInterface {

	public boolean isSecretKeyInRing( long pKeyId );
	
	public PGPSecretKey getSecretKey( long pKeyId );
	
	public String getSecretKeyUserId();
	
	public PGPSecretKeyRing getSecretKeyRing();
	
	
}

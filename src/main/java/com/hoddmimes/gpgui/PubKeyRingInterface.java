package com.hoddmimes.gpgui;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;

public interface PubKeyRingInterface extends KeyRingInterface 
{
	public PGPPublicKey getPublicEncryptionKey();
	public PGPPublicKeyRing getPublicKeyRing();
}

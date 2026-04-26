package com.hoddmimes.pgpgui;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;

public interface PubKeyRingInterface extends KeyRingInterface 
{
	PGPPublicKey getPublicEncryptionKey();
	PGPPublicKeyRing getPublicKeyRing();
}

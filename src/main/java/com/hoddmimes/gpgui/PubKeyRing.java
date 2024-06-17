package com.hoddmimes.gpgui;

import java.util.Iterator;
import java.util.List;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;


public class PubKeyRing implements PubKeyRingInterface {
	private PGPPublicKeyRing mPubKeyRing;
	private String			 mKeyRingRepository;
	private boolean 		 mIsSelected;
	
	
	public PubKeyRing( PGPPublicKeyRing pKeyRing, String pKeyRingRepository ) {
		mKeyRingRepository = pKeyRingRepository;
		mIsSelected = false;
		mPubKeyRing = pKeyRing;
	}
	
	@Override
	public long getMasterKeyId() {
		return mPubKeyRing.getPublicKey().getKeyID();
	}

	@Override
	public String getFirstUserId() {
		if (mPubKeyRing.getPublicKey().getUserIDs().hasNext()) {
			return mPubKeyRing.getPublicKey().getUserIDs().next();
		} 
		return "<not found>";
	}

	@Override
	public int getSubKeysElements() {
		int i = 0;
		Iterator<PGPPublicKey> tKeyItr = mPubKeyRing.getPublicKeys();
		while( tKeyItr.hasNext()) {
			if (!tKeyItr.next().isMasterKey()) {
				i++;
			}
		}
		return i;
	}

	@Override
	public int getMasterKeyAlgorithm() {
		return mPubKeyRing.getPublicKey().getAlgorithm();
	}

	@Override
	public String getKeyRingRepositoryName() {
		return mKeyRingRepository;
	}

	@Override
	public void setIsSelected(boolean tFlag) {
		mIsSelected = tFlag;
	}

	@Override
	public boolean isSelected() {
		return mIsSelected;
	}

	@Override
	public int getBitStrength() {
		return mPubKeyRing.getPublicKey().getBitStrength();
	}

	private PGPPublicKey findFirstPublicSubKey( Iterator<PGPPublicKey> pKeyItr ) {
		while( pKeyItr.hasNext() ) {
			PGPPublicKey tKey = pKeyItr.next();
			if ((!tKey.isMasterKey()) && (tKey.isEncryptionKey())) {
				return tKey;
			}
		}
		return null;
	}
	
	@Override
	public PGPPublicKey getPublicEncryptionKey() {
		PGPPublicKey tKey = findFirstPublicSubKey( mPubKeyRing.getPublicKeys());
		if (tKey != null) {
			return tKey;
		}
		
		if ((mPubKeyRing.getPublicKey() != null) && (mPubKeyRing.getPublicKey().isEncryptionKey())) {
		  return mPubKeyRing.getPublicKey();
		}
		
		return null;
	}

	@Override
	public PGPPublicKeyRing getPublicKeyRing() {
		return mPubKeyRing;
	}

	@Override
	public int getKeyRingRepositoryId() {
		if (mKeyRingRepository.equals(GPGAdapter.GNUPG_REPOSITORY)) {
			return GPGAdapter.GNUPG_REPOSITORY_INT;
		} else {
			return GPGAdapter.GPGUI_REPOSITORY_INT;
		}
	}
		
}

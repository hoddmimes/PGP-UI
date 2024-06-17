package com.hoddmimes.gpgui;

import java.util.Iterator;

import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;

public class SecKeyRing implements SecKeyRingInterface {
	private PGPSecretKeyRing mSecKeyRing;
	private String			 mKeyRingRepository;
	private boolean 		 mIsSelected;
	
	
	public SecKeyRing( PGPSecretKeyRing pKeyRing, String pKeyRingRepository ) {
		mKeyRingRepository = pKeyRingRepository;
		mIsSelected = false;
		mSecKeyRing = pKeyRing;
	}
	
	
	@Override
	public PGPSecretKeyRing getSecretKeyRing() {
		return mSecKeyRing;
	}
	
	@Override
	public long getMasterKeyId() {
		return mSecKeyRing.getSecretKey().getKeyID();
	}

	@Override
	public String getFirstUserId() {
		if (mSecKeyRing.getSecretKey().getUserIDs().hasNext()) {
			return mSecKeyRing.getSecretKey().getUserIDs().next();
		} 
		return "<not found>";
	}

	@Override
	public int getSubKeysElements() {
		int i = 0;
		Iterator<PGPSecretKey> tKeyItr = mSecKeyRing.getSecretKeys();
		while( tKeyItr.hasNext()) {
			if (!tKeyItr.next().isMasterKey()) {
				i++;
			}
		}
		return i;
	}

	@Override
	public int getMasterKeyAlgorithm() {
		return mSecKeyRing.getSecretKey().getKeyEncryptionAlgorithm();
	}

	@Override
	public String	getKeyRingRepositoryName() {
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
		if (mSecKeyRing.getSecretKey().getPublicKey() != null) {
			return mSecKeyRing.getSecretKey().getPublicKey().getBitStrength();
		}
		return -1;
	}

	@Override
	public boolean isSecretKeyInRing(long pKeyId) {
		Iterator<PGPSecretKey> tKeyItr = mSecKeyRing.getSecretKeys();
		while( tKeyItr.hasNext() ) {
			PGPSecretKey tKey = tKeyItr.next();
			if (tKey.getKeyID() == pKeyId) {
				return true;
			}
		}
		return false;
	}

	@Override
	public PGPSecretKey getSecretKey( long pKeyId ) {
		Iterator<PGPSecretKey> tKeyItr = mSecKeyRing.getSecretKeys();
		while( tKeyItr.hasNext() ) {
			PGPSecretKey tKey = tKeyItr.next();
			if (tKey.getKeyID() == pKeyId) {
				return tKey;
			}
		}
		return null;
	}

	@Override
	public String getSecretKeyUserId() {
		if ((mSecKeyRing.getSecretKey() != null) && (mSecKeyRing.getSecretKey().getUserIDs().hasNext())) {
			return mSecKeyRing.getSecretKey().getUserIDs().next();
		}
		return "<unknown>";
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

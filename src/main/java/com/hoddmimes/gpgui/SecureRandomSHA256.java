package com.hoddmimes.gpgui;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Random;

//import org.bouncycastle.crypto.engines.ext.NumConvert;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class SecureRandomSHA256 extends SecureRandom
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MessageDigest mSHA256 = null;
	private byte[] mState;
	
	
	public SecureRandomSHA256() {
		super();
		try {
			if (mSHA256 == null) {
				mSHA256 = MessageDigest.getInstance("SHA256", "BC");
			}
			SecureRandom tRand = new SecureRandom();
			tRand.setSeed(getSeed());
			mState = new byte[64];
			tRand.nextBytes(mState);
		}
		catch(Exception e) { e.printStackTrace(); }
	}
	
	public SecureRandomSHA256(byte[] pSeed) {
		super(pSeed);
		try {
			if (mSHA256 == null) {
				mSHA256 = MessageDigest.getInstance("SHA256", "BC");
			}
			
			mState = new byte[64];
			super.nextBytes(mState);
		}
		catch(Exception e) { e.printStackTrace(); }
	}
	
	public void setPredictableSeed( byte[] pPredictableSeed) {
		try {
				mSHA256 = MessageDigest.getInstance("SHA256", "BC");
			}
		catch(Exception e) { e.printStackTrace(); }
			mState = new byte[64];
			for( int i = 0;  i < mState.length; i++ ) {
				mState[i] = pPredictableSeed[ i % pPredictableSeed.length];
			}
	}
	
	private long getSeed() {
		  long t = System.currentTimeMillis();
	       long h =  fugazi(Runtime.getRuntime().hashCode());
	      long f = fugazi(  this.hashCode() );
	      long s = (t ^ h ^ f);
	      return s;
	}
	
	private long fugazi( int pValue ) {
		double x = pValue;
		while( x > 1.0d ) {
			x = x / 10.0d;
		}
		double y =   Math.exp(-x*x / 2) / Math.sqrt(2 * Math.PI);
		long l = (long) ( 10000000000000000L * y);
		return l;
	}
	

	
	@Override
	public String getAlgorithm() {
		return "SecureRandomSHA256";
	}
	
	@Override
	public void setSeed( long pSeed ) {
		if (mSHA256 == null) {
			try {
				mSHA256 = MessageDigest.getInstance("SHA256", "BC");
			}
			catch( Exception e) {
				e.printStackTrace();
			}
		}
		Random tRand = new Random(pSeed);
		mState = new byte[64];
		tRand.nextBytes(mState);
	}
	
	@Override 
	public void nextBytes(byte[] bytes)
	{
		int    off = 0;
        mSHA256.update(mState);
        while (off < bytes.length)
        {                
        	mState = mSHA256.digest();

			int length = Math.min(bytes.length - off, mState.length);
			System.arraycopy(mState, 0, bytes, off, length);

            off += mState.length;

            mSHA256.update(mState);
        }
    }
	
	
	@Override 
	public int nextInt() {
		    byte[] tIntBytes = new byte[4];
	        this.nextBytes(tIntBytes);
            return NumConvert.bytesToInt(tIntBytes, 0);
	}
	
	public long nextLong() {
	    byte[] tLongBytes = new byte[8];
        this.nextBytes(tLongBytes);
        return NumConvert.bytesToLong(tLongBytes, 0);
}
	
	public static void main( String[] pArgs ) {
		Security.addProvider(new BouncyCastleProvider());
		byte[] tSeed = {'3','k','j','l','5','9'};
		long tPredicableSeed = 4711279313L;
		SecureRandomSHA256 r = new SecureRandomSHA256( );
		r.setPredictableSeed(tSeed);
//		for( int i = 0;  i < 5; i++ ) {
//			System.out.println( i + "   " + Integer.toHexString( r.nextInt()));
//		}
	}
}

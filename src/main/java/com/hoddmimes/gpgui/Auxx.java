package com.hoddmimes.gpgui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;


//import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyRingGenerator;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;

public class Auxx 
{
	 
	  public static  FileOutputStream createIfNotExists( String pFilename ) throws IOException{
		  File tFile = new File(pFilename);
		  if ((!tFile.exists()) || (!tFile.canWrite())) {
			  int tIndex = pFilename.lastIndexOf(File.separator);
			  if (tIndex >= 0) {
				  String tDirs = pFilename.substring(0, tIndex);
				  File tDirsFile = new File(tDirs);
				  if (!tDirsFile.exists()) {
					  tDirsFile.mkdirs();
				  }
			  }
		  }
		  FileOutputStream tOut = new FileOutputStream( tFile, false);
		  return tOut;
	  }

	 
	  public static PGPSecretKeyRingCollection getSecretKeyCollection( String pFilename ) throws IOException,PGPException
	  {
		  FileInputStream tFileIn = null;
		  File tFile = new File( pFilename );
		  if (tFile.exists()) {
			  tFileIn = new FileInputStream(tFile);
		  } else {
			  return new PGPSecretKeyRingCollection( new ArrayList<>());
		  }

		  InputStream tIn = PGPUtil.getDecoderStream(tFileIn);
		  PGPSecretKeyRingCollection skc = new PGPSecretKeyRingCollection( tIn,  new BcKeyFingerprintCalculator());
		  tIn.close();
		  return skc;
	  }

		 
	  private static PGPPublicKeyRingCollection getPublicKeyCollection(String pFilename ) throws IOException,PGPException
	  {
		  FileInputStream tFileIn = null;
		  File tFile = new File( pFilename );
		  if (tFile.exists()) {
			  tFileIn = new FileInputStream(tFile);
		  } else {
			  return new PGPPublicKeyRingCollection( new ArrayList<>());
		  }

	
		  InputStream tIn = PGPUtil.getDecoderStream(tFileIn);
		  PGPPublicKeyRingCollection pkc = new PGPPublicKeyRingCollection( tIn,  new BcKeyFingerprintCalculator());
		  tIn.close();
		  return pkc;
	  } 

	 
   public static void deleteAndSavePublicKeys( PubKeyRing pKeyRing, int pRespType ) throws IOException, PGPException  {
		Settings tSettings = Settings.getInstance();
		PGPPublicKeyRingCollection tKeyCollection = null;
		String tFilename = null;
		
		
		
		
		/**
		 * Check if key is found in PGPUI key ring
		 */
		tFilename = (pRespType == GPGAdapter.GNUPG_REPOSITORY_INT) ? tSettings.getPGPPublicKeyRingFilename() : tSettings.getPGPUIPublicKeyRingFilename();
		tKeyCollection = getPublicKeyCollection( tFilename );
		if (tKeyCollection.contains(pKeyRing.getMasterKeyId())) {
			tKeyCollection = PGPPublicKeyRingCollection.removePublicKeyRing(tKeyCollection, pKeyRing.getPublicKeyRing());
		} else {
			tFilename = tSettings.getPGPPublicKeyRingFilename();
			tKeyCollection = getPublicKeyCollection( tFilename );
			if (tKeyCollection.contains(pKeyRing.getMasterKeyId())) {
				tKeyCollection = PGPPublicKeyRingCollection.removePublicKeyRing(tKeyCollection, pKeyRing.getPublicKeyRing());
			} else {
				tFilename = null;
				tKeyCollection = null;
			}
		}
		
		if (tKeyCollection == null) {
			throw new IOException("Key " +  Long.toHexString(pKeyRing.getMasterKeyId())  + " not found");
		}
		
		// Save the new collection
		ByteArrayOutputStream tOutByteStream = new ByteArrayOutputStream();
//		ArmoredOutputStream tArmored = new ArmoredOutputStream(tOutByteStream);
//		tKeyCollection.encode(tArmored);
//		tArmored.close();
		tKeyCollection.encode(tOutByteStream);
		
		FileOutputStream tOutFile = createIfNotExists( tFilename );
		tOutFile.write(tOutByteStream.toByteArray());
		tOutFile.flush();
		tOutFile.close();
   }
   
   
   public static void deleteAndSaveSecretKeys( SecKeyRing pKeyRing, int pRespType ) throws IOException, PGPException  {
		Settings tSettings = Settings.getInstance();
		PGPSecretKeyRingCollection tKeyCollection = null;
		String tFilename = null;
		
		
		/**
		 * Check if key is found in PGPUI key ring
		 */
		tFilename = (pRespType == GPGAdapter.GNUPG_REPOSITORY_INT) ? tSettings.getPGPSecretKeyRingFilename() : tSettings.getPGPUISecretKeyRingFilename();
		tKeyCollection = getSecretKeyCollection( tFilename );
		if (tKeyCollection.contains(pKeyRing.getMasterKeyId())) {
			tKeyCollection = PGPSecretKeyRingCollection.removeSecretKeyRing(tKeyCollection, pKeyRing.getSecretKeyRing());
		} else {
			tFilename =  tSettings.getPGPSecretKeyRingFilename();
			tKeyCollection = getSecretKeyCollection( tFilename );
			if (tKeyCollection.contains(pKeyRing.getMasterKeyId())) {
				tKeyCollection = PGPSecretKeyRingCollection.removeSecretKeyRing(tKeyCollection, pKeyRing.getSecretKeyRing());
			} else {
				tKeyCollection = null;
				tFilename = null;
			}
		}
		
		if (tKeyCollection == null) {
			throw new IOException("Key " +  Long.toHexString(pKeyRing.getMasterKeyId())  + " not found");
		}
		
		// Save the new collection
		ByteArrayOutputStream tOutByteStream = new ByteArrayOutputStream();
		//ArmoredOutputStream tArmored = new ArmoredOutputStream(tOutByteStream);
		//tKeyCollection.encode(tArmored);
		//tArmored.close();
		tKeyCollection.encode(tOutByteStream);
		
		FileOutputStream tOutFile = createIfNotExists(tFilename);
		tOutFile.write(tOutByteStream.toByteArray());
		tOutFile.flush();
		tOutFile.close();
   }
   
   

	 
	public static void addAndSaveSecretKeys(PGPKeyRingGenerator pKeyRing, int pRespType) throws IOException, PGPException {
		addAndSaveSecretKeys( pKeyRing.generateSecretKeyRing(), pRespType);
	}
	
//	public static void addAndSaveSecretKeys(PGPSecretKeyRing pKeyRing) throws IOException, PGPException {
//		Settings tSettings = Settings.getInstance();
//		PGPSecretKeyRingCollection tKeyCollection = getSecretKeyCollection();
//		PGPSecretKeyRingCollection.addSecretKeyRing(tKeyCollection, pKeyRing);
//
//		ByteArrayOutputStream tOutByteStream = new ByteArrayOutputStream();
//		ArmoredOutputStream tArmored = new ArmoredOutputStream(tOutByteStream);
//		tKeyCollection.encode(tArmored);
//		FileOutputStream tOutFile = createIfNotExists(tSettings.getPGPUISecretKeyRingFilename());
//		tOutFile.write(tOutByteStream.toByteArray());
//		tOutFile.flush();
//		tOutFile.close();
//	}
	
	
	private static boolean isKeyInSecretKeyRing( PGPSecretKeyRingCollection pKeyRingCollection, PGPSecretKeyRing pKeyRing ) {
		
		Iterator<PGPSecretKeyRing> tKeyRingItr = pKeyRingCollection.getKeyRings();
		while( tKeyRingItr.hasNext()) {
			PGPSecretKeyRing tKeyRing = tKeyRingItr.next();
			Iterator<PGPSecretKey> tKeyItr = tKeyRing.getSecretKeys();
			while(tKeyItr.hasNext()) {
				PGPSecretKey tKey = tKeyItr.next();
				if (pKeyRing.getSecretKey(tKey.getKeyID()) != null) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	private static boolean isKeyInPublicKeyRing( PGPPublicKeyRingCollection pKeyRingCollection, PGPPublicKeyRing pKeyRing ) {
		
		Iterator<PGPPublicKeyRing> tKeyRingItr = pKeyRingCollection.getKeyRings();
		while( tKeyRingItr.hasNext()) {
			PGPPublicKeyRing tKeyRing = tKeyRingItr.next();
			Iterator<PGPPublicKey> tKeyItr = tKeyRing.getPublicKeys();
			while(tKeyItr.hasNext()) {
				PGPPublicKey tKey = tKeyItr.next();
				if (pKeyRing.getPublicKey(tKey.getKeyID()) != null) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static void addAndSaveSecretKeys(PGPSecretKeyRing pKeyRing, int pRespType) throws IOException, PGPException {
		Settings tSettings = Settings.getInstance();
		String tFilename;
		
		if (pRespType == GPGAdapter.GNUPG_REPOSITORY_INT) {
			tFilename = tSettings.getPGPSecretKeyRingFilename();
		} else {
			tFilename = tSettings.getPGPUISecretKeyRingFilename();
		}
		
		
		PGPSecretKeyRingCollection tKeyCollection = getSecretKeyCollection( tFilename);
		if (isKeyInSecretKeyRing(tKeyCollection,pKeyRing)) {
		   throw new PGPException("Secret key already exists");
		}

		tKeyCollection = PGPSecretKeyRingCollection.addSecretKeyRing(tKeyCollection, pKeyRing);
		
		ByteArrayOutputStream tOutByteStream = new ByteArrayOutputStream();
		
		//ArmoredOutputStream tArmored = new ArmoredOutputStream(tOutByteStream);
		//tKeyCollection.encode(tArmored);
		//tArmored.close();
		tKeyCollection.encode(tOutByteStream);
		
		FileOutputStream tOutFile = createIfNotExists(tFilename);
		tOutFile.write(tOutByteStream.toByteArray());
		tOutFile.flush();
		tOutFile.close();
	}

	public static void addAndSavePublicKeys(PGPPublicKeyRing pKeyRing, int pRespType) throws IOException, PGPException {
		Settings tSettings = Settings.getInstance();
		String tFilename;
		
		if (pRespType == GPGAdapter.GNUPG_REPOSITORY_INT) {
			tFilename = tSettings.getPGPPublicKeyRingFilename();
		} else {
			tFilename = tSettings.getPGPUIPublicKeyRingFilename();
		}
		PGPPublicKeyRingCollection tKeyCollection = getPublicKeyCollection( tFilename );
		
		
		if (isKeyInPublicKeyRing(tKeyCollection,pKeyRing)) {
		   throw new PGPException("Public key already exists");
		}
		tKeyCollection = PGPPublicKeyRingCollection.addPublicKeyRing(tKeyCollection, pKeyRing);

		ByteArrayOutputStream tOutByteStream = new ByteArrayOutputStream();
		//ArmoredOutputStream tArmored = new ArmoredOutputStream(tOutByteStream);
		//tKeyCollection.encode(tArmored);
		//tArmored.close();
		tKeyCollection.encode(tOutByteStream);
		
		FileOutputStream tOutFile = createIfNotExists(tFilename);
		tOutFile.write(tOutByteStream.toByteArray());
		tOutFile.flush();
		tOutFile.close();
	}
	 
	public static void addAndSavePublicKeys(PGPKeyRingGenerator pKeyRing, int pRespType) throws IOException, PGPException {
		addAndSavePublicKeys(pKeyRing.generatePublicKeyRing(), pRespType );
	}

	
	public static boolean ifKeyRingFileExists( String pFilename ) {
		File tFile = new File(pFilename);
		if (tFile.exists() && tFile.canRead()) {
			return true;
		}
		return false;
	}
}



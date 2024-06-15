package com.hoddmimes.gpgui;

import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPOnePassSignature;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.PGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;


import javax.imageio.ImageIO;


public class GPGAdapter
{
	static final String GNUPG_REPOSITORY = "GnuPG";
	static final String GPGUI_REPOSITORY = "PGPGUI";
	static final int GNUPG_REPOSITORY_INT = 0;
	static final int GPGUI_REPOSITORY_INT = 1;
	
	private static SimpleDateFormat cSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private static GPGAdapter cInstance = null;
	
	public interface GetPasswordInterface 
	{
		public char[] getPasswordForDecrypt( long pKeyId, String puserId );
		public Component getComponent();
	}
	
	public interface DecryptInterface 
	{
		public void 	decryptedMessage( byte[] pMessageBytes);
		public char[]   getPasswordForDecrypt( long pKeyId, String pUserId);
		public void 	encryptSignOnePassUsers( String pUserList);
		public void 	encryptSignUsers( String pUserList);
	}

	static void setAppIcon( Object pSourceObject, java.awt.Window pWindow) {
		try {

			URL tURL = pSourceObject.getClass().getClassLoader().getResource("lock32.png");
			if (tURL == null) {
				tURL = pSourceObject.getClass().getResource("/resources/lock32.png");
			}
			if (tURL != null) {
				pWindow.setIconImage(ImageIO.read( tURL));
			} else {
//				FileOutputStream tOut = new FileOutputStream("frotz.frotz");
//				tOut.write(42);
//				tOut.close();
				File tFile = new File("./resources/lock32.png");
				if (!tFile.exists()) {
					tFile = new File("./gpgui/resources/lock32.png");
				}
				pWindow.setIconImage(ImageIO.read( tFile ));
			}
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}


	public static GPGAdapter getInstance() 
	{
		if (cInstance == null)  
		{
			cInstance = new GPGAdapter();
			Security.addProvider(new BouncyCastleProvider());
		}
		return cInstance;
	}
	
	
	
	static String getKeyAlgorithm( int pAlgorithm ) {
		switch( pAlgorithm ) {
			case PGPPublicKey.DIFFIE_HELLMAN:
				return "DFH";
			case PGPPublicKey.DSA:
				return "DSA";
			case PGPPublicKey.ECDH:
				return "ECDH";
			case PGPPublicKey.ECDSA:
				return "ECDSA";
			case PGPPublicKey.ELGAMAL_ENCRYPT:
				return "ELGAMAL";
			case PGPPublicKey.ELGAMAL_GENERAL:
				return "ELGAMAL";
			case PGPPublicKey.RSA_ENCRYPT:
				return "RSA";
			case PGPPublicKey.RSA_GENERAL:
				return "RSA";
			case PGPPublicKey.RSA_SIGN:
				return "RSA";
			default:
				return "Unknown";
		}
	}
	
	
	
	private List<PGPSecretKeyRing> getSecretPGPKeyRings( String pKeyRingFilename) throws Exception 
	{
		PGPSecretKeyRingCollection tSecretKeyRingCollection = null;
		ArrayList<PGPSecretKeyRing> tKeyRingList = new ArrayList<PGPSecretKeyRing>();

		File tInFile = new File(pKeyRingFilename);
		if ((!tInFile.exists()) || (!tInFile.canRead())) {
			return tKeyRingList;
		}

		try {
			tSecretKeyRingCollection = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(Files.newInputStream(Paths.get(pKeyRingFilename))), new JcaKeyFingerprintCalculator());
		}
		catch( Exception e) {
			throw new IOException("Failed to open\"" + pKeyRingFilename + "\" key ring.");
		}

		Iterator tKeyRingItr = tSecretKeyRingCollection.getKeyRings();
			
		while (tKeyRingItr.hasNext()) {
			tKeyRingList.add( (PGPSecretKeyRing) tKeyRingItr.next());
		}
		return tKeyRingList;
	}
	
	
	 private List<PGPPublicKeyRing> getPublicPGPKeyRings( String pKeyRingFilename) throws Exception 
	 {
		PGPPublicKeyRingCollection tPublicKeyRingCollection = null;
		ArrayList<PGPPublicKeyRing> tKeyRingList = new ArrayList<PGPPublicKeyRing>();

		File tInFile = new File(pKeyRingFilename);
		if ((!tInFile.exists()) || (!tInFile.canRead())) {
			return tKeyRingList;
	   	}

		try {
			tPublicKeyRingCollection = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(Files.newInputStream(Paths.get(pKeyRingFilename))), new JcaKeyFingerprintCalculator());
	   	}
		catch( Exception e) {
			throw new IOException("Failed to open\"" + pKeyRingFilename + "\" key ring.");
	   	}

		Iterator tKeyRingItr = tPublicKeyRingCollection.getKeyRings();
	   		
		while (tKeyRingItr.hasNext()) {
			tKeyRingList.add( (PGPPublicKeyRing) tKeyRingItr.next());
	   	}
		return tKeyRingList;
	 }
	

	 public List<KeyRingInterface> getSecretKeyRings() throws Exception {
		 List<PGPSecretKeyRing> tList = null;
		 ArrayList<KeyRingInterface> tKeyRingList = new ArrayList<KeyRingInterface>();
		 
		 tList = getSecretPGPKeyRings( Settings.getInstance().getPGPSecretKeyRingFilename());
		 for( PGPSecretKeyRing kr : tList ) {
			 tKeyRingList.add( new SecKeyRing(kr, GNUPG_REPOSITORY));
		 }
		 
		 tList = getSecretPGPKeyRings( Settings.getInstance().getPGPUISecretKeyRingFilename());
		 for( PGPSecretKeyRing kr : tList ) {
			 tKeyRingList.add( new SecKeyRing(kr, GPGUI_REPOSITORY));
		 }
		 
		 return tKeyRingList;
	 }
	 
	 public List<KeyRingInterface> getPublicKeyRings() throws Exception {
		 List<PGPPublicKeyRing> tList = null;
		 ArrayList<KeyRingInterface> tKeyRingList = new ArrayList<KeyRingInterface>();
		 
		 tList = getPublicPGPKeyRings( Settings.getInstance().getPGPPublicKeyRingFilename());
		 for( PGPPublicKeyRing kr : tList ) {
			 tKeyRingList.add( new PubKeyRing(kr,  GNUPG_REPOSITORY));
		 }
		 
		 tList = getPublicPGPKeyRings( Settings.getInstance().getPGPUIPublicKeyRingFilename());
		 for( PGPPublicKeyRing kr : tList ) {
			 tKeyRingList.add( new PubKeyRing(kr,  GPGUI_REPOSITORY));
		 }
		 
		 return tKeyRingList;
	 }

	private SecKeyRingInterface findSecretKeyRing(long pKeyId) throws Exception {
		List<KeyRingInterface> tKeyRings = (List<KeyRingInterface>) this.getSecretKeyRings();
		for (KeyRingInterface kr : tKeyRings) {
			SecKeyRingInterface skr = (SecKeyRingInterface) kr;
			if (skr.isSecretKeyInRing(pKeyId)) {
				return skr;
			}
		}
		return null;
	}

	static public String getValidKeyTime(long pSeconds) {
		if (pSeconds == 0) {
			return "<no termination set>";
		}
		long days = (pSeconds / 86400L);
		long hh = (pSeconds - (days * 86400L)) / 3600L;
		long min = (pSeconds - (days * 86400L) - (hh * 3600)) / 60L;
		long ss = pSeconds % 60L;
		return String.format("%d %02d:%02d:%02d", days, hh, min, ss);
	}

	 private int getEncryptionAlgorithm( String pAlgo, int pAESkeyStrength ) throws InvalidParameterException {
		if (PGPGUI.USE_EXTENTION) {
			throw new InvalidParameterException("Not configured for Extended use");
		}
		if (pAlgo.compareTo(PGPGUI.ENCRYPT_ALGO_AES) == 0) {
		  return PGPEncryptedData.AES_256;
		}
//		  if (pAESkeyStrength == 256) {
//			  return PGPEncryptedData.AES_256;
//		  } else if (pAESkeyStrength == 512) { 
//			  return PGPEncryptedData.AESx2; 
//		  } else if (pAESkeyStrength == 1024) { 
//			  return PGPEncryptedData.AESx4; 
//		  } else if (pAESkeyStrength == 2048) { 
//			  return PGPEncryptedData.AESx8; 
//		  } else {
//			  throw new InvalidParameterException("Unknown encryption algorithm (" + pAlgo + ")");
//		  }
		else if (pAlgo.compareTo(PGPGUI.ENCRYPT_ALGO_BLOWFISH) == 0) {
			return PGPEncryptedData.BLOWFISH;
		} else if (pAlgo.compareTo(PGPGUI.ENCRYPT_ALGO_CAMELLIA_256) == 0) {
			return PGPEncryptedData.CAMELLIA_256;
		} else if (pAlgo.compareTo(PGPGUI.ENCRYPT_ALGO_CAST5) == 0) {
			return PGPEncryptedData.CAST5;
		} else if (pAlgo.compareTo(PGPGUI.ENCRYPT_ALGO_IDEA) == 0) {
			return PGPEncryptedData.IDEA;
//		} else if (pAlgo.compareTo(PGPGUI.ENCRYPT_ALGO_SNOWFLAKE) == 0) {
//			return PGPEncryptedData.SNOWFLAKE;
		} else {
			throw new InvalidParameterException("Unknown encryption algorithm (" + pAlgo + ")");
		}
	 }
	 
	 
	 public ByteArrayOutputStream encryptMessage( byte[] pInMessage, PGPPublicKey pPubKey, PGPSecretKeyRing pSecSignKeyRing,  GetPasswordInterface pGetPasswordInterface,  String pEncryptAlgo, int pAESStrength ) throws Exception {
		 
		 PGPSignatureGenerator tSignatureGenerator = null;
		 PGPCompressedDataGenerator tCompressDataGen = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);
		 ByteArrayOutputStream tCompressByteArrayOutputStream = new ByteArrayOutputStream(); 
		 
		 OutputStream tCompOutputStream = tCompressDataGen.open(tCompressByteArrayOutputStream); 
		
		 if (pSecSignKeyRing != null) {
			 PGPSecretKey tSecSignKey = pSecSignKeyRing.getSecretKey();
			 char[] tPassword = Settings.getInstance().getPasswordForKeyId(tSecSignKey.getKeyID());
						 
			 if (tPassword == null) {
				 tPassword = pGetPasswordInterface.getPasswordForDecrypt(tSecSignKey.getKeyID(), (String) pSecSignKeyRing.getPublicKey().getUserIDs().next());
			 }	 
			 
			 PGPPrivateKey pgpPrivKey = getPrivateKey(tSecSignKey, tPassword);
			 
			 tSignatureGenerator = new PGPSignatureGenerator( new BcPGPContentSignerBuilder(tSecSignKey.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA256 ));
		                  
			 tSignatureGenerator.init(PGPSignature.BINARY_DOCUMENT, pgpPrivKey);
			 
			 tSignatureGenerator.generateOnePassVersion(false).encode(tCompOutputStream);
			 
		 }
		 
		 
		 PGPLiteralDataGenerator tLitterdataData = new PGPLiteralDataGenerator();
		
		 
		 OutputStream tLitteralOutputStream = tLitterdataData.open(tCompOutputStream, // the compressed output stream
	                											   PGPLiteralData.BINARY, "console", // "filename" to store
	                											   pInMessage.length, // length of clear data
	                											   new Date()); // current time
		 
		 tLitteralOutputStream.write( pInMessage ); // write litter data, compressed to "tCompressByteArrayOutputStream" (which will be the input later on)
		 if (tSignatureGenerator != null ) {
		   tSignatureGenerator.update(pInMessage);
		 }
		 
		 tLitteralOutputStream.flush();
		 tLitteralOutputStream.close();
		 if (tSignatureGenerator != null ) {
			 tSignatureGenerator.generate().encode(tCompOutputStream);
		 }
		 tCompOutputStream.flush();
		 tCompOutputStream.close();
		 
		// Init encrypted data generator
		 ByteArrayOutputStream tOutByteArrayOutputStream = new ByteArrayOutputStream(); 
		 OutputStream tOutStream = new ArmoredOutputStream(tOutByteArrayOutputStream);
		 
		 
		 
		 PGPDataEncryptorBuilder tEncBuilder = new BcPGPDataEncryptorBuilder(getEncryptionAlgorithm( pEncryptAlgo, pAESStrength)).setWithIntegrityPacket(true);
				 		 
		 PGPEncryptedDataGenerator tEncDataGenerator = new PGPEncryptedDataGenerator(tEncBuilder);
		 tEncDataGenerator.addMethod(new BcPublicKeyKeyEncryptionMethodGenerator(pPubKey));

		 OutputStream tEncryptedOut = tEncDataGenerator.open(tOutStream, new byte[1024]);
		 tEncryptedOut.write(tCompressByteArrayOutputStream.toByteArray());

		 tEncryptedOut.flush();
		 tEncryptedOut.close();
		    
		 tOutStream.flush();
		 tOutStream.close();
		 return tOutByteArrayOutputStream;
	 }
	 
	 public double encryptFile( File pInfile, File pOutFile,  PGPPublicKey pPubKey, PGPSecretKeyRing pSecSignKeyRing, boolean pArmoredFlag,  String pEncryptAlgo, int pAESStrength  ) throws Exception {
		 int tBytesRead;
		 long tStartTime = System.currentTimeMillis();
		 
		 File tTmpFile = File.createTempFile("pgpui", null);
		 tTmpFile.deleteOnExit();
		 OutputStream tTmpOutputStream = Files.newOutputStream(tTmpFile.toPath());
		 

		 
		 PGPCompressedDataGenerator tCompressDataGen = new PGPCompressedDataGenerator(PGPCompressedData.ZIP); 
		 
		 OutputStream tCompOutputStream = tCompressDataGen.open(tTmpOutputStream); 
		
		 PGPLiteralDataGenerator tLitterdataData = new PGPLiteralDataGenerator();
		 
		 PGPUtil.writeFileToLiteralData(tCompOutputStream, // the compressed output stream
				 					    PGPLiteralData.BINARY,
				 					    pInfile );
		 
		 tCompOutputStream.flush();
		 tCompOutputStream.close();
		 tTmpOutputStream.flush();
		 tTmpOutputStream.close();
		 
		 
		// Init encrypted data generator
		 OutputStream tFileOutputStream = Files.newOutputStream(pOutFile.toPath());
		 OutputStream tOutStream = (pArmoredFlag) ? new ArmoredOutputStream(tFileOutputStream) : tFileOutputStream;
		 
		 InputStream tTmpInputStream = Files.newInputStream(tTmpFile.toPath());
		 
		 
		 PGPDataEncryptorBuilder tEncBuilder = new BcPGPDataEncryptorBuilder(getEncryptionAlgorithm( pEncryptAlgo, pAESStrength)).setWithIntegrityPacket(true);
		 
		 PGPEncryptedDataGenerator tEncDataGenerator = new PGPEncryptedDataGenerator(tEncBuilder);
		 tEncDataGenerator.addMethod(new BcPublicKeyKeyEncryptionMethodGenerator(pPubKey));

		 OutputStream tEncryptedOut = tEncDataGenerator.open(tOutStream, new byte[2048]);
		 byte[] tBuffer = new byte[1024];
		 while ((tBytesRead = tTmpInputStream.read(tBuffer, 0, tBuffer.length)) != -1) {
			 tEncryptedOut.write(tBuffer,0,tBytesRead);
		 }

		 tEncryptedOut.flush();
		 tEncryptedOut.close();
		 
		 tOutStream.flush();
		 tFileOutputStream.flush();
		 tOutStream.close();
		 if (pArmoredFlag) {
			 tFileOutputStream.close(); 
		 }
		 double tSec = (double) ((double)(System.currentTimeMillis() - tStartTime) / 1000.0d);
		 
		 tTmpInputStream.close();
		 tTmpFile.delete();
		 return tSec;
	 }
		 
	 private PGPPrivateKey getPrivateKey( PGPSecretKey pSecretKey, char[] pPassword ) throws PGPException {
		    PBESecretKeyDecryptor tDecryptor = new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider()).build(pPassword);
		    return pSecretKey.extractPrivateKey(tDecryptor);
		}
		
	 private String getUserIdFromSecretKey( PGPSecretKey pSeckey ) {
		 Iterator<byte[]> tUsrIdItr = pSeckey.getPublicKey().getRawUserIDs();
		 String tUserId = "<unknown>";
		 if (tUsrIdItr.hasNext()) {
			 try {tUserId = new String( tUserId.getBytes(), StandardCharsets.UTF_8);}
			 catch( Exception e) {
				 e.printStackTrace();
			 }	
		 }
		 return tUserId;
	 }
	 
	private byte[] readPGPLiteralData(PGPLiteralData pLiteralData) throws IOException {
		ByteArrayOutputStream tOutStream = new ByteArrayOutputStream();
		int tBytesRead = 0;

		// System.out.println("Orginal File: " +
		// tLitteralData.getFileName() + " ModTime: " +
		// cSDF.format(tLitteralData.getModificationTime()));

		InputStream tInputStream = pLiteralData.getInputStream();
		byte[] tBuffer = new byte[1024];
		while ((tBytesRead = tInputStream.read(tBuffer, 0, tBuffer.length)) != -1) {
			tOutStream.write(tBuffer, 0, tBytesRead);
		}
		tOutStream.flush();
		return tOutStream.toByteArray();
	}
	 
	 public void decryptMessage( byte[] pEncryptedBytes, DecryptInterface pDecryptInterface) throws Exception {
		 InputStream  tInStream = null;
		 byte[] tDecryptBytes = null;
		 SecKeyRingInterface  tSecKeyRing  = null;
		 PGPEncryptedDataList tEncDataList = null;
		 PGPPrivateKey tPrivKey = null;
		 int tBytesRead;
		 
		 try { tInStream = PGPUtil.getDecoderStream(new ByteArrayInputStream(pEncryptedBytes)); }
		 catch(IOException e) { throw new IOException("failed to decode encrypted stream (" + e.getMessage() + ")");}
		 
		 PGPObjectFactory tPGPObjectFactory = new PGPObjectFactory(tInStream, new BcKeyFingerprintCalculator());
		 Object tPGPObject = tPGPObjectFactory.nextObject();
		
		// the first object might be a PGP marker packet.
		 tEncDataList = (tPGPObject instanceof  PGPEncryptedDataList) ? (PGPEncryptedDataList) tPGPObject : (PGPEncryptedDataList) tPGPObjectFactory.nextObject();
		 
		 for (PGPEncryptedData tEncDataEl : tEncDataList) {
			 PGPPublicKeyEncryptedData tEncData = (PGPPublicKeyEncryptedData)tEncDataEl;
			 tSecKeyRing = GPGAdapter.getInstance().findSecretKeyRing(tEncData.getKeyID());
			 
			 if (tSecKeyRing == null) {
				throw new Exception("failed to find decryption key (" + Long.toHexString(tEncData.getKeyID()) + ")");
			 }
			 	 
			 char[] tPassword = Settings.getInstance().getPasswordForKeyId(tEncData.getKeyID());
				
			 tPassword = Settings.getInstance().getPasswordForKeyId(tEncData.getKeyID());
				 
			 if (tPassword == null) {
				 tPassword = pDecryptInterface.getPasswordForDecrypt(tEncData.getKeyID(), tSecKeyRing.getSecretKeyUserId());
			 }	 
			 
			

			 tPrivKey = getPrivateKey(tSecKeyRing.getSecretKey(tEncData.getKeyID()), tPassword); 
			 Settings.getInstance().savePasswordInCache( tEncData.getKeyID(), tPassword);  
			 //System.out.println("Encrypt key id: " + Long.toHexString(tEncData.getKeyID()) + " Key Id Found: " + Long.toHexString(tPrivKey.getKeyID()));
	
			 InputStream tClearInputStream = tEncData.getDataStream(new BcPublicKeyDataDecryptorFactory(tPrivKey));
			 
			 PGPObjectFactory tPlainFact = new PGPObjectFactory(tClearInputStream, new BcKeyFingerprintCalculator());
			 Object tPlainMsgObj = tPlainFact.nextObject();
			 while( tPlainMsgObj != null) {
				 if (tPlainMsgObj instanceof PGPCompressedData) {
					PGPCompressedData tCompressedData = (PGPCompressedData) tPlainMsgObj;
					PGPObjectFactory tCompFact = new PGPObjectFactory(tCompressedData.getDataStream(), new BcKeyFingerprintCalculator());
					Object tCompMsgObj = tCompFact.nextObject();
					while( tCompMsgObj != null ) {
						 if (tCompMsgObj instanceof PGPLiteralData) {
							 pDecryptInterface.decryptedMessage(readPGPLiteralData((PGPLiteralData) tCompMsgObj ));
						 } else if (tCompMsgObj instanceof PGPOnePassSignatureList) {
							 pDecryptInterface.encryptSignOnePassUsers(checkSignatureList((PGPOnePassSignatureList) tCompMsgObj));
						 } else if (tCompMsgObj instanceof PGPSignatureList) {
							 pDecryptInterface.encryptSignUsers(checkSignature((PGPSignatureList) tCompMsgObj));
						 } else {
							throw new PGPException("Unexpected Compressed PGP Object, type: " + tCompMsgObj.getClass().getName());
						}
						tCompMsgObj = tCompFact.nextObject();
					}
				 } else if (tPlainMsgObj instanceof PGPLiteralData) {
					 pDecryptInterface.decryptedMessage(readPGPLiteralData((PGPLiteralData) tPlainMsgObj ));
				} else if (tPlainMsgObj instanceof PGPOnePassSignatureList) {
					 pDecryptInterface.encryptSignOnePassUsers(checkSignatureList((PGPOnePassSignatureList) tPlainMsgObj));
				 } else if (tPlainMsgObj instanceof PGPSignatureList) {
					 pDecryptInterface.encryptSignUsers(checkSignature((PGPSignatureList) tPlainMsgObj));
				 } else {
					throw new PGPException("Unexpected PGP Object, type: " + tPlainMsgObj.getClass().getName());
				}
				tPlainMsgObj = tPlainFact.nextObject();
			}
		 }
	}
		 
	private String checkSignature(PGPSignatureList  pSignatureList ) throws Exception {
		List<KeyRingInterface> tPubKeyRings = null;
		tPubKeyRings = getPublicKeyRings(); 
		
		Iterator<PGPSignature> tSignItr = pSignatureList.iterator();
		boolean tFirst = true;
		StringBuilder sb = new StringBuilder();
		while (tSignItr.hasNext()) {
			 PGPSignature tSign = tSignItr.next();
			 for( KeyRingInterface kr : tPubKeyRings) {
				PGPPublicKey pk = ((PubKeyRing) kr).getPublicKeyRing().getPublicKey( tSign.getKeyID());
				if (pk != null) {
					@SuppressWarnings("unchecked")
					Iterator<String> tUserItr = pk.getUserIDs();
					while( tUserItr.hasNext()) {
						if (!tFirst) {
							sb.append(", ").append(tUserItr.next());
						} else {
							sb.append(tUserItr.next());
							tFirst = false;
						}
					}
					break;
				}
			 }
		 }
		 if (sb.toString().isEmpty()) {
			 return null;
		 }
		 return sb.toString();
	}
	private String  checkSignatureList( PGPOnePassSignatureList pSignatureList ) throws Exception{
		Iterator<PGPOnePassSignature> tSignItr = pSignatureList.iterator();
		List<KeyRingInterface> tPubKeyRings = null;
		tPubKeyRings = getPublicKeyRings(); 
		
		boolean tFirst = true;
		StringBuilder sb = new StringBuilder();
		while (tSignItr.hasNext()) {
			 PGPOnePassSignature tSign = tSignItr.next();
			 for( KeyRingInterface kr : tPubKeyRings) {
				PGPPublicKey pk = ((PubKeyRing) kr).getPublicKeyRing().getPublicKey( tSign.getKeyID());
				if (pk != null) {
					@SuppressWarnings("unchecked")
					Iterator<String> tUserItr = pk.getUserIDs();
					while( tUserItr.hasNext()) {
						if (!tFirst) {
							sb.append(", ").append(tUserItr.next());
						} else {
							sb.append(tUserItr.next());
							tFirst = false;
						}
					}
					break;
				}
			 }
		 }
		 if (sb.toString().isEmpty()) {
			 return null;
		 }
		 return sb.toString();
	}
	 
	public void decryptFile(File pInFile, File pOutFile, DecryptInterface pDecryptInterface) throws Exception {
		InputStream tInStream = null;
		OutputStream tOutStream = null;
		SecKeyRingInterface tSecKeyRing = null;
		PGPEncryptedDataList tEncDataList = null;
		PGPPrivateKey tPrivKey = null;
		int tBytesRead;

		try {
			try {
				tInStream = PGPUtil.getDecoderStream(Files.newInputStream(pInFile.toPath()));
			} catch (IOException e) {
				throw new IOException("failed to decode encrypted stream (" + e.getMessage() + ")");
			}

			PGPObjectFactory tPGPObjectFactory = new PGPObjectFactory(tInStream, new BcKeyFingerprintCalculator());
			Object tPGPObject = tPGPObjectFactory.nextObject();

			// the first object might be a PGP marker packet.
			tEncDataList = (tPGPObject instanceof PGPEncryptedDataList) ? (PGPEncryptedDataList) tPGPObject
					: (PGPEncryptedDataList) tPGPObjectFactory.nextObject();

			for (PGPEncryptedData tEncDataEl : tEncDataList) {
				PGPPublicKeyEncryptedData tEncData = (PGPPublicKeyEncryptedData)tEncDataEl;
				tSecKeyRing = GPGAdapter.getInstance().findSecretKeyRing(tEncData.getKeyID());

				if (tSecKeyRing == null) {
					throw new Exception(
							"failed to find decryption key (" + Long.toHexString(tEncData.getKeyID()) + ")");
				}

				char[] tPassword = Settings.getInstance().getPasswordForKeyId(tEncData.getKeyID());

				tPassword = Settings.getInstance().getPasswordForKeyId(tEncData.getKeyID());

				if (tPassword == null) {
					tPassword = pDecryptInterface.getPasswordForDecrypt(tEncData.getKeyID(),tSecKeyRing.getSecretKeyUserId());
				}

				tPrivKey = getPrivateKey(tSecKeyRing.getSecretKey(tEncData.getKeyID()), tPassword);
				Settings.getInstance().savePasswordInCache(tEncData.getKeyID(), tPassword);
//				System.out.println("Encrypt key id: " + Long.toHexString(tEncData.getKeyID()) + " Key Id Found: "
//						+ Long.toHexString(tPrivKey.getKeyID()));

				InputStream tClearInputStream = tEncData.getDataStream(new BcPublicKeyDataDecryptorFactory(tPrivKey));

				PGPObjectFactory tPlainFact = new PGPObjectFactory(tClearInputStream, new BcKeyFingerprintCalculator());
				Object tMessageObject = tPlainFact.nextObject();

				if (tMessageObject instanceof PGPCompressedData) {
					PGPCompressedData tCompressedData = (PGPCompressedData) tMessageObject;
					PGPObjectFactory pgpFact = new PGPObjectFactory(tCompressedData.getDataStream(),
							new BcKeyFingerprintCalculator());
					tMessageObject = pgpFact.nextObject();
				}
				if (tMessageObject instanceof PGPLiteralData) {
					PGPLiteralData tLitteralData = (PGPLiteralData) tMessageObject;

					//System.out.println("Orginal File: " + tLitteralData.getFileName() + " ModTime: "
					//		+ cSDF.format(tLitteralData.getModificationTime()));

					tOutStream = Files.newOutputStream(pOutFile.toPath());
					InputStream tInputStream = tLitteralData.getInputStream();
					byte[] tBuffer = new byte[1024];
					while ((tBytesRead = tInputStream.read(tBuffer, 0, tBuffer.length)) != -1) {
						tOutStream.write(tBuffer, 0, tBytesRead);
					}
					tOutStream.flush();
					tOutStream.close();
					tOutStream = null;
					
					tInStream.close();
					
				} else if (tMessageObject instanceof PGPOnePassSignatureList) {
					throw new PGPException("Encrypted message contains a signed message - not literal data.");
				} else {
					throw new PGPException("Message is not a simple encrypted file - type unknown.");
				}
			}
		} finally {
			if (tInStream != null) {
				tInStream.close();
			}
			if (tOutStream != null) {
				tOutStream.close();
			}
		}
	}
		 
}

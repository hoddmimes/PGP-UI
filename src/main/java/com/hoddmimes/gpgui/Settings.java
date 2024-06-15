package com.hoddmimes.gpgui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;



public class Settings 
{
	private static Settings cInstance = null;
	
	
	private boolean mIsWindows;
	private final String mWinTempDir = System.getenv("APPDATA") + "\\Local\\Temp\\";
	private String  mWinGPGPublicKeyRingFile = System.getenv("APPDATA") + "\\gnupg\\pubring.gpg";
	private  String mWinGPGSecretKeyRingFile = System.getenv("APPDATA") + "\\gnupg\\secring.gpg";
	private String  mWinGPGUIPublicKeyRingFile = System.getProperty("user.home") + "\\gpgui-pubring.kr";
	private  String mWinGPGUISecretKeyRingFile = System.getProperty("user.home") + "\\gpgui-secring.kr";
	
	private final String mLinuxTempDir = "/tmp/";
	private String  mLinuxGPGPublicKeyRingFile = System.getProperty("user.home") + "/.gnupg/pubring.gpg";
	private  String mLinuxGPGSecretKeyRingFile = System.getProperty("user.home") + "/.gnupg/secring.gpg";
	private String  mLinuxGPGUIPublicKeyRingFile = System.getProperty("user.home") + "/.gpgui-pubring.kr";
	private  String mLinuxGPGUISecretKeyRingFile = System.getProperty("user.home") + "/.gpgui-secring.kr";
	
	private long mPasswordCacheLiveTime = Long.MAX_VALUE;
	private String mCurrentDir = System.getProperty("user.home");
	private Map<Long,CachePasswordEntry> mPasswordCache;
	
	private int mPasswordTTL = 5; // 5 MIN password live time

	Settings() {
		String OS = System.getProperty("os.name").toLowerCase();
        mIsWindows = OS.contains("win");
		mPasswordCache = new HashMap<Long,CachePasswordEntry>(27);
		this.restore();
	}
	
	
	
	
	public boolean isWindows() {
		return mIsWindows;
	}
	
	public static Settings getInstance() {
		if (cInstance == null) {
			cInstance = new Settings();
		}
		return cInstance;
	}
	
	
	public void setPasswordTTL( int pMinute ) {
		mPasswordTTL = pMinute;
	}
	
	
	public int getPasswordTTL() {
		return mPasswordTTL;
	}
	
	void setCurrentDir( String pDir ) {
		mCurrentDir = pDir;
	}
	
	String getCurrentDir() {
		return mCurrentDir;
	}
	
	void savePasswordInCache( long pKeyId, char[] pPassword ) {
		if (!this.mPasswordCache.containsKey(pKeyId)) {
			mPasswordCache.put(pKeyId, new CachePasswordEntry(pPassword, pKeyId));
		}
	}
	char[] getPasswordForKeyId( long pKeyId ) {
		CachePasswordEntry tEntry = mPasswordCache.get(pKeyId);
		if (tEntry == null) {
			return null;
		}
		if ((tEntry.getCreateTime() + mPasswordCacheLiveTime) < System.currentTimeMillis()) {
			mPasswordCache.remove(pKeyId);
			return null;
		}
		return tEntry.getPassword();
	}
	
	
	String getTempDir()
	{
		if (mIsWindows) {
			return mWinTempDir;
		} else {
			return mLinuxTempDir;
		}
	}
	
	void setPGPPublicKeyRingFilename( String pFilename ) {
	   if (mIsWindows) {
		   mWinGPGPublicKeyRingFile = pFilename;
	   } else {
		   mLinuxGPGPublicKeyRingFile = pFilename;
	   }
	}
	
	String getPGPPublicKeyRingFilename()
	{
		if (mIsWindows) {
			return mWinGPGPublicKeyRingFile;
		} else {
			return mLinuxGPGPublicKeyRingFile;
		}
	}
	
	void setPGPSecretKeyRingFilename( String pFilename ) {
		   if (mIsWindows) {
			   mWinGPGSecretKeyRingFile = pFilename;
		   } else {
			   mLinuxGPGSecretKeyRingFile = pFilename;
		   }
		}
	
	String getPGPSecretKeyRingFilename()
	{
		if (mIsWindows) {
			return mWinGPGSecretKeyRingFile;
		} else {
			return mLinuxGPGSecretKeyRingFile;
		}
	}
	
	void setPGPUIPublicKeyRingFilename( String pFilename ) {
		   if (mIsWindows) {
			   mWinGPGUIPublicKeyRingFile = pFilename;
		   } else {
			   mLinuxGPGUIPublicKeyRingFile = pFilename;
		   }
		}

	
	
	
	String getPGPUIPublicKeyRingFilename()
	{
		if (mIsWindows) {
			return mWinGPGUIPublicKeyRingFile;
		} else {
			return mLinuxGPGUIPublicKeyRingFile;
		}
	}
	
	void setPGPUISecretKeyRingFilename( String pFilename ) {
		   if (mIsWindows) {
			   mWinGPGUISecretKeyRingFile = pFilename;
		   } else {
			   mLinuxGPGUISecretKeyRingFile = pFilename;
		   }
		}

	
	String getPGPUISecretKeyRingFilename()
	{
		if (mIsWindows) {
			return mWinGPGUISecretKeyRingFile;
		} else {
			return mLinuxGPGUISecretKeyRingFile;
		}
	}
	
	
	void restore() {
		FileInputStream tIn;
		try {
			tIn = new FileInputStream("pgpui.properties");
			Properties tProp = new Properties();
			tProp.load(tIn);
			mWinGPGPublicKeyRingFile =  tProp.getProperty("pgp.win.public.filename");
			mWinGPGSecretKeyRingFile =  tProp.getProperty("pgp.win.secret.filename");
			mWinGPGUIPublicKeyRingFile =  tProp.getProperty("pgpui.win.public.filename");
			mWinGPGUISecretKeyRingFile =  tProp.getProperty("pgpui.win.secret.filename");
			
			
			mLinuxGPGPublicKeyRingFile =  tProp.getProperty("pgp.linux.public.filename");
			mLinuxGPGSecretKeyRingFile =  tProp.getProperty("pgp.linux.secret.filename");
			mLinuxGPGUIPublicKeyRingFile =  tProp.getProperty("pgpui.linux.public.filename");
			mLinuxGPGUISecretKeyRingFile =  tProp.getProperty("pgpui.linux.secret.filename");
			
			mPasswordTTL = Integer.parseInt( tProp.getProperty("passwordttl"));
			mCurrentDir = tProp.getProperty("currdir");
		}
		catch( IOException e ) {
			
		}
	}
	void save() {
		FileOutputStream tOut;
		
		try {
			tOut = new FileOutputStream("pgpui.properties");
			 Properties tProp = new Properties();
			 tProp.setProperty("pgp.win.public.filename", mWinGPGPublicKeyRingFile);
			 tProp.setProperty("pgp.win.secret.filename", mWinGPGSecretKeyRingFile);
			 tProp.setProperty("pgp.Linux.public.filename", mLinuxGPGPublicKeyRingFile);
			 tProp.setProperty("pgp.Linux.secret.filename", mLinuxGPGSecretKeyRingFile);
			 
			 tProp.setProperty("pgpui.win.public.filename", mWinGPGUIPublicKeyRingFile);
			 tProp.setProperty("pgpui.win.secret.filename", mWinGPGUISecretKeyRingFile);
			 tProp.setProperty("pgpui.Linux.public.filename", mLinuxGPGUIPublicKeyRingFile);
			 tProp.setProperty("pgpui.Linux.secret.filename", mLinuxGPGUISecretKeyRingFile);
			 
			 tProp.setProperty("passwordttl", String.valueOf(mPasswordTTL));
			 tProp.setProperty("currdir", mCurrentDir);
			 tProp.store(tOut, null);
			 tOut.flush();
			 tOut.close();
		}
		catch( IOException e) {
			e.printStackTrace();
		}
	}
	
	
	static class CachePasswordEntry {
		private char[]  mPassword;
		private long 	mKeyId;
		private long	mCreateTime;
		
		CachePasswordEntry( char[] pPassword, long pKeyId  ) {
			mPassword = pPassword;
			mKeyId = pKeyId;
			mCreateTime = System.currentTimeMillis();
		}
		
		long getCreateTime() {
			return mCreateTime;
		}
		
		char[] getPassword() {
			return mPassword;
		}
	}
}

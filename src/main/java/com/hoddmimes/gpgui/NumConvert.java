package com.hoddmimes.gpgui;


public class NumConvert 
{


    
    public static byte[] intToBytes( int pValue )
    {
    	byte[] tBuffer = new byte[4];
        tBuffer[0] = (byte) (pValue >>> 24);
        tBuffer[1] = (byte) (pValue >>> 16);
        tBuffer[2] = (byte) (pValue >>> 8);
        tBuffer[3] = (byte) (pValue >>> 0);
        return tBuffer;
    }
    
    public static void intToBytes( int pValue, byte[] pBuffer, int pOffset )
    {
        pBuffer[ pOffset + 0] = (byte) (pValue >>> 24);
        pBuffer[ pOffset + 1] = (byte) (pValue >>> 16);
        pBuffer[ pOffset + 2] = (byte) (pValue >>> 8);
        pBuffer[ pOffset + 3] = (byte) (pValue >>> 0);
    }
    
    public static int bytesToInt(byte[] pBuffer, int pOffset )
    {
        int tValue = 0;
        tValue += ((pBuffer[ pOffset + 0] & 0xff) << 24);
        tValue += ((pBuffer[ pOffset + 1] & 0xff) << 16);
        tValue += ((pBuffer[ pOffset + 2] & 0xff) << 8);
        tValue += ((pBuffer[ pOffset + 3] & 0xff) << 0);
        return tValue;
    }
    
    public static byte[] longToBytes( long pValue )
    {
    	byte[] tBuffer = new byte[8];
        tBuffer[0] = (byte) (pValue >>> 56);
        tBuffer[1] = (byte) (pValue >>> 48);
        tBuffer[2] = (byte) (pValue >>> 40);
        tBuffer[3] = (byte) (pValue >>> 32);
        tBuffer[4] = (byte) (pValue >>> 24);
        tBuffer[5] = (byte) (pValue >>> 16);
        tBuffer[6] = (byte) (pValue >>> 8);
        tBuffer[7] = (byte) (pValue >>> 0);
        return tBuffer;
    }
    
    public static void longToBytes( long pValue, byte[] pBuffer, int pOffset )
    {
        pBuffer[ pOffset + 0] = (byte) (pValue >>> 56);
        pBuffer[ pOffset + 1] = (byte) (pValue >>> 48);
        pBuffer[ pOffset + 2] = (byte) (pValue >>> 40);
        pBuffer[ pOffset + 3] = (byte) (pValue >>> 32);
        pBuffer[ pOffset + 4] = (byte) (pValue >>> 24);
        pBuffer[ pOffset + 5] = (byte) (pValue >>> 16);
        pBuffer[ pOffset + 6] = (byte) (pValue >>> 8);
        pBuffer[ pOffset + 7] = (byte) (pValue >>> 0);
    }
    
    public static long bytesToLong(byte[] pBuffer, int pOffset )
    {
        long tValue = 0;
        tValue += (long)(pBuffer[ pOffset + 0] & 0xff) << 56;
        tValue += (long)(pBuffer[ pOffset + 1] & 0xff) << 48;
        tValue += (long)(pBuffer[ pOffset + 2] & 0xff) << 40;
        tValue += (long)(pBuffer[ pOffset + 3] & 0xff) << 32;
        tValue += (long)(pBuffer[ pOffset + 4] & 0xff) << 24;
        tValue += (long)(pBuffer[ pOffset + 5] & 0xff) << 16;
        tValue += (long)(pBuffer[ pOffset + 6] & 0xff) << 8;
        tValue += (long)(pBuffer[ pOffset + 7] & 0xff) << 0;
        return tValue;
    }
    
}

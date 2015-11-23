// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: IO.java,v 1.1 2006/01/21 17:28:01 gregw Exp $
// ---------------------------------------------------------------------------

package com.mortbay.iwiki;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;


/* ======================================================================== */
/** IO Utilities.
 * Provides stream handling utilities in
 * singleton Threadpool implementation accessed by static members.
 */
public class IO 
{
    /* ------------------------------------------------------------------- */
    public final static String
        CRLF      = "\015\012";

    /* ------------------------------------------------------------------- */
    public final static byte[]
        CRLF_BYTES    = {(byte)'\015',(byte)'\012'};

    /* ------------------------------------------------------------------- */
    public static int bufferSize = 8192;
    
    
    /* ------------------------------------------------------------------- */
    /** Copy Stream in to Stream out until EOF or exception.
     */
    public static void copy(InputStream in, OutputStream out)
         throws IOException
    {
        copy(in,out,-1);
    }
    
    
    /* ------------------------------------------------------------------- */
    /** Copy Reader to Writer out until EOF or exception.
     */
    public static void copy(Reader in, Writer out)
         throws IOException
    {
        copy(in,out,-1);
    }
    
    /* ------------------------------------------------------------------- */
    /** Copy Reader to Writer out until EOF or exception.
     */
    public static void copy(File in, File out)
         throws IOException
    {
    	FileInputStream fis = new FileInputStream(in);
    	FileOutputStream fos = new FileOutputStream(out,false);   	
        copy(fis,fos,-1);
    }
    
    /* ------------------------------------------------------------------- */
    /** Copy Stream in to Stream for byteCount bytes or until EOF or exception.
     */
    public static void copy(InputStream in,
                            OutputStream out,
                            long byteCount)
         throws IOException
    {     
        byte buffer[] = new byte[bufferSize];
        int len=bufferSize;
        
        if (byteCount>=0)
        {
            while (byteCount>0)
            {
                if (byteCount<bufferSize)
                    len=in.read(buffer,0,(int)byteCount);
                else
                    len=in.read(buffer,0,bufferSize);                   
                
                if (len==-1)
                    break;
                
                byteCount -= len;
                out.write(buffer,0,len);
            }
        }
        else
        {
            while (true)
            {
                len=in.read(buffer,0,bufferSize);
                if (len<0 )
                    break;
                out.write(buffer,0,len);
            }
        }
    }  
    
    /* ------------------------------------------------------------------- */
    /** Copy Reader to Writer for byteCount bytes or until EOF or exception.
     */
    public static void copy(Reader in,
                            Writer out,
                            long byteCount)
         throws IOException
    {  
        char buffer[] = new char[bufferSize];
        int len=bufferSize;
        
        if (byteCount>=0)
        {
            while (byteCount>0)
            {
                if (byteCount<bufferSize)
                    len=in.read(buffer,0,(int)byteCount);
                else
                    len=in.read(buffer,0,bufferSize);                   
                
                if (len==-1)
                    break;
                
                byteCount -= len;
                out.write(buffer,0,len);
            }
        }
        else
        {
            while (true)
            {
                len=in.read(buffer,0,bufferSize);
                if (len==-1)
                    break;
                out.write(buffer,0,len);
            }
        }
    }

    /* ------------------------------------------------------------ */
    /** Read input stream to string.
     */
    public static String toString(InputStream in)
        throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in,out);
        return new String(out.toByteArray());
    }


    /* ------------------------------------------------------------ */
    /** Delete File.
     * This delete will recursively delete directories - BE CAREFULL
     * @param file The file to be deleted.
     */
    public static boolean delete(File file)
    {
        if (!file.exists())
            return false;
        if (file.isDirectory())
        {
            File[] files = file.listFiles();
            for (int i=0;files!=null && i<files.length;i++)
                delete(files[i]);
        }
        return file.delete();
    }

    /* ------------------------------------------------------------ */
    /** 
     * @return An outputstream to nowhere
     */
    public static OutputStream getNullStream()
    {
        return __nullStream;
    }
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private static class NullOS extends OutputStream                                    
    {
        public void close(){}
        public void flush(){}
        public void write(byte[]b){}
        public void write(byte[]b,int i,int l){}
        public void write(int b){}
    }
    private static NullOS __nullStream = new NullOS();
    
    /* ------------------------------------------------------------ */
    /** 
     * @return An writer to nowhere
     */
    public static Writer getNullWriter()
    {
        return __nullWriter;
    }
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private static class NullWrite extends Writer                                    
    {
        public void close(){}
        public void flush(){}
        public void write(char[]b){}
        public void write(char[]b,int o,int l){}
        public void write(int b){}
        public void write(String s){}
        public void write(String s,int o,int l){}
    }
    private static NullWrite __nullWriter = new NullWrite();
}










/*
 * Created on Apr 28, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package it.colletta;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * @author janb
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Configuration 
{
    public static final String PROPS_FILENAME = "config.properties";
    private static Object lock = new Object();
    private static Configuration myInstance = null;
    private Properties props = new Properties();
    private boolean isLoaded = false;
    
    public static Configuration getInstance ()
    {
        synchronized (lock)
        {
            if (myInstance == null)
                myInstance = new Configuration();
            
        }
        return myInstance;
    }
    
    
    public void load (Properties properties)
    {
        props.putAll(properties);
        isLoaded = true;
    }
    
    
    public void load (URL propsURL)
    throws Exception
    {
        InputStream is = propsURL.openStream();
        props.load (is);       
        isLoaded = true;
    }
    
    public String getProperty (String name)
    {
        if (!isLoaded)
            throw new IllegalStateException ("config.properties not loaded");
        
        return props.getProperty(name);
    }

    
    public String getProperty (String name, String defaultValue)
    {
        if (!isLoaded)
            throw new IllegalStateException ("config.properties not loaded");
        
        return props.getProperty(name, defaultValue);
    }
    
    public int getIntProperty (String name, int defaultValue)
    {
        if (!isLoaded)
            throw new IllegalStateException ("config.properties not loaded");
        
        String val = props.getProperty(name);
        if (val==null)
            return defaultValue;
        return Integer.parseInt(val);
    }
    
    public long getLongProperty (String name, long defaultValue)
    {
    	 if (!isLoaded)
             throw new IllegalStateException ("config.properties not loaded");
         
         String val = props.getProperty(name);
         if (val==null)
             return defaultValue;
         return Long.parseLong(val);
    }
    
    public Properties getProperties ()
    {
        if (!isLoaded)
            throw new IllegalStateException ("config.properties not loaded");
        
        Properties copy = new Properties();
        copy.putAll(props);
        return copy;
    }
    
    
    /**
     * Not an alternative to loading the the config.properties file.
     * This method just allows the odd individual property to be set
     * once the initial load has been done.
     * @param name
     * @param value
     */
    public void setProperty (String name, String value)
    {
        props.setProperty (name, value);
    }
    
    private Configuration ()
    {
        //TODO doesn't support UTF-8 encoded stuff
        /* rely on front end to set us up with the url of where to look
        try
        {
            InputStream is = getClass().getResourceAsStream("/"+PROPS_FILENAME);
            if (is == null)
                System.err.println ("No config.properties file found on classpath");
            else      
                props.load(is);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        */
    }
 
}

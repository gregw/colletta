/*
 * Created on May 4, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package it.colletta.reservation;

import it.colletta.Configuration;

import java.util.Properties;

import junit.framework.TestCase;

/**
 * @author janb
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class EmailServerTest extends TestCase
{
    public EmailServerTest ()
    {
        
    }
    
    public void setUp ()
    throws Exception
    {        
        Properties props = new Properties();
        props.put ("admin.email", "janb@mortbay.com");
        props.put ("system.email", "janb@mortbay.com");
        props.put("mail.smtp.host", "localhost");
        props.put("mail.debug", "true");
        props.put ("deposit.hrs", "56");
        props.put ("total.days", "7");
        props.put ("colletta.website", "http://www.blah.blah.it");
        props.put ("colletta.phone", "999912345");
        Configuration config = Configuration.getInstance();
        config.load(props);
    }
    
    public void testSend1 ()
    throws Exception
    {
        EmailServer eserver = new EmailServer();
        eserver.send("janb@mortbay.com", "froodA@mortbay.com", "HelloA", "This is A test message.");
        eserver.send("janb@mortbay.com", "froodB@mortbay.com", "HelloB", "This is B test message.");
        Thread.sleep(2000L);
    }
}

/*
 * Created on Apr 22, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package it.colletta.reservation;

import it.colletta.Configuration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


/**
 * EmailServer
 * 
 * Generates email messages to recipients only. Does not receive email.
 * @author janb
 *
 * 
 */
public class EmailServer
{
    private static final Logger log = Logger.getLogger(EmailServer.class.getName());
    
    public static final String DATE_FORMAT = "EEE, d MMM yy HH:mm:ss Z";
    public static final String FROM = "from";
    public static final String TO = "to";
    public static final String SUBJECT = "subject";
    public static final String TEXT = "text";
    private Session session = null;
    private SimpleDateFormat dateFormat = null;
    private List workQueue = null;
    private boolean initDone = false;
    
   

    
    private class MailAuthenticator extends Authenticator
    {
        private PasswordAuthentication pwdAuth = null;
        
        public MailAuthenticator (String user, String pwd)
        {
            pwdAuth = new PasswordAuthentication (user, pwd);
        }
        
        public PasswordAuthentication getPasswordAuthentication()
        {
            return pwdAuth;
        }
    }
    
    
    private class MailThread extends Thread
    {
        public void run ()
        {
            boolean goodToGo = true;
            while (goodToGo)
            {
                try
                {
                    log.log(Level.FINE,"Waiting for some work");
                    Map msg = EmailServer.this.getWork();
                    log.log(Level.FINE,"Got some work");
                    if (msg != null)
                        EmailServer.this.send(msg);
                }
                catch (InterruptedException e)
                {
                    goodToGo = false;
                }
            }
        }
    }
    
    
    public EmailServer()
    {
        
    }
 
   

    
    public void send (String recipient, String sender, String subject, String message)
    throws Exception
    {
        init();
        HashMap msg = new HashMap();
        msg.put (FROM, sender);
        msg.put (TO, recipient);
        msg.put (SUBJECT, subject);
        msg.put (TEXT, message);
        addWork (msg);
    }
    
    
    
    private synchronized void init()
    throws Exception
    {
        if (!initDone)
        {
            dateFormat = new SimpleDateFormat(DATE_FORMAT);
            
            String user =  Configuration.getInstance().getProperty("User");
            String pwd =  Configuration.getInstance().getProperty("Password");
                        
            session = Session.getInstance (Configuration.getInstance().getProperties(), new MailAuthenticator (user, pwd));
            workQueue = new ArrayList();
            initDone = true;
            MailThread thread = new MailThread();
            thread.start();
            
            log.log(Level.FINE,"Email async thread started");
        }
    }
                                                                                 
    
    private void addWork (Map message)
    {
        synchronized (workQueue)
        {
            workQueue.add(message);
            //signal waiting thread
            workQueue.notifyAll();
        }
    }
    
    
    private Map getWork ()
    throws InterruptedException
    {
        Map m = null;
        
        synchronized (workQueue)
        {
            if (workQueue.size() == 0)
            {
                try
                {
                    //wait for some work to appear   
                    workQueue.wait();
                }
                catch (InterruptedException e)
                {
                    log.warning("Interrupted when waiting on work queue");
                    throw e;
                }
            }
            
            // if the queue now has something in it, take the first element off the queue
            if (workQueue.size() > 0)
                m = (Map)workQueue.remove(0);
            
        }
        return m;
    }
    
    private void send (Map m)
    {        
        String recipient="unknown";
        try
        {
            String sender = (String)m.get(FROM);
            if ((sender == null) || (sender.trim().equals("")))
            {
                log.warning("No from: address in message "+ m);
                return;
            }
            
            recipient = (String)m.get(TO);
            if ((recipient == null) || (recipient.trim().equals("")))
            {
                log.warning("No to: address in message "+ m);
                return;
            }
            
           
            Message msg = new MimeMessage(session);        
            //      set the from and to address
            msg.setFrom(new InternetAddress(sender));     
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            msg.setSubject((String)m.get(SUBJECT));
            msg.setContent((String)m.get(TEXT), "text/plain");
            msg.addHeader ("Date", dateFormat.format(new Date()));
            log.info("Sending a message from: "+sender+" to: "+recipient+" ...");
            Transport.send(msg);
            log.info("Sent: "+m);
        }
        catch (Exception e)
        {
            log.warning("Error sending message to "+recipient);
            log.log(Level.FINE,"Error sending message ", e);
        }
    }

}

/*
 * Created on Apr 23, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package it.colletta.reservation;

import it.colletta.Apartment;
import it.colletta.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mortbay.iwiki.User;
import com.mortbay.iwiki.YyyyMmDd;

/**
 * Sends notifications to appropriate people on changes to Reservations.
 * @author janb
 *
 */
public class EmailNotificationManager implements NotificationManager
{
    private static final Logger log = Logger.getLogger(EmailNotificationManager.class.getName());
    
    public static final String DEFAULT_SYSTEM_EMAIL = "affitti@colletta-it.com";
    public static final String DEFAULT_SITE_ADMIN = "mortbay@mortbay.com";
    public static final String DEFAULT_LANG = "EN";
    public static final String DEFAULT_ROOT = ".";
    protected  EmailServer emailServer = null;
    private  String rootDir = DEFAULT_ROOT;
    private  String adminEmail = DEFAULT_SITE_ADMIN;
    private  String systemEmail = DEFAULT_SYSTEM_EMAIL;
    private  HashMap notificationMap = null;






    protected EmailNotificationManager()
    {}


    
    /**
     * Send notification of a change in a reservation
     * @param nt type of notification
     * @param rd current reservation state
     * @throws Exception
     */
    public void notify (String nt, ReservationData rd)
    {
        String recipient = null;


        try
        {
            init ();
            User manager = User.getUser(rd.getManager());
            Properties props = ReservationPropertyConverter.toProperties(rd);
            Properties mgrProps = convertManager (manager);
            props.putAll(mgrProps);
            //add in synthetic properties which are to do with amount calculations
            props.put("res.total", rd.getTotal().toString());
            props.put("res.owing", rd.getOwing().toString());
            props.put("res.deposit", rd.getDeposit().toString());
            int totalDueDays = (Long.getLong("totaldue.days", 56).intValue() * -1);
            YyyyMmDd totalDueDate = new YyyyMmDd(rd.getStartDate());
            totalDueDate.addDays(totalDueDays);
            props.put("totaldue.date", totalDueDate.toString());
            
            sendClient(nt, props, rd);
            sendManager(nt, props, rd);
            sendColletta (nt,props, rd);
            sendOwners (nt, props, rd);
            sendSystem (nt, props, rd);
        }
        catch (Exception e)
        {
            log.log(Level.WARNING,"sending notification: "+nt+" to: "+recipient,e);
        }
    }


    private void sendClient (String nt, Properties props, ReservationData rd)
    {
        //check is there a message to be sent to the client
        String clientTemplate = lookupNotification (nt, Recipient.CLIENT, rd.getLanguage());
        //nothing to do for client 
        if (clientTemplate == null)
        {
            log.log (Level.FINE,"No templates for "+nt+" in "+rd.getLanguage()+"(client) or EN");
            return;
        }
        
        String recipient = rd.getEmail();
        try
        {
            if (clientTemplate != null)
            {
              
                if (recipient != null)
                {
                    log.info("Sending "+nt+" to CLIENT "+recipient+" in "+rd.getLanguage());
                    sendNotification (substitute (clientTemplate, props), systemEmail, recipient);
                }
                else
                    log.info ("No email address for res="+rd.getId()+" for notification="+nt);
            }
            else
                log.info ("No email template for CLIENT for "+nt);
        }
        catch (Exception e)
        {
            log.log(Level.WARNING,"sending notification: "+nt+" to: "+recipient,e);
        }
    }

    
    
    
    private void sendManager (String nt, Properties props, ReservationData rd)
    {
        User manager = User.getUser(rd.getManager());
        String mgrLanguage = (manager==null?DEFAULT_LANG:(manager.getLang()==null?DEFAULT_LANG:manager.getLang()));
        String mgrTemplate = lookupNotification (nt, Recipient.MANAGER, mgrLanguage);
        String recipient=null;
        try
        {
            if (mgrTemplate != null)
            {
                //send email to the manager for the apartment, or the default if no manger set or no email address
                recipient = (manager.getEmail()==null?User.getDefaultManager().getEmail():manager.getEmail());
                log.info("Sending "+nt+" to MANAGER "+recipient);
                sendNotification (substitute (mgrTemplate, props), systemEmail, recipient);
            }
            else
                log.info("No email template for MANAGER for "+nt);
        }
        catch (Exception e)
        {
            log.log(Level.WARNING,"sending notification: "+nt+" to: "+recipient,e);
        }
    }
    
    
    private void sendColletta (String nt, Properties props, ReservationData rd)
    {
        String collettaTemplate = lookupNotification(nt, Recipient.COLLETTA, "IT");
        String recipient = null;
        try
        {
            if (collettaTemplate != null)
            {
                //send email to the village
                recipient = Configuration.getInstance().getProperty("colletta.email");
                log.info("Sending "+nt+" to COLLETTA "+recipient);
                sendNotification (substitute (collettaTemplate, props), systemEmail, recipient);
            }
            else
                log.info ("No template for COLLETTA for notification "+nt);
        }
        catch (Exception e)
        {
            log.log(Level.WARNING,"sending notification: "+nt+" to: "+recipient,e);
        }
    }

    
    
    private void sendOwners (String nt, Properties props, ReservationData rd)
    {
        Apartment apt = Apartment.getApartment(rd.getAptId());
        if (apt==null)
        {
            log.log(Level.WARNING,"No apt in sendOwners for "+rd.getId());
            return;
        }
        
        User[] owners = apt.getOwners();
        if (owners == null)
        {
            log.log(Level.WARNING,"Apartment "+rd.getAptId()+" has no owners!");
        }

        for (int i=0; i<owners.length; i++)
        {
            String ownerLanguage = (owners[i]==null?DEFAULT_LANG:(owners[i].getLang()==null?DEFAULT_LANG:owners[i].getLang()));
            String ownerTemplate = lookupNotification (nt, Recipient.OWNER, ownerLanguage);
            
            try
            {
                if (ownerTemplate != null)
                {
                    if (owners[i].getEmail() == null)
                        log.log(Level.WARNING,"No email address for owner "+owners[i].getFullName());
                    else
                    {
                    	String[] recipients = owners[i].getEmail().split(",");
                    	for (int j=0;j<recipients.length;j++) 
                    	{
                    		log.info("Sending "+nt+" to OWNER "+recipients[j]);
                    		sendNotification (substitute (ownerTemplate, props), systemEmail, recipients[j]);
                    	}
                    }
                }
                else
                    log.info ("No template for OWNER in "+ownerLanguage+" for notification "+nt);
            }
            catch (Exception e)
            {
                log.log(Level.WARNING,"sending notification: "+nt,e);
            }
        }
    }
    
    
    
    private void sendSystem (String nt, Properties props, ReservationData rd)
    {
        String recipient = systemEmail;
        try
        {
            StringBuffer buf=new StringBuffer(1024);
            
            String link = Configuration.getInstance().getProperty("colletta.website")+"/renting/book/?ref="+props.getProperty("res.id");
            String subject="Notification: "+nt+" for ref="+rd.getId();
            buf.append(subject);
            buf.append('\n');
            buf.append('\n');
            buf.append(link);
            buf.append('\n');
            buf.append('\n');
            
            buf.append("# ");
            buf.append(subject);
            buf.append("\n# ");
            buf.append(new Date());
            buf.append('\n');
            
            ArrayList keys = new ArrayList(props.keySet());
            Collections.sort(keys);
            Iterator i=keys.iterator();
            while(i.hasNext())
            {
                String p=(String)i.next();
                String v=URLEncoder.encode((String)props.get(p),"UTF-8");
                buf.append(p);
                buf.append(": ");
                buf.append(v);
                buf.append('\n');
            }
            String systemMsg =buf.toString();
            
            sendNotification (systemMsg, systemEmail, adminEmail);
            rd.notified (nt);
        }
        catch (Exception e)
        {
            log.log(Level.WARNING,"sending notification: "+nt+" to: "+recipient,e);
        }
    }
    
    
    
    /**
     * Read all templates into memory
     * @throws Exception
     */
    protected synchronized void init()
    throws Exception
    {
        //load all of the notification files if not already
        if (notificationMap == null)
        {
           loadProperties();

           notificationMap = new HashMap();
           
           System.err.println("ROOTDIR = "+this.rootDir);
           File root = new File (this.rootDir);

            File templateDir = new File (root, "templates");
            if (!templateDir.exists() || !templateDir.isDirectory())
                throw new IllegalStateException("No notification templates in "+ templateDir.getCanonicalFile());

            //iterate over all templates and load into hashmap
            File[] files = templateDir.listFiles();
            for (int i=0; (files != null) && (i < files.length); i++)
            {
                if (files[i].isFile())
                {
                    String name = files[i].getName();
                    //name format is Type_Recipient_lang.properties/txt/whatever
                    int i1 = name.indexOf(".");
                    if (i1 > 0)
                    {
                        name = name.substring (0, i1).toUpperCase();

                        //open file and suck contents out
                        InputStreamReader isr = new InputStreamReader(new FileInputStream(files[i]), "UTF-8");
                        char[] buf = new char[256];
                        int len=0;
                        StringBuffer out = new StringBuffer();
                        while ((len=isr.read(buf))>0)
                            out.append(buf, 0, len);

                        //put into hashmap
                        notificationMap.put(name, out.toString());
                    }
                }
            }

            //hacky, for testing purposes
            if (emailServer == null)
                emailServer = new EmailServer();
        }
    }


    /**
     * Look up a suitable message template based on:
     * <ol> <li> the type of notification</li><li>the type of recipient</li><li>language</li></ol>
     * @param nt type of notification
     * @param nr the type of the recipient
     * @param nl the language
     * @return
     */
    private String lookupNotification (String nt, String nr, String nl)
    {
        if (nt == null)
            return null;
        if (nr == null)
            return null;
        if (nl == null)
            return null;

        String value = null;
        String key = nt+"_"+nr+"_"+nl.trim().toUpperCase();
        value = (String)notificationMap.get(key);

        if (value == null)
        {
            //maybe notification doesn't exist in the particular language,
            //so attempt a fallback to ENGLISH
            log.info("Notification for "+nt+"_"+nr+" doesn't exist in "+nl+", defaulting to "+nt+"_"+nr+"_EN");
            value = (String)notificationMap.get(nt+"_"+nr+"_"+"EN");
        }

        return value;
    }




    /**
     * Send the notification. The Subject line is extracted from the first line of the message.
     * @param msg
     * @param from
     * @param to
     * @throws Exception
     */
    private void sendNotification (String msg, String from, String to)
    throws Exception
    {
        //subject is first line of message
        String subject;
        String body;
        if (msg != null)
        {
            int lf = msg.indexOf('\n');
            int cr = msg.indexOf('\r');

            int idx = -1;
            if ((cr >= 0) && (cr < lf))
                idx = cr;
            else if ((lf >= 0) && (lf > cr))
                idx = lf;

            subject = msg.substring (0, idx);
            //if line break is /r/n skip over the /n
            if ((cr+1 == lf))
                body = msg.substring (idx+2);
            else
                body = msg.substring (idx+1);

            log.log(Level.FINE,"SENDING TO: "+to+" FROM: "+from+" SUBJECT: "+subject+ " \nBODY="+ body);
            emailServer.send (to, from, subject, body);
        }
    }




    /**
     * Substitute values from the reservation into the message template.
     * Variables to be substituted are surrounded by '${}' characters and
     * must match the name of a reservation property (see ReservationPropertyConverter)
     * or one of the properties set in config.properties.
     * @param template
     * @param props
     * @return
     */
    private String substitute (String template, Properties props)
    {
        Properties combinedProps = Configuration.getInstance().getProperties();
        combinedProps.putAll(props);



        StringBuffer msgBuff = new StringBuffer();
        StringBuffer varBuff = new StringBuffer();

        char[] templateChars = template.toCharArray();


        final int NORMAL = 0;
        final int VAR = 1;
        int state = NORMAL;

        for (int i= 0; i < templateChars.length; i++)
        {
            switch (state)
            {
                case NORMAL:
                {
                    //start getting var name
                    if ((templateChars[i] == '$') && (((i+1) < templateChars.length) && (templateChars[i+1] == '{')))
                    {
                        state = VAR;
                        varBuff.setLength(0);
                    }
                    else
                        msgBuff.append (templateChars[i]);

                    break;
                }
                case VAR:
                {
                    if (templateChars[i] == '{')
                        break; //skip ${

                    //finished getting var name
                    if (templateChars[i] == '}')
                    {
                        //handle case differences?
                        String val = (String)combinedProps.get(varBuff.toString());
                        if (val != null)
                            msgBuff.append (val);

                        state = NORMAL;
                    }
                    else
                        varBuff.append(templateChars[i]);

                    break;
                }
            }

        }

        return msgBuff.toString();
    }

    /**
     * @throws Exception
     */
    private void loadProperties ()
    throws Exception
    {
        rootDir = Configuration.getInstance().getProperty ("root.dir", DEFAULT_ROOT);
        adminEmail = Configuration.getInstance().getProperty ("admin.email", DEFAULT_SITE_ADMIN);
        systemEmail = Configuration.getInstance().getProperty ("system.email", DEFAULT_SYSTEM_EMAIL);
     }


    /** Convert some of the manager details to properties to insert into email
     * @param mgr
     * @return
     */
    private Properties convertManager (User mgr)
    {
        Properties props = new Properties();
        if (mgr == null)
            return props;
        props.put("mgr.email", mgr.getEmail());
        props.put("mgr.phone", mgr.getContact());
        props.put("mgr.name", mgr.getFullName());

        return props;
    }
}

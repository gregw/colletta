 /*
 * Created on Apr 24, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package it.colletta.reservation;

import it.colletta.Configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.mortbay.iwiki.User;
import com.mortbay.iwiki.YyyyMmDd;

import junit.framework.TestCase;

/**
 * @author janb
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class NotificationManagerTest extends TestCase
{
    ReservationData rd = new ReservationData();
    
    public class MockEmailServer extends EmailServer
    {
        public List recipientList = new ArrayList();
        public List senderList = new ArrayList();
        public List msgList = new ArrayList();
        public List subjectList = new ArrayList();
        
        public void send(String recipient, String sender, String subject,
                String message) throws Exception
        {
            assertEquals (recipient, recipientList.remove(0));
            assertEquals (sender, senderList.remove(0));
            msgList.add(message);
            subjectList.add(subject);
        }
        
        public void expectSend (String recipient, String sender)
        {
            recipientList.add(recipient);
            senderList.add(sender);
        }
        
        public void verify ()
        {
            assertTrue (recipientList.isEmpty());
            assertTrue (senderList.isEmpty());
            for (int i=0; i< msgList.size(); i++)
            {
                System.out.println ("Subject: "+subjectList.get(i));
                System.out.println (msgList.get(i));
                System.out.println ("========");
            }
            reset();
        }
        
        public void reset ()
        {
            recipientList.clear();
            senderList.clear();
            msgList.clear();
            subjectList.clear();
        }
    }
    
    public NotificationManagerTest ()
    {
        
    }
    
    public void setUp ()
    throws Exception
    {
             
       Properties props = new Properties();
       props.put ("admin.email", "janb@mortbay.com");
       props.put ("system.email", "testsystem@mortbay.com");    
       props.put ("User", "janb");     
       props.put ("Password", "Chocolate");
       props.put ("mail.smtp.host", "mail.mortbay.com");
       props.put ("mail.debug", "false"); 
       props.put ("remindersleep.hrs", "1");       
       props.put ("remindertotal.days", "1");       
       props.put ("depositdue.hrs", "24");
       props.put ("totaldue.days", "7");
       props.put ("colletta.website", "http://www.blah.blah.it");
       props.put ("colletta.phone", "999912345");
       props.put ("colletta.email", "janb@mortbay.com");
       props.put ("root.dir", Util.findWebInf());
       
       Configuration cfg = Configuration.getInstance();   
       cfg.load (props);
              
       File tmpUsers = File.createTempFile("tmpuser", "properties");
       tmpUsers.deleteOnExit();
       OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tmpUsers), "UTF-8");
       writer.write("janb:xxx:JanBartel:janb@mortbay.com:1234:fr:EDITOR");
       
       writer.flush();
       writer.close();
      
       
       User.loadUsers(tmpUsers.toURL());
       
      
       
       rd.setName ("Jacky Onassis");
       rd.setStartDate (new YyyyMmDd(2005, 7, 21));
       rd.setEndDate (new YyyyMmDd(2005, 7, 30));
       YyyyMmDd now = new YyyyMmDd();
       now.setToNow();
       rd.setEmail("janb@mortbay.com");
       rd.setLanguage("EN");
       rd.setManager("janb");
       rd.setPrice (new BigDecimal("950.00"));
       rd.setCurrency ("EUR");
       rd.addAdjustment(Adjustment.__DISCOUNT, new BigDecimal("120.00"), null);
       rd.setAptId("BC");
       rd.setStatus (ReservationStatus.APPROVED);
       rd.setId("9999");
    }
    
    public void tearDown ()
    throws Exception
    {
        
    }
    
  
    
    public void testEmailNotifications()
    throws Exception
    {   	
      EmailNotificationManager nm = new EmailNotificationManager();
      
      
      //set null EmailServer
      nm.emailServer = new MockEmailServer();
      
      //    test REQUESTED MESSAGE
      //send to client
      ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");
      //send to the manager
      ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");
      //send to mortbay
      ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com"); 
      nm.notify(EmailNotificationManager.Type.CREATED, rd);     
      System.out.println ("---- REQUESTED MESSAGE TEST ----");
      ((MockEmailServer)nm.emailServer).verify();
      System.out.println ("---- END ----");
      
      
      // test APPROVED MESSAGE
      //send to client
      ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");
      //send to the manager
      ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");
      //send to mortbay
      ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");    
      nm.notify(EmailNotificationManager.Type.APPROVED, rd);
      System.out.println ("---- APPROVED MESSAGE TEST ----");
      ((MockEmailServer)nm.emailServer).verify();
      System.out.println ("---- END ----");      
      
      // test AUTOMATIC APPROVED MESSAGE
      //send to client
      ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");
      //send to the manager
      ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");
      //send to mortbay
      ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");    
      nm.notify(EmailNotificationManager.Type.AUTO_APPROVED, rd);
      System.out.println ("---- AUTO APPROVED MESSAGE TEST ----");
      ((MockEmailServer)nm.emailServer).verify();
      System.out.println ("---- END ----");          
      
      // test CONFIRMED MESSAGE
      //send to client
      ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");
      //send to the manager
      ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");
      //send to mortbay
      ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");    
      //send to village
      ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");    
      nm.notify(EmailNotificationManager.Type.CONFIRMATION, rd);
      System.out.println ("---- CONFIRM MESSAGE TEST ----");
      ((MockEmailServer)nm.emailServer).verify();
      System.out.println ("---- END ----");
      
      // test EXPIRY MESSAGE
      //send to client
      ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");
      //send to the manager
      ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");
      //send to mortbay
      ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");    
      nm.notify(EmailNotificationManager.Type.EXPIRED, rd);
      System.out.println ("---- EXPIRY MESSAGE TEST ----");
      ((MockEmailServer)nm.emailServer).verify();
      System.out.println ("---- END ----");
      
      // test CANCELLED MESSAGE
      //send to client
      ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");
      //send to the manager
      ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");
      //send to mortbay
      ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com"); 
      //send to colletta
      ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com"); 
      nm.notify(EmailNotificationManager.Type.CANCELLED, rd);
      System.out.println ("---- CANCEL MESSAGE TEST ----");
      ((MockEmailServer)nm.emailServer).verify();
      System.out.println ("---- END ----");
      
      //test UPDATE MESSAGE
      //send to client
      ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");
      //send to the manager
      ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");
      //send to mortbay
      ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");    
      nm.notify(EmailNotificationManager.Type.UPDATE, rd);
      System.out.println ("---- UPDATE MESSAGE TEST ----");
      ((MockEmailServer)nm.emailServer).verify();
      System.out.println ("---- END ----");
      
      //test TOTAL-DUE MESSAGE
      //send to client
      ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");
      //send to the manager
      ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");
      //end to mortbay
      ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");    
      nm.notify(EmailNotificationManager.Type.TOTAL_DUE, rd);
      System.out.println ("---- TOTAL-DUE MESSAGE TEST ----");
      ((MockEmailServer)nm.emailServer).verify();
      System.out.println ("---- END ----");
      
    }
    
    public void testEmailNotificationLanguages ()
    throws Exception
    {
        //the default is the EmailNotificationManager
        EmailNotificationManager nm = new EmailNotificationManager();
        
        //really hacky, set null EmailServer
        nm.emailServer = new MockEmailServer();
        //send to client
        ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");
        //send to the manager
        ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");
        //send to mortbay
        ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");
        
        
        nm.notify(EmailNotificationManager.Type.APPROVED, rd);
        
       
        MockEmailServer mes = (MockEmailServer)(nm.emailServer);
        mes.verify();
        
        rd.setLanguage("RU");
        //expect fallback to EN
        //client
        ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");
        //manager
        ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");
        //mortbay
        ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");
        nm.notify(EmailNotificationManager.Type.APPROVED, rd);
        ((MockEmailServer)nm.emailServer).verify();
        
        rd.setLanguage("EN");
        //client
        ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");
        //manager
        ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");
        //mortbay
        ((MockEmailServer)nm.emailServer).expectSend ("janb@mortbay.com", "testsystem@mortbay.com");
        nm.notify(EmailNotificationManager.Type.CREATED, rd);
        ((MockEmailServer)nm.emailServer).verify();
        
    }
}

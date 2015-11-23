/*
 * Created on May 5, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package it.colletta.reservation;

import it.colletta.Configuration;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.mortbay.iwiki.YyyyMmDd;
import com.mortbay.iwiki.YyyyMmDdHM;

import junit.framework.TestCase;

/**
 */
public class ReservationTimerTest extends TestCase
{
    private ReservationData[] reservations = null;
    private MockNotificationManager nmgr = null;
    
    private class MockNotificationManager implements NotificationManager
    {
        
        private Map exMap = new HashMap();
        
        
        public void notify(String notificationType, ReservationData rd)
        {
            System.out.println ("Notify: "+notificationType+" id="+rd.getId());
            assertTrue (exMap.containsKey(rd.getId()));
            
            String notification = (String)exMap.get(rd.getId());
            assertEquals (notification, notificationType);
            exMap.remove(rd.getId());      
            
        }
        
        public void addExpectation (String notificationType, String id)
        {
            exMap.put (id, notificationType);
        }
        
        public void checkExpectations ()
        {
            System.err.println("exMap="+exMap);
            assertTrue (exMap.isEmpty());
        }
        
        public void reset()
        {
            exMap.clear();
        }
        
    }
    
    private class MockReservationDAO implements ReservationDAO
    {
        public void save(ReservationData rd) throws Exception
        {
            // TODO Auto-generated method stub
            
        }

       
        public ReservationData load(String resId) throws Exception
        {
            // TODO Auto-generated method stub
            return null;
        }

       
        public ReservationData[] loadAll() throws Exception
        {
            return reservations;
        }        
    }
    
    public ReservationTimerTest()
    {
    }
    
    public void setUp()
    throws Exception
    {
        Properties props = new Properties();
        props.put ("admin.email", "janb@mortbay.com");
        props.put ("system.email", "testsystem@mortbay.com");
        props.put("User", "janb");     
        props.put("Password", "Chocolate");
        props.put("mail.smtp.host", "mail.mortbay.com");
        props.put("mail.debug", "false");
        props.put ("remindersleep.hrs", "1"); //run once per hour
        props.put ("remindertotal.days", "1"); //send a total due reminder every day
        props.put("totaldue.days", "5");
        props.put ("depositdue.hrs", "24");
        props.put ("colletta.website", "http://www.blah.blah.it");
        props.put ("colletta.phone", "999912345");
        Configuration.getInstance().load(props);
        nmgr = new MockNotificationManager();
       
        NotificationManagerFactory.setNotificationManager(nmgr);
        ReservationDAOFactory.setReservationDAO(new MockReservationDAO ());
    }
    
    public void tearDown()
    throws Exception
    {
        
    }
    
    
    
    
    public void testNotifications ()
    throws Exception
    {
        //set up some reservations
        YyyyMmDd now = new YyyyMmDd();
        
        //r0 should not be expired: not within deposit.hrs even tho approved
        ReservationData r0 = new ReservationData();
        r0.setId("0");
        r0.setAptId("apt0");
        r0.addHistory((new YyyyMmDd()),"Test","CREATED");
        r0.addHistory((new YyyyMmDd()),"Test","APPROVED");
        r0.setStartDate(new YyyyMmDd(2010, 1, 1));
        r0.setEndDate (new YyyyMmDd(2010, 1, 20));
        r0.setStatus(ReservationStatus.APPROVED);
        r0.setPrice(new BigDecimal("100.00"));
        
        //r1 should be EXPIRED: no deposit > x hrs after approval
        ReservationData r1 = new ReservationData();
        r1.setId("1");
        r1.setAptId("apt1");
        r1.addHistory((new YyyyMmDd(2004,2,1)),"Test","CREATED");
        r1.setStartDate(new YyyyMmDd(2004, 10, 1));
        r1.setEndDate (new YyyyMmDd(2004, 10, 10));
        r1.setStatus(ReservationStatus.APPROVED);
        r1.addHistory((new YyyyMmDd(2004, 2, 2)),"Test","APPROVED");
        r1.setPrice(new BigDecimal("100.00"));
        
        //r2 should TOTAL-DUE: deposit paid && less than totaldue.days before booking
        ReservationData r2 = new ReservationData();
        r2.setId("2");
        r2.setAptId("apt2");
        r2.addHistory((new YyyyMmDd(2004, 3, 1)),"Test","CREATED");
        now.addDays(4);
        r2.setStartDate(now);
        now.addDays(10);
        r2.setEndDate(now);
        r2.setStatus(ReservationStatus.APPROVED);
        r2.addHistory((new YyyyMmDd(2004, 3, 2)),"Test","APPROVED");
        r2.setPaid (new BigDecimal("20.00"));
        r2.setPrice(new BigDecimal("100.00"));
        
        //r3 should be EXPIRED: paid < deposit
        ReservationData r3 = new ReservationData();
        r3.setId("3");
        r3.setAptId("apt3");
        r3.addHistory( (new YyyyMmDd(2004, 3, 1)),"Test","CREATED");
        r3.setStartDate (new YyyyMmDd(2004, 10, 1));
        r3.setEndDate (new YyyyMmDd(2004, 10, 10));
        r3.setStatus(ReservationStatus.APPROVED);
        r3.addHistory((new YyyyMmDd(2004, 3, 2)),"Test","APPROVED");
        r3.setPaid(new BigDecimal("10.00"));
        r3.setPrice(new BigDecimal("100.00"));
        
        //r4 shoult NOT CHANGE: not approved && not within total.days
        now = new YyyyMmDd();
        ReservationData r4 = new ReservationData();
        r4.setId("4");
        r4.setAptId("apt4");
        r4.addHistory((new YyyyMmDd(2004, 3, 1)),"Test","CREATED");
        now.addDays(30);
        r4.setStartDate(now);
        now.addDays(15);
        r4.setEndDate(now);
        r4.setStatus(ReservationStatus.REQUESTED);
        r4.setPrice(new BigDecimal("100.00"));
        
        //r5 should be TOTAL-DUE: confirmed even tho no deposit &&  within total.days
        ReservationData r5 = new ReservationData();
        r5.setId("5");
        r5.setAptId("apt5");
        r5.addHistory((new YyyyMmDd(2004, 3, 1)),"Test","CREATED");
        now = new YyyyMmDd();
        now.addDays(3);
        r5.setStartDate(now);
        now.addDays(5);
        r5.setEndDate(now);
        r5.addHistory((new YyyyMmDd(2004,3,2)),"Test","APPROVED");
        r5.setStatus(ReservationStatus.CONFIRMED);
        r5.setPrice(new BigDecimal("100.00"));
        
        //r6 should be TOTAL_DUE: approved and paid deposit 
        ReservationData r6 = new ReservationData();
        r6.setId("6");
        r6.setAptId("apt6");
        r6.addHistory((new YyyyMmDd(2004,2,1)),"Test","CREATED");
        now = new YyyyMmDd();
        now.addDays(2);
        r6.setStartDate(now);
        now.addDays(10);
        r6.setEndDate(now);
        r6.setStatus(ReservationStatus.APPROVED);
        r6.addHistory((new YyyyMmDd(2004, 2, 2)),"Test","APPROVED");
        r6.setPrice(new BigDecimal("100.00"));
        r6.setPaid (r6.getDeposit());
        
                
        //r7 should NOT be reminded: < total.days but already paid
        ReservationData r7 = new ReservationData();
        r7.setId("7");
        r7.setAptId("apt7");
        r7.addHistory((new YyyyMmDd(2004, 2, 1)),"Test","CREATED");
        now = new YyyyMmDd();
        now.addDays(1);
        r7.setStartDate(now);
        now.addDays(10);
        r7.setEndDate(now);
        r7.setStatus(ReservationStatus.CONFIRMED);
        r7.addHistory((new YyyyMmDd(2004, 2, 2)),"Test","APPROVED");
        r7.setPrice(new BigDecimal("100.00"));
        r7.setPaid(new BigDecimal("100.00"));
        
        //r8 should not be TOTAL DUE: !paid fully but we sent a reminder less than remindertotal.days
        ReservationData r8 = new ReservationData();
        r8.setId("8");
        r8.setAptId("apt8");
        r8.addHistory((new YyyyMmDd(2004, 2, 1)),"Test","CREATED");
        now = new YyyyMmDd();
        now.addDays(10);
        r8.setStartDate(now);
        now.addDays(20);
        r8.setEndDate(now);
        r8.setStatus(ReservationStatus.CONFIRMED);
        r8.addHistory((new YyyyMmDd(2004,2,2)),"Test","APPROVED");
        r8.setPrice(new BigDecimal("100.00"));
        r8.setPaid(r8.getDeposit());
        YyyyMmDdHM lastreminder = new YyyyMmDdHM();
        lastreminder.addDays(-3);//pretend we sent the reminder 3 days ago
        r8.setLastTotalDueReminder(lastreminder);
        
        //r9 should be TOTAL-DUE: < totaldue.days to go
        ReservationData r9 = new ReservationData();
        r9.setId("9");
        r9.setAptId("apt9");
        r9.addHistory((new YyyyMmDd(2004,2,1)),"Test","CREATED");
        now = new YyyyMmDd();
        now.addDays(3);
        r9.setStartDate(now);
        now.addDays(10);
        r9.setEndDate(now);
        r9.addHistory((new YyyyMmDd(2004,2,2)),"Test","APPROVED");
        r9.setStatus(ReservationStatus.CONFIRMED);
        r9.setPrice(new BigDecimal("100.00"));
        r9.setPaid(r9.getDeposit());
        r9.notified (NotificationManager.Type.TOTAL_DUE);
        
        //r10 should be nothing, as the date of the reservation has passed
        ReservationData r10 = new ReservationData();
        r10.setId("10");
        r10.setAptId("apt10");
        r10.addHistory((new YyyyMmDd(2004,2,1)),"Test","CREATED");
        r10.setStartDate(new YyyyMmDd(2004,10,10));
        r10.setEndDate(new YyyyMmDd(2004,10,17));
        r10.addHistory((new YyyyMmDd(2004,2,2)),"Test","APPROVED");
        r10.setStatus(ReservationStatus.CONFIRMED);
        r10.setPrice(new BigDecimal("100.00"));
        r10.notified (NotificationManager.Type.TOTAL_DUE);
        
        
        
        //r11 should NOT be reminded: > total.days 
        ReservationData r11 = new ReservationData();
        r11.setId("11");
        r11.setAptId("apt11");
        r11.addHistory((new YyyyMmDd(2004, 2, 1)),"Test","CREATED");
        now = new YyyyMmDd();
        now.addDays(10);
        r11.setStartDate(now);
        now.addDays(10);
        r11.setEndDate(now);
        r11.setStatus(ReservationStatus.CONFIRMED);
        r11.addHistory((new YyyyMmDd(2004, 2, 2)),"Test","APPROVED");
        r11.setPrice(new BigDecimal("100.00"));
       
        
        reservations = new ReservationData[] {r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11};  
        for (int i=0;i<reservations.length;i++)
        {
            reservations[i].setPhone1("0000"+(i+1));
            reservations[i].setName("Test Name "+(i+1));
        }
        nmgr.addExpectation (NotificationManager.Type.EXPIRED, "1");
        nmgr.addExpectation (NotificationManager.Type.EXPIRED, "3"); 
        nmgr.addExpectation (NotificationManager.Type.TOTAL_DUE, "2");
        nmgr.addExpectation (NotificationManager.Type.TOTAL_DUE, "5");
        nmgr.addExpectation (NotificationManager.Type.TOTAL_DUE, "9");
        nmgr.addExpectation (NotificationManager.Type.TOTAL_DUE, "6");
        ReservationManager mgr = ReservationManager.getInstance();
        
        
        
        ReservationTimer timer = new ReservationTimer();
        timer.start();
        Thread.sleep (2000L); 
        timer.stop();   
        nmgr.checkExpectations();
    }
    
    
    
   
}

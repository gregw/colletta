/*
 * Created on Apr 27, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package it.colletta.reservation;


import it.colletta.Configuration;

import java.util.logging.Level;
import java.util.logging.Logger;


import com.mortbay.iwiki.YyyyMmDd;
import com.mortbay.iwiki.YyyyMmDdHM;

/**
 * ReservationTimer
 *
 * Thread that periodically wakes up and checks the reservations
 * for:
 * 1. reservations that have not had the deposit paid within
 *    a configurable timeout
 * 2. reservations that are within a configurable period of
 *    arrival date but which have not been fully paid
 * 
 */
public class ReservationTimer
{
    private static final Logger log = Logger.getLogger(ReservationTimer.class.getName());

    public static final long MILLIS_PER_HOUR = 1000 * 60 * 60;
    public static final long DEFAULT_REMINDER_SLEEP_HOURS = 6; //wake up thread every 6 hours
    public static final long DEFAULT_TOTAL_DUE_REMINDER_DAYS = 7;  //send reminder every 7 days about total due
    public static final long DEFAULT_TOTAL_DUE_DAYS = 56; //balance due 56 days before booking starts
    private static final String REMINDER_INTERVAL_PROP = "remindersleep.hrs";
    private static final String REMINDER_TOTAL_DUE_REMINDER_PROP = "remindertotal.days";
    private static final String REMINDER_TOTAL_DUE_PROP = "totaldue.days";
    private long reminderInterval = Configuration.getInstance().getLongProperty(REMINDER_INTERVAL_PROP, new Long(DEFAULT_REMINDER_SLEEP_HOURS).longValue()); //Long.getLong(REMINDER_INTERVAL_PROP, new Long(DEFAULT_REMINDER_SLEEP_HOURS)).longValue();
    private long totalDueReminder = Configuration.getInstance().getLongProperty(REMINDER_TOTAL_DUE_REMINDER_PROP, new Long(DEFAULT_TOTAL_DUE_REMINDER_DAYS).longValue()); //Long.getLong(REMINDER_TOTAL_DUE_REMINDER_PROP, new Long(DEFAULT_TOTAL_DUE_REMINDER_DAYS)).longValue();
    private long totalDue = Configuration.getInstance().getLongProperty(REMINDER_TOTAL_DUE_PROP, new Long(DEFAULT_TOTAL_DUE_DAYS).longValue());   //Long.getLong(REMINDER_TOTAL_DUE_PROP, new Long(DEFAULT_TOTAL_DUE_DAYS)).longValue();
    private boolean started = false;
    private NotificationManager notificationManager = null;
    private Worker worker = null;
    
    
    public class TimerFilter implements ReservationDataFilter
    { 
        /**
         * Examine all bookings
         * @see it.colletta.reservation.ReservationDataFilter#accept(it.colletta.reservation.ReservationData)
         */
        public boolean accept(ReservationData rd)
        {
            if (rd == null)
                return false;
                        
            YyyyMmDd now = new YyyyMmDd();

            System.err.println("Filter at reservation "+rd.getId());
            // test if reservation should be expired
            try
            {
                checkExpiry (rd, now);
            }
            catch(Exception e)
            {
                log.log(Level.WARNING,"expire ",e);
            }
                
            // check if there are reminders for total payments that need to be sent
            try
            {
                checkReminder (rd, new YyyyMmDd());
            }
            catch (Exception e)
            {
                log.log(Level.WARNING,"reminder due ", e);
            }
            return false; //return false because processing is already done, and it is pointless to accumulate results
        } 
    }
  
    
    public class Worker extends Thread
    {
        public Worker()
        {
          super("ReminderThread");
          setDaemon(true);
        }


        public void run ()
        {
            //wake up every INTERVAL and check state of reservations
            boolean ok = true;
            while (ok)
            {
                try
                {
                    ReservationTimer.this.processReservations();
                    log.log(Level.FINE,"Reminder timer thread sleeping for "+reminderInterval+"hrs");
                    sleep (reminderInterval * MILLIS_PER_HOUR);
                }
                catch (InterruptedException ie)
                {
                    ReservationTimer.log.info("Stopping reminder thread on interrupt");
                    ok = false;
                }
                catch (Exception e)
                {
                    ReservationTimer.log.log(Level.WARNING,"Error processing reservations for reminders",e);
                }
            }
        }
    }
    
    
    public ReservationTimer ()
    {
        notificationManager = NotificationManagerFactory.getNotificationManager();
        worker = new Worker();
    }
    
    public synchronized void start ()
    {
        if (!started)
        {
            started = true;
            worker.start();
        }
        
    }
    
    public synchronized void stop ()
    {
        worker.interrupt();
        started = false;
    }
    
    public void processReservations ()
    throws Exception
    {
        log.log(Level.FINE,"Reminder thread processing reservations");
        TimerFilter filter = new TimerFilter();
        ReservationManager.getInstance().findReservationIds (filter);
    }
    
    
        
    /**
     * Check for reminders for total due.
     * 
     * 28/12/2006: reminder is due iff total not paid. Also ensure we only
     * send this once a week.
     * @param rd
     * @param now
     * @return
     */
    public void checkReminder (ReservationData rd, YyyyMmDd now) throws Exception
    {
        if (rd == null)
            return;
        
        System.err.println ("Total-due reminder: considering id="+rd.getId());
        System.err.println ("Days to go: "+now.daysTo(rd.getStartDate())+ " and totalDue days = "+totalDue);
       
        if ((rd.getStatus().equals (ReservationStatus.APPROVED) || (rd.getStatus().equals(ReservationStatus.CONFIRMED))) 
       		&&
       		(now.before(rd.getStartDate()))
                &&
                (rd.getPaid().compareTo(rd.getTotal()) < 0)
                && 
                (rd.getDiscountCode()==null || !rd.getDiscountCode().toLowerCase().startsWith("mamberto"))
                &&
                (now.daysTo(rd.getStartDate()) <= totalDue)
                &&
                ((rd.getLastTotalDueReminder()==null) || (rd.getLastTotalDueReminder().daysTo(now) >= totalDueReminder))
        )
        {         
            notificationManager.notify(NotificationManager.Type.TOTAL_DUE, rd);
            rd.notified (NotificationManager.Type.TOTAL_DUE);
            rd.setLastTotalDueReminder(now);
            ReservationManager.getInstance().updateReservation(rd);
        }
    }
    
    
    
    
    /**
     * Check for reservations that should be expired.
     * 
     * A reservation can expire iff:
     * 
     * 1. an amount at least equivalent to the deposit was not paid within the given interval
     * 
     * @param rd the booking to check
     * @param now the date now
     * @return
     */
    public void checkExpiry (ReservationData rd, YyyyMmDd now) throws Exception
    {
        log.info ("Expiry: considering id="+rd.getId());
        if ((rd.getStatus().equals(ReservationStatus.APPROVED)))
        {
        	System.err.println("Checking reservation that is approved: "+rd.getId());
        	//if what they've paid is less than the deposit ...
        	if (rd.getPaid().compareTo(rd.getDeposit()) < 0)
        	{
        		// ... and it's been xhrs since they booked ...
        		String approval = rd.getLastHistoryWith(ReservationStatus.APPROVED.toString());
        		if (approval!=null)
        		{
        			System.err.println("Reservation "+rd.getId()+" is approved");
        			YyyyMmDdHM approvalDate = ReservationData.getHistoryDate(approval);
        			if (approvalDate != null)
        			{
        				
        				long interval = ((System.currentTimeMillis() - approvalDate.getTimeInMillis())/MILLIS_PER_HOUR);
        				log.info ("Expiry: id="+rd.getId() +" was approved "+interval+"hrs ago.");

        				if (interval/ReservationManager.getInstance().depositDueHours >= 1)
        				{
        					ReservationManager.getInstance().expireReservation(rd.getId());
        					return;                  
        				}               
        			}
        		}
        	}
        }
    }
   
}

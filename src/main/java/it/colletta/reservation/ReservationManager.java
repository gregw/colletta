package it.colletta.reservation;

import it.colletta.Apartment;
import it.colletta.Configuration;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import com.mortbay.iwiki.User;
import com.mortbay.iwiki.YyyyMmDd;
import com.mortbay.iwiki.YyyyMmDdHM;

/**
 * ReservationManager.java
 * 
 * <pre>
 * Reservation State Machine:
 *                             [O]
 *                              |
 *           ___________________o________________
 *           | (dates ok&&apt!=null)            |(apt==null or !week boundary)
 *           v                                  v
 *        APPROVED                            REQUESTED  
 *           |  ^                               |     |
 *           |  |_______________________________|     |
 *           |          (apt!=null&&manual)           |(manual)
 *           |                                        |
 * __________o_______________________________________ | 
 * |(&gt;=deposit or manual)    |(no deposit)       | |
 * |                            |                   | |
 * v                            v                   v v
 * CONFIRMED                 EXPIRED[X]          CANCELLED[X]
 * |      |                                          ^  
 * |      |__________________________________________|   
 * |               (&lt; full payment or manual)    
 * v
 * CHECKED-IN
 *     |
 *     v
 * CHECKED-OUT [X]
 *
 * </pre>
 *
 * 28/12/06  
 *  State machine:
 * <ul> 
 * <li> at least deposit must be paid immediately (within 24hrs of booking) or booking expires
 * <li> full payment must be paid BEFORE arrival by bonifico or online
 * <li> notices requesting full payment go out weekly from the time of paying deposit
 * </ul>
 *   Email notifications:
 * <ul>
 *   <li> -&lt; APPROVED: notification to pay deposit or expires
 *   <li> -&lt; CONFIRMED: notification full payment required
 *   <li> -&lt; EXPIRED: notification booking expired
 *   <li> -&lt; CANCELLED: notification booking cancelled
 *   <li> -&lt; reminders periodically start date
 * </ul>
 * 
 *
 *
 * Created: Sun Mar 28 22:32:26 2004
 *
 * @author <a href="mailto:janb@wafer">Jan Bartel</a>
 * @version 1.0
 */
public class ReservationManager 
{   
    private static final Logger log = Logger.getLogger(ReservationManager.class.getName());

    public static final int RESERVATION_ID_LENGTH = 8;
    public static final String PAYMENT="Payment";
    public static final BigDecimal ZERO = new BigDecimal("0.00");
    public static final BigDecimal DEPOSIT = new BigDecimal("0.20");
    private static final long ONE_YEAR = 365L * (1000L * 60L * 60L * 24L);
    
    public long depositDueHours = Configuration.getInstance().getLongProperty("depositdue.hrs", new Long(24).longValue()); //number of hours after making booking after which deposit must be paid
    private OccupancyManager occupancyManager;
    private AuditManager auditManager;
    private NotificationManager notificationManager;
    private Map reservationMap;
    private static ReservationDAO reservationDAO;
    private Random idGenerator;
    private static ReservationManager myInstance;
    
    

    
    public static synchronized ReservationManager getInstance()
    throws Exception
    {
        if (myInstance == null)
                new ReservationManager();
        return myInstance;
    }
    
    /**
     * Construct the reservations manager.
     * Loads the reservations.
     * @throws Exception
     */
    private ReservationManager ()
    throws Exception
    {
        if ((myInstance != null) && (myInstance != this))
            throw new IllegalStateException("Only I can call my constructor. JSPs suck.");
        myInstance = this;
        
        auditManager = (AuditManager)AuditManagerFactory.getAuditManager(); 
        if (auditManager == null)
            throw new IllegalStateException ("No AuditManager available");
        reservationMap = new HashMap();
        reservationDAO = ReservationDAOFactory.getReservationDAO();
        occupancyManager = OccupancyManagerFactory.getOccupancyManager ();
        notificationManager = NotificationManagerFactory.getNotificationManager();
        idGenerator = new Random();     
        initialize();
    }
    

    
    /**
     * Make a reservation. If no apartment is specified, then this is just
     * a request for reservation.
     * @param rd
     * @return
     * @throws Exception
     */
    public ReservationData requestReservation (ReservationData rd, boolean autoApprove)
    throws Exception
    {
        if (rd == null)
            throw new IllegalArgumentException ("No reservation presented");
        
        validate (rd);
        
        ReservationData newReservation = (ReservationData)rd.clone();
        
        //make a reservation id about 8 char long
        String id = "";
        while (id.length() < RESERVATION_ID_LENGTH || reservationMap.containsKey(id))
        {
            id = Long.toString(idGenerator.nextLong()^System.currentTimeMillis(), 36).toUpperCase();
            id=id.replace("-","2");
            id=id.replace("I","3");
            id=id.replace("O","4");
            if (id.length() > RESERVATION_ID_LENGTH)
                id = id.substring(0, RESERVATION_ID_LENGTH);
        }
        newReservation.setId(id);
        YyyyMmDdHM now = new YyyyMmDdHM();
        now.setToNow();
        User user=User.getCurrentUser();
        
        //the state of a new request is either:
        // REQUESTED: iff  (aptId==null or (aptId!=null && dates not on week boundary))
        //            then needs manual intervention to allocate apartment and/or approve booking
        // APPROVED: iff (aptId!=null && dates on week boundary)
        
        newReservation.setStatus(ReservationStatus.REQUESTED);
        
        
        //If an apartment has been specified, we can only automatically approve
        //the booking iff any of the following:
        //1. it runs from SATURDAY-SATURDAY
        //2. non SAT-SAT booking is not in high season
        //3. the manager does not insist on manual approval
        //4. the apt is zero priced, meaning it is an owner booking
        String notification = null;
        
        if (newReservation.getAptId() != null) 
        {
            Apartment apt = Apartment.getApartment(newReservation.getAptId());

            newReservation.addHistory(now,user.getName(),"CREATED");
            
            if  ((newReservation.getPriceBasis() != null) && newReservation.getPriceBasis().equalsIgnoreCase("unavailable"))
            {
                //owner booking, make it confirmed                
                occupancyManager.allocateApartment (newReservation, 
                        newReservation.getAptId(), 
                        newReservation.getStartDate(),
                        newReservation.getEndDate());
                newReservation.setStatus(ReservationStatus.CONFIRMED);
                newReservation.addHistory(now,user.getName(),ReservationStatus.CONFIRMED+" (Unavailable)");
                notification = NotificationManager.Type.CONFIRMATION;
            }
            else if (autoApprove)
            {
                //will throw exception if apartment not available
                occupancyManager.allocateApartment (newReservation, 
                        newReservation.getAptId(), 
                        newReservation.getStartDate(),
                        newReservation.getEndDate());
                newReservation.setStatus(ReservationStatus.APPROVED);
                newReservation.addHistory(now,user.getName(),ReservationStatus.APPROVED+" (auto)");
                notification = NotificationManager.Type.AUTO_APPROVED;
            }
        }
        
        //all OK
        addReservation(newReservation);
        saveReservation(newReservation);
        auditManager.record (newReservation, "Created");
      
        rd.copy(newReservation);
               
        if (notification != null)
            notificationManager.notify (notification, rd);
        else
            notificationManager.notify(NotificationManager.Type.CREATED, rd);
        
        return rd;
    }
    
    
    
    
    /**
     * Validate a reservation request. This is used by the GUI
     * to check that the apartment is available.
     * @param rd
     * @return true if an apartment is selected and is free or if no apartment is selected, false otherwise
     * @throws Exception
     */
    public boolean validateRequest (ReservationData rd)
    throws Exception
    {
        //check availability if an apartment has been selected
        if (rd.getAptId() != null)
          return occupancyManager.isApartmentAvailable(rd.getAptId(), rd.getStartDate(), rd.getEndDate());
        else
          return true;
    }
    
    
    /**
     * Make a payment against a reservation
     * @param resId
     * @param amount
     * @throws Exception
     */
    public void makePayment (String resId, BigDecimal amount, String paymentInfo)
    throws Exception
    {
        ReservationData rd = getReservation (resId);
        synchronized (rd)
        {
            BigDecimal prevTotal = rd.getPaid();
            rd.setPaid(prevTotal.add(amount));
            paymentInfo=PAYMENT+" "+amount+" "+paymentInfo;
            auditManager.record (rd, paymentInfo);
            rd.addHistory(paymentInfo);
            
            //if they have now paid the deposit amount, and they aren't already confirmed, make them confirmed
            if (ReservationStatus.APPROVED.equals(rd.getStatus()))
            {
                BigDecimal deposit = rd.getDeposit();
                if ((prevTotal.compareTo(deposit) < 0) && (rd.getPaid().compareTo(deposit))>=0)
                {
                    rd.setStatus (ReservationStatus.CONFIRMED);
                    auditManager.record (rd, "Confirmed");
                    rd.addHistory(rd.getStatus().toString());
                    notificationManager.notify(NotificationManager.Type.CONFIRMATION, rd);
                }
                else
                    notificationManager.notify(NotificationManager.Type.UPDATE, rd); 
            }
            else
                notificationManager.notify(NotificationManager.Type.UPDATE, rd);
            
            saveReservation(rd);
        }       
    }
    
    
    /* ------------------------------------------------------------ */
    /** Get sum of all payments confirmed by user.
     * @param rd Reservation data
     * @param user User
     * @return sum of all payments confirmed by user.
     */
    public static BigDecimal getPayments(ReservationData rd, String user)
    {
        List history = rd.getHistory();
        if (history==null || history.size()==0)
            return ZERO;
        
        BigDecimal payments = ZERO;
        for (int i=0;i<history.size();i++)
        {
            String item = (String)history.get(i);
            int u = item.indexOf(user);
            int p = item.indexOf(PAYMENT);
            if (u>=0 && p>u)
            {
                int s1 = item.indexOf(' ',p);
                int s2 = item.indexOf(' ',s1+1);
                payments=payments.add(new BigDecimal(s2<0?item.substring(s1+1):item.substring(s1+1,s2)));
                
            }
        }
        return payments;
    }

    /* ------------------------------------------------------------ */
    /** Get summary of all payments confirmed by user.
     * @param rd Reservation data
     * @return summary of all payments confirmed by user.
     */
    public static String getPayments(ReservationData rd)
    {
        List history = rd.getHistory();
        if (history==null || history.size()==0)
            return "";
        
        String payments="";
        for (int i=0;i<history.size();i++)
        {
            String item = (String)history.get(i);
            int p = item.indexOf(PAYMENT);
            if (p>0)
            {
                int s0 = item.lastIndexOf(' ',p-2);
                int s1 = item.indexOf(' ',p);
                int s2 = item.indexOf(' ',s1+1);
                
                payments=payments+
                  (payments.length()==0?"":", ")+
                  item.substring(s0+1,p-1) +
                  " "+
                  (s2<0?item.substring(s1+1):item.substring(s1+1,s2));
                
            }
        }
        return payments;
    }
    
 
    
    /**
     * (Re)Allocate an apartment to a booking
     *
     * The params passed in must be set as the reservation SHOULD be, not
     * just deltas. Thus, if the reservation already has an apartment allocated,
     * and just the dates are changing, then the aptId must be set to the existing 
     * apartment id.
     * @param resId the identity of the reservation
     * @param aptId the identity of an apartment
     * @param startDate start of booking
     * @param endDate end of booking
     * 
     */
    public void allocateApartment (String resId, String aptId, YyyyMmDd startDate, YyyyMmDd endDate)
    throws Exception
    {
        ReservationData res = getReservation (resId);
        if (res == null)
            throw new IllegalStateException ("No such reservation "+ resId);
        
        
        synchronized (res)
        {
            //if the reservation already has an apartment id, it cannot be removed
            if ((res.getAptId() != null) && ((aptId == null) || ("".equals(aptId.trim()))))
                throw new IllegalStateException("Apartment id cannot be removed");
            
            if (res.getStatus() == null)
                throw new IllegalStateException ("No state for reservation");
                 
            
            switch (res.getStatus().getIntCode())
            {
                case ReservationStatus.REQUESTED_INT:
                {
                    // Reservation in REQUESTED state, therefore requires manual approval before
                    // occupancy matrix is affected, so just set the new apt and/or dates
                    res.setStartDate(startDate);
                    res.setEndDate (endDate);
                    res.setAptId(aptId);
                  
                    auditManager.record (res, "Apartment allocated");
                    res.addHistory("Apt allocated");
                    saveReservation(res);  
                    break;
                }
                default:
                {
                    //Reservation is either APPROVED or CONFIRMED (ie the reservation already
                    //has an apartment allocated in the occupancy matrix), in which case we want to
                    //leave it in that state, and change the occupancy matrix
                    if (res.isOccupiable())
                    {
                        // re-allocating only the apartment, or re-allocating the dates or all  
                        occupancyManager.reallocateApartment (res, aptId, startDate, endDate);
                        
                        res.setAptId(aptId);
                        res.setStartDate(startDate);
                        res.setEndDate(endDate);
                        auditManager.record (res, "Apartment reallocated");
                        res.addHistory("Apt allocated");
                        saveReservation(res);  
                    }
                    
                    break;
                }
            }      
        }    
    }
    
    
    /**
     * Manually approve the allocation of an apartment to a reservation.
     * This is performed when booking requests are received with an apartment
     * specified, but for dates that are not on a week boundary (Sat-Sat).
     * @param resId
     * @throws Exception
     */
    public void approveReservation (String resId)
    throws Exception
    {
        ReservationData rd = getReservation (resId);
        if (rd == null)
            throw new IllegalStateException ("No such reservation "+ resId);   
        if (rd.getAptId() == null)
            throw new IllegalStateException ("No apartment allocated to reservation "+resId);
        if (rd.getStartDate() == null)
            throw new IllegalStateException ("No starting date for reservation "+resId);
        if (rd.getEndDate() == null)
            throw new IllegalStateException ("No end date for reservation "+resId);
        synchronized (rd)
        {
            //if already approved, nothing to do
            if (rd.getStatus().equals(ReservationStatus.APPROVED))
                return;
            
            //Due to the two step process (allocate an apartment then approve it)
            //it is possible that the apartment is no longer available
            occupancyManager.allocateApartment(rd, rd.getAptId(), rd.getStartDate(), rd.getEndDate());
            rd.setStatus(ReservationStatus.APPROVED);
            rd.clearNotifications();
            auditManager.record(rd, "Approved");
            rd.addHistory(rd.getStatus().toString());
            String notification = NotificationManager.Type.APPROVED;   
            saveReservation(rd);
            notificationManager.notify(notification, rd);
        }
    }
    
    
    /**
     * Confirm a reservation manually.
     * Reservation 
     * @param resId
     */
    public void confirmReservation (String resId)
    throws Exception
    {
        ReservationData rd = getReservation (resId);
        if (rd == null)
            throw new IllegalStateException ("No such reservation "+ resId);
        
        if (rd.getAptId() == null)
            throw new IllegalStateException ("No apartment allocated to reservation "+resId);
        
        if (!ReservationStatus.APPROVED.equals(rd.getStatus()))
            approveReservation(resId);
        
        synchronized (rd)
        {
        	//check if it has already been confirmed
        	if (ReservationStatus.CONFIRMED.equals(rd.getStatus()))
        		return;
        	
            rd.setStatus(ReservationStatus.CONFIRMED);
            auditManager.record (rd, "Confirmed");
            rd.addHistory(rd.getStatus().toString());     
            notificationManager.notify (NotificationManager.Type.CONFIRMATION, rd);
            saveReservation(rd);           
        }
    }
    
    
    
    /**
     * Cancel an existing reservation
     * @param resId
     * @throws Exception
     */
    public void cancelReservation (String resId)
    throws Exception
    {
        ReservationData rd = getReservation (resId);
        if (rd == null)
            throw new IllegalStateException ("No such reservation "+ resId);
        
        synchronized (rd)
        {
            //if already cancelled, nothing to do
            if (ReservationStatus.CANCELLED.equals(rd.getStatus()))
                return;
            
            if ((rd.getAptId() != null) && !ReservationStatus.REQUESTED.equals(rd.getStatus()))
                occupancyManager.deallocateApartment(rd);
            
            rd.setStatus(ReservationStatus.CANCELLED);
            rd.addHistory(rd.getStatus().toString());
            saveReservation(rd);
            auditManager.record (rd, "Cancelled");
            notificationManager.notify(NotificationManager.Type.CANCELLED, rd);
        }
    }
    
    
    /**
     * Bookings that don't have the deposit paid within a certain time
     * are thrown out.
     * @param resId
     * @throws Exception
     */
    public void expireReservation (String resId)
    throws Exception
    {
        ReservationData rd = getReservation (resId);
        if (rd == null)
            throw new IllegalStateException ("No such reservation "+ resId);
        
        synchronized (rd)
        {
        	if (ReservationStatus.EXPIRED.equals(rd.getStatus()))
        		return;//already expired
        	
            if (rd.getAptId() != null)
                occupancyManager.deallocateApartment(rd);
            
            rd.setStatus(ReservationStatus.EXPIRED);
            auditManager.record (rd, "Expired");
            rd.addHistory(rd.getStatus().toString());
            notificationManager.notify(NotificationManager.Type.EXPIRED, rd);
            saveReservation(rd);
        }
    }
    
    
    
    /**
     * Checkin for a reservation
     * @param resId
     * @throws Exception
     */
    public void checkinReservation (String resId)
    throws Exception
    {
        ReservationData rd = getReservation(resId);
        if (rd == null)
            throw new IllegalStateException ("No such reservation "+ resId);
        
        synchronized (rd)
        {
        	if (ReservationStatus.CHECKIN.equals(rd.getStatus()))
        		return;//already checked in
            rd.setStatus (ReservationStatus.CHECKIN);
            rd.addHistory(rd.getStatus().toString());
            saveReservation(rd);
            auditManager.record (rd, "Checked in");
        }
    }
    
    
    
    /**
     * Checkout for a reservation
     * @param resId
     * @throws Exception
     */
    public void checkoutReservation (String resId)
    throws Exception
    {
        ReservationData rd = getReservation (resId);
        if (rd == null)
            throw new IllegalStateException ("No such reservation "+ resId);
        
        synchronized (rd)
        {
        	if (ReservationStatus.CHECKOUT.equals(rd.getStatus()))
        		return;//already checked out
            rd.setStatus (ReservationStatus.CHECKOUT);
            rd.addHistory(rd.getStatus().toString());
            saveReservation(rd);
            auditManager.record (rd, "Checked out");
        }
    }
    
    
    
    /**
     * Make some bulk changes to an existing reservation.
     * 
     * The reservation must NOT be a delta: it must be a complete
     * record, with changes to the appropriate fields.
     * 
     * This method should not be used to allocate apartments or
     * change the state of the reservation, just data about the reservation
     * (eg name, address etc).
     * 
     * @param nd
     * @throws Exception
     */
    public void updateReservation (ReservationData nd)
    throws Exception
    {
        
        if (nd == null)
            throw new IllegalStateException ("Updated reservation is null");
       
        validate (nd);
 
        ReservationData rd = getReservation(nd.getId());
        if (rd == null)
            throw new IllegalStateException ("No such reservation "+rd.getId());
        
        synchronized (rd)
        {
            if (!nd.getStatus().equals(rd.getStatus()))
                throw new IllegalStateException ("Cannot change state of reservation with an update");
                
            //update the new data
            rd.update(nd);            
            rd.addHistory("updated");
            saveReservation(rd);
            auditManager.record (rd, "Bulk update");
            //no notification for updates, just for state changes         
        }
    }
    
    
    /**
     * Find apartments that are occupied between the given dates.
     * The from date is inclusive, but the to date is exclusive.
     * An apartment is considered available if it does NOT have a 
     * confirmed booking during that period.
     * @param from
     * @param to
     * @return
     */
    public String[] findAllocatedApartments (YyyyMmDd from, YyyyMmDd to)
    throws Exception
    {
        Set allocatedAptSet = occupancyManager.findAllocatedApartments(from, to);
        
        return (String[]) allocatedAptSet.toArray(new String[0]);
    }
    
    
    /**
     * Lookup a reservation
     * @param resId
     * @return
     */
    public ReservationData findReservation(String resId)
    {
        ReservationData rd = getReservation(resId);
        if (rd == null)
            return null;
        return new ReservationData(rd);
    }
    
    
    
    /**
     * Find the ID of reservations matching the filter
     * @param filter
     * @return
     */
    public String[] findReservationIds (ReservationDataFilter filter)
    {
        Set results = new HashSet();
        
        synchronized (reservationMap)
        {
            Iterator it = reservationMap.entrySet().iterator();
            while (it.hasNext())
            {
                ReservationData rd = (ReservationData)((Map.Entry)it.next()).getValue();
                System.err.println("Iterating at "+rd.getId());
                synchronized(rd)
                {
                    if (filter.accept(rd))
                        results.add(rd.getId());
                }	
            }
        }
        return (String[])results.toArray(new String[0]);
    }
    
    
    
    /**
     * Report on the occupancy of a particular apartment for a particular month.
     * There is a fixed array of 31 ReservationData objects returned. Null 
     * indicates no reservation for that day.
     * @param aptId
     * @param month
     * @return
     */
    public ReservationData[] findReservations (String aptId, int yyyymm)
    throws Exception
    {
        ReservationData[] reservations = null;
        
        synchronized (reservationMap)
        {
            String ids[] = occupancyManager.findReservations(aptId, yyyymm);
            reservations = new ReservationData[ids.length];
            for (int i=0; i<ids.length; i++)
            {
                ReservationData res = (ReservationData)reservationMap.get(ids[i]);
                if (res == null)
                    reservations[i] = null;
                else
                    reservations[i] = new ReservationData(res);
            }
        }
        return reservations;
    }
    
    

    
    /**
     * Get all reservations in a state
     * @param status
     * @return
     * @throws Exception
     */
    public ReservationData[] findReservations (ReservationStatus status)
    throws Exception
    {
        Set results = new HashSet();
        
        synchronized (reservationMap)
        {
            Iterator it = reservationMap.entrySet().iterator();
            while (it.hasNext())
            {
                ReservationData rd = (ReservationData)((Map.Entry)it.next()).getValue();
                if (rd.getStatus().equals(status))
                    results.add(new ReservationData(rd));
            }
        }
        return (ReservationData[])results.toArray(new ReservationData[0]);
    }
    
    
    /**
     * Get all reservations that fall at least partially between the dates
     * @param status
     * @param from Inclusive start date
     * @param to Inclusive end date
     * @return
     * @throws Exception
     */
    public ReservationData[] findReservations (ReservationStatus status, YyyyMmDd from , YyyyMmDd to)
    throws Exception
    {
        Set results = new HashSet();
        to = new YyyyMmDd(to);
        to.addDays(1);
        
        synchronized (reservationMap)
        {
            // TODO - perhaps use a date lookup?
            Iterator it = reservationMap.entrySet().iterator();
            while (it.hasNext())
            {
                ReservationData rd = (ReservationData)((Map.Entry)it.next()).getValue();
                if (rd.getStatus().equals(status) && 
                    YyyyMmDdHM.isOverlap(from,to,rd.getStartDate(),rd.getEndDate()))
                    results.add(new ReservationData(rd));
            }
        }
        
        return (ReservationData[])results.toArray(new ReservationData[0]);
    }
    
    
    
    /**
     * Return a copy of all ReservationData objects that meet the filter criteria
     * @param filter
     * @return
     * @see it.colletta.reservation.ReservationDataFilter
     */
    public ReservationData[] findReservations (ReservationDataFilter filter)
    {
        Set results = new HashSet();
        
        synchronized (reservationMap)
        {
            Iterator it = reservationMap.entrySet().iterator();
            while (it.hasNext())
            {               
                ReservationData rd = (ReservationData)((Map.Entry)it.next()).getValue();
                synchronized(rd)
                {
                    if (filter.accept(rd))
                        results.add(new ReservationData(rd));
                }
            }
        }
        return (ReservationData[])results.toArray(new ReservationData[0]);
    }
    
    
    
    /**
     * Get a reservation from memory cache
     * @param id
     * @return
     * @throws Exception
     */
    private ReservationData getReservation (String id)
    {
        ReservationData rd = null;
        synchronized (reservationMap)
        {
            rd = (ReservationData)reservationMap.get(id);
        }
        return rd;
    }
    
    
    
    
    /**
     * Put a reservation into cache
     * @param res
     */
    private void addReservation (ReservationData res)
    {
        synchronized (reservationMap)
        {
            reservationMap.put (res.getId(), res);
        }
    }
    
    private void removeReservation (ReservationData res)
    {
        synchronized (reservationMap)
        {
            reservationMap.remove (res.getId());
        }
    }
       
    
    /**
     * Save changes to a reservation to disk
     * @param res
     * @throws Exception
     */
    private void saveReservation (ReservationData res)
    throws Exception
    {
        reservationDAO.save(res);
    }
    
    
    /**
     * Validate a reservation request.
     * @param rd
     * @throws Exception
     */
    private void validate (ReservationData rd)
    throws Exception
    {
        if (rd.getStartDate() == null)
            throw new IllegalStateException ("Start date is null");
        if (rd.getEndDate() == null)
            throw new IllegalStateException ("End date is null");
        if (rd.getEndDate().before(rd.getStartDate()))
            throw new IllegalStateException ("End date is before start date");
        
        long startMillis = rd.getStartDate().getCalendar().getTimeInMillis();
        long endMillis = rd.getEndDate().getCalendar().getTimeInMillis();
        
        //check reservation isn't longer than one year
        if ((endMillis - startMillis) > ONE_YEAR)
            throw new IllegalArgumentException ("Reservations longer than one year are not permitted");
        
        if (rd.getName() == null || rd.getName().trim().length()==0)
            throw new IllegalArgumentException ("Client's full name must be supplied for "+rd.toString());
        
        // must supply at least one of: email, phone1, phone2
        int meansOfId = 0;
               
        if ((rd.getEmail() != null) && (rd.getEmail().trim().length() != 0))
            meansOfId++;
        
        if ((rd.getPhone1() != null) && (rd.getPhone1().trim().length() != 0))
            meansOfId++;
        
        if ((rd.getPhone2() != null) && (rd.getPhone2().trim().length() != 0))
            meansOfId++;
        
        if (meansOfId < 1)
            throw new IllegalArgumentException ("Must supply one of: email address and phone number");
    }
    
   
    
    /**
     * Check the dates of a reservation for automatic approval.
     * A reservation can be automatically approved iff:
     * <ul><li>it runs from a Saturday to a Saturday</li>
     * OR
     * <li>it isn't a Saturday-Saturday but it isn't high season</li>
     * </ul>
     * @param res
     * @return
     */
    private boolean checkDatesForAutoApproval (ReservationData res)
    {
        //check for Saturday-Saturday
        if ((res.getStartDate().getDayOfWeek() == 0)
       	   && 
       	    (res.getEndDate().getDayOfWeek() == 0))
            return true;
        
        //check each day isn't a high season day
        boolean ok = true;
        for (YyyyMmDd date = new YyyyMmDd(res.getStartDate()); date.before(res.getEndDate()) && ok; date.addDays(1))
        {
            if (Apartment.getSeason(date) == Apartment.PEAK)
                ok = false;
        }
        
        return ok;
    }
    
    /**
     * Load reservations into cache and into occupancy matrix
     * @throws Exception
     */
    private void initialize ()
    throws Exception
    {
        ReservationData[] reservations = reservationDAO.loadAll();
        if (reservations == null)
            return;
        
        //set up the list of reservations
        for (int i=0; i<reservations.length; i++)
        {
            reservationMap.put (reservations[i].getId(), reservations[i]);
            System.err.println("PUT "+reservations[i].getId());
        }
        
        //init the occupancy matrix
        occupancyManager.initialize(reservations);
    }
    
}

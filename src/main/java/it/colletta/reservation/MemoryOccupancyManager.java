/*
 * Created on Mar 30, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package it.colletta.reservation;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mortbay.iwiki.YyyyMmDd;

/**
 * @author janb
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class MemoryOccupancyManager implements OccupancyManager
{
    private static final Logger log = Logger.getLogger(MemoryOccupancyManager.class.getName());
    //we ignore the 0th array element even tho we allocate it and start at 1
    private static final int MAX_MONTH_LENGTH = 32;
    private static final int MAX_MONTHS = 13;
    protected Map occupancyMatrix;
   
   
    
    
    /**
     * Constructor.
     */
    public MemoryOccupancyManager ()
    {
        occupancyMatrix = new HashMap();
    }
    
    
    /**
     * Initialize our in-memory structure for searching over reservations that
     * @param reservations
     */
    public void initialize (ReservationData[] reservations)
    throws Exception
    {
              
        occupancyMatrix.clear();
        
        if (reservations == null)
            return;
        
        // arrange into a datastructure suitable for searching:
        // list of years
        //    each year has an array of 0-11 months
        //      each month has 0-30 days
        //        each day has a map of apartment id to reservationdata object       
        for (int i=0; i<reservations.length; i++)
        {
            ReservationData res = reservations[i];
            
            //only bookings that have an apartment allocated form part of the matrix
            //NOTE: we assume that bookings that have expired, or that have passed are not
            //available to be loaded
            if ((res.getAptId() != null) && res.isOccupiable())
            {
                //insert into our search structure:
                //one apt->reservation entry per day of the rental period
                

                for (YyyyMmDd date = new YyyyMmDd(res.getStartDate()); date.before(res.getEndDate()); date.addDays(1))
                {
                    Integer year = new Integer(date.getYyyy());
                    HashMap[][] monthMap = (HashMap[][])occupancyMatrix.get(year);
                    if (monthMap == null)
                    {
                        monthMap = new HashMap [MAX_MONTHS][MAX_MONTH_LENGTH];
                        occupancyMatrix.put (year, monthMap);
                    }  
                    int month = date.getMm();
                    int day = date.getDd();
                    
                    //if no existing reservations already on the start date:
                    if (monthMap[month][day] == null)
                        monthMap[month][day] = new HashMap();
                    
                    log.log(Level.FINE,"Adding entry res="+res.getId()+" apt="+res.getAptId()+" date= "+ year+"-"+month+"-"+day);
                    //for each day of the reservation, put an entry into the occupancy matrix           
                    monthMap[month][day].put (res.getAptId(), res);
                }
            }  
        }
        log.log(Level.FINE,"Init complete!");
    }
    
    

    /** 
     * @see it.colletta.reservation.OccupancyManager#findAllocatedApartments(com.mortbay.iwiki.YyyyMmDd, com.mortbay.iwiki.YyyyMmDd)
     */
    public Set findAllocatedApartments(YyyyMmDd from, YyyyMmDd to) throws Exception
    {
        if (from == null)
            throw new IllegalArgumentException ("From date cannot be null");
        if (to == null)
            throw new IllegalArgumentException ("To date cannot be null");
        
        HashSet aptSet = new HashSet();       
        synchronized (occupancyMatrix)
        {
 
            for (YyyyMmDd date = new YyyyMmDd(from); date.before(to); date.addDays(1))
            {
                HashMap[][] monthMap = (HashMap[][])occupancyMatrix.get(new Integer(date.getYyyy()));
                if (monthMap != null)
                {
                    HashMap dayMap = monthMap[date.getMm()][date.getDd()];
                    if (dayMap != null)
                    {
                        aptSet.addAll(dayMap.keySet());
                    }
                }
            }
        }
        return aptSet;
    }


    /**
     * @see it.colletta.reservation.OccupancyManager#findReservations(com.mortbay.iwiki.YyyyMmDd, com.mortbay.iwiki.YyyyMmDd)
     */
    public Set findReservations(YyyyMmDd from, YyyyMmDd to) throws Exception
    {
       
        HashSet set = new HashSet();
        
        synchronized (occupancyMatrix)
        {
            
            for (YyyyMmDd date = new YyyyMmDd(from); date.before(to); date.addDays(1))
            {
                int year = date.getYyyy();
                HashMap[][] dayMap = (HashMap[][])occupancyMatrix.get(new Integer(year));
                HashMap resMap = dayMap[date.getMm()][date.getDd()];
                set.addAll (resMap.values());
            }
        }
        
        return set;
    }


    
    
    /**
     * @see it.colletta.reservation.OccupancyManager#findReservations(it.colletta.reservation.ReservationStatus, com.mortbay.iwiki.YyyyMmDd, com.mortbay.iwiki.YyyyMmDd)
     */
    public Set findReservations(ReservationStatus status, YyyyMmDd from, YyyyMmDd to) throws Exception
    {
        Set resSet = findReservations(from, to);
        
        synchronized (occupancyMatrix)
        {
            Iterator itor = resSet.iterator();
            while (itor.hasNext())
            {
                ReservationData rd = (ReservationData)itor.next();
                if (!rd.getStatus().equals(status))
                    resSet.remove(rd);
            }
        }
        return resSet;
    }


    
    
    /**
     * @see it.colletta.reservation.OccupancyManager#isApartmentAvailable(java.lang.String, com.mortbay.iwiki.YyyyMmDd, com.mortbay.iwiki.YyyyMmDd)
     */
    public  boolean isApartmentAvailable(String aptId, YyyyMmDd from, YyyyMmDd to) throws Exception
    {
        if (aptId == null)
            throw new IllegalArgumentException ("No apartment id");
        if (from == null)
            throw new IllegalArgumentException ("No from date");
        if (to == null)
            throw new IllegalArgumentException ("No to date");
        
        boolean available = true;
        synchronized (occupancyMatrix)
        {
            for (YyyyMmDd date = new YyyyMmDd(from); (available && date.before(to)); date.addDays(1))
            {
                HashMap[][] monthMap = (HashMap[][])occupancyMatrix.get(new Integer(date.getYyyy()));
                if (monthMap != null)
                {
                    HashMap dayMap = monthMap[date.getMm()][date.getDd()];
                    if (dayMap != null)
                    {
                        if (dayMap.containsKey(aptId.trim()))
                            available = false;
                    }
                }
            }
        }
        return available;
    }


    /**
     * @see it.colletta.reservation.OccupancyManager#findReservations(java.lang.String, int)
     */
    public String[] findReservations(String aptId, int yyyymm) throws Exception
    {
        if (aptId == null)
            throw new IllegalStateException ("Apartment is null");
        
        YyyyMmDd month = new YyyyMmDd ();
        month.setYyyymm(yyyymm);
        
        //make an array long enough to store any month length
        String[] ids = new String[MAX_MONTH_LENGTH];
        
        synchronized (occupancyMatrix)
        {
            HashMap[][] yearMap = (HashMap[][])occupancyMatrix.get(new Integer(month.getYyyy()));
            if (yearMap != null)
            {
                for (int i=0; i< MAX_MONTH_LENGTH; i++)
                {
                    HashMap dayResMap = yearMap[month.getMm()][i];
                    if (dayResMap != null)
                    {
                        ReservationData res = (ReservationData)dayResMap.get(aptId);
                        if (res != null)
                            ids[i] = res.getId();
                    }
                }
            }
        }
        return ids;
    }


    /**
     * @see it.colletta.reservation.OccupancyManager#allocateApartment(java.lang.String, java.lang.String)
     */
    public void allocateApartment(ReservationData res, String aptId, YyyyMmDd startDate, YyyyMmDd endDate)
    throws Exception
    {
        if (res == null)
            throw new IllegalArgumentException ("No such reservation: "+res.getId());
        if (aptId == null)
            throw new IllegalArgumentException ("Apartment is null");
        if (startDate == null)
            throw new IllegalArgumentException ("Start date is null");
        if (endDate == null)
            throw new IllegalArgumentException ("End date is null");
        
        synchronized (occupancyMatrix)
        {
            if (!isApartmentAvailable(aptId, startDate, endDate))
                throw new IllegalStateException ("Apartment "+aptId+" is not available start="+startDate+"end="+endDate);
            
            for (YyyyMmDd date = new YyyyMmDd(startDate); date.before(endDate); date.addDays(1))
            {
                Integer year = new Integer(date.getYyyy());
                
                HashMap[][] monthMap = (HashMap[][])occupancyMatrix.get(year);
                
                if (monthMap == null)
                {
                    monthMap = new HashMap [MAX_MONTHS][MAX_MONTH_LENGTH];
                    occupancyMatrix.put (year, monthMap);
                }  
                int month = date.getMm();
                int day = date.getDd();
                
                //if no existing reservations already on the start date:
                if (monthMap[month][day] == null)
                    monthMap[month][day] = new HashMap();
                
                //insert the reservation for the apartment for that day        
                monthMap[month][day].put (aptId, res);
            }

        }
          
    }


    /**
     * 
     * @see it.colletta.reservation.OccupancyManager#deallocateApartment(it.colletta.reservation.ReservationData)
     */
    public void deallocateApartment(ReservationData res) throws Exception
    {
       if (res == null)
           throw new IllegalArgumentException ("No reservation");
       if (res.getAptId() == null)
           return;
       if (res.getStartDate() == null)
           return;
       if (res.getEndDate() == null)
           return;
       
       synchronized (occupancyMatrix)
       {         
           
           for (YyyyMmDd date = new YyyyMmDd(res.getStartDate()); date.before(res.getEndDate()); date.addDays(1))
           {
               Integer year = new Integer (date.getYyyy());
               HashMap[][] dayMap = (HashMap[][])occupancyMatrix.get(year);
               if (dayMap == null)
                   throw new IllegalStateException ("No reservations recorded for "+year);
               
               HashMap resMap = dayMap[date.getMm()][date.getDd()];
               if (resMap == null)
                   throw new IllegalStateException ("No reservations recorded for "+date);
               ReservationData prev = (ReservationData)resMap.remove(res.getAptId());  
               if (prev != null)
                   log.info("Deallocated booking id "+prev.getId()+ " for "+prev.getAptId());
           } 
       }
    }


    /**
     * @see it.colletta.reservation.OccupancyManager#reallocateApartment(java.lang.String, java.lang.String, com.mortbay.iwiki.YyyyMmDd, com.mortbay.iwiki.YyyyMmDd)
     */
    public void reallocateApartment(ReservationData res, String aptId, YyyyMmDd startDate, YyyyMmDd endDate) 
    throws Exception
    {
       if (res == null)
           throw new IllegalArgumentException ("Reservation cannot be null");
              
     
       synchronized (occupancyMatrix)
       {
           deallocateApartment(res);
           try
           {
               allocateApartment(res, aptId, startDate, endDate);
           }
           catch (Exception e)
           {
               //put things back the way they were
               allocateApartment(res, res.getAptId(), res.getStartDate(), res.getEndDate());
               throw e;
           }
       } 
    }
}

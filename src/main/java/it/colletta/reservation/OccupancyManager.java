/*
 * Created on Mar 30, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package it.colletta.reservation;


import java.util.Set;

import com.mortbay.iwiki.YyyyMmDd;

/**
 * @author janb
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface OccupancyManager
{
    public void initialize(ReservationData[] reservations)
    throws Exception;
    

    /**
     * Allocate an apartment to a reservation.
     * First checks to see if the apartment is free during the period of the reservation.
     * @param res
     * @param aptId
     * @throws Exception
     */
    public void allocateApartment (ReservationData res, String aptId, YyyyMmDd startDate, YyyyMmDd endDate)
    throws Exception;
    
    
    /**
     * Free up an apartment reservation
     * @param res
     * @throws Exception
     */
    public void deallocateApartment (ReservationData res)
    throws Exception;
    
    
    /**
     * Deallocate and reallocate an apartment/dates in one shot.
     * @param res is the reservation
     * @param aptId is the new apartment 
     * @param startDate is the new booking start date
     * @param endDate is the new booking end date
     * @throws Exception
     */
    public void reallocateApartment (ReservationData res, String aptId, YyyyMmDd startDate, YyyyMmDd endDate)
    throws Exception;
    
    /**
     * Check if apartment is available for the whole of a given period.
     * NOTE: from is inclusive, to is not (this is checkout day)
     * @param aptId
     * @param from
     * @param to
     * @return true if available for whole period, false otherwise
     * @throws Exception
     */
    public boolean isApartmentAvailable (String aptId, YyyyMmDd from, YyyyMmDd to)
    throws Exception;
    
    /**
     * Find all apartments that participate in bookings between the given dates.
     * NOTE: the from date is inclusive, but the to date is not (because
     * this is the checkout day).
     * @param from
     * @param to
     * @return set of apartment ids
     * @throws Exception
     */
    public Set findAllocatedApartments (YyyyMmDd from, YyyyMmDd to)
    throws Exception;
    
    
    /**
     * Find all the reservations(in any state) between the given dates.
     * NOTE: from is inclusive and to is exclusive
     * @param from
     * @param to
     * @return set of reservation ids
     * @throws Exception
     */
    public Set findReservations (YyyyMmDd from, YyyyMmDd to)
    throws Exception;
    
    
    /**
     * Get one month's worth of reservations for a particular apartment
     * @param aptId
     * @param yyyymm
     * @return array of 0-30 reservation ids, null 
     * element where there is no reservation for that day
     * @throws Exception
     */
    public String[] findReservations (String aptId, int yyyymm)
    throws Exception;
    
    /**
     * Find all reservations in a particular state between the given dates.
     * NOTE: from is inclusive and to is exclusive
     * @param status
     * @param from
     * @param to
     * @return set of reservation ids
     * @throws Exception
     */
    public Set findReservations (ReservationStatus status, YyyyMmDd from, YyyyMmDd to)
    throws Exception;
}

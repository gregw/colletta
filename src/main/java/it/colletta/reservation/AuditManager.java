package it.colletta.reservation;


/**
 * ReservationAuditManager.java
 *
 *
 * Created: Sun Mar 28 17:16:21 2004
 *
 * @author <a href="mailto:janb@wafer">Jan Bartel</a>
 * @version 1.0
 */

public interface AuditManager 
{
    public void record (Object o, String comments);

}

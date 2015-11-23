/*
 * Created on Mar 29, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package it.colletta.reservation;

/**
 * @author janb
 *
 */
public interface ReservationDAO
{
    public void save (ReservationData rd)
    throws Exception;
    
    public ReservationData load (String resId)
    throws Exception;
    
    public ReservationData[] loadAll ()
    throws Exception;
}

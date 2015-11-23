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
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ReservationDAOFactory
{
    private static ReservationDAO dao = null;
    
    
    protected static synchronized void setReservationDAO (ReservationDAO d)
    {
        dao = d;
    }
    
    public static synchronized ReservationDAO getReservationDAO ()
    throws Exception
    {
        if (dao == null)
            dao = new FileReservationDAO();
        
        return dao;
    }
}

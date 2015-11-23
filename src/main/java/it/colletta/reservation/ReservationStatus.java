package it.colletta.reservation;


import java.io.Serializable;
import java.util.HashMap;

/**
 * ReservationStatus.java
 *
 *
 * Created: Sat Mar 27 21:19:47 2004
 *
 * @author <a href="mailto:janb@wafer">Jan Bartel</a>
 * @version 1.0
 */
public class ReservationStatus implements Serializable
{
    private static final HashMap enumMap = new HashMap();
    
    public static final int REQUESTED_INT = 0;
    public static final int APPROVED_INT = 10;
    public static final int CONFIRMED_INT = 20;
    public static final int CANCELLED_INT = 30;
    public static final int EXPIRED_INT = 40;
    public static final int CHECKIN_INT = 50;
    public static final int CHECKOUT_INT = 60;
    
    public static final ReservationStatus REQUESTED = new ReservationStatus (new Integer(REQUESTED_INT),"REQUESTED");
    public static final ReservationStatus APPROVED = new ReservationStatus (new Integer(APPROVED_INT),"APPROVED");
    public static final ReservationStatus CONFIRMED = new ReservationStatus (new Integer(CONFIRMED_INT),"CONFIRMED");
    public static final ReservationStatus CANCELLED = new ReservationStatus (new Integer(CANCELLED_INT),"CANCELLED");
    public static final ReservationStatus EXPIRED   = new ReservationStatus (new Integer(EXPIRED_INT),"EXPIRED");
    public static final ReservationStatus CHECKIN = new ReservationStatus (new Integer(CHECKIN_INT),"CHECKIN");
    public static final ReservationStatus CHECKOUT = new ReservationStatus (new Integer(CHECKOUT_INT),"CHECKOUT");


    private Integer statusCode;
    private String name;

    private ReservationStatus (Integer i,String name)
    {
        statusCode = i;
        enumMap.put (i, this);
        this.name=name;
    }

    public Integer getCode ()
    {
        return statusCode;
    }

    public int getIntCode ()
    {
        return statusCode.intValue();
    }

    public static ReservationStatus getByCode (Integer i)
    {
        return (ReservationStatus)enumMap.get (i);
    }

    public static ReservationStatus getByIntCode (int i)
    {
        return (ReservationStatus)enumMap.get (new Integer(i));
    }

    public boolean equals (Object o)
    {
        if (o instanceof ReservationStatus)
        {
            return (((ReservationStatus)o).getIntCode() == statusCode.intValue());
        }

        return false;
    }
    
    public String toString()
    {
        return name;
    }
    
    public int hashCode()
    {
        return statusCode.intValue();
    }

} // ReservationStatus

/*
 * Created on Apr 29, 2004
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
public interface NotificationManager
{
    public static interface Type
     {
         public static String TOTAL_DUE = "TOTAL-DUE"; //final payment due
         public static String CREATED = "CREATED"; //res. request created
         public static String EXPIRED = "EXPIRED"; //res. has expired (no deposit with xhrs)
         public static String CANCELLED = "CANCELLED"; //res. has been cancelled
         public static String APPROVED = "APPROVED";//res. +apt OK
         public static String UPDATE = "UPDATE"; //could be anything about the res. that changed
         public static String NOVACANCY = "NO-VACANCY"; //no vacancy for date/apt combo
         public static String CONFIRMATION = "CONFIRM";//reservation confirmed manually or on deposit
         public static String AUTO_APPROVED = "CREATED-APPROVED";//auto approved on creation
         public static String AUTO_APPROVED_TOTAL="CREATED-APPROVED-TOTAL";//auto approved, less than 4wks to booking
         public static String APPROVED_TOTAL="APPROVED-TOTAL";//approved
         public static String EXPIRED_CONFIRMED="EXPIRED-CONFIRMED";
     }
     
     
     public static interface Recipient
     {
         public static String CLIENT = "CLIENT";
         public static String MANAGER = "MANAGER";
         public static String COLLETTA = "COLLETTA";
         public static String OWNER = "OWNER";
     }
     
     public void notify (String notificationType, ReservationData rd);
     
}

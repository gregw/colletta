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
public class NotificationManagerFactory
{
    private static NotificationManager mgr = null;
    
    public static synchronized NotificationManager getNotificationManager ()
    {
        if (mgr == null)
        {
            mgr = new EmailNotificationManager();
        }
        return mgr;
    }
    
    protected static synchronized void setNotificationManager (NotificationManager m)
    {
        mgr = m;
    }
}

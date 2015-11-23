package it.colletta.reservation;




/**
 * AuditManagerFactory.java
 *
 *
 * Created: Sun Mar 28 17:17:24 2004
 *
 * @author <a href="mailto:janb@wafer">Jan Bartel</a>
 * @version 1.0
 */
public class AuditManagerFactory 
{

    private static AuditManager manager = null;
   
    public synchronized static AuditManager getAuditManager ()
    {
        if (manager == null)
            manager = new DefaultAuditManager();
        
        return manager;
    }
    
    protected synchronized static void setAuditManager (AuditManager am)
    {
        manager = am;
    }
    
} // AuditManagerFactory

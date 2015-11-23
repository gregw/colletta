/*
 * Created on Mar 30, 2004
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
public class OccupancyManagerFactory
{
    private static OccupancyManager occupancyManager;
    
    
    
    public static synchronized OccupancyManager getOccupancyManager()
    {
        if (occupancyManager == null)
            occupancyManager = new MemoryOccupancyManager();
        
        return occupancyManager;
    }
    
    
    
    protected static void setOccupancyManager (OccupancyManager m)
    {
        occupancyManager = m;
    }
    
}

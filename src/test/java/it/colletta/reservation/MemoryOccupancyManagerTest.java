/*
 * Created on Mar 30, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package it.colletta.reservation;


import java.util.HashMap;
import java.util.Set;

import com.mortbay.iwiki.YyyyMmDd;


/**
 * @author janb
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class MemoryOccupancyManagerTest extends TestCaseFixture
{
 
    
    public void testInitializeNoAptsAllocated ()
    throws Exception
    {       
        reservations = new ReservationData[] {res1, res2};
        
        MemoryOccupancyManager occupancyManager = new MemoryOccupancyManager();
        occupancyManager.initialize(reservations);
        
        assertTrue (occupancyManager.occupancyMatrix.isEmpty());
               
    }
    
    public void testOneAptAllocated ()
    throws Exception
    {
        reservations = new ReservationData[] {res1, res2};
        res1.setAptId("apt1");
        res1.setStatus(ReservationStatus.APPROVED);
        MemoryOccupancyManager occupancyManager = new MemoryOccupancyManager();
        
        assertTrue (res1.getStartDate().before(res1.getEndDate()));        
        occupancyManager.initialize(reservations);   
        assertEquals(1, occupancyManager.occupancyMatrix.size());   
        HashMap[][] dayMap = (HashMap[][])occupancyManager.occupancyMatrix.get(new Integer(2004));
        assertNotNull(dayMap);
        
        //bookings should be for start date thru to the day before the end date
        checkTrue (dayMap[10][4], "apt1", res1);
        checkTrue (dayMap[10][5], "apt1", res1);
        checkNull (dayMap[10][6]);
        checkNull (dayMap[10][3]);
        checkNull (dayMap[10][7]);
    }
    
    public void testAptYearBoundary ()
    throws Exception
    {
        reservations = new ReservationData[] {res2};
       
        res2.setAptId("apt2");
        res2.setStatus(ReservationStatus.APPROVED);
        MemoryOccupancyManager occupancyManager = new MemoryOccupancyManager();
        occupancyManager.initialize(reservations);
        
        //2 years: 2004, 2005
        assertEquals(2, occupancyManager.occupancyMatrix.size());
        
        HashMap[][] dayMap = (HashMap[][])occupancyManager.occupancyMatrix.get(new Integer(2004));
        //must be entry for 2004
        assertNotNull (dayMap);
        //check all months other than 12th are empty
        for (int i=1; i<12; i++)
        {
            for (int j=1; j<32; j++)
                checkNull (dayMap[i][j]);
        }
        //check day before booking starts is empty
        checkNull (dayMap[12][24]);
        
        //check period of booking is allocated for 2004
        for (int i=25; i<32; i++)
            checkTrue (dayMap[12][i], "apt2", res2);
        
        //check booking extends into 2005
        dayMap = (HashMap[][])occupancyManager.occupancyMatrix.get(new Integer(2005));
        assertNotNull(dayMap);
        //check that the booking extends into January
        for (int i=1; i<5; i++)
            checkTrue (dayMap[1][i], "apt2", res2);
        checkNull (dayMap[1][6]);
        checkNull (dayMap[1][7]);
        
        //check rest of year is unallocated
        for (int i=2; i<=12; i++)
        {
            for (int j=1; j<32; j++)
                checkNull (dayMap[i][j]);
        }
        
        
    }
    
    public void testMultipleReservationsSameDay ()
    throws Exception
    {
       res2.setAptId("apt2");
       res2.setStatus(ReservationStatus.APPROVED);
       res3.setAptId("apt3");
       res3.setStatus(ReservationStatus.APPROVED);
       reservations = new ReservationData[] {res2, res3};
       
        MemoryOccupancyManager occupancyManager = new MemoryOccupancyManager();
        occupancyManager.initialize(reservations);
        
        HashMap[][] dayMap = (HashMap[][])occupancyManager.occupancyMatrix.get(new Integer(2004));
        assertNotNull(dayMap);
        HashMap resMap = dayMap[12][25];
        assertNotNull(resMap);
        assertEquals(2, resMap.size());
        assertTrue (resMap.containsKey("apt2"));
        assertTrue (resMap.containsKey("apt3"));
        assertEquals (resMap.get("apt2"), res2);
        assertEquals (resMap.get("apt3"), res3);
    }
    
    
    
    public void testAllocatedApartments ()
    throws Exception
    {
        res1.setAptId("apt1");
        res1.setStatus(ReservationStatus.APPROVED);
        res2.setAptId("apt2");
        res2.setStatus(ReservationStatus.APPROVED);
        res3.setAptId("apt3");
        res3.setStatus(ReservationStatus.APPROVED);
        reservations = new ReservationData[] {res1, res2, res3};
        
        MemoryOccupancyManager occupancyManager = new MemoryOccupancyManager();
        occupancyManager.initialize(reservations);
        
        Set apts = occupancyManager.findAllocatedApartments(new YyyyMmDd(2004, 12, 1), new YyyyMmDd(2006, 10, 12));
        assertEquals(2, apts.size());
        assertTrue (apts.contains("apt2"));
        assertTrue (apts.contains("apt3"));
    }
    
    
    
    public void testAllocateApartment ()
    throws Exception
    {
        reservations = new ReservationData[] {res1};
        res1.setAptId("apt1");
        MemoryOccupancyManager occupancyManager = new MemoryOccupancyManager();
        
        occupancyManager.initialize(reservations);
        
        
        occupancyManager.allocateApartment(res3, "apt1", res3.getStartDate(), res3.getEndDate());
        HashMap[][] dayMap = (HashMap[][])occupancyManager.occupancyMatrix.get(new Integer(2004));
        assertNotNull(dayMap);
        
        for (int j=10; j<26; j++)
        {
            checkTrue (dayMap[12][j], "apt1", res3);
        }
               
        try
        {
            occupancyManager.allocateApartment (res4, "apt1", res4.getStartDate(), res4.getEndDate());
            fail("Expected exception for apt1 in use");
        }
        catch (IllegalStateException e)
        {
            
        }
    }
    
    
    public void testIsApartmentAvailable ()
    throws Exception
    {
        res1.setAptId("apt1");
        res1.setStatus(ReservationStatus.APPROVED);
        reservations = new ReservationData[] {res1};
        
        MemoryOccupancyManager occupancyManager = new MemoryOccupancyManager();
        occupancyManager.initialize(reservations);     
        
        assertTrue (occupancyManager.isApartmentAvailable ("apt1", new YyyyMmDd(2004, 10, 10), new YyyyMmDd(2004, 10, 20)));
        assertFalse(occupancyManager.isApartmentAvailable ("apt1", new YyyyMmDd(2004, 10, 4), new YyyyMmDd(2004,10,5)));
        assertTrue (occupancyManager.isApartmentAvailable ("apt1", new YyyyMmDd(2004, 10, 6), new YyyyMmDd(2004,10,10)));
    }
    
    
    public void testFindReservations ()
    throws Exception
    {
        res4.setAptId("apt4");
        res4.setStatus(ReservationStatus.APPROVED);
        res5.setAptId("apt4");
        res5.setStatus(ReservationStatus.APPROVED);
        reservations = new ReservationData[] {res4,res5};
        MemoryOccupancyManager occupancyManager = new MemoryOccupancyManager();
        occupancyManager.initialize(reservations);  
        
        String[] ids = occupancyManager.findReservations("apt4", 200412);
        
        assertNotNull(ids);
        assertEquals(32, ids.length);
        for (int i=0; i<32; i++)
        {
            if ((i >= 10) && (i <= 14))
            {
                assertNotNull (ids[i]);
                assertEquals( "4", ids[i]);
            }
            else if ((i >= 15) && (i <=20))
            {
                assertNotNull (ids[i]);
                assertEquals ("5", ids[i]);
            }
            else
                assertNull (ids[i]);
        }
        
    }
    
    
    public void testDeallocateApartment ()
    throws Exception
    {
        res1.setAptId("apt1");
        res1.setStatus(ReservationStatus.APPROVED);
        res2.setAptId("apt2");
        res2.setStatus(ReservationStatus.APPROVED);
        reservations = new ReservationData[] {res1,res2};
        MemoryOccupancyManager occupancyManager = new MemoryOccupancyManager();
        occupancyManager.initialize(reservations);  
        
        occupancyManager.deallocateApartment(res2);
        
        for (YyyyMmDd date = new YyyyMmDd(2004, 12, 25); date.before( new YyyyMmDd(2005, 1, 5)); date.addDays(1))
        {
            HashMap[][] map = (HashMap[][])occupancyManager.occupancyMatrix.get(new Integer(date.getYyyy()));
            HashMap resmap = map[date.getMm()][date.getDd()];
            assertFalse(resmap.containsKey("apt2"));
        }
    }
    
    
    private void checkTrue (HashMap aptMap, String aptId, ReservationData res)
    {
        assertNotNull(aptMap);
        assertTrue(aptMap.containsKey(aptId));
        assertEquals(aptMap.get(aptId), res);
    }
    
    private void checkNull (HashMap aptMap)
    {
        assertNull (aptMap);
    }
    
    private void checkNotContains (HashMap aptMap, String aptId)
    {
        assertFalse (aptMap.containsKey(aptId));
    }
}

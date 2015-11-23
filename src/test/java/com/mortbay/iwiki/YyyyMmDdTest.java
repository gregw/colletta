/* ==============================================
 * Copyright 2003 Mort Bay Consulting Pty Ltd. All rights reserved.
 * Distributed under the artistic license.
 * Created on 16/04/2004
 * $Id: YyyyMmDdTest.java,v 1.1 2006/01/21 17:28:01 gregw Exp $
 * ============================================== */
 
package com.mortbay.iwiki;

import java.util.Calendar;

import com.mortbay.iwiki.YyyyMmDd;
import com.mortbay.iwiki.YyyyMmDdHM;

import junit.framework.TestCase;

/* ------------------------------------------------------------------------------- */
/** 
 * 
 * @version $Revision: 1.1 $
 * @author gregw
 */
public class YyyyMmDdTest extends TestCase
{
    /* ------------------------------------------------------------------------------- */
    /** Constructor.
     * 
     */
    public YyyyMmDdTest()
    {
    }
    
    public void testEaster()
    {
        YyyyMmDd[] easter=
        {
                new YyyyMmDd(2004,4,11),
                new YyyyMmDd(2005,3,27),
                new YyyyMmDd(2006,4,16),
                new YyyyMmDd(2007,4,8),
                new YyyyMmDd(2008,3,23),
                new YyyyMmDd(2009,4,12),
                new YyyyMmDd(2010,4,4),
                new YyyyMmDd(2011,4,24),
                new YyyyMmDd(2012,4,8),
                new YyyyMmDd(2013,3,31),
                new YyyyMmDd(2014,4,20),
                new YyyyMmDd(2015,4,5),
                new YyyyMmDd(2016,3,27),
                new YyyyMmDd(2017,4,16),
                new YyyyMmDd(2018,4,1),
                new YyyyMmDd(2019,4,21),
        };
        
        for (int i=2004;i<=2019;i++)
        {
            System.err.println(easter[i-2004]);
            assertEquals(easter[i-2004],YyyyMmDd.easter(i));
        }
        
        try
        {
            YyyyMmDd.easter(2004).setYyyy(2006);
            assertTrue(false);
        }
        catch(IllegalStateException e)
        {
        }
        catch(Exception e)
        {
            assertTrue(false);
        }
        
    }
    
    public void testPrevNextWeekDay()
    {
        YyyyMmDd ymd = new YyyyMmDd(2004,12,24);
        ymd.toNextDayOfWeek(Calendar.FRIDAY);
        assertEquals("2004-12-24",ymd.toString());
        ymd.toPrevDayOfWeek(Calendar.FRIDAY);
        assertEquals("2004-12-24",ymd.toString());
        
        ymd.toPrevDayOfWeek(Calendar.TUESDAY);
        assertEquals("2004-12-21",ymd.toString());
        ymd.toPrevDayOfWeek(Calendar.WEDNESDAY);
        assertEquals("2004-12-15",ymd.toString());
        

        ymd.toNextDayOfWeek(Calendar.FRIDAY);
        assertEquals("2004-12-17",ymd.toString());
        ymd.toNextDayOfWeek(Calendar.SATURDAY);
        assertEquals("2004-12-18",ymd.toString());
        ymd.toNextDayOfWeek(Calendar.FRIDAY);
        assertEquals("2004-12-24",ymd.toString());
        
    }
    

    public void testYyyyMmDdHM()
    {
        YyyyMmDdHM ymdhm0 = new YyyyMmDdHM();
        YyyyMmDdHM ymdhm1 = new YyyyMmDdHM(ymdhm0.toString());

        assertEquals(ymdhm0,ymdhm1);
        assertEquals(ymdhm0.toString(),ymdhm1.toString());
    }
    
}

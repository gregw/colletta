package it.colletta.reservation;
import com.mortbay.iwiki.YyyyMmDd;

import junit.framework.TestCase;

/*
 * Created on Mar 31, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
/**
 * @author janb
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TestCaseFixture extends TestCase
{
    ReservationData[] reservations = null;
    ReservationData res1 = new ReservationData ();
    ReservationData res2 = new ReservationData ();
    ReservationData res3 = new ReservationData ();
    ReservationData res4 = new ReservationData ();
    ReservationData res5 = new ReservationData ();
    
    public void setUp ()
    {   
        
        res1.setId("1");
        YyyyMmDd startDate1 = new YyyyMmDd(2004, 10, 4);  
        YyyyMmDd endDate1 = new YyyyMmDd (2004, 10, 6);
        res1.setStartDate(startDate1);
        res1.setEndDate(endDate1);
        
 
        res2.setId ("2");
        YyyyMmDd startDate2 = new YyyyMmDd(2004, 12, 25);
        YyyyMmDd endDate2 = new YyyyMmDd (2005, 1, 5);
        res2.setStartDate(startDate2);
        res2.setEndDate(endDate2);
        
        res3.setId("3");
        YyyyMmDd startDate3 = new YyyyMmDd(2004, 12, 10);
        YyyyMmDd endDate3 = new YyyyMmDd(2004, 12, 26);
        res3.setStartDate(startDate3);
        res3.setEndDate(endDate3);
        
        res4.setId("4");
        YyyyMmDd startDate4 = new YyyyMmDd(2004, 12, 10);
        YyyyMmDd endDate4 = new YyyyMmDd(2004, 12, 15);
        res4.setStartDate(startDate4);
        res4.setEndDate(endDate4);
        
        res5.setId("5");
        YyyyMmDd startDate5 = new YyyyMmDd(2004, 12, 15);
        YyyyMmDd endDate5 = new YyyyMmDd(2004, 12, 21);
        res5.setStartDate(startDate5);
        res5.setEndDate(endDate5);
        
    }
    
    public void testNothing()
    {
    }
    
    public void tearDown ()
    {
    }
}

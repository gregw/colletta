/* ==============================================
 * Copyright 2003 Mort Bay Consulting Pty Ltd. All rights reserved.
 * Distributed under the artistic license.
 * Created on 3/04/2004
 * $Id: Apartment.java,v 1.23 2006/01/21 17:20:59 gregw Exp $
 * ============================================== */

package it.colletta;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.mortbay.iwiki.Page;
import com.mortbay.iwiki.User;
import com.mortbay.iwiki.YyyyMmDd;

/* ------------------------------------------------------------------------------- */
/**
 * Conveniance wrapper for accessing Page data about Apartments.
 * 
 * @version $Revision: 1.23 $
 * @author gregw
 */
public class Apartment
{
    public final static int LOW = 0,MID=1,MED = 1,PEAK = 2,HIGH = 2;
    public final static int NOCHARGE=0, AIRCON = 1, PEAK_HEAT = 2, HEAT = 3;
    public final static String[] seasonId={"L","M","H"};
    private static HashMap map = new HashMap();
    private static String[] aptIds;
    private static Page __root;
   

    public static void setRoot(Page root)
    {
        __root=root;
        Page view = root.getPageByPath("/renting/view/");
        Page[] apartments = view.getChildren();
        aptIds = new String[apartments.length];
        synchronized (map)
        {
            map.clear();
            for (int i = 0; i < apartments.length; i++)
            {
                aptIds[i] = apartments[i].getDirName();
                map.put(apartments[i].getDirName(), new Apartment(apartments[i]));
            }
        }
    }
    
    public static Page getRoot()
    {
        return __root;
    }
    

    public static Apartment getApartment(String name)
    {
        synchronized (map)
        {
            return (Apartment) map.get(name);
        }
    }

    public static String[] getApartmentIds()
    {
        return aptIds;
    }

    private Page page;
    private User[] owners;

    public Apartment()
    {
        throw new UnsupportedOperationException();
    }

    private Apartment(Page page)
    {
        this.page = page;
    }
    
    public String getName()
    {
        return page.getDirName();
    }
    
    public String getDisplayName()
    {
        return page.getDisplayName();
    }

    public String getName(String lang)
    {
        return page.getName(lang);
    }

    public Page getPage()
    {
        return page;
    }


    /* ------------------------------------------------------------------------------- */
    /** Get a value from the properties.
     * If the value is a percentage, it is applied to the dft value.
     */
    private int getValue(int dft, String n)
    {
        Integer cached=(Integer)page.cache().get(n);
        
        if (cached!=null)
            return cached.intValue();
        
        int value=dft;
        
        String pc=page.getPathProperty(null,n);
        if (pc!=null)
        {
            if (pc.endsWith("%"))
                value =  (49 + dft * (Integer.parseInt(pc.substring(0,pc.length()-1)))) / 100;
            else 
                value = Integer.parseInt(pc);
        }
        
        page.cache().put(n,new Integer(value));
        
        return value;
    }
    
    /* ------------------------------------------------------------------------------- */
    /**
     * getManager.
     * 
     * @return
     */
    public String getManager()
    {
        return page.getProperty("A_manager");
    }
    
    public User getManagerUser()
    {
        User m = User.getUser(getManager());
        if (m==null)
            m=User.getDefaultManager();
        return m;
    }
    
    public User[] getOwners()
    {
        if (owners==null)
        {
            List list = new ArrayList();
            User[] users = User.getUsers();
            for (int i=0;i<users.length;i++)
                if (users[i].owns(getName()))
                    list.add(users[i]);
            owners = (User[])list.toArray(new User[list.size()]);
        }
        return owners;
    }
    
    
    public boolean isApproval()
    {
        return "1".equals(page.getPathProperty(null,"A_approval"));
    }

    public boolean isAirConditioned ()
    {
        return "1".equals(page.getPathProperty(null, "A_aircond"));
    }
    
    public int getListLow7()
    {
        return getValue(getRentPeak7(),"A_rent_low");
    }
    
    public int getListPeak7()
    {
        return getValue(getRentPeak7(),"A_rent");
    }
    
    public int getRentLow7()
    {
        return getValue(getRentPeak7(),"A_rent_low");
    }

    public int getRentMid7()
    {
        return getValue(getRentPeak7(),"A_rent_mid");
    }

    public int getRentPeak7()
    {
        return getValue(700,"A_rent");
    }
    
    public int getRentLow3()
    {
        int rent7=getRentLow7();
        return (rent7*getValue(100,"A_nights_1_2")*2/7+rent7*100/7)/100;
    }

    public int getRentMid3()
    {
        int rent7=getRentMid7();
        return (rent7*getValue(100,"A_nights_1_2")*2/7+rent7*100/7)/100;
    }

    public int getRentPeak3()
    {
        int rent7=getRentPeak7();
        return (rent7*getValue(100,"A_nights_1_2")*2/7+rent7*100/7)/100;
    }

    public int getRentLow1()
    {
        int rent7=getRentLow7();
        return rent7*getValue(100,"A_nights_1_2")/700;
    }

    public int getRentMid1()
    {
        int rent7=getRentMid7();
        return rent7*getValue(100,"A_nights_1_2")/700;
    }

    public int getRentPeak1()
    {
        int rent7=getRentPeak7();
        return rent7*getValue(100,"A_nights_1_2")/700;
    }
    
    public int getDeposit()
    {
        return getValue(100,"A_deposit");
    }
    
    public int getMinStay()
    {
        return getValue(1,"A_minStay");
    }
    
    public int getMaxOccupancy()
    {
        return getValue(1,"A_sleeps_max");
    }
    
    public int calculatePowerChargeInCents (YyyyMmDd day, int dayInStay, int totalDays)
    {
        int utilities = getValue(0, "A_utilities");
        if (utilities>0)
            return 100*utilities;
        
        int seasonOfDay = getPowerConsumptionSeason(day);
        
        int surcharge;
        
        switch (seasonOfDay)
        {
            case NOCHARGE:
            {
                surcharge=0;
                break;
            }
            case HEAT:
            {
                int heat_surcharge = getValue(0, "A_heat");
                surcharge=heat_surcharge * 100;
                break;
            }
            case PEAK_HEAT:
            {
                int peakheat_surcharge = getValue(0, "A_peak_heat");
                surcharge=peakheat_surcharge * 100;
                break;
            }
            case AIRCON:
            {
                int aircon_surcharge = getValue(0, "A_aircon");
                if (isAirConditioned())
                    surcharge=aircon_surcharge * 100;
                else
                    surcharge=0;
                break;
            }
            default:
            {
                surcharge=0;
            }
        }
        
        return surcharge;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param day
     * @param dayInStay The day within the stay, starting from 0.
     * @param totalDays Total days (actually nights) of the stay.
     * @param rentbasis StringBuffer to add to for display of the rental basis.
     * @return
     */
    public int calculateRentInCents(YyyyMmDd day, int dayInStay, int totalDays, StringBuffer rentbasis)
    {
        int season=getSeason(day);
        int rent=getRentPeak7();
        if (season==MED)
            rent=getRentMid7();
        else if (season==LOW)
            rent=getRentLow7();
                
        if (rentbasis!=null) rentbasis.append(seasonId[season]);
        
        // Discount for long stays
        if (totalDays>14 && dayInStay>=14)
        {
            if (dayInStay>=28)
            {
                if (rentbasis!=null) rentbasis.append("--");
                rent=rent*getValue(100,"A_nights_29_")/7;
            }
            else
            {
                if (rentbasis!=null) rentbasis.append("-");
                rent=rent*getValue(100,"A_nights_15_28")/7;
            }
        }
        else if (dayInStay<2)
        {
            rent=rent*getValue(100,"A_nights_1_2")/7;
        }
        else if (dayInStay==5 || dayInStay==6)
        {
            rent=rent*getValue(100,"A_nights_6_7")/7;
        }
        else
        {
            rent=rent*100/7;
        }
        
        return rent;
    }
    
    
    
    /**
     * Heating charges apply:
     * PEAK_HEAT charge: December, January, February, first week March
     * HEAT charge:      rest of March, April, second week September, October, November
     * AIRCON charge:    May, June, July, August, first week September 
     * @param day
     * @return
     */
    public static int getPowerConsumptionSeason (YyyyMmDd day)
    {
        switch (day.getMm())
        {
            case 1:
            {
                //JANUARY is PEAK_HEAT
                return PEAK_HEAT;
            }
            case 2:
            {
                //FEBRUARY is PEAK_HEAT
                return PEAK_HEAT;
            }
            case 3:
            {
                //MARCH all days up to end of first full week is PEAK_HEAT, then HEAT

                
                // work out first full week
                YyyyMmDd d = new YyyyMmDd(day);
                //goto the first day of the month
                d.setDd(1);
                // look for the first saturday
                d.toNextDayOfWeek(Calendar.SATURDAY);
                // go to the first next friday
                d.addDays(6);
                
                int dd = day.getDd();
                
                if (dd<=d.getDd())
                    return PEAK_HEAT;
                return HEAT;
            }
            case 4:
            {
                //APRIL is HEAT
                return HEAT;
            }
            case 5:
            {
                //MAY is AIRCON
                return AIRCON;
            }
            case 6:
            {
                //JUNE is AIRCON
                return AIRCON;
            }
            case 7:
            {
                //JULY is AIRCON
                return AIRCON;
            }
            case 8:
            {
                //AUGUST is AIRCON
                return AIRCON;
            }
            case 9:
            {
                //SEPTEMBER first week is AIRCON, then HEAT
                //see comment for MARCH
 
                // work out first full week
                YyyyMmDd d = new YyyyMmDd(day);
                //goto first day of month
                d.setDd(1);
                // look for first saturday
                d.toNextDayOfWeek(Calendar.SATURDAY);
                // go to the next friday
                d.addDays(6);
                
                int dd = day.getDd();
                if (dd<=d.getDd())
                    return AIRCON;
                return HEAT;
            }
            case 10:
            {
                //OCTOBER is HEAT
                return HEAT;
            }
            case 11:
            {
                //NOVEMBER is HEAT
                return HEAT;
            }
            case 12:
            {
                //DECEMBER is PEAK_HEAT
                return PEAK_HEAT;
            }
            default:
            {
                return NOCHARGE;
            }
        }
    }
    
    
    public static int getSeason(YyyyMmDd day)
    {
        // Low Season:  from the 01-01 till the 14-05, 16-09 till 30-12
        // Mid Season:  from the 03-04-04 till the 09-04-04, from the 17-04-04 till the 11-06-04,
        //              from the 11-09-04 till the 15-10-04
        // High Season: from the 01-01-04 till the 09-01-04, from the 10-04-04 till the 16-04-04,
        //              from the 12-06-04 till the 10-09-04, from the 18-12-04 till the 07-01-05

        int dd = day.getDd();
        YyyyMmDd d; // temp date
        int td; // temp days
        
        switch(day.getMm())
        {
        case 1:
            if (dd <= 7)
              return PEAK;

            return LOW;
            
        case 2:
            return LOW;
            
        case 3:
            
            // easter check!
            /*
            td = day.daysTo(YyyyMmDd.easter(day.getYyyy()));
            if (td>=-5 && td<=8)
                return PEAK;
            */
       
            return LOW;
            
        case 4:
            // easter check!
            /*
            td = day.daysTo(YyyyMmDd.easter(day.getYyyy()));
            if (td>=-5 && td<=8)
                return PEAK;
            */
            
            return LOW;
            
        case 5:
            if (dd>=15)
                return PEAK;
            
            return LOW;
            
        case 6:
            return PEAK;
            
        case 7:
            return PEAK;
        case 8:
            return PEAK;
        case 9:
            if (dd<=15)
                return PEAK;
            return LOW;
            
        case 10:
            return LOW;
        case 11:
            return LOW;
            
        case 12:
            if (dd >= 23)
              return PEAK;
            return LOW;
        }
        
        return LOW;
        		
    }
    
    
    
}

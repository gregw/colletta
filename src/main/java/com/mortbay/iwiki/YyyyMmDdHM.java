/* ==============================================
 * Copyright 2003 Mort Bay Consulting Pty Ltd. All rights reserved.
 * Distributed under the artistic license.
 * Created on 28/03/2004
 * $Id: YyyyMmDdHM.java,v 1.1 2006/01/21 17:28:01 gregw Exp $
 * ============================================== */
 
package com.mortbay.iwiki;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

/* ------------------------------------------------------------------------------- */
/** 
 * 
 * @version $Revision: 1.1 $
 * @author gregw
 */
public class YyyyMmDdHM implements Cloneable, Comparable
{ 
    private static HashMap symbols = new HashMap();
    
    public static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static DateFormatSymbols dfs(Locale locale)
    {
        DateFormatSymbols dfs = (DateFormatSymbols)symbols.get(locale);
        if (dfs==null)
        {
            dfs=new DateFormatSymbols(locale);
            symbols.put(locale, dfs);
        }
        return dfs;
    }
    
    public static String month(int mm, Locale locale)
    {
        return dfs(locale).getMonths()[mm-1];
    }
    
    public static String shortMonth(int mm, Locale locale)
    {
        return dfs(locale).getShortMonths()[mm-1];
    }
    
    public static String shortWeekday(int wd, Locale locale)
    {
        return dfs(locale).getShortWeekdays()[wd==0?7:wd];
    }

    public static String weekday(int wd, Locale locale)
    {
        return dfs(locale).getWeekdays()[wd==0?7:wd];
    }
    
    
    public static int normalizeYyyyMm(int yyyymm)
    {
        int y=yyyymm/100;
        int m=yyyymm%100;
        
        if (m>12 && m<24)
        {
            y++;
            m=m-12;
        }
        if (m==0 || m>88 && m<100)
        {
            y--;
            m=(m+12)%100;
        }
        
        return y*100+m;
    }
    
    private GregorianCalendar calendar;
    private boolean readonly;
    private boolean dirty=false;
    
    public YyyyMmDdHM()
    {
        calendar = new GregorianCalendar(UTC);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
    }
    
    public YyyyMmDdHM(long msFromEpoch)
    {
        calendar = new GregorianCalendar(UTC);
        calendar.setTimeInMillis(msFromEpoch);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
    }

    public YyyyMmDdHM(String ymdhm)
    {
        this();
        String[] dt  = ymdhm.split("\\s",2);
        String[] ymd = dt[0].split("-",3);
        String[] hm  = dt[1].split(":",2);
        
        setYyyy(Integer.parseInt(ymd[0]));
        setMm(Integer.parseInt(ymd[1]));
        setDd(Integer.parseInt(ymd[2]));

        setH(Integer.parseInt(hm[0]));
        setM(Integer.parseInt(hm[1]));
    }
    
    public YyyyMmDdHM(int yyyy,int mm, int dd)
    {
        calendar = new GregorianCalendar(UTC);
        calendar.setTimeInMillis(0);
        calendar.set(Calendar.YEAR, yyyy);
        calendar.set(Calendar.MONTH, mm-1);
        calendar.set(Calendar.DAY_OF_MONTH, dd);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
    }
    
    public YyyyMmDdHM(int yyyy,int mm, int dd, int h, int m)
    {
        calendar = new GregorianCalendar(UTC);
        calendar.setTimeInMillis(0);
        calendar.set(Calendar.YEAR, yyyy);
        calendar.set(Calendar.MONTH, mm-1);
        calendar.set(Calendar.DAY_OF_MONTH, dd);
        calendar.set(Calendar.HOUR_OF_DAY, h);
        calendar.set(Calendar.MINUTE, m);
    }
    
    public YyyyMmDdHM(int yyyy,int mm, int dd, boolean readonly)
    {
        this(yyyy,mm,dd);
        this.readonly=readonly;
    }
    
    public YyyyMmDdHM(int yyyy,int mm, int dd , int h, int m,boolean readonly)
    {
        this(yyyy,mm,dd,h,m);
        this.readonly=readonly;
    }
    
    public YyyyMmDdHM(YyyyMmDdHM ymdhm)
    {
        calendar = new GregorianCalendar(UTC);
        calendar.setTimeInMillis(0);
        calendar.set(Calendar.YEAR, ymdhm.getYyyy());
        calendar.set(Calendar.MONTH, ymdhm.getMm()-1);
        calendar.set(Calendar.DAY_OF_MONTH, ymdhm.getDd());
        calendar.set(Calendar.HOUR_OF_DAY, ymdhm.getH());
        calendar.set(Calendar.MINUTE, ymdhm.getM());
    }

    public YyyyMmDdHM(YyyyMmDdHM ymdhm, boolean readonly)
    {
        this(ymdhm);
        this.readonly=readonly;
    }

    /* ------------------------------------------------------------------------------- */
    public void addDays(int days)
    {
        if (readonly) throw new IllegalStateException("Readonly");
        calendar.add(Calendar.DAY_OF_MONTH, days);
    }

    /* ------------------------------------------------------------------------------- */
    public boolean after(YyyyMmDdHM ymdhm)
    {
        return calendar.getTimeInMillis()>ymdhm.calendar.getTimeInMillis();
    }

    /* ------------------------------------------------------------------------------- */
    public boolean before(YyyyMmDdHM ymdhm)
    {
        return calendar.getTimeInMillis()<ymdhm.calendar.getTimeInMillis();
    }

    /* ------------------------------------------------------------------------------- */
    public int daysTo(YyyyMmDdHM ymdhm)
    {
        return (int) ((ymdhm.calendar.getTimeInMillis()-calendar.getTimeInMillis())/(1000*60*60*24));
    }
    
    /* ------------------------------------------------------------------------------- */
    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        return new YyyyMmDdHM(this);
    }
    
    
    /* ------------------------------------------------------------------------------- */
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object to)
    {
        YyyyMmDdHM ymdhm=(YyyyMmDdHM)to;
        long t0=calendar.getTimeInMillis();
        long t1=ymdhm.calendar.getTimeInMillis();
        if (t0==t1)
            return 0;
        if (t0<t1)
            return -1;
        return 1;
    }
    
    /* ------------------------------------------------------------------------------- */
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o)
    {
        if (!(o instanceof YyyyMmDdHM))
                return false;
        return calendar.getTimeInMillis()==((YyyyMmDdHM)o).calendar.getTimeInMillis();
    }

    /* ------------------------------------------------------------------------------- */
    public Calendar getCalendar()
    {
        return calendar;
    }
    
    /* ------------------------------------------------------------------------------- */
    public long getTimeInMillis()
    {
        return calendar.getTimeInMillis();
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * @return 0-7. 0==Saturday
     */
    public int getDayOfWeek()
    {
        int d=calendar.get(Calendar.DAY_OF_WEEK);
        return d==7?0:d;
    }

   
    /* ------------------------------------------------------------------------------- */ 
    /**
     * @return Returns the ordinal number of the week within the month:
     */
    public int getWeekOfMonth()
    {
        return calendar.get(Calendar.WEEK_OF_MONTH);
    }
    
    /* ------------------------------------------------------------------------------- */ 
    /**
     * @return Returns the dd.
     */
    public int getDd()
    {
        if (dirty)
            calendar.getTime();
        dirty=false;
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    /* ------------------------------------------------------------------------------- */
    public int getMaxDd()
    {
        if (dirty)
            calendar.getTime();
        dirty=false;
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * @return Returns the mm.
     */
    public int getMm()
    {
        if (dirty)
            calendar.getTime();
        dirty=false;
        return calendar.get(Calendar.MONTH)+1;
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * @return Returns the yyyy.
     */
    public int getYyyy()
    {
        if (dirty)
            calendar.getTime();
        dirty=false;
        return calendar.get(Calendar.YEAR);
    }
    
    /* ------------------------------------------------------------------------------- */
    /**
     * @return Returns the yyyy.
     */
    public YyyyMmDd getYyyyMmDd()
    {
        if (dirty)
            calendar.getTime();
        dirty=false;
        return new YyyyMmDd(calendar.getTimeInMillis());
    }

    /* ------------------------------------------------------------------------------- */ 
    /**
     * @return Returns the dd.
     */
    public int getH()
    {
        if (dirty)
            calendar.getTime();
        dirty=false;
        return calendar.get(Calendar.HOUR_OF_DAY);
    }
    
    /* ------------------------------------------------------------------------------- */ 
    /**
     * @return Returns the dd.
     */
    public int getM()
    {
        if (dirty)
            calendar.getTime();
        dirty=false;
        return calendar.get(Calendar.MINUTE);
    }
    
    /* ------------------------------------------------------------------------------- */
    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return calendar.hashCode();
    }

    
    /* ------------------------------------------------------------------------------- */
    /**
     * @param yyyymm The yyyymm to set.
     */   
    public void setYyyymm(int yyyymm)
    {
        yyyymm=normalizeYyyyMm(yyyymm);
        setYyyy(yyyymm/100);
        setMm(yyyymm%100);
        setDd(1);
    }  

    /* ------------------------------------------------------------------------------- */
    /**
     * @param yyyymmdd The yyyymm to set.
     */
    public void setYyyymmdd(int yyyymmdd)
    {
        int yyyymm=yyyymmdd/100;
        int dd=yyyymmdd%100;
        yyyymm=normalizeYyyyMm(yyyymm);
        setYyyy(yyyymm/100);
        setMm(yyyymm%100);
        setDd(dd);
    }  
    
    /* ------------------------------------------------------------------------------- */
    /**
     * @param dd The dd to set.
     */
    public void setDd(int dd)
    {
        if (readonly) throw new IllegalStateException("Readonly");
       calendar.set(Calendar.DAY_OF_MONTH,dd);
       dirty=true;
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * @param mm The mm to set.
     */
    public void setMm(int mm)
    {
        if (readonly) throw new IllegalStateException("Readonly");
        calendar.set(Calendar.MONTH,mm-1);
        dirty=true;
    }

    /* ------------------------------------------------------------------------------- */
    public void setToNow()
    {
        if (readonly) throw new IllegalStateException("Readonly");
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * @param yyyy The yyyy to set.
     */
    public void setYyyy(int yyyy)
    {
        if (readonly) throw new IllegalStateException("Readonly");
        calendar.set(Calendar.YEAR,yyyy);
        dirty=true;
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * @param h The hour to set.
     */
    public void setH(int h)
    {
        if (readonly) throw new IllegalStateException("Readonly");
        calendar.set(Calendar.HOUR_OF_DAY,h);
        dirty=true;
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * @param m The minute to set.
     */
    public void setM(int m)
    {
        if (readonly) throw new IllegalStateException("Readonly");
        calendar.set(Calendar.MINUTE,m);
        dirty=true;
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * Goto the next day of the week.  
     * If the current date is that passed day, then no change is made.
     * @param dayOfWeek 0-7, 0==Saturday
     */
    public void toNextDayOfWeek(int dayOfWeek)
    {
        if (readonly) throw new IllegalStateException("Readonly");
        dayOfWeek%=7;
        
        int dow=getDayOfWeek();
        if (dow!=dayOfWeek)
        {
            int days=dayOfWeek-dow;
            if (days<0)
                days+=7;
            addDays(days);
        }
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * Goto the prev day of the week.  
     * If the current date is that passed day, then no change is made.
     * @param dayOfWeek 0-7, 0==Saturday
     */
    public void toPrevDayOfWeek(int dayOfWeek)
    {
        if (readonly) throw new IllegalStateException("Readonly");
        dayOfWeek%=7;
        
        int dow=getDayOfWeek();
        if (dow!=dayOfWeek)
        {
            int days=dayOfWeek-dow;
            if (days>0)
                days=days-7;
            addDays(days);
        }
    }
    
    /* ------------------------------------------------------------------------------- */
    public boolean isBetween(YyyyMmDdHM inclusiveStart, YyyyMmDdHM exclusiveEnd)
    {
        if (!inclusiveStart.after(this) && exclusiveEnd.after(this))
            return true;
        return false;
    }

    /* ------------------------------------------------------------------------------- */
    public static boolean isOverlap(YyyyMmDdHM s1, YyyyMmDdHM e1, YyyyMmDdHM s2, YyyyMmDdHM e2)
    {
        return s1.isBetween(s2,e2) || s2.isBetween(s1,e1);
    }
    
    /* ------------------------------------------------------------------------------- */
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return getYyyy()+
        (getMm()<10?"-0":"-")+getMm()+
        (getDd()<10?"-0":"-")+getDd()+
        (getH()<10?" 0":" ")+getH()+
        (getM()<10?":0":":")+getM();
    }
    
}

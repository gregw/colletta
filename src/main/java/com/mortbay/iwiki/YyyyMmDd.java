/* ==============================================
 * Copyright 2003 Mort Bay Consulting Pty Ltd. All rights reserved.
 * Distributed under the artistic license.
 * Created on 28/03/2004
 * $Id: YyyyMmDd.java,v 1.1 2006/01/21 17:28:01 gregw Exp $
 * ============================================== */
 
package com.mortbay.iwiki;


/* ------------------------------------------------------------------------------- */
/** 
 * 
 * @version $Revision: 1.1 $
 * @author gregw
 */
public class YyyyMmDd extends YyyyMmDdHM
{
    private static YyyyMmDd[] easters = new YyyyMmDd[100];   
    
    public static YyyyMmDd easter(int yyyy)
    {   
        int e=yyyy-2000;
        if (e>=0 && e<easters.length && easters[e]!=null)
            return easters[e];
        
        int century=yyyy/100;
        int g=yyyy%19;
        int k = ((century - 17) / 25);
        int i = (century - (century / 4) - ((century - k) / 3) + 19 * g + 15) % 30;
        i = i - (i/28) * (1 - (i/28) * (29/(i+1)) * ((21-g)/11));
        int j= (yyyy + (yyyy/4) + i + 2 - century + (century/4)) % 7;
        int l = i - j;
            
        int mm = 3 + (l+40)/44;
        int dd = l + 28 - 31 * (mm/4);

        YyyyMmDd ymd=new YyyyMmDd(yyyy,mm,dd,true);
        if (e>=0 && e<easters.length)
            easters[e]=ymd;
        
        return ymd;
    }
  
    
    public YyyyMmDd()
    {
        super();
        setH(0);
        setM(0);
    }
    
    public YyyyMmDd(long msFromEpoch)
    {
	super(msFromEpoch);
        setH(0);
        setM(0);
    }


    public YyyyMmDd(String ymd)
    {
        super(ymd+" 0:0");
    }
    
    
    public YyyyMmDd(int yyyy,int mm, int dd)
    {
        super(yyyy,mm,dd,0,0);
    }
    
    public YyyyMmDd(int yyyy,int mm, int dd, boolean readonly)
    {
        super(yyyy,mm,dd,0,0,readonly);
    }
    
    public YyyyMmDd(YyyyMmDd ymd)
    {
        super(ymd);
    }

    public YyyyMmDd(YyyyMmDd ymd, boolean readonly)
    {
        super(ymd,readonly);
    }
    
    /* ------------------------------------------------------------------------------- */
    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        return new YyyyMmDd(this);
    }
    


    /* ------------------------------------------------------------------------------- */
    /**
     * @return Returns the yyyymm.
     */
    public int getYyyymm()
    {
        return getYyyy()*100+getMm();
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * @return Returns the yyyymmdd.
     */
    public int getYyyymmdd()
    {
        return getYyyy()*10000+getMm()*100+getDd();
    }
    
    /* ------------------------------------------------------------------------------- */
    public void setToNow()
    {
        super.setToNow();
        setH(0);
        setM(0);
    }
    

    
    /* ------------------------------------------------------------------------------- */
    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return getYyyy()+
        (getMm()<10?"-0":"-")+getMm()+
        (getDd()<10?"-0":"-")+getDd();
    }
    
}

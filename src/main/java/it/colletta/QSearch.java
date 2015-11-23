/* ==============================================
 * Copyright 2003 Mort Bay Consulting Pty Ltd. All rights reserved.
 * Distributed under the artistic license.
 * Created on 28/03/2004
 * $Id: QSearch.java,v 1.3 2006/01/21 17:20:59 gregw Exp $
 * ============================================== */
 
package it.colletta;

import com.mortbay.iwiki.YyyyMmDd;

/* ------------------------------------------------------------------------------- */
/** 
 * 
 * @version $Revision: 1.3 $
 * @author gregw
 */
public class QSearch extends YyyyMmDd
{
    private int adults;
    private int children;
    private int infants;
    private int nights;
    private String qsearch;
    
    /**
     * @return Returns the people.
     */
    public int getAdults()
    {
        return adults;
    }
    
    /**
     * @param people The # of people to set.
     */
    public void setAdults(int people)
    {
        this.adults=people;
    }
    
    /**
     * @return Returns the nights.
     */
    public int getNights()
    {
        return nights;
    }
    
    /**
     * @param nights The nights to set.
     */
    public void setNights(int nights)
    {
        this.nights=nights;
    }

    public void setYyyymm(int yyyymm)
    {
        super.setYyyymm(yyyymm);
        nights=0;
    }
    
    /**
     * @return Returns the qsearch.
     */
    public String getQsearch()
    {
        return qsearch;
    }
    
    /**
     * @param qsearch The qsearch to set.
     */
    public void setQsearch(String qsearch)
    {
        if ("off".equals(qsearch))
            this.qsearch=null;
        else
            this.qsearch=qsearch;
    }
    
    public boolean isSearch()
    {
        return qsearch!=null && qsearch.length()>0;
    }
    /**
     * @return Returns the children.
     */
    public int getChildren()
    {
        return children;
    }
    /**
     * @param children The children to set.
     */
    public void setChildren(int children)
    {
        this.children=children;
    }
    /**
     * @return Returns the infants.
     */
    public int getInfants()
    {
        return infants;
    }
    /**
     * @param infants The infants to set.
     */
    public void setInfants(int infants)
    {
        this.infants=infants;
    }
    
    public int getPeople()
    {
        return adults+children+((infants+1)/2);
    }
}

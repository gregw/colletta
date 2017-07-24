/* ==============================================
 * Copyright 2003 Mort Bay Consulting Pty Ltd. All rights reserved.
 * Distributed under the artistic license.
 * Created on 11/03/2004
 * $Id: ReservationData.java,v 1.43 2006/01/23 17:47:33 gregw Exp $
 * ============================================== */

package it.colletta.reservation;


import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;

import com.mortbay.iwiki.StringUtil;
import com.mortbay.iwiki.User;
import com.mortbay.iwiki.YyyyMmDd;
import com.mortbay.iwiki.YyyyMmDdHM;


/**
 * ReservationData.java
 *
 *
 * Created: Sat Mar 27 21:06:02 2004
 *
 * @author <a href="mailto:janb@wafer">Jan Bartel</a>
 * @version 1.0
 */
public class ReservationData implements Serializable, Cloneable
{
    private static final long serialVersionUID = -882720970660969512L;
    
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal DEPOSIT = new BigDecimal("0.2");
    private String id = null;
    private String name = null;
    private String email = null;
    private Address postal = null;
    private String phone1 = null;
    private String phone2 = null;
    private String fax = null;
    private String codice = null;
    private YyyyMmDd startDate = null;
    private YyyyMmDd endDate = null;
    private int adults = 0;
    private int children = 0;
    private int infants = 0;
    private String manager = null;
    private String aptId = null;
    private String groupId = null;
    private String referer = null;
    private ReservationStatus status = null;
    private BigDecimal paid = ZERO;
    private BigDecimal price = ZERO;
    private String discountCode = null;
    private String notes = null;
    private String notesPrivate = null;
    private int etaHh;
    private String language = null;
    private String currency = "EUR";//the default
    private Currency currencyObj = Currency.getInstance(currency);
    private String priceBasis = null;
    private List notifications = null;
    private List history = null;
    private List adjustments = null;
    private YyyyMmDdHM lastTotalDueReminder = null;
    private int commissionPercent=0;
    

    public ReservationData (ReservationData rd)
    {
        copy (rd);
    }

    public ReservationData (String id)
    {
        this.id = id;
    }
    
    public ReservationData ()
    {
        
    }

    /**
     * Copy info from one data object to this one
     * @param rd
     */
    public void copy (ReservationData rd)
    {       
        this.id = rd.getId();       
        this.aptId = rd.getAptId(); 
        this.startDate = (rd.getStartDate()==null?null:new YyyyMmDd(rd.getStartDate()));
        this.endDate = (rd.getEndDate()==null?null:new YyyyMmDd(rd.getEndDate()));
        this.status = rd.getStatus();
        this.paid = rd.getPaid();
        update(rd);
    }

    /**
     * Copy info from one data object to this one,
     * excluding id, aptId, start date, end date, status, paid
     * @param rd
     */
    public void update(ReservationData rd)
    {   
        this.email = rd.getEmail();      
        this.fax = rd.getFax();
        this.codice = rd.getCodice();
        this.groupId = rd.getGroupId();
        this.adults = rd.getAdults();
        this.children = rd.getChildren();
        this.infants = rd.getInfants();
        this.name = rd.getName();
        this.notes = rd.getNotes();
        this.notesPrivate = rd.getNotesPrivate();
        this.phone1 = rd.getPhone1();
        this.phone2 = rd.getPhone2();
        this.postal = (rd.getPostal()==null?null:new Address(rd.getPostal()));
        this.discountCode = rd.getDiscountCode();
        this.price = rd.getPrice();
        this.manager = rd.getManager();
        this.language = rd.getLanguage();
        this.currency = rd.getCurrency();
        this.currencyObj = Currency.getInstance(this.currency);
        this.etaHh = rd.getEtaHh();
        this.notifications = rd.getNotifications()==null?null:new ArrayList(rd.getNotifications());
        this.history = new ArrayList(rd.getHistory());
        this.adjustments = new ArrayList(rd.getAdjustments());
        this.priceBasis = rd.getPriceBasis();
        this.referer=rd.getReferer();
        this.lastTotalDueReminder = (rd.getLastTotalDueReminder()==null?null:new YyyyMmDdHM(rd.getLastTotalDueReminder()));
        this.commissionPercent = rd.getCommissionPercent();
    }    

    public Object clone ()
    throws CloneNotSupportedException
    {
        ReservationData rdclone = (ReservationData)super.clone();
        rdclone.copy (this);
        return rdclone;
    }
    
    public void setId (String id)
    {
        this.id = id;
    }
    
    public String getId ()
    {
        return id;
    }

    /**
     * Get the Name value.
     * @return the Name value.
     */
    public String getName() 
    {
        return name;
    }

    public String getReportName() 
    {
        return name.replace(',',' ');
    }

    /**
     * Set the LastName value.
     * @param newLastName The new Name value.
     */
    public void setName(String newName) 
    {
        this.name = newName;

    }

    
    /**
     * Get the EmailAddress value.
     * @return the EmailAddress value.
     */
    public String getEmail() {
        return email;
    }


    /**
     * Set the EmailAddress value.
     * @param newEmailAddress The new EmailAddress value.
     */
    public void setEmail(String newEmailAddress) 
    {
        this.email = newEmailAddress;

    }

      

    /**
     * Get the PostalAddress value.
     * @return the PostalAddress value.
     */
    public Address getPostal() 
    {
        return postal;
    }

    /**
     * Set the PostalAddress value.
     * @param newPostalAddress The new PostalAddress value.
     */
    public void setPostal(Address a) 
    {
        this.postal = a;       
    }

   

    /**
     * Get the Phone1 value.
     * @return the Phone1 value.
     */
    public String getPhone1() 
    {
        return phone1;
    }

    /**
     * Set the Phone1 value.
     * @param newPhone1 The new Phone1 value.
     */
    public void setPhone1(String newPhone1) 
    {
        this.phone1 = newPhone1;
    }

   
    /**
     * Get the Phone2 value.
     * @return the Phone2 value.
     */
    public String getPhone2() 
    {
        return phone2;
    }

    /**
     * Set the Phone2 value.
     * @param newPhone2 The new Phone2 value.
     */
    public void setPhone2(String newPhone2) 
    {
        this.phone2 = newPhone2;
    }

    

    /**
     * Get the Fax value.
     * @return the Fax value.
     */
    public String getFax() 
    {
        return fax;
    }

    /**
     * Set the Fax value.
     * @param newFax The new Fax value.
     */
    public void setFax(String newFax) 
    {
        this.fax = newFax;
    }

    /**
     * Get the Codice value.
     * @return the Codice value.
     */
    public String getCodice() 
    {
        return codice;
    }

    /**
     * Set the Codice value.
     * @param newCodice The new Codice value.
     */
    public void setCodice(String newCodice) 
    {
        this.codice = newCodice;
    }

    

    
    
   

    /**
     * Get the StartDate value.
     * @return the StartDate value.
     */
    public YyyyMmDd getStartDate() 
    {
        return startDate;
    }

    /**
     * Set the StartDate value.
     * @param newStartDate The new StartDate value.  A copy is taken of the YyyyMmDd object.
     */
    public void setStartDate(YyyyMmDd newStartDate) 
    {
        this.startDate = new YyyyMmDd(newStartDate);
    }

 

    /**
     * Get the EndDate value.
     * @return the EndDate value.
     */
    public YyyyMmDd getEndDate() 
    {
        return endDate;
    }
    
    public int getNights()
    {
        return startDate.daysTo(endDate);
    }

    /**
     * Set the EndDate value.
     * @param newEndDate The new EndDate value. A copy is taken of the YyyyMmDd object.
     */
    public void setEndDate(YyyyMmDd newEndDate) 
    {
        this.endDate = new YyyyMmDd(newEndDate);
    }

   
    public void setAdults  (int n)
    {
        adults = n;
    }

    public int getAdults ()
    {
        return adults;
    }

    public void setChildren (int n)
    {
        children = n;
    }
    
    public int getChildren ()
    {
        return children;
    }
    
    public void setInfants (int n)
    {
        infants = n;
    }
    
    public int getInfants ()
    {
        return infants;
    }
    
    public void setManager (String mgr)
    {
        this.manager = mgr;
    }
    
    public String getManager ()
    {
        return this.manager;
    }
    
    
    /**
     * Get the TotalAmount value.
     * @return  price - discount
     */
    public BigDecimal getTotal() 
    {
        BigDecimal total = price;
        Iterator i = getAdjustments().iterator();
        while(i.hasNext())
        {
            Adjustment adj = (Adjustment)i.next();
            total=total.add(adj.getAmount());
        }
        return total.setScale(currencyObj.getDefaultFractionDigits(), BigDecimal.ROUND_UP);
    }
        
    /**
     * @return Returns the total discount.
     */
    public BigDecimal getPriceAdjustments()
    {
        BigDecimal discount = ZERO;
        Iterator i = getAdjustments().iterator();
        while(i.hasNext())
        {
            Adjustment adj = (Adjustment)i.next();
            if (adj.isPrice())
                discount=discount.add(adj.getAmount());
        }
        return discount.setScale(currencyObj.getDefaultFractionDigits(), BigDecimal.ROUND_UP);
    }
    
    /**
     * @return Returns the total discount.
     */
    public BigDecimal getOwnerAdjustments()
    {
        BigDecimal discount = ZERO;
        Iterator i = getAdjustments().iterator();
        while(i.hasNext())
        {
            Adjustment adj = (Adjustment)i.next();
            if (adj.isOwner())
                discount=discount.add(adj.getAmount());
        }
        return discount.setScale(currencyObj.getDefaultFractionDigits(), BigDecimal.ROUND_UP);
    }
    
    /**
     * @return Returns the total discount.
     */
    public BigDecimal getManagerAdjustments()
    {
        BigDecimal discount = ZERO;
        Iterator i = getAdjustments().iterator();
        while(i.hasNext())
        {
            Adjustment adj = (Adjustment)i.next();
            if (adj.isManager())
                discount=discount.add(adj.getAmount());
        }
        return discount.setScale(currencyObj.getDefaultFractionDigits(), BigDecimal.ROUND_UP);
    }
    
    /**
     * @return Returns the total discount.
     */
    public boolean hasAdjustments(String type)
    {
        Iterator i = getAdjustments().iterator();
        while(i.hasNext())
        {
            Adjustment adj = (Adjustment)i.next();
            if (type.equals(adj.getType()))
                return true;
        }
        return false;
    }

  
    public String getNotes ()
    {
        return notes;
    }

    public void setNotes (String str)
    {
        this.notes = str;
    }
    
    public String getNotesPrivate ()
    {
        return notesPrivate;
    }

    public void setNotesPrivate (String str)
    {
        this.notesPrivate = str;
    }
    
    public String getNotesSummary ()
    {
        if (notes==null)
            return "";
        String n = StringUtil.replace(getNotes(), "\n", " ");
        n=StringUtil.replace(n, "\r", " ");
        n=StringUtil.replace(n, "\010", " ");
        n=StringUtil.replace(n, "\013", " ");
        return n;
    }


    /**
     * Get the paid value.
     * @return the amount of money paid
     */
    public BigDecimal getPaid() 
    {
        return paid;
    }

    /**
     * Get the amount of money still owing.
     * Can be negative if overpaid
     *
     * @return total - paid
     */
    public BigDecimal getOwing ()
    {
        BigDecimal result = getTotal().subtract(getPaid());
        return (result.setScale(currencyObj.getDefaultFractionDigits(), BigDecimal.ROUND_UP));
    }
    
    /**
     * Get the amount of money still owing.
     * Can be negative if overpaid
     *
     * @return total - paid
     */
    public BigDecimal getDepositOwing()
    {
        BigDecimal result = getDeposit().subtract(getPaid());
        if (result.compareTo(ZERO)<=0)
            return ZERO;

        return (result.setScale(currencyObj.getDefaultFractionDigits(), BigDecimal.ROUND_UP));
    }

    public void setAptId (String aptId)
    {
        this.aptId = aptId;
    }

    public String getAptId ()
    {
        return this.aptId;
    }

    /**
     * Join this reservation to a group of reservations
     *
     * @param gid an id of a set of bookings
     */
    public void setGroupId (String gid)
    {
        this.groupId = gid;
    }

    public String getGroupId ()
    {
        return groupId;
    }

    public ReservationStatus getStatus ()
    {
        return this.status;
    }

    public String toString ()
    {
        StringBuffer buff = new StringBuffer();

        buff.append ("id="+getId()+", ");       
        buff.append ("name="+getName()+", ");
        buff.append ("apt="+getAptId()+", ");
        buff.append ("from="+getStartDate()+", ");
        buff.append ("to="+getEndDate()+", ");
        buff.append ("adults="+getAdults()+", ");
        buff.append ("children="+getChildren()+", ");
        buff.append ("infants="+getInfants()+", ");
        buff.append ("manager="+getManager()+", ");
        buff.append ("status="+getStatus()+", ");
        
        buff.append ("owing=("+getCurrency()+")="+getOwing()+",");
        return buff.toString();
    }
    
    /**
     * @return Returns the etaHh.
     */
    public int getEtaHh()
    {
        return etaHh;
    }
    
    /**
     * @param etaHh The etaHh to set.
     */
    public void setEtaHh(int etaHh)
    {
        this.etaHh=etaHh;
    }
    /**
     * @return Returns the 2 letter code for the language the booking was made in.
     */
    public String getLanguage()
    {
        return language;
    }
    /**
     * @param language The 2 letter code for the language the booking was made in.
     */
    public void setLanguage(String language)
    {
        this.language = language;
    }
    
    /** Set a code which calculates a discount.
     * CF also the lump sum discount.
     * @param name
     */
    public void setDiscountCode (String name)
    {
        if (name == null)
        {
            this.discountCode = null;
        }
        else 
        {                   
            //change it if it is different
            if ((this.discountCode == null) || (!this.discountCode.equals(name)))
            {
                this.discountCode = name;
            }
        }
       
    }
    
    public String getDiscountCode ()
    {
        return this.discountCode;
    }
    
    /**
     * @return Returns the price.
     */
    public BigDecimal getPrice()
    {
        return price;
    }
    
    /**
     * @param price The price to set.
     */
    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }
    
    /**
     * @return Returns the currency.
     */
    public String getCurrency()
    {
        return currency;
    }
    
    /**
     * @param currency The currency to set.
     */
    public void setCurrency(String currency)
    {
        this.currency = currency;
        this.currencyObj = Currency.getInstance(currency);
    }
    

    public boolean isActive()
    {
        return 
          status!=null &&
          !status.equals(ReservationStatus.CANCELLED) &&
          !status.equals(ReservationStatus.EXPIRED);
    }

    public boolean isActivatable ()
    {
        return 
          status!=null &&
           !status.equals(ReservationStatus.CANCELLED);
    }
    
    
    public boolean isOccupiable()
    {
        return 
          status!=null &&
          !status.equals(ReservationStatus.REQUESTED) &&
          !status.equals(ReservationStatus.CANCELLED) &&
          !status.equals(ReservationStatus.EXPIRED);
    }
    
    public boolean isCancelled ()
    {
        return status != null && status.equals(ReservationStatus.CANCELLED);
    }

    /**
     * @return
     */
    public String getSource ()
    {
        if (history==null || history.size()==0)
            return User.INTERNET.toString();
        String history=getHistory().get(0).toString();
        return getHistoryUser(history);
    }
    
    public boolean isNotified (String notificationType)
    {
        if (notifications == null)
            return false;
        
        return notifications.contains(notificationType.toUpperCase());
    }
    
    protected void notified (String notificationType)
    {
        if (notificationType == null)
            return;
        
        if (notifications == null)
            notifications = new ArrayList();
        
        if (!notifications.contains(notificationType.toUpperCase()))
          notifications.add(notificationType.toUpperCase());
    }
    
    protected List getNotifications ()
    {
        return notifications;
    }
    
    
    protected void setPaid (BigDecimal paid)
    {
        this.paid = paid.setScale(currencyObj.getDefaultFractionDigits(), BigDecimal.ROUND_UP);
    }

    protected void setStatus (ReservationStatus status)
    {
        this.status = status;
    }

    
    /**
     * @return Returns the priceBasis.
     */
    public String getPriceBasis()
    {
        return priceBasis;
    }
    /**
     * @param priceBasis The priceBasis to set.
     */
    public void setPriceBasis(String fareBasis)
    {
        this.priceBasis = fareBasis;
    }

    /**
     * 
     */
    public void clearNotifications()
    {
        if (this.notifications!=null)
            this.notifications.clear();
    }
    
    
    protected void setHistory (ArrayList list)
    {
        history = (ArrayList)list.clone();
    }
    
    /** Add a new history item
     */
    public void addHistory (String comment)
    {
        if (history == null)
            history = new ArrayList();
        history.add (new YyyyMmDdHM()+" "+User.getCurrentUser().getName()+" "+comment);           
    }
    
    /** Add a new history item
     */
    public void addHistory (YyyyMmDdHM ymdhm,String user,String comment)
    {
        if (history == null)
            history = new ArrayList();
        history.add (ymdhm+" "+user+" "+comment);           
    }
    
    public List getHistory ()
    {   
        return history==null
               ?Collections.EMPTY_LIST
               :Collections.unmodifiableList(history);
    }


    public void setAdjustments (List list)
    {
        if (list==null)
            adjustments=new ArrayList();
        else
            adjustments = new ArrayList(list);
    }
    
    /** Add a new adjustment item
     */
    public void addAdjustement (Adjustment adj)
    {
        if (adjustments == null)
            adjustments = new ArrayList();
        adjustments.add (adj);           
    }
    
    
    public List getAdjustments()
    {   
        return adjustments==null
               ?Collections.EMPTY_LIST
               :Collections.unmodifiableList(adjustments);
    }

    public void removeAdjustments(String type) 
    {
        if (adjustments==null)
            return;
        Iterator i=adjustments.iterator();
        while(i.hasNext())
        {
            Adjustment adj = (Adjustment)i.next();
            if (type!=null && type.equals(adj.getType()))
                i.remove();
        }
    };

    /* ------------------------------------------------------------ */
    public Adjustment addAdjustment(String type, BigDecimal amount, String comment)
    {
        Adjustment adj = new Adjustment();
        adj.setType(type);
        amount=amount.setScale(2, BigDecimal.ROUND_HALF_DOWN);
        adj.setAmount(amount);
        adj.setComment(comment);
        addAdjustement(adj);
        return adj;
    };

    /* ------------------------------------------------------------ */
    public String getLastHistoryWith(String substring)
    {
        for (int i=history.size();i-->0;)
        {
            String h=history.get(i).toString();
            int s0=h.indexOf(' ');
            int s1=h.indexOf(' ',s0+1);
            if (h.indexOf(substring, s1+1)>s1)
                return h;
        }
        return null;
    }
    

    /* ------------------------------------------------------------ */
    public static YyyyMmDdHM getHistoryDate(String history)
    {
        int s0=history.indexOf(' ');
        int s1=history.indexOf(' ',s0+1);
        try
        {
            if (s1<0 || !Character.isDigit(history.charAt(s0+1)))
                return new YyyyMmDd(history.substring(0,s0));
            return new YyyyMmDdHM(history.substring(0,s1));
        }
        catch (Exception e)
        {
            System.err.println(history);
            e.printStackTrace();
            return null;
        }
    }

    /* ------------------------------------------------------------ */
    public static String getHistoryUser(String history)
    {
        int s0=history.indexOf(' ');
        int s1=history.indexOf(' ',s0+1);

        if (!Character.isDigit(history.charAt(s0+1)))
        {
            return history.substring(s0+1,s1);
        }
        else
        {
            int s2=history.indexOf(' ',s1+1);
            return history.substring(s1+1,s2);
        }
    }

    /* ------------------------------------------------------------ */
    public YyyyMmDdHM getCreateDate()
    {
        if (history==null || history.size()==0)
            return null;
        String h=(String)history.get(0);
        return getHistoryDate(h);
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the referer.
     */
    public String getReferer()
    {
        return referer;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param referer The referer to set.
     */
    public void setReferer(String referer)
    {
        this.referer = referer;
    }

	public YyyyMmDdHM getLastTotalDueReminder() {
		return lastTotalDueReminder;
	}

	public void setLastTotalDueReminder(YyyyMmDdHM lastTotalDueReminder) {
		this.lastTotalDueReminder = lastTotalDueReminder;
	}

    /* ------------------------------------------------------------ */
    public BigDecimal getDeposit()
    {
        BigDecimal deposit=getTotal().multiply(DEPOSIT);
        deposit=deposit.setScale(2, RoundingMode.HALF_UP);
        return deposit;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return the commissionPercent
     */
    public int getCommissionPercent()
    {
        return commissionPercent;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param commissionPercent the commissionPercent to set
     */
    public void setCommissionPercent(int commissionPercent)
    {
        this.commissionPercent = commissionPercent;
    }
    

}

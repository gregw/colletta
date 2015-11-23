/*
 * Created on Apr 22, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package it.colletta.reservation;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.mortbay.iwiki.YyyyMmDd;
import com.mortbay.iwiki.YyyyMmDdHM;

/**
 * Convert a ReservationDAO into a set of properties and vice versa
 * @author janb
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ReservationPropertyConverter
{
    private static final Logger log = Logger.getLogger(ReservationPropertyConverter.class.getName());
    public static ReservationData toObject (Properties props)
    throws Exception
    {
        ReservationData rd = new ReservationData();
        rd.setId(props.getProperty("res.id", null));
        rd.setName(props.getProperty("res.name", null));
        rd.setEmail(props.getProperty("res.email", null));
        rd.setFax(props.getProperty("res.fax", null));
        rd.setCodice(props.getProperty("res.codice", null));
        rd.setPhone1(props.getProperty("res.phone1", null));
        rd.setPhone2(props.getProperty("res.phone2", null));
        rd.setPostal (
                new Address (
                        props.getProperty("res.postal.address", null),
                        props.getProperty("res.postal.postcode", null),
                        props.getProperty("res.postal.country", null)));
        
        String tmp = props.getProperty("res.status", null);
        rd.setStatus ((tmp==null?null:ReservationStatus.getByIntCode(Integer.parseInt(tmp.trim()))));
        tmp = props.getProperty("res.adults");
        rd.setAdults ((tmp==null?0:Integer.parseInt(tmp.trim())));
        tmp = props.getProperty("res.children");
        rd.setChildren ((tmp==null?0:Integer.parseInt(tmp.trim())));
        tmp = props.getProperty ("res.infants");
        rd.setInfants ((tmp==null?0:Integer.parseInt(tmp.trim())));
        tmp = props.getProperty("res.startDate", null);
        if ((tmp != null) && (tmp.trim().length() > 0))
        {
            YyyyMmDd startDate = null;
            if (tmp.indexOf("-") >= 0)
             startDate = new YyyyMmDd(tmp);
            else
            {
                startDate = new YyyyMmDd();
                startDate.setYyyymmdd(Integer.parseInt(tmp));
            }
            rd.setStartDate(startDate);
        }
        
        tmp = props.getProperty("res.endDate", null);
        
        if ((tmp != null) && (tmp.trim().length() > 0))
        {
            YyyyMmDd endDate = null;
            
            if (tmp.indexOf("-") >= 0)
                endDate = new YyyyMmDd(tmp);
            else
            {
                endDate = new YyyyMmDd();
                endDate.setYyyymmdd(Integer.parseInt(tmp));
            }
            rd.setEndDate(endDate);
        }
        
        rd.setManager(props.getProperty("res.manager"));
        rd.setLanguage(props.getProperty("res.language"));
        rd.setReferer(props.getProperty("res.referer", null));
        
        tmp = props.getProperty("res.currency");
        if (tmp != null)
            rd.setCurrency(tmp);
        rd.setAptId(props.getProperty("res.aptId", null));
        tmp = props.getProperty("res.paid", "0.00");
        rd.setPaid(new BigDecimal(tmp));
        tmp = props.getProperty("res.price" ,"0.00");
        rd.setPrice(new BigDecimal(tmp));
        rd.setDiscountCode(props.getProperty("res.discountCode", null));
        rd.setNotes(props.getProperty("res.notes", null));
        rd.setNotesPrivate(props.getProperty("res.notesPrivate", null));
        tmp = props.getProperty("res.notifications", null);
        if (tmp != null)
        {
            StringTokenizer strtok = new StringTokenizer(tmp, ",");
            while (strtok.hasMoreTokens())
            {
                rd.notified(strtok.nextToken().trim());
            }
        }
        tmp = props.getProperty("res.eta");
        if (tmp != null)
            rd.setEtaHh(Integer.parseInt(tmp.trim()));
        
        rd.setPriceBasis(props.getProperty("res.priceBasis", null));
        rd.setCommissionPercent(Integer.parseInt(props.getProperty("res.commission", "40")));
        
        ArrayList adjustments = new ArrayList();
        for (int i=0; true; i++ )
        {
            String adj=null;
            if (i<10)
            {
                adj=props.getProperty("res.adjustment0"+i, null);
                if (adj==null)
                    adj=props.getProperty("res.adjustment"+i, null);
            }
            else
                adj=props.getProperty("res.adjustment"+i, null);
            if (adj==null)
                break;
            adjustments.add(new Adjustment(adj)); 
        }
        
        if (!adjustments.isEmpty())
            rd.setAdjustments(adjustments);
        
        
        ArrayList history = new ArrayList();
        for (int i=0; true; i++ )
        {
            String event=null;
            if (i<10)
            {
                event=props.getProperty("res.history0"+i, null);
                if (event==null)
                    event=props.getProperty("res.history"+i, null);
            }
            else
                event=props.getProperty("res.history"+i, null);
            if (event==null)
                break;
            history.add(event);          
        }
        
        if (!history.isEmpty())
            rd.setHistory(history);
        
        tmp = props.getProperty("res.lastTotalDueReminder", null);
        
        if ((tmp != null) && (tmp.trim().length() > 0))
        {
            YyyyMmDdHM lastTotalDueReminder = null;
            
            if (tmp.indexOf("-") >= 0)
                lastTotalDueReminder = new YyyyMmDdHM(tmp);
            else
            {
                lastTotalDueReminder = new YyyyMmDdHM();
                lastTotalDueReminder.setYyyymmdd(Integer.parseInt(tmp));
            }
            rd.setLastTotalDueReminder(lastTotalDueReminder);
        }
        
        return rd;
    }
    
    
    public static Properties toProperties (ReservationData rd)
    throws Exception
    {
        Properties props = new Properties();

        setProperty (props,"res.id", rd.getId());        
        setProperty (props,"res.name", rd.getName());
        setProperty (props,"res.email", rd.getEmail());
        setProperty (props,"res.fax", rd.getFax());
        setProperty (props,"res.codice", rd.getCodice());
        setProperty (props,"res.phone1", rd.getPhone1());
        setProperty (props,"res.phone2", rd.getPhone2());
        if (rd.getPostal() != null)
        {
            setProperty (props,"res.postal.address", rd.getPostal().getAddress());
            setProperty (props,"res.postal.postcode", rd.getPostal().getPostcode());
            setProperty (props,"res.postal.country", rd.getPostal().getCountry());
        }
        if (rd.getStatus() != null)
            setProperty (props,"res.status", rd.getStatus().getCode().toString());
        
        setProperty (props,"res.adults", String.valueOf(rd.getAdults()));
        setProperty (props,"res.children", String.valueOf(rd.getChildren()));
        setProperty (props,"res.infants", String.valueOf(rd.getInfants()));
        YyyyMmDd date = rd.getStartDate();
        if (date != null)
            setProperty (props,"res.startDate", date.toString());
        date = rd.getEndDate();
        if (date != null)
            setProperty (props,"res.endDate", date.toString());
        setProperty (props,"res.aptId", rd.getAptId());
        setProperty (props,"res.paid", (rd.getPaid()==null?null:rd.getPaid().toString()));
        setProperty (props,"res.price", (rd.getPrice()==null?null:rd.getPrice().toString()));
        setProperty (props,"res.notes", rd.getNotes());
        setProperty (props,"res.notesPrivate", rd.getNotesPrivate());
        setProperty (props,"res.manager", rd.getManager());
        setProperty (props,"res.referer", rd.getReferer());
        setProperty (props,"res.language", rd.getLanguage());
        setProperty (props,"res.currency", rd.getCurrency());
        setProperty (props,"res.discountCode", rd.getDiscountCode());
        List al = rd.getNotifications();
        String tmp = (al==null?Collections.EMPTY_LIST.toString():al.toString());
        tmp = tmp.substring(1);
        tmp = tmp.substring(0,tmp.length()-1);
        setProperty (props,"res.notifications", tmp);
        setProperty (props,"res.eta", String.valueOf(rd.getEtaHh()));
        setProperty (props, "res.source", rd.getSource());
        setProperty (props, "res.priceBasis", rd.getPriceBasis());
        setProperty (props, "res.commission", ""+rd.getCommissionPercent());

        al = rd.getAdjustments();
        for (int i=0; (null!=al && i< al.size()); i++)
            props.setProperty("res.adjustment"+(i<10?"0":"")+i, al.get(i).toString());
        
        al = rd.getHistory();
        for (int i=0; (null!=al && i< al.size()); i++)
            props.setProperty("res.history"+(i<10?"0":"")+i, al.get(i).toString());
        
        
        YyyyMmDdHM rdate = rd.getLastTotalDueReminder();
        if (rdate != null)
            setProperty (props,"res.lastTotalDueReminder", rdate.toString());
        rdate = rd.getLastTotalDueReminder();
        if (rdate != null)
            setProperty (props,"res.lastTotalDueReminder", rdate.toString());
        return props;
    }
    
    public static void setProperty (Properties props, String name, String value)
    {
        if (value == null)
            return;
        
        if (props == null)
            return;
        
        if (name == null)
            return;
        
        props.setProperty(name,value);
    }
}

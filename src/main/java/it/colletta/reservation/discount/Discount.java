//========================================================================
//$Id: Discount.java,v 1.3 2005/08/29 08:33:11 gregw Exp $
//Copyright 2000-2004 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package it.colletta.reservation.discount;

import it.colletta.reservation.ReservationData;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import it.colletta.reservation.Adjustment;
import it.colletta.Apartment;

/**
 * 
 * Discount
 *
 * @author janb
 * @version $Revision: 1.3 $ $Date: 2005/08/29 08:33:11 $
 *
 */
public abstract class Discount
{
    public static final boolean DISCOUNT=true;
    public static final boolean NO_DISCOUNT=false;
    public static final BigDecimal ZERO=new BigDecimal("0.00");
    public static final BigDecimal ONE=new BigDecimal("1.00");
    public static final BigDecimal TWO=new BigDecimal("2.00");
    public static final BigDecimal ONE_HUNDRED=new BigDecimal("100.00");
    private static HashMap discountMap = new HashMap();
    
    
    private static Discount loadDiscount (String className)
    throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {                          
        Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
        return (Discount)clazz.newInstance();        
    }
    
    public static Discount getDiscount (String name)
    {
        String n = name.toLowerCase();
        
        Discount disc = (Discount)discountMap.get(n);
        if (disc == null)
        {
            String cname = n.substring(0,1).toUpperCase()+n.substring(1);
            if (cname.indexOf("-")>0)
                cname=cname.substring(0,cname.indexOf("-"));
            
            try
            {                           
                disc = loadDiscount("it.colletta.reservation.discount."+cname);
                discountMap.put (n, disc);
            }
            catch (Exception e)
            {
                //try to find class with "Discount" suffixed
                try
                {
                    disc = loadDiscount("it.colletta.reservation.discount."+cname+"Discount");
                    discountMap.put (n, disc);
                }
                catch (Exception x)
                {
                    
                }
            }
        }
        return disc;
    }
    
    protected void applySystemSurchargeDiscounts(ReservationData res, Apartment apt)
    {
        int clean_discount=10;
        String basis=res.getPriceBasis();
        if (basis!=null && basis.length()>0)
        {
            char season = basis.charAt(0);
            if (season!='H' && season!='L' && season!='M')
                season='H';
            clean_discount = Integer.parseInt(apt.getPage().getPathProperty("10", "A_discountClean_"+season));
        }
        if (clean_discount > 0)
        {
            BigDecimal adjust = new BigDecimal("0." + (100 - clean_discount));
        }
    }
    
    public abstract String getDescription();
    
    public abstract boolean calculate (ReservationData res);
}

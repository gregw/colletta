//========================================================================
//$Id: KpmgDiscount.java,v 1.2 2006/01/21 17:21:00 gregw Exp $
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

import it.colletta.Apartment;
import it.colletta.reservation.Adjustment;
import it.colletta.reservation.ReservationData;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.mortbay.iwiki.YyyyMmDd;


/**
 * 
 * KPMG Colletta Discount
 *
 * @author gregw
 * @version $Revision: 1.2 $ $Date: 2006/01/21 17:21:00 $
 *
 */
public class KpmgDiscount extends Discount
{
    public final BigDecimal SCONTO=new BigDecimal("-0.08");
   
    public String getName ()
    {
        return "kpmg";
    }
    
    /** Mamberto agent.
     * @see it.colletta.reservation.discount.Discount#getDescription()
     */
    public String getDescription ()
    {
        return "KPMG 8%";
    }

    /** Modify the reservation with any discount that is due
     * @see it.colletta.reservation.discount.Discount#calculate(it.colletta.reservation.ReservationData)
     */
    public boolean calculate (ReservationData res)
    {
        String code=res.getDiscountCode();
        String email=res.getEmail();
        if (code==null || email==null || code.length()==0 || email.length()==0)
            return false;
        
        if (email.toLowerCase().indexOf("@kpmg")<0)
            return false;
        
        BigDecimal price = res.getPrice();
        BigDecimal sconto = price.multiply(SCONTO);
        sconto=sconto.setScale(2, BigDecimal.ROUND_UP);
        
        res.addAdjustment(Adjustment.__CODE , sconto, getDescription());
          
        return true;
    }
    
}

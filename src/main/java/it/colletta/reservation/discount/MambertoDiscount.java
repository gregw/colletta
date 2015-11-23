//========================================================================
//$Id: MambertoDiscount.java,v 1.15 2006/07/16 11:40:03 gregw Exp $
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
import java.math.RoundingMode;

import com.mortbay.iwiki.User;
import com.mortbay.iwiki.YyyyMmDd;


/**
 * 
 * MambertoDiscount
 *
 * @author janb
 * @version $Revision: 1.15 $ $Date: 2006/07/16 11:40:03 $
 *
 */
public class MambertoDiscount extends Discount
{
    public static final int NO_SEASON = 0;
    public static final int A_SEASON = 1;
    public static final int B_SEASON = 2;
    public static final int C_SEASON = 3;
    public static final int D_SEASON = 4;
    public static final BigDecimal CENTO=new BigDecimal(100);
    public static final BigDecimal SEVEN=new BigDecimal(7);

    public String getName ()
    {
        return "mamberto";
    }
    
    /** Mamberto agent.
     * @see it.colletta.reservation.discount.Discount#getDescription()
     */
    public String getDescription ()
    {
        return "Mamberto";
    }

    /** Modify the reservation with any discount that is due
     * @see it.colletta.reservation.discount.Discount#calculate(it.colletta.reservation.ReservationData)
     */
    public boolean calculate (ReservationData res)
    {
        if (User.getCurrentUser()==null || User.getCurrentUser().equals(User.INTERNET))
            return NO_DISCOUNT;
        
        if (res.getStartDate() == null)
            return NO_DISCOUNT;
        
        if (res.getEndDate() == null)
            return NO_DISCOUNT;
        
        if (res.getPrice() == null)
            return NO_DISCOUNT;
        
        if (res.getPrice().compareTo(ZERO) <= 0)
            return NO_DISCOUNT;
        
        if (res.getAptId() == null)
            return NO_DISCOUNT;
        

        String code = res.getDiscountCode().toLowerCase();
        String[] codes=code.split("-");
        
        // check discount codes on apartment for "mamberto" discounts
        Apartment apt = Apartment.getApartment(res.getAptId());
        String apt_type = apt.getPage().getProperty("A_mamberto");
        String type = apt_type;
        

        int nights = res.getStartDate().daysTo(res.getEndDate());
        int discount=0;
        
        if (codes.length>1)
        {
            for (int i=1;i<codes.length;i++)
            {
                if (codes[i].endsWith("n"))
                    nights-=Integer.parseInt(codes[i].substring(0,codes[i].length()-1));
                else if (codes[i].endsWith("%"))
                    discount=Integer.parseInt(codes[i].substring(0,codes[i].length()-1));
                else
                    type=codes[i];
            }
        }
        
        if (apt_type==null)
            return NO_DISCOUNT;
        

        int season = getSeason(res.getStartDate());
        String season_basis=""+(char)('A'+season-1);
        int rate=rate(type,season);
            
        
        
        BigDecimal mambertoPrice = ZERO;
        
        if (nights<7)
        {
            BigDecimal price_nights=null;
            switch (nights)
            { 
                case 1: 
                case 2: 
                    price_nights=new BigDecimal("4.0");
                    break;
                case 3: 
                    price_nights=new BigDecimal("4.5");
                    break;
                case 4: 
                    price_nights=new BigDecimal("5.0");
                    break;
                default:
                    price_nights=new BigDecimal(nights+".0");
                    break;
            }

            mambertoPrice = price_nights.multiply(new BigDecimal(rate)).divide(SEVEN,RoundingMode.HALF_UP);
        }
        else if (nights==7)
            mambertoPrice =new BigDecimal(rate);
        else
        {
            mambertoPrice=ZERO;
            int n=nights;
            YyyyMmDd week = new YyyyMmDd(res.getStartDate());
            while (n>=7)
            {
                mambertoPrice = mambertoPrice.add(new BigDecimal(rate));
                n-=7;
                week.addDays(7);
                season = getSeason(week);
                rate=rate(type,season);
                
                if (n>0)
                    season_basis+=""+(char)('A'+season-1);
            }
            
            if (n>0)
                mambertoPrice = mambertoPrice.add(new BigDecimal(n).multiply(new BigDecimal(rate).divide(SEVEN,RoundingMode.HALF_UP)));
        }
        
        if (discount>0)
        {
            mambertoPrice =  mambertoPrice.multiply(new BigDecimal(100-discount)).setScale(2).divide(CENTO,RoundingMode.HALF_UP);
        }
        
        mambertoPrice.setScale(2,RoundingMode.HALF_UP);

        super.applySystemSurchargeDiscounts(res, apt);
        
        BigDecimal discountTotal=res.getTotal().subtract(mambertoPrice);
        res.addAdjustment(Adjustment.__CODE, discountTotal.negate(), getName());

        res.setCommissionPercent(apt.getPage().getIntPathProperty("A_commissionMamberto"));
        res.setPriceBasis(type+" "+season_basis+"x"+nights+"-"+discount+"%"); //set the basis to reflect Mamberto seasons
        res.setManager("mamberto");
        return DISCOUNT;
    }

    private int rate(String type,int season)
    {
        int rate=0;

        if ("A2".equalsIgnoreCase(type))
        {
            switch(season)
            {
                case A_SEASON: rate=250; break;
                case B_SEASON: rate=300; break;
                case C_SEASON: rate=425; break;
                case D_SEASON: rate=525; break;
            }
        }
        else if ("B4".equalsIgnoreCase(type))
        {
            switch(season)
            {
                case A_SEASON: rate=350; break;
                case B_SEASON: rate=450; break;
                case C_SEASON: rate=600; break;
                case D_SEASON: rate=700; break;
            }
        }
        else if ("C4".equalsIgnoreCase(type))
        {
            switch(season)
            {
                case A_SEASON: rate=400; break;
                case B_SEASON: rate=500; break;
                case C_SEASON: rate=700; break;
                case D_SEASON: rate=800; break;
            }
        }
        else if ("C6".equalsIgnoreCase(type))
        {
            switch(season)
            {
                case A_SEASON: rate=500; break;
                case B_SEASON: rate=600; break;
                case C_SEASON: rate=750; break;
                case D_SEASON: rate=900; break;
            }
        }
        else if ("C6s".equalsIgnoreCase(type))
        {
            switch(season)
            {
                case A_SEASON: rate=550; break;
                case B_SEASON: rate=650; break;
                case C_SEASON: rate=800; break;
                case D_SEASON: rate=950; break;
            }
        }
        else if ("D6s".equalsIgnoreCase(type))
        {
            switch(season)
            {
                case A_SEASON: rate=800; break;
                case B_SEASON: rate=1000; break;
                case C_SEASON: rate=1200; break;
                case D_SEASON: rate=1300; break;
            }
        }
        return rate;
    }

    private int getSeason (YyyyMmDd day)
    {
        // A 06/01 - 7/04, 14/04 - 04/05, 08/09 - 30/11
        // B 5/05 - 8/06, 25/08 - 07/09
        // C 1/01 - 5/01, 7/04 - 13/04, 9/06 - 13/07
        // D 14/07 - 17/08
        
        switch (day.getMm())
        {
            case 1:
            {
                if (day.getDd() < 6)
                    return C_SEASON;
                return A_SEASON;
            }
            case 2:
            {
                return A_SEASON;
             }
            case 3:
            {
                return A_SEASON;
            }
            case 4:
            {
                if (day.getDd() >=7 && day.getDd()<14)
                    return C_SEASON;
                return A_SEASON;
            }
            case 5:
            {
                if (day.getDd() < 5)
                    return A_SEASON;
                return B_SEASON;
            }
            case 6:
            {
                if (day.getDd() < 9)
                    return B_SEASON;
                return C_SEASON;
            }
            case 7:
            {
                if (day.getDd() < 14)
                    return C_SEASON;
                return D_SEASON;
            }
            case 8:
            {
                if (day.getDd() < 18)
                    return D_SEASON;
                if (day.getDd() < 25)
                    return C_SEASON;
                return B_SEASON;
            }
            case 9:
            {
                if (day.getDd() < 8)
                    return B_SEASON;
                return A_SEASON;
            }
            case 10:
            {
                return A_SEASON;
            }
            case 11:
            {
                return A_SEASON;               
            }
            case 12:
            {
                if (day.getDd() < 30)
                    return C_SEASON;
                return A_SEASON;
            }
            default:
            {
                return NO_SEASON;
            }
        }
    }
}

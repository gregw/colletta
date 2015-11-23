//========================================================================
//$Id: Adjustment.java,v 1.2 2005/08/31 09:19:17 gregw Exp $
//Copyright 2004 Mort Bay Consulting Pty. Ltd.
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

package it.colletta.reservation;

import java.math.BigDecimal;
import java.util.Arrays;

public class Adjustment
{
    public final static String
        __EXTRA="Extra",           // part of price. 10% IVA
        __DISCOUNT="Discount",     // part of price. 10% IVA
        __CODE="Code",             // part of price. 10% IVA
        __SERVICE="Service",       // To Manager.  20% IVA
        __OWNER="Owner";           // To Owner.  0% IVA
                                   // every thing else to Manager 20% IVA
    
    private String type;        // eg. Extra, Discount, Service, Owner
    private BigDecimal amount;
    private String comment;
    
    public Adjustment()
    {}
    
    public Adjustment(String string)
    {
        String[] bits = string.split("\\|");
        if (bits.length>2)
        {
            // convert 2008 format
            type=bits[0];
            String description=bits[1];
            amount=new BigDecimal(bits[2].substring(0,bits[2].length()-1));
            boolean system=Boolean.valueOf(bits[3]).booleanValue();
            boolean auto=Boolean.valueOf(bits[4]).booleanValue();
            if (bits.length>5)
                comment=bits[5];
            else
                comment="";
            
            if ("Surcharge".equals(type))
            {
                if ("Ad-hoc".equals(description))
                    type=__DISCOUNT;
                else if ("Associazione".equals(description))
                    type=__EXTRA;
                else if ("Booking".equals(description))
                    type=__EXTRA;
                else if ("CheckIn".equals(description))
                    type=__EXTRA;
                else if ("Energia".equals(description))
                    type=__OWNER;
                else if ("Extra".equals(description))
                    type=__EXTRA;
                else if ("Pulizia".equals(description))
                {
                    type=__EXTRA;
                    comment ="Pulizia "+comment;
                }
                else
                    type=description;
            }
            else if ("Discount".equals(type))
            {
                if ("Code".equals(description))
                    type=__CODE;
            }
        }
        else
        {
            bits = string.split(",",3);
            type=bits[0];
            amount=new BigDecimal(bits[1]);
            comment=bits[2];
        }
    }
    
    public String toString()
    {
        String s= type+","+amount+","+(comment==null?"":comment);
        return s;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the amount.
     */
    public BigDecimal getAmount() {
        return amount;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param amount The amount to set.
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the comment.
     */
    public String getComment() 
    {
        return comment;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param comment The comment to set.
     */
    public void setComment(String comment) 
    {
        this.comment = comment;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the type.
     */
    public String getType() 
    {
        return type;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the type.
     */
    public String getTypeInfo() 
    {
        return "I_"+type;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param type The type to set.
     */
    public void setType(String type) 
    {
        this.type = type;
    }

    /* ------------------------------------------------------------ */
    public boolean isPrice() 
    {
        return __DISCOUNT.equalsIgnoreCase(getType()) || __EXTRA.equalsIgnoreCase(getType())|| __CODE.equalsIgnoreCase(getType());
    } 
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the system.
     */
    public boolean isOwner()
    {
        return __OWNER.equalsIgnoreCase(getType());
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the system.
     */
    public boolean isManager()
    {
        return !isPrice() && !isOwner();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return the taxIncluded
     */
    public boolean isTaxIncluded()
    {
        return !__OWNER.equals(type);
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return the taxIncluded
     */
    public int getIva()
    {
        if (__OWNER.equals(type))
            return 0;
        if (__DISCOUNT.equals(type))
            return 10;
        if (__CODE.equals(type))
            return 10;
        if (__EXTRA.equals(type))
            return 10;
        return 20;
    }
    
}
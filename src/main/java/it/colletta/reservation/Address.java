/*
 * Created on Mar 31, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package it.colletta.reservation;

import java.io.Serializable;

/**
 * @author janb
 *
 * 
 */
public class Address implements Serializable, Cloneable
{
    private String address;
    private String postcode;
    private String country;
    
    public Address ()
    {
    }
    
    public Address (String address, String postcode, String country)
    {
        this.address = address;
        this.postcode = postcode;
        this.country = country;
    }
    
    public Address (Address copy)
    {
        this.address = copy.getAddress();
        this.postcode = copy.getPostcode();
        this.country = copy.getCountry();
    }
    /**
     * @return Returns the address.
     */
    public String getAddress()
    {
        return address;
    }
    /**
     * @param address The address to set.
     */
    public void setAddress(String address)
    {
        this.address = address;
    }
    /**
     * @return Returns the country.
     */
    public String getCountry()
    {
        return country;
    }
    /**
     * @param country The country to set.
     */
    public void setCountry(String country)
    {
        this.country = country;
    }
    /**
     * @return Returns the postcode.
     */
    public String getPostcode()
    {
        return postcode;
    }
    /**
     * @param postcode The postcode to set.
     */
    public void setPostcode(String postcode)
    {
        this.postcode = postcode;
    }
    
    public Object clone ()
    throws CloneNotSupportedException
    {
        return super.clone();
    }
}

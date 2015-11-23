package it.colletta.payment;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.mortbay.iwiki.StringUtil;
import com.mortbay.iwiki.User;

import it.colletta.Configuration;
import it.colletta.reservation.ReservationData;
import it.colletta.reservation.ReservationManager;

public class XPay 
{
    public static final int __RESPONSE_0K = 0;
    public static final int __RESPONSE_PARSE_ERROR = 1;
    public static final int __RESPONSE_TECH_ERROR = 2;
    public static final int __RESPONSE_DUP_TRANS = 3;
    public static final int __RESPONSE_BAD_LANG = 4;
    public static final int __RESPONSE_BAD_URL = 5;
    public static final int __RESPONSE_BAD_IP = 6;
    public static final int __RESPONSE_BAD_OPT = 7;
    public static final int __RESPONSE_BAD_MAC = 8;
    public static final int __RESPONSE_BAD_VERSION = 9;
    public static final int __RESPONSE_BAD_ACTION_CODE = 10;
    public static final int __RESPONSE_BAD_AMOUNT = 11;
    public static final int __RESPONSE_BAD_CURRENCY = 12;
    public static final int __RESPONSE_BAD_EMAIL = 13;
    public static final int __RESPONSE_BAD_TRANS = 15;
    public static final int __RESPONSE_BAD_TERMINAL_ID = 16;
    public static final int __RESPONSE_MAX_TRANS = 17;
    
    public static final String __LANG_ITA = "ITA";
    public static final String __LANG_ENG = "ENG";
    public static final String __LANG_FRA = "FRA";
    public static final String __LANG_ESP = "ESP";
    public static final String __LANG_DEU = "DEU";
    
    public static final String __CURR_EUR = "EUR";//euro
    public static final String __CURR_978 = "978"; //euro
    
    public static final String __AUT_CONT = "AUT-CONT"; //contab. implicit
    public static final String __AUT = "AUT"; //contab. explicit
    
    public static final String __TERMINAL_ID_PROP = "TERMINAL_ID";
    public static final String __TRANSACTION_ID_PROP = "TRANSACTION_ID";
    public static final String __AMOUNT_PROP = "AMOUNT";
    public static final String __CURRENCY_PROP = "CURRENCY";
    public static final String __VERSION_CODE_PROP = "VERSION_CODE";
    public static final String __CO_PLATFORM_PROP = "CO_PLATFORM";
    public static final String __ACTION_CODE_PROP = "ACTION_CODE";
    public static final String __EMAIL_PROP = "EMAIL";
    public static final String __MAC_PROP = "MAC";
    public static final String __RESPONSE_PROP = "RESPONSE";
    public static final String __LANGUAGE_PROP = "LANGUAGE";
    public static final String __NOTIFICATION_URL_PROP = "NOTIFICATION_URL";
    public static final String __RESULT_URL_PROP = "RESULT_URL";
    public static final String __ERROR_URL_PROP = "ERROR_URL";
    public static final String __ANNULMENT_URL_PROP = "ANNULMENT_URL";
    public static final String __DESC_ORDER_PROP = "DESC_ORDER";
    public static final String __MESSAGE_TYPE_PROP = "MESSAGE_TYPE";
    public static final String __CARD_TYPE_PROP = "CARD_TYPE";
    public static final String __AUTH_CODE_PROP = "AUTH_CODE";
    
    public static final String __PAYMENT_URL_PROP = "PAYMENT_URL"; //used by XPay.tag file
 

    private static final String __MAC_KEY_VALUE;
    private static final String __TERMINAL_ID_VALUE;
    private static final String __CO_PLATFORM_VALUE;
    private static final String __VERSION_VALUE;
    private static final String __ANNULMENT_URL_VALUE;
    private static final String __NOTIFICATION_URL_VALUE;
    private static final String __ERROR_URL_VALUE;
    private static final String __RESULT_URL_VALUE;
    private static final String __PAYMENT_URL_VALUE;

    private static final List idtransList;/* list of already processed transaction ids*/
  
    /* Initialize static data */
    static
    {
        __MAC_KEY_VALUE = Configuration.getInstance().getProperty("xpay.mackey");
        __TERMINAL_ID_VALUE = Configuration.getInstance().getProperty("xpay.terminalid"); 
        __CO_PLATFORM_VALUE = Configuration.getInstance().getProperty("xpay.coplatform");
        __VERSION_VALUE = Configuration.getInstance().getProperty("xpay.version");
        __PAYMENT_URL_VALUE = Configuration.getInstance().getProperty("xpay.paymenturl");
        __ANNULMENT_URL_VALUE = Configuration.getInstance().getProperty("colletta.website")+Configuration.getInstance().getProperty("xpay.annulmenturl");
        __ERROR_URL_VALUE = Configuration.getInstance().getProperty("colletta.website")+Configuration.getInstance().getProperty("xpay.errorurl");
        __NOTIFICATION_URL_VALUE = Configuration.getInstance().getProperty("colletta.website")+Configuration.getInstance().getProperty("xpay.notificationurl");
        __RESULT_URL_VALUE = Configuration.getInstance().getProperty("colletta.website")+Configuration.getInstance().getProperty("xpay.resulturl");
        idtransList = new ArrayList();
    }
    
    
    public static int getVPOSResCode (HttpServletRequest request)
    {
        String s = request.getParameter(__RESPONSE_PROP);
        System.err.println("RESPONSE = "+s);
        if (s == null)
            return -1;
        
        try
        {
            int i = Integer.parseInt(s.trim());
            return i;
        }
        catch (NumberFormatException e)
        {
            return -1;
        }
    }
    
    public static String handlePayment (HttpServletRequest srequest, ServletContext context)
    throws Exception
    {
        User old=User.getCurrentUser();
        User.setCurrentUser(User.XPAY);
        try
        {
            //Verify that the payment confirmation message is authentic
            if (!verifyVPOSNotificationMAC(srequest))
            {
                //authentication of the message failed, log it
                context.log ("AUTH FAILED for XPAY NOTIFICATION: "+srequest.toString());
                return null;
            }
            
            String transactionId = srequest.getParameter(__TRANSACTION_ID_PROP);
            
            //Notification message is authentic, and by definition positive, so process it
            ReservationManager resMgr = ReservationManager.getInstance();
            synchronized (idtransList) 
            {
                //check if this payment message has been received before
                if (idtransList.contains(transactionId))
                {
                    //log duplicate message
                    context.log ("Ignoring duplicate payment confirmation message transactionId="+transactionId);
                    return null;
                }
                else
                {
                    //payment message not already received, process the payment
                    idtransList.add(transactionId);
                                       
                    if (srequest.getParameter(__CURRENCY_PROP).equals(__CURR_978))
                    {   
                        
                        String resId = transactionId;
                        int i = resId.indexOf("-");
                        if (i >= 0)
                            resId = resId.substring(0,i);
                        
                        //Get last 8 chars, which is the zero-padded reservation id
                        if (resId.length() > 8)
                        {
                            resId = resId.substring(resId.length()-8);
                        }
                        
                        ReservationData reservation=resMgr.findReservation(resId);
                        if (reservation==null)
                        {
                            throw new Exception ("Cannot make payment transactionId="+transactionId+": no such reservation resId="+resId);
                        }
                        
                        String str = srequest.getParameter(__AMOUNT_PROP);
                        str = str.substring(0, str.length()-2) + "." + str.substring(str.length()-2);
                        BigDecimal amount = new BigDecimal(str);    
                        resMgr.makePayment(resId,amount,transactionId+", "+srequest.getParameter(__CARD_TYPE_PROP)+", "+srequest.getParameter(__AUTH_CODE_PROP));
                        
                        context.log ("PAYMENT PROCESSED: resId="+resId+" for amount(EUR)="+str+" by "+srequest.getParameter(__CARD_TYPE_PROP)+" auth code="+srequest.getParameter(__AUTH_CODE_PROP));
                        return resId;
                    }           
                    else
                    {
                        throw new Exception ("Payment was not in EURO");
                    }
                }
            }  

        }
        finally
        {
            User.setCurrentUser(old);
        }     
    }


    
    
    
    /**
     * TERMINAL_ID
     * TRANSACTION_ID
     * ACTION_CODE
     * AMOUNT
     * CURRENCY
     * LANGUAGE
     * NOTIFICATION_URL
     * RESULT_URL
     * ERROR_URL
     * ANNULMENT_URL
     * VERSION_CODE
     * EMAIL
     * DESC_ORDER
     * CO_PLATFORM
     * MAC
     * MESSAGE_TYPE
     * OPTION FIELDS
     */
    public static void setVPOSReqLightAttributes (HttpServletRequest request, String resId, BigInteger amount, String lang, String email)
    throws Exception
    {
        String transactionId = makeTransactionId (resId);
        System.err.println("TransactionId = "+transactionId);
        
        request.setAttribute(__PAYMENT_URL_PROP, __PAYMENT_URL_VALUE);
        request.setAttribute(__TERMINAL_ID_PROP, __TERMINAL_ID_VALUE);
        request.setAttribute(__TRANSACTION_ID_PROP, transactionId);
        request.setAttribute(__ACTION_CODE_PROP, __AUT_CONT);
        String s = amount.toString();
        if (s.length() < 9)
        {
            StringBuffer buff = new StringBuffer();
            int i = 9 - s.length();
            for (int j=i; j> 0; j--)
                buff.append("0");
            buff.append(s);
            s = buff.toString();
        }
        request.setAttribute(__AMOUNT_PROP, s);
        request.setAttribute(__CURRENCY_PROP, __CURR_978);
        request.setAttribute(__LANGUAGE_PROP, convertLanguage(lang));
        request.setAttribute(__NOTIFICATION_URL_PROP, __NOTIFICATION_URL_VALUE);
        request.setAttribute(__RESULT_URL_PROP, __RESULT_URL_VALUE); 
        request.setAttribute(__ERROR_URL_PROP, __ERROR_URL_VALUE);
        request.setAttribute(__ANNULMENT_URL_PROP, __ANNULMENT_URL_VALUE);
        request.setAttribute(__VERSION_CODE_PROP, __VERSION_VALUE);
        request.setAttribute(__EMAIL_PROP, email);
        request.setAttribute(__DESC_ORDER_PROP, "");
        request.setAttribute(__CO_PLATFORM_PROP, __CO_PLATFORM_VALUE);
        request.setAttribute(__MAC_PROP, calculateVPOSReqLightMAC(request));
        //request.setAttribute(__MESSAGE_TYPE_PROP, );
        
    }
    
    
    public static void dumpVPOSReqLight (HttpServletRequest request)
    {
        System.err.println(request.getAttribute(__PAYMENT_URL_PROP));
        System.err.println(request.getAttribute(__TERMINAL_ID_PROP));
        System.err.println(request.getAttribute(__TRANSACTION_ID_PROP));
        System.err.println(request.getAttribute(__ACTION_CODE_PROP));
        System.err.println(request.getAttribute(__AMOUNT_PROP));
        System.err.println(request.getAttribute(__CURRENCY_PROP));
        System.err.println(request.getAttribute(__LANGUAGE_PROP));
        System.err.println(request.getAttribute(__NOTIFICATION_URL_PROP));
        System.err.println(request.getAttribute(__RESULT_URL_PROP)); 
        System.err.println(request.getAttribute(__ERROR_URL_PROP));
        System.err.println(request.getAttribute(__ANNULMENT_URL_PROP));
        System.err.println(request.getAttribute(__VERSION_CODE_PROP));
        System.err.println(request.getAttribute(__EMAIL_PROP));
        System.err.println(request.getAttribute(__DESC_ORDER_PROP));
        System.err.println(request.getAttribute(__CO_PLATFORM_PROP));
        System.err.println(request.getAttribute(__MAC_PROP));
    }
    
    public static String makeTransactionId (String resId)
    throws Exception
    {
        
       
        ReservationData rd = ReservationManager.getInstance().findReservation(resId);
        if (rd == null)
            throw new IllegalStateException("Unknown reservation: "+resId);
      
        //Make a unique id by appending ms since epoch as alpha string
        String str = (resId+"-"+Long.toString(System.currentTimeMillis(), 36));
        
        //pad to 20 char as per spec p.43
        if (str.length() < 20)
        {
            StringBuffer buff = new StringBuffer(20);
            int j = 20 - str.length();
            for (int i=j; i>0; i--)
                buff.append("0");
            buff.append(str);
            str = buff.toString();
        }
      
       
        return str;
    }
    
    /**
     * Generate a XPay MAC by using fields:
     * TERMINAL_ID = ESE_WEB_00000001
     * TRANSACTION_ID = 01234abcdefg01234567
     * AMOUNT = 000000009
     * CURRENCY = 978
     * VERSION_CODE = 01.00
     * CO_PLATFORM = L
     * ACTION_CODE = AUT
     * EMAIL = nome_cognome@tiscali.it
     * Chiave per MAC
     * 
     * @return
     */
    public static String calculateVPOSReqLightMAC (HttpServletRequest request)
    throws Exception
    {
       
        StringBuffer buff = new StringBuffer();
        append (buff, request.getAttribute(__TERMINAL_ID_PROP));
        append(buff, request.getAttribute(__TRANSACTION_ID_PROP));
        append(buff, request.getAttribute(__AMOUNT_PROP));
        append(buff, request.getAttribute(__CURRENCY_PROP));
        append(buff, request.getAttribute(__VERSION_CODE_PROP));
        append(buff, request.getAttribute(__CO_PLATFORM_PROP));
        append(buff, request.getAttribute(__ACTION_CODE_PROP));
        append(buff, request.getAttribute(__EMAIL_PROP));
        append(buff, __MAC_KEY_VALUE);
       
        return calculateMAC(buff.toString());
          
    }
    
    public static String calculateMAC (String str) 
    throws Exception
    {
        if (str == null)
            return null;
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        digest.update (str.getBytes("UTF-8"));
        return  toHexString(digest.digest());    
    }
    
    /**
     * Calculate a hash of text received from bank and compare it to
     * the XPay field received:
     * TERMINAL_ID
     * TRANSACTION_ID
     * RESPONSE
     * AMOUNT
     * CURRENCY
     * Chiave per MAC

     * @param request containing params
     * @return hash
     */
    public static String calculateVPOSNotificationMAC (HttpServletRequest request)
    throws Exception
    {
       StringBuffer buff = new StringBuffer();
       append(buff, request.getParameter(__TERMINAL_ID_PROP));
       append(buff, request.getParameter(__TRANSACTION_ID_PROP));
       append(buff, request.getParameter(__RESPONSE_PROP));
       append(buff, request.getParameter(__AMOUNT_PROP));
       append(buff, request.getParameter(__CURRENCY_PROP));
       append(buff, __MAC_KEY_VALUE);
       return calculateMAC(buff.toString());
    }

    
    /**
     * Check the mac sent by XPay matches the mac we calculate
     * from the message.
     * 
     * @param request
     * @return
     */
    public static boolean verifyVPOSNotificationMAC (HttpServletRequest request)
    throws Exception
    {
        String mac = request.getParameter(__MAC_PROP);
        String calculatedMac = calculateVPOSNotificationMAC(request);
        if (mac != null && mac.equals(calculatedMac))
            return true;
        
        return false;
    }

    
    public static String convertLanguage (String siteLang)
    {
        if (siteLang == null)
            return __LANG_ENG;
        String str = siteLang.trim();
        if (str.equals(""))
            return __LANG_ENG;
        if (str.equalsIgnoreCase("IT"))
            return __LANG_ITA;
        if (str.equalsIgnoreCase("EN"))
            return __LANG_ENG;
        if (str.equalsIgnoreCase("FR"))
            return __LANG_FRA;
        if (str.equalsIgnoreCase("DE"))
            return __LANG_DEU;
        
        return __LANG_ENG;
    }

    /**
     * Convert hashed value from bytes to hex string (uppercase)
     * @param bytes
     * @return
     */
    private static String toHexString (byte[] b)
    {
        char hexDigit[] = {'0', '1', '2', '3', '4', '5', '6', '7',
                           '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        StringBuffer buf = new StringBuffer();
        for (int j=0; j<b.length; j++) {
            buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
            buf.append(hexDigit[b[j] & 0x0f]);
        }
        return buf.toString();
    }
    
    
    private static void append (StringBuffer buff, Object value)
    {
        if (buff == null)
            return;
        if (value == null)
            return;
        buff.append(value.toString().trim());
    }
}

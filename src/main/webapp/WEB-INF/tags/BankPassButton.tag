<%@ tag
  import="com.mortbay.iwiki.*,it.colletta.*,it.colletta.reservation.*, it.colletta.payment.*, java.util.*,java.math.*"
%>
<%@ attribute name="resId" %>
<%@ attribute name="name" %>
<%@ attribute name="value"%>
<%@ attribute name="enabled" type="java.lang.Boolean" %>
<%@ attribute name="test" type="java.lang.Boolean" %>

<% 


BigDecimal dvalue = new BigDecimal(value).setScale(2);
        
   if (enabled==null || enabled.booleanValue())
   {
   /* NUMORD will be the reservation id plus a number to make it unique because BankPass does not */
   /* allow more than one payment with the same id. To make it unique we concatenate the resId with */
   /* the number of history entries (which increments each payment) */
   ReservationData rd = ReservationManager.getInstance().findReservation(resId);
   List history = rd.getHistory();
   request.setAttribute("NUMORD", resId+(history==null?"":"-"+history.size()));
   
   /* id of Colletta as far as the bank is concerned (CRN) */
   request.setAttribute("IDNEGOZIO", Configuration.getInstance().getProperty("bankpass.crn"));
      
   /* IMPORTO has to be in eurocents */

   request.setAttribute("IMPORTO", dvalue.scaleByPowerOfTen(2).round(new MathContext(0)).toBigInteger().toString());
      
   /* currency is ISO value 978=Euro */   
   request.setAttribute ("VALUTA", "978");
   
   /* set the url for the client to be redirected to after payment */
   request.setAttribute("URLDONE", Configuration.getInstance().getProperty("colletta.website")+"/renting/book/");
   
   /* set the url when transaction is cancelled */
   request.setAttribute("URLBACK", Configuration.getInstance().getProperty("colletta.website")+"/renting/book/");
   
   /* set the url to receive payment notification */
   request.setAttribute("URLMS", Configuration.getInstance().getProperty("colletta.website")+"/payment");
   
   /* set the field to control clearing  of the amount */
   request.setAttribute("TCONTAB", "I");
    
   /* set the field to control immediate verification with VISA etc*/
   request.setAttribute("TAUTOR", "I");
   
   /* calculate the authentication field */
   request.setAttribute("MAC", BankPass.calculateSendingMAC (request));
   
   /* email address to post payment notifications to */
   request.setAttribute ("EMAILESERC", "payments@colletta-it.com");
   
   /* map the language */
   String lang;
   Object o = session.getAttribute("lang");
   if (o == null || o.equals(""))
       lang = "en";
   else
       lang = o.toString();
           
   if (lang.equalsIgnoreCase("it"))
       lang = "ITA";
   else if (lang.equalsIgnoreCase("de"))
       lang = "DEU";  
   else if (lang.equalsIgnoreCase("fr"))
       lang = "FRA";
   else
       lang = "EN";
   
   lang = lang.toUpperCase();
       
   request.setAttribute("LINGUA", lang);

   request.setAttribute ("BANKFORM", Configuration.getInstance().getProperty("bankpass.url"));
   
%>

<form action="${BANKFORM}" method="POST"  id="bankpass${name}">
<input type="hidden" name="NUMORD" value="${NUMORD}">
<input type="hidden" name="IDNEGOZIO" value="${IDNEGOZIO}">
<input type="hidden" name="IMPORTO" value="${IMPORTO}">
<input type="hidden" name="VALUTA" value="${VALUTA}">
<input type="hidden" name="URLDONE" value="${URLDONE}">
<input type="hidden" name="URLBACK" value="${URLBACK}">
<input type="hidden" name="URLMS" value="${URLMS}">
<input type="hidden" name="TCONTAB" value="${TCONTAB}">
<input type="hidden" name="TAUTOR" value="${TAUTOR}">
<input type="hidden" name="MAC" value="${MAC}">
<input type="hidden" name="LINGUA" value="${LINGUA}">
<input type="hidden" name="EMAILESERC" value="${EMAILESERC}">
<input type="image" src="${contextPath}/${lang}/images/${name}.gif" border="0" name="submit" alt="Pay ${name} with BankPass"><jsp:doBody/></form>
<% } %>

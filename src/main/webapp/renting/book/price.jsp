<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.mortbay.iwiki.*,it.colletta.*,it.colletta.reservation.*,java.util.*" %>
<%@ taglib prefix="tag" tagdir="/WEB-INF/tags" %>


<% 
    Apartment apartment=(Apartment)request.getAttribute("apartment");
    BookingFilter.Reservation reservation=(BookingFilter.Reservation)session.getAttribute("reservation");
    User user = (User)session.getAttribute("user");

    Boolean owned=(Boolean)request.getAttribute("owned");
    Boolean managed=(Boolean)request.getAttribute("managed");
    Boolean view=(Boolean)request.getAttribute("view");
    Boolean ro=(Boolean)request.getAttribute("ro");
    Boolean rro=(Boolean)request.getAttribute("rro");

    Map properties = (Map) request.getAttribute("properties");
%>


<% if (reservation.getAptId()==null) { %>
<tr>
  <td class="Label">${properties.H_discountCode}:</td> 
  <td class="Form"><tag:textInput style="Form" name="discountCode" value="${reservation.discountCode}" size="9" readonly="${ro}"/></td>
  <td class="Form">&nbsp;</td>
  <td class="Form">&nbsp;</td>
</tr>

<% }
   else
   {
%>

<% 
if (managed.booleanValue() || owned.booleanValue() || user!=null && user.isViewAll() ) 
{ 
%>
<tr>
  <td class="Label">${properties.H_basis}:</td> 
  <td class="FormBasis" colspan="3">${reservation.priceBasis}</td>
</tr>
<tr>
  <td class="Label">${properties.H_commission}:</td> 
  <td class="Form"><tag:textInput style="Form" name="commissionPercent" value="${reservation.commissionPercent}" size="2" readonly="${rro || !managed}"/>%</td>
  <td class="Form"></td>
  <td class="Form"></td>
</tr>
<% } %>


<tr>
  <td class="Label">${properties.H_price}:</td> 
  <td class="FormNum">&euro;&nbsp;${reservation.price}</td>
  <td class="Form" >&nbsp;&nbsp;&nbsp;(10${properties.M_ivato})</td>
  <td class="Form">${properties.H_discountCode}:&nbsp;<tag:textInput style="Form" name="discountCode" value="${reservation.discountCode}" size="12" readonly="${rro}"/></td>
</tr>

<%
  int i=0;
  Iterator adjs = reservation.getAdjustments().iterator();
  while (adjs.hasNext())
  {
    Adjustment adjustment = (Adjustment)adjs.next();
    request.setAttribute("adjustment",adjustment);
    boolean boardOption = ("O_Colazione".equals(adjustment.getType()) || "O_MezzaPensione".equals(adjustment.getType()) || "O_Pensione".equals(adjustment.getType()));
%> 
    <tr>
    <td class="Label"><a class="info" href="#">${properties[adjustment.type]}<span>${properties[adjustment.typeInfo]}</span></a>:</td>
    <td class="FormNum">&euro;&nbsp;${adjustment.amount}</td>
    <td class="Form">
      <% if (!rro.booleanValue() && managed.booleanValue() &&  !boardOption) 
         { %><input type="checkbox" checked name="adjustment<%=i%>"/><%}%>&nbsp;&nbsp;&nbsp;
       
      <% if (adjustment.isTaxIncluded()) { %> (${adjustment.iva}${properties.M_ivato})<%}%>
         
    </td>
    <td class="Form">${adjustment.comment}</td>
    </tr>
<%i++;}%>


<% if (!rro.booleanValue() && managed.booleanValue() ) { %>

<tr>
  <td class="Label">
  <select name="adjType">
      <Option value="Discount">${properties.Discount}</Option>
      <Option value="Extra">${properties.Extra}</Option>
      <Option value="Service">${properties.Service}</Option>
      <Option value="Owner">${properties.Owner}</Option>
      <Option value="O_Welcome">${properties.O_Welcome}</Option>
      <Option value="O_WelcomeBig">${properties.O_WelcomeBig}</Option>
      <Option value="O_Osteria">${properties.O_Osteria}</Option>
      <Option value="O_Eoffice">${properties.O_Eoffice}</Option>
      <Option value="O_Late">${properties.O_Late}</Option>
    </select></td>  
  <td class="FormNum">&euro;&nbsp;&nbsp;<tag:textInput style="FormNum" name="adjAmount" value="" size="7" readonly="${rro || !managed}"/></td>
  <td class="Form" colspan="2"><tag:textInput style="Form" name="adjComment" value="" size="30" readonly="${rro || !managed}"/></td>
</tr> 


<% } %>




<tr>
  <td class="Label">${properties.H_total}:</td> 
  <td class="FormTotal">&euro;&nbsp;${reservation.total}</td>
  <td class="Form">&nbsp;</td>
  <td class="Form">&nbsp;</td>
</tr>  

<% if (	(reservation.getStatus()==null ||
         ReservationStatus.REQUESTED.equals(reservation.getStatus()) ||
         ReservationStatus.APPROVED.equals(reservation.getStatus()))) 
   { %>
<tr>
  <td class="Label">${properties.H_deposit}:</td>  
  <td class="FormNum">&euro;&nbsp;${reservation.deposit}</td>
  <td class="Form"></td>
  <td class="Form">
<%  
     if (!managed.booleanValue() && reservation.getId()!=null && ReservationStatus.APPROVED.equals(reservation.getStatus()) && reservation.getPaid().compareTo(reservation.getDeposit())<0)
     {
%>  
  	   <img class='fakesubmit' src="${contextPath}/images/cartasideposit.gif" onclick="getElementById('xpaydeposit').submit()"/>
<%
     }
%>
  
  </td>
</tr>
<% } %>

<tr>
  <td class="Label">${properties.H_paid}:</td>  
  <td class="FormNum">&euro;&nbsp;${reservation.paid}</td>
  <td class="Form">&nbsp;</td>
  <td class="Form">&nbsp;</td>
</tr>

<tr>
  <td class="Label">${properties.H_due}:</td>  
  <td class="FormTotal">&euro;&nbsp;${reservation.owing}</td>
  <td class="FormNum">
<% 
    if (reservation.getId() != null &&  !managed.booleanValue() &&
        (ReservationStatus.APPROVED.equals(reservation.getStatus())||ReservationStatus.CONFIRMED.equals(reservation.getStatus())) &&
         reservation.getPaid().compareTo(reservation.getTotal())<0) 
    { 
%>
       <a href="bonifico">${properties.M_altPayment}</a>
  </td>
  <td class="Form">
     <img class='fakesubmit' src="${contextPath}/images/cartasitotal.gif" onclick="getElementById('xpaytotal').submit()"/>
<%
    }
    else
    {
%>
   </td><td>
<%
    }
%>
  </td>


</tr>

<% 
if (managed.booleanValue() && reservation.getStatus()!=null) 
{ 
%>
<tr>
  <td class="Label">${properties.H_payment}:</td>  
  <td class="FormNum">&euro;&nbsp;<tag:textInput style="FormNum" name="payment" value="0.00" size="7" readonly="false"/></td>
  <td class="Label">${properties.H_paymentRef}:</td>
  <td class="Form"><tag:textInput style="Form" name="paymentRef" size="12" readonly="false"/></td>
</tr>

<% } 
if (managed.booleanValue() || (reservation.getStatus()==null || ReservationStatus.REQUESTED.equals(reservation.getStatus()))) { %>
<tr>
<td class="Label">&nbsp;</td>
<td class="FormWrap" colspan="3">
<br/>
<tag:Button name="update" image="update.gif" enabled="${!rro}">&nbsp;</tag:Button>


<%
   if (!rro.booleanValue() && managed.booleanValue()) 
   {  
%>
   &nbsp;<input type="checkbox" name="recalc"/>&nbsp;${properties.H_recalc}
<% } %>

</tr>
<%} %>



<% } %>


<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.mortbay.iwiki.*,it.colletta.*,it.colletta.reservation.*,java.util.*" %>
<%@ taglib prefix="tag" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="qsearch" scope="session" class="it.colletta.QSearch" />
<jsp:useBean id="today" scope="session" class="com.mortbay.iwiki.YyyyMmDd" />
<% 
    Apartment apartment=(Apartment)request.getAttribute("apartment");
    BookingFilter.Reservation reservation=(BookingFilter.Reservation)session.getAttribute("reservation");
    
    User user = (User)session.getAttribute("user");
    Boolean owned=(user!=null && apartment!=null && user.owns(apartment.getName()))
      ?Boolean.TRUE:Boolean.FALSE;
    Boolean managed=(user!=null && 
                     ( user.isAdmin() ||
                      (apartment==null && user.isManager()) ||
                      (apartment!=null && (user.manages(apartment.getName()) )) ||
                      (reservation!=null && owned.booleanValue() && "Unavailable".equals(reservation.getPriceBasis()))
                      ))
      ? Boolean.TRUE:Boolean.FALSE;
    Boolean view = (owned.booleanValue() || managed.booleanValue() || (user!=null && user.isViewAll()))? Boolean.TRUE:Boolean.FALSE;
  
    request.setAttribute("owned",owned);
    request.setAttribute("managed",managed);
    request.setAttribute("view",view);
    
    Map properties = (Map) request.getAttribute("properties");

    Boolean ro=Boolean.FALSE;
    Boolean rro=Boolean.FALSE;
    if (reservation==null)
    {
	  response.sendRedirect(".");
	  return;
    }

    if (reservation.getId()!=null)
    { 
      ro=managed.booleanValue()?Boolean.FALSE:Boolean.TRUE; 
    }
    else
    {
      rro=reservation.isReady()?Boolean.TRUE:Boolean.FALSE;
      ro=rro;
    }
    
    request.setAttribute("ro",ro);
    request.setAttribute("rro",rro);
%>

<ul>
<%
Iterator iter=reservation.getErrors();
while(iter.hasNext())
{
    String error=(String)iter.next();
    String msg=(String)properties.get("M_"+error);
    if (msg!=null)error=msg;
%>
<li><span class="Error"><%=error%></span></li>
<% } 
iter=reservation.getWarnings();
while(iter.hasNext())
{
    String warning=(String)iter.next();
    String msg=(String)properties.get("M_"+warning);
    if (msg!=null)warning=msg;
    
    
%>
<li><span class="Warning"><%=warning%></span></li>
<% }%>
</ul>

<% if ("xpay".equals(request.getParameter("redirect")))
{%>
<span class="Error">Redirecting to XPay...</span>
<% } %>

<table class="Form" >
<form method="POST" name="book"><input type="hidden" name="form" value="book"/>

<tr>

<td colspan="3" class="FormSection"><div class="SubTitle">${properties.H_dates}: ${reservation.id}</div></td>

<td class="FormR" colspan="1" rowspan="6">
<% if (apartment!=null ) { %>
<tag:Calendar apt="${apartment.page}" highlight="${reservation.reservationData}" />
<input type="hidden" name="id" value="${reservation.id}" />
<% } else { %>
<tag:Calendar highlight="${reservation.reservationData}" />
<% } %>
<table class="Cal Legenda">
<tr><td class="Cal">&nbsp;&nbsp;</td><td class="CalAltDay">${properties.M_Cal}</td></tr>
<tr><td class="CalR">&nbsp;&nbsp;</td><td class="CalAltDay">${properties.M_CalR}</td></tr>
<tr><td class="CalB">&nbsp;&nbsp;</td><td class="CalAltDay">${properties.M_CalB}</td></tr>
<tr><td class="CalX">&nbsp;&nbsp;</td><td class="CalAltDay">${properties.M_CalX}</td></tr>
</table>
</td>
</tr>


<jsp:include page="dates.jsp" flush="true"/>

<% if (managed.booleanValue()||owned.booleanValue())
{ %>
<tr>
<td class="Label">&nbsp;</td>
<td class="FormWrap" colspan="3">
<br/>
<tag:Button name="update" image="update.gif" enabled="${managed && !rro && (reservation.status=='REQUESTED' || reservation.status=='APPROVED' || reservation.status=='CONFIRMED'|| reservation.status=='CHECKIN' || reservation.status=='CANCELLED' || reservation.status==null) }">&nbsp;</tag:Button>
<tag:Button name="approve" image="approve.gif" enabled="${managed && reservation.id != NULL && (reservation.status=='REQUESTED' || reservation.status=='CANCELLED' || reservation.status=='EXPIRED') }">&nbsp;</tag:Button>
<tag:Button name="confirm" image="confirm.gif" enabled="${managed && ( reservation.status=='REQUESTED' || reservation.status=='APPROVED' ) }">&nbsp;</tag:Button>
<tag:Button name="cancel" image="cancel.gif" enabled="${managed && reservation.id != NULL && ((managed && reservation.status=='CONFIRMED') || reservation.status=='REQUESTED' || reservation.status=='APPROVED' ) }"> </tag:Button>
<tag:Button name="unavailable" image="unavailable.gif" enabled="${!rro && reservation.aptId !=null && ( managed || owned ) && reservation.status==null }">&nbsp;</tag:Button>
</td></tr>
<% } else if (reservation.getStatus()==null || ReservationStatus.REQUESTED.equals(reservation.getStatus())) { %>
<tr>
<td class="Label">&nbsp;</td>
<td class="FormWrap" colspan="3">
<tag:Button name="update" image="update.gif" enabled="${!rro}">&nbsp;</tag:Button>
</tr>
<%} %>

<% if (!rro.booleanValue() && (managed.booleanValue() || new YyyyMmDd().daysTo(reservation.getStartDate())>1)) { %>
<tr id="offers"><td class="FormSection" colspan="4"><div class="SubTitle">${properties.H_offers}:</div></td></tr>
<jsp:include page="offers.jsp" flush="true"/>
<% } %> 

<tr id="money"><td class="FormSection" colspan="4"><div class="SubTitle">${properties.H_payments}:</div></td></tr>
<jsp:include page="price.jsp" flush="true"/>

<tr><td class="FormSection" colspan="4"><div class="SubTitle">${properties.H_details}:</div></td></tr>
<jsp:include page="client.jsp" flush="true"/>

<% if (rro.booleanValue())
{ %>

<tr id="conditions"><td class="FormSection" colspan="4">&nbsp;<div class="SubTitle">${properties.H_conditions}:</div></td></tr>
<tr> 
  <td class="Label">&nbsp;</td>
  <td class="Form" colspan="2"><input type="checkbox" name="acceptedConditions"/>&nbsp;${properties.M_acceptConditions}<a href="../conditions">${properties.M_conditions}</a></td>
  <td class="Form"></td>
</tr>

<% } %>


<tr>
<td class="Label">&nbsp;</td>
<td class="Form" colspan="3">
&nbsp;<br/>
<tag:Button name="request" image="request.gif" enabled="${rro}"> </tag:Button>
<tag:Button name="update" image="update.gif" enabled="${rro || reservation.status=='REQUESTED' || reservation.status=='APPROVED' || reservation.status=='CONFIRMED'|| reservation.status=='CHECKIN' || reservation.status=='CANCELLED' || reservation.status==null }"> </tag:Button>
<tag:Button name="continue" image="continue.gif" enabled="${!rro && reservation.id == NULL}"> </tag:Button>
<tag:Button name="approve" image="approve.gif" enabled="${managed && reservation.id != NULL && (reservation.status=='REQUESTED' || reservation.status=='CANCELLED' || reservation.status=='EXPIRED')}"> </tag:Button>
<tag:Button name="confirm" image="confirm.gif" enabled="${managed && ( reservation.status=='REQUESTED' || reservation.status=='APPROVED' ) }"> </tag:Button>
<tag:Button name="cancel" image="cancel.gif" enabled="${reservation.id != NULL && ((managed && reservation.status=='CONFIRMED') || reservation.status=='REQUESTED' || reservation.status=='APPROVED' ) }"> </tag:Button>

</td></tr>

</form>

<tr>
<td class="Form" colspan="4"><br/>
<div class="hidden">

<tag:XPayButton resId="${reservation.id}" name="deposit" value="${reservation.depositOwing}" enabled="${!managed && reservation.id != NULL && (reservation.status=='APPROVED' ) && reservation.depositOwing > 0 }"></tag:XPayButton>
<tag:XPayButton resId="${reservation.id}" name="total" value="${reservation.owing}" enabled="${!managed && reservation.active && reservation.status!='REQUESTED' && reservation.owing > 0 }"></tag:XPayButton>
<% if (reservation.getId() != null && reservation.getStatus()!=null && reservation.getPaid().compareTo(reservation.getOwing())<0) { %>
<a href="bonifico">${properties.M_altPayment}</a>
<% } %>
</div>

<form method="POST" name="book">
<input type="hidden" name="form" value="book"/>
<br/>
<tag:Button name="new" image="new.gif" enabled="true"><br/> </tag:Button>
</form>



</td></tr>

</table>

<ul>
<%
iter=reservation.getErrors();
while(iter.hasNext())
{
    String error=(String)iter.next();
    String msg=(String)properties.get("M_"+error);
    if (msg!=null)error=msg;
%>
<li><span class="Error"><%=error%></span></li>
<%
}
iter=reservation.getWarnings();
while(iter.hasNext())
{
    String warning=(String)iter.next();
    String msg=(String)properties.get("M_"+warning);
    if (msg!=null)warning=msg;
%>
<li><span class="Warning"><%=warning%></span></li>
<% } %>
</ul>



<% if ("xpay".equals(request.getParameter("redirect")))
{%>
<script type="text/javascript">
function gotoXPay() {
document.getElementById('xpaydeposit').submit();
}
setTimeout('XPay()',1000);
</script>
<% } %>


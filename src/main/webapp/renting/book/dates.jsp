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


<tr>
  <td class="Label">${properties.H_status}:</td>
  <td colspan="2" class="FormWrap">
    <span class="Info"><b>${reservation.status}</b>
    <%
      if (reservation.getId()==null && rro.booleanValue())
      {
	  	String s=(String)properties.get("M_BookIt");
	  	if (s!=null)
	     out.print("<span class=\"Error\">"+s+"</span>");
      }
    %>
    <br/>
    <%
      String s = (String)properties.get("M_"+reservation.getStatus());
      if (s!=null)
	  	out.print(s);
      if (ReservationStatus.APPROVED.equals(reservation.getStatus()))
      {
	  s=(String)properties.get("M_depositDue");
	  if (s!=null)
	      out.print(s);
      }
    %>
      
    <% if (ReservationStatus.CONFIRMED.equals(reservation.getStatus())) {  %>
      <br/>&nbsp;<br/>
      <a class="voucher" href="voucher.jsp" target="_blank"><img src="${contextPath}/${lang}/images/voucher.gif"></a>
      <br/>
      <i><small>(${properties.M_voucherExplanation})</small></i>
      <br/>&nbsp;
    <% } %>
    </span>
  </td>
</tr>


<tr>
  <td class="Label">${properties.H_apartment}:</td>
  <td class="Form" colspan="2"><tag:AptSelect style="Form" name="aptId" value="${reservation.aptId}" readonly="${ro}" /></td>
</tr>


<tr>
  <td class="Label">${properties.H_from}:</td>
  <td class="Form" colspan="2">
    <tag:IntSelect style="Form" name="startDd"   min="1" max="31" value="${reservation.startDd}" readonly="${ro}"/>
    <tag:MonthSelect style="Form" name="startMm" locale="${locale}" value="${reservation.startMm}" readonly="${ro}"/>
    <tag:IntSelect style="Form" name="startYyyy" min="${today.yyyy - 1}" max="${today.yyyy + 2}" value="${reservation.startYyyy}" readonly="${ro}" />
  </td>
</tr>


<tr>
  <td class="Label">${properties.H_to}:</td>
  <td class="Form" colspan="2">
    <tag:IntSelect style="Form" name="endDd"   min="1" max="31" value="${reservation.endDd}" readonly="${ro}"/>
    <tag:MonthSelect style="Form" name="endMm" locale="${locale}" value="${reservation.endMm}" readonly="${ro}"/>
    <tag:IntSelect style="Form" name="endYyyy" min="${today.yyyy - 1}" max="${today.yyyy + 2}" value="${reservation.endYyyy}" readonly="${ro}"/>
  </td>
</tr>


<% if (user!=null && (user.isManager() || user.isOwner() || user.isViewAll() )) { %>

<tr>
  <td class="Label">${properties.H_referer}:</td>
  <td class="Form" colspan="2"><tag:textInput style="Form" name="referer" value="${reservation.referer}" size="40" readonly="${ro}"/></td>
</tr>

<% } else { %>

<tr>
  <td class="Label">&nbsp;</td>
  <td class="Form" colspan="2">&nbsp;</td>
</tr>

<% } %>





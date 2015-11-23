<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.mortbay.iwiki.*,it.colletta.*,it.colletta.reservation.*,java.util.*,java.math.BigDecimal" %>
<%@ taglib prefix="tag" tagdir="/WEB-INF/tags" %>
<% 
    BookingFilter.Reservation reservation=(BookingFilter.Reservation)session.getAttribute("reservation");
    Apartment apartment=Apartment.getApartment(reservation.getAptId());
    request.setAttribute("apartment",apartment);
%>
<html>
<head>
<title>Voucher</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link rel="alternate" media="print" href="voucher.jsp">
</head>
<body onload="window.print()">

<table border="2" cellspacing="0" cellpadding="3" width="90%">
<tr>
  <td align="center" colspan="3"><b><big>Colletta di Castelbianco</big></b><br/>Voucher di Affitti / Voucher for Rental www.colletta.it
  <br/>
Borgo Telematico S.r.l. Frazione Colletta, 17030 SV Italia.<br/> Partita Iva e Codice fiscale:01459790091
  </td>
<tr>
  <th align="right" width="10%">Reference:</th>
  <td colspan="2">${reservation.id}</td>
</tr>

<tr>
  <th align="right" width="10%">Nome/Name:</th>
  <td colspan="2">${reservation.name}</td>
</tr>

<tr>
  <th align="right" width="10%">Quanti/Numbers:</th>
  <td colspan="2">${reservation.adults} adulti/adults, ${reservation.children} bimbini/children, ${reservation.infants} neo nati/infants, </td>
</tr>

<tr>
  <th align="right" width="10%">Date/Dates:</th>
  <td colspan="2">a/from: ${reservation.startDate} - da/to:${reservation.endDate}.  <%=reservation.getStartDate().daysTo(reservation.getEndDate())%> notte/nights</td>
</tr>

<tr>
  <th align="right" width="10%">Appartamento/Apartment:</th>
  <td colspan="2">${reservation.aptId}</td>
</tr>

<tr>
  <th align="right" width="10%">Affitti/Rental:</th>
  <td align="right" width="10%">&euro;<%=reservation.getPrice()%></td>
  <td>&nbsp;</td>
</tr>


<%
  Iterator adjs = reservation.getAdjustments().iterator();
  while (adjs.hasNext())
  {
    Adjustment adjustment = (Adjustment)adjs.next();
    request.setAttribute("adjustment",adjustment);
%>
<tr>
  <th align="right" width="10%">${adjustment.type}:</th>
  <td align="right" width="10%">&euro;${adjustment.amount}</td>
  <td>
  <%  if (adjustment.isTaxIncluded()) {%>${adjustment.iva}% iva/VAT <% }  %>
  ${adjustment.comment}&nbsp;
  </td>
</tr>
<%
  }
%>


<tr>
  <th align="right" width="10%">Totale/Total:</th>
  <td align="right" width="10%"><b>&euro;${reservation.total}</b></td>
  <td>&nbsp;</td>
</tr>
<tr>
  <th align="right" width="10%">Pagato/Paid:</th>
  <td align="right" width="10%">&euro;${reservation.paid}</td>
  <td>&nbsp;</td>
</tr>
<tr>
  <th align="right" width="10%">Da pagare/Due:</th>
  <td align="right" width="10%"><b>&euro;${reservation.total-reservation.paid}</b></td>
  <td>&nbsp;</td>
</tr>

<tr>
  <th align="right" width="10%">Stampato/Printed:</th>
  <td colspan="2"><%=new YyyyMmDdHM()%></td>
</tr>

<tr>
<td>
</td>
</tr>

<tr>
  <td colspan="3"><b><p>Conditions</b></p><p><font size="-1"><jsp:include page="/renting/conditions/text.jsp" flush="true"/></font></p></td>
</tr>



</table>

</body>
</html>

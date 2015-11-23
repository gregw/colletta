<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.mortbay.iwiki.*,it.colletta.*,it.colletta.reservation.*,java.util.*" %>
<%@ taglib prefix="tag" tagdir="/WEB-INF/tags" %>


<% 
    Apartment apartment=(Apartment)request.getAttribute("apartment");
    BookingFilter.Reservation reservation=(BookingFilter.Reservation)session.getAttribute("reservation");
    User user = (User)session.getAttribute("user");

    Map properties = (Map) request.getAttribute("properties");
%>

<% 
/*
if (!reservation.hasAdjustments("O_Welcome") &&  !reservation.hasAdjustments("O_WelcomeBig")) 
{%>

<tr>
<td class="Label"><a class="info" href="#">${properties.O_Welcome}<span>${properties.I_O_Welcome}</span>:</td>
<td class="FormNum">${properties.O_WelcomePrice}</td>
<td  colspan="2"><input type="checkbox" name="offerWelcome"/>&nbsp;${properties.O_WelcomeBlurb}. <a href="welcome" target="_blank">${properties.O_more}</a></td>
</tr>

<tr>
<td class="Label"><a class="info" href="#">${properties.O_WelcomeBig}<span>${properties.I_O_WelcomeBig}</span>:</td>
<td class="FormNum">${properties.O_WelcomeBigPrice}</td>
<td  colspan="2"><input type="checkbox" name="offerWelcomeBig"/>&nbsp;${properties.O_WelcomeBigBlurb}. <a href="welcome" target="_blank">${properties.O_more}</a></td>
</tr>

<% }

if (!reservation.hasAdjustments("O_Osteria"))
{
%>

<tr>
<td class="Label"><a class="info" href="#">${properties.O_Osteria}<span>${properties.I_O_Osteria}</span>:</td>
<td class="FormNum">${properties.O_OsteriaPrice}</td>
<td  colspan="2"><input type="checkbox" name="offerOsteria"/>&nbsp;${properties.O_OsteriaBlurb}.</td>
</tr>

<% }
if (!reservation.hasAdjustments("O_Eoffice"))
{
%>

<tr>
<td class="Label"><a class="info" href="#">${properties.O_Eoffice}<span>${properties.I_O_Eoffice}</span>:</td>
<td class="FormNum">${properties.O_EofficePrice}</td>
<td  colspan="2"><input type="checkbox" name="offerEoffice"/>&nbsp;${properties.O_EofficeBlurb}. <a href="../../eoffice/" target="_blank">${properties.O_more}</a></td>
</tr>

<% }
*/
/*
if (!reservation.hasAdjustments("O_Late"))
{
%>

<tr>
<td class="Label"><a class="info" href="#">${properties.O_Late}<span>${properties.I_O_Late}</span>:</td>
<td class="FormNum">${properties.O_LatePrice}</td>
<td  colspan="2"><input type="checkbox" name="offerLate"/>&nbsp;${properties.O_LateBlurb}. <a href="../conditions/" target="_blank">${properties.O_more}</a></td>
</tr>

<% } */ %>



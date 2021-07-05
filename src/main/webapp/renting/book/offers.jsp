<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.mortbay.iwiki.*,it.colletta.*,it.colletta.reservation.*,java.util.*" %>
<%@ taglib prefix="tag" tagdir="/WEB-INF/tags" %>


<% 
    Apartment apartment=(Apartment)request.getAttribute("apartment");
    BookingFilter.Reservation reservation=(BookingFilter.Reservation)session.getAttribute("reservation");
    User user = (User)session.getAttribute("user");

    Map properties = (Map) request.getAttribute("properties");

    boolean colazione = reservation.hasAdjustments("O_Colazione");
    boolean mezzaPensione = reservation.hasAdjustments("O_MezzaPensione");
    boolean pensione = reservation.hasAdjustments("O_Pensione");
    boolean noBoard = (!colazione && !mezzaPensione && !pensione);

%>
<tr>
<td class="Label"><a class="info" href="#">${properties.O_NoBoard}<span>${properties.I_O_NoBoard}</span>:</td>
<td class="FormNum">&nbsp;</td>
<td  colspan="2">
<%
    if (noBoard)
    {
%>
<input type="radio" name="offerBoard" value="offerNone" checked/>${properties.O_NoBoardBlurb}</td>
<%
    }
    else
    {
%>
<input type="radio" name="offerBoard" value="offerNone"/>${properties.O_NoBoardBlurb}</td>
<%
    }
%>
</tr>

<tr>
<td class="Label"><a class="info" href="#">${properties.O_Colazione}<span>${properties.I_O_Colazione}</span>:</td>
<td class="FormNum">${properties.O_ColazionePrice}&nbsp;${properties.O_perDay}</td>
<%
    if (colazione)
    {
%>
<td  colspan="2"><input type="radio" name="offerBoard" value="offerColazione" checked/>${properties.O_ColazioneBlurb}</td>
<%
    }
    else
    {
%>
<td  colspan="2"><input type="radio" name="offerBoard" value="offerColazione"/>${properties.O_ColazioneBlurb}</td>
<%
    }
%>
</tr>

<tr>
<td class="Label"><a class="info" href="#">${properties.O_MezzaPensione}<span>${properties.I_O_MezzaPensione}</span>:</td>
<td class="FormNum">${properties.O_MezzaPensionePrice}&nbsp;${properties.O_perDay}</td>

<%
    if (mezzaPensione)
    {
%>
<td  colspan="2"><input type="radio" name="offerBoard" value="offerMezzaPensione" checked/>${properties.O_MezzaPensioneBlurb}</td>
<%
    }
    else
    {
%>
<td  colspan="2"><input type="radio" name="offerBoard" value="offerMezzaPensione"/>${properties.O_MezzaPensioneBlurb}</td>
<%
    }
%>
</tr>     
      
<tr>
<td class="Label"><a class="info" href="#">${properties.O_Pensione}<span>${properties.I_O_Pensione}</span>:</td>
<td class="FormNum">${properties.O_PensionePrice}&nbsp;${properties.O_perDay}</td>
<%
    if (pensione)
    {
%>
<td colspan="2"><input type="radio" name="offerBoard" value="offerPensione" checked />${properties.O_PensioneBlurb}</td>
<%
	}
	else
	{
%>
	<td colspan="2"><input type="radio" name="offerBoard" value="offerPensione" />${properties.O_PensioneBlurb}</td>
<%
    }
%>
</tr> 

<tr>
<td class="Label"><a class="info" href="#">${properties.O_Yoga}<span>${properties.I_O_Yoga}</span>:</td>
<td class="FormNum">${properties.O_enquireForPrice}</td>
<td  colspan="2">${properties.O_YogaBlurb}</td>
</tr>
<tr>
<td class="Label"><a class="info" href="#">${properties.O_Fitness}<span>${properties.I_O_Fitness}</span>:</td>
<td class="FormNum">${properties.O_enquireForPrice}</td>
<td  colspan="2">${properties.O_FitnessBlurb}</td>
</tr>
<tr>
<td class="Label"><a class="info" href="#">${properties.O_EBike}<span>${properties.I_O_EBike}</span>:</td>
<td class="FormNum">${properties.O_enquireForPrice}</td>
<td  colspan="2">${properties.O_EBikeBlurb}</td>
</tr>
<tr>
<td class="Label"><a class="info" href="#">${properties.O_Orienteering}<span>${properties.I_O_Orienteering}</span>:</td>
<td class="FormNum">${properties.O_enquireForPrice}</td>
<td  colspan="2">${properties.O_OrienteeringBlurb}</td>
</tr>
<tr>
<td class="Label"><a class="info" href="#">${properties.O_Climbing}<span>${properties.I_O_Climbing}</span>:</td>
<td class="FormNum">${properties.O_enquireForPrice}</td>
<td  colspan="2">${properties.O_ClimbingBlurb}</td>
</tr>

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



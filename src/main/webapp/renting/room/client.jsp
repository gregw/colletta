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
<td class="Label">${properties.H_adults}:</td>
<td class="Form" colspan="3"><tag:IntSelect style="Form" name="adults" min="1" max="2" value="${reservation.adults}" dft="2" readonly="${ro}"/>
<span class="Label">${properties.H_children}:</span><tag:IntSelect style="Form" name="children" min="0" max="2" value="${reservation.children}" dft="0" readonly="${ro}"/>
<span class="Label">${properties.H_infants}:</span><tag:IntSelect style="Form" name="infants" min="0" max="2" value="${reservation.infants}" dft="0" readonly="${ro}"/>
</td>
</tr>

<tr><td class="Label">${properties.H_name}:</td><td class="Form" colspan="3">
<tag:textInput style="Form" name="name" value="${reservation.name}" size="50" readonly="${ro}"></tag:textInput>
</td></tr>

<tr><td class="Label">${properties.H_address}:</td><td class="Form" colspan="3">
<tag:textInput style="Form" name="address" value="${reservation.address}" size="50" readonly="${ro}"/>
</td></tr>

<tr><td class="Label">${properties.H_country}:</td><td class="Form" colspan="1">
<tag:textInput style="Form" name="country" value="${reservation.country}" size="20" readonly="${ro}"/>

<td class="Label">${properties.H_postcode}:</td><td class="Form" colspan="1">
<tag:textInput style="Form" name="postcode" value="${reservation.postcode}" size="10" readonly="${ro}"/>
</td></tr>

<tr><td class="Label">${properties.H_email}:</td><td class="Form" colspan="1">
<tag:textInput style="Form" name="email" value="${reservation.email}" size="20" readonly="${ro}"></tag:textInput>

<td class="Label">${properties.H_phone1}:</td><td class="Form" colspan="1">
<tag:textInput style="Form" name="phone1" value="${reservation.phone1}" size="10" readonly="${ro}"></tag:textInput>
</td></tr>


<tr><td class="Label">${properties.H_notes}:</td><td class="Form" colspan="3">
<% if(rro.booleanValue()) { %>
${reservation.notes}
<% } else { %>
<textarea name="notes" class="notes" >${reservation.notes}</textarea><br/>
<% } %>
</td></tr>

<% if(managed.booleanValue()) { %>
<tr><td class="Label">${properties.H_notesPrivate}:</td><td class="Form" colspan="3">
<% if(rro.booleanValue()) { %>
${reservation.notesPrivate}
<% } else { %>
<textarea name="notesPrivate" class="notes" >${reservation.notesPrivate}</textarea><br/>
<% } %>
</td></tr>

<tr>
<td class="Label">${properties.H_history}:</td>
<td class="Form" colspan="3">
<textarea class="History">
<%
ListIterator history=reservation.getHistory().listIterator(reservation.getHistory().size());
while(history.hasPrevious())
{
    String hist = (String)history.previous();
    out.println(hist);
}
%>
</textarea>

</td>
</tr>

<%}%>





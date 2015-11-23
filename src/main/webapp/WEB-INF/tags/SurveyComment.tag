<%@ attribute name="subject" %>

<%
request.setAttribute("comment","comment_"+subject);
%>
<tr>
<td class="label">${properties['comment']}:</td>
<td>&nbsp;</td>
<td colspan="5"><input class="comment" type="text" name="comment_${subject}" value="${lastPOST[comment][0]}"/></td>
</tr>
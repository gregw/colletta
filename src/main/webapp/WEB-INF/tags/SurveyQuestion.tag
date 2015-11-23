<%@ attribute name="subject" %>
<%@ attribute name="name" %>

<%
request.setAttribute("sname",subject+"_"+name);
%>
<tr>
<td class="label">${properties[name]}:</th>
<td>&nbsp;</td>
<td class="tick"><input type="radio" name="${subject}_${name}" value="1" ${lastPOST[sname][0] == '1' ? 'checked="checked"' : '' }/></td>
<td class="tick"><input type="radio" name="${subject}_${name}" value="2" ${lastPOST[sname][0] == '2' ? 'checked="checked"' : '' }/></td>
<td class="tick"><input type="radio" name="${subject}_${name}" value="3" ${lastPOST[sname][0] == '3' ? 'checked="checked"' : '' } /></td>
<td class="tick"><input type="radio" name="${subject}_${name}" value="4" ${lastPOST[sname][0] == '4' ? 'checked="checked"' : '' }/></td>
<td class="tick"><input type="radio" name="${subject}_${name}" value="5" ${lastPOST[sname][0] == '5' ? 'checked="checked"' : '' }/></td>
</tr>
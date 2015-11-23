
<%@ tag
           body-content="empty"
%>

<%@ attribute name="style"  required="false" %>
<%@ attribute name="name"  required="true" %>
<%@ attribute name="min"  type="java.lang.Integer" required="true" %>
<%@ attribute name="max"  type="java.lang.Integer" required="true" %>
<%@ attribute name="value"  type="java.lang.Integer" required="false" %>
<%@ attribute name="dft"  type="java.lang.Integer" required="false" %>
<%@ attribute name="readonly"  type="java.lang.Boolean" required="false" %>


<% if (readonly!=null && readonly.booleanValue()){ %>
<span class=${style}>${value}</span>
<% } else { %>

<select class="${style}" name="${name}" >
<%
  int s=value.intValue();
  if (s<min.intValue() && dft!=null)
  	s=dft.intValue();
  	
  for (int i=min.intValue();i<=max.intValue();i++)
  {
      if (i==s)
      	out.print("<option selected=\"selected\">");
      else
        out.print("<option>");
      out.print(i);
      out.println("</option>");
  }
%>
</select>
<% } %>

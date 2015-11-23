
<%@ tag
           body-content="empty"
           import="com.mortbay.iwiki.*,it.colletta.*"
%>

<%@ attribute name="style"  required="false" %>
<%@ attribute name="name"  required="true" %>
<%@ attribute name="locale" type="java.util.Locale" required="true" %>
<%@ attribute name="value"  type="java.lang.Integer" required="false" %>
<%@ attribute name="readonly"  type="java.lang.Boolean" required="false" %>


<% 
int m=value.intValue();
if (m<1 || m>12)
  	m=1;
  	
if (readonly!=null && readonly.booleanValue()){ %>
<span class=${style}><%=YyyyMmDd.shortMonth(m,locale)%></span>
<% } else { %>

<select class="${style}" name="${name}" >
<%
  	
  for (int i=1;i<=12;i++)
  {
      out.print("<option value=\"");
      out.print(i);
      if (i==m)
      	out.print("\" selected=\"selected\">");
      else
        out.println("\">");
      
      out.print(YyyyMmDd.shortMonth(i,locale));
      out.println("</option>");
  }
%>
</select>
<% } %>

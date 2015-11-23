<%@ tag
           body-content="empty"
           import="com.mortbay.iwiki.*,it.colletta.*"
%>

<%@ attribute name="style"  required="false" %>
<%@ attribute name="name"  required="true" %>
<%@ attribute name="value"  type="java.lang.String" required="false" %>
<%@ attribute name="dft"  type="java.lang.Integer" required="false" %>
<%@ attribute name="readonly"  type="java.lang.Boolean" required="false" %>


<% 
   Apartment apt = Apartment.getApartment(value);
   if (apt!=null)
     request.setAttribute("aptId",apt.getName());
   if (readonly!=null && readonly.booleanValue()){ 
%>

<a class=${style} href="${contextPath}/renting/view/${aptId}"><%= apt==null?"---":apt.getDisplayName() %></a>

<% } else { %>

<select class="${style}" name="${name}" ><option>---</option>
<%
  String[] apartments = Apartment.getApartmentIds();
  
  for (int i=0;i<apartments.length;i++)
  {
      apt = Apartment.getApartment(apartments[i]);
      
      if (value!=null && value.equalsIgnoreCase(apartments[i]))
      	out.print("<option value=\""+apartments[i]+ "\" selected>"+apt.getDisplayName()+"</option>");
      else
        out.print("<option value=\""+apartments[i]+ "\">"+apt.getDisplayName()+"</option>");
  }
%>
</select>
<a href="${contextPath}/renting/view/${aptId}"><img src="${contextPath}/images/link.gif"/></a>
<% } %>

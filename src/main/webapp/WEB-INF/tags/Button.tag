<%@ attribute name="name" %>
<%@ attribute name="image" %>
<%@ attribute name="enabled" type="java.lang.Boolean" required="false" %>

<% if (enabled==null || enabled.booleanValue()){ %>
<input class="Button" type="image" src="${contextPath}/${lang}/images/${image}" name="${name}"  alt="[${name}]"/>
<jsp:doBody/>
<% } %>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.mortbay.iwiki.Page" %>

<% 
  String lang = (String) request.getAttribute("lang");
  Page p = (Page) request.getAttribute("page");
  if (p==null)
    return;
%>

<h1><%= p.getTitle(lang) %></h1>


<iframe width="95%" height=600 src="http://maps.google.com/maps?f=q&hl=en&q=colletta,+castelbianco,+17030,+SV,+Italia&sll=45.213004,7.382813&sspn=18.451087,31.948242&ie=UTF8&z=5&ll=45.39845,8.217773&spn=18.391857,31.948242&om=1&iwloc=addr"></iframe>

<jsp:include page="text.jsp" flush="true"/>




<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.mortbay.iwiki.Page" %>
<%@ page import="com.mortbay.iwiki.YyyyMmDd" %>

<% 
  String lang = (String) request.getAttribute("lang");
  Page p = (Page) request.getAttribute("page");
  Page r = p;
  while (r.getParent()!=null)
    r=r.getParent();

  request.setAttribute("map",r);
  request.setAttribute("level",new Integer(0));
%>

<h1><%= p.getTitle(lang) %></h1>

<jsp:include page="text.jsp" flush="true"/>
<jsp:include page="map.jsp" flush="true"/>

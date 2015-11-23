
<% 
  String lang = (String) request.getAttribute("lang");
  Page p = (Page) request.getAttribute("page");
  if (p==null)
    return;
%>

<h1><%= p.getTitle(lang) %></h1>


<%@ include file="seasoncal.jsp" %>



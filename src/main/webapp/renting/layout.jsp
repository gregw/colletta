<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.mortbay.iwiki.Page" %>

<% 
  String lang = (String) request.getAttribute("lang");
  Page p = (Page) request.getAttribute("page");
  if (p==null)
    return;
%>

<h1><%= p.getTitle(lang) %></h1>

<%
  String[] images = p.getImages();
  if (p.getProperty("images")==null)
  {
%>
    <div class="imageblock"> 
<%
    for (int i=0;i<(images.length-1);i++)
    {
       request.setAttribute("image",images[i]);
%>
       <img class="page" src="${image}"/>
<%  } %>
    </div>
<%} %>

<jsp:include page="text.jsp" flush="true"/>

<% 
  if (!"0".equals(p.getProperty("children")))
  {
    out.println("<div id=\"children\">");
    Page[] child = p.getChildren();
    for (int i=0;i<child.length;i++)
    {
      if ("hide".equals(child[i].getProperty("entry")))
          continue;
      request.setAttribute("child",child[i]);
%>
<a href="${child.dirName}"><%=child[i].getName(lang)%></a>&nbsp;-&nbsp;
<%
  String b=(String) child[i].getProperty(lang,"blurb");
  if (b!=null)
      out.println(b);
%>
<br/>
<%} 
  out.println("</div>");
} %>




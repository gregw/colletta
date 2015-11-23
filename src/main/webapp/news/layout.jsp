<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.mortbay.iwiki.Page" %>
<%@ page import="com.mortbay.iwiki.YyyyMmDd" %>

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
    for (int i=0;i<images.length;i++)
    {
       request.setAttribute("image",images[i]);
%>
       <img class="page" src="${image}"/>
<%  } %>
    </div>
<%} %>

<jsp:include page="text.jsp" flush="true"/>

<% 
  YyyyMmDd now = new YyyyMmDd();

  if (!"0".equals(p.getProperty("children")))
  {
    out.println("<div id=\"children\">");
    Page[] child = p.getChildren();
    int old=0;
    for (int i=child.length;i-->0;)
    {
      request.setAttribute("child",child[i]);
%>
${child.date} - <a href="${child.dirName}"><%=child[i].getName(lang)%></a>&nbsp;-&nbsp;
<%
  String b=(String) child[i].getProperty(lang,"blurb");
  if (b!=null)
      out.println(b);
%>
<br/>
<%}%>
</div>
<%}%>




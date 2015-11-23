<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.mortbay.iwiki.Page" %>

<% 
  String lang = (String) request.getAttribute("lang");
  Page p = (Page) request.getAttribute("page");

%>

<h1><%= p.getTitle(lang) %></h1>

<jsp:include page="text.jsp" flush="true"/>
<div class="Left">
<% 
  if (!"0".equals(p.getProperty("children")))
  {
    Page[] child = p.getChildren();
    for (int i=0;i<child.length;i++)
    {
      request.setAttribute("child",child[i]);
%>
<a href="${child.dirName}"><%=child[i].getName(lang)%></a>&nbsp;-&nbsp;
<%
  String b=(String) child[i].getProperty(lang,"blurb");
  if (b!=null)
      out.println(b);
%>
<br/>
<%} } %>
</div>
&nbsp;<br/>
<% 
  String[] images = p.getImages();
%>

<table>
<%
    for (int i=0;i<images.length;i++)
    {
       String image = images[i];
       int d=image.lastIndexOf(".");
       String mov=image.substring(0,d)+".mov";
       request.setAttribute("image",image);
       request.setAttribute("mov",mov);
    
%>
<tr>
<td><a href="./${mov}"><img class="Pano" src="${image}"/></a></td>
</tr>
<% 
    } 
%>
</table>
<big><big>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
</big></big>



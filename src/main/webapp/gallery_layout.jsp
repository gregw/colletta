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
  String img = request.getParameter("img");
  boolean done=false;

  if (img!=null)
  {
      int i = Integer.parseInt(img);
      if (i>=0 && i<images.length)
      {
	  done=true;
	  String image = images[i];
	  request.setAttribute("image",image);
	  request.setAttribute("i",new Integer(i));
%>
<a href=".?img=${i-1}"><img src="${contextPath}/images/prevB.gif"/></a>
<a href="."><img src="${contextPath}/images/upB.gif"/></a>
<a href=".?img=${i+1}"><img src="${contextPath}/images/nextB.gif"/></a>
<br/>
<a href=".?img=${i+1}"><img class="Gallery" src="${image}"/></a>

<%
  }}

  if (!done)
  {
%>

<table class="Gallery">
<tr>
<%
    for (int i=0;i<images.length;i++)
    {
       String image = images[i];
       int d=image.lastIndexOf(".");
       String thumb=image.substring(0,d)+".thumb"+image.substring(d);
       request.setAttribute("image",image);
       request.setAttribute("thumb",thumb);
       request.setAttribute("i",new Integer(i));
    
%>
<td class="Gallery"><a href=".?img=${i}"><img class="Thumb" src="${thumb}"/></a></td>
<% 
       if (i%2!=0) out.print("</tr><tr>");
    } 
%>

</tr>
</table>
<%}%>
<big><big>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
</big></big>



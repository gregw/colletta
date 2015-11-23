<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.mortbay.iwiki.Page" %>

<% 
  String lang = (String) request.getAttribute("lang");
  Page p = (Page) request.getAttribute("page");

%>

<h1><%= p.getTitle(lang) %></h1>

<jsp:include page="text.jsp" flush="true"/>
<br/>
&nbsp;
<br/>
<% 
  String[] images = p.getImages();
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
<td class="Gallery"><a href="${image}"><img class="Thumb" src="${thumb}"/></a></td>
<% 
       if (i%3==2) out.print("</tr><tr>");
    } 
%>

</tr>
</table>

<big><big>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
</big></big>



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
  if (!"0".equals(p.getProperty("children")))
  {
    out.println("<table id=\"children\">");
    Page[] child = p.getChildren();
    for (int i=0;i<child.length;i++)
    {
      request.setAttribute("child",child[i]);
%>
<tr><td>
<a href="${child.dirName}"><%=child[i].getName(lang)%></a><br/>
<%
  String b=(String) child[i].getProperty(lang,"blurb");
  if (b!=null)
      out.println(b);
%>
<br/>&nbsp;<br/><b>${properties.H_available}:</b>
<%
  b=(String) child[i].getProperty(lang,"available");
  if (b!=null)
      out.println(b);
%>
<br/><b>${properties.H_price}:</b>
<%
  b=(String) child[i].getProperty(lang,"price");
  if (b!=null)
      out.println(b);
%>
</td>
<td>&nbsp;
</td>
<td>
<a href="${child.dirName}"><img class="BigThumb" src="${child.dirName}/thumb.jpg"/></a>
</td>
</tr>

<%} 
  out.println("</table>");
} %>



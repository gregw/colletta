<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.mortbay.iwiki.Page" %>
<%@ page import="com.mortbay.iwiki.YyyyMmDd" %>


<% 
  String lang = (String) request.getAttribute("lang");
  Page map = (Page) request.getAttribute("map");
  int level = ((Integer) request.getAttribute("level")).intValue();

  Page[] children = map.getChildren();
%>
<%
  for (int i=0;i<children.length;i++)
  {
      Page child=children[i];
      request.setAttribute("child",child);
      String blurb=(String) child.getProperty(lang,"blurb");
      if (blurb==null)
	  blurb=child.getTitle(lang);
%>
      <div class="map${level}"> 
      <a href="${child.path}"><%= child.getTitle(lang) %></a> -
      <%= blurb %> 
      </div>
<%
      if (child.getChildren()!=null && child.getChildren().length>0)
      {
        request.setAttribute("map",child);
        request.setAttribute("level",new Integer(level+1));
%>
        <div class="maps${level}">
        <jsp:include page="map.jsp" flush="true"/>
	</div>
<%
      }
      request.setAttribute("level",new Integer(level));
  }
%>


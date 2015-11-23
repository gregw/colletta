<%@ page 
           contentType="text/html; charset=UTF-8"
           import="com.mortbay.iwiki.*,it.colletta.*,it.colletta.reservation.*,java.util.*"
 %>
 

<%
String lang = (String) request.getAttribute("lang");
Page root = (Page) request.getAttribute("page");
Page[] child;

%>


<div id="front">

<b>${properties.H_modified}</b>
<div id="modified">
<% 
Page[] modified=(Page[])request.getAttribute("modified");
int count=20;
for(int i=0;i<modified.length;i++) {
  if (modified[i].getParent()==null || modified[i].getPath().startsWith("/renting/"))
    continue;    
  if (count--==0)
    break;
  request.setAttribute("m",modified[i]);
%>
  <%=modified[i].getLastModified()%>&nbsp;-&nbsp;<a href="<%=modified[i].getPath()%>"> <%=modified[i].getTitle(lang)%> </a>&nbsp;-&nbsp;<%=modified[i].getPathProperty(lang,"blurb")%> <br/>
<% } %>
</div>

<a href="village/gallery/"><img id="frontPic" src="village.gif" alt="Telework"/></a>
<br/>&nbsp;<br/>

<a href="/events/">${properties.H_events}</a>
<div id="frontEvents">
<%
    Page events=root.getPageByPath("/events/");
    child = events.getChildren();
    YyyyMmDd now = new YyyyMmDd();
    now.addDays(-7);
    for (int i=0;i<child.length;i++)
    {
	if (now.after(child[i].getDate()))
	    continue;
      request.setAttribute("child",child[i]);
%>
      ${child.date} - <a href="/events/${child.dirName}"><%=child[i].getName(lang)%></a><br/>
<%
    }
%>
<a href="/events">${properties.H_moreEvents}...</a>
</div>


</div>

<a href="/news">${properties.H_news}</a>
<div id="frontNews">
<%
    Page news=root.getPageByPath("/news/");
    child = news.getChildren();
    for (int i=child.length;i-->Math.max(0,child.length-6);)
    {
      request.setAttribute("child",child[i]);
%>
      ${child.date} - <a href="/news/${child.dirName}"><%=child[i].getName(lang)%></a>&nbsp;-&nbsp;<%=child[i].getPathProperty(lang,"blurb")%><br/>
<%
    }
%>
<a href="/news">${properties.H_morenews}...</a>
&nbsp;
&nbsp;
<a href="http://groups.google.com/group/Colletta">${properties.H_forum}...</a>
&nbsp;
&nbsp;
<a href="http://groups.google.com/group/Collettiani">${properties.H_forumP}...</a>
</div>
<jsp:include page="text.jsp" flush="true"/>
&nbsp;
<br/>
<a href="/village/gallery/virtualtour/">${properties.H_vtour}:<br/></a>
<a href="/village/gallery/virtualtour/"><img class="Pano" src="/village/gallery/virtualtour/tour3.jpg"/><br/></a>


<br class="clear"/>

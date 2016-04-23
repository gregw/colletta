<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.mortbay.iwiki.*,it.colletta.*,it.colletta.reservation.*,java.util.*" %>
<%@ taglib prefix="tag" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="qsearch" scope="session" class="it.colletta.QSearch" />
<jsp:setProperty name="qsearch"  property="*"  />

<% 
  String lang = (String) request.getAttribute("lang");
  Locale locale = (Locale) request.getAttribute("locale");
  Page p = (Page) request.getAttribute("page");
  User user=(User) session.getAttribute("user");
%>

<% 

  List occupied = null;
  
  if (qsearch.isSearch()) { 
    if (qsearch.getNights()>0)
    {  
      YyyyMmDd first = new YyyyMmDd(qsearch);
      YyyyMmDd last = new YyyyMmDd(qsearch);
      last.addDays(qsearch.getNights());    
      String[] apts = ReservationManager.getInstance().findAllocatedApartments(first,last);
      occupied=Arrays.asList(apts);
    }
%>

<h1>${properties.searchResults}</h1>

<b>${properties.H_from}:</b> <%=YyyyMmDd.weekday(qsearch.getDayOfWeek(),locale)%> ${qsearch.dd}-<%=YyyyMmDd.month(qsearch.getMm(),locale)%>-${qsearch.yyyy}  &nbsp;&nbsp;&nbsp;
<%if(qsearch.getNights()>0){%><b>${properties.H_nights}:</b> ${qsearch.nights}  &nbsp;&nbsp;<%}%>
<br/>
<b>${properties.H_adults}:</b> ${qsearch.adults}  &nbsp;&nbsp;
<b>${properties.H_children}:</b> ${qsearch.children}  &nbsp;&nbsp;
<b>${properties.H_infants}:</b> ${qsearch.infants}  &nbsp;&nbsp;
<br/>
<a href=".?qsearch=off">${properties.H_view} ${properties.H_all}</a>
<br/>&nbsp;
<% } else { %>
<h1>${properties.title}</h1>

<br/>

<%if (user!=null && (user.isOwner()||user.isViewAll())) { %>
  <a href="./owner.jsp?yyyymm=${qsearch.yyyymm}">OWNER REPORT</a><br/>&nbsp;<br/>
<% } %>

<%if (user!=null && (user.isManager()||user.isViewAll())) { %>
  <a href="./confirmed.jsp?yyyymm=${qsearch.yyyymm}">CONFIRMED REPORT</a><br/>&nbsp;<br/>
<%
  ReservationData[] data = ReservationManager.getInstance().findReservations(ReservationStatus.REQUESTED);
  if (data!=null && data.length>0)
  {
    String s="<b>Bookings Requested:</b><br/>&nbsp;&nbsp;";
    for (int r=0;r<data.length;r++)
    { 
      out.print(s+"<a href=\"../book/?ref="+data[r].getId()+"\">"+data[r].getId()+"</a>");
      out.print(": "+data[r].getStartDate()+" - "+data[r].getEndDate()+" : "+data[r].getName()+"<br/>");
      s="&nbsp;&nbsp;";
    }
    if (data.length>0)
	out.println("&nbsp;<br/>");
  }
 } 
%>

<% } %>
<div class="navMonth">
<a href=".?yyyymm=${qsearch.yyyymm - 1}" class="btn btn-default"><i class="glyphicon glyphicon-backward"></i></a>
<b><%=YyyyMmDd.month(qsearch.getMm(),locale)%>&nbsp;${qsearch.yyyy}</b>
<a href=".?yyyymm=${qsearch.yyyymm + 1}" class="btn btn-default"><i class="glyphicon glyphicon-forward"></i></a>
</div>
<div class="calendar">
<table  class="AptL">
<tag:AptListRow/>
<% 
  Page[] child = p.getChildren();
  for (int i=0;i<child.length;i++)
  {
      String hidden = child[i].getProperty("entry");
      if (hidden != null && hidden.equalsIgnoreCase("hide") && (user==null || (!user.isManager() && !user.isViewAll())))
          continue;
      if (qsearch.isSearch())
      {
        if (child[i].getIntProperty("A_sleeps_min")>qsearch.getPeople() ||
            child[i].getIntProperty("A_sleeps_max")<qsearch.getPeople())
            continue;
        if (occupied!=null && occupied.contains(child[i].getDirName()))
            continue;
      }
        
      request.setAttribute("child",child[i]);
      request.setAttribute("image",child[i].getImages()[0]);
%>
<tag:AptListRow apt="${child}"/>
<%}%>

</table>
</div>


<br>
<%if(qsearch.isSearch()){%><a href=".?qsearch=off">${properties.H_view} ${properties.title}</a><%}%>

<jsp:include page="text.jsp" flush="true"/>



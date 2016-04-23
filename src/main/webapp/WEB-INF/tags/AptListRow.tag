<%@ tag
           import="com.mortbay.iwiki.*,it.colletta.*,it.colletta.reservation.*,java.util.*"
           body-content="empty"
%>
<%@ attribute name="apt"  type="com.mortbay.iwiki.Page" required="false" %>
<jsp:useBean id="qsearch" scope="session" class="it.colletta.QSearch" />

<% 
  Locale locale = (Locale) request.getAttribute("locale");
  YyyyMmDd today= new YyyyMmDd();
%>

<tr>

<% if (apt==null) { %>

<td class="AptLH">${properties.H_name}</td>
<td class="AptLH">${properties.H_sleeps}</td>
<td class="AptLH">${properties.H_bedrooms}</td>
<td class="AptLH">${properties.H_kitchen}</td>
<td class="AptLH">${properties.H_garden}</td>
<td class="AptLH">${properties.H_terrace}</td>
<td class="AptLH">${properties.H_price}</td>

<% } else { 
  Apartment apartment=Apartment.getApartment(apt.getDirName());
  ReservationData[] reservations = ReservationManager.getInstance().findReservations(apt.getDirName(),qsearch.getYyyymm()); 
%>


<td class="AptL"><strong>${apt.displayName}</strong></td>
<td class="AptL"><%=apt.getProperty("A_layout")%></td>
<td class="AptL"><%=apt.getProperty("A_bedrooms")%></td>
<td class="AptLC"><% if(apt.getIntProperty("A_kitchen")>0){%><img src="${contextPath}/images/tick.gif"/><%}else{%><img src="${contextPath}/images/cross.gif"/><%}%></td>
<td class="AptLC"><% if(apt.getIntProperty("A_garden")>0){%><img src="${contextPath}/images/tick.gif"/><%}else{%><img src="${contextPath}/images/cross.gif"/><%}%></td>
<td class="AptLC"><% if(apt.getIntProperty("A_terrace")>0){%><img src="${contextPath}/images/tick.gif"/><%}else{%><img src="${contextPath}/images/cross.gif"/><%}%></td>
<td class="AptL">&euro;<%=apartment.getListLow7()%>-&euro;<%=apartment.getListPeak7()%></td>

<td rowspan="2" class="AptFS"><a href="${apt.dirName}">
<% if(apt.getProperty("A_forsale")!=null&&false) { %>
<img class="AptTiny" src="/images/forsale.jpg"/>
<% } %>
<% if(apt.getProperty("entry")!=null&&apt.getProperty("entry").equals("hide")) { %>
NOT AVAILABLE
<% } %>
</td>

</tr><tr>
<td colspan="7" class="AptLB">

<table class="AptLCal">
<tr>
<%
YyyyMmDd date=new YyyyMmDd(qsearch);
int mm=date.getMm();
for(int i=1;i<=qsearch.getMaxDd();i++)
{
    date.setDd(i);  
    boolean old=date.before(today);
    String we=(date.getDayOfWeek()<=1)?"WE":"";
    String dc=date.before(today)?"AptLCalX":"AptLCal";
    String arg="?apt="+apt.getDirName();
    
    User user = (User)session.getAttribute("user");
    boolean viewBookings=(user!=null && (user.manages(apt.getDirName()) || user.owns(apt.getDirName()) || user.isViewAll() ));
    boolean link=viewBookings;
    
    if (reservations!=null && reservations[i]!=null)
    {
        if (!old || viewBookings)
        {
          dc="AptLCalB";
          if (ReservationStatus.REQUESTED.equals(reservations[i].getStatus()) ||
	        ReservationStatus.APPROVED.equals(reservations[i].getStatus()))
            dc="AptLCalR";
          if (viewBookings && "Unavailable".equals(reservations[i].getPriceBasis()))
            dc="AptLCalU";
        }   
	    if (i>1 && !date.equals(reservations[i].getStartDate()))
	      link=false;
	    arg+="&ref="+reservations[i].getId();
    }
    else
    {
        link=!old;
        arg+="&dd="+i;
    }
    
	out.print("<td class=\"cal "+dc+we+"\">");
	if (link)
	    out.print("<a class=\""+dc+we+"\" href=\"../book/"+arg+"\">"+i+"</a></td>");
	else
	    out.print(i);
	out.println("</td>");
}
%>
</tr></table>

</td>
</tr>

<% } %>


</tr>

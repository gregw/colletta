<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.mortbay.iwiki.Page" %>
<%@ page import="com.mortbay.iwiki.YyyyMmDd" %>
<%@ page import="java.util.Map" %>

<%@ taglib prefix="tag" tagdir="/WEB-INF/tags" %>

<%
  
  String yyyy = request.getParameter("calyyyy");
  int yyyyInt;

  
  
  if ((null!=yyyy) && !("".equals(yyyy.trim())))
  {
      yyyy = yyyy.trim();
      yyyyInt = Integer.parseInt(yyyy);
  }
  else
  {
      YyyyMmDd today = new YyyyMmDd();
      yyyyInt = today.getYyyy();
      yyyy = String.valueOf(yyyyInt);
  }
 
%>


<% 
  int prevYyyyInt = yyyyInt - 1;
  String prevYyyy = String.valueOf(prevYyyyInt);
  int nextYyyyInt = yyyyInt + 1;
  String nextYyyy = String.valueOf(nextYyyyInt);
%>





<table>

<tr>
<td class="CalPeak">
<%= ((Map)request.getAttribute("properties")).get("CalPeak") %>
</td>
<td class="CalLow">
<%= ((Map)request.getAttribute("properties")).get("CalLow") %>
</td>
</tr>

<tr><td>&nbsp;</td></tr>

<tr>
<td>
<h2>
<font size="-1">
<a href="/renting/conditions/seasons?calyyyy=<%= prevYyyy %>"><%= prevYyyy %>&nbsp;<img src="/images/prev.gif"></a>&nbsp;&nbsp;</font><font size="larger"><%= yyyy %></font><font size="-1">&nbsp;&nbsp;<a href="/renting/conditions/seasons?calyyyy=<%= nextYyyy %>"><img src="/images/next.gif">&nbsp;<%= nextYyyy %></a></font>
</h2>
</td>
</tr>


<tr>

<% 
  for (int mm = 1; mm <=12 ; mm++)
  {
     String yyyymm = yyyy+"-"+(mm<10?"0":"")+mm;   
  
%>
<td>
<tag:SeasonCalendar yyyymm="<%= yyyymm %>"/>
</td>
<%
     if ((mm % 3) == 0)
     {
         out.println ("</tr>");

         if (mm < 12)
             out.println ("<tr>");
     }
  }
%>
</tr>
</table>

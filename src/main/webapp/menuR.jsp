<%@ page 
           contentType="text/html; charset=UTF-8"
           import="com.mortbay.iwiki.*,it.colletta.*,it.colletta.reservation.*,java.util.*"
 %>
<%@ taglib prefix="tag" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="qsearch" scope="session" class="it.colletta.QSearch">
<% 
  qsearch.addDays(14);
  qsearch.toNextDayOfWeek(0);
%>
</jsp:useBean>
<jsp:useBean id="today" scope="session" class="com.mortbay.iwiki.YyyyMmDd" >
  <% today.setToNow(); %>
</jsp:useBean>

<jsp:setProperty name="qsearch"  property="*"  />

<h1>${properties.H_view} ${properties.H_apartments}</h1>
<form id="QSearch" action="${contextPath}/renting/view/">
<table class="QSearch">


<tr><td class="QSearch">${properties.H_from}:</td>
<td class="QSearch">
<tag:IntSelect style="QSearch" name="dd" min="1" max="31" value="${qsearch.dd}" dft="1" /><br/>
<tag:MonthSelect style="QSearch" name="mm" locale="${locale}" value="${qsearch.mm}" /><br/>
<tag:IntSelect style="QSearch" name="yyyy" min="${today.yyyy - 1}" max="${today.yyyy + 2}" value="${qsearch.yyyy}" dft="${today.yyyy}" />
</td>
</tr>

<tr>
<td class="QSearch">${properties.H_nights}:</td>
<td class="QSearch"><tag:IntSelect style="QSearch" name="nights" min="1" max="31" value="${qsearch.nights}" dft="7" /></td>
</tr>
<tr>
<td class="QSearch">${properties.H_adults}:</td>
<td class="QSearch"><tag:IntSelect style="QSearch" name="adults" min="1" max="9" value="${qsearch.adults}" dft="2" /></td>
</tr>
<tr>
<td class="QSearch">${properties.H_children}:</td>
<td class="QSearch"><tag:IntSelect style="QSearch" name="children" min="0" max="9" value="${qsearch.children}" dft="0" /></td>
</tr>
<tr>
<td class="QSearch">${properties.H_infants}:</td>
<td class="QSearch"><tag:IntSelect style="QSearch" name="infants" min="0" max="9" value="${qsearch.infants}" dft="0" /></td>
</tr>


<tr>
<td class="QSearch">
<input type="submit" class="QSearch" name="qsearch" value="${properties.B_search}"/>
</td>

</tr>
</table>
</form>


<h1>${properties.H_view} ${properties.H_booking}</h1>
<form id="Lookup" action="${contextPath}/renting/book/">
<table class="QSearch">
<tr>
<td class="QSearch">${properties.H_ref}:</td>
<td class="QSearch"><input class="QSearch" type="text" name="ref" size="8" value="${reservation_id}"/></td>
</tr>
<tr>
<td class="QSearch">
<input type="submit" class="QSearch" name="booking" value="view"/>
</td>
</tr>
</table>
</form>

<%@ include file="login.jsp" %>


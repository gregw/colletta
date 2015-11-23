<%@ tag
           import="com.mortbay.iwiki.*,it.colletta.*,it.colletta.reservation.*,java.util.*,java.math.*"
%>
<%@ attribute name="resId" %>
<%@ attribute name="name" %>
<%@ attribute name="value" type="java.lang.String" %>
<%@ attribute name="enabled" type="java.lang.Boolean" %>

<% if (enabled==null || enabled.booleanValue()){ %>
<form action="https://www.paypal.com/cgi-bin/webscr" method="get" id="paypal${name}" >
<input type="hidden" name="cmd" value="_xclick">
<input type="hidden" name="business" value="borgotelematico@colletta-it.com">
<input type="hidden" name="item_name" value="${name} for reservation ${resId}">
<input type="hidden" name="item_number" value="${resId}">
<input type="hidden" name="amount" value="${value}">
<input type="hidden" name="page_style" value="BorgoTelematico">
<input type="hidden" name="no_shipping" value="1">
<input type="hidden" name="return" value="http://www.colletta-it.com/renting/book/?ref=${resId}">
<input type="hidden" name="cancel_return" value="http://www.colletta-it.com/renting/book/?ref=${resId}">
<input type="hidden" name="currency_code" value="EUR">
<input type="hidden" name="lc" value="${lang}">
<input type="image" src="${contextPath}/images/paypal${name}.gif" border="0" name="submit" alt="Pay deposit with PayPal" }>
</form><jsp:doBody/>
<% } %>

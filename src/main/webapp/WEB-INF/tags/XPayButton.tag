<%@ tag
  import="com.mortbay.iwiki.*,it.colletta.*,it.colletta.reservation.*, it.colletta.payment.*, java.util.*,java.math.*"
%>
<%@ attribute name="resId" %>
<%@ attribute name="name" %>
<%@ attribute name="value"%>
<%@ attribute name="enabled" type="java.lang.Boolean" %>
<%@ attribute name="test" type="java.lang.Boolean" %>

<% 


BigDecimal dvalue = new BigDecimal(value).setScale(2);
        
   if (enabled==null || enabled.booleanValue())
   {
     XPay.setVPOSReqLightAttributes(request, resId, dvalue.scaleByPowerOfTen(2).round(new MathContext(0)).toBigInteger(), (String)session.getAttribute("lang"), null);
   
%>

<form action="${PAYMENT_URL}" method="POST"  id="xpay${name}">

<input type="hidden" name="TERMINAL_ID" value="${TERMINAL_ID}">
<input type="hidden" name="TRANSACTION_ID" value="${TRANSACTION_ID}">
<input type="hidden" name="ACTION_CODE" value="${ACTION_CODE}">
<input type="hidden" name="AMOUNT" value="${AMOUNT}">
<input type="hidden" name="CURRENCY" value="${CURRENCY}">
<input type="hidden" name="LANGUAGE" value="${LANGUAGE}">
<input type="hidden" name="NOTIFICATION_URL" value="${NOTIFICATION_URL}">
<input type="hidden" name="RESULT_URL" value="${RESULT_URL}">
<input type="hidden" name="ERROR_URL" value="${ERROR_URL}">
<input type="hidden" name="ANNULMENT_URL" value="${ANNULMENT_URL}">
<input type="hidden" name="VERSION_CODE" value="${VERSION_CODE}">
<input type="hidden" name="EMAIL" value="${EMAIL}">
<input type="hidden" name="DESC_ORDER" value="${DESC_ORDER}">
<input type="hidden" name="CO_PLATFORM" value="${CO_PLATFORM}">
<input type="hidden" name="MAC" value="${MAC}">
<input type="image" src="${contextPath}/${lang}/images/cartasi${name}.gif" border="0" name="submit" alt="Pay ${name} with CartaSi"><jsp:doBody/></form>
<% 
  XPay.dumpVPOSReqLight(request);

  } %>

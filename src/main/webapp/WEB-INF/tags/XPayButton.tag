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
     XPay.setPaymentAttributes(request, resId, dvalue.scaleByPowerOfTen(2).round(new MathContext(0)).toBigInteger(), (String)session.getAttribute("lang"), null);
   
%>

<form action="${PAYMENT_URL}" method="POST"  id="xpay${name}">

<input type="hidden" name="alias" value="${alias}">
<input type="hidden" name="codTrans" value="${codTrans}">
<input type="hidden" name="importo" value="${importo}">
<input type="hidden" name="divisa" value="EUR">
<input type="hidden" name="languageId" value="${languageId}">
<input type="hidden" name="mail" value="${mail}">
<input type="hidden" name="url" value="${url}">
<input type="hidden" name="url_back" value="${url_back}">
<input type="hidden" name="urlpost" value="${urlpost}">
<input type="hidden" name="mac" value="${mac}">
<input type="image" src="${contextPath}/${lang}/images/cartasi${name}.gif" border="0" name="submit" alt="Pay ${name} with CartaSi"><jsp:doBody/></form>
<% 
  XPay.dumpPaymentAttributes(request);

  } %>

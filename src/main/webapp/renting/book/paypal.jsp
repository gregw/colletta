<%@ page import="java.util.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.io.*" %>
<%@ page import="java.math.*" %>
<%@ page import="it.colletta.*" %>
<%@ page import="com.mortbay.iwiki.*" %>
<%@ page import="it.colletta.reservation.*" %>
<%
// read post from PayPal system and add 'cmd'
Enumeration en = request.getParameterNames();
String str = "cmd=_notify-validate";
while(en.hasMoreElements()){
String paramName = (String)en.nextElement();
String paramValue = request.getParameter(paramName);
str = str + "&" + paramName + "=" + URLEncoder.encode(paramValue);
}
pageContext.getServletContext().log("PAYPAL Notify: "+str);

// Check that we have a valid reservation for this.
String resId=request.getParameter("item_number");
ReservationManager reserve=ReservationManager.getInstance();
ReservationData reservation=reserve.findReservation(resId);

if (reservation==null ||
    reservation.getStatus()==null ||
    !reservation.isActive())
{
	pageContext.getServletContext().log("PAYPAL Invalid reservation: "+resId);
	out.print("400 - Invalid request");
    response.setStatus(400);
	return;
}


// post back to PayPal system to validate
// NOTE: change http: to https: in the following URL to verify using SSL (for increased security).
// using HTTPS requires either Java 1.4 or greater, or Java Secure Socket Extension (JSSE)
// and configured for older versions.
URL u = new URL("http://www.paypal.com/cgi-bin/webscr");
URLConnection uc = u.openConnection();
uc.setDoOutput(true);
uc.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
PrintWriter pw = new PrintWriter(uc.getOutputStream());
pw.println(str);
pw.close();

BufferedReader in = new BufferedReader(
new InputStreamReader(uc.getInputStream()));
String res = in.readLine();
in.close();

// assign posted variables to local variables
String itemName = request.getParameter("item_name");
String itemNumber = request.getParameter("item_number");
String paymentStatus = request.getParameter("payment_status");
String paymentAmount = request.getParameter("mc_gross");
String paymentCurrency = request.getParameter("mc_currency");
String txnId = request.getParameter("txn_id");
String receiverEmail = request.getParameter("receiver_email");
String payerEmail = request.getParameter("payer_email");



// check notification validation
if(res.equals("VERIFIED")) {
	// check that txnId has not been previously processed
	// TODO
	
	// check that paymentStatus=Completed
	int sign=0;
	if("Completed".equalsIgnoreCase(paymentStatus) ||
	   "Canceled_Reversal".equals(paymentStatus))
	   sign=1;
	if("Refunded".equalsIgnoreCase(paymentStatus) ||
	   "Reversed".equals(paymentStatus))
	   sign=-1;

	// check that receiverEmail is your Primary PayPal email
	if (!receiverEmail.equals("info@colletta.it"))
		sign=0;

	// check that paymentAmount/paymentCurrency are correct
	if (!"EUR".equals(paymentCurrency))
		sign=0;
	
	// process payment
	try
	{	if (sign!=0)
		{
			BigDecimal amount = new BigDecimal(paymentAmount);
				
		    pageContext.getServletContext().log("PAYPAL VERIFIED: "+amount+", "+resId+", "+txnId);
			reserve.makePayment(resId,amount,txnId);
		}
	}
	catch(Exception e)
	{
		e.printStackTrace();
		pageContext.getServletContext().log("PAYPAL FAILURE:"+str+" : "+e);
	}
}
else if(res.equals("INVALID")) {
	pageContext.getServletContext().log("PAYPAL INVALID:"+str);
}
else {
	// error
}
%>

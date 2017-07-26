/*
 * Created on Aug 20, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package it.colletta;


import it.colletta.payment.XPay;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author janb
 *
 * 
 */
public class PaymentServlet extends HttpServlet
{
    private static final long serialVersionUID = 6444677911880828790L;

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        super.doPost(req,resp);
    }

    /**
     * Handle a payment notification message from the XPay payment system
     * as the URL_POST.
     * 
     * The confirmation can indicate success or failure.
     * 
     * Also, the confirmation might have already been processed by the booking filter
     * when the user is returned to the Colletta site after the payment.
     */
    public void doPost (HttpServletRequest srequest, HttpServletResponse sresponse)
    throws IOException, ServletException
    {
        PrintWriter out = sresponse.getWriter();
        sresponse.setContentType("text/html");
        
        try
        {
            XPay.handlePayment(srequest, getServletContext());
            out.print("RESPONSE=0");
        }
        catch (Exception e)
        {
            getServletContext().log("Problem processing payment: ", e);
            out.print("400 - Problem with payment message:"+e);
            sresponse.setStatus(400);
            throw new ServletException (e.getMessage());
        }
   }
}

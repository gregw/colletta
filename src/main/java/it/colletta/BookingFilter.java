/* ==============================================
 * Copyright 2003 Mort Bay Consulting Pty Ltd. All rights reserved.
 * Distributed under the artistic license.
 * Created on 2/04/2004
 * $Id: BookingFilter.java,v 1.88 2006/08/15 08:58:24 gregw Exp $
 * ============================================== */

package it.colletta;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.mortbay.iwiki.FormFilter;
import com.mortbay.iwiki.LangFilter;
import com.mortbay.iwiki.Page;
import com.mortbay.iwiki.User;
import com.mortbay.iwiki.YyyyMmDd;

import it.colletta.payment.XPay;
import it.colletta.reservation.Address;
import it.colletta.reservation.Adjustment;
import it.colletta.reservation.OccupancyManager;
import it.colletta.reservation.OccupancyManagerFactory;
import it.colletta.reservation.ReservationData;
import it.colletta.reservation.ReservationManager;
import it.colletta.reservation.ReservationStatus;
import it.colletta.reservation.ReservationTimer;
import it.colletta.reservation.discount.Discount;

/* ------------------------------------------------------------------------------- */
/**
 * 
 * @version $Revision: 1.88 $
 * @author gregw
 */
public class BookingFilter extends FormFilter
{
    private static final Logger log = Logger.getLogger(BookingFilter.class.getName());
    public static final BigDecimal ZERO = new BigDecimal("0.00");
    ReservationManager reserve;
    ReservationTimer timer;

    /* ------------------------------------------------------------------------------- */
    /**
     * Constructor.
     * 
     * @param submit
     */
    public BookingFilter()
    {
        super("book");
    }

    /* ------------------------------------------------------------------------------- */
    public void destroy()
    {
        if (timer != null) timer.stop();
        timer = null;

        super.destroy();
    }

    /* ------------------------------------------------------------------------------- */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        HttpServletResponse sresponse = (HttpServletResponse) response;
        sresponse.setHeader("Pragma", "no-cache");
        sresponse.setHeader("Cache-Control", "no-cache,no-store");
        sresponse.setDateHeader("Expires", 0);


        // TODO fix this hack
        HttpSession session = ((HttpServletRequest)request).getSession(true);
        Reservation reservation = (Reservation) session.getAttribute("reservation");
        boolean albergo = ((HttpServletRequest)request).getRequestURI().indexOf("/room/")>=0;
        if (albergo && reservation!=null && reservation.getStatus()!=null)
        {
            System.err.println("RESET!");
            session.removeAttribute("reservation");
        }
        
        //If the user made a payment and it was:
        // successful, cancelled or in error we will land here     
        if (null != request.getParameter(XPay.COD_TRANS) && null != request.getParameter(XPay.ESITO))
        {
            String uri =((HttpServletRequest)request).getRequestURI();
            
            try
            {
                uri =  handleReturnFromPayment((HttpServletRequest)request);
            }
            catch (Exception e)
            {
                context.log("Error processing XPay notification", e);
            }
            
            ((HttpServletResponse)response).sendRedirect(uri);
            return;
        }
        
        super.doFilter(request, response, chain);
    }

    /* ------------------------------------------------------------------------------- */
    protected String handleGET(HttpServletRequest srequest, HttpServletResponse sresponse, String form) throws Exception
    {
        //TODO I don't think we'll ever get here because in doFilter we are going to call handleReturnFromPayment
        //regardless of a GET or POST
        initValues(srequest);

        srequest.getSession(true);

        String s = srequest.getParameter(XPay.ESITO);
        if (s == null) return null; // not an XPay payment message

        return handleReturnFromPayment(srequest);
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * Handle a redirect from the Xpay system after the user has finished with the
     * payment system.
     * 
     * The user might have:
     * <ul>
     * <li> cancelled the payment: xpay redirects to URL_BACK</li>
     * <li> produced an error during payment: xpay redirects to URL_BACK </li>
     * <li> been successful: xpay redirects to URL </li>
     * </ul>
     * 
     * Currently both URL_BACK and URL point to this filter, although xpay sends
     * back a different set of params to each.
     * 
     * NOTE that the XPay notification POST message (to URL_POST) is handled 
     * by the PaymentServlet.
     * 
     * @param srequest the request with the Xpay message
     * @return
     * @throws Exception
     */
    protected String handleReturnFromPayment(HttpServletRequest srequest) throws Exception
    {
        context.log("handling return from PAYMENT");
        try
        {
            String resId = XPay.getResIdFromPaymentMessage(srequest, context);
            return srequest.getRequestURI() + "?ref=" + resId + "&booking=view";
        }
        catch (Exception e)
        {
            context.log("Error processing XPay redirect", e);
            return srequest.getRequestURI();
        }

        
        
        
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * @see com.mortbay.iwiki.FormFilter#handlePOST(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected String handlePOST(HttpServletRequest srequest, HttpServletResponse sresponse, String form) throws Exception
    {
        String next = null;
        try
        {
            HttpSession session = srequest.getSession(true);

            String action = getAction(srequest);

            if (action == null || action.length() == 0) 
                return null;

            if (action.equals("new"))
            {
                session.removeAttribute("reservation");
                return null;
            }

            Reservation reservation = (Reservation) session.getAttribute("reservation");

            boolean albergo = srequest.getRequestURI().indexOf("/room/")>=0;
            if (albergo && reservation!=null && reservation.getStatus()!=null)
            {
                session.removeAttribute("reservation");
                return null;
            }

            initValues(srequest);
            User user = User.getCurrentUser();
            Apartment apartment = (Apartment) srequest.getAttribute("apartment");

            boolean owned = (user != null && apartment != null && user.owns(apartment.getName()));
            boolean managed = false;
            if (user != null && (user.isAdmin() || (apartment == null && user.isManager()) || (apartment != null && (user.manages(apartment.getName()))) || (reservation != null && owned && "Unavailable".equals(reservation.getPriceBasis())))) managed = true;

            context.log("action=" + action + " user=" + user + " ref=" + (reservation == null ? "?" : reservation.getId()) + " apt=" + apartment);

            // Handle request here to make sure no values are changed after last view.
            if (action.equals("request"))
            {
                if ("on".equals(srequest.getParameter("acceptedConditions")))
                {
                    reservation.delError("NotAccepted");
                }
                else
                {
                    reservation.clearErrors();
                    reservation.addError("NotAccepted");
                    reservation.setReady(true);
                    return next;
                }

                next = srequest.getContextPath()+"/renting/book/";

                if (!reservation.hasErrors())
                {   
                    ReservationData data = reserve.requestReservation(reservation.getReservationData(), apartment != null && !apartment.isApproval() && !reservation.hasWarnings());
                    reservation.setReservationData(data);
                    
		    /* TODO reenable redirection to payment system
                    if (!managed && reservation.getTotal().compareTo(ZERO)>0 && ReservationStatus.APPROVED.equals(reservation.getStatus()))
                        return next+"?redirect=bankpass";
		    */
                    return next;
                }
            }

            reservation.setReady(false);
            reservation.setRealloc(false);

            // really nasty hack!
            // the discountCode must be able to be removed by deleting the value in the text input
            // box.
            // however, the method srequest.getParameter("discountCode") cannot differentiate
            // between
            // the value being empty, or the parameter not even being present, which will be the
            // case
            // when editing is turned off (eg when in non-editing mode after pressing "continue"
            // then
            // moving to editing mode when pressing "update").
            // So, we want to null out the value ONLY if the user has
            // deliberately deleted the discountCode. The only way I can reliably find to do this
            // is to enumerate over the parameters - it seems that if the parameter is set to the
            // empty string (as opposed to not even being present when in read-only mode), it will
            // be reported in the enumeration, even tho getParameter() still
            // reports null.
            java.util.Enumeration names = srequest.getParameterNames();
            while (names.hasMoreElements())
            {
                String name=(String)names.nextElement();
                if (name.equals("discountCode"))
                {
                    String dc=srequest.getParameter("discountCode");
                    if(reservation.getDiscountCode()!=null && !reservation.getDiscountCode().equals(dc) ||
                       reservation.getDiscountCode()==null && dc!=null && dc.length()>0)
                    {
                        reservation.setRealloc(true);
                        reservation.setDiscountCode(dc);
                    }
                }
            }

            // Read values from the form parameters
            org.apache.jasper.runtime.JspRuntimeLibrary.introspect(reservation, srequest);
            reservation.resolve();

            if (albergo)
            {
                OccupancyManager om = OccupancyManagerFactory.getOccupancyManager();
                reservation.setAptId(null);
                srequest.setAttribute("apartment", null);
                
                double people = 1.0*reservation.getAdults()+1.0*reservation.getChildren()+0.5*reservation.getInfants();
                
                // TODO remove this fixed list!
                Page renting = Apartment.getRoot().getPageByPath("/renting/");
                
                String[] apts = renting.getProperty("albergo").split(",");
                
                for (int a=0;a<apts.length;a++)
                {
                    String aptId=apts[a];
                    Apartment apt=Apartment.getApartment(aptId);
                    System.err.println("albergo try "+aptId);
                    if (apt.getMaxOccupancy()<people)
                        continue;
                    
                    if (om.isApartmentAvailable(aptId,reservation.getStartDate(),reservation.getEndDate()))
                    {
                        System.err.println("albergo is available "+aptId);
                        reservation.setAptId(aptId);
                        apartment = apt;
                        srequest.setAttribute("apartment", apartment);
                        break;
                    }
                }
            }

            if (managed)
            {
                if (srequest.getParameter("commissionPercent")!=null)
                    reservation.data.setCommissionPercent(Integer.parseInt(srequest.getParameter("commissionPercent")));
                reservation.data.setReferer(srequest.getParameter("referer"));
                
                // Delete unwanted adjustments
                ArrayList adjustments = new ArrayList(reservation.getReservationData().getAdjustments());
                for (int i = adjustments.size(); i-- > 0;)
                {
                    System.err.println("adjustment" + i + ": " + srequest.getParameter("adjustment" + i) + " Adjustment=" + adjustments.get(i));
                    Adjustment adj = (Adjustment) adjustments.get(i);
                    if (!"on".equals(srequest.getParameter("adjustment" + i)))
                    {
                        Object o = adjustments.remove(i);
                        System.err.println("Removed adjustment " + o);
                    }
                }
                reservation.getReservationData().setAdjustments(adjustments);   
            }

            // Handle offers
            handleOffers(srequest,reservation);

            // Handle surcharges
            if (managed)
                handleAdjustments(srequest,reservation);
            
            // Validation
            reservation.clearWarnings();
            reservation.clearErrors();

            boolean recalcDiscount=false;
            if (!"Unavailable".equals(reservation.getPriceBasis()))
            {
                if (reservation.isRealloc() || 
                    reservation.getStatus() == null || 
                    reservation.getStatus().equals(ReservationStatus.REQUESTED) ||
                    "on".equals(srequest.getParameter("recalc")))
                {
                    reservation.calculatePrice();
                    recalcDiscount=true;
                }

                if (reservation.isRealloc() || "on".equals(srequest.getParameter("recalc")))
                {
                    recalcDiscount=true;
                }
            }

            if (recalcDiscount)
                reservation.calcDiscount();

            if (reservation.getStartDate().equals(reservation.getEndDate()) || reservation.getStartDate().after(reservation.getEndDate()))
                reservation.addError("BadDateRange");
            else if (reservation.getStartDate().daysTo(reservation.getEndDate()) >= 100)
                reservation.addError("LongDateRange");
            else if (reservation.getId() == null && !reserve.validateRequest(reservation.getReservationData())) reservation.addError("NotAvail");

            if (reservation.getName() == null || reservation.getName().length() == 0) reservation.addError("NoName");
            if ((reservation.getPhone1() == null || reservation.getPhone1().length() == 0) ||
	        (reservation.getEmail() == null || reservation.getEmail().length() == 0) ||
		(reservation.getAddress() == null || reservation.getAddress().length() == 0))
		reservation.addError("NoContact");

            // Warnings
            if (apartment != null && reservation.getStartDate().daysTo(reservation.getEndDate()) < apartment.getMinStay()) reservation.addWarning("MinStay");
            if ((apartment == null || !apartment.isApproval()) && 
                    (apartment == null || apartment.getMinStay()>1 )&&
                    (reservation.getStatus() == null || reservation.getStatus().equals(ReservationStatus.REQUESTED)) && 
                    (reservation.getStartDate().getDayOfWeek() != 0 || reservation.getEndDate().getDayOfWeek() != 0) && 
                    reservation.getPriceBasis() != null && 
                    reservation.getPriceBasis().indexOf("H") >= 0) 
                reservation.addWarning("NotWeek");
            if ((reservation.getStatus() == null || reservation.getStatus().equals(ReservationStatus.REQUESTED)) && reservation.getAptId() == null) reservation.addWarning("NoApartment");
            
            try
            {
                
                // Do general update
                if (action.equals("update") || action.equals("approve") || action.equals("confirm"))
                {
                    if (reservation.getId() != null)
                    {
                        if (reservation.isRealloc() && (managed || ReservationStatus.REQUESTED.equals(reservation.getStatus()))) reserve.allocateApartment(reservation.getId(), reservation.getAptId(), reservation.getStartDate(), reservation.getEndDate());

                        reserve.updateReservation(reservation.getReservationData());

                        String payment = srequest.getParameter("payment");
                        String paymentRef = srequest.getParameter("paymentRef");
                        if (managed && payment != null && !"0.00".equals(payment))
                        {
                            BigDecimal p = new BigDecimal(payment);
                            context.log("Payment entered by " + user.getName() + " for " + reservation.getId() + " of " + p);
                            reserve.updateReservation(reservation.getReservationData());
                            reserve.makePayment(reservation.getId(), p, paymentRef == null ? "unknown" : paymentRef);
                        }
                    }
                }

                // Do specific actions
                if (action.equals("approve"))
                {
                    if (reservation.getAptId() == null)
                    {
                        reservation.addError("AllocApartment");
                    }
                    else if (user != null && !user.manages(reservation.getAptId()))
                    {
                        reservation.addError("NotManager");
                    }
                    else if (managed && reservation.getAptId() != null)
                    {
                        reservation.clearWarnings();
                        reserve.updateReservation(reservation.getReservationData());
                        reserve.approveReservation(reservation.getId());
                    }
                }
                else if (action.equals("cancel"))
                {
                    if (reservation.getId() != null) reserve.cancelReservation(reservation.getId());
                }
                else if (action.equals("confirm"))
                {
                    if (managed && reservation.getId() != null)
                    {
                        reservation.clearWarnings();
                        reserve.confirmReservation(reservation.getId());
                    }
                }
                else if (action.equals("continue"))
                {
                    if (reservation.getId() == null)
                    {                        
                        reservation.calculatePrice();
                        reservation.setReady(!reservation.hasErrors());
                        
                    }
                }
                else if (action.equals("unavailable"))
                {
                    if (user != null && apartment != null && (user.owns(reservation.getAptId()) || user.manages(reservation.getAptId()) || user.isAdmin()))
                    {
                        boolean notAvail = reservation.hasError("NotAvail");
                        reservation.clearWarnings();
                        reservation.clearErrors();

                        reservation.setName(user.getFullName());
                        reservation.setEmail(user.getEmail());
                        if (user.getContact() == null || user.getContact().length() == 0)
                            reservation.setPhone1("unavailable");
                        else
                            reservation.setPhone1(user.getContact());

                        reservation.setPrice(ZERO);
                        reservation.getReservationData().setAdjustments(null);
                        if (reservation.getNotes() == null) reservation.setNotes("Unavailable");
                        reservation.setPriceBasis("Unavailable");
                        reservation.setDiscountCode(null);
                        reservation.data.setCommissionPercent(0);

                        if (!notAvail)
                        {
                            ReservationData data = reserve.requestReservation(reservation.getReservationData(), true);
                            reservation.setReservationData(data);

                            // if (ReservationStatus.REQUESTED.equals(reservation.getStatus()))
                            // reserve.approveReservation(reservation.getId());
                            // reserve.confirmReservation(reservation.getId());
                            // reservation.setReservationData(reserve.findReservation(reservation.getId()));
                        }
                        else
                            reservation.addError("AlreadyBooked");
                    }
                }

                // Update session object
                if (reservation.getId() != null) reservation.setReservationData(reserve.findReservation(reservation.getId()));

            }
            catch (Exception e)
            {
                e.printStackTrace();
                reservation.addError(e.getMessage());
            }

            return next;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    private void handleAdjustments(HttpServletRequest srequest, Reservation reservation)
    {
        try
        {
            String amount = srequest.getParameter("adjAmount");

            if (amount != null && amount.length() > 0)
            {
                BigDecimal x = new BigDecimal(amount);
                x = x.setScale(2);
                
                String adjType=srequest.getParameter("adjType");
                String adjComment=srequest.getParameter("adjComment");
            
                if (Adjustment.__DISCOUNT.equals(adjType) && x.doubleValue()>0)
                    x=x.negate();
                    
                    
                Adjustment adj = reservation.getReservationData().addAdjustment(adjType,x,adjComment);
            }
        }
        catch (NumberFormatException e)
        {
            log.log(Level.WARNING,e.getMessage(),e);
        }

    }

    /* ------------------------------------------------------------------------------- */
    private void handleOffers(HttpServletRequest request, Reservation reservation)
    {
        Page page = Apartment.getRoot().getPageByPath(request.getServletPath());
        String lang=(String)request.getAttribute("lang");
        
        
        Adjustment adjustment=null;
        
        if ("on".equals(request.getParameter("offerWelcome")))
        {
            adjustment=reservation.data.addAdjustment("O_Welcome", 
                    new BigDecimal(page.getProperty("O_WelcomePrice")), 
                    page.getProperty(lang,"O_WelcomeBlurb"));
        }
        
        if ("on".equals(request.getParameter("offerWelcomeBig")))
        {
            adjustment=reservation.data.addAdjustment("O_WelcomeBig", 
                    new BigDecimal(page.getProperty("O_WelcomeBigPrice")), 
                    page.getProperty(lang,"O_WelcomeBigBlurb"));
        }
        
        if ("on".equals(request.getParameter("offerEoffice")))
        {
            adjustment=reservation.data.addAdjustment("O_Eoffice", 
                    new BigDecimal(page.getProperty("O_EofficePrice")), 
                    page.getProperty(lang,"O_EofficeBlurb"));
        }
        
        if ("on".equals(request.getParameter("offerLate")))
        {
            adjustment=reservation.data.addAdjustment("O_Late", 
                    new BigDecimal(page.getProperty("O_LatePrice")), 
                    page.getProperty(lang,"O_LateBlurb"));
        }
        
        
        //Handle the 3 board options: breakfast, half board, full board
        //allowing the user to change between them
        if ("offerNone".equals(request.getParameter("offerBoard")) || request.getParameter("offerBoard") == null)
        {
            reservation.data.removeAdjustments("O_Colazione");
            reservation.data.removeAdjustments("O_MezzaPensione");
            reservation.data.removeAdjustments("O_Pensione");
        }
        
        BigDecimal personNights = new BigDecimal(reservation.getNights()).multiply(new BigDecimal(reservation.getChildren() + reservation.getAdults()));
        
        //if breakfast selected, was not already selected, remove any other types of board
        if ("offerColazione".equals(request.getParameter("offerBoard")))
        {
            reservation.data.removeAdjustments("O_Colazione");
            reservation.data.removeAdjustments("O_MezzaPensione");
            reservation.data.removeAdjustments("O_Pensione");
            BigDecimal price = new BigDecimal(page.getProperty("O_ColazionePrice")).multiply(personNights);
            adjustment=reservation.data.addAdjustment("O_Colazione", 
                    price, 
                    page.getProperty(lang,"O_ColazioneBlurb"));
        }
        
        //if half board selected, was not already selected, remove any other types of board
        if ("offerMezzaPensione".equals(request.getParameter("offerBoard")))
        {
            reservation.data.removeAdjustments("O_Colazione");
            reservation.data.removeAdjustments("O_MezzaPensione");
            reservation.data.removeAdjustments("O_Pensione");
            BigDecimal price = new BigDecimal(page.getProperty("O_MezzaPensionePrice")).multiply(personNights);
            adjustment=reservation.data.addAdjustment("O_MezzaPensione", 
                    price, 
                    page.getProperty(lang,"O_MezzaPensioneBlurb"));
        }      

        //if full board selected, was not already selected, remove any other types of board
        if ("offerPensione".equals(request.getParameter("offerBoard")))
        {
            reservation.data.removeAdjustments("O_Colazione");
            reservation.data.removeAdjustments("O_MezzaPensione");
            reservation.data.removeAdjustments("O_Pensione");
            BigDecimal price = new BigDecimal(page.getProperty("O_PensionePrice")).multiply(personNights);
            adjustment=reservation.data.addAdjustment("O_Pensione", 
                    price, 
                    page.getProperty(lang,"O_PensioneBlurb"));
        }
    }

    /* ------------------------------------------------------------------------------- */
    public void init(FilterConfig config) throws ServletException
    {
        super.init(config);

        try
        {
            Configuration configuration = Configuration.getInstance();
            URL resource = config.getServletContext().getResource("/WEB-INF/" + Configuration.PROPS_FILENAME);
            config.getServletContext().log("Configuration=" + resource);
            configuration.load(resource);
            if (".".equals(configuration.getProperty("root.dir")))
            {
                URL root = config.getServletContext().getResource("/WEB-INF/");

                configuration.setProperty("root.dir", root.getFile());
            }
            reserve = ReservationManager.getInstance();
            config.getServletContext().setAttribute("reserve", reserve);

            timer = new ReservationTimer();
            timer.start();

        }
        catch (Exception e)
        {
            config.getServletContext().log("reserve", e);
            throw new ServletException(e);
        }
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * @see com.mortbay.iwiki.FormFilter#initValues(javax.servlet.http.HttpServletRequest)
     */
    protected void initValues(HttpServletRequest srequest) throws Exception
    {
        HttpSession session = srequest.getSession(true);

        // find or create qsearch object
        QSearch qsearch = (QSearch) session.getAttribute("qsearch");
        if (qsearch == null)
        {
            qsearch = new QSearch();
            session.setAttribute("qsearch", qsearch);
        }
        org.apache.jasper.runtime.JspRuntimeLibrary.introspect(qsearch, srequest);

        // find or create reservation data.
        String ref = srequest.getParameter("ref");
        String apt = srequest.getParameter("apt");
        String dd = srequest.getParameter("dd");
        String yyyymm = srequest.getParameter("yyyymm");
        Reservation reservation = (Reservation) session.getAttribute("reservation");

        // Do we have a reservation object?
        if (reservation != null)
        {
            // yes

            // Are we changing the details of the reservation (by ref or date)?
            if (ref != null || apt != null || dd != null || yyyymm != null)
            {
                // From link
                reservation.clearErrors();
                reservation.clearWarnings();
                reservation.setReady(false);
            }

            // Do we have a ref specified in the URL?
            if (ref != null)
            {
                // Get it's data
                ReservationData data = reserve.findReservation(ref);
                if (data != null)
                {
                    // New ref data for existing object
                    reservation.setReservationData(data);

                    qsearch.setYyyymmdd(reservation.getStartDate().getYyyymmdd());
                }
                else
                {
                    // Make a new reservation object
                    reservation = new Reservation(srequest, qsearch);
                }
            }

            // else is our existing reservation already created?
            else if (reservation.getId() != null)
            {
                // yes
                // So if new apartment or date specified
                if (apt != null || dd != null || yyyymm != null)
                {
                    // make a new reservation
                    reservation = new Reservation(srequest, qsearch);
                }
                else
                {
                    // otherwise look for the persisted data
                    ReservationData data = reserve.findReservation(reservation.getId());
                    if (data != null)
                    {
                        // refresh the data
                        reservation.setReservationData(data);
                        qsearch.setYyyymmdd(reservation.getStartDate().getYyyymmdd());
                    }
                    else
                        // make a new reservation
                        reservation = new Reservation(srequest, qsearch);
                }
            }

            // Else this must be a non persisted reservation.
            else
            {
                // adjust unrequested booking
                if (apt != null) 
                    reservation.setAptId(apt);
                if (dd != null || yyyymm != null) 
                    reservation.updateFromQSearch(qsearch);
                if (apt != null || dd != null || yyyymm != null) 
                {
                    reservation.calculatePrice();
                    reservation.calcDiscount();
                }
            }

        }
        // Else we have no reservation object and we have to make one.
        // Do we have a reference?
        else if (ref != null)
        {
            // Does it exist?
            ReservationData data = reserve.findReservation(ref);
            if (data != null)
            {
                // refresh the data
                reservation = new Reservation(data);
                qsearch.setYyyymmdd(reservation.getStartDate().getYyyymmdd());
            }
            else
                // make a new reservation
                reservation = new Reservation(srequest, qsearch);

            if (ReservationStatus.REQUESTED.equals(reservation.getStatus())) 
                reservation.calculatePrice();
        }
        // else we know nothing
        else
        {
            // make a new reservation
            reservation = new Reservation(srequest, qsearch);
        }

        if (reservation.getId() != null) 
            session.setAttribute("reservation_id", reservation.getId());

        // find the apartment object
        Apartment apartment = null;
        if (reservation.getAptId() != null)
            apartment = Apartment.getApartment(reservation.getAptId());
        else if (apt != null) apartment = Apartment.getApartment(apt);
        srequest.setAttribute("apartment", apartment);

        // Set the manager
        if (reservation.getManager() == null)
        {
            User user = User.getCurrentUser();
            if (apartment == null)
                reservation.setManager(User.getDefaultManager().getName());
            else if (user != null && user.manages(apartment.getName()))
                reservation.setManager(user.getName());
            else
                reservation.setManager(apartment.getManager());
        }

        session.setAttribute("reservation", reservation);
        session.setAttribute("apartments", Apartment.getApartmentIds());
    }

    /* ------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------- */
    public static class Reservation
    {
        ReservationData data;
        ArrayList errors = new ArrayList();
        boolean ready;
        boolean realloc;
        ArrayList warnings = new ArrayList();
        int nights;

        Reservation(HttpServletRequest srequest, QSearch qsearch)
        {
            data = new ReservationData();
            updateFromQSearch(qsearch);
            data.setPostal(new Address());
            data.setAptId(srequest.getParameter("apt"));
            data.setLanguage((String) srequest.getSession().getAttribute(LangFilter.LANG));
            data.setPrice(ZERO);
            data.setDiscountCode(null);

            HttpSession session = srequest.getSession(false);
            data.setReferer((String) session.getAttribute("referer"));

            this.calculatePrice();
            this.calcDiscount();
        }

        /**
         * @param comment
         */
        public void addHistory(String comment)
        {
            data.addHistory(comment);
        }

        public void clearNotifications()
        {
            data.clearNotifications();
        }

        public List getHistory()
        {
            return data.getHistory();
        }

        Reservation(ReservationData data)
        {
            this.data = data;
        }

        public void addError(String s)
        {
            errors.add(s);
        }

        public void delError(String s)
        {
            errors.remove(s);
        }

        public void addWarning(String s)
        {
            warnings.add(s);
        }

        public boolean isDue()
        {
            int due = Configuration.getInstance().getIntProperty("total.days", 28);
            int days = new YyyyMmDd().daysTo(getStartDate());
            return days <= due;
        }

        public void calculatePrice()
        {
            int nights = getStartDate().daysTo(getEndDate());

            int price = 0;

            String aptId = getAptId();

            if (aptId == null)
            {
                data.setPrice(ZERO);
                return;
            }

            Apartment apt = Apartment.getApartment(aptId);
            if (apt == null)
            {
                data.setPrice(ZERO);
                return;
            }

            data.setCommissionPercent(apt.getPage().getIntPathProperty("A_commission"));
            
            YyyyMmDd day = new YyyyMmDd(getStartDate());
            StringBuffer fb = new StringBuffer();
            for (int i = 0; i < nights; i++)
            {
                int rent = apt.calculateRentInCents(day, i, nights, fb);
                if (day.getDayOfWeek() == GregorianCalendar.FRIDAY) fb.append(' ');
                price += rent;
                day.addDays(1);
            }

            setPrice(new BigDecimal(((price + 50) / 100) + ".00"));
            setPriceBasis(fb.toString());
        }

        public void clearErrors()
        {
            errors.clear();
        }

        public void clearWarnings()
        {
            warnings.clear();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#copy(it.colletta.reservation.ReservationData)
         */
        public void copy(ReservationData rd)
        {
            data.copy(rd);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object arg0)
        {
            return data.equals(arg0);
        }

        /* ------------------------------------------------------------------------------- */
        public String getAddress()
        {
            return getPostal().getAddress();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#getAdults()
         */
        public int getAdults()
        {
            return data.getAdults();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#getAptId()
         */
        public String getAptId()
        {
            return data.getAptId();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#getChildren()
         */
        public int getChildren()
        {
            return data.getChildren();
        }

        /* ------------------------------------------------------------------------------- */
        public String getCountry()
        {
            return getPostal().getCountry();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @return
         */
        public String getCurrency()
        {
            return data.getCurrency();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#getEmail()
         */
        public String getEmail()
        {
            return data.getEmail();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#getEndDate()
         */
        public YyyyMmDd getEndDate()
        {
            return data.getEndDate();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @return Returns the endDd.
         */
        public int getEndDd()
        {
            return getEndDate().getDd();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @return Returns the endMm.
         */
        public int getEndMm()
        {
            return getEndDate().getMm();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @return Returns the endYyyy.
         */
        public int getEndYyyy()
        {
            return getEndDate().getYyyy();
        }

        /* ------------------------------------------------------------------------------- */
        public int getNights()
        {
            return getStartDate().daysTo(getEndDate());
        }

        /* ------------------------------------------------------------------------------- */
        public void setNights(int nights)
        {
            this.nights=nights;
        }
        
        /* ------------------------------------------------------------------------------- */
        public void resolve()
        {
            if (nights>0)
            {
                YyyyMmDd end = new YyyyMmDd(getStartDate());
                end.addDays(nights);
                setEndDate(end);
                nights=0;
            }
        }
        
        /* ------------------------------------------------------------------------------- */
        public Iterator getErrors()
        {
            return errors.iterator();
        }

        /* ------------------------------------------------------------------------------- */
        public boolean hasError(String error)
        {
            return errors.contains(error);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#getEtaHh()
         */
        public int getEtaHh()
        {
            return data.getEtaHh();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#getFax()
         */
        public String getFax()
        {
            return data.getFax();
        }

        /* ------------------------------------------------------------------------------- */
        public String getCodice()
        {
            return data.getCodice();
        }


        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#getGroupId()
         */
        public String getGroupId()
        {
            return data.getGroupId();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#getId()
         */
        public String getId()
        {
            return data.getId();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#getInfants()
         */
        public int getInfants()
        {
            return data.getInfants();
        }

        /**
         * @return
         */
        public String getLanguage()
        {
            return data.getLanguage();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#getManager()
         */
        public String getManager()
        {
            return data.getManager();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#getName()
         */
        public String getName()
        {
            return data.getName();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#getNotes()
         */
        public String getNotes()
        {
            return data.getNotes();
        }

        /* ------------------------------------------------------------ */
        /**
         * @return
         */
        public String getNotesPrivate()
        {
            return data.getNotesPrivate();
        }

        /* ------------------------------------------------------------ */
        /**
         * @param str
         */
        public void setNotesPrivate(String str)
        {
            data.setNotesPrivate(str);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#getOwing()
         */
        public BigDecimal getOwing()
        {
            return data.getOwing();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#getOwing()
         */
        public BigDecimal getDepositOwing()
        {
            return data.getDepositOwing();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#getPaid()
         */
        public BigDecimal getPaid()
        {
            return data.getPaid();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#getPaid()
         */
        public BigDecimal getDeposit()
        {
            return data.getDeposit();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#getPhone1()
         */
        public String getPhone1()
        {
            return data.getPhone1();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#getPhone2()
         */
        public String getPhone2()
        {
            return data.getPhone2();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#getPostal()
         */
        public Address getPostal()
        {
            return data.getPostal();
        }

        public String getPostcode()
        {
            return getPostal().getPostcode();
        }

        /**
         * @return
         */
        public BigDecimal getPrice()
        {
            return data.getPrice();
        }

        public ReservationData getReservationData()
        {
            return data;
        }

        /**
         * @return
         */
        public String getSource()
        {
            return data.getSource();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#getStartDate()
         */
        public YyyyMmDd getStartDate()
        {
            return data.getStartDate();
        }

        /**
         * @return Returns the startDd.
         */
        public int getStartDd()
        {
            return getStartDate().getDd();
        }

        /**
         * @return Returns the startMm.
         */
        public int getStartMm()
        {
            return getStartDate().getMm();
        }

        /**
         * @return Returns the startYyyy.
         */
        public int getStartYyyy()
        {
            return getStartDate().getYyyy();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#getStatus()
         */
        public ReservationStatus getStatus()
        {
            return data.getStatus();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#getPrice()
         */
        public BigDecimal getTotal()
        {
            return data.getTotal();
        }

        /* ------------------------------------------------------------------------------- */
        public Iterator getWarnings()
        {
            return warnings.iterator();
        }

        /* ------------------------------------------------------------------------------- */
        public boolean hasErrors()
        {
            return errors.size() > 0;
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see java.lang.Object#hashCode()
         */
        public int hashCode()
        {
            return data.hashCode();
        }

        public boolean hasWarnings()
        {
            return warnings.size() > 0;
        }

        /**
         * @return
         */
        public boolean isActive()
        {
            return data.isActive();
        }

        /**
         * @param notificationType
         * @return
         */
        public boolean isNotified(String notificationType)
        {
            return data.isNotified(notificationType);
        }

        public boolean isReady()
        {
            return ready;
        }

        boolean isRealloc()
        {
            return realloc;
        }

        public void setAddress(String a)
        {
            getPostal().setAddress(a);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#setAdults(int)
         */
        public void setAdults(int n)
        {
            data.setAdults(n);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#setAptId(java.lang.String)
         */
        public void setAptId(String aptId)
        {
            if (aptId != null && (aptId.length() == 0 || aptId.startsWith("-"))) aptId = null;

            if (aptId != null && !aptId.equals(data.getAptId())) data.setManager(null);

            realloc = realloc || aptId == null && data.getAptId() != null || aptId != null && !aptId.equals(data.getAptId());

            data.setAptId(aptId);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#setChildren(int)
         */
        public void setChildren(int n)
        {
            data.setChildren(n);
        }

        /* ------------------------------------------------------------------------------- */
        public void setCountry(String c)
        {
            getPostal().setCountry(c);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @param currency
         */
        public void setCurrency(String currency)
        {
            data.setCurrency(currency);
        }

        /* ------------------------------------------------------------------------------- */
        public void setDiscountCode(String code)
        {
            data.setDiscountCode(code);
            calcDiscount();
        }

        /* ------------------------------------------------------------------------------- */
        public String getDiscountCode()
        {
            return data.getDiscountCode();
        }

        /* ------------------------------------------------------------------------------- */
        public void calcDiscount()
        {
            String code = data.getDiscountCode();
            data.removeAdjustments(Adjustment.__CODE);

            if (code != null && !code.trim().equals(""))
            {
                Discount d = Discount.getDiscount(code);
                if (d == null)
                {
                    this.addError("discountUnknown");
                }
                else
                {
                    boolean codeDiscount = false;
                    try
                    {
                        codeDiscount = d.calculate(data);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    if (!codeDiscount) this.addWarning("discountNotApplicable");
                }
            }
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#setEmail(java.lang.String)
         */
        public void setEmail(String newEmailAddress)
        {
            data.setEmail(newEmailAddress);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#setEndDate(com.mortbay.iwiki.YyyyMmDd)
         */
        public void setEndDate(YyyyMmDd newEndDate)
        {
            data.setEndDate(newEndDate);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @param endDd The endDd to set.
         */
        public void setEndDd(int endDd)
        {
            realloc = realloc || endDd != getEndDate().getDd();
            getEndDate().setDd(endDd);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @param endMm The endMm to set.
         */
        public void setEndMm(int endMm)
        {
            realloc = realloc || endMm != getEndDate().getMm();
            getEndDate().setMm(endMm);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @param endYyyy The endYyyy to set.
         */
        public void setEndYyyy(int endYyyy)
        {
            realloc = realloc || endYyyy != getEndDate().getYyyy();
            getEndDate().setYyyy(endYyyy);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#setEtaHh(int)
         */
        public void setEtaHh(int etaHh)
        {
            data.setEtaHh(etaHh);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#setFax(java.lang.String)
         */
        public void setFax(String newFax)
        {
            data.setFax(newFax);
        }

        /* ------------------------------------------------------------------------------- */
        public void setCodice(String codice)
        {
            data.setCodice(codice);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#setGroupId(java.lang.String)
         */
        public void setGroupId(String gid)
        {
            data.setGroupId(gid);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#setId(java.lang.String)
         */
        public void setId(String id)
        {
            data.setId(id);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#setInfants(int)
         */
        public void setInfants(int n)
        {
            data.setInfants(n);
        }

        /**
         * @param language
         */
        public void setLanguage(String language)
        {
            data.setLanguage(language);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#setManager(java.lang.String)
         */
        public void setManager(String mgr)
        {
            data.setManager(mgr);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#setName(java.lang.String)
         */
        public void setName(String newName)
        {
            data.setName(newName);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#setNotes(java.lang.String)
         */
        public void setNotes(String str)
        {
            data.setNotes(str);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#setPhone1(java.lang.String)
         */
        public void setPhone1(String newPhone1)
        {
            data.setPhone1(newPhone1);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#setPhone2(java.lang.String)
         */
        public void setPhone2(String newPhone2)
        {
            data.setPhone2(newPhone2);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#setPostal(it.colletta.reservation.Address)
         */
        public void setPostal(Address a)
        {
            data.setPostal(a);
        }

        /* ------------------------------------------------------------------------------- */
        public void setPostcode(String c)
        {
            getPostal().setPostcode(c);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @param price
         */
        public void setPrice(BigDecimal price)
        {
            data.setPrice(price);
        }

        /* ------------------------------------------------------------------------------- */
        public void setReady(boolean ready)
        {
            this.ready = ready;
        }

        /* ------------------------------------------------------------------------------- */
        void setRealloc(boolean realloc)
        {
            this.realloc = realloc;
        }

        /* ------------------------------------------------------------------------------- */
        void setReservationData(ReservationData data)
        {
            this.data = data;
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#setStartDate(com.mortbay.iwiki.YyyyMmDd)
         */
        public void setStartDate(YyyyMmDd newStartDate)
        {
            data.setStartDate(newStartDate);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @param startDd The startDd to set.
         */
        public void setStartDd(int startDd)
        {
            realloc = realloc || startDd != getStartDate().getDd();
            getStartDate().setDd(startDd);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @param startMm The startMm to set.
         */
        public void setStartMm(int startMm)
        {
            realloc = realloc || startMm != getStartDate().getMm();
            getStartDate().setMm(startMm);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @param startYyyy The startYyyy to set.
         */
        public void setStartYyyy(int startYyyy)
        {
            realloc = realloc || startYyyy != getStartDate().getYyyy();
            getStartDate().setYyyy(startYyyy);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @see it.colletta.reservation.ReservationData#toString()
         */
        public String toString()
        {
            return data.toString();
        }

        /* ------------------------------------------------------------------------------- */
        void updateFromQSearch(QSearch qsearch)
        {
            int nights = 0;
            qsearch.getNights();
            if (getStartDate() != null && getEndDate() != null)
                nights = getStartDate().daysTo(getEndDate());
            else
                nights = qsearch.getNights();
            if (nights <= 0 || nights > 100) nights = 7;

            data.setStartDate(qsearch);
            data.setEndDate(qsearch);
            data.getEndDate().addDays(nights);
            data.setAdults(qsearch.getAdults());
            data.setChildren(qsearch.getChildren());
            data.setInfants(qsearch.getInfants());
            ready = false;
            clearErrors();
            clearWarnings();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @return Returns the priceBasis.
         */
        public String getPriceBasis()
        {
            return data.getPriceBasis();
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @param priceBasis The priceBasis to set.
         */
        public void setPriceBasis(String priceBasis)
        {
            data.setPriceBasis(priceBasis);
        }

        /* ------------------------------------------------------------------------------- */
        /**
         * @return
         */
        public boolean isOccupiable()
        {
            return data.isOccupiable();
        }

        /* ------------------------------------------------------------------------------- */
        public String getBankPassPayments()
        {
            return ReservationManager.getPayments(data, "BankPass").toString();
        }

        /* ------------------------------------------------------------------------------- */
        public String getDagvinPayments()
        {
            return ReservationManager.getPayments(data, "massimo").toString();
        }

        /* ------------------------------------------------------------------------------- */
        public String getPayments()
        {
            return ReservationManager.getPayments(data).toString();
        }

        /* ------------------------------------------------------------ */
        public Adjustment addAdjustment(String type, BigDecimal amount, String comment)
        {
            return data.addAdjustment(type, amount, comment);
        }
        
        /* ------------------------------------------------------------ */
        public boolean hasAdjustments(String type)
        {
            return data.hasAdjustments(type);
        }
        
        /* ------------------------------------------------------------ */
        /*
         * @see it.colletta.reservation.ReservationData#getAdjustments()
         */
        public List getAdjustments()
        {
            return data.getAdjustments();
        }

        /* ------------------------------------------------------------ */
        /*
         * @see it.colletta.reservation.ReservationData#getReferer()
         */
        public String getReferer()
        {
            return data.getReferer();
        }

        /* ------------------------------------------------------------ */
        /**
         * @return
         * @see it.colletta.reservation.ReservationData#getCommissionPercent()
         */
        public int getCommissionPercent()
        {
            return data.getCommissionPercent();
        }


    }
}

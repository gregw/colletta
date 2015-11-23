<%@ page contentType="text/comma-separated-values; charset=ISO-8859-1" %><%@ page import="com.mortbay.iwiki.*,it.colletta.*,it.colletta.reservation.*,java.util.*" %><% 
  String[] others = {"&nbsp;","&nbsp;"};
  response.setHeader("Pragma", "no-cache");
  response.setHeader("Cache-Control", "no-cache,no-store");
        
  String lang = (String) request.getAttribute("lang");
  Locale locale = (Locale) request.getAttribute("locale");
  User user=(User) session.getAttribute("user");

  if (user==null || !(user.isManager() || user.isOwner() || user.isViewAll()))
  	return;

  YyyyMmDd start=new YyyyMmDd();
  String yyyymm =request.getParameter("yyyymm");
  if (yyyymm!=null)
  	start.setYyyymm(Integer.parseInt(yyyymm));
  start.setDd(1);
  YyyyMmDd end=new YyyyMmDd(start.getYyyy(),start.getMm(),start.getMaxDd());
  start.setMm(1);
 
  ArrayList bookings = new ArrayList();
  ReservationData[] data = ReservationManager.getInstance().findReservations(ReservationStatus.CONFIRMED,start,end);
  if (data==null)
    data = new ReservationData[0];


  String[] aptIds = Apartment.getApartmentIds();
   
  for (int a=0; a<aptIds.length; a++)
  {
      String id=aptIds[a];

      if (!user.isManager() && !user.isViewAll() && user.isOwner() && !user.owns(id))
	  continue;

      Apartment apt=Apartment.getApartment(id);

      YyyyMmDd day=new YyyyMmDd(start);
      while (!day.after(end))
      {
	  for (int r=0;r<data.length;r++)
	  { 
	    if (!id.equals(data[r].getAptId()))
		continue;
	    if (day.equals(data[r].getStartDate()))
		bookings.add(data[r]);
	  }
	  day.addDays(1);
      }

  }
%>Apt, Rif, Nome, Creato, Arriva, Notti, Genti, Da, Basis, Codice, Prezzo, Discont/Extra, Service, Owner, Totale, Pagato, Pagamento, Tassa, Referrer, Note
Apt, Ref, Name, Created, Arrive, Nights, People, By, Basis, Code, Price, Discount/Extra, Servizi, Proprietario, Total, Paid, Summary, Commission, Referrer, Note
<%
    for (int r=0;r<bookings.size();r++)
    { 
      ReservationData booking = (ReservationData)bookings.get(r);
      if ("Unavailable".equals(booking.getPriceBasis()))
          continue;
      request.setAttribute("booking",booking);
      String id = booking.getAptId();
      Apartment apt=Apartment.getApartment(id);
      request.setAttribute("apt",apt);
%>${apt.name}, ${booking.id}, ${booking.reportName}, ${booking.createDate.yyyyMmDd}, ${booking.startDate}, ${booking.nights}, ${booking.adults+booking.children+booking.infants}, ${booking.source}, ${booking.priceBasis}, ${booking.discountCode}, ${booking.price}, <%=booking.getPriceAdjustments()%>, <%=booking.getManagerAdjustments()%>, <%=booking.getOwnerAdjustments()%>, ${booking.total}, ${booking.paid}, "<%=ReservationManager.getPayments(booking)%>", "0.${booking.commissionPercent}", "${booking.referer}", "${booking.notesSummary}"
<%
    }
%>

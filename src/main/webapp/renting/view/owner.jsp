<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.mortbay.iwiki.*,it.colletta.*,it.colletta.reservation.*,java.util.*" %>
<%@ taglib prefix="tag" tagdir="/WEB-INF/tags" %>
<head>
<style type="text/css">
table
{
  border: 1px solid black; 
  border-right: 0px;
  border-bottom: 0px;
  border-spacing: 0px;
  page-break-inside: avoid;
  float: center;
  font-size: 10px;
  empty-cells: show;
}

td
{
  border: 1px solid black; 
  border-top: 0px;
  border-left: 0px;
  border-spacing: 0px;
  text-align: left;
  vertical-align: top;
  padding: 2px;
  font-size: 10px;
}

td.R
{
  text-align: right;
}

td.Apt, td.H
{
  text-align: left;
  font-weight: bold;
  width: 1;
  font-size: 10px;
}

</style>
</head>
<body>

<% 
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
  
  out.println("<B>Colletta Da "+start+" A "+end+"</b>&nbsp;");
  out.println("<a href=\"ownerCSV.jsp?yyyymm="+(end.getYyyy()*100+end.getMm())+"\">download</a>&nbsp;");
  out.println("<i>Stampato: "+user+" "+new YyyyMmDdHM()+"</i><br/>&nbsp;");

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
       	
%>
<table>
<tr>
<td class="H">Apt</td>
<td class="H">Rif</td>
<td class="H">Nome</td>
<td class="H">Creato</td>
<td class="H">Arriva</td>
<td class="H">Notti</td>
<td class="H">Genti</td>
<td class="H">da</td>
<td class="H">Basis</td>
<td class="H">Codice</td>
<td class="H">Prezzo&nbsp;&euro;</td>
<td class="H">Extra/Sconto&nbsp;&euro;</td>
<td class="H">Services&nbsp;&euro;</td>
<td class="H">Proprietario&nbsp;&euro;</td>
<td class="H">Totale&nbsp;&euro;</td>
<td class="H">Pagato&nbsp;&euro;</td>
<td class="H">Pagamenti</td>
<td class="H">Tassa&nbsp;%</td>
<td class="H">Referrer</td>
<td class="H">Note</td>
</tr>

<tr>
<td class="H">Apt</td>
<td class="H">Ref</td>
<td class="H">Name</td>
<td class="H">Created</td>
<td class="H">Arrive</td>
<td class="H">Nights</td>
<td class="H">People</td>
<td class="H">by</td>
<td class="H">Basis</td>
<td class="H">Code</td>
<td class="H">Price&nbsp;&euro;</td>
<td class="H">Extra/Discount&nbsp;&euro;</td>
<td class="H">Services&nbsp;&euro;</td>
<td class="H">Owner&nbsp;&euro;</td>
<td class="H">Total&nbsp;&euro;</td>
<td class="H">Paid&nbsp;&euro;</td>
<td class="H">Payments</td>
<td class="H">Commission&nbsp;%</td>
<td class="H">Referrer</td>
<td class="H">Note</td>
</tr>


<%
    String last=null;
    for (int r=0;r<bookings.size();r++)
    { 
      ReservationData booking = (ReservationData)bookings.get(r);
      request.setAttribute("booking",booking);
      String id = booking.getAptId();
      Apartment apt=Apartment.getApartment(id);
   	  request.setAttribute("apt",apt);
   	  

%>
      <tr>
<%        
   	  if (!id.equals(last)) 
   	  {
          int rows=1;
          while ((r+rows)<bookings.size() && 
                 id.equals(((ReservationData)bookings.get(r+rows)).getAptId()))
                 rows++;
%>
          <td class="Apt" rowspan="<%=rows%>">${apt.name}</td>
<%
        } 
%>
        <td class="ID"><a href="/renting/book?ref=${booking.id}">${booking.id}</td>
        <td>${booking.name}</td>
        <td>${booking.createDate.yyyyMmDd}</td>
        <td>${booking.startDate}</td>
        <td>${booking.nights}</td>
        <td>${booking.adults}+${booking.children}+${booking.infants}</td>
        <td>${booking.source}</td>
        <td>${booking.priceBasis}</td>
        <td>${booking.discountCode}</td>
        <td class="R">${booking.price}</td>
        <td class="R"><%=booking.getPriceAdjustments()%></td>
        <td class="R"><%=booking.getManagerAdjustments()%></td>
        <td class="R"><%=booking.getOwnerAdjustments()%></td>
        <td class="R"><b>${booking.total}</b></td>
        <td class="R"><b>${booking.paid}</b></td>
        <td class="R"><%=ReservationManager.getPayments(booking)%></td>
        <td class="R">${booking.commissionPercent}%</td>
        <td>${booking.referer}&nbsp;</td>
        <td>${booking.notes}&nbsp;</td>
      </tr>
<%
      last=id;
    }
%>
</table>
</body>


<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.mortbay.iwiki.*,it.colletta.*,it.colletta.reservation.*,java.util.*" %>
<%@ taglib prefix="tag" tagdir="/WEB-INF/tags" %>
<head>
<style type="text/css">
table
{
  border-collapse: collapse ;
  border: 1px solid black; 
  padding: 4px;
  page-break-inside: avoid;
  float: center;
  font-size: 10px;
}

table.Cal
{
  page-break-after: always;
  padding: 2px;
  width: 95%;
  font-size: 10px;
}

td
{
  border: 1px solid black; 
  text-align: left;
  vertical-align: top;
  padding: 1px;
  line-height: 90%;
  font-size: 10px;
}

td.R
{
  border: 1px solid black; 
  text-align: right;
  vertical-align: top;
  padding: 1px;
  line-height: 90%;
  font-size: 10px;
}

td.Apt, td.H
{
  text-align: left;
  font-weight: bold;
  width: 1;
  font-size: 10px;
}

td.Day
{
  text-align: center;
  font-family: mono,fixed;
  font-size: 10px;
}


td.ID
{
  font-family: mono,fixed;
  font-size: 10px;
}

td.M
{
  border-right: 0px solid black; 
  color: #d0d0d0;
  text-align: center;
  padding-left: 1px;
  padding-right: 0px;
  font-family: mono,fixed;
  font-size: 10px;
}

td.Mb
{
  border-right: 0px solid black;
  border-left: 1px dashed #d0d0d0; 
  text-align: center;
  padding-left: 0px;
  padding-right: 0px;
  font-family: mono,fixed;
  font-size: 10px;
}

td.L
{
  border-left: 0px solid black; 
  border-right: 0px solid black; 
  text-align: center;
  padding-left: 0px;
  padding-right: 0px;
  font-family: mono,fixed;
  font-size: 10px;
}

td.A
{
  border-left: 0px solid black; 
  color: #d0d0d0;
  text-align: center;
  padding-left: 0px;
  padding-right: 1px;
  font-family: mono,fixed;
  font-size: 10px;
}

td.Ab
{
  border-left: 0px solid black;
  border-right: 1px dashed #d0d0d0;  
  text-align: center;
  padding-left: 0px;
  padding-right: 0px;
  font-family: mono,fixed;
  font-size: 10px;
}


</style>
</head>
<body>

<% 
  String[] others = {"CM-Kimono","DM-Maxine","GB-Gardino","FF-Cuneo","DO/A-Bombon","BF-Marios","&nbsp;","&nbsp;"};
  response.setHeader("Pragma", "no-cache");
  response.setHeader("Cache-Control", "no-cache,no-store");
        
  String lang = (String) request.getAttribute("lang");
  Locale locale = (Locale) request.getAttribute("locale");
  User user=(User) session.getAttribute("user");

  if (user==null || !(user.isManager() || user.isViewAll()))
     return;

  YyyyMmDd start=new YyyyMmDd();
  String yyyymm =request.getParameter("yyyymm");
  if (yyyymm!=null)
  	start.setYyyymm(Integer.parseInt(yyyymm));
  
  start.setDd(1);
  YyyyMmDd end=new YyyyMmDd(start.getYyyy(),start.getMm(),start.getMaxDd());
  
  out.println("<B>Colletta Da "+start+" A "+end+"</b>&nbsp;");
  out.println("<i>Stampato: "+user+" "+new YyyyMmDdHM()+"</i><br/>&nbsp;");

  start.addDays(-1);
  
  ReservationData[] data = ReservationManager.getInstance().findReservations(ReservationStatus.CONFIRMED,start,end);
  if (data==null)
    data = new ReservationData[0];
  
  ArrayList bookings = new ArrayList();
    
%>

<table class="Cal">
   <tr>
     <td class="H">Appartamento</td>   
<%
   String[] aptIds = Apartment.getApartmentIds();
   
   for (int d=1;d<=end.getDd();d++)
   {
%>
      <td class="Day" colspan="3">&nbsp;<%=(d<=9?"0":"")+d%>&nbsp;</td>
<%   
   }
%>
   </tr>
   <tr>
<%   
   ArrayList ra = new ArrayList();
   
   for (int a=0; a<aptIds.length; a++)
   {
      String id=aptIds[a];
      String genti=null;
      Apartment apt=Apartment.getApartment(id);
   	request.setAttribute("apt",apt);
   	
%>
      <td class="Apt">${apt.displayName}</td>
<%
		 ra.clear();
       for (int r=0;r<data.length;r++)
       { 
       	if (!id.equals(data[r].getAptId()))
       	    continue;
       	
       	int i=0;
       	YyyyMmDd d=data[r].getStartDate();
       	while (i< ra.size() && d.after(((ReservationData)ra.get(i)).getStartDate()))
       	    i++;
       	ra.add(i,data[r]);
       }
       
       
       int nri=0;
       ReservationData nr=null;
       if (nri<ra.size())
           nr=(ReservationData)ra.get(nri);
       if (nr !=null && !bookings.contains(nr))
       	bookings.add(nr);
       
       
       YyyyMmDd day=new YyyyMmDd(start);
       boolean ib=nr!=null&& day.isBetween(nr.getStartDate(),nr.getEndDate());
       
       day.addDays(1);
       
       int b=0;
       
       for (int d=1;d<=end.getDd();d++)
       {
          day.setDd(d);
       	 
       	 // handle the morning
       	 if (ib)
       	 {
       	     if (nr!=null && day.equals(nr.getEndDate()))
       	     {
       	     	  // last day of booking
       	     	  ib=false;
                 nri++;
                 nr=(nri<ra.size())?(ReservationData)ra.get(nri):null;
                 if (nr !=null && !bookings.contains(nr))
       	          bookings.add(nr);
       	          
                 char c= (genti!=null && b<genti.length())
                    ?genti.charAt(b):' ';
       	     	  genti=null;
%>
                 <td class="Mb">&gt;<br/><%=c%></td>
<%              
       	     }
       	     else
       	     {
       	        // within booking
       	        String s="-";
       	        if (b<nr.getName().length())
       	          s=""+nr.getName().charAt(b);
       	        if (genti!=null && b<genti.length())
       	          s=s+"<br/>"+genti.charAt(b);
       	        else
       	          s=s+"<br/>&nbsp;";
       	        b++;
%>
                 <td class="Mb"><%=s%></td>
<%                               
       	     }
       	 }
       	 else
       	 {  
       	    // no booking
%>
             <td class="M">&nbsp;</td>
<%                                                
       	 }
       	 
       	 
       	 
       	 // handle lunchtime
       	 if (ib)
       	 {
       	    // within booking
       	    String s="-";
       	    if (b<nr.getName().length())
       	      s=""+nr.getName().charAt(b);
       	      
            if (genti!=null && (b)<genti.length())
               s=s+"<br/>"+genti.charAt(b);
            else
               s=s+"<br/>&nbsp;";
       	   b++;
%>
            <td class="L"><%=s%></td>
<%                    
       	 }
       	 else            
       	 {
%>
             <td class="L">&nbsp;</td>       	 
<%       	
          } 
          
          
          
       	 // handle the afternoon
       	 if (ib)
       	 {
       	    // within booking
       	    String s="-";
       	    if (b<nr.getName().length())
       	      s=""+nr.getName().charAt(b);
            if (genti!=null && (b)<genti.length())
               s=s+"<br/>"+genti.charAt(b);
            else
               s=s+"<br/>&nbsp;";
       	   b++;
%>
            <td class="Ab"><%=s%></td>
<%                    
       	 }
       	 else if (nr!=null && day.equals(nr.getStartDate()))
       	 {
       	    // first day of booking
       	    ib=true;
       	    b=0;
       	    genti=
       	       "XXXXXXXXXX".substring(0,nr.getAdults())+
       	       "oooooooooo".substring(0,nr.getChildren())+
       	       "++++++++++".substring(0,nr.getInfants());
            if (nr.getNotes()!=null)
               genti += " "+nr.getNotes();
       	       
       	    char c=genti.length()>0?genti.charAt(0):'?';
       	    if (genti.length()>0)
       	       genti=genti.substring(1);
%>
             <td class="Ab">&lt;<br/><%=c%></td>
<%                                          
            
       	 }
       	 else
       	 {
       	    // no booking
%>
             <td class="A">&nbsp;<br/>&nbsp;</td>
<%                                  
          }
       }
%>
       </tr>
<%      
   }
%>   
   <tr><td></td>
<%
   for (int d=1;d<=end.getDd();d++)
   {
%>
      <td class="Day" colspan="3"><%=(d<=9?"0":"")+d%></td>
<%   
   }
%>
</tr>

<% for (int l=0;l<others.length;l++) { %>
<tr><td><%=others[l]%></td>
<%
   for (int d=1;d<=end.getDd();d++)
   {
%>
      <td class="Day" colspan="3">&nbsp;<br/>&nbsp;</td>
<%   
   }
%>
</tr>
<% } %>
<tr><td></td>
<%
   for (int d=1;d<=end.getDd();d++)
   {
%>
      <td class="Day" colspan="3"><%=(d<=9?"0":"")+d%></td>
<%   
   }
%>
</tr>



</table>
<br/>
<%   
  out.println("<B>Colletta Da "+start+" A "+end+"</b><br/>");
  out.println("<i>Stampato: "+user+" "+new YyyyMmDdHM()+"</i><br/>");
%>
<table>
<tr>
<td class="H">Appartamento</td>
<td class="H">Rif</td>
<td class="H">Arriva</td>
<td class="H">Portenza</td>
<td class="H">Quanti</td>
<td class="H">Genti</td>
<td class="H">Nome</td>
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
          <td class="Apt" rowspan="<%=rows%>">${apt.displayName}</td>
<%
        } 
%>
        <td class="ID">${booking.id}</td>
        <td>${booking.startDate}</td>
        <td>${booking.endDate}</td>
        <td class="R">&euro;&nbsp;${booking.total - booking.paid}</td>
        <td>${booking.adults}/${booking.children}/${booking.infants}</td>
        <td>${booking.name}</td>
        <td>${booking.notes}&nbsp;</td>
      </tr>
<%
      last=id;
    }
    if (data.length>0)
	 out.println("&nbsp;<br/>");
%>
</table>
</body>

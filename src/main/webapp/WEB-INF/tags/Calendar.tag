<%@ tag
           import="com.mortbay.iwiki.*,it.colletta.*,it.colletta.reservation.*,java.util.*"
           body-content="empty"
%>

<%@ attribute name="apt"  type="com.mortbay.iwiki.Page" required="false" %>
<%@ attribute name="highlight"  type="it.colletta.reservation.ReservationData" required="false" %>
<%@ attribute name="click"  type="String" required="false" %>

<%    
        YyyyMmDd today = new YyyyMmDd();
        User user = (User)session.getAttribute("user");
        boolean viewBookings=(user!=null && (apt!=null&&user.manages(apt.getDirName()) || apt!=null&&user.owns(apt.getDirName()) || user.isViewAll() ));
        QSearch qsearch = (QSearch) session.getAttribute("qsearch");
  
        YyyyMmDd date = (highlight==null)?new YyyyMmDd(qsearch):new YyyyMmDd(highlight.getStartDate());
        date.setDd(1);
        
        ReservationData[] reservations = apt==null?null:ReservationManager.getInstance().findReservations(apt.getDirName(),date.getYyyymm()); 
        String q="?apt=";
        if (apt!=null)
        	q+=apt.getDirName();

        // Localise
        Locale locale=(Locale)request.getAttribute(LangFilter.LOCALE);
        Locale altLocale=(Locale)request.getAttribute(LangFilter.ALTLOCALE);
        
        // Display
        out.println("<table class=\"Cal\">");
        out.println("<tr><td class=\"CalMonth\" >");
        out.println("<a href=\""+request.getRequestURI()+q+"&yyyymm="+
                YyyyMmDd.normalizeYyyyMm(date.getYyyymm()-1)+"\" class=\"btn btn-default\"><i class=\"glyphicon glyphicon-backward\"></i></a>");
        out.println("</td><td class=\"CalMonth\" colspan=\"5\">");
        out.println(YyyyMmDd.month(date.getMm(),locale)+"&nbsp;-&nbsp;"+YyyyMmDd.month(date.getMm(),altLocale)+"<br/>"+date.getYyyy());
        out.println("</td><td class=\"CalMonth\" >");
        out.println("<a href=\""+request.getRequestURI()+q+"&yyyymm="+
                YyyyMmDd.normalizeYyyyMm(date.getYyyymm()+1)+"\" class=\"btn btn-default\"><i class=\"glyphicon glyphicon-forward\"></i></a>");
        out.println("</td></tr>");

        out.println("<tr>");
        for (int d=0;d<7;d++)
            out.println("<td class=\"CalDay\" >"+YyyyMmDd.shortWeekday(d,locale)+"</td>");
        out.println("</tr>");
        out.println("<tr>");
        for (int d=0;d<7;d++)
            out.println("<td class=\"CalAltDay\" >"+YyyyMmDd.shortWeekday(d,altLocale)+"</td>");
        out.println("</tr>");
        
        int mm=date.getMm();
        for (int w=0;w<6;w++)
        {
            out.println("<tr>");
            for (int d=0;d<7;d++)
            {
                if (date.getMm()==mm && d==date.getDayOfWeek())
                {
                    int dd=date.getDd();
		            boolean old=date.before(today);
                    String dc=old?"CalX":"Cal";
                    String arg=null;
                    boolean link=viewBookings;
    
                    if (reservations!=null && reservations[dd]!=null)
                    {
				        if (viewBookings || !old)
        				{
                          dc="CalB";
                          if (ReservationStatus.REQUESTED.equals(reservations[dd].getStatus()) ||
			                ReservationStatus.APPROVED.equals(reservations[dd].getStatus()))
                            dc="CalR";
                          else if (user!=null && (user.isManager()||user.isOwner()) && "Unavailable".equals(reservations[dd].getPriceBasis()))
                            dc="CalU";
                        }
	                    if (dd>1 && !date.equals(reservations[dd].getStartDate()))
	                        link=false;
	                    arg=q+"&ref="+reservations[dd].getId();
                    }
                    else
                    {
                        link=!old;
                        arg=q+"&mm="+mm+"&dd="+dd;
                    }
                            
                    if (highlight!=null && 
                        !date.before(highlight.getStartDate()) && 
                        date.before(highlight.getEndDate()))
                        dc+="TR";
                            
                    out.println("<td class=\""+dc+"\">");
                    String path=request.getRequestURI();
                    if (click!=null)
                        path=request.getContextPath()+click;
                    if (link)
                        out.print("<a class=\""+dc+"\" href=\""+path+arg+"\">"+dd+"</a>");
                    else    
                        out.print(dd);
                            
                    out.println("</td>");
                    date.getCalendar().add(Calendar.DAY_OF_MONTH, 1);
                }
                else
                {
                    out.println("<td class=\"CalNull\">&nbsp;</td>");
                }
            }
            out.println("</tr>");
            if (date.getMm()!=mm)
                break;
        }
            
        out.println("</table>");
%>

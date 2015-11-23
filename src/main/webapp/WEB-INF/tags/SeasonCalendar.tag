<%@ tag
import="com.mortbay.iwiki.*,it.colletta.*,it.colletta.reservation.*,java.util.*"
body-content="empty"
%>

<%@ attribute name="apt"  type="com.mortbay.iwiki.Page" required="false" %>
<%@ attribute name="highlight"  type="it.colletta.reservation.ReservationData" required="false" %>
<%@ attribute name="yyyymm" type="java.lang.String" required="true" %>
<%    


// Localise
Locale locale=(Locale)request.getAttribute(LangFilter.LOCALE);
Locale altLocale=(Locale)request.getAttribute(LangFilter.ALTLOCALE);

YyyyMmDd date = new YyyyMmDd (yyyymm+"-01");



// Display
out.println("<table class=\"Cal\">");
out.println("<tr><td class=\"CalMonth\" >");
out.println("<td class=\"CalMonth\" colspan=\"7\">");
out.println(YyyyMmDd.month(date.getMm(),locale)+"&nbsp;-&nbsp;"+YyyyMmDd.month(date.getMm(),altLocale)+"<br/>"+date.getYyyy());
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
         
         int season = Apartment.getSeason (date);
         String seasonClass = "";
         switch (season)
         {
             case Apartment.LOW:
             {
                 seasonClass = "CalLow";
                 break;
             }
             case Apartment.MID:
             {
                 seasonClass = "CalMid";
                 break;
             }
             case Apartment.PEAK:
             {
                 seasonClass = "CalPeak";
                 break;
             }
         }
 
                 
         out.println("<td class=\""+seasonClass+"\">"); 
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

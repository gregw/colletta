<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.mortbay.iwiki.Page,java.util.*,java.io.*,it.colletta.reservation.discount.ScDiscount" %>
<%@ taglib prefix="tag" tagdir="/WEB-INF/tags" %>

<% 
  String email = request.getParameter("email");
  String lang = (String) request.getAttribute("lang");
  Page p = (Page) request.getAttribute("page");
  if (p==null)
    return;
%>

<h1><%= p.getTitle(lang) %></h1>

<%
  Map lastPOST=(Map)request.getSession().getAttribute("lastPOST");
  String code=null;
  if (lastPOST !=null && lastPOST.containsKey("emailaddr"))
  {
    String[] emaila=(String[])lastPOST.get("emailaddr");
    if (emaila.length==1)
    {
      email = emaila[0];
      
      if (email!=null && email.length()>0)
      {
      	code=ScDiscount.encode(email);
      	request.setAttribute("code",code);
      }
    }
  }
  
  
  if (lastPOST !=null && lastPOST.containsKey("form") &&
      "survey".equals(((String[])lastPOST.get("form"))[0]))
  {
      String filename=code==null?request.getSession().getId():code;
  
      File survey = new File(getServletContext().getRealPath("/WEB-INF/survey/"));
      if (!survey.exists())
      	  survey.mkdir();
      File result= new File(survey,filename);
      	
      PrintWriter writer = new PrintWriter(new FileWriter(result));
      Iterator iter = lastPOST.entrySet().iterator();
      while (iter.hasNext())
      {
      	    Map.Entry entry = (Map.Entry)iter.next();
      	    writer.print(entry.getKey());
      	    writer.print("=");
      	    writer.println(Arrays.asList((String[])entry.getValue()));
      }
      writer.close();
 	
      if (code!=null)
      {
%>
        <div class="code">
        ${properties.thankyou}
        ${properties.yourcode} <b>${code}</b>
        ${properties.explain}
        <br/>&nbsp;<br/>
        </div>
<%
      }
      else
      {
%>
        <div class="code">
        ${properties.thankyou}
        ${properties.nocode}
        <br/>&nbsp;<br/>
        </div>
<%
      }
  }
  else
  {
%>
    <jsp:include page="text.jsp" flush="true"/>
<%
  }
  
  request.setAttribute("email",email);
%>

<form method="POST">
<input type="hidden" name="form" value="survey"/>

<table class="survey" >

<tag:SurveySubject  subject="colletta"/>
<tag:SurveyQuestion subject="colletta" name="location"/>
<tag:SurveyQuestion subject="colletta" name="activities"/>
<tag:SurveyQuestion subject="colletta" name="value"/>
<tag:SurveyQuestion subject="colletta" name="overall"/>
<tag:SurveyQuestion subject="colletta" name="return"/>
<tag:SurveyComment  subject="colletta"/>

<tag:SurveySubject  subject="apartment"/>
<tag:SurveyQuestion subject="apartment" name="location"/>
<tag:SurveyQuestion subject="apartment" name="finish"/>
<tag:SurveyQuestion subject="apartment" name="clean"/>
<tag:SurveyQuestion subject="apartment" name="value"/>
<tag:SurveyQuestion subject="apartment" name="overall"/>
<tag:SurveyComment  subject="apartment"/>

<tag:SurveySubject  subject="osteria"/>
<tag:SurveyQuestion subject="osteria" name="food"/>
<tag:SurveyQuestion subject="osteria" name="service"/>
<tag:SurveyQuestion subject="osteria" name="value"/>
<tag:SurveyQuestion subject="osteria" name="overall"/>
<tag:SurveyComment  subject="osteria"/>

<tag:SurveySubject  subject="services"/>
<tag:SurveyQuestion subject="servides" name="website"/>
<tag:SurveyQuestion subject="services" name="booking"/>
<tag:SurveyQuestion subject="services" name="payment"/>
<tag:SurveyQuestion subject="services" name="welcome"/>
<tag:SurveyQuestion subject="services" name="staff"/>
<tag:SurveyQuestion subject="services" name="network"/>
<tag:SurveyQuestion subject="services" name="departure"/>
<tag:SurveyComment  subject="services"/>
<tr><td>&nbsp;</td></tr>


<tr>
<th colspan="2">${properties.found}:</th>
<td>&nbsp;</td>
<td colspan="4"><input type="text" name="found" value="${lastPOST.found[0]}"></td>
</tr>

<tr><td>&nbsp;</td></tr>

<tr>
<th colspan="2">${properties.email}:</th>
<td>&nbsp;</td>
<td colspan="4"><input type="text" name="emailaddr" value="${email}"></td>
</tr>
<tr>
<th colspan="2">${properties.date}:</th>
<td>&nbsp;</td>
<td colspan="4"><input type="text" name="date" value="${lastPOST.date[0]}"></td>
</tr>
<tr>
<th colspan="2">${properties.apartment}:</th>
<td>&nbsp;</td>
<td colspan="4"><input type="text" name="apartment" value="${lastPOST.apartment[0]}"></td>
</tr>
<tr>
<th colspan="2">${properties.reference}:</th>
<td>&nbsp;</td>
<td colspan="4"><input type="text" name="reference" value="${lastPOST.reference[0]}"></td>
</tr>

<tr><td>&nbsp;</td></tr>


<tr><td colspan="7">

<%
  if (lastPOST !=null && lastPOST.containsKey("form") &&
      "survey".equals(((String[])lastPOST.get("form"))[0]))
  {
%>
<tag:Button name="update" image="update.gif" enabled="true"/>
<%
  }
  else
  {
%>
<tag:Button name="continue" image="continue.gif" enabled="true"/>
<%
  }
%>
</td></tr>



</table>
</form>
&nbsp;
<br/>


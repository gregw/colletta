<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="it.colletta.*" %>
<%@ page import="com.mortbay.iwiki.*" %>
<%@ taglib prefix="tag" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="qsearch" scope="session" class="it.colletta.QSearch" />
<jsp:setProperty name="qsearch"  property="yyyymm"  />

<% 
  String lang = (String) request.getAttribute("lang");
  Page p = (Page) request.getAttribute("page");
  User user=(User) session.getAttribute("user");
  String[] images = p.getImages();
  String location = null;
  if (images!=null && images.length>0 && images[images.length-1].startsWith("zz"))
  {
    location=images[images.length-1];
    request.setAttribute("location",location);
  }
  
  Apartment apt = Apartment.getApartment(p.getDirName());
  request.setAttribute("apt",apt);
%>


<table>
<tr><td>
<h1><%= p.getTitle(lang) %></h1>
<br/>
<table class="AptV">
<tr>
<td class="AptVH">${properties.H_sleeps}:</td><td class="AptV">${properties.A_sleeps_max}</td>     
<td class="AptVH">${properties.H_layout}:</td><td class="AptV">${properties.A_layout}</td>            
</tr>

<tr>
<td class="AptVH">${properties.H_bedrooms}:</td><td class="AptV">${properties.A_bedrooms}</td>     
<td class="AptVH">${properties.H_bathrooms}:</td><td class="AptV">${properties.A_bathrooms}</td>            
</tr>

<tr>
<td class="AptVH">${properties.H_kitchen}:</td><td class="AptV"><% if(p.getIntProperty("A_kitchen")>0){%><img src="${contextPath}/images/tick.gif"/><%}else{%><img src="${contextPath}/images/cross.gif"/><%}%></td>     
<td class="AptVH">${properties.H_laundry}:</td><td class="AptV"><% if(p.getIntProperty("A_laundry")>0){%><img src="${contextPath}/images/tick.gif"/><%}else{%><img src="${contextPath}/images/cross.gif"/><%}%></td>            
</tr>

<tr>
<td class="AptVH">${properties.H_terrace}:</td><td class="AptV"><% if(p.getIntProperty("A_terrace")>0){%><img src="${contextPath}/images/tick.gif"/><%}else{%><img src="${contextPath}/images/cross.gif"/><%}%></td>     
<td class="AptVH">${properties.H_garden}:</td><td class="AptV"><% if(p.getIntProperty("A_garden")>0){%><img src="${contextPath}/images/tick.gif"/><%}else{%><img src="${contextPath}/images/cross.gif"/><%}%></td>            
</tr>

<tr>
<td class="AptVH">${properties.H_TV}:</td><td class="AptV">${properties.A_TV}</td>     
<td class="AptVH">${properties.H_payTV}:</td><td class="AptV"><% if(p.getIntProperty("A_payTV")>0){%><img src="${contextPath}/images/tick.gif"/><%}else{%><img src="${contextPath}/images/cross.gif"/><%}%></td>            
</tr>

<tr>
<td class="AptVH">${properties.H_comp}:</td><td class="AptV"><% if(p.getIntProperty("A_comp")>0){%><img src="${contextPath}/images/tick.gif"/><%}else{%><img src="${contextPath}/images/cross.gif"/><%}%></td>     
<td class="AptVH">${properties.H_DVD}:</td><td class="AptV"><% if(p.getIntProperty("A_DVD")>0){%><img src="${contextPath}/images/tick.gif"/><%}else{%><img src="${contextPath}/images/cross.gif"/><%}%></td>            
</tr>

<tr>
<td class="AptVH">${properties.H_aircond}:</td><td class="AptV"><% if(p.getIntProperty("A_aircond")>0){%><img src="${contextPath}/images/tick.gif"/><%}else{%><img src="${contextPath}/images/cross.gif"/><%}%></td>     
<td class="AptVH">${properties.H_smoking}:</td><td class="AptV"><% if(p.getIntProperty("A_smoking")>0){%><img src="${contextPath}/images/tick.gif"/><%}else{%><img src="${contextPath}/images/cross.gif"/><%}%></td>            
</tr>

<tr>    
<td class="AptVH">Internet:</td><td class="AptV"><img src="${contextPath}/images/tick.gif"/></td> 
<td class="AptVH">${properties.H_pets}:</td><td class="AptV"><% if(p.getIntProperty("A_pets")>0){%><img src="${contextPath}/images/tick.gif"/><%}else{%><img src="${contextPath}/images/cross.gif"/><%}%></td>            
</tr>

<% if (user!=null && (user.isManager() || user.isEditor() || user.owns(p.getDirName()))) { %>
<tr>
<td>&nbsp;</td>
</tr>

<tr>
<td class="AptVH">${properties.H_owner}:</td><td class="AptV" colspan="3">
<%
  User[] users = User.getUsers();
  for (int i=0; i<users.length; i++)
  {
      User u = users[i];
      if (!u.owns(p.getDirName()))
          continue;
%>
      <tag:UserSelect value="<%=u.getName()%>" readonly="true"/>
<% } %>
 <a href="../owner.jsp?yyyymm=${qsearch.yyyymm}">OWNER REPORT</a>
</td>  
</tr>

<tr>
<td class="AptVH">Group:</td><td class="AptV" colspan="3">${properties.A_groupDiscount}</td>
</tr>

<tr>
<td class="AptVH">${properties.H_manager}:</td><td class="AptV" colspan="3">
<%
  for (int i=0; i<users.length; i++)
  {
      User u = users[i];
      if (!u.getName().equals(p.getProperty("A_manager")))
          continue;
          
%>
      <tag:UserSelect value="<%=u.getName()%>" readonly="true"/><br/> 
<% 
  }
  for (int i=0; i<users.length; i++)
  {
      User u = users[i];
      if (u.getName().equals(p.getProperty("A_manager")))
          continue;
      if (!u.managerOf(p.getDirName()))
          continue;
%>
      <tag:UserSelect value="<%=u.getName()%>" readonly="true"/><br/> 
<% 
  }
%>
</td>  
</tr>

<tr>
<td class="AptVH">${properties.H_notes}:</td><td class="AptV" colspan="3">${properties.notes}</td>
</tr>
<% } %>

</table>
<br/>
<table class="Price">
<tr> <td class="PriceH">${properties.H_nights}</td><td class="PriceH">${properties.H_sLow}</td><td class="PriceH">${properties.H_sPeak}</td>       </tr>
<tr> <td class="PriceC">7</td>    <td class="Price">&euro;${apt.rentLow7}</td>  <td class="Price">&euro;${apt.rentPeak7}</td>  </tr>

<% if (apt.getMinStay()<=3 ) { %>
<tr> <td class="PriceC">3</td>    <td class="Price">&euro;${apt.rentLow3}</td>  <td class="Price">&euro;${apt.rentPeak3}</td>  </tr>

<% if (apt.getMinStay()<=1) { %>
<tr> <td class="PriceC">1</td>    <td class="Price">&euro;${apt.rentLow1}</td>  <td class="Price">&euro;${apt.rentPeak1}</td>  </tr>
<% }
} %>

<tr><td colspan="4">${properties.H_minStay}: ${apt.minStay} ${properties.H_nights}
<br/>
${properties.H_plusFees}
</td></tr>
</table>

<br/>
<a href="../../book/?apt=${page.dirName}">${properties.bookit}</a>
&nbsp; &nbsp; &nbsp; &nbsp;
<a href="mailto:${apt.managerUser.email}?Subject=Colletta%20${properties.inquiry}%20${apt.displayName}%3f">${properties.inquiry}?</a>
&nbsp; &nbsp; &nbsp; &nbsp; 
<a href="../../conditions/">${properties.conditions}</a>
<br/>

<p>
<jsp:include page="text.jsp"/>
</p>

<br/>

<% if (location!=null)
{ %>
<img src="${location}"/>
<% } %>

<% if (p.getProperty("A_forsale")!=null)
{ %>
<p>
    <a href="${properties.A_forsale}"><img src="/images/forsale.jpg"/>${properties.forsale}</a>
<br/>
</p>
<% } %>




<br/>
<a href="../../book/?apt=${page.dirName}">${properties.bookit}</a>
&nbsp; &nbsp; &nbsp; &nbsp;
<a href="mailto:${apt.managerUser.email}?Subject=Colletta%20${properties.inquiry}%20${apt.displayName}%3f">${properties.inquiry}?</a>
&nbsp; &nbsp; &nbsp; &nbsp; 
<a href="/renting/conditions/">${properties.conditions}</a>


</td>

<td>
<tag:Calendar click="/renting/book" apt="${page}"/>

<table class="Cal">
<tr><td class="Cal">&nbsp;&nbsp;</td><td class="CalAltDay">${properties.M_Cal}</td></tr>
<tr><td class="CalR">&nbsp;&nbsp;</td><td class="CalAltDay">${properties.M_CalR}</td></tr>
<tr><td class="CalB">&nbsp;&nbsp;</td><td class="CalAltDay">${properties.M_CalB}</td></tr>
<tr><td class="CalX">&nbsp;&nbsp;</td><td class="CalAltDay">${properties.M_CalX}</td></tr>
</table>

<%
  for (int i=0;i<images.length;i++)
  {
     if (images[i].startsWith("zz"))
       continue;
     request.setAttribute("image",images[i]);
%>
 <a href="${image}" class="AptBig"><img class="AptSmall" src="${image}"/></a><br/>
<%}%>


</td>

</table>


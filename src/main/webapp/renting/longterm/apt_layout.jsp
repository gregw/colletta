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
<td class="AptVH"></td><td class="AptV"></td>            
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
</td>  
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

<tr>
<td class="AptVH">${properties.H_available}:</td><td class="AptV" colspan="3">${properties.available}</td>
</tr>

<tr>
<td class="AptVH">${properties.H_price}:</td><td class="AptV" colspan="3">${properties.price}</td>
</tr>

<tr>
<td class="AptVH">${properties.H_contact}:</td><td class="AptV" colspan="3"><a href="${properties.contact}?Subject=Colletta%20${properties.name}">${properties.contactName}</a></td>
</tr>

</table>

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



</td>

<td>
<h1>&nbsp;</h1>
<br/>
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


<%@ page 
           contentType="text/html; charset=UTF-8"
           import="com.mortbay.iwiki.*,it.colletta.*,it.colletta.reservation.*,java.util.*"
 %>

<%@ include file="/default_layout.jsp" %>
 
<%
    User user = (User)session.getAttribute("user");
    
    if (user == null)
    {
%>
    
<div class="SubTitle">${properties.H_login}</div>
<div class="Form">
<form name="Login" method="POST" >
<table class="Form">
<tr>
<td class="Label" width="100">${properties.H_user}:</td>
<td class="Form"><input class="Form" type="Text" name="name" size="8"></td>
</tr>
<tr>
<td class="Label">${properties.H_password}:</td>
<td class="Form"><input class="Form" type="Password" name="password" size="8"></td>
</tr>
<tr>
<td class="Label" >
<input type="submit" class="Form" name="login" value="${properties.H_login}"/>
</td>
</tr>
</table>
<input type="hidden" name="form" value="login"/>
</form>
</div>

<% } else { %>

<div class="SubTitle">User: ${user.name}</div>

<div class="Form">

<form name="ChangeUser" method="POST" >
<input type="hidden" name="form" value="updateuser"/>
<table class="Form">

<tr>
<td class="Label" width="100">${properties.H_name}:</td>
<td class="Form"><input class="Form" type="Text" name="name" size="30" value="${user.fullName}"></td>
</tr>

<tr>
<td class="Label" >${properties.H_email}:</td>
<td class="Form"><input class="Form" type="Text" name="email" size="30" value="${user.email}"></td>
</tr>

<tr>
<td class="Label">${properties.H_contact}:</td>
<td class="Form"><input class="Form" type="Text" name="contact" size="30" value="${user.contact}"></td>
</tr>

<tr>
<td class="Label" >${properties.H_lang}:</td>
<td class="Form">${user.lang}</td>
</tr>

<tr>
<td class="Label" >${properties.H_owns}:</td>
<td class="Form"><a href="/renting/view/owner.jsp">${user.owns}</a></td>
</tr>

<tr>
<td class="Label" >
<input type="submit" class="Form" name="button" value="${properties.H_updateuser}"/>
</td>
</tr>
</table>
</form>

<br/>

<form name="ChangePW" method="POST" >
<input type="hidden" name="form" value="changepw"/>
<table class="Form">
<tr>
<td class="Label" width="100">${properties.H_newpw1}:</td>
<td class="Form"><input class="Form" type="Password" name="newpw1" size="8"></td>
</tr>
<tr>
<td class="Label">${properties.H_newpw2}:</td>
<td class="Form"><input class="Form" type="Password" name="newpw2" size="8"></td>
</tr>
<tr>
<td class="Label">${properties.H_password}:</td>
<td class="Form"><input class="Form" type="Password" name="oldpw" size="8"></td>
</tr>
<tr>
<td class="Label" >
<input type="submit" class="Form" name="button" value="${properties.H_changepw}"/>
</td>
</tr>
</table>
</form>


<% } %>

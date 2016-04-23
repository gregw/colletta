<%@ page 
           contentType="text/html; charset=UTF-8"
           import="com.mortbay.iwiki.*,it.colletta.*,it.colletta.reservation.*,java.util.*"
 %>
 
<%
    User user = (User)session.getAttribute("user");
    
    if (user == null)
    {
%>
    
<h1>${properties.H_login}</h1>
<form name="Login" method="POST" >

<div class="QSearch">
<span class="QSearch">${properties.H_user}:</span>
<span class="QSearch"><input class="QSearch" type="Text" name="name" size="6"/></span>
</div>

<div class="QSearch">
<span class="QSearch">${properties.H_password}:</span>
<span class="QSearch"><input class="QSearch" type="Password" name="password" size="6"/></span>
</div>

<div>
<span class="QSearch" colspan="2">
<input type="submit" class="QSearch" name="login" value="${properties.H_login}"/>
</span>
</div>

<input type="hidden" name="form" value="login"/>
</form>

<% } else { %>

<h1>User: ${user.name}</h1>

<table class="QSearch">
<tr>
<td class="QSearch">

<form name="Login" method="POST" >
<input type="hidden" name="form" value="logout"/>
<input type="submit" class="QSearch" name="logout" value="${properties.H_logout}"/>
</form>

<% if (user.isEditor()) { %>
<form name="Edit" method="POST" >

<% if ("on".equals(session.getAttribute("edit"))) { %>
<input type="submit" class="QSearch" name="editOff" value="Edit Off"/>
<% } else { %>
<input type="submit" class="QSearch" name="editOn" value="Edit On"/>
<% } %>
<input type="hidden" name="form" value="edit"/>
</form>

</td></tr></table>



<% } %> 

<a href="/services/internet/profile/">${properties.H_profile}</a>

</td></tr></table>
<%  } %>

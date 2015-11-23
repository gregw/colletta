
<%@ tag
           body-content="empty"
           import="com.mortbay.iwiki.*,it.colletta.*"
%>

<%@ attribute name="style"  required="false" %>
<%@ attribute name="name"  required="false" %>
<%@ attribute name="value"  type="java.lang.String" required="false" %>
<%@ attribute name="readonly"  type="java.lang.Boolean" required="false" %>
<%@ attribute name="manager"  type="java.lang.Boolean" required="false" %>
<%@ attribute name="aptId"  required="false" %>

<% 
	User user=User.getUser(value);
	if (user!=null && User.NOBODY.equals(user))
		user=User.getCurrentUser();
	String link=User.getUserLink(value);
	
    if (readonly!=null && readonly.booleanValue()){ 
%><span class="${style}"><%=link%></span><% } else { %>
<select class="${style}" name="${name}" >
<%
  User[] users=User.getUsers();
  if (aptId!=null && aptId.length()==0)
  	aptId=null;
  	
  for (int i=0;i<users.length;i++)
  {   
    if (manager!=null && manager.booleanValue() )
    {
    	if (!users[i].equals(user) && (
	    (aptId==null && !users[i].isManager()) ||
    	    (aptId!=null && !users[i].managerOf(aptId))))
    	    continue;
    }

      if (users[i].equals(user))
      	out.print("<option value=\""+users[i].getName()+ "\" selected>"+users[i].getFullName());
      else
      	out.print("<option value=\""+users[i].getName()+ "\" >"+users[i].getFullName());

	  if (users[i].getEmail()!=null && users[i].getEmail().length()>0)
      	out.print("&lt;"+users[i].getEmail()+"&gt;</option>");
      else
      	out.print("</option>");
	  
	  
  }
%>
</select>
<% } %>

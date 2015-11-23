/* ==============================================
 * Copyright 2003 Mort Bay Consulting Pty Ltd. All rights reserved.
 * Distributed under the artistic license.
 * Created on 16/04/2004
 * $Id: UserFilter.java,v 1.1 2006/01/21 17:28:01 gregw Exp $
 * ============================================== */
 
package com.mortbay.iwiki;

import java.io.IOException;
import java.net.URL;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/* ------------------------------------------------------------------------------- */
/** 
 * 
 * @version $Revision: 1.1 $
 * @author gregw
 */
public class UserFilter extends FormFilter
{
    ServletContext context=null;
    URL users;

    /* ------------------------------------------------------------------------------- */
    public UserFilter()
    {
        super(new String[]{"login","logout","changepw","updateuser"});
    }
    
    /* ------------------------------------------------------------------------------- */
    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException
    {
        try
        {
            super.init(config);
            context=config.getServletContext();
            users=context.getResource("/WEB-INF/users.properties");
            User.loadUsers(users);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }    

    /* ------------------------------------------------------------------------------- */
    /* (non-Javadoc)
     * @see it.colletta.FormFilter#initValues(javax.servlet.http.HttpServletRequest)
     */
    protected void initValues(HttpServletRequest srequest) throws Exception
    {
        HttpSession session=srequest.getSession(true);
        User user= (User)session.getAttribute("user");
        if (user==null || User.NOBODY.equals(user))
            user=User.INTERNET;
        User.setCurrentUser(user);
    }


    /* ------------------------------------------------------------------------------- */
    /* (non-Javadoc)
     * @see it.colletta.FormFilter#handlePOST(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected String handlePOST(HttpServletRequest srequest, HttpServletResponse sresponse, String form) throws Exception
    {
        HttpSession session=srequest.getSession(true);

        try
        {
            if ("login".equals(form))
            {
                String name=srequest.getParameter("name");
                String password=srequest.getParameter("password");
                
                User user = User.getUser(name);
                if (user!=null && user.checkCredential(password))
                {
                    context.log("login OK "+name);
                    session.setAttribute("user", user);
                }
                else
                {
                    context.log("login FAILED"+name);
                    session.setAttribute("user", null);
                    session.setAttribute("edit", null);
                }
                
            }
            else if ("logout".equals(form))
            {
                context.log("logout "+session.getAttribute("user"));
                session.setAttribute("user", null);
            }
            else if ("changepw".equals(form))
            {
                User user= (User)session.getAttribute("user");
                if (user!=null)
                {
                    String password=srequest.getParameter("newpw1");
                    String check=srequest.getParameter("newpw2");
                    String old=srequest.getParameter("oldpw");
                    if (password!=null && password.length()>0 &&
                        password.equals(check) &&
                        user.checkCredential(old))
                    {
                        user.changeCredential(password);
                        User.saveUsers(users);
                    }
                }
            }
            else if ("updateuser".equals(form))
            {
                User user= (User)session.getAttribute("user");
                if (user!=null)
                {
                    String name=srequest.getParameter("name");
                    if (name==null)name="";
                    String email=srequest.getParameter("email");
                    if (email==null)email="";
                    String contact=srequest.getParameter("contact");
                    if (contact==null)contact="";
                    user.setFullName(name);
                    user.setEmail(email);
                    user.setContact(contact);
                    User.saveUsers(users);
                }
            }
        }
        catch(Exception e)
        {
            context.log("login", e);
        }
        
        return null;
    }
    
    
    protected String handleGET (HttpServletRequest srequest, HttpServletResponse sresponse, String form)
    throws Exception
    {
        
        return null;
    }
    
    
    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        try
        {
            super.doFilter(request, response, chain);
        }
        finally
        {
            User.setCurrentUser(null);
        }
    }
}

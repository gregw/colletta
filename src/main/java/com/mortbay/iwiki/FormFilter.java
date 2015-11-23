/* ==============================================
 * Copyright 2003 Mort Bay Consulting Pty Ltd. All rights reserved.
 * Distributed under the artistic license.
 * Created on 2/04/2004
 * $Id: FormFilter.java,v 1.2 2006/08/15 08:58:24 gregw Exp $
 * ============================================== */
 
package com.mortbay.iwiki;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* ------------------------------------------------------------------------------- */
/** 
 * 
 * @version $Revision: 1.2 $
 * @author gregw
 */
public abstract class FormFilter implements Filter
{
    protected ServletContext context= null;
    private List forms;

    /**
     * @param form If non null, then the form must a have a field "form" set to 
     * this value before the handlePOST method will be called
     */
    protected FormFilter(String form)
    {
        if (form!=null)
        {
            forms = new ArrayList();
            forms.add(form);
        }
    }
    
    /**
     * @param form If non null, then the form must a have a field "form" set to 
     * this value before the handlePOST method will be called
     */
    protected FormFilter(String[] forms)
    {
        this.forms=Arrays.asList(forms);
    }
    
    /* ------------------------------------------------------------------------------- */
    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException
    {
        context= config.getServletContext();
        Log.setContext (context);
    }


    /* ------------------------------------------------------------------------------- */
    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,ServletException
    {
        HttpServletRequest srequest=(HttpServletRequest)request;
        HttpServletResponse sresponse=(HttpServletResponse)response;
        srequest.setCharacterEncoding("UTF-8");
        String form=srequest.getParameter("form");
        
        if ("POST".equalsIgnoreCase(srequest.getMethod()) &&
            (forms==null || forms.size()==0 || forms.contains(form)))
        {
            String next = srequest.getRequestURI();
            try
            {
                String n=handlePOST(srequest,sresponse, form);
                if (n!=null)
                    next=n;
            }
            catch (IOException e)
            {
                throw e;
            }
            catch(Exception e)
            {
                context.log(next, e);
            }
            sresponse.sendRedirect(next);
        }
        else if ("GET".equalsIgnoreCase(srequest.getMethod()))
        {
            
            try
            {
                String next = handleGET (srequest, sresponse, form);
                if (next != null)
                    sresponse.sendRedirect(next);
                else
                    chain.doFilter (request, response);
            }
            catch (IOException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                context.log (srequest.getRequestURI(), e);
            }
        }
        else
        {
            try
            {
                initValues(srequest);
            }
            catch (IOException e)
            {
                throw e;
            }
            catch(Exception e)
            {
                context.log("",e);
            }
            chain.doFilter(request,response);
        }
    }

    public String getAction(HttpServletRequest srequest)
    {
        String action=null;
        Enumeration e = srequest.getParameterNames();
        while (e.hasMoreElements())
        {
            String n=(String)e.nextElement();
            if (n.endsWith(".x"))
            {
                action=n.substring(0,n.length()-2);
                break;
            }
        }
        return action;
    }
    /* ------------------------------------------------------------------------------- */
    /** initValues.
     * @param srequest
     */
    protected abstract void initValues(HttpServletRequest srequest)
        throws Exception;

    /* ------------------------------------------------------------------------------- */
    /** handlePOST.
     * @param srequest
     * @param sresponse
     * @return
     */
    protected abstract String handlePOST(HttpServletRequest srequest,HttpServletResponse sresponse, String form)
        throws Exception;

    
    protected  abstract String handleGET (HttpServletRequest srequest,HttpServletResponse sresponse, String form)
    throws Exception;
    
    /* ------------------------------------------------------------------------------- */
    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy()
    {
    }
}

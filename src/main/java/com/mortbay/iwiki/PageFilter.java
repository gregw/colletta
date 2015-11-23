/* ==============================================
 * Copyright 2003 Mort Bay Consulting Pty Ltd. All rights reserved.
 * Distributed under the artistic license.
 * Created on 16/02/2004
 * $Id: PageFilter.java,v 1.7 2006/08/15 08:58:24 gregw Exp $
 * ============================================== */
package com.mortbay.iwiki;

import it.colletta.Apartment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedOutput;

/* ------------------------------------------------------------------------------- */
/**
 * @version $Revision: 1.7 $
 * @author gregw
 */
public abstract class PageFilter implements Filter
{
    protected ServletContext context = null;
    
    /* ------------------------------------------------------------------------------- */
    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException
    {
        context = config.getServletContext();
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
     *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest srequest = (HttpServletRequest) request;
        HttpServletResponse sresponse = (HttpServletResponse) response;

        
        Page page = (Page)request.getAttribute("page");
        if (page==null)
            chain.doFilter(request, response);
        else
        {
            String lang=(String)request.getAttribute(LangFilter.LANG);
            render(page,lang,srequest,sresponse,chain);
        }
    }

    /* ------------------------------------------------------------------------------- */
    protected abstract void render(Page page, String lang,HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException;


    /* ------------------------------------------------------------------------------- */
    protected void index(PrintWriter out, String contextPath, String lang, Page section, Page[] page_path, int level)
    {
        Page[] menu = section.getChildren();

        if (menu == null || menu.length == 0) return;

        out.println("<div id=\"level" + level + "\">");

        for (int i = 0; i < menu.length; i++)
        {
            if ("hide".equals(menu[i].getProperty("entry"))) continue;

            // Is this the selected item?
            if (level + 1 < page_path.length && menu[i] == page_path[level + 1])
            {
                out.print("<div class=\"selected\">");
                out.print("<a href=\"" + contextPath + menu[i].getPath() + "\" class=\"selected\">");
                out.print(menu[i].getName(lang));
                out.print("</a>");
                out.println("</div>");

                if (level < 2) index(out, contextPath, lang, menu[i], page_path, level + 1);
            }
            else
            {
                out.print("<div class=\"unselected\">");
                out.print("<a href=\"" + contextPath + menu[i].getPath() + "\">");
                out.print(menu[i].getName(lang));
                out.print("</a>");
                out.println("</div>");
            }
        }
        out.println("</div>");

    }

    /* ------------------------------------------------------------------------------- */
    protected void editForm(HttpServletRequest srequest, HttpServletResponse sresponse, String uri, Page page, String lang, PrintWriter out) throws ServletException, IOException
    {
        String[] langs = page.getLanguages();
        out.println("<br/><form action=\"" + uri + "\" method=\"POST\">");
        out.println("<div class=\"Edit\"><input type=\"Submit\" name=\"Edit\" value=\"Save\"/></div>");
        for (int i = 0; i < langs.length; i++)
        {
            out.println("<div class=\"Edit\"><b>EDIT PAGE TEXT[" + langs[i] + "]:</b><br/>");
            out.println("<textarea class=\"Edit\" name=\"T_" + langs[i] + "\">");
            String text = page.getText(langs[i]);
            if (text != null) out.println(text);
            out.println("</textarea>");
            out.println("</div>");
        }
        out.println("<div class=\"Edit\"><b>EDIT PAGE PROPERTIES:</b><br/>");
        out.println("<table class=\"Edit\"><tr><th class=\"Edit\">&nbsp;</th><th class=\"Edit\">common</th>");
        for (int i = 0; i < langs.length; i++)
            out.println("<th class=\"Edit\">" + langs[i] + "</th>");
        out.println("</tr>");
        HashSet props = new HashSet(page.getProperties(null).keySet());
        for (int i = 0; i < langs.length; i++)
        {
            Map m = page.getProperties(langs[i]);
            if (m != null) props.addAll(m.keySet());
        }
        ArrayList sprops = new ArrayList(props);
        Collections.sort(sprops);
        Iterator iter = sprops.iterator();
        while (iter.hasNext())
        {
            String p = (String) iter.next();
            out.println("<tr>");
            out.println("<th class=\"Edit\">" + p + ":</th>");
            out.println("<td class=\"Edit\"><input class=\"Edit\" type=\"text\"  name=\"P_" + p + "_\"");
            Object v = page.getPageProperty(null, p);
            if (v != null) out.println(" value=\"" + v + "\"");
            out.println("/></td>");
            for (int i = 0; i < langs.length; i++)
            {
                out.println("<td class=\"Edit\"><input class=\"Edit\"  type=\"text\" name=\"P_" + p + "_" + langs[i] + "\"");
                v = page.getPageProperty(langs[i], p);
                if (v != null) out.println(" value=\"" + v + "\"");
                out.println("/></td>");
            }
            out.println("</tr>");
        }
        out.println("<tr>");
        out.println("<td class=\"Edit\"><input class=\"Edit\" type=\"text\"  name=\"N\"/>:</td>");
        out.println("<td class=\"Edit\"><input class=\"Edit\" type=\"text\"  name=\"N_\"/></td>");
        for (int i = 0; i < langs.length; i++)
            out.println("<td class=\"Edit\"><input class=\"Edit\"  type=\"text\" name=\"N_" + langs[i] + "\"/></td>");
        out.println("</tr>");
        out.println("</table>");
        out.println("<br/><input type=\"Submit\" name=\"Edit\" value=\"Save\"/>");
        out.println("<br/>&nbsp;");
        out.println("<br/>New page: <input type=\"Text\" name=\"NP\"/>");
        out.println("<input type=\"Submit\" name=\"Edit\" value=\"New\"/>");
        out.println("</div>");
        out.println("</form>");
        out.println("<form method=\"POST\" enctype=\"multipart/form-data\">");
        out.println("<div class=\"Edit\"><b>EDIT IMAGES:</b><br/>");
        String[] images = page.getImages();
        out.println("<table><tr>");
        for (int i = 0; i < images.length; i++)
        {
            out.println("<td class=\"Edit\"><img class=\"Edit\" src=\"" + images[i] + "\"/><br/>");
            out.println(images[i] + "&nbsp;<input type=\"checkbox\" name=\"D_" + images[i] + "\"/>delete</td>");
            if ((i + 1) % 4 == 0) out.println("</tr><tr>");
        }
        out.println("</tr></table>");
        out.println("<br/>Upload Image: <input type=\"file\" name=\"Image\"/><br/>&nbsp;<br/>");
        out.println("<input type=\"Submit\" name=\"Edit\" value=\"Update Images\"/>");
        out.println("</div></form>");
        out.println("<div class=\"Edit\"><b>Page Instructions</b><br/>");
        String page_instructions = findExistingInPath(page.getPath(), "page_instr.html");
        if (page_instructions != null)
        {
            RequestDispatcher dispatcher = context.getRequestDispatcher(page_instructions);
            dispatcher.include(srequest, sresponse);
        }
        else
            out.println("<small><p>None</p></small>");
        RequestDispatcher dispatcher = context.getRequestDispatcher("/edit_instr.html");
        dispatcher.include(srequest, sresponse);
        out.println("</div>");
    }
    
    /* ------------------------------------------------------------------------------- */
    protected String findExistingInPath(String uri, String name)
    {
        if (!uri.endsWith("/")) throw new IllegalArgumentException();
        int slash = uri.lastIndexOf("/");
        try
        {
            while (slash >= 0)
            {
                String path = uri.substring(0, slash + 1) + name;
                if (context.getResource(path) != null) return path;
                slash = uri.lastIndexOf("/", slash - 1);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy()
    {
    }
}

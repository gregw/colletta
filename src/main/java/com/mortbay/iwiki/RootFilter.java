/* ==============================================
 * Copyright 2003 Mort Bay Consulting Pty Ltd. All rights reserved.
 * Distributed under the artistic license.
 * Created on 16/02/2004
 * $Id: PageFilter.java,v 1.7 2006/08/15 08:58:24 gregw Exp $
 * ============================================== */
package com.mortbay.iwiki;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
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

import it.colletta.Apartment;

/* ------------------------------------------------------------------------------- */
/**
 * @version $Revision: 1.7 $
 * @author gregw
 */
public class RootFilter implements Filter
{
    public final static String CONTEXTPATH = "contextPath";
    public final static String CONTEXTURI = "contextURI";
    public final static String MODIFIED = "modified";
    protected ServletContext context = null;
    protected String contextPath = "";
    protected Page root;
    protected Page[] modified;

    /* ------------------------------------------------------------------------------- */
    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException
    {
        context = config.getServletContext();
        updateRoot(context.getContextPath());
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

        String host = srequest.getServerName();

        String uri = srequest.getRequestURI();
        int p = uri.indexOf(';');
        if (p >= 0) uri = uri.substring(0, p);
        HttpSession session = srequest.getSession(true);

        String old_referer = (String) session.getAttribute("referer");
        String referer = null;
        Cookie[] cookies = srequest.getCookies();
        for (int i = 0; cookies != null && i < cookies.length; i++)
            if (cookies[i].getName().equals("referer")) referer = cookies[i].getValue();

        if (referer == null || referer.length() == 0 || srequest.getParameter("referer") != null) referer = srequest.getParameter("referer");
        if (referer == null || referer.length() == 0 || srequest.getParameter("referrer") != null) referer = srequest.getParameter("referrer");
        if (referer == null || referer.length() == 0 || srequest.getParameter("Referer") != null) referer = srequest.getParameter("Referer");
        if (referer == null || referer.length() == 0 || srequest.getParameter("Referrer") != null) referer = srequest.getParameter("Referrer");
        if (referer == null || referer.length() == 0) referer = srequest.getHeader("Referer");
        if (referer == null || referer.length() == 0) referer = srequest.getHeader("Referrer");

        if (referer != null && (referer.startsWith("http:") || referer.startsWith("https:")))
        {
            URL ref = new URL(referer);
            referer = ref.getHost() + ref.getPath();
            if (referer.startsWith("www.colletta.it"))
                referer = null;
            else if (referer.startsWith("www.colletta-it.com")) referer = null;
        }

        if (referer != null && referer.length() > 0 && !referer.equals(old_referer))
        {
            Cookie cookie = new Cookie("referer", referer);
            cookie.setPath("/");
            sresponse.addCookie(cookie);
            session.setAttribute("referer", referer);
        }

        User user = (User) srequest.getSession(true).getAttribute("user");

        boolean edit = false;
        synchronized (this)
        {
            if ("on".equals(session.getAttribute("edit")))
            {
                edit = true;
                updateRoot(request.getServletContext().getContextPath());
            }
        }
        if (!uri.endsWith("/") || request.getAttribute("page") != null)
            chain.doFilter(request, response);
        else
        {
            contextPath = srequest.getContextPath();
            String path = uri.substring(contextPath.length());
            srequest.setAttribute(CONTEXTPATH, contextPath);
            srequest.setAttribute(CONTEXTURI, path);
            srequest.setAttribute(MODIFIED, modified);
            String lang = (String) srequest.getAttribute(LangFilter.LANG);
            Page page = root.getPageByPath(path);
            if (page == null)
            {
                chain.doFilter(request, response);
                return;
            }
            response.setContentType("text/html; charset=utf-8");
            String redirect = page.getPathProperty(null, "redirect");
            if (redirect != null && redirect.startsWith("http:") && !edit)
            {
                sresponse.sendRedirect(redirect);
                return;
            }
            Map properties = page.getPathProperties(lang);
            request.setAttribute("properties", properties);
            request.setAttribute("root", root);
            request.setAttribute("page", page);

            // Handle posts
            if ("POST".equalsIgnoreCase(srequest.getMethod()))
            {
                session.setAttribute("lastPOST", request.getParameterMap());
                try
                {
                    if ("edit".equals(srequest.getParameter("form")))
                    {
                        edit = srequest.getParameter("editOn") != null;
                        session.setAttribute("edit", edit ? "on" : null);
                    }
                    else
                        doEdit(srequest, sresponse, page);
                }
                finally
                {
                    sresponse.sendRedirect(srequest.getRequestURI());
                }
                return;
            }

            if ("on".equals(session.getAttribute("edit")) && user != null && user.isEditor()) 
                request.setAttribute("edit","on");
            
            // Generate page
            chain.doFilter(request, response);
        }
    }
       
    
    /* ------------------------------------------------------------------------------- */
    /**
     * doEdit.
     * 
     * @param srequest
     * @param sresponse
     * @param page
     */
    protected void doEdit(HttpServletRequest srequest, HttpServletResponse sresponse, Page page) throws IOException
    {
        if (!"on".equals(srequest.getSession(true).getAttribute("edit"))) return;
        User user = (User) srequest.getSession(true).getAttribute("user");
        if (user == null || !user.isEditor()) return;

        context.log("EDIT by " + user.getName() + " of " + srequest.getRequestURI());

        if (srequest.getContentType() != null && srequest.getContentType().startsWith("multipart/form-data"))
        {
            doImages(srequest, sresponse, page);
        }
        else
        {
            String edit = srequest.getParameter("Edit");
            if ("Save".equals(edit))
            {
                Enumeration e = srequest.getParameterNames();
                while (e.hasMoreElements())
                {
                    String param = (String) e.nextElement();
                    String value = srequest.getParameter(param);
                    if (param.startsWith("P_"))
                    {
                        if (value != null && value.trim().length() == 0) value = null;
                        String lang = null;
                        if (!param.endsWith("_")) lang = param.substring(param.length() - 2);
                        String name = param.substring(2, param.length() - (lang == null ? 1 : 3));
                        page.setPageProperty(lang, name, value);
                    }
                    else if (param.startsWith("T_"))
                    {
                        String lang = param.substring(2);
                        page.setText(lang, value);
                    }
                    else if (param.startsWith("N_"))
                    {
                        String lang = param.substring(2);
                        if (lang.length() == 0) lang = null;
                        String name = srequest.getParameter("N");
                        if (name != null && name.length() > 0 && value != null && value.trim().length() > 0) page.setPageProperty(lang, name, value);
                    }
                }
                page.save();
            }
            else if ("New".equals(edit))
            {
                String n = srequest.getParameter("NP");
                if (n != null)
                {
                    n = n.trim();
                    n = StringUtil.replace(n, " ", "");
                    if (n.length() > 0) page.newChild(n);
                }
            }
        }
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * doEdit.
     * 
     * @param srequest
     * @param sresponse
     * @param page
     */
    protected void doImages(HttpServletRequest srequest, HttpServletResponse sresponse, Page page) throws IOException
    {
        String[] images = page.getImages();
        for (int i = 0; i < images.length; i++)
        {
            if ("on".equals(srequest.getParameter("D_" + images[i])))
            {
                File file = new File(context.getRealPath(page.getPath() + images[i]));
                System.err.println("file=" + file);
                if (file.exists())
                {
                    context.log("DELETE " + file);
                    file.delete();
                }
            }
        }
        String filename = srequest.getParameter("Image");
        File file = (File) srequest.getAttribute("Image");
        if (filename != null && file != null)
        {
            filename=filename.replaceAll("^[A-Z]:","");
            filename=filename.replace("/","");
            File newFile = new File(context.getRealPath(page.getPath() + filename));
            IO.copy(file, newFile);
        }
    }



    /* ------------------------------------------------------------------------------- */
    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy()
    {
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

    /* ------------------------------------------------------------ */
    /**
     * 
     */
    protected synchronized void updateRoot(String contextPath) throws ServletException
    {
        try
        {
            File dir = new File(context.getRealPath("/"));
            root = new Page(null, "/", dir, null, null);
            Apartment.setRoot(root);
            List m = new ArrayList(root.getAllPages());
            Collections.sort(m, new Page.LastModified());
            modified = (Page[]) m.toArray(new Page[m.size()]);

            // Syndicate

            String[] lang = { "it", "en", "fr", "de"};
            for (int l = 0; l < lang.length; l++)
            {
                List updates = new ArrayList();

                for (int i = 0; i < modified.length && i < 15; i++)
                {
                    Page page = modified[i];
                    SyndEntry entry = new SyndEntryImpl();
                    entry.setTitle(page.getTitle(lang[l]));
                    entry.setLink(contextPath + lang[l] + page.getPath());
                    entry.setPublishedDate(new Date(page.getLastModified().getTimeInMillis()));
                    SyndContent description = new SyndContentImpl();
                    description.setType("text/html");
                    description.setValue(page.getPageProperty(lang[l], "blurb"));
                    entry.setDescription(description);
                    updates.add(entry);
                }

                SyndFeed feed = new SyndFeedImpl();
                feed.setFeedType("rss_2.0");
                feed.setTitle("Colletta di Castelbianco " + lang[l]);
                feed.setLink(contextPath + lang[l]);
                feed.setDescription("Colletta di Castelbianco");
                feed.setEntries(updates);
                SyndFeedOutput output = new SyndFeedOutput();
                output.output(feed, new FileWriter(new File(dir, "updates_" + lang[l] + ".rss")));

                List news_and_events = new ArrayList();

                Page news = root.getPageByPath("/news/");
                Page[] news_pages = news.getChildren();
                for (int n = news_pages.length; n-- > 2;)
                {
                    news_and_events.add(news_pages[n]);
                    if (news_and_events.size() > 10) break;
                }

                Page events = root.getPageByPath("/events/");
                Page[] event_pages = events.getChildren();
                YyyyMmDd now = new YyyyMmDd();
                now.addDays(-7);
                for (int e = 0; e < event_pages.length; e++)
                {
                    if (now.after(event_pages[e].getDate())) continue;
                    news_and_events.add(event_pages[e]);
                }

                Collections.sort(news_and_events, new Page.LastModified());

                for (int i = 0; i < news_and_events.size(); i++)
                {
                    Page page = (Page) news_and_events.get(i);
                    SyndEntry entry = new SyndEntryImpl();
                    entry.setTitle(page.getTitle(lang[l]));
                    entry.setLink(contextPath + lang[l] + page.getPath());
                    entry.setPublishedDate(new Date(page.getLastModified().getTimeInMillis()));
                    SyndContent description = new SyndContentImpl();
                    description.setType("text/html");
                    description.setValue(page.getPageProperty(lang[l], "blurb"));
                    entry.setDescription(description);
                    news_and_events.set(i, entry);
                }

                feed = new SyndFeedImpl();
                feed.setFeedType("rss_2.0");
                feed.setTitle("Colletta di Castelbianco " + news.getTitle(lang[l]));
                feed.setLink(contextPath + lang[l]);
                feed.setDescription("Colletta di Castelbianco " + news.getTitle(lang[l]));
                feed.setEntries(news_and_events);
                output = new SyndFeedOutput();
                output.output(feed, new FileWriter(new File(dir, "news_" + lang[l] + ".rss")));

            }

        }
        catch (Exception e)
        {
            context.log("Page init: ", e);
            throw new ServletException(e);
        }
    }

}

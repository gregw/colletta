package com.mortbay.iwiki;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

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

public class LangFilter implements Filter
{
    public final static String LANG= "lang";
    public final static String ALTLANG= "altLang";
    public final static String LOCALE= "locale";
    public final static String ALTLOCALE= "altLocale";

    private ServletContext context= null;
    private HashMap locales = new HashMap();

    /* ------------------------------------------------------------------------------- */
    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException
    {
        context= config.getServletContext();
    }

    /* ------------------------------------------------------------------------------- */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException
    {
        HttpServletRequest srequest= (HttpServletRequest)request;
        HttpServletResponse sresponse= (HttpServletResponse)response;
        srequest.setCharacterEncoding("UTF-8");
        HttpSession session= srequest.getSession(true);

        String lang=null;
        
        boolean include=false;
        boolean dispatch=false;
        
        String uri=(String)srequest.getAttribute("javax.servlet.include.request_uri");
        if (uri==null)
            uri=srequest.getRequestURI();
        else
            include=true;
        
        String path= uri.substring(srequest.getContextPath().length());
        
        if (path.length()>=4 && path.charAt(0)=='/' && path.charAt(3)=='/')
        {
          dispatch=true;
          lang=path.substring(1,3);
          path=
            path.substring(3);
        }

        // Do we have a forced language?
        if (lang != null)
        {
            // Yes - so save it where we can find it.
            Cookie cookie = new Cookie(LANG, lang);
            cookie.setPath(srequest.getContextPath());
            sresponse.addCookie(cookie);
            session.setAttribute(LANG, lang);
        }
        else
        {
            // No forced language
            // so look for it in the session
            lang= (String)session.getAttribute(LANG);

            // maybe a cookie?
            if (lang == null)
            {
                Cookie[] cookies= srequest.getCookies();
                if (cookies != null && cookies.length > 0)
                {
                    for (int c= 0; c < cookies.length; c++)
                        if (cookies[c].getName().equals(LANG))
                            lang= cookies[c].getValue();
                }

                // maybe an accept header
                if (lang == null)
                {
                    String accept= srequest.getHeader("Accept-Language");
                    if (accept != null && accept.length() > 2)
                        lang= accept.substring(0, 2);
                }

                // OK English will do
                if (lang == null)
                    lang= "en";

                sresponse.addCookie(new Cookie(LANG, lang));
                session.setAttribute(LANG, lang);
            }
        }
        
        String altLang=lang.equals("it")?"en":"it";
        srequest.setAttribute(LANG,lang);
        srequest.setAttribute(ALTLANG, altLang);
        Locale locale=getLocale(lang);
        Locale altLocale=getLocale(altLang);
        srequest.setAttribute(LOCALE,locale);
        srequest.setAttribute(ALTLOCALE,altLocale);

        
        // Is the request for a localized resource?
        int slash= path.lastIndexOf("/");
        int dot= path.indexOf(".", slash < 0 ? 0 : slash);
        if (dot > 0 && path.charAt(dot-3)!='_')
        {
            // No - so look for a localized resource...
            String contentBase= path.substring(0, dot);
            String ext= path.substring(dot);
            String content= contentBase + "_" + lang + ext;
            if (context.getResource(content) != null)
            {
                RequestDispatcher dispatcher= context.getRequestDispatcher(content);
                if (include)
                    dispatcher.include(request, response);
                else
                    dispatcher.forward(request,response);
                return;
            }
            
            // Try English
            content= contentBase + "_en" + ext;
            if (context.getResource(content) != null)
            {
                RequestDispatcher dispatcher= context.getRequestDispatcher(content);
                if (include)
                    dispatcher.include(request, response);
                else
                    dispatcher.forward(request,response);
                return;
            }
            
            // Try Italian
            content= contentBase + "_it" + ext;
            if (context.getResource(content) != null)
            {
                RequestDispatcher dispatcher= context.getRequestDispatcher(content);
                if (include)
                    dispatcher.include(request, response);
                else
                    dispatcher.forward(request,response);
                return;
            }
        }

        // Do we have to disatch for forced language?
        if (dispatch)
        {
          RequestDispatcher dispatcher=context.getRequestDispatcher(path);
          if (include)
              dispatcher.include(request, response);
          else
              dispatcher.forward(request,response);
          return;
        }
        
        // Let the request continue normally
	try
	{
	    chain.doFilter(request, response);
	}
	catch(java.io.FileNotFoundException e)
	{
	    System.err.println(path+": "+e);
	}
    }

    public void destroy()
    {
    }

    private Locale getLocale(String lang)
    {
        Locale l = (Locale)locales.get(lang);
        if (l==null)
        {
            l=new Locale(lang);
            locales.put(lang,l);
        }
        return l;
    }
}

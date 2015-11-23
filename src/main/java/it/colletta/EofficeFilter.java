//========================================================================
//Copyright 2006 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package it.colletta;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.mortbay.iwiki.Page;

public class EofficeFilter extends com.mortbay.iwiki.PageFilter
{
    protected void render(Page page, String lang, HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        String uri = request.getRequestURI();
        int p = uri.indexOf(';');
        if (p >= 0)
            uri = uri.substring(0,p);

        String contextPath = request.getContextPath();


        PrintWriter out = response.getWriter();

        // Generate page
        out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" >");
        out.println("<head>");

        String page_title = page.getProperty(lang,"pagetitle");
        if (page_title == null)
            page_title = page.getPathProperty(lang,"title");
        if (page_title != null)
            out.println("<title>" + page_title + "</title>");
        
        out.println("<link REL=\"icon\" HREF=\"/favicon.gif\" TYPE=\"image/gif\">");
        out.println("<link rel=\"alternate\" type=\"application/rss+xml\" title=\"Colletta\" href=\"/updates_" + lang + ".rss\" />");
        out.println("<link rel='stylesheet' href='/eoffice/eoffice.css' type='text/css'></link>");
        out.println("</head>");
        out.println("<body id=\"eoffice\">");

        String redirect = page.getPathProperty(null,"redirect");
        if (redirect != null)
        {
            boolean edit = "on".equals(request.getSession(true).getAttribute("edit"));
            if (edit)
                out.println("REDIRECT TO: " + redirect);
            else
            {
                RequestDispatcher dispatcher = context.getRequestDispatcher(redirect + "layout.jsp");
                dispatcher.include(request,response);
            }
        }
        else
        {
            RequestDispatcher dispatcher = request.getRequestDispatcher("layout.jsp");
            dispatcher.include(request,response);
        }

        if ("on".equals(request.getAttribute("edit")))
            editForm(request, response, uri, page, lang, out);

        out.println("<div id=\"spare1\"><span></span></div>");
        out.println("<div id=\"spare2\"><span></span></div>");
        out.println("<div id=\"spare3\"><span></span></div>");
        out.println("<div id=\"spare4\"><span></span></div>");

        out.println("<script src=\"http://www.google-analytics.com/urchin.js\" type=\"text/javascript\"></script><script type=\"text/javascript\">_uacct=\"UA-1149868-1\";urchinTracker();</script>");
        out.println("</body>");
        out.println("</html>");
    }

}

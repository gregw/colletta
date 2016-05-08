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

public class CollettaFilter extends com.mortbay.iwiki.PageFilter
{
    protected void render(Page page, String lang, HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException
    {
    	String css = Configuration.getInstance().getProperty("css.url", "http://www.egomedia.it/colletta/colletta.css"); 
        if ("false".equals(page.getProperty("colletta")))
        {
            chain.doFilter(request,response);
            return;
        }

        String uri = request.getRequestURI();
        if (uri.equals("/")) {
        	response.sendRedirect("/renting/view/");
        	return;
        }
        int p = uri.indexOf(';');
        if (p >= 0)
            uri = uri.substring(0,p);

        String contextPath = request.getContextPath();
        String path = uri.substring(contextPath.length());

        Map properties = page.getPathProperties(lang);
        String query = request.getQueryString();
        PrintWriter out = response.getWriter();

        // Generate page
        out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" >");
        out.println("<head>");
        if (request.getParameter("r") != null) {
        	out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        }
        String page_title = page.getPageProperty(lang,"pagetitle");
        if (page_title == null)
            page_title = page.getPathProperty(lang,"title");
        if (page_title != null)
        {
            out.println("<title>Colletta - " + page_title + "</title>");
        }
        else if ("it".equals(lang))
        {
            out.println("<title>Colletta - Appartamenti vacanze e telelavoro in Ligure Italia SV</title>");
        }
        else
        {
            out.println("<title>Colletta - Apartments for holiday or telework in Liguria Italy SV</title>");
        }
        out.println("<link REL=\"icon\" HREF=\"/favicon.gif\" TYPE=\"image/gif\">");
        out.println("<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css\" integrity=\"sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7\" crossorigin=\"anonymous\">");
        out.println("<style type=\"text/css\" title=\"colletta\">");
        out.println("  @import \"" + css + "\";");
        out.println("</style>");
        out.println("</head>");
        out.println("<body id=\"colletta\">");

        // Header
        out.print("<div id=\"header\" class=\"container-fluid\">");
        out.print("<div class=\"row\">");
        out.print("<div class=\"logo col-sm-6\">");
        out.print("<h1><a href=\"http://www.colletta.it/\" title=\"Colletta di Castelbianco, Italy\"><span>Colletta di Castelbianco</span></a></h1>");
        out.println("</div>");

        // Flags
        if (query != null)
        {
            int i = query.indexOf("lang=");
            if (i >= 0)
            {
                int j = query.indexOf("&",i);
                if (i > 0 && query.charAt(i - 1) == '&')
                    i--;
                else if (j > 0)
                    j++;
                if (i == 0 && j < 0)
                    query = null;
                else
                    query = j < 0?query.substring(0,i):(query.substring(0,i) + query.substring(j));
            }
        }

        out.println("<div id=\"flags\" class=\"col-sm-6 text-right\"><h1>");
        out.println("<a class=\"btn btn-default\" href=\"" + contextPath + "/it" + path
                + "\">IT</a>");
        out.println("<a class=\"btn btn-default\" href=\"" + contextPath + "/en" + path
                + "\">EN</a>");
        out.println("<a class=\"btn btn-default\" href=\"" + contextPath + "/fr" + path
                + "\">FR</a>");
        out.println("<a class=\"btn btn-default\" href=\"" + contextPath + "/de" + path
                + "\">DE</a>");
        out.println("</h1></div>");

        out.print("</div>");
        out.println("</div>");

        // No more left Menu
        out.println("<div class=\"container-fluid\">");
        out.println("<div class=\"row\">");

        // Content
        out.println("<div id=\"content\" class=\"col-md-9\">");
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

        out.println("</div>");

        // Right Menu
        out.println("<div class=\"sidebar col-md-3\">");
        String menu = findExistingInPath(path,"menuR.jsp");
        if (menu != null)
        {
            RequestDispatcher dispatcher = context.getRequestDispatcher(menu);
            dispatcher.include(request,response);
        }
        out.println("</div>");
        out.println("</div>");
        out.println("</div>");

        // Footer
        out.println("<div id=\"footer\" class=\"container-fluid\">");
        out.println("<div class=\"row\">");
        out.println("<div class=\"col-md-12\">");
        out.println("<span>&copy;2006 <a href=\"http://www.mortbay.com\">Mort Bay Consulting Pty. Ltd.</a>. <a href=\"http://jetty.mortbay.org\">Powered by Jetty.</a> </span>");
        out.println("</div>");
        out.println("</div>");
        out.println("</div>");

        if ("on".equals(request.getAttribute("edit")))
            editForm(request, response, uri, page, lang, out);

        out.println("<div id=\"spare1\"><span></span></div>");
        out.println("<div id=\"spare2\"><span></span></div>");
        out.println("<div id=\"spare3\"><span></span></div>");
        out.println("<div id=\"spare4\"><span></span></div>");

out.println("<script type=\"text/javascript\"> var _gaq = _gaq || []; _gaq.push(['_setAccount', 'UA-20726932-1']); _gaq.push(['_setDomainName', '.colletta.it']); _gaq.push(['_trackPageview']); (function() { var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true; ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js'; var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s); })(); </script>");

        out.println("</body>");
        out.println("</html>");
    }

}

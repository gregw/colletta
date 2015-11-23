package it.colletta;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ReportServlet extends HttpServlet
{
    public void doGet(HttpServletRequest request,HttpServletResponse response) 
        throws IOException, ServletException
    {
        PrintWriter out = response.getWriter();
        String referer=request.getHeader("referer");
        response.setContentType("text/html");
        
        // loop over all wrappers
        ServletRequest sr = request;

        while (sr!=null)
        {
            out.write("<h3>"+sr.getClass()+":"+sr.hashCode()+"</h3>");
        
            out.write("<table border=\"1\"><tr><th>Name</th><th>Value</th></tr>");
            out.write("<tr><td>getScheme()</td><td>"+sr.getScheme()+"</td></tr>");
            out.write("<tr><td>getServerName()</td><td>"+sr.getServerName()+"</td></tr>");
            out.write("<tr><td>getServlerPort()</td><td>"+sr.getServerPort()+"</td></tr>");

            if (sr instanceof HttpServletRequest)
            {    
                HttpServletRequest hsr = (HttpServletRequest)sr;
            
                out.write("<tr><td>getRequestURL()</td><td>"+hsr.getRequestURL()+"</td></tr>");
                out.write("<tr><td>getRequestURI()</td><td>"+hsr.getRequestURI()+"</td></tr>");
                out.write("<tr><td>getContextPath()</td><td>"+hsr.getContextPath()+"</td></tr>");
                out.write("<tr><td>getServletPath()</td><td>"+hsr.getServletPath()+"</td></tr>");
                out.write("<tr><td>getPathInfo()</td><td>"+hsr.getPathInfo()+"</td></tr>");
            }
            
            Enumeration e=sr.getAttributeNames();
            while (e.hasMoreElements())
            {
                String name=(String)e.nextElement();
                out.write("<tr><td>getAttribute(\""+name+"\")</td><td>"+sr.getAttribute(name)+"</td></tr>");
            }
            out.write("</table>");

            if (sr instanceof ServletRequestWrapper)
                sr=((ServletRequestWrapper)sr).getRequest();
            else
                sr=null;
        }
        
        if (referer!=null) out.write("<p><a href=\""+referer+"\">BACK</a></p>");
        out.write("</body></html>");
        
    }
}

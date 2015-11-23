/* ==============================================
 * Copyright 2003 Mort Bay Consulting Pty Ltd. All rights reserved.
 * Distributed under the artistic license.
 * Created on 17/03/2004
 * $Id: Wiki.java,v 1.1 2006/01/21 17:28:01 gregw Exp $
 * ============================================== */

package com.mortbay.iwiki;

import java.util.Arrays;
import java.util.regex.Pattern;

/* ------------------------------------------------------------------------------- */
/**
 * @version $Revision: 1.1 $
 * @author gregw
 */
public class Wiki
{
    private Pattern pattern;
    private String substitute;

    private static String[] patterns =
    {
        // unix new lines
        "\\r\\n","\n",
        "\\n\\s+\\n","\n",
        "\\n\\n+","\n\n\n\n",
        
        // escape HTML
        "&", "\\&amp;",
        "<", "\\&lt;",

        // properties
        "\\$\\{properties.","\\${",
        "\\$\\{(\\S[^\\s}]*)(\\s)","\\${$1}$2",
        "\\$\\{","\\${properties.",
        
        // detect bullet list
        "\\n\\n?(\\* .*?)\\n\\n", "\n<ul>\n$1\n\n</ul>\n",
        "\\n\\* ", "\n\n\n\n* ",
        "\\n\\n\\* (.*?)\\n\\n", "<li>$1</li>\n",
        "(<ul class=.Content.>)\\n","$1",

        // detect ordered list
        "\\n\\n?(# .*?)\\n\\n", "\n<ol>\n$1\n\n</ol>\n",
        "\\n# ", "\n\n\n\n# ",
        "\\n\\n# (.*?)\\n\\n", "<li>$1</li>\n",
        "(<ol class=.Content.>)\\n","$1",
        
        // paragraphs
        "\\n\\n([#A-Za-z0-9\\[].*?)\\n\\n", "\n<p>$1</p>\n",

        // Sections
        "\\n!\\s*(\\S[^\\n]*)\\n","\n<h3>$1</h3>\n\n",
        
        // URLs 
        "\\[\\[(http:[^:]*?):([A-Za-z ]*?):([A-Za-z]*?):([^\\]]*?.(jpg|gif|png|JPG|GIF|PNG))]]",
                 "<a href=\"$1\"><img alt=\"$2\" class=\"$3\" src=\"$4\"/></a>",
        "\\[\\[(/[^:]*?):([A-Za-z ]*?):([A-Za-z]*?):([^\\]]*?.(jpg|gif|png|JPG|GIF|PNG))]]",
                 "<a href=\"\\${contextPath}$1\"><img alt=\"$2\" class=\"$3\" src=\"$4\"/></a>",
        "\\[\\[(/[^:]*?):([^\\]]*?)]]",
                 "<a href=\"\\${contextPath}$1\">$2</a>",
        "\\[\\[(http:[^\\]]*?):([^:\\]]*?)]]",
                 "<a href=\"$1\" target=\"_blank\" class=\"ext\">$2</a>",
        "\\[\\[([^\\]]*?):([^:\\]]*?)]]",
                 "<a href=\"$1\">$2</a>",
        "([-A-Za-z0-9]*?@[-A-Za-z0-9\\.]*)",
        "<a href=\"mailto:$1\" />$1</a>",
        
        
        // Images
        
        "([A-Za-z]*?):([A-Za-z ]*?):(\\S*\\.(jpg|gif|png|JPG|GIF|PNG))",
        "<img class=\"$1\" src=\"$3\" alt=\"$2\"/>",
        "([A-Za-z]*?):(\\S*\\.(jpg|gif|png|JPG|GIF|PNG))",
                 "<img class=\"$1\" src=\"$2\" alt=\"$2\"/>",
                
        
        // Spans
        "\\[([^:]*?)]",
                 "<span class=\"Bold\">$1</span>",
        "\\[([A-Za-z]*):(.*?)]",
                 "<span class=\"$1\">$2</span>",
                 
        // divs
        "\\(\\(([A-Za-z]*?):",
                 "<div class=\"$1\">",
        ":\\)\\)",
                 "</div>",
                          
        // breaks
        "\\$\\$","<br/>",
        // new para
        "\\{\\{","<p class=\"Content\">",
        // end para
        "\\}\\}","</p>",
        
        
        
    };
    
    static Wiki[] wikis = new Wiki[patterns.length/2];
    static
    {
      for (int i=0;i<patterns.length;i+=2)
        wikis[i/2]=new Wiki(patterns[i],patterns[i+1]);
    }
    
    /* ------------------------------------------------------------------------------- */
    /**
     * Constructor.
     */
    private Wiki(String regex,  String substitute)
    {
        pattern = Pattern.compile(regex,Pattern.DOTALL);
        this.substitute=substitute;
    }

    /* ------------------------------------------------------------------------------- */
    private String apply(String text)
    {
        text=pattern.matcher(text).replaceAll(substitute);
        return text;
    }

    /* ------------------------------------------------------------------------------- */
    public String toString()
    {
        return ("pattern="+pattern);
    }

    /* ------------------------------------------------------------------------------- */
    public static String transform(String text)
    {
        text="\n\n"+text+"\n\n";
        
        String[] bits = text.split("(\\{\\{\\{)|(\\}\\}\\})");
        
        StringBuffer buf = new StringBuffer();
        
        for (int b=0;b<bits.length;b++)
        {
            if (b%2==1)
            {
                buf.append(bits[b]);
            }
            else
            {
                String t=bits[b];
            
                for (int i =0; i < wikis.length;i++)
                {
                    try
                    {
                        t=wikis[i].apply(t);
                    }
                    catch(Exception e)  
                    {
                        System.err.println(patterns[2*i]+":"+patterns[2*i+1]+":"+t);
                        e.printStackTrace();
                    }
                    catch(Error e)      
                    {
                        System.err.println(patterns[2*i]+":"+patterns[2*i+1]+":"+t);
                        e.printStackTrace();
                    }
                }
                buf.append(t);
            }
        }
        return buf.toString();
    }
}

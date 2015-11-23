//========================================================================
//$Id: Log.java,v 1.1 2006/01/21 17:28:01 gregw Exp $
//Copyright 2000-2004 Mort Bay Consulting Pty. Ltd.
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

package com.mortbay.iwiki;

import javax.servlet.ServletContext;


/**
 * 
 * Log
 *
 * @author janb
 * @version $Revision: 1.1 $ $Date: 2006/01/21 17:28:01 $
 *
 */
public class Log
{
    private static ServletContext context;
    
    public static void setContext(ServletContext c)
    {
        context = c;
    }
    public static void log (String message)
    {
        if (context != null)
            context.log(message);
        else
            System.err.println (message);
        
    }
    
    public static void log (String message, Throwable t)
    {
        if (context != null)
            context.log (message, t);
        else
        {
            System.err.println (message);
            System.err.println (t);
        }
    }
}

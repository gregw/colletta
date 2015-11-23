/* ==============================================
 * Copyright 2003 Mort Bay Consulting Pty Ltd. All rights reserved.
 * Distributed under the artistic license.
 * Created on 11/03/2004
 * $Id: Page.java,v 1.1 2006/01/21 17:28:01 gregw Exp $
 * ============================================== */
 
package com.mortbay.iwiki;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/* ------------------------------------------------------------------------------- */
/** 
 * 
 * @version $Revision: 1.1 $
 * @author gregw
 */
public class Page implements Comparable
{
    /* ------------------------------------------------------------------------------- */
    private static Page[] buildPagePath(Page p,int n)
    {
        Page pp=p.getParent();
        if (pp==null)
        {
            Page[] path=new Page[n+1];
            path[0]=p;
            return path;
        }
        
        Page[] path=buildPagePath(pp,n+1);
        path[path.length-n-1]=p;
        return path;
    }
    private Page[] children;
    private File directory;
    private String dirName;
    private String[] images;
    private String[] langs;
    private HashSet languages;
    private HashMap mutablePropertyMap=new HashMap();
    private Page[] pagePath;
    private Page parent;
    private String path;
    private HashMap pathMap;
    private HashMap pathPropertyMap= new HashMap();
    private HashMap propertyMap=new HashMap();
    private Page root;
    private HashMap cache=new HashMap();
    private long lastModified;
    private YyyyMmDdHM lastModifiedYMDHM;
    private YyyyMmDd date;
    
    public Page(String name,String path, File directory,Page parent, Page root)
        throws Exception
    {  
        this.dirName=name;
        this.path=path;
        this.directory=directory;
        this.parent=parent;
        if (root==null)
        {
            pathMap=new HashMap();
            languages=new HashSet();
            root=this;
        }
        this.root=root;
        
        ArrayList children=new ArrayList();
        ArrayList images=new ArrayList();
        File[] files = directory.listFiles();
        for (int i=0;files!=null && i<files.length;i++)
        {
            File f=files[i];
            String n=f.getName();
            
            if (f.isDirectory())
            {
                File pageProps = new File(f,"page.properties");
                if (!pageProps.exists())
                    continue;
                
                Page child = new Page(n,path+n+"/",f,this,this.root);
                children.add(child);
            }
            else if (n==null || n.startsWith("child_"))
            {
                continue;
            }
            else if (n.startsWith("page") && n.endsWith(".properties"))
            {
                String lang=null;
                if (n.charAt(4)=='_' && n.charAt(7)=='.')
                    lang=n.substring(5,7);
                else if (!n.equals("page.properties"))
                    continue;
                if (lang!=null)
                {
                    root.languages.add(lang);
                    root.langs=null;
                }
                Properties properties = new Properties();
                FileInputStream fis = new FileInputStream(f);
                properties.load(fis);
                fis.close();
                Iterator iter = properties.keySet().iterator();
                while (iter.hasNext())
                {
                    String p=(String)iter.next();
                    properties.setProperty(p, URLDecoder.decode(properties.getProperty(p),"UTF-8"));
                }
                
                mutablePropertyMap.put(lang, properties);
                propertyMap.put(lang, Collections.unmodifiableMap(properties));
                
            }
            else if (n.endsWith(".gif") || n.endsWith(".GIF") || n.endsWith(".jpg") || n.endsWith(".JPG") || n.endsWith(".png") || n.endsWith(".PNG") )
            {
                if (f.lastModified()>lastModified)
                    lastModified=f.lastModified();
                if (n.indexOf("-tiny.")<0 &&
                    n.indexOf(".thumb.")<0)
                    images.add(n);
            }
            else if (n.endsWith(".txt"))
            {
                if (f.lastModified()>lastModified)
                    lastModified=f.lastModified();
                checkWiki(n);
            }
        }
        if (lastModified==0)
            lastModified=directory.lastModified();
        
        lastModifiedYMDHM=new YyyyMmDdHM(lastModified);
        
        root.pathMap.put(path,this);
        Collections.sort(children);
        this.children=(Page[])children.toArray(new Page[children.size()]);
        
        Collections.sort(images);
        this.images=(String[])images.toArray(new String[images.size()]);
    }
    
    /* ------------------------------------------------------------------------------- */
    public void checkWiki(String txtname)
    throws IOException
    {
        File txt = new File(directory,txtname);
        File jsp = new File(directory,txtname.substring(0,txtname.length()-4)+".jsp");
        
        if (!txt.exists())
            return;
        if (jsp.exists())
        {
            if (jsp.lastModified()>=txt.lastModified())
                return;
            jsp.renameTo(new File(directory,jsp.getName()+"."+(System.currentTimeMillis()/1000)));
        }

        Reader in = new InputStreamReader(new FileInputStream(txt),"UTF-8");
        char[] buf = new char[4096];
        int len=0;
        StringBuffer out = new StringBuffer();
        while ((len=in.read(buf))>0)
            out.append(buf, 0, len);
        String text=out.toString();
        
        if (text!=null && text.trim().length()>0)
        {
            PrintWriter pout=new PrintWriter(new OutputStreamWriter(new FileOutputStream(jsp,false),"UTF-8"));
            pout.println(wiki(text));
            pout.close();
        }
    }
    

    /* ------------------------------------------------------------------------------- */
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object page)
    {
        Page p2 = (Page)page;
        
        String d1=getProperty(null,"date");
        String d2=p2.getProperty(null,"date");
        if (d1!=null && d2!=null)
            return d1.compareTo(d2);
        
        String o1=getProperty(null,"order");
        if (o1==null)
            o1=getDirName();
        String o2=p2.getProperty(null,"order");
        if (o2==null)
            o2=p2.getDirName();
        
        return o1.compareTo(o2);
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * @return Returns the children.
     */
    public Page[] getChildren()
    {
        return children;
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * @return Returns the directory.
     */
    public File getDirectory()
    {
        return directory;
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * @return Returns the name.
     */
    public String getDirName()
    {
        return dirName;
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * @return Returns the name property, or dirname if not set.
     */
    public String getDisplayName()
    {
        String n=getProperty("name");
        if (n==null)
            n=getDirName();
        return n;
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * @return Returns the images.
     */
    public String[] getImages()
    {
        return images;
    }

    /* ------------------------------------------------------------------------------- */
    public YyyyMmDdHM getLastModified()
    {
        return lastModifiedYMDHM;
    }

    /* ------------------------------------------------------------------------------- */
    public YyyyMmDd getDate()
    {
        if (date==null)
        {
            String d = getProperty("date");
            if (d!=null)
                date=new YyyyMmDd(d);
            else
                date=new YyyyMmDd(lastModified);
        }
        return date;
    }


    /* ------------------------------------------------------------------------------- */
    public int getIntProperty(String property)
    {
        String p = getProperty(null, property);
        if (p!=null)
        {
            try
            {
                return Integer.parseInt(p);
            }
            catch(Exception e){}
        }
        return 0;
    }

    /* ------------------------------------------------------------------------------- */
    public int getIntPathProperty(String property)
    {
        String p = getPathProperty(null, property);
        if (p!=null)
        {
            try
            {
                return Integer.parseInt(p);
            }
            catch(Exception e){}
        }
        return 0;
    }

    /* ------------------------------------------------------------------------------- */
    public String[] getLanguages()
    {
        if (root.langs==null)
            root.langs = (String[])root.languages.toArray(new String[root.languages.size()]);
        return root.langs;
    }
    
    /* ------------------------------------------------------------------------------- */
    /**
     * @return Returns the name.
     */
    public String getName(String lang)
    {
        String n=getProperty(lang,"name");
        return n==null?dirName:n;
    }
    
    /* ------------------------------------------------------------------------------- */
    /** getPageByPath.
     * @param path Path in page hierarchy
     * @return The page specified by the path or null if not found.
     */
    public Page getPageByPath(String path)
    {
        return (Page)root.pathMap.get(path);
    }
    
    /* ------------------------------------------------------------------------------- */
    /** getPagePath.
     * @return Array of Pages from root to this page.
     */
    public Page[] getPagePath()
    {
        if (pagePath==null)
            pagePath=buildPagePath(this,0);
        return pagePath;
    }

    /* ------------------------------------------------------------------------------- */
    /** getProperty.
     * Get a property from only from Page properties of the type lang.
     * @param lang
     * @param property
     * @return
     */
    public String getPageProperty(String lang, String property)
    {
        Object o=null;
        Map m = getProperties(lang);
        if (m!=null)
            o=m.get(property);
        
        if (o instanceof String)
            return (String)o;
        if (o !=null)
            return o.toString();
        return null;
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * @return Returns the parent.
     */
    public Page getParent()
    {
        return parent;
    }
    

    /* ------------------------------------------------------------------------------- */
    /**
     * @return Returns the path.
     */
    public String getPath()
    {
        return path;
    }
    
    /* ------------------------------------------------------------------------------- */
    /** getProperties for this path.
     * Combination of the properties for all nodes to this page combining the default and the 
     * language properties.
     * @param lang The lang (eg en, fr, it) or null for the default properties. 
     * @return Map of properties.
     */
    public Map getPathProperties(String lang)
    {
        Map map = (Map)pathPropertyMap.get(lang);
        if (map==null)
        {
            map=new HashMap();
            Page[] pp = getPagePath();
            for (int i=0;i<pp.length;i++)
            {
                Map pm=pp[i].getProperties(null);
                if (pm!=null)
                    map.putAll(pm);
                if (lang!=null)
                {
                    pm=pp[i].getProperties(lang);
                    if (pm!=null)
                        map.putAll(pm);
                }
            }
            pathPropertyMap.put(lang,Collections.unmodifiableMap(map));
        }
        return map;
    }
    
    /* ------------------------------------------------------------------------------- */
    /** getPathProperty.
     * Get a property from the path.  If not found try english then italian.
     * @param lang
     * @param property
     * @return
     */
    public String getPathProperty(String lang, String property)
    {
        Object o = null;
        Map m=getPathProperties(lang);
        if (m!=null)
            o=m.get(property);
        
        // try english
        if (o==null)
        {
            m=getPathProperties("en");
            if (m!=null)
                o=m.get(property);
        }
        
        // try italian
        if (o==null)
        {
            m=getPathProperties("it");
            if (m!=null)
                o=m.get(property);
        }
        
        if (o instanceof String)
            return (String)o;
        if (o !=null)
            return o.toString();
        return null;
    }
    
    /* ------------------------------------------------------------------------------- */
    /** getProperties for this page.
     * 
     * @param lang The lang (eg en, fr, it) or null for the default properties. 
     * @return Map of properties.
     */
    public Map getProperties(String lang)
    {
        return (Map) propertyMap.get(lang);
    }

    /* ------------------------------------------------------------------------------- */
    /** getProperty.
     * Get a property from only this page Page. If not defined for the default properties, then try en, then it.
     * @param lang
     * @param property
     * @return
     */
    public String getProperty(String property)
    {
        return getProperty(null,property);
    }
    
    /* ------------------------------------------------------------------------------- */
    /** getProperty.
     * Get a property from only this page Page by lang. If not defined for the lang looking in 
     * default properties, then en, then it.
     * @param lang
     * @param property
     * @return
     */
    public String getProperty(String lang, String property)
    {
        Object o=null;
        Map m = getProperties(lang);
        if (m!=null)
            o=m.get(property);
        if (o==null && lang!=null)
        {
            m=getProperties(null);
            if (m!=null)
                o=m.get(property);
        }

        // Try English
        if (o==null )
        {
            m=getProperties("en");
            if (m!=null)
                o=m.get(property);
        }
        
        // Try Italian
        if (o==null )
        {
            m=getProperties("it");
            if (m!=null)
                o=m.get(property);
        }
        
        if (o instanceof String)
            return (String)o;
        if (o !=null)
            return o.toString();
        return null;
    }

    /* ------------------------------------------------------------------------------- */
    public String getText(String lang)
        throws IOException
    {
        String filename = "text_"+lang+".txt";
        File file = new File(directory,filename);
        if (!file.exists())
            return null;
        Reader in = new InputStreamReader(new FileInputStream(file),"UTF-8");
        char[] buf = new char[4096];
        int len=0;
        StringBuffer out = new StringBuffer();
        while ((len=in.read(buf))>0)
            out.append(buf, 0, len);
        return out.toString().trim();
    }
    
    /* ------------------------------------------------------------------------------- */
    /**
     * @return Returns the name.
     */
    public String getTitle(String lang)
    {
        String t=getProperty(lang,"title");
        return t==null?getName(lang):t;
    }


    /* ------------------------------------------------------------------------------- */
    /** newChild.
     * @param n
     */
    public void newChild(String n)
    throws IOException
    {
        n=n.toLowerCase();
        File nd = new File(directory,n);
        if (nd.exists())
            return;
        
        nd.mkdir();
       
        // Copy child templates down
        File[] files = directory.listFiles();
        for (int f=0;f<files.length;f++)
        {
            if (files[f].getName().startsWith("child_"))
            {
                File to = new File(nd,files[f].getName().substring(6));
                IO.copy(files[f], to);
            }
        }
        
        
        // customize as required
        File ol = new File(directory,"layout.jsp");
        File nl = new File(nd,"layout.jsp");
        if (!nl.exists())
            IO.copy(ol,nl);
        
        File pp = new File(nd,"page.properties");
        if (!pp.exists())
        {
            PrintWriter out=new PrintWriter(new FileWriter(pp,false));
            out.println("# Common page properties");
            out.println("order: 100");
            out.close();
        }
        
        File ep = new File(nd,"page_en.properties");
        PrintWriter out=new PrintWriter(new FileWriter(ep,true));
        out.println("name: "+n.toUpperCase().substring(0,1)+n.toLowerCase().substring(1));
        out.println("title: "+n.toUpperCase().substring(0,1)+n.toLowerCase().substring(1));
        out.println("blurb: About "+n);
        out.close();
    }
    
    /* ------------------------------------------------------------------------------- */
    public void save()
    throws IOException
    {
        Iterator iter=propertyMap.keySet().iterator();
        
        while (iter.hasNext())
        {
            String lang = (String)iter.next();
            Map pm = (Map)propertyMap.get(lang);
            if (pm!=null)
            {      
                String filename="page"+(lang==null?".properties":("_"+lang+".properties"));
                File file = new File(directory,filename);
                if (file.exists())
                    file.renameTo(new File(directory,filename+"."+(System.currentTimeMillis()/1000)));
                
                PrintWriter out=new PrintWriter(new FileWriter(file,false));
                out.println("# Generated by Colletta");

                ArrayList keys = new ArrayList(pm.keySet());
                Collections.sort(keys);
                Iterator i=keys.iterator();
                while(i.hasNext())
                {
                    String p=(String)i.next();
                    String v=URLEncoder.encode((String)pm.get(p),"UTF-8");
                    out.println(p+": "+v);
                }
                out.flush();
                out.close();
            }
        }
    }

    /* ------------------------------------------------------------------------------- */
    /** setPageParameter.
     * @param lang
     * @param property
     * @param value
     */
    public void setPageProperty(String lang,String property,String value)
    {
        Map m = (Map)mutablePropertyMap.get(lang);
        if (value==null)
        {
            if (m!=null)
                m.remove(property);
        }
        else
        {
            if (m==null)
            {
                m=new HashMap();
                mutablePropertyMap.put(lang, m);
                propertyMap.put(lang, Collections.unmodifiableMap(m));
            }
            m.put(property, value);
        }
        pathPropertyMap.clear();
    }
    
    /* ------------------------------------------------------------------------------- */
    public void setText(String lang, String text)
        throws IOException
    {
        long ts=System.currentTimeMillis();
        
        String filename = "text_"+lang+".txt";
        File file = new File(directory,filename);
        if (file.exists())
            file.renameTo(new File(directory,filename+"."+(ts/1000)));

        if (text!=null)
        {
            text=StringUtil.replace(text, "\r\n", "\n");
            text=text.trim();
        }
        
        if (text!=null && text.trim().length()>0)
        {
            PrintWriter out=new PrintWriter(new OutputStreamWriter(new FileOutputStream(file,false),"UTF-8"));
            out.println(text);
            out.flush();
            out.close();
        }
        
        // Generate JSP
        filename = "text_"+lang+".jsp";
        file = new File(directory,filename);
        if (file.exists())
            file.renameTo(new File(directory,filename+"."+(ts/1000)));
        
        if (text!=null && text.trim().length()>0)
        {
            PrintWriter out=new PrintWriter(new OutputStreamWriter(new FileOutputStream(file,false),"UTF-8"));
            out.println(wiki(text));
            out.flush();
            out.close();
        }
        
    }
    
    
    /* ------------------------------------------------------------------------------- */
    public String toString()
    {
        return (dirName==null?"ROOT":dirName);
    }

    /* ------------------------------------------------------------------------------- */
    public String wiki(String text)
    {
        text="<%@ page contentType=\"text/html; charset=UTF-8\" %>\n"+
        "<!-- DO NOT EDIT.  THIS IS A GENERATED FILE -->\n"+
        Wiki.transform(text);
        return text;
    }
    
    /**
     * @return
     */
    public HashMap cache()
    {
        return cache;
    }
    
    public Collection getAllPages()
    {
        List l=new ArrayList();
        getAllPages(l);
        return l;
    }
    
    private void getAllPages(Collection pages)
    {
        pages.add(this);
        if (children!=null)
        {
            for(int c=0;c<children.length;c++)
                children[c].getAllPages(pages);
        }
    }
    
    
    public static class LastModified implements Comparator
    {

        /* ------------------------------------------------------------ */
        /* 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object o1, Object o2)
        {
            long mp1=((Page)o1).lastModified;
            long mp2=((Page)o2).lastModified;
            if (mp1>mp2)
                return -1;
            if (mp1<mp2)
                return 1;
            return 0;
        }
    
    }
    
}

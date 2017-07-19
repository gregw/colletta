package it.colletta.reservation;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class Util 
{
	
	public static String findWebInf ()
	throws Exception
	{	    
	        Path webInf = FileSystems.getDefault().getPath("src", "main", "webapp", "WEB-INF");
	        File f = webInf.toFile();
	        if (!f.exists())
	            return null;
	        
	        return f.getCanonicalPath();
	}
	
}

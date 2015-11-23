package it.colletta.reservation;

import java.io.File;


public class Util 
{
	
	public static String findWebInf ()
	throws Exception
	{
		File currentDir = new File(System.getProperty("user.dir"));
		
		System.err.println("Trying "+currentDir);
		String webInf = getWebInf(currentDir);
		if (webInf != null)
			return webInf;
		
		currentDir = currentDir.getParentFile();
		System.err.println("Trying "+currentDir);
		webInf = getWebInf(currentDir);
		if (webInf != null)
			return webInf;
		
		currentDir = new File (System.getProperty("user.home")+File.separator+"src"+File.separator+"colletta");
		System.err.println("Trying "+currentDir);
		webInf = getWebInf(currentDir);
		if (webInf != null)
			return webInf;
		
		currentDir = new File (System.getProperty("user.home")+File.separator+"src"+File.separator+"Colletta");
		System.err.println("Trying "+currentDir);
		webInf = getWebInf(currentDir);
		if (webInf != null)
			return webInf;
		
		return null;
	}
	
	private static String getWebInf (File dir)
	throws Exception
	{
		File webInf = new File(dir, "WEB-INF");
		if (webInf.exists())
			return webInf.getCanonicalPath();
		
		return null;
	}

}

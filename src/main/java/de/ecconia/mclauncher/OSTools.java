package de.ecconia.mclauncher;

public class OSTools
{
	public static String osName;
	
	static
	{
		osName = System.getProperty("os.name").toLowerCase();
	}
	
	public static boolean isOsName(String name)
	{
		return osName.contains(name);
	}
}

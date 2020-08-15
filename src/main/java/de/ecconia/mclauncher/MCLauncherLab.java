package de.ecconia.mclauncher;

import de.ecconia.mclauncher.data.VersionInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MCLauncherLab
{
	public static void installNatives(VersionInfo version)
	{
		File nativesFolder = new File(new File(Locations.versionsFolder, version.getInfo().getId()), version.getInfo().getId() + "-natives");
		version.getLibraryInfo().installNatives(Locations.librariesFolder, nativesFolder);
	}
	
	public static void run(VersionInfo version, LoginProfile profile)
	{
		Locations.gameFolder.mkdirs();
		Locations.runFolder.mkdirs();
		
		File versionFolder = new File(Locations.versionsFolder, version.getInfo().getId());
		//> which java
		//> l /usr/bin/java
		//> l /etc/alternatives/java
		//> --> /usr/lib/jvm/java-8-oracle/jre/bin/java
		//Find: /usr/lib/jvm/java-8-oracle/jre/bin/java
		// Why not just "java" then
		
		//Why: -Xmx1G -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:-UseAdaptiveSizePolicy -Xmn128M
		
		//Create classpath:
		String classpath = version.getLibraryInfo().genClasspath(Locations.librariesFolder);
		classpath += File.pathSeparator + new File(versionFolder, version.getInfo().getId() + ".jar").getAbsolutePath();
		
		//Create natives directory:
		File nativesFolder = new File(versionFolder, version.getInfo().getId() + "-natives");
		
		List<String> arguments = new ArrayList<>();
		arguments.add("java"); //Here?
		arguments.add("-Xmx1G");
		//arguments.add("-XX:+UseConcMarkSweepGC");
		//arguments.add("-XX:+CMSIncrementalMode");
		//arguments.add("-XX:-UseAdaptiveSizePolicy");
		//arguments.add("-Xmn128M");
		arguments.addAll(version.getArguments().build(version, classpath, nativesFolder.getAbsolutePath(), profile));

		for(int i = 0; i < arguments.size(); i++)
		{
			String argument = arguments.get(i);
			if(argument.indexOf(' ') != -1)
			{
				argument = '"' + argument + '"';
				arguments.set(i, argument);
			}
		}

		for(String arg : arguments)
		{
			System.out.println(arg);
		}
		
		ProcessBuilder builder = new ProcessBuilder(arguments);
		builder.directory(Locations.runFolder);
		
		try
		{
			Process process = builder.start();
			new Thread(() -> {
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String tmp;
				try
				{
					while((tmp = reader.readLine()) != null)
					{
						System.out.println(">> " + tmp);
					}
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}).start();
			new Thread(() -> {
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				String tmp;
				try
				{
					while((tmp = reader.readLine()) != null)
					{
						System.out.println("x> " + tmp);
					}
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}).start();
			System.out.println("Started");
			process.waitFor();
			System.out.println("Terminated...");
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
	}
}

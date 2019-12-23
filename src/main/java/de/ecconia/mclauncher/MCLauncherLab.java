package de.ecconia.mclauncher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import de.ecconia.java.json.JSONObject;
import de.ecconia.java.json.JSONParser;
import de.ecconia.mclauncher.data.OnlineVersionList;
import de.ecconia.mclauncher.data.OnlineVersionList.OnlineVersion;
import de.ecconia.mclauncher.data.VersionInfo;

public class MCLauncherLab
{
	public static void main(String[] args)
	{
		OnlineVersionList onlineList = new OnlineVersionList();
		OnlineVersion latestStable = onlineList.getVersion(onlineList.getLatestRelease());
		
		EasyRequest request = new EasyRequest(latestStable.getUrl());
		File gameFolder = new File("data/versions/1.15.1/");
		gameFolder.mkdirs();
		try
		{
			Files.write(new File(gameFolder, "1.15.1.json").toPath(), request.asBytes());
		}
		catch(IOException e)
		{
			System.out.println("Error saving file.");
			e.printStackTrace(System.out);
			return;
		}
		
		JSONObject object = (JSONObject) JSONParser.parse(request.getBody());
		VersionInfo version = new VersionInfo(object, latestStable.getUrl());
//		download(version, gameFolder);
		run(version, gameFolder);
	}
	
	public static void run(VersionInfo version, File gameFolder)
	{
		//> which java
		//> l /usr/bin/java
		//> l /etc/alternatives/java
		//> --> /usr/lib/jvm/java-8-oracle/jre/bin/java
		//Find: /usr/lib/jvm/java-8-oracle/jre/bin/java
		// Why not just "java" then
		
		//Why: -Xmx1G -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:-UseAdaptiveSizePolicy -Xmn128M
		
		//Create classpath:
		File libraryFile = new File("data/libraries/");
		String classpath = version.getLibraryInfo().genClasspath(libraryFile);
		classpath += ':' + new File(gameFolder, "1.15.1.jar").getAbsolutePath();
		
		//Create natives directory:
//		File libraryFolder = new File("data/libraries/");
		File nativesFolder = new File(gameFolder, "1.15.1-natives");
//		version.getLibraryInfo().installNatives(libraryFolder, nativesFolder);
		
		List<String> arguments = new ArrayList<>();
		arguments.add("java"); //Here?
		arguments.add("-Xmx1G");
		arguments.add("-XX:+UseConcMarkSweepGC");
		arguments.add("-XX:+CMSIncrementalMode");
		arguments.add("-XX:-UseAdaptiveSizePolicy");
		arguments.add("-Xmn128M");
		arguments.addAll(version.getArguments().build(version, classpath, nativesFolder.getAbsolutePath()));
		
		for(String arg : arguments)
		{
			System.out.println(arg);
		}
		
		ProcessBuilder builder = new ProcessBuilder(arguments);
		builder.directory(new File("data"));
		
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
	
	public static void download(VersionInfo version, File folder)
	{
		version.getAssetsInfo().download();
		version.getLibraryInfo().download();
		version.getDownloads().download(new File(folder, "1.15.1.jar"), "client");
	}
}

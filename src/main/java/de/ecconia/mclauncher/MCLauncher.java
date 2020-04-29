package de.ecconia.mclauncher;

import de.ecconia.java.json.JSONObject;
import de.ecconia.java.json.JSONParser;
import de.ecconia.mclauncher.data.OnlineVersionList;
import de.ecconia.mclauncher.data.VersionInfo;
import de.ecconia.mclauncher.download.VersionDownloader;
import de.ecconia.mclauncher.webrequests.Requests;
import de.ecconia.mclauncher.webrequests.Response;
import java.io.IOException;

public class MCLauncher
{
	private static VersionInfo currentVersion;
	private static byte[] versionInfoRaw;
	
	public static void main(String[] args)
	{
		//You may remove or adjust this as you please, but if you run it from an IDE its helpful to have.
		String ideUsername = "Ecconia";
		if(args.length > 0)
		{
			ideUsername = args[0];
		}
		
		//Install and or Run:
		setCurrentVersion("1.15.2"); //No verification, that it exists.
//		installVersion(); //Just needs to be done once!
		run(ideUsername); //Start the game from the selected version.
	}
	
	private static void setCurrentVersion(String targetVersion)
	{
		OnlineVersionList onlineList = new OnlineVersionList(); //Download all versions file
		OnlineVersionList.OnlineVersion targetVersionEntry = onlineList.getVersion(targetVersion); //Pick desired version
		
		//Download amd parse full info file for this version:
		Response response = Requests.sendGetRequest(targetVersionEntry.getUrl());
		JSONObject object = (JSONObject) JSONParser.parse(response.getResponse());
		currentVersion = new VersionInfo(object, targetVersionEntry.getUrl());
		versionInfoRaw = response.getResponseRaw();
	}
	
	public static void installVersion()
	{
		if(currentVersion == null)
		{
			throw new IllegalStateException("Please run \"setCurrentVersion\" first.");
		}
		Locations.rootFolder.mkdirs(); //Ensure the root folder is ready.
		VersionDownloader.download(currentVersion, versionInfoRaw);
		MCLauncherLab.installNatives(currentVersion);
	}
	
	public static void run(String username)
	{
		if(currentVersion == null)
		{
			throw new IllegalStateException("Please run \"setCurrentVersion\" first.");
		}
		
		try
		{
			LoginProfile profile = SimpleProfileStorage.loadProfile(username);
			if(profile == null)
			{
				System.err.println("Something went wrong querying the login data from the Mojang server.");
				System.exit(1);
			}
			MCLauncherLab.run(currentVersion, profile);
		}
		catch(IOException e)
		{
			System.err.println("Could not load profile file, aborting to prevent more errors.");
			e.printStackTrace();
			System.exit(1);
		}
	}
}

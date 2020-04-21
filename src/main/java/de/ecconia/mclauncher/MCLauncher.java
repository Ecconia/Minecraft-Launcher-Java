package de.ecconia.mclauncher;

import de.ecconia.java.json.JSONObject;
import de.ecconia.java.json.JSONParser;
import de.ecconia.mclauncher.data.OnlineVersionList;
import de.ecconia.mclauncher.data.VersionInfo;
import de.ecconia.mclauncher.download.VersionDownloader;

public class MCLauncher
{
	private static VersionInfo currentVersion;
	private static byte[] versionInfoRaw;
	
	public static void main(String[] args)
	{
		//Prints a new access token to console please paste it into the RunArguments class
//		AccessTokenQuery.queryAccessToken();
		
		//Install and or Run:
		setCurrentVersion("1.15.2"); //No verification, that it exists.
		installVersion(); //Just needs to be done once!
		run(); //Start the game from the selected version.
	}
	
	private static void setCurrentVersion(String targetVersion)
	{
		OnlineVersionList onlineList = new OnlineVersionList(); //Download all versions file
		OnlineVersionList.OnlineVersion targetVersionEntry = onlineList.getVersion(targetVersion); //Pick desired version
		
		//Download amd parse full info file for this version:
		EasyRequest request = new EasyRequest(targetVersionEntry.getUrl());
		JSONObject object = (JSONObject) JSONParser.parse(request.getBody());
		currentVersion = new VersionInfo(object, targetVersionEntry.getUrl());
		versionInfoRaw = request.asBytes();
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
	
	public static void run()
	{
		if(currentVersion == null)
		{
			throw new IllegalStateException("Please run \"setCurrentVersion\" first.");
		}
		MCLauncherLab.run(currentVersion);
	}
}

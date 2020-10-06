package de.ecconia.mclauncher;

import de.ecconia.mclauncher.download.VersionDownloader;
import de.ecconia.mclauncher.newdata.LoadedVersion;
import de.ecconia.mclauncher.newdata.Version;
import java.io.IOException;

public class MCLauncher
{
	private static LoadedVersion currentVersion;
	
	public static void main(String[] args)
	{
		//You may remove or adjust this as you please, but if you run it from an IDE its helpful to have.
		String ideUsername = "Ecconia";
		if(args.length > 0)
		{
			ideUsername = args[0];
		}
		
		//Install and or Run:
		LauncherCore core = new LauncherCore();
		
		String versionName = "1.16.3";
		Version version = core.loadVersion(versionName);
		if(version == null)
		{
			LauncherCore.error("Could not load version '" + versionName + "'");
		}
		else if(!(version instanceof LoadedVersion))
		{
			throw new RuntimeException("Got unloaded version which should never be the case.");
		}
		else
		{
			System.out.println();
			LauncherCore.normal("Successfully loaded version '" + versionName + "'");
			currentVersion = (LoadedVersion) version;
			
//			installVersion(); //Just needs to be done once!
			run(ideUsername); //Start the game from the selected version.
		}
	}
	
	public static void installVersion()
	{
		if(currentVersion == null)
		{
			throw new IllegalStateException("Please load a version first.");
		}
		Locations.rootFolder.mkdirs(); //Ensure the root folder is ready.
		VersionDownloader.download(currentVersion);
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

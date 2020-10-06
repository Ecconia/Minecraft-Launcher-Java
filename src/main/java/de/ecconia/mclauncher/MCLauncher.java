package de.ecconia.mclauncher;

import de.ecconia.mclauncher.newdata.LoadedVersion;
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
		LoadedVersion version = core.loadVersion(versionName);
		System.out.println();
		if(version == null)
		{
			LauncherCore.error("Could not load version '" + versionName + "'");
		}
		else
		{
			System.out.println();
			LauncherCore.normal("Successfully loaded version '" + versionName + "'");
			currentVersion = version;
			
			try
			{
				core.downloadNecessaryFiles(version);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
			
			System.out.println();
			run(ideUsername); //Start the game from the selected version.
		}
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

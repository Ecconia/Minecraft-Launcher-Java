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
		String ideProfile = "1.17";
		if(args.length > 0)
		{
			ideUsername = args[0];
		}
		
		if(args.length > 1)
		{
			ideProfile = args[0];
			ideUsername = args[1];
		}
		
		//Install and or Run:
		LauncherCore core = new LauncherCore();
		
		LoadedVersion version = core.loadVersion(ideProfile);
		System.out.println();
		if(version == null)
		{
			LauncherCore.error("Could not load version '" + ideProfile + "'");
		}
		else
		{
			System.out.println();
			LauncherCore.normal("Successfully loaded version '" + ideProfile + "'");
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

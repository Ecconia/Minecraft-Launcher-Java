package de.ecconia.mclauncher;

import de.ecconia.java.json.JSONObject;
import de.ecconia.java.json.JSONParser;
import de.ecconia.mclauncher.data.OnlineVersionList;
import de.ecconia.mclauncher.newdata.FullVersion;
import de.ecconia.mclauncher.newdata.LoadedVersion;
import de.ecconia.mclauncher.newdata.OnlineVersion;
import de.ecconia.mclauncher.newdata.Version;
import de.ecconia.mclauncher.webrequests.RequestException;
import de.ecconia.mclauncher.webrequests.Requests;
import de.ecconia.mclauncher.webrequests.Response;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;

public class LauncherCore
{
	private OnlineVersionList onlineList;
	
	public LoadedVersion loadVersion(String versionID)
	{
		//Attempt to load the profile from local:
		File versionFolder = new File(Locations.versionsFolder, versionID);
		if(!versionFolder.exists())
		{
			warning("Version '" + versionID + "' is not installed locally. Attempting to look through Mojang-Version-List.");
			
			OnlineVersionList onlineList = getOnlineVersionList();
			if(onlineList == null)
			{
				warning("Mojang versions list is not accessible, cannot use version '" + versionID + "'.");
				return null;
			}
			OnlineVersion onlineVersion = onlineList.getVersion(versionID); //Pick desired version
			if(onlineVersion == null)
			{
				warning("Version '" + versionID + "' does not seem to be a Mojang-Version. Please install manually.");
				return null;
			}
			
			return installOnlineVersion(onlineVersion);
		}
		
		try
		{
			normal("Loading local version '" + versionID + "'");
			byte[] bytes = Files.readAllBytes(new File(versionFolder, versionID + ".json").toPath());
			JSONObject versionObject = (JSONObject) JSONParser.parse(new String(bytes));
			return jsonToOfflineVersion(versionObject);
		}
		catch(Exception e)
		{
			//TODO: Properly.
			throw new RuntimeException("Was not able to read local file (or parse it whatever)...", e);
		}
	}
	
	public OnlineVersionList getOnlineVersionList()
	{
		if(onlineList == null)
		{
			try
			{
				onlineList = new OnlineVersionList(); //Download all versions file
			}
			catch(RequestException e)
			{
				IOException ioEx = (IOException) e.getCause();
				if(ioEx == null)
				{
					LauncherCore.error("Was not able to download Mojang versions, no internet? Report stacktrace:");
					e.printStackTrace(System.out);
					return null;
				}
				
				if(ioEx instanceof UnknownHostException)
				{
					LauncherCore.error("Was not able to resolve domain '" + ioEx.getMessage() + "'. No internet connection?");
				}
				else
				{
					LauncherCore.error("Was not able to download Mojang versions, got IOException. No internet? Report stacktrace:");
					e.printStackTrace(System.out);
				}
			}
			catch(Exception e)
			{
				LauncherCore.error("Was not able to download Mojang versions, no internet? Report stacktrace:");
				e.printStackTrace(System.out);
			}
		}
		
		return onlineList;
	}
	
	public LoadedVersion installOnlineVersion(OnlineVersion onlineVersion)
	{
		//Download amd parse full info file for this version:
		Response response = Requests.sendGetRequest(onlineVersion.getUrl());
		JSONObject versionObject = (JSONObject) JSONParser.parse(response.getResponse());
		LoadedVersion version = jsonToOfflineVersion(versionObject);
		
		//Save profile.json
		normal("Saving version " + onlineVersion.getId());
		String id = version.getId();
		Locations.rootFolder.mkdirs();
		File versionFolder = new File(Locations.versionsFolder, id);
		versionFolder.mkdirs();
		saveBytes(response.getResponseRaw(), new File(versionFolder, id + ".json"));
		
		return version;
	}
	
	public LoadedVersion jsonToOfflineVersion(JSONObject versionObject)
	{
		String inheritsFrom = versionObject.getStringOrNull("inheritsFrom");
		if(inheritsFrom == null)
		{
			return new FullVersion(versionObject);
		}
		else
		{
			//TODO: Support it.
			throw new RuntimeException("Inheriting of versions is not yet supported.");
//			return new InheritingVersion(versionObject);
		}
	}
	
	private static void saveBytes(byte[] bytes, File location)
	{
		try
		{
			Files.write(location.toPath(), bytes);
		}
		catch(IOException e)
		{
			//TODO: Proper handling:
			e.printStackTrace();
			throw new RuntimeException("Oopsie, see stacktrace, error handling after cleanup.");
		}
	}
	
	public static void normal(String message)
	{
		System.out.println("[NORMAL] " + message);
	}
	
	public static void warning(String message)
	{
		System.out.println("[WARNING] " + message);
	}
	
	public static void error(String message)
	{
		System.out.println("[ERROR] " + message);
	}
}

package de.ecconia.mclauncher;

import de.ecconia.java.json.JSONObject;
import de.ecconia.java.json.JSONParser;
import de.ecconia.mclauncher.data.DownloadSection;
import de.ecconia.mclauncher.data.OnlineVersionList;
import de.ecconia.mclauncher.data.assets.AssetsInfo;
import de.ecconia.mclauncher.data.libraries.LibraryEntry;
import de.ecconia.mclauncher.download.ArtifactDownload;
import de.ecconia.mclauncher.download.DownloadInfo;
import de.ecconia.mclauncher.newdata.FullVersion;
import de.ecconia.mclauncher.newdata.InheritingVersion;
import de.ecconia.mclauncher.newdata.LoadedVersion;
import de.ecconia.mclauncher.newdata.OnlineVersion;
import de.ecconia.mclauncher.webrequests.RequestException;
import de.ecconia.mclauncher.webrequests.Requests;
import de.ecconia.mclauncher.webrequests.Response;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.Map;

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
			LoadedVersion inheritFromVersion = loadVersion(inheritsFrom);
			if(inheritFromVersion == null)
			{
				error("Parent version '" + inheritsFrom + "' could not be loaded, thus cannot load '" + versionObject + "'.");
				return null;
			}
			return new InheritingVersion(inheritFromVersion, versionObject);
		}
	}
	
	public void downloadNecessaryFiles(LoadedVersion version) throws IOException
	{
		Files.createDirectories(Locations.rootFolder.toPath());
		
		//Download assets:
		normal("Checking assets:");
		AssetsInfo assets = version.getAssetsInfo();
		File assetsIndexFile = new File(Locations.assetsIndexesFolder, assets.getId() + ".json");
		if(!assetsIndexFile.exists())
		{
			warning("Assets index '" + assets.getId() + "' not present, downloading...");
			assets.getAssetsManifest().download(assetsIndexFile);
			normal("Assets index '" + assets.getId() + "' downloaded.");
		}
		//Load assets index file:
		byte[] bytes = Files.readAllBytes(assetsIndexFile.toPath());
		JSONObject assetsIndexJSON = (JSONObject) JSONParser.parse(new String(bytes));
		assets.resolveFromJSON(assetsIndexJSON);
		//Create objects folder:
		Files.createDirectories(Locations.assetsObjectsFolder.toPath());
		//Download the assets (if not already):
		for(AssetsInfo.AssetsPart object : version.getAssetsInfo().getObjects())
		{
			File destination = new File(Locations.assetsObjectsFolder, object.getHash().substring(0, 2));
			if(!destination.exists())
			{
				Files.createDirectories(destination.toPath());
			}
			destination = new File(destination, object.getHash());
			
			if(destination.exists() && destination.length() == object.getSize())
			{
				continue;
			}
			normal("Downloading missing asset: " + object.getPath());
			
			Response response = Requests.sendGetRequest("https://resources.download.minecraft.net/" + object.getHash().substring(0, 2) + "/" + object.getHash());
			if(response.getResponseRaw().length != object.getSize())
			{
				System.out.println("File size " + response.getResponseRaw().length + "/" + object.getSize());
				throw new RuntimeException("Oopsie, see stacktrace, error handling after cleanup.");
			}
			
			Files.write(destination.toPath(), response.getResponseRaw());
		}
		normal("> Assets present.");
		
		//Download jar:
		DownloadSection dlSection = version.getDownloads();
		if(dlSection != null)
		{
			ArtifactDownload clientJarDL = dlSection.get("client");
			if(clientJarDL == null)
			{
				throw new RuntimeException("Download section without client download.");
			}
			String id = version.getId();
			File versionFolder = new File(Locations.versionsFolder, id);
			File clientFile = new File(versionFolder, id + ".jar");
			if(!clientFile.exists() || clientFile.length() != clientJarDL.getSize())
			{
				normal("Client jar missing, downloading " + id);
				clientJarDL.download(clientFile);
				normal("Done downloading client jar.");
			}
		}
		
		//Download libraries:
		normal("Checking libraries:");
		for(LibraryEntry entry : version.getLibraryInfo().getRelevantLibraries())
		{
			//Native entries come along with the same entry without the natives. Thus skip it once.
			if(entry.isNonNative())
			{
				if(!entry.exists(Locations.librariesFolder))
				{
					normal("Downloading missing library: " + entry.getName());
					if(!entry.download(Locations.librariesFolder))
					{
						throw new RuntimeException("Oopsie, see stacktrace, error handling after cleanup.");
					}
				}
			}
			else
			{
				for(Map.Entry<String, String> nativesTypeEntry : entry.getNatives().entrySet())
				{
					//TODO: PLZ find a better way to just query for the right OS @Future self (For example save the different types while parsing).
					if(OSTools.isOsName(nativesTypeEntry.getKey()))
					{
						DownloadInfo nativeFile = entry.getClassifiers().get(nativesTypeEntry.getValue());
						if(nativeFile == null)
						{
							System.out.println("Expected library: " + entry.getName() + " to have native module in classified section, but it had not. For OS: " + nativesTypeEntry.getKey());
							throw new RuntimeException("Oopsie, see stacktrace, error handling after cleanup.");
						}
						
						if(!nativeFile.exists(Locations.librariesFolder))
						{
							normal("Downloading missing natives: " + entry.getName());
							if(!nativeFile.download(Locations.librariesFolder))
							{
								throw new RuntimeException("Oopsie, see stacktrace, error handling after cleanup.");
							}
						}
						
						break;
					}
				}
			}
		}
		normal("> Libraries present.");
		
		//Extract natives:
		File nativesFolder = new File(new File(Locations.versionsFolder, version.getId()), version.getId() + "-natives");
		if(!nativesFolder.exists())
		{
			normal("Natives folder does not exist, creating...");
			version.getLibraryInfo().installNatives(Locations.librariesFolder, nativesFolder);
			normal("Natives folder created.");
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

package de.ecconia.mclauncher.download;

import de.ecconia.mclauncher.Locations;
import de.ecconia.mclauncher.OSTools;
import de.ecconia.mclauncher.data.VersionInfo;
import de.ecconia.mclauncher.data.assets.AssetsInfo.AssetsPart;
import de.ecconia.mclauncher.data.libraries.LibraryEntry;
import de.ecconia.mclauncher.webrequests.Requests;
import de.ecconia.mclauncher.webrequests.Response;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map.Entry;

public class VersionDownloader
{
	public static void download(VersionInfo version, byte[] versionInfoSource)
	{
		String id = version.getInfo().getId();
		
		//Save versions/<version>/<version>.json
		File versionFolder = new File(Locations.versionsFolder, id);
		versionFolder.mkdirs();
		if(versionInfoSource != null)
		{
			saveBytes(versionInfoSource, new File(versionFolder, id + ".json"));
		}
		
		//Save versions/<version>/<version>.jar
		DownloadInfo clientJarDL = version.getDownloads().get("client");
		//TODO: Handle null - never trust Mojang!
		clientJarDL.genericDownload(new File(versionFolder, id + ".jar"));
		
		//Save assets/indexes/<version>.json
		byte[] assetsInfoSource = version.getAssetsInfo().resolve();
		if(assetsInfoSource == null)
		{
			System.out.println("WARNING: Assets information has been resolved before download -> No saving of the assets info possible. Pray its saved already.");
		}
		else
		{
			Locations.assetsIndexesFolder.mkdirs();
			saveBytes(assetsInfoSource, new File(Locations.assetsIndexesFolder, version.getAssetsInfo().getId() + ".json"));
		}
		
		//Save assets/objects/*
		Locations.assetsObjectsFolder.mkdirs();
		for(AssetsPart object : version.getAssetsInfo().getObjects())
		{
			File destination = new File(Locations.assetsObjectsFolder, object.getHash().substring(0, 2));
			destination.mkdir();
			destination = new File(destination, object.getHash());
			
			Response response = Requests.sendGetRequest("https://resources.download.minecraft.net/" + object.getHash().substring(0, 2) + "/" + object.getHash());
			if(response.getResponseRaw().length != object.getSize())
			{
				System.out.println("File size " + response.getResponseRaw().length + "/" + object.getSize());
				throw new RuntimeException("Oopsie, see stacktrace, error handling after cleanup.");
			}
			
			try
			{
				Files.write(destination.toPath(), response.getResponseRaw());
			}
			catch(IOException e)
			{
				e.printStackTrace();
				throw new RuntimeException("Oopsie, see stacktrace, error handling after cleanup.");
			}
		}
		
		//Save library/*
		Locations.librariesFolder.mkdirs();
		for(LibraryEntry libraryEntry : version.getLibraryInfo().getRelevantLibraries())
		{
			//If there are natives there !!probably!! also is another library entry for this artifact.
			if(libraryEntry.isNonNative())
			{
				if(!libraryEntry.getArtifact().genericDownload(Locations.librariesFolder))
				{
					throw new RuntimeException("Oopsie, see stacktrace, error handling after cleanup.");
				}
			}
			else
			{
				for(Entry<String, String> nativesTypeEntry : libraryEntry.getNatives().entrySet())
				{
					//TODO: PLZ find a better way to just query for the right OS @Future self (For example save the different types while parsing).
					if(OSTools.isOsName(nativesTypeEntry.getKey()))
					{
						DownloadInfo nativeFile = libraryEntry.getClassifiers().get(nativesTypeEntry.getValue());
						if(nativeFile == null)
						{
							System.out.println("Expected library: " + libraryEntry.getName() + " to have native module in classified section, but it had not. For OS: " + nativesTypeEntry.getKey());
							throw new RuntimeException("Oopsie, see stacktrace, error handling after cleanup.");
						}
						
						if(!nativeFile.genericDownload(Locations.librariesFolder))
						{
							throw new RuntimeException("Oopsie, see stacktrace, error handling after cleanup.");
						}
						
						break;
					}
				}
			}
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
}

package de.ecconia.mclauncher.data;

import de.ecconia.java.json.JSONObject;
import de.ecconia.mclauncher.data.OnlineVersionList.OnlineVersion;
import de.ecconia.mclauncher.data.assets.AssetsInfo;
import de.ecconia.mclauncher.data.libraries.LibraryInfo;

public class VersionInfo
{
	private final OnlineVersion info; //TODO: Check if same - sanity test
	//URL in there is null?
	
	private final String mainClass; //Used when starting the game
	private final String assets; //Used when start the game
	private final AssetsInfo assetsInfo; //Used when downloading the game
	
	private final LibraryInfo libraryInfo;
	private final DownloadSection downloads;
	private final RunArguments arguments;
	
	//Logging ?
	private final int minimumLauncherVersion;
	
	public VersionInfo(JSONObject object, String url)
	{
		object.put("url", url);
		
		this.info = new OnlineVersion(object);
		this.assets = object.getString("assets");
		this.assetsInfo = new AssetsInfo(object.getObject("assetIndex"));
		this.minimumLauncherVersion = object.getInt("minimumLauncherVersion");
		this.libraryInfo = new LibraryInfo(object.getArray("libraries"));
		this.downloads = new DownloadSection(object.getObject("downloads"));
		this.mainClass = object.getString("mainClass");
		this.arguments = new RunArguments(object.getObject("arguments"));
	}
	
	public OnlineVersion getInfo()
	{
		return info;
	}
	
	public String getAssets()
	{
		return assets;
	}
	
	public AssetsInfo getAssetsInfo()
	{
		return assetsInfo;
	}
	
	public LibraryInfo getLibraryInfo()
	{
		return libraryInfo;
	}
	
	public String getMainClass()
	{
		return mainClass;
	}
	
	public RunArguments getArguments()
	{
		return arguments;
	}
	
	public int getMinimumLauncherVersion()
	{
		return minimumLauncherVersion;
	}

	public DownloadSection getDownloads()
	{
		return downloads;
	}
}

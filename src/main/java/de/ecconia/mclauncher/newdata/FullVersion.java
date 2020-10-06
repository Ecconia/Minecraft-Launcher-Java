package de.ecconia.mclauncher.newdata;

import de.ecconia.java.json.JSONObject;
import de.ecconia.mclauncher.data.DownloadSection;
import de.ecconia.mclauncher.data.RunArguments;
import de.ecconia.mclauncher.data.assets.AssetsInfo;
import de.ecconia.mclauncher.data.libraries.LibraryInfo;

public class FullVersion extends LoadedVersion
{
	private final String assets; //Used when start the game
	private final AssetsInfo assetsInfo; //Used when downloading the game
	
	private final LibraryInfo libraryInfo;
	private final DownloadSection downloads;
	private final RunArguments arguments;
	
	//TODO: Logging.
	
	private final int minimumLauncherVersion;
	
	public FullVersion(JSONObject versionObject)
	{
		super(versionObject); //TODO: Validate data matches!
		
		this.assets = versionObject.getString("assets");
		this.assetsInfo = new AssetsInfo(versionObject.getObject("assetIndex"));
		this.minimumLauncherVersion = versionObject.getInt("minimumLauncherVersion");
		this.libraryInfo = new LibraryInfo(versionObject.getArray("libraries"));
		this.downloads = new DownloadSection(versionObject.getObject("downloads"));
		this.arguments = new RunArguments(versionObject.getObject("arguments"));
	}
	
	public String getAssets()
	{
		return assets;
	}
	
	@Override
	public AssetsInfo getAssetsInfo()
	{
		return assetsInfo;
	}
	
	@Override
	public LibraryInfo getLibraryInfo()
	{
		return libraryInfo;
	}
	
	@Override
	public RunArguments getArguments()
	{
		return arguments;
	}
	
	public int getMinimumLauncherVersion()
	{
		return minimumLauncherVersion;
	}
	
	@Override
	public DownloadSection getDownloads()
	{
		return downloads;
	}
}

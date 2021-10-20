package de.ecconia.mclauncher.newdata;

import de.ecconia.java.json.JSONObject;
import de.ecconia.mclauncher.data.DownloadSection;
import de.ecconia.mclauncher.data.RunArguments;
import de.ecconia.mclauncher.data.RunArgumentsNew;
import de.ecconia.mclauncher.data.assets.AssetsInfo;
import de.ecconia.mclauncher.data.libraries.LibraryInfo;

public abstract class LoadedVersion extends Version
{
	private String mainClass;
	
	public LoadedVersion(String type, String time, String releaseTime, String id, String mainClass)
	{
		super(type, time, releaseTime, id);
		this.mainClass = mainClass;
	}
	
	public LoadedVersion(JSONObject object)
	{
		super(object);
		this.mainClass = object.getString("mainClass");
	}
	
	public String getMainClass()
	{
		return mainClass;
	}
	
	public abstract DownloadSection getDownloads();
	
	public abstract AssetsInfo getAssetsInfo();
	
	public abstract LibraryInfo getLibraryInfo();
	
	public abstract RunArguments getArguments();
}

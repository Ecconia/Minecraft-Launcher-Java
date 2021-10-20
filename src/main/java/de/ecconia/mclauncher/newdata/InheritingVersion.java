package de.ecconia.mclauncher.newdata;

import de.ecconia.java.json.JSONObject;
import de.ecconia.mclauncher.data.DownloadSection;
import de.ecconia.mclauncher.data.RunArguments;
import de.ecconia.mclauncher.data.RunArgumentsNew;
import de.ecconia.mclauncher.data.assets.AssetsInfo;
import de.ecconia.mclauncher.data.libraries.LibraryInfo;

public class InheritingVersion extends LoadedVersion
{
	private final LoadedVersion parentVersion;
	private final LibraryInfo libraryInfo;
	
	public InheritingVersion(LoadedVersion parentVersion, JSONObject versionObject)
	{
		super(versionObject);
		this.parentVersion = parentVersion;
		
		this.libraryInfo = LibraryInfo.mergedCopy(parentVersion.getLibraryInfo(), new LibraryInfo(versionObject.getArray("libraries")));
		RunArgumentsNew arguments = new RunArgumentsNew(versionObject.getObject("arguments"));
		if(!arguments.isEmpty())
		{
			throw new RuntimeException("Arguments are not empty, please send the version.json to the developer.");
		}
	}
	
	@Override
	public DownloadSection getDownloads()
	{
		return parentVersion.getDownloads();
	}
	
	@Override
	public AssetsInfo getAssetsInfo()
	{
		return parentVersion.getAssetsInfo();
	}
	
	@Override
	public LibraryInfo getLibraryInfo()
	{
		return libraryInfo;
	}
	
	@Override
	public RunArguments getArguments()
	{
		return parentVersion.getArguments();
	}
}

package de.ecconia.mclauncher.download;

import de.ecconia.java.json.JSONObject;
import de.ecconia.mclauncher.webrequests.Requests;
import de.ecconia.mclauncher.webrequests.Response;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ArtifactDownload implements DownloadInfo
{
	private final String sha1;
	private final long size;
	private final String url;
	private final String path;
	
	public ArtifactDownload(JSONObject object)
	{
		this.sha1 = object.getString("sha1");
		this.size = object.getLong("size");
		this.url = object.getString("url");
		
		//Optional:
		//TODO: Add test if its equal to name.
		this.path = object.getStringOrNull("path");
	}
	
	public String getPath()
	{
		return path;
	}
	
	public String getUrl()
	{
		return url;
	}
	
	public long getSize()
	{
		return size;
	}
	
	public boolean download(File destination)
	{
		if(path != null)
		{
			destination = new File(destination, path);
		}
		
		try
		{
			Files.createDirectories(destination.getParentFile().toPath());
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
		
		Response response = Requests.sendGetRequest(url);
		
		if(response.getResponseRaw().length != size)
		{
			System.out.println("File size " + response.getResponseRaw().length + "/" + size);
			System.out.println("FILESIZE ERROR");
			return false;
		}
		
		//TODO: Do SHA1 probe.
		
		try
		{
			Files.write(destination.toPath(), response.getResponseRaw());
		}
		catch(IOException e)
		{
			System.out.println("Error saving file.");
			e.printStackTrace(System.out);
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean exists(File destination)
	{
		if(path != null)
		{
			destination = new File(destination, path);
		}
		
		return destination.exists() && destination.length() == getSize();
	}
}

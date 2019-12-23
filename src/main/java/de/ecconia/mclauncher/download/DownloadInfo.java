package de.ecconia.mclauncher.download;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import de.ecconia.java.json.JSONObject;
import de.ecconia.mclauncher.EasyRequest;

public class DownloadInfo
{
	private final String sha1;
	private final long size;
	private final String url;
	private final String path;
	
	public DownloadInfo(JSONObject object)
	{
		this.sha1 = object.getString("sha1");
		this.size = object.getLong("size");
		this.url = object.getString("url");
		
		//Optional:
		this.path = object.getStringOrNull("path");
	}
	
	public String getSha1()
	{
		return sha1;
	}
	
	public String getPath()
	{
		return path;
	}
	
	public long getSize()
	{
		return size;
	}
	
	public String getUrl()
	{
		return url;
	}
	
	public boolean genericDownload(File destination)
	{
		if(path != null)
		{
			int lastSlash = path.lastIndexOf('/') + 1;
			String fileName = path.substring(lastSlash, path.length());
			String pathRoot = path.substring(0, lastSlash);
			
			destination = new File(destination, pathRoot);
			destination.mkdirs();
			destination = new File(destination, fileName);
		}
		
		EasyRequest request = new EasyRequest(url);
		
		if(request.asBytes().length != size)
		{
			System.out.println("File size " + request.asBytes().length + "/" + size);
			System.out.println("FILESIZE ERROR");
			return false;
		}
		
		try
		{
			Files.write(destination.toPath(), request.asBytes());
		}
		catch(IOException e)
		{
			System.out.println("Error saving file.");
			e.printStackTrace(System.out);
			return false;
		}
		
		return true;
	}
}

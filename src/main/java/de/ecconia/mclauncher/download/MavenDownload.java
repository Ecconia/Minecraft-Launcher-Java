package de.ecconia.mclauncher.download;

import de.ecconia.mclauncher.webrequests.Requests;
import de.ecconia.mclauncher.webrequests.Response;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MavenDownload implements DownloadInfo
{
	private final String url;
	private final String path;
	
	public MavenDownload(String url, String path)
	{
		this.url = url;
		this.path = path;
	}
	
	@Override
	public boolean download(File destination)
	{
		String downloadURL = url + path;
		destination = new File(destination, path);
		
		try
		{
			Files.createDirectories(destination.getParentFile().toPath());
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
		
		Response response = Requests.sendGetRequest(downloadURL);
		
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
		destination = new File(destination, path);
		return destination.exists();
	}
}

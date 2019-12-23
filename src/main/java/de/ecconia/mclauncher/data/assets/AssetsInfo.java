package de.ecconia.mclauncher.data.assets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import de.ecconia.java.json.JSONObject;
import de.ecconia.java.json.JSONParser;
import de.ecconia.mclauncher.EasyRequest;
import de.ecconia.mclauncher.download.DownloadInfo;

public class AssetsInfo
{
	private final DownloadInfo assetsManifest;
	private final String id;
	private final long totalSize;
	
	private List<AssetsPart> objects;
	
	public AssetsInfo(JSONObject object)
	{
		this.assetsManifest = new DownloadInfo(object);
		this.id = object.getString("id");
		this.totalSize = object.getLong("totalSize");
	}
	
	public DownloadInfo getAssetsManifest()
	{
		return assetsManifest;
	}
	
	public String getId()
	{
		return id;
	}
	
	public long getTotalSize()
	{
		return totalSize;
	}
	
	public void download()
	{
		if(objects == null)
		{
			objects = new ArrayList<>();
			System.out.println("Downloading " + id + " index file.");
			EasyRequest request = new EasyRequest(assetsManifest.getUrl());
			String sourceCode = request.getBody();
			
			File destination = new File("data/assets/indexes/");
			destination.mkdirs();
			destination = new File(destination, id + ".json");
			
			try
			{
				Files.write(destination.toPath(), request.asBytes());
			}
			catch(IOException e)
			{
				System.out.println("Error saving index file.");
				e.printStackTrace(System.out);
				return;
			}
			
			JSONObject object = (JSONObject) JSONParser.parse(sourceCode);
			JSONObject objects = object.getObject("objects");
			for(Entry<String, Object> entry : objects.getEntries().entrySet())
			{
				String key = entry.getKey();
				JSONObject value = JSONObject.asObject(entry.getValue());
				this.objects.add(new AssetsPart(key, value.getLong("size"), value.getString("hash")));
			}
		}
		
		String downloadRoot = "https://resources.download.minecraft.net/";
		File destination = new File("data/assets/objects/");
		destination.mkdirs();
		
		System.out.println("Starting download of assets:");
		for(AssetsPart object : objects)
		{
			System.out.println(object.getPath() + " -> " + object.getHash());
			
			File thisDir = new File(destination, object.getHash().substring(0, 2));
			thisDir.mkdir();
			File thisFile = new File(thisDir, object.getHash());
			EasyRequest request = new EasyRequest(downloadRoot + object.getHash().substring(0, 2) + "/" + object.getHash());
			if(request.asBytes().length != object.getSize())
			{
				System.out.println("File size " + request.asBytes().length + "/" + object.getSize());
				System.out.println("FILESIZE ERROR");
				break;
			}
			
			try
			{
				Files.write(thisFile.toPath(), request.asBytes());
			}
			catch(IOException e)
			{
				System.out.println("Error saving file.");
				e.printStackTrace(System.out);
				break;
			}
		}
		System.out.println("Download done/aborted.");
	}
	
	private static class AssetsPart
	{
		private final String path;
		private final long size;
		private final String hash; //TBI: Type? (sha1, probs)
		
		public AssetsPart(String path, long size, String hash)
		{
			this.path = path;
			this.size = size;
			this.hash = hash;
		}
		
		public String getHash()
		{
			return hash;
		}
		
		public String getPath()
		{
			return path;
		}
		
		public long getSize()
		{
			return size;
		}
	}
}

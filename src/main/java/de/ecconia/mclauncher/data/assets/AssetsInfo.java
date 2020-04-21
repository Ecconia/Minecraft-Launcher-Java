package de.ecconia.mclauncher.data.assets;

import de.ecconia.java.json.JSONObject;
import de.ecconia.java.json.JSONParser;
import de.ecconia.mclauncher.download.DownloadInfo;
import de.ecconia.mclauncher.webrequests.Requests;
import de.ecconia.mclauncher.webrequests.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

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
	
	public List<AssetsPart> getObjects()
	{
		return objects;
	}
	
	public byte[] resolve()
	{
		if(objects == null)
		{
			objects = new ArrayList<>();
			//TBI: Is this a proper place to download - in a data class?
			Response reponse = Requests.sendGetRequest(assetsManifest.getUrl());
			String sourceCode = reponse.getResponse();
			
			JSONObject object = (JSONObject) JSONParser.parse(sourceCode);
			JSONObject objects = object.getObject("objects");
			for(Entry<String, Object> entry : objects.getEntries().entrySet())
			{
				String key = entry.getKey();
				JSONObject value = JSONObject.asObject(entry.getValue());
				this.objects.add(new AssetsPart(key, value.getLong("size"), value.getString("hash")));
			}
			
			return reponse.getResponseRaw();
		}
		
		return null;
	}
	
	public static class AssetsPart
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

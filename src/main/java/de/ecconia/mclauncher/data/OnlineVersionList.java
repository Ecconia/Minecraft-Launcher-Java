package de.ecconia.mclauncher.data;

import de.ecconia.java.json.JSONArray;
import de.ecconia.java.json.JSONObject;
import de.ecconia.java.json.JSONParser;
import de.ecconia.mclauncher.webrequests.Requests;
import de.ecconia.mclauncher.webrequests.Response;
import java.util.HashMap;
import java.util.Map;

public class OnlineVersionList
{
	private static final String versionManifest = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
	/* Return format:
	{
	·   "versions": [
	·   ·   {
	·   ·   ·   "releaseTime": "2019-12-16T10:29:47+00:00"
	·   ·   ·   "id": "1.15.1"
	·   ·   ·   "time": "2019-12-16T10:33:36+00:00"
	·   ·   ·   "type": "release"
	·   ·   ·   "url": "https://launchermeta.mojang.com/v1/packages/57c1b5f7376febf269f7e555f96def8f1a43bb76/1.15.1.json"
	·   ·   }
	·   ]
	·   "latest": {
	·   ·   "release": "1.15.1"
	·   ·   "snapshot": "1.15.1"
	·   }
	}
	*/
	
	private final Map<String, OnlineVersion> versions = new HashMap<>();
	private final String latestRelease;
	private final String latestSnapshot;
	
	public OnlineVersionList()
	{
		String versionManifestSource = getRequest(versionManifest);
		JSONObject versionManifestJSON = (JSONObject) JSONParser.parse(versionManifestSource);
		
		JSONArray versionsJSON = versionManifestJSON.getArray("versions");
		for(Object obj : versionsJSON.getEntries())
		{
			JSONObject versionJSON = JSONArray.asObject(obj);
			OnlineVersion version = new OnlineVersion(versionJSON);
			addVersion(version);
		}
		
		JSONObject latestJSON = versionManifestJSON.getObject("latest");
		latestRelease = latestJSON.getString("release");
		latestSnapshot = latestJSON.getString("snapshot");
	}
	
	private void addVersion(OnlineVersion version)
	{
		versions.put(version.getId(), version);
	}
	
	public String getLatestRelease()
	{
		return latestRelease;
	}
	
	public String getLatestSnapshot()
	{
		return latestSnapshot;
	}
	
	public OnlineVersion getVersion(String id)
	{
		return versions.get(id);
	}
	
	public static String getRequest(String address)
	{
		Response request = Requests.sendGetRequest(address);
		return request.getResponse();
	}
	
	public static class OnlineVersion
	{
		//Use better smaller types.
		private final String type;
		private final String time;
		private final String releaseTime;
		private final String url;
		private final String id;
		
		public OnlineVersion(String type, String time, String releaseTime, String url, String id)
		{
			this.type = type;
			this.time = time;
			this.releaseTime = releaseTime;
			this.url = url;
			this.id = id;
		}
		
		public OnlineVersion(JSONObject object)
		{
			this(
					object.getString("type"),
					object.getString("time"),
					object.getString("releaseTime"),
					object.getString("url"),
					object.getString("id"));
		}
		
		public String getId()
		{
			return id;
		}
		
		public String getReleaseTime()
		{
			return releaseTime;
		}
		
		public String getTime()
		{
			return time;
		}
		
		public String getType()
		{
			return type;
		}
		
		public String getUrl()
		{
			return url;
		}
	}
}

package de.ecconia.mclauncher.data;

import de.ecconia.java.json.JSONArray;
import de.ecconia.java.json.JSONObject;
import de.ecconia.java.json.JSONParser;
import de.ecconia.mclauncher.LauncherCore;
import de.ecconia.mclauncher.newdata.OnlineVersion;
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
		LauncherCore.normal("Downloading Mojang version list.");
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
		LauncherCore.normal("> Found " + versions.size() + " versions.");
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
}

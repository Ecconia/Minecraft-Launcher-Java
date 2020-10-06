package de.ecconia.mclauncher.newdata;

import de.ecconia.java.json.JSONObject;

public class OnlineVersion extends Version
{
	private final String url;
	
	public OnlineVersion(String type, String time, String releaseTime, String id, String url)
	{
		super(type, time, releaseTime, id);
		this.url = url;
	}
	
	public OnlineVersion(JSONObject object)
	{
		super(object);
		this.url = object.getString("url");
	}
	
	public String getUrl()
	{
		return url;
	}
}

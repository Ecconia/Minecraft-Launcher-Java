package de.ecconia.mclauncher.newdata;

import de.ecconia.java.json.JSONObject;
import de.ecconia.mclauncher.data.DownloadSection;

public abstract class Version
{
	//Use better smaller types.
	private String id;
	private String releaseTime;
	private String time;
	private String type;
	
	public Version(String type, String time, String releaseTime, String id)
	{
		this.type = type;
		this.time = time;
		this.releaseTime = releaseTime;
		this.id = id;
	}
	
	public Version(JSONObject object)
	{
		this(
				object.getString("type"),
				object.getString("time"),
				object.getString("releaseTime"),
				object.getString("id")
		);
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
}

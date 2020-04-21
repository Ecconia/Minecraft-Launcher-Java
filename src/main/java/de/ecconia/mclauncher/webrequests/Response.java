package de.ecconia.mclauncher.webrequests;

import java.nio.charset.StandardCharsets;

public class Response
{
	private final int code;
	private final String message;
	private final byte[] body;
	
	private String content;
	
	public Response(int code, String message, byte[] body)
	{
		this.code = code;
		this.message = message;
		this.body = body;
	}
	
	public boolean isOk()
	{
		return code >= 200 && code < 300;
	}
	
	public boolean hasBody()
	{
		return body.length != 0;
	}
	
	public int getResponseCode()
	{
		return code;
	}
	
	public String getResponseMessage()
	{
		return message;
	}
	
	public String getResponse()
	{
		if(content == null)
		{
			this.content = new String(body, StandardCharsets.UTF_8);
		}
		return content;
	}
	
	public byte[] getResponseRaw()
	{
		return body;
	}
}

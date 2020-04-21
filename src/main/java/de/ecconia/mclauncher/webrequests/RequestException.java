package de.ecconia.mclauncher.webrequests;

import java.io.IOException;

public class RequestException extends RuntimeException
{
	public RequestException(String s)
	{
		super(s);
	}
	
	public RequestException(String s, IOException e)
	{
		super(s, e);
	}
}

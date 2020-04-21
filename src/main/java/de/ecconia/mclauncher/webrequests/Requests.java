package de.ecconia.mclauncher.webrequests;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class Requests
{
	public static Response sendJSONRequest(String url, String json)
	{
		try
		{
			HttpsURLConnection con = (HttpsURLConnection) new URL(url).openConnection();
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.addRequestProperty("Content-Type", "application/json");
			
			PrintWriter writer = new PrintWriter(con.getOutputStream());
			writer.write(json);
			writer.flush();
			
			int code = con.getResponseCode();
			String message = con.getResponseMessage();
			byte[] body = readBody(code, con);
			
			return new Response(code, message, body);
		}
		catch(IOException e)
		{
			throw new RequestException("IOException while opening connection/writing data/getting response.", e);
		}
	}
	
	public static Response sendGetRequest(String url)
	{
		try
		{
			HttpsURLConnection con = (HttpsURLConnection) new URL(url).openConnection();
			con.setDoInput(true);
			con.setRequestMethod("GET");
			con.addRequestProperty("Content-Type", "application/json");
			
			int code = con.getResponseCode();
			String message = con.getResponseMessage();
			byte[] body = readBody(code, con);
			
			return new Response(code, message, body);
		}
		catch(IOException e)
		{
			throw new RequestException("IOException while opening connection/writing data/getting response.", e);
		}
	}
	
	private static byte[] readBody(int code, HttpsURLConnection con)
	{
		if(code != 201 && code != 204)
		{
			try
			{
				InputStream is = code >= 200 && code < 300 ? con.getInputStream() : con.getErrorStream();
				return readBody(is, con);
			}
			catch(IOException e)
			{
				throw new RequestException("IOException while opening InputStream.", e);
			}
		}
		else
		{
			return new byte[0];
		}
	}
	
	private static byte[] readBody(InputStream is, HttpsURLConnection con)
	{
		try
		{
			byte[] body;
			int expectedLength = con.getContentLength();
			if(expectedLength == -1)
			{
				//TBI: Is this a good buffer size? Some files are huge, some responses are small.
				byte[] buffer = new byte[10000];
				body = new byte[0];
				
				int readAmount = 0;
				while(true)
				{
					int amountAdded = is.read(buffer, readAmount, buffer.length - readAmount);
					if(amountAdded == -1)
					{
						//Stream is closed/has finished. All data read.
						break;
					}
					
					readAmount += amountAdded;
					if(readAmount >= buffer.length)
					{
						byte[] newBody = new byte[body.length + buffer.length];
						System.arraycopy(body, 0, newBody, 0, body.length);
						System.arraycopy(buffer, 0, newBody, body.length, buffer.length);
						body = newBody;
						readAmount = 0;
					}
				}
				
				is.close();
				
				if(readAmount > 0)
				{
					byte[] newBody = new byte[body.length + readAmount];
					System.arraycopy(body, 0, newBody, 0, body.length);
					System.arraycopy(buffer, 0, newBody, body.length, readAmount);
					body = newBody;
				}
			}
			else
			{
				body = new byte[expectedLength];
				
				int amountRemaining = expectedLength;
				int amountRead = 0;
				while(amountRemaining > 0)
				{
					int readAmount = is.read(body, amountRead, amountRemaining);
					if(readAmount == -1)
					{
						throw new RequestException("Connection aborted while receiving.");
					}
					amountRead += readAmount;
					amountRemaining -= readAmount;
				}
				
				is.close();
				
				if(amountRemaining < 0)
				{
					//TBI: What happened?
					throw new RequestException("Connection return more data than announced.");
				}
			}
			
			return body;
		}
		catch(IOException e)
		{
			throw new RequestException("IOException while reading response.", e);
		}
	}
}

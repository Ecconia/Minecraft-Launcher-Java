package de.ecconia.mclauncher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;

import javax.net.ssl.HttpsURLConnection;

public class EasyRequest
{
	private int responseCode;
	private String responseString;
	private byte[] body;
	
	private static final Charset utf8 = Charset.forName("utf-8");
	
	public EasyRequest(String url, String postBody)
	{
		try
		{
			HttpsURLConnection con = (HttpsURLConnection) new URL(url).openConnection();
			con.setDoInput(true);
			con.setDoOutput(true);
			
			con.setRequestMethod("POST");
			con.addRequestProperty("Content-Type", "application/json");
			
			PrintWriter writer = new PrintWriter(con.getOutputStream());
			writer.write(postBody);
			writer.flush();
			
			responseCode = con.getResponseCode();
			responseString = con.getResponseMessage();

			if(hasBody())
			{
				try
				{
					InputStream is = isOk() ? con.getInputStream() : con.getErrorStream();
					int expectedLength = con.getContentLength();
					
					if(expectedLength == -1)
					{
						//String request?
						byte[] buffer = new byte[10000];
						body = new byte[0];
						
						int readAmount = 0;
						while(true)
						{
							int amountAdded = is.read(buffer, readAmount, buffer.length - readAmount);
							if(amountAdded == -1)
							{
								//Did not read anything this cycle. And last cycle was the last one.
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
						
						if(readAmount > 0)
						{
							byte[] newBody = new byte[body.length + readAmount];
							System.arraycopy(body, 0, newBody, 0, body.length);
							System.arraycopy(buffer, 0, newBody, body.length, readAmount);
							body = newBody;
						}
						
						System.out.println("* Read " + body.length + " bytes.");
					}
					else
					{
						//System.out.println(expectedLength);
						body = new byte[expectedLength];
						
						int amountRemaining = expectedLength;
						int amountRead = 0;
						while(amountRemaining > 0)
						{
							int readAmount = is.read(body, amountRead, amountRemaining);
							amountRead += readAmount;
							amountRemaining -= readAmount;
						}
						
						is.close();
						
						if(amountRead != expectedLength)
						{
							System.out.println("Unexpected byte amount read: " + amountRead + '/' + expectedLength);
							body = new byte[0]; //Invalid anyway.
							return;
						}
					}
				}
				catch(IOException e)
				{
					if("Premature EOF".equals(e.getMessage()))
					{
						body = new byte[0];
					}
					else
					{
						throw e;
					}
				}
			}
			else
			{
				body = new byte[0];
			}
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public EasyRequest(String url)
	{
		try
		{
			HttpsURLConnection con = (HttpsURLConnection) new URL(url).openConnection();
			//con.setRequestProperty("user-agent", "Blabla-Client");
			con.setDoInput(true);
			
			con.connect();
			
			responseCode = con.getResponseCode();
			responseString = con.getResponseMessage();
			
//			System.out.println(responseCode + ": " + responseString);
			
			if(hasBody())
			{
				try
				{
					InputStream is = isOk() ? con.getInputStream() : con.getErrorStream();
					int expectedLength = con.getContentLength();
					
					if(expectedLength == -1)
					{
						//String request?
						byte[] buffer = new byte[10000];
						body = new byte[0];
						
						int readAmount = 0;
						while(true)
						{
							int amountAdded = is.read(buffer, readAmount, buffer.length - readAmount);
							if(amountAdded == -1)
							{
								//Did not read anything this cycle. And last cycle was the last one.
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
						
						if(readAmount > 0)
						{
							byte[] newBody = new byte[body.length + readAmount];
							System.arraycopy(body, 0, newBody, 0, body.length);
							System.arraycopy(buffer, 0, newBody, body.length, readAmount);
							body = newBody;
						}
						
						System.out.println("* Read " + body.length + " bytes.");
					}
					else
					{
						//System.out.println(expectedLength);
						body = new byte[expectedLength];
						
						int amountRemaining = expectedLength;
						int amountRead = 0;
						while(amountRemaining > 0)
						{
							int readAmount = is.read(body, amountRead, amountRemaining);
							amountRead += readAmount;
							amountRemaining -= readAmount;
						}
						
						is.close();
						
						if(amountRead != expectedLength)
						{
							System.out.println("Unexpected byte amount read: " + amountRead + '/' + expectedLength);
							body = new byte[0]; //Invalid anyway.
							return;
						}
					}
				}
				catch(IOException e)
				{
					if("Premature EOF".equals(e.getMessage()))
					{
						body = new byte[0];
					}
					else
					{
						throw e;
					}
				}
			}
			else
			{
				body = new byte[0];
			}
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public boolean isOk()
	{
		return responseCode >= 200 && responseCode < 300;
	}
	
	public boolean hasBody()
	{
		if(body == null)
		{
			return responseCode != 201 && responseCode != 204;
		}
		else
		{
			return body.length != 0;
		}
	}
	
	public int getResponseCode()
	{
		return responseCode;
	}
	
	public String getResponseString()
	{
		return responseString;
	}
	
	public String getBody()
	{
		return new String(body, utf8);
	}
	
	public byte[] asBytes()
	{
		return body;
	}
}

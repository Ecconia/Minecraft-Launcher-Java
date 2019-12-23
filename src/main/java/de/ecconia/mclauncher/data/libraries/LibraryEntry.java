package de.ecconia.mclauncher.data.libraries;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import de.ecconia.java.json.JSONArray;
import de.ecconia.java.json.JSONObject;
import de.ecconia.mclauncher.OSTools;
import de.ecconia.mclauncher.data.rules.Rules;
import de.ecconia.mclauncher.download.DownloadInfo;

public class LibraryEntry
{
	private final String name;
	private final Rules rules;
	
	private final DownloadInfo artifact;
	private final Map<String, DownloadInfo> classifiers;
	private final Map<String, String> natives;
	private final List<String> extractionEcludes;
	
	public LibraryEntry(JSONObject object)
	{
		JSONArray rulesJSON = object.getArrayOrNull("rules");
		this.rules = rulesJSON != null ? new Rules(rulesJSON) : null;
		this.name = object.getString("name");
		
		JSONObject downloadJSON = object.getObject("downloads");
		artifact = new DownloadInfo(downloadJSON.getObject("artifact"));
		
		JSONObject classifiersJSON = downloadJSON.getObjectOrNull("classifiers");
		if(classifiersJSON == null)
		{
			classifiers = null;
		}
		else
		{
			classifiers = new HashMap<>();
			for(Entry<String, Object> entry : classifiersJSON.getEntries().entrySet())
			{
				classifiers.put(entry.getKey(), new DownloadInfo(JSONObject.asObject(entry.getValue())));
			}
		}
		
		JSONObject nativesJSON = object.getObjectOrNull("natives");
		if(nativesJSON == null)
		{
			natives = null;
		}
		else
		{
			natives = new HashMap<>();
			for(Entry<String, Object> entry : nativesJSON.getEntries().entrySet())
			{
				natives.put(entry.getKey(), JSONObject.asString(entry.getValue()));
			}
		}
		
		JSONObject extractionJSON = object.getObjectOrNull("extract");
		if(extractionJSON == null)
		{
			extractionEcludes = null;
		}
		else
		{
			extractionEcludes = new ArrayList<>();
			JSONArray excludesJSON = extractionJSON.getArray("exclude");
			for(Object entry : excludesJSON.getEntries())
			{
				extractionEcludes.add(JSONArray.asString(entry));
			}
		}
	}
	
	public boolean isRelevant()
	{
		if(rules == null)
		{
			return true;
		}
		
		return rules.test();
	}
	
	public String getName()
	{
		return name;
	}
	
	public DownloadInfo getArtifact()
	{
		return artifact;
	}
	
	public boolean download(File rootLocation)
	{
		//If there are natives there !!probably!! also is another library entry for this artifact.
		if(isNonNative())
		{
			if(!artifact.genericDownload(rootLocation))
			{
				return false;
			}
		}
		else
		{
			for(Entry<String, String> entry : natives.entrySet())
			{
				if(OSTools.isOsName(entry.getKey()))
				{
					DownloadInfo nativeFile = classifiers.get(entry.getValue());
					if(nativeFile == null)
					{
						System.out.println("Expected library: " + name + " to have native module in classified section, but it had not. For OS: " + entry.getKey());
						return false;
					}
					
					if(!nativeFile.genericDownload(rootLocation))
					{
						return false;
					}
					
					break;
				}
			}
		}
		
		return true;
	}
	
	public String getClasspath(File libraryFile)
	{
		return new File(libraryFile, artifact.getPath()).getAbsolutePath();
	}
	
	public boolean isNonNative()
	{
		return natives == null;
	}
	
	public void installNatives(File libLocation, File destination)
	{
		for(Entry<String, String> entry : natives.entrySet())
		{
			if(OSTools.isOsName(entry.getKey()))
			{
				DownloadInfo nativeFile = classifiers.get(entry.getValue());
				if(nativeFile == null)
				{
					return;
				}
				
				File nativeJarLocation = new File(libLocation, nativeFile.getPath());
				
				try
				{
					JarFile jar = new JarFile(nativeJarLocation);
					Enumeration<JarEntry> enumEntries = jar.entries();
					while(enumEntries.hasMoreElements())
					{
						JarEntry file = enumEntries.nextElement();
						if(file.getName().endsWith(".so"))
						{
							File f = new File(destination + File.separator + file.getName());
							if(file.isDirectory())
							{
								//f.mkdir();
								continue;
							}
							InputStream is = jar.getInputStream(file);
							FileOutputStream fos = new FileOutputStream(f);
							while(is.available() > 0)
							{
								fos.write(is.read());
							}
							fos.close();
							is.close();
						}
					}
					jar.close();
				}
				catch(IOException e)
				{
					e.printStackTrace(System.out);
				}
				
				break;
			}
		}
	}
}

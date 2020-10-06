package de.ecconia.mclauncher.data.libraries;

import de.ecconia.java.json.JSONArray;
import de.ecconia.java.json.JSONObject;
import de.ecconia.mclauncher.OSTools;
import de.ecconia.mclauncher.data.rules.Rules;
import de.ecconia.mclauncher.download.ArtifactDownload;
import de.ecconia.mclauncher.download.DownloadInfo;
import de.ecconia.mclauncher.download.MavenDownload;
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

public class LibraryEntry
{
	private final String name;
	private final Rules rules;
	
	private final DownloadInfo download;
	private final Map<String, ArtifactDownload> classifiers;
	private final Map<String, String> natives;
	private final List<String> extractionExcludes;
	
	public LibraryEntry(JSONObject object)
	{
		JSONArray rulesJSON = object.getArrayOrNull("rules");
		this.rules = rulesJSON != null ? new Rules(rulesJSON) : null;
		
		this.name = object.getString("name");
		
		JSONObject downloadJSON = object.getObjectOrNull("downloads");
		if(downloadJSON != null)
		{
			download = new ArtifactDownload(downloadJSON.getObject("artifact"));
			
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
					classifiers.put(entry.getKey(), new ArtifactDownload(JSONObject.asObject(entry.getValue())));
				}
			}
		}
		else
		{
			String url = object.getString("url");
			download = new MavenDownload(url, getPath());
			
			classifiers = null;
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
			extractionExcludes = null;
		}
		else
		{
			extractionExcludes = new ArrayList<>();
			JSONArray excludesJSON = extractionJSON.getArray("exclude");
			for(Object entry : excludesJSON.getEntries())
			{
				extractionExcludes.add(JSONArray.asString(entry));
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
	
	public String getPath()
	{
		String[] parts = this.name.split(":");
		if(parts.length != 3)
		{
			throw new RuntimeException("Invalid library name: " + name);
		}
		
		String folders = parts[0].replace('.', '/');
		String fileName = parts[1];
		String version = parts[2];
		
		return folders + '/' + fileName + '/' + version + '/' + fileName + '-' + version + ".jar";
	}
	
	public Map<String, ArtifactDownload> getClassifiers()
	{
		return classifiers;
	}
	
	public Map<String, String> getNatives()
	{
		return natives;
	}
	
	public String getClasspath(File libraryFile)
	{
		return new File(libraryFile, getPath()).getAbsolutePath();
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
				ArtifactDownload nativeFile = classifiers.get(entry.getValue());
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
						if(file.getName().endsWith(".so") || file.getName().endsWith(".dll"))
						{
							File f = new File(destination + File.separator + file.getName());
							if(file.isDirectory())
							{
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
	
	public boolean download(File librariesFolder)
	{
		return download.download(librariesFolder);
	}
	
	public boolean exists(File destination)
	{
		return download.exists(destination);
	}
}

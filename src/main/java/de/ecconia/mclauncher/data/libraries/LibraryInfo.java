package de.ecconia.mclauncher.data.libraries;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.ecconia.java.json.JSONArray;
import de.ecconia.java.json.JSONObject;

public class LibraryInfo
{
	private final List<LibraryEntry> allLibararies = new ArrayList<>();
	private final List<LibraryEntry> relevantLibararies;
	
	public LibraryInfo(JSONArray object)
	{
		for(Object entry : object.getEntries())
		{
			JSONObject libraryEntryObject = JSONArray.asObject(entry);
			allLibararies.add(new LibraryEntry(libraryEntryObject));
		}
		
		//Apply rules, this should only filter the wrong operating systems (MaxOS or other).
		relevantLibararies = allLibararies.stream().filter(LibraryEntry::isRelevant).collect(Collectors.toList());
	}
	
	public String genClasspath(File libraryFile)
	{
		String tmp = relevantLibararies.get(0).getClasspath(libraryFile);
		for(int i = 1; i < relevantLibararies.size(); i++)
		{
			if(relevantLibararies.get(i).isNonNative())
			{
				tmp += File.pathSeparator + relevantLibararies.get(i).getClasspath(libraryFile);
			}
		}
		return tmp;
	}
	
	public void installNatives(File libLocation, File destination)
	{
		destination.mkdirs();
		for(LibraryEntry entry : relevantLibararies)
		{
			if(!entry.isNonNative())
			{
				entry.installNatives(libLocation, destination);
			}
		}
	}
	
	public List<LibraryEntry> getRelevantLibraries()
	{
		return relevantLibararies;
	}
}

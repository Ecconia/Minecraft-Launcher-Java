package de.ecconia.mclauncher.data.libraries;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.ecconia.java.json.JSONArray;
import de.ecconia.java.json.JSONObject;

public class LibraryInfo
{
	private final List<LibraryEntry> allLibraries = new ArrayList<>();
	private final List<LibraryEntry> relevantLibraries;
	
	public LibraryInfo(JSONArray object)
	{
		for(Object entry : object.getEntries())
		{
			JSONObject libraryEntryObject = JSONArray.asObject(entry);
			allLibraries.add(new LibraryEntry(libraryEntryObject));
		}
		
		//Apply rules, this should only filter the wrong operating systems (MaxOS or other).
		relevantLibraries = allLibraries.stream().filter(LibraryEntry::isRelevant).collect(Collectors.toList());
	}
	
	public LibraryInfo(List both)
	{
		allLibraries.addAll(both);
		
		//Apply rules, this should only filter the wrong operating systems (MaxOS or other).
		relevantLibraries = allLibraries.stream().filter(LibraryEntry::isRelevant).collect(Collectors.toList());
	}
	
	public static LibraryInfo mergedCopy(LibraryInfo one, LibraryInfo two)
	{
		List both = new ArrayList();
		both.addAll(one.getAllLibraries());
		both.addAll(two.getAllLibraries());
		return new LibraryInfo(both);
	}
	
	public String genClasspath(File libraryFile)
	{
		String tmp = relevantLibraries.get(0).getClasspath(libraryFile);
		for(int i = 1; i < relevantLibraries.size(); i++)
		{
			if(relevantLibraries.get(i).isNonNative())
			{
				tmp += File.pathSeparator + relevantLibraries.get(i).getClasspath(libraryFile);
			}
		}
		return tmp;
	}
	
	public void installNatives(File libLocation, File destination)
	{
		destination.mkdirs();
		for(LibraryEntry entry : relevantLibraries)
		{
			if(!entry.isNonNative())
			{
				entry.installNatives(libLocation, destination);
			}
		}
	}
	
	public List<LibraryEntry> getRelevantLibraries()
	{
		return relevantLibraries;
	}
	
	public List<LibraryEntry> getAllLibraries()
	{
		return allLibraries;
	}
}

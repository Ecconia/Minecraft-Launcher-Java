package de.ecconia.mclauncher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.List;

public class SimpleProfileStorage
{
	private static final File folder = new File("profiles");
	
	public static LoginProfile loadProfile(String name) throws IOException
	{
		if(!folder.exists())
		{
			System.out.println("Profile folder does not exist.");
			folder.mkdirs();
			return requestNewInteractive();
		}

		File profileFile = new File(folder, name);
		if(!profileFile.exists())
		{
			System.out.println("Profile does not exist.");
			return requestNewInteractive();
		}
		
		List<String> lines = Files.readAllLines(profileFile.toPath());
		if(lines.size() < 2)
		{
			System.out.println("Profile file corrupted.");
			return requestNewInteractive();
		}
		
		return new LoginProfile(name, lines.get(0), lines.get(1));
	}
	
	private static LoginProfile requestNewInteractive() throws IOException
	{
		LoginProfile profile = AccessTokenQuery.queryLoginProfile();
		if(profile == null)
		{
			return null;
		}
		
		//Save to file:
		File profileFile = new File(folder, profile.getUsername());
		PrintWriter pw = new PrintWriter(new FileWriter(profileFile, false));
		pw.write(profile.getUuid());
		pw.write("\n");
		pw.write(profile.getAccessToken());
		pw.write("\n");
		pw.close();
		
		return profile;
	}
}

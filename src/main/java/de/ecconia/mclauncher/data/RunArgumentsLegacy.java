package de.ecconia.mclauncher.data;

import de.ecconia.mclauncher.Locations;
import de.ecconia.mclauncher.LoginProfile;
import de.ecconia.mclauncher.newdata.LoadedVersion;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RunArgumentsLegacy implements RunArguments
{
	private final String[] arguments;
	
	public RunArgumentsLegacy(String arguments)
	{
		System.out.println(arguments);
		this.arguments = arguments.split(" ");
	}
	
	@Override
	public Collection<String> build(LoadedVersion version, String classpath, String nativesDirectory, LoginProfile profile)
	{
		List<String> finalArguments = new ArrayList<>();
		finalArguments.add("-Djava.library.path=" + nativesDirectory);
		finalArguments.add("-Dminecraft.launcher.brand=ecconia-minecraft-launcher");
		finalArguments.add("-Dminecraft.launcher.version=1.0");
		finalArguments.add("-cp");
		finalArguments.add(classpath);
		finalArguments.add(version.getMainClass());
		
		Pattern pat = Pattern.compile("\\$\\{([a-z_]+)\\}");
		List<String> args = Arrays.asList(arguments);
		for(int i = 0; i < args.size(); i++)
		{
			String arg = args.get(i);
			Matcher m = pat.matcher(arg);
			if(m.find())
			{
				String found = m.group(1);
				String replacement;
				if("auth_player_name".equals(found))
				{
					replacement = profile.getUsername();
				}
				else if("version_name".equals(found))
				{
					replacement = version.getId();
				}
				else if("game_directory".equals(found))
				{
					replacement = Locations.gameFolder.getAbsolutePath().replace("\\", "\\\\");
				}
				else if("assets_root".equals(found))
				{
					replacement = Locations.assetsFolder.getAbsolutePath().replace("\\", "\\\\");
				}
				else if("assets_index_name".equals(found))
				{
					replacement = version.getAssetsInfo().getId();
				}
				else if("auth_uuid".equals(found))
				{
					replacement = profile.getUuid();
				}
				else if("auth_access_token".equals(found))
				{
					replacement = profile.getAccessToken();
				}
				else if("user_type".equals(found))
				{
					replacement = "legacy";
				}
				else if("version_type".equals(found))
				{
					replacement = "release";
				}
				else
				{
					throw new RuntimeException("Unknown replacement variable: " + found);
				}
				args.set(i, m.replaceFirst(replacement));
			}
		}
		
		finalArguments.addAll(args);
		return finalArguments;
	}
}

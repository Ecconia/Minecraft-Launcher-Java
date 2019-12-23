package de.ecconia.mclauncher.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ecconia.java.json.JSONArray;
import de.ecconia.java.json.JSONObject;
import de.ecconia.mclauncher.data.rules.Rules;

public class RunArguments
{
	private List<Argument> argumentsJVM = new ArrayList<>();
	private List<Argument> argumentsGame = new ArrayList<>();
	
	public RunArguments(JSONObject argumentsJSON)
	{
		JSONArray jvmJSON = argumentsJSON.getArray("jvm");
		for(Object entry : jvmJSON.getEntries())
		{
			if(entry instanceof String)
			{
				String data = (String) entry;
				argumentsJVM.add(new StaticArgument(data));
			}
			else if(entry instanceof JSONObject)
			{
				JSONObject data = (JSONObject) entry;
				argumentsJVM.add(new RuledArgument(data));
			}
		}
		
		JSONArray gameJSON = argumentsJSON.getArray("game");
		for(Object entry : gameJSON.getEntries())
		{
			if(entry instanceof String)
			{
				String data = (String) entry;
				argumentsGame.add(new StaticArgument(data));
			}
			else if(entry instanceof JSONObject)
			{
				JSONObject data = (JSONObject) entry;
				argumentsGame.add(new RuledArgument(data));
			}
		}
	}
	
	public List<String> build(VersionInfo version, String classpath, String nativesDirectory)
	{
		Pattern pat = Pattern.compile("\\$\\{([a-z_]+)\\}");
		List<String> jvm = new ArrayList<>();
		for(Argument arg : argumentsJVM)
		{
			arg.apply(jvm);
		}
		//Apply arguments:
		for(int i = 0; i < jvm.size(); i++)
		{
			String arg = jvm.get(i);
			Matcher m = pat.matcher(arg);
			if(m.find())
			{
				String found = m.group(1);
				String replacement;
				if("classpath".equals(found))
				{
					replacement = classpath;
				}
				else if("launcher_name".equals(found))
				{
					replacement = "ecconia-minecraft-launcher";
				}
				else if("launcher_version".equals(found))
				{
					replacement = "1.0";
				}
				else if("natives_directory".equals(found))
				{
					replacement = nativesDirectory;
				}
				else
				{
					throw new RuntimeException("Unknown replacement variable: " + found);
				}
				jvm.set(i, m.replaceFirst(replacement));
			}
		}
		jvm.add(version.getMainClass());
//		System.out.println("JVM args:");
//		for(String arg : jvm)
//		{
//			System.out.println(arg);
//		}
		
		List<String> game = new ArrayList<>();
		for(Argument arg : argumentsGame)
		{
			arg.apply(game);
		}
		//Apply arguments:
		for(int i = 0; i < game.size(); i++)
		{
			String arg = game.get(i);
			Matcher m = pat.matcher(arg);
			if(m.find())
			{
				String found = m.group(1);
				String replacement;
				if("auth_player_name".equals(found))
				{
					replacement = "Ecconia";
				}
				else if("version_name".equals(found))
				{
					replacement = "1.15.1";
				}
				else if("game_directory".equals(found))
				{
					replacement = new File("data/").getAbsolutePath();
				}
				else if("assets_root".equals(found))
				{
					replacement = new File("data/assets").getAbsolutePath();
				}
				else if("assets_index_name".equals(found))
				{
					replacement = "1.15";
				}
				else if("auth_uuid".equals(found))
				{
					replacement = "ee01e49d577749bdb74dc676059694d3";
				}
				else if("auth_access_token".equals(found))
				{
					replacement = "<removed>";
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
				game.set(i, m.replaceFirst(replacement));
			}
		}
//		System.out.println("Game args:");
//		for(String arg : game)
//		{
//			System.out.println(arg);
//		}
		
		jvm.addAll(game);
		return jvm;
	}
	
	private abstract static class Argument
	{
		public abstract void apply(List<String> arguments);
	}
	
	private static class StaticArgument extends Argument
	{
		private final String value;
		
		public StaticArgument(String data)
		{
			value = data;
		}
		
		@Override
		public void apply(List<String> arguments)
		{
			arguments.add(value);
		}
	}
	
	private static class RuledArgument extends Argument
	{
		private final Rules rules;
		private final String[] values;
		
		public RuledArgument(JSONObject object)
		{
			Object valueObj = object.get("value");
			if(valueObj instanceof String)
			{
				values = new String[] {
					(String) valueObj
				};
			}
			else
			{
				JSONArray valuesJSON = (JSONArray) valueObj;
				values = new String[valuesJSON.getEntries().size()];
				for(int i = 0; i < valuesJSON.getEntries().size(); i++)
				{
					values[i] = JSONArray.asString(valuesJSON.getEntries().get(i));
				}
			}
			
			rules = new Rules(object.getArray("rules"));
		}
		
		@Override
		public void apply(List<String> arguments)
		{
			if(rules.test())
			{
				for(int i = 0; i < values.length; i++)
				{
					arguments.add(values[i]);
				}
			}
		}
	}
}

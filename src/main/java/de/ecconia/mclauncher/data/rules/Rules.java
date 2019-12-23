package de.ecconia.mclauncher.data.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import de.ecconia.java.json.JSONArray;
import de.ecconia.java.json.JSONObject;
import de.ecconia.mclauncher.OSTools;

public class Rules
{
	/*
	// -> All but OSX
	"rules": [
		{
			"action": "allow"
		}
		{
			"os": {
				"name": "osx"
			}
			"action": "disallow"
		}
	]
	
	// -> Only OSX
	"rules": [
		{
			"os": {
				"name": "osx"
			}
			"action": "allow"
		}
	]
	
	// -> Only "Windows"
	"rules": [
		{
			"os": {
				"name": "windows"
			}
			"action": "allow"
		}
	]
	
	// -> Only Windows 10
	"rules": [
		{
			"os": {
				"name": "windows"
				"version": "^10\."
			}
			"action": "allow"
		}
	]
	
	// -> Whatever??
	"rules": [
		{
			"os": {
				"arch": "x86"
			}
			"action": "allow"
		}
	]
	
	// -> Feature flag "demo" -> FALSE
	"rules": [
		{
			"features": {
				"is_demo_user": true
			}
			"action": "allow"
		}
	]
	
	// -> Feature flag custom boot resolution -> false
	"rules": [
		{
			"features": {
				"has_custom_resolution": true
			}
			"action": "allow"
		}
	]
	 */
	
	private List<Rule> rules = new ArrayList<>();
	
	public Rules(JSONArray rulesJSON)
	{
		for(Object ruleObject : rulesJSON.getEntries())
		{
			JSONObject ruleJSON = JSONObject.asObject(ruleObject);
			if(ruleJSON.get("os") != null)
			{
				rules.add(new OSRule(ruleJSON.getObject("os"), actionToBool(ruleJSON)));
			}
			else if(ruleJSON.get("features") != null)
			{
				//TODO: Actually let this rule ask for the permissions - somewhere...
				rules.add(new FalseRule());
			}
			else
			{
				if(actionToBool(ruleJSON))
				{
					rules.add(new TrueRule());
				}
				else
				{
					rules.add(new FalseRule());
				}
			}
		}
	}
	
	private static boolean actionToBool(JSONObject object)
	{
		String action = object.getString("action");
		if("allow".equals(action))
		{
			return true;
		}
		else if("disallow".equals(action))
		{
			return false;
		}
		else
		{
			//TODO: Change.
			throw new RuntimeException("Unexpected rule action: " + action);
		}
	}
	
	public static void main(String[] args)
	{
		System.out.println(System.getProperty("os.name"));
		System.out.println(System.getProperty("os.version"));
		System.out.println(System.getProperty("os.arch"));
		/* Ubuntu 18
			Linux
			4.15.0-72-generic
			amd64
		 */
		/* Windows 10
			Windows 10
			10.0
			amd64
		 */
	}
	
	public boolean test()
	{
		boolean action = false;
		
		for(Rule rule : rules)
		{
			Boolean result = rule.test();
			if(result != null)
			{
				action = result;
			}
		}
		
		return action;
	}
	
	private interface Rule
	{
		Boolean test();
	}
	
	private static class TrueRule implements Rule
	{
		@Override
		public Boolean test()
		{
			return true;
		}
	}
	
	private static class FalseRule implements Rule
	{
		@Override
		public Boolean test()
		{
			return false;
		}
	}
	
	private abstract static class WrappedCondition implements Rule
	{
		private final boolean action;
		
		public WrappedCondition(boolean action)
		{
			this.action = action;
		}
		
		protected abstract boolean applies();
		
		@Override
		public Boolean test()
		{
			if(applies())
			{
				return action;
			}
			else
			{
				return null;
			}
		}
	}
	
	private static class OSRule extends WrappedCondition
	{
		private final String name;
		private final String version;
		private final String arch;
		
		public OSRule(JSONObject osJSON, boolean action)
		{
			super(action);
			
			name = osJSON.getStringOrNull("name");
			version = osJSON.getStringOrNull("version");
			arch = osJSON.getStringOrNull("arch");
		}
		
		@Override
		protected boolean applies()
		{
			if(name != null && !OSTools.isOsName(name))
			{
				return false;
			}
			
			if(arch != null)
			{
				String system = System.getProperty("os.arch");
				if(!system.equals(arch))
				{
					return false;
				}
			}
			
			if(version != null)
			{
				String system = System.getProperty("os.version");
				if(!Pattern.compile(version).matcher(system).find())
				{
					return false;
				}
			}
			
			return true;
		}
	}
}

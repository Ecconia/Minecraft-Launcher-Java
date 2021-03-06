package de.ecconia.mclauncher;

import de.ecconia.java.json.JSONObject;
import de.ecconia.java.json.JSONParser;
import de.ecconia.mclauncher.webrequests.Requests;
import de.ecconia.mclauncher.webrequests.Response;
import java.util.Scanner;

public class AccessTokenQuery
{
	private static final String authServerURL = "https://authserver.mojang.com";
	
	public static void queryAccessToken()
	{
		LoginProfile profile = queryLoginProfile();
		
		System.out.println("-> Access Token:");
		System.out.println(profile.getAccessToken());
		System.out.println("-> Username:");
		System.out.println(profile.getUsername());
		System.out.println("-> UUID:");
		System.out.println(profile.getUuid());
	}
	
	public static LoginProfile queryLoginProfile()
	{
		Scanner scanner = new Scanner(System.in);
		System.out.println("Interactive access token acquirement:");
		System.out.println();
		System.out.println("Please type your Mojang-Account-Name (email):");
		String email = scanner.nextLine();
		System.out.println("Please type your Mojang-Account-Password:");
		String password = scanner.nextLine();
		
		return queryAccessToken(email, password);
	}
	
	public static LoginProfile queryAccessToken(String email, String password)
	{
		JSONObject body = new JSONObject();
		
		JSONObject agent = new JSONObject();
		agent.put("name", "Minecraft");
		agent.put("version", 1);
		body.put("agent", agent);
		
		body.put("username", email);
		body.put("password", password);
		
		Response response = Requests.sendJSONRequest(authServerURL + "/authenticate", body.printJSON());
		
		if(response.getResponseCode() == 200)
		{
			JSONObject jsonResponse = (JSONObject) JSONParser.parse(response.getResponse());
			String accessToken = jsonResponse.getString("accessToken");
			JSONObject profile = jsonResponse.getObject("selectedProfile");
			String username = profile.getString("name");
			String uuid = profile.getString("id");
			return new LoginProfile(username, uuid, accessToken);
		}
		else
		{
			System.out.println("Something went wrong acquiring a new AccessToken: " + response.getResponseCode() + " " + response.getResponseMessage());
			System.out.println("Response: \n\n" + response.getResponse());
			return null;
		}
	}
}

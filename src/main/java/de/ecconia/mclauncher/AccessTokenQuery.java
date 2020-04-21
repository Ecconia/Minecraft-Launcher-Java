package de.ecconia.mclauncher;

import de.ecconia.java.json.JSONObject;
import de.ecconia.java.json.JSONParser;
import java.util.Scanner;

public class AccessTokenQuery
{
	private static final String authServerURL = "https://authserver.mojang.com";
	
	public static void queryAccessToken()
	{
		Scanner scanner = new Scanner(System.in);
		System.out.println("Interactive access token acquirement:");
		System.out.println();
		System.out.println("Please type your Mojang-Account-Name (email):");
		String email = scanner.nextLine();
		System.out.println("Please type your Mojang-Account-Password:");
		String password = scanner.nextLine();
		
		queryAccessToken(email, password);
	}
	
	public static void queryAccessToken(String email, String password)
	{
		JSONObject body = new JSONObject();
		
		JSONObject agent = new JSONObject();
		agent.put("name", "Minecraft");
		agent.put("version", 1);
		body.put("agent", agent);
		
		body.put("username", email);
		body.put("password", password);
		
		EasyRequest request = new EasyRequest(authServerURL + "/authenticate", body.printJSON());
		
		if(request.getResponseCode() == 200)
		{
			JSONObject response = (JSONObject) JSONParser.parse(request.getBody());
			String accessToken = response.getString("accessToken");
			JSONObject profile = response.getObject("selectedProfile");
			String username = profile.getString("name");
			String uuid = profile.getString("id");
			System.out.println("-> Access Token:");
			System.out.println(accessToken);
			System.out.println("-> Username:");
			System.out.println(username);
			System.out.println("-> UUID:");
			System.out.println(uuid);
		}
		else
		{
			System.out.println("Something went wrong acquiring a new AccessToken: " + request.getResponseCode() + " " + request.getResponseString());
			System.out.println("Response: \n\n" + request.getResponseString());
		}
	}
}

package de.ecconia.mclauncher;

import java.io.File;

public class Locations
{
	public static final File rootFolder = new File("data");
	public static final File librariesFolder = new File(rootFolder, "libraries");
	public static final File versionsFolder = new File(rootFolder, "versions");
	public static final File assetsFolder = new File(rootFolder, "assets");
	public static final File assetsObjectsFolder = new File(assetsFolder, "objects"); //May not be renamed.
	public static final File assetsIndexesFolder = new File(assetsFolder, "indexes"); //May not be renamed, although "indices" is a better form.
	
	public static final File gameFolder = new File(rootFolder, "game-data"); //Test to move away the game files. More structure. And research.
	public static final File runFolder = new File(rootFolder, "game-run"); //Test to move away the game files. More structure. And research.
}

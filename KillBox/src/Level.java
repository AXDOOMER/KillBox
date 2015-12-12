//Copyright (C) 2014-2015 Alexandre-Xavier Labonté-Lamoureux
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program.  If not, see <http://www.gnu.org/licenses/>.

import java.io.*;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;  // GL_NEAREST

public class Level
{
	String Name = "Unknown";
	String Sky;
	String Fog = "black";
	int Visibility = 0;
	int Filter = GL_NEAREST;

	ArrayList<Plane> Planes = new ArrayList<Plane>();
	ArrayList<Thing> Things = new ArrayList<Thing>();
	ArrayList<Player> Players = new ArrayList<Player>();

	// Small list that keeps references on spawns only
	ArrayList<Thing> Spawns = new ArrayList<Thing>();

	// Keep an ordered list of textures that have been loaded previously for the level
	private static ArrayList<Texture> Textures = new ArrayList<Texture>();

	public void LoadLevel(String LvlName, int PlaneFilter)
	{
		Filter = PlaneFilter;

		try
		{
			// Load from file
			BufferedReader LevelFile = new BufferedReader(new FileReader(LvlName));
			String Line;

			try
			{
				while ((Line = LevelFile.readLine()) != null)
				{
					if (Line.length() < 2 || Line.charAt(0) == '#')
					{
						// It's a comment. Get another line.
						continue;
					}

					//Serious business
					int Position = Line.length();
					char[] Find = {':', ';', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
					// Find first of
					for (int i = 0; i < 12; i++)
					{
						int Temp = Line.indexOf(Find[i]);

						if (Temp < Position && Temp != -1)
						{
							Position = Temp;
						}
					}

					String Content = Line.substring(0, Position);
					String id;
					int Number;

					if (Line.contains("level:"))
					{
						while (!Line.contains("}"))
						{
							Line = LevelFile.readLine();

							if (Line.contains("{"))
							{
								continue;
							}
							else if (Line.contains("name: "))
							{
								Name = Line.substring(Line.indexOf("name: ") + 6, Line.indexOf(";"));
							}
							else if (Line.contains("fog: "))
							{
								Fog = Line.substring(Line.indexOf("fog: ") + 5, Line.indexOf(";"));
							}
							else if (Line.contains("sky: "))
							{
								Sky = Line.substring(Line.indexOf("sky: ") + 5, Line.indexOf(";"));
							}
						}
					}
					else if (Line.contains("wall") || Line.contains("floor") || Line.contains("ceiling") || Line.contains("plane") || Line.contains("slope"))
					{
						Planes.add(new Plane());
						boolean NameIsSet = false;

						while (!Line.contains("}"))
						{
							Line = LevelFile.readLine();

							if (Line.contains("{"))
							{
								continue;
							}
							else if (Line.contains("x: "))
							{
								Planes.get(Planes.size() - 1).AddVertex(Integer.parseInt(Line.substring(Line.indexOf("x: ") + 3, Line.indexOf(";"))));
							}
							else if (Line.contains("y: "))
							{
								Planes.get(Planes.size() - 1).AddVertex(Integer.parseInt(Line.substring(Line.indexOf("y: ") + 3, Line.indexOf(";"))));
							}
							else if (Line.contains("z: "))
							{
								Planes.get(Planes.size() - 1).AddVertex(Integer.parseInt(Line.substring(Line.indexOf("z: ") + 3, Line.indexOf(";"))));
							}
							else if (Line.contains("2sided: "))
							{
								if ((Line.substring(Line.indexOf("2sided: ") + 8, Line.indexOf(";"))).equals("false"))
								{
									Planes.get(Planes.size() - 1).TwoSided(false);
								}
							}
							else if (Line.contains("impassable: "))
							{
								if ((Line.substring(Line.indexOf("impassable: ") + 12, Line.indexOf(";"))).equals("false"))
								{
									Planes.get(Planes.size() - 1).Impassable(false);
								}
							}
							else if (Line.contains("texture: "))
							{
								Planes.get(Planes.size() - 1).SetTextureName(Line.substring(Line.indexOf("texture: ") + 9, Line.indexOf(";")));
								// The following line loads the textures if it needs to and sets a reference to it inside the plane.
								Planes.get(Planes.size() - 1).SetReference(LoadTexture("textures/" + Planes.get(Planes.size() - 1).TextureName));
								NameIsSet = true;
							}
							else if (Line.contains("light: "))
							{
								Planes.get(Planes.size() - 1).Lightning(Integer.parseInt(Line.substring(Line.indexOf("light: ") + 9, Line.indexOf(";"))));
							}
						}

						if (!NameIsSet)
						{
							Planes.get(Planes.size() - 1).SetReference(LoadTexture("textures/DOOR9_1.bmp"));
						}
					}
					else if (Line.contains("spawn"))
					{
						int PosX = 0;
						int PosY = 0;
						int PosZ = 0;
						short Angle = 0;

						while (!Line.contains("}"))
						{
							Line = LevelFile.readLine();

							if (Line.contains("{"))
							{
								continue;
							}
							else if (Line.contains("x: "))
							{
								PosX = Integer.parseInt(Line.substring(Line.indexOf("x: ") + 3, Line.indexOf(";")));
							}
							else if (Line.contains("y: "))
							{
								PosY = Integer.parseInt(Line.substring(Line.indexOf("y: ") + 3, Line.indexOf(";")));
							}
							else if (Line.contains("z: "))
							{
								PosZ = Integer.parseInt(Line.substring(Line.indexOf("z: ") + 3, Line.indexOf(";")));
							}
							else if (Line.contains("angle: "))
							{
								Angle = Short.parseShort(Line.substring(Line.indexOf("angle: ") + 7, Line.indexOf(";")));
							}
							else if (Line.contains("}"))
							{
								break;
							}
							else
							{
								System.out.println("Invalid property: " + Line);
							}
						}

						Things.add(new Thing("Spawn", PosX, PosY, PosZ, Angle));
						Spawns.add(new Thing("Spawn", PosX, PosY, PosZ, Angle));
					}
					else if (Line.contains("thing"))
					{
						int PosX = 0;
						int PosY = 0;
						int PosZ = 0;
						short Angle = 0;	// Doesn't matter because the thing is a sprite
						String Type = "";
						boolean Impassable = true;

						while (!Line.contains("}"))
						{
							Line = LevelFile.readLine();

							if (Line.contains("{"))
							{
								continue;
							}
							else if (Line.contains("x: "))
							{
								PosX = Integer.parseInt(Line.substring(Line.indexOf("x: ") + 3, Line.indexOf(";")));
							}
							else if (Line.contains("y: "))
							{
								PosY = Integer.parseInt(Line.substring(Line.indexOf("y: ") + 3, Line.indexOf(";")));
							}
							else if (Line.contains("z: "))
							{
								PosZ = Integer.parseInt(Line.substring(Line.indexOf("z: ") + 3, Line.indexOf(";")));
							}
							else if (Line.contains("angle: "))
							{
								Angle = Short.parseShort(Line.substring(Line.indexOf("angle: ") + 7, Line.indexOf(";")));
							}
							else if (Line.contains("type: "))
							{
								Type = Line.substring(Line.indexOf("type: ") + 6, Line.indexOf(";"));
							}
							else if (Line.contains("impassable: "))
							{
								Impassable = Boolean.parseBoolean(Line.substring(Line.indexOf("impassable: ") + 12, Line.indexOf(";")));
							}
							else if (Line.contains("}"))
							{
								break;
							}
							else
							{
								System.out.println("Invalid property: " + Line);
							}
						}

						Things.add(new Thing(Type, PosX, PosY, PosZ));
					}
					else if (Line.contains("palmtree") || Line.contains("smallpalmtree"))
					{
						String Sprite = "";
						int PosX = 0;
						int PosY = 0;
						int PosZ = 0;
						int Light = 0;
						int Radius = 16;
						int Height = 96;
						int Health = 100;
						boolean Impassable = true;

						while (!Line.contains("}"))
						{
							Line = LevelFile.readLine();

							if (Line.contains("{"))
							{
								continue;
							}
							else if (Line.contains("texture: "))
							{
								Sprite = Line.substring(Line.indexOf("texture: ") + 9, Line.indexOf(";"));
							}
							else if (Line.contains("sprite: "))
							{
								Sprite = Line.substring(Line.indexOf("sprite: ") + 8, Line.indexOf(";"));
							}
							else if (Line.contains("x: "))
							{
								PosX = Integer.parseInt(Line.substring(Line.indexOf("x: ") + 3, Line.indexOf(";")));
							}
							else if (Line.contains("y: "))
							{
								PosY = Integer.parseInt(Line.substring(Line.indexOf("y: ") + 3, Line.indexOf(";")));
							}
							else if (Line.contains("z: "))
							{
								PosZ = Integer.parseInt(Line.substring(Line.indexOf("z: ") + 3, Line.indexOf(";")));
							}
							else if (Line.contains("light: "))
							{
								Light = Integer.parseInt(Line.substring(Line.indexOf("light: ") + 7, Line.indexOf(";")));
							}
							else if (Line.contains("radius: "))
							{
								Radius = Integer.parseInt(Line.substring(Line.indexOf("radius: ") + 8, Line.indexOf(";")));
							}
							else if (Line.contains("height: "))
							{
								Height = Integer.parseInt(Line.substring(Line.indexOf("height: ") + 8, Line.indexOf(";")));
							}
							else if (Line.contains("health: "))
							{
								Health = Integer.parseInt(Line.substring(Line.indexOf("health: ") + 8, Line.indexOf(";")));
							}
							else if (Line.contains("impassable: "))
							{
								Impassable = Boolean.parseBoolean(Line.substring(Line.indexOf("impassable: ") + 12, Line.indexOf(";")));
							}
							else if (Line.contains("}"))
							{
								break;
							}
							else
							{
								System.out.println("Invalid property: " + Line);
							}
						}

						Things.add(new Thing("res/sprites/" + Sprite, PosX, PosY, PosZ, Light, Radius, Height, Health));
					}
					else
					{
						System.out.println("Unknown data: " + Line);
					}
				}
			}
			catch (IOException ioe)
			{
				System.err.println(ioe.getStackTrace());
			}

		}
		catch (FileNotFoundException fnfe)
		{
			System.err.println("The specified level cannot be found: " + LvlName);
			System.exit(1);
		}
		catch (Exception e)
		{
			System.err.println("Failed to load the following level: " + LvlName);
			System.err.println("The must be a syntax error in the file (missing closing accolade, colon, semicolon, etc.)");
			System.exit(1);
		}
	}

	// This method will load a texture. It will find it using the specified path.
	// It checks if the texture has already been loaded and the name is case-sensitive.
	// If a texture is not already loaded, it will create a new texture and add it to the list.
	// Else, it will return the reference to the texture that has already been loaded.
	// If the texture can't be loaded, it will return 'NULL'.
	private Texture LoadTexture(String Path)
	{
		// Add texture to the list if it's not already loaded.
		// Search through the texture list. They are in the alphabetical order of the file names.
		int Search = 0;
		String Name = Path.substring(Path.lastIndexOf('/') + 1);
		Texture NewTexture = null;

		while (Search >= 0 && Search < Textures.size())
		{
			// Check if the texture name at this position is the same as the new texture
			boolean Same = Textures.get(Search).Name().equals(Name);

			if (!Same)
			{
				// Increase the search index because this is not the good texture.
				Search++;
			}
			else
			{
				// Take the reference to the texture that's already there.
				NewTexture = Textures.get(Search);
				Search = -1;
			}
		}

		if (Search > 0)
		{
			// Add it there. Since the index of 'Search' is an element after the required position, one is subtracted.
			NewTexture = new Texture("res/" + Path, Filter);
			Textures.add(Search - 1, NewTexture);
		}
		else if (Search == 0)
		{
			// It's the only element, so it must be the first element.
			NewTexture = new Texture("res/" + Path, Filter);
			Textures.add(NewTexture);
		}

		return NewTexture;
	}

	// Multi-thing cooperative system
	public void UpdateLevel(/*ArrayList<Plane> Planes, ArrayList<Thing> Things*/)
	{
		// Nice article:  http://blog.noctua-software.com/entity-references.html
		for (int Thing = 0; Thing < Things.size(); Thing++)
		{
			// Give a change to every thing to update itself
			if (!Things.get(Thing).Update(/*Planes, Things*/))
			{
				Things.remove(Thing);
			}
		}
	}

	public void RemoveTypeOfThingsFromLevel(Thing.Names Type)
	{
		for (int Thing = 0; Thing < Things.size(); Thing++)
		{
			if (Things.get(Thing).Type == Type)
			{
				Things.remove(Thing);
			}
		}
	}
	
	// Get the players list
	public ArrayList<Player> Players()
	{
		return Players;
	}
}

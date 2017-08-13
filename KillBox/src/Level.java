// Copyright (C) 2014-2017 Alexandre-Xavier Labonté-Lamoureux
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import java.io.*;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;  // GL_NEAREST

public class Level
{
	int Filter;
	public float ShortestWall;

	// Map geometry and its stuff that's found in it
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
		ShortestWall = Integer.MAX_VALUE;
		String Line = null;
		long loadingStart = System.currentTimeMillis();

		try
		{
			// Load from file
			BufferedReader LevelFile = new BufferedReader(new FileReader(LvlName));

			while ((Line = LevelFile.readLine()) != null)
			{
				if (Line.length() < 2 || Line.charAt(0) == '#')
				{
					// It's an empty line or it's a comment. Get another line.
					continue;
				}

				if (Line.contains("wall") || Line.contains("floor") || Line.contains("ceiling") || Line.contains("plane") || Line.contains("slope"))
				{
					Planes.add(new Plane());

					while (!Line.contains("}"))
					{
						Line = LevelFile.readLine();

						if (Line.contains("{"))
						{
							continue;
						}
						else if (Line.contains("x: "))
						{
							int PosX = Integer.parseInt(Line.substring(Line.indexOf("x: ") + 3, Line.indexOf(";")));
							Planes.get(Planes.size() - 1).AddVertex(PosX);
						}
						else if (Line.contains("y: "))
						{
							int PosY = Integer.parseInt(Line.substring(Line.indexOf("y: ") + 3, Line.indexOf(";")));
							Planes.get(Planes.size() - 1).AddVertex(PosY);
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
						}
						else if (Line.contains("light: "))
						{
							Planes.get(Planes.size() - 1).Lightning(Integer.parseInt(Line.substring(Line.indexOf("light: ") + 7, Line.indexOf(";"))));
						}
					}

					// Calculate the coordinates for a vector
					Planes.get(Planes.size() - 1).ComputeCoordinates();

					// Calculate the plane's flat size
					Planes.get(Planes.size() - 1).FlatLength = Planes.get(Planes.size() - 1).CalculateMaxFlatLength();
					if (Planes.get(Planes.size() - 1).FlatLength < ShortestWall && Planes.get(Planes.size() - 1).FlatLength != 0)
					{
						ShortestWall = Planes.get(Planes.size() - 1).FlatLength;
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

					Spawns.add(new Thing("Spawn", PosX, PosY, PosZ, Angle));
				}
				else if (Line.contains("thing"))
				{
					int PosX = 0;
					int PosY = 0;
					int PosZ = 0;
					int Light = 127;
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
						else if (Line.contains("light: "))
						{
							Light = Integer.parseInt(Line.substring(Line.indexOf("light: ") + 7, Line.indexOf(";")));
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
				else if (Line.contains("palmtree") || Line.contains("smallpalmtree"))	//TODO: This should be made a regular thing
				{
					String Sprite = "";
					int PosX = 0;
					int PosY = 0;
					int PosZ = 0;
					int Light = 127;
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
							System.err.println("Invalid property: " + Line);
						}
					}

					Things.add(new Thing("res/sprites/" + Sprite, PosX, PosY, PosZ, Light, Radius, Height, Health));
				}
				else
				{
					System.err.println("Unknown data: " + Line);
				}
			}
		}
		catch (FileNotFoundException fnfe)
		{
			System.err.println("The specified level cannot be found: " + LvlName);
			System.exit(1);
		}
		catch (IOException ioe)
		{
			System.err.println(ioe.getStackTrace());
			System.exit(1);
		}
		catch (Exception e)
		{
			System.err.println("Failed to load the following level: " + LvlName);
			System.err.println("There must be a syntax error in the file (missing closing brace, colon, semicolon, etc.)");
			System.err.println("The error occurred on the following line: \"" + Line + "\"");
			System.exit(1);
		}

		System.out.println("Level of " + (Planes.size() + Things.size() + Spawns.size()) + " elements loaded in " + (System.currentTimeMillis() - loadingStart) + "ms");
	}

	// This method will load a texture from the image specified in the path.
	// The name is case-sensitive and duplicates are no loaded again.
	private Texture LoadTexture(String Path)
	{
		int SearchIndex = 0;
		String Name = Path.substring(Path.lastIndexOf('/') + 1);
		Texture NewTexture = null;

		// Search through the texture list. Add texture to the list if it's not already loaded.
		while (SearchIndex >= 0 && SearchIndex < Textures.size())
		{
			// Check if the texture name at this position is the same as the new texture.
			boolean Same = Textures.get(SearchIndex).Name().equals(Name);

			if (!Same)
			{
				// Increase the search index because this is not the good texture.
				SearchIndex++;
			}
			else
			{
				// Take the reference to the texture that's already there.
				NewTexture = Textures.get(SearchIndex);
				SearchIndex = -1;
			}
		}

		//System.out.println("Texture " + Name + " is " + NewTexture);	// Texture loading for debug

		if (SearchIndex > 0)
		{
			// Add it there. Since the index of 'Search' is an element after the required position, one is subtracted.
			NewTexture = new Texture("res/" + Path, Filter);
			Textures.add(SearchIndex - 1, NewTexture);
		}
		else if (SearchIndex == 0)
		{
			// It's the only element, so it must be the first element.
			NewTexture = new Texture("res/" + Path, Filter);
			Textures.add(NewTexture);
		}

		return NewTexture;
	}

	// Update the things!
	public void UpdateLevel()
	{
		// Nice article:  http://blog.noctua-software.com/entity-references.html
		for (int Thing = 0; Thing < Things.size(); Thing++)
		{
			// Give a change to every thing to update itself
			if (!Things.get(Thing).Update())
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

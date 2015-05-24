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
    String Name_ = "Unknown";
    String Sky_;
    String Fog_ = "black";
    int Visibility = 0;

    ArrayList<Plane> Planes = new ArrayList<Plane>();
    ArrayList<Thing> Things = new ArrayList<Thing>();
    ArrayList<Player> Players = new ArrayList<Player>();

    // Keep an ordered list of textures that have been loaded previously for the level
    private static ArrayList<Texture> Textures = new ArrayList<Texture>();

    public Level(String LvlName)
    {

    }

    public Level()
    {

    }

    public void LoadLevel(String LvlName)
    {
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

                            if (Line.indexOf("{") != -1)
                            {
                                continue;
                            }
                            else if (Line.indexOf("name: ") != -1)
                            {
                                Name_ = Line.substring(Line.indexOf("name: ") + 6, Line.indexOf(";"));
                            }
                            else if (Line.indexOf("fog: ") != -1)
                            {
                                Fog_ = Line.substring(Line.indexOf("fog: ") + 5, Line.indexOf(";"));
                            }
                            else if (Line.indexOf("sky: ") != -1)
                            {
                                Sky_ = Line.substring(Line.indexOf("sky: ") + 5, Line.indexOf(";"));
                            }
                        }
                    }
                    else if (Line.contains("wall") || Line.contains("plane") || Line.contains("floor") || Line.contains("slope"))
                    {
                        Planes.add(new Plane());
                        boolean NameIsSet = false;

                        while (!Line.contains("}"))
                        {
                            Line = LevelFile.readLine();

                            if (Line.indexOf("{") != -1)
                            {
                                continue;
                            }
                            else if (Line.indexOf("2sided: ") != -1)
                            {
                                if ((Line.substring(Line.indexOf("2sided: ") + 8, Line.indexOf(";"))).equals("false"))
                                {
                                    Planes.get(Planes.size() - 1).TwoSided(false);
                                }
                            }
                            else if (Line.indexOf("impassable: ") != -1)
                            {
                                if ((Line.substring(Line.indexOf("impassable: ") + 12, Line.indexOf(";"))).equals("false"))
                                {
                                    Planes.get(Planes.size() - 1).Impassable(false);
                                }
                            }
                            else if (Line.indexOf("x: ") != -1)
                            {
                                Planes.get(Planes.size() - 1).AddVertex(Integer.parseInt(Line.substring(Line.indexOf("x: ") + 3, Line.indexOf(";"))));
                            }
                            else if (Line.indexOf("y: ") != -1)
                            {
                                Planes.get(Planes.size() - 1).AddVertex(Integer.parseInt(Line.substring(Line.indexOf("y: ") + 3, Line.indexOf(";"))));
                            }
                            else if (Line.indexOf("z: ") != -1)
                            {
                                Planes.get(Planes.size() - 1).AddVertex(Integer.parseInt(Line.substring(Line.indexOf("z: ") + 3, Line.indexOf(";"))));
                            }
                            else if (Line.indexOf("texture: ") != -1)
                            {
                                Planes.get(Planes.size() - 1).SetTextureName(Line.substring(Line.indexOf("texture: ") + 9, Line.indexOf(";")));
                                // The following line loads the textures if it needs to and sets a reference to it inside the plane.
                                Planes.get(Planes.size() - 1).SetReference(LoadTexture(Planes.get(Planes.size() - 1).TextureName));
                                NameIsSet = true;
                            }
                            else if (Line.indexOf("texture: ") != -1)
                            {
                                Planes.get(Planes.size() - 1).Lightning(Integer.parseInt(Line.substring(Line.indexOf("light: ") + 9, Line.indexOf(";"))));
                            }
                        }

                        if (!NameIsSet)
                        {
                            Planes.get(Planes.size() - 1).SetReference(LoadTexture("DOOR9_1.bmp"));
                        }
                    }
                    else
                    {
                        System.out.println("Unknown data: " + Line);
                    }
                }
            }
            catch (IOException ioe)
            {
                System.err.println(ioe.getMessage());
            }

        }
        catch (FileNotFoundException fnfe)
        {
            System.err.println("The specified level cannot be found: " + LvlName);
        }
    }

    // This method will load a texture. It will find it using the specified path.
    // It checks if the texture has already been loaded and the name is case-sensitive.
    // If a texture is not already loaded, it will create a new texture and add it to the list.
    // Else, it will return the reference to the texture that has already been loaded.
    private Texture LoadTexture(String Path)
    {
        // Add texture to the list if it's not already loaded.
        // Search through the texture list. They are in the alphanetical order of the file names.
        int Search = 0;
        String Name = Path.substring(Path.lastIndexOf('/') + 1);
        Texture NewTexture = null;

        while (Search >= 0 && Search < Textures.size())
        {
            // Result is -1 if smaller than, is 0 is equals and is 1 if is bigger than the compared string.
            int Result = Textures.get(Search).Name().compareTo(Name);

            if (Result < 0)
            {
                // Put it farther, so increase search index.
                Search++;
            }
            else if (Result == 0)
            {
                // It's already there.
                NewTexture = Textures.get(Search);
                Search = -1;
            }
            else
            {
                // This means we are farther than the texture that was looked for.
                // Break because the right place was found.
                break;
            }
        }

        if (Search == 0)
        {
            // It's the only element, so it must be the first element.
            NewTexture = new Texture("Stuff/" + Path, GL_NEAREST);
            Textures.add(NewTexture);
        }
        else if (Search > 0)
        {
            // Add it there. Since the index of 'Search' is an element after the required position, one is substracted.
            NewTexture = new Texture("Stuff/" + Path, GL_NEAREST);
            Textures.add(Search - 1, NewTexture);
        }

        return NewTexture;
    }

    /*public void BindTexturesToPlanes()
    {
        Texture CurrentTexture = new Texture("Stuff/" + Planes.get(Planes.size() - 1).TextureName, GL_NEAREST);

        for (int Index = 0; Index < Planes.size(); Index++)
        {
            Planes.get(Index).Reference = GetTextureFromName(Planes.get(Index).TextureName);
        }
    }*/

    /*private void AddTextureToList(Texture TextureRef)
    {
        // Add texture to the list
        // Search through the texture list. They are in the alphanetical order of the file names.
        int Search = 0;

        while (Search < Textures.size())
        {
            // Result is -1 if smaller than, is 0 is equals and is 1 if is bigger than the compared string.
            int Result = Textures.get(Search).Name.compareToIgnoreCase(Path.substring(Path.lastIndexOf('/') + 1));

            if (Result < 0)
            {
                // Put it farther, so increase search index.
                Search++;
            }
            else if (Result == 0)
            {
                // It's already there.
                Search = -1;
            }
            else
            {
                // This means we are farther than the texture that was looked for.
                // Break because the right place was found.
                break;
            }
        }

        if (Search == 0)
        {
            // It's the only element, so it must be the first element.
            Textures.add(this);
        }
        else if (Search > 0)
        {
            // Add it there. Since the index of 'Search' is an element after the required position, one is substracted.
            Textures.add(Search - 1, this);
        }
    }*/

    /*public int GetTextureIdByName(String Name)
    {
        for (int Texture = 0; Texture < Textures.size(); Texture++)
        {
            if (Textures.get(Texture).equals(Name))
            {
                return Textures.get(Texture).Id;
            }
        }

        return -1;
    }*/

    /*public Texture GetTextureFromName(String Name)
    {
        for (int Texture = 0; Texture < Textures.size(); Texture++)
        {
            if (Textures.get(Texture).equals(Name))
            {
                return Textures.get(Texture);
            }
        }

        return null;
    }*/

    /*public boolean CheckTextureExists()
    {
        // Search if texture already exists
        int AlreadyExists = 0;
        String FileNameFromPath = Path.substring(Path.lastIndexOf('/') + 1);
        for (int Search = 0; AlreadyExists == 0 && Search < Textures.size(); Search++)
        {
            if (Textures.get(Search).equals(FileNameFromPath))
            {
                AlreadyExists = Search;
            }
        }
    }*/

    // Multi-thing cooperative system
    public void UpdateLevel(ArrayList<Plane> Planes, ArrayList<Thing> Things)
    {
        // Nice article:  http://blog.noctua-software.com/entity-references.html
        for (int i = 0; i < Things.size(); i++)
        {
			// Give a change to every thing to update itself
			Things.get(i).Update(Planes, Things);
        }
    }
}

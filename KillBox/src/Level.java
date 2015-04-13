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

public class Level
{
    String Name_ = "Unknown";
    String Sky_;
    String Fog_ = "black";
    int Visibility = 0;

    ArrayList<Plane> Planes = new ArrayList<>();

    public Level(String LvlName)
    {
        try
        {
            // Load from file
            BufferedReader LevelFile = new BufferedReader(new FileReader(LvlName));
            String Line;

            try
            {
                while((Line = LevelFile.readLine()) != null)
                {
                    if (Line.length() < 2 || Line.charAt(0) == '#')
                    {
                        // It's a comment. Get another line.
                        continue;
                    }

                    //Serious business
                    int Position = Line.length();
                    char[] Find = {':', ';', '0','1', '2', '3', '4', '5', '6', '7', '8', '9'};
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

                            if (Line.indexOf("{") != -1 )
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

                        while (!Line.contains("}"))
                        {
                            Line = LevelFile.readLine();

                            if (Line.indexOf("{") != -1 )
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
                            else if (Line.indexOf("x: ") != -1)
                            {
                                Planes.get(Planes.size() - 1).AddVertex(Float.parseFloat(Line.substring(Line.indexOf("x: ") + 3, Line.indexOf(";"))));
                            }
                            else if (Line.indexOf("y: ") != -1)
                            {
                                Planes.get(Planes.size() - 1).AddVertex(Float.parseFloat(Line.substring(Line.indexOf("y: ") + 3, Line.indexOf(";"))));
                            }
                            else if (Line.indexOf("z: ") != -1)
                            {
                                Planes.get(Planes.size() - 1).AddVertex(Float.parseFloat(Line.substring(Line.indexOf("z: ") + 3, Line.indexOf(";"))));
                            }
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
}

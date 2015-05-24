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

import java.util.*;

public class Plane
{
	ArrayList<Float> Vertices = new ArrayList<Float>();

	public String TextureName = "";
	Texture Reference;
	boolean TwoSided = true;   // The texture is only drawn on the front side of the plane.
	boolean Impassable = true;  // Player can't walk through the plane. They are clipped.
	byte Light = 0;             // Amount of light on the texture

	// Colorization of the texture.
	byte Red = 0;
	byte Green = 0;
	byte Blue = 0;

	public void SetReference(Texture Reference)
	{
		this.Reference = Reference;
	}

	public void Bind()
	{
		if (Reference != null)
		{
			Reference.Bind();
		}
		else
		{
			System.out.println("ERROR: Plane has texture name '" + TextureName + "', but the reference is '" + Reference + "'.");
		}
	}

	public String Name()
	{
		return TextureName;
	}

    /*public void SetReferenceByName(String TextureName)
	{
        Texture Textures = new Texture();

        for (int i = 0; i < Textures.GetTextureList().size(); i++)
        {
            if (Textures.GetTextureList().get(i).Name().equals(TextureName))
            {
                this.Reference = Textures.GetTextureList().get(i);
            }
        }
    }*/

	public void SetTextureName(String TextureName)
	{
		this.TextureName = TextureName;
	}

	public void Lightning(int Light)
	{
		if (Light > 255)
		{
			Light = 255;
		}
		if (Light < 0)
		{
			Light = 0;
		}

		this.Light = (byte) (Light - 128);
	}

	public void Colorize(int Red, int Green, int Blue)
	{
		// Set color values to the texture
		// These RGB values must be between 0 and 255.
		this.Red = (byte) (Red - 128);
		this.Green = (byte) (Green - 128);
		this.Blue = (byte) (Blue - 128);
	}

	public void AddVertex(float Vertex)
	{
		Vertices.add(Vertex);
	}

	public void Flush()
	{
		Vertices.clear();
	}

	public boolean TwoSided()
	{
		return this.TwoSided;
	}

	public void TwoSided(boolean IsTwoSided)
	{
		TwoSided = IsTwoSided;
	}

	public boolean Impassable()
	{
		return this.Impassable;
	}

	public void Impassable(boolean IsImpassable)
	{
		Impassable = IsImpassable;
	}
}

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

import java.util.*;

public class Plane
{
	ArrayList<Float> Vertices = new ArrayList<Float>();

	// Coordinates for walls that are square and straight up vertical
	float[] Coordinates = new float[4];

	public String TextureName = "";
	Texture Reference;				// TODO: Implement a comparable so planes can be sort in order of texture id
	boolean TwoSided = true;		// The texture is only drawn on the front side of the plane.
	boolean Impassable = true;		// Player can't walk through the plane. They are clipped.
	byte Light = 127;				// Amount of light on the texture

	// Colorization of the texture.
	byte Red = 0;
	byte Green = 0;
	byte Blue = 0;

	// 2-Dimensional length
	float FlatLength = 0;

	// Check for bullet collision? For horizontal floors and ceilings, this should always be false.
	boolean BlocksBullets = true;	// TODO: Add this setting to the level loader
	byte MayBlockPlayers = -1;		// -1 is not set, 0 is false, 1 is true.

	public void SetReference(Texture Reference)
	{
		this.Reference = Reference;
	}

	// Get vertices
	public ArrayList<Float> Vertices()
	{
		return Vertices;
	}

	// Get vectors (edges of the polygon that can be used for 2D collsion detection)
	public float[][] Vectors()
	{
		int vectors = 0;

		// TODO: Implementation

		// Return x1, y1, x2, y2 for a number of vectors
		return new float[vectors][4];
	}

	public int NumberOfVertices()
	{
		if (Vertices.size() % 3 != 0)
		{
			System.err.println("A plane's number of properties defined for a coordinated of its vertices is not a multiple of three. (" + Reference + ")");
		}

		return Vertices.size() / 3;
	}

	public int GetTextureId()
	{
		return Reference.Id();
	}

	public void Bind()
	{
		Reference.Bind();
	}

	public String Name()
	{
		return TextureName;
	}

	public void SetTextureName(String Name)
	{
		TextureName = Name;
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

	// Get the horizontal angle of the polygon
	public float GetAngle()
	{
		// Takes in account that the polygon is completly plane
		if (Vertices.size() == 12 || Vertices.size() == 9)
		{
			// Check vertices if they are right above each other
			if (Vertices.get(0).floatValue() != Vertices.get(3).floatValue() && Vertices.get(1).floatValue() != Vertices.get(4).floatValue())
			{
				float DiffX = Vertices.get(0) - Vertices.get(3);	// X1 - X2
				float DiffY = Vertices.get(1) - Vertices.get(4);	// Y1 - Y2

				return (float)Math.atan2(DiffY, DiffX);
			}
			else if (Vertices.get(3).floatValue() != Vertices.get(6).floatValue() && Vertices.get(4).floatValue() != Vertices.get(7).floatValue())
			{
				float DiffX = Vertices.get(3) - Vertices.get(6);	// X1 - X2
				float DiffY = Vertices.get(4) - Vertices.get(7);	// Y1 - Y2

				return (float)Math.atan2(DiffY, DiffX);
			}
			else if (Vertices.size() == 12)
			{
				float DiffX = Vertices.get(6) - Vertices.get(9);	// X1 - X2
				float DiffY = Vertices.get(7) - Vertices.get(10);	// Y1 - Y2

				return (float)Math.atan2(DiffY, DiffX);
			}
		}

		// Can't calculate the wall's orientation
		return Float.NaN;
	}

	public void ComputeCoordinates()
	{
		if (Vertices.size() >= 9)
		{
			// Find how the plane is placed in the environment
			float StartX = Vertices().get(0);
			float StartY = Vertices().get(1);

			float EndX = Vertices().get(3);
			float EndY = Vertices().get(4);

			// Get another point if both are above each other.
			if (StartX == EndX && StartY == EndY)
			{
				EndX = Vertices().get(6);
				EndY = Vertices().get(7);
			}

			Coordinates[0] = StartX;
			Coordinates[1] = StartY;
			Coordinates[2] = EndX;
			Coordinates[3] = EndY;
		}
		else
		{
			System.err.println("Error in Plane::ComputerCoordinates()");
			System.exit(1);
		}
	}

	// Max distance from one vertex to another (X and Y)
	public float CalculateMaxFlatLength()
	{
		float MaxFlatLengthFound = 0;
		int NumberOfTest = Vertices.size() / 3;

		for (int Point = 0; Point < NumberOfTest; Point++)
		{
			for (int PointToTestAgainst = 0; PointToTestAgainst < NumberOfTest; PointToTestAgainst++)
			{
				float Distance = (float)Math.sqrt(Math.pow(Vertices.get(Point * 3) - Vertices.get(PointToTestAgainst * 3), 2) + Math.pow(Vertices.get(Point * 3 + 1) - Vertices.get(PointToTestAgainst * 3 + 1), 2));

				if (Distance > MaxFlatLengthFound)
				{
					MaxFlatLengthFound = Distance;
				}
			}
		}

		return MaxFlatLengthFound;
	}

	public boolean CanBlock()
	{
		// Check if this plane may block players
		if (MayBlockPlayers == 0)
		{
			return false;
		}
		else if (MayBlockPlayers == 1)
		{
			return true;
		}
		else
		{
			// Assume it can't block at first
			MayBlockPlayers = 0;

			// Check if this plane may block players
			float Highest = Vertices().get(2);
			float Lowest = Vertices().get(2);
			for (int Point = 5; Point < Vertices().size(); Point += 3)
			{
				if (Vertices().get(Point) < Lowest)
				{
					Lowest = Vertices().get(Point);
				}
				else if (Vertices().get(Point) > Highest)
				{
					Highest = Vertices().get(Point);
				}
			}

			// Check if the player would be able to touch the plane
			if (Lowest >= 56 && Highest >= 56)	// TODO: Remove magic numbers
			{
				// The wall is far above the player
				return CanBlock();
			}
			else if (Lowest <= 0 && Highest <= 0)
			{
				// The wall is below the player
				return CanBlock();
			}
			else
			{
				MayBlockPlayers = 1;
				return CanBlock();
			}
		}
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
		return TwoSided;
	}

	public void TwoSided(boolean IsTwoSided)
	{
		TwoSided = IsTwoSided;
	}

	public boolean Impassable()
	{
		return Impassable;
	}

	public void Impassable(boolean IsImpassable)
	{
		Impassable = IsImpassable;
	}
}

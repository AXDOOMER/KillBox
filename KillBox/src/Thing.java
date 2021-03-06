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

import static org.lwjgl.opengl.GL11.*;
import java.util.ArrayList;

public class Thing
{
	float PosX;		// Horizontal position
	float PosY;		// Vertical position
	float PosZ;		// Height, do not mix with Y.
	float MoX = 0;		// Like DirX
	float MoY = 0;		// Like DirY
	float MoZ = 0;		// Like DirZ
	byte Light = 127;
	int Height = 96;
	int Health = 0;
	int Radius = 16;
	short Angle = 0;
	String Sound;		// A thing can make a sound
	Texture Sprite;		// What does it looks like?
	int Frame = 0;		// Object state
	boolean Impassable = false;
	boolean CanBePickedUp = false;
	String ResPath = "res/sprites/";
	static int TextureFilter = GL_NEAREST;	// Selected texture filter

	public enum Names
	{
		Pistol, Ak47, Tek9, Spawn,
		Blood, DeadPlayer, Flag, PalmTree, SmallPalmTree, Unknown, Custom
	}

	Names Type;

	public Thing(String Sprite, float PosX, float PosY, float PosZ, int Light, int Radius, int Height, int Health)
	{
		Type = Names.Custom;
		this.Sprite = new Texture(Sprite, TextureFilter);

		Impassable = true;
		CanBePickedUp = false;
		this.PosX = PosX;
		this.PosY = PosY;
		this.PosZ = PosZ;

		if (Light > 255)
		{
			Light = 255;
		}
		if (Light < 0)
		{
			Light = 0;
		}

		this.Light = (byte) (Light - 128);
		this.Radius = Radius;
		this.Height = Height;
		this.Health = Health;
	}

	// A special thing that takes an angle
	public Thing(String Type, float X, float Y, float Z, short Angle)
	{
		try
		{
			Names Value = Names.valueOf(Type);
			this.Type = Value;

			switch (Value)
			{
				case Spawn:
					Radius = 16;
					Health = 1000;
					Height = 56;
					Sprite = null;
					break;

				default:
					Radius = 0;
					Height = 0;
					Health = 0;
					Sprite = null;
					this.Type = Names.Unknown;    // Don't know its type, so set it to 'Unknown'.
					break;
			}

			PosX = X;
			PosY = Y;
			PosZ = Z;
			this.Angle = (short)((float)Angle * 91.022222222222222222222222222222);//(byte)(Angle * 1.40625);

		}
		catch (Exception ex)
		{
			System.err.println("An exception occured when constructing a 'thing' object of type " + Type + ": " + ex.getMessage());
		}
	}

	public Thing(String Type, float X, float Y, float Z)
	{
		try
		{
			// Special case
			if (Type.equals("ak47"))
			{
				Type = "Ak47";
			}
			else if (Type.equals("tek9"))
			{
				Type = "Tek9";
			}
			else if (Type.equals("flag"))
			{
				Type = "Flag";
			}
			else if (Type.equals("palmtree"))
			{
				Type = "PalmTree";
			}
			else if (Type.equals("smallpalmtree"))
			{
				Type = "SmallPalmTree";
			}

			Names Value = Names.valueOf(Type);
			this.Type = Value;

			switch (Value)
			{
				case Ak47:
					Health = 10;
					Radius = 16;
					Height = 24;
					Impassable = false;
					CanBePickedUp = true;
					Sprite = new Texture(ResPath + "Ak47.png", TextureFilter);
					break;
				case Tek9:
					Health = 10;
					Radius = 10;
					Height = 16;
					Impassable = false;
					CanBePickedUp = true;
					Sprite = new Texture(ResPath + "Tek9.png", TextureFilter);
					break;
				case Pistol:
					Health = 10;
					Radius = 16;
					Height = 24;
					Impassable = false;
					CanBePickedUp = true;
					Sprite = new Texture(ResPath + "Pistol.png", TextureFilter);
					break;
				case Flag:
					Radius = 16;
					Height = 48;
					Health = 10;
					Impassable = false;
					CanBePickedUp = true;
					Sprite = new Texture(ResPath + "Flag.png", TextureFilter);
					break;
				case Blood:
					Radius = 7;
					Height = 14;
					Health = 10;
					Impassable = false;
					CanBePickedUp = false;
					Sprite = new Texture(ResPath + "blood1.png", TextureFilter);
					break;
				case DeadPlayer:
					Radius = 22;
					Height = 50;
					Health = 10;
					Impassable = false;
					CanBePickedUp = false;
					Sprite = new Texture("res/player/PLAYH0.png", TextureFilter);
					break;
				case PalmTree:
					Radius = 24;
					Height = 96;
					Health = 100;
					Impassable = true;
					CanBePickedUp = false;
					Sprite = new Texture(ResPath + "nyar40.png", TextureFilter);
					break;
				case SmallPalmTree:
					Radius = 32;
					Height = 96;
					Health = 100;
					Impassable = true;
					CanBePickedUp = false;
					Sprite = new Texture(ResPath + "palm_tree_PNG2494.png", TextureFilter);
					break;

				default:
					Radius = 0;
					Height = 0;
					Health = 0;
					Sprite = null;
					this.Type = Names.Unknown;    // Don't know its type, so set it to 'Unknown'.
					break;
			}

			PosX = X;
			PosY = Y;
			PosZ = Z;

		}
		catch (Exception ex)
		{
			System.err.println("An exception occured when constructing a 'thing' object of type " + Type + ": " + ex.getMessage());
		}
	}

	public boolean Update()
	{
		switch (Type)
		{
			case Blood:
				Fall();

				Frame++;
				if (Frame == 2)
					Sprite = new Texture(ResPath + "blood2.png", TextureFilter);
				else if (Frame == 4)
					Sprite = new Texture(ResPath + "blood3.png", TextureFilter);

				// Check if it has completely fallen.
				if (PosZ <= 0)
				{
					// The code in which this method is called should take care of deleting the reference
					return false;
				}
				break;

			case DeadPlayer:
				if (Frame == 3)
				{
					Sprite = new Texture("res/player/PLAYI0.png", TextureFilter);
					Radius = 18;
					Height = 43;
				}
				else if (Frame == 6)
				{
					Sprite = new Texture("res/player/PLAYJ0.png", TextureFilter);
					Radius = 18;
					Height = 46;
				}
				else if (Frame == 9)
				{
					Sprite = new Texture("res/player/PLAYK0.png", TextureFilter);
					Radius = 20;
					Height = 38;
				}
				else if (Frame == 12)
				{
					Sprite = new Texture("res/player/PLAYL0.png", TextureFilter);
					Radius = 25;
					Height = 14;
				}
				else if (Frame == 15)
				{
					Sprite = new Texture("res/player/PLAYM0.png", TextureFilter);
					Radius = 25;
					Height = 14;
				}
				else if (Frame == 18)
				{
					Sprite = new Texture("res/player/PLAYN0.png", TextureFilter);
					Radius = 25;
					Height = 14;
				}

				if (Frame < 40 && Frame >= 0)
				{
					Frame++;
				}
				else
				{
					Frame = -1;
					Sprite = null;
				}
				break;
		}

		return true;
	}

	public void Fall()
	{
		// Finds the ground under it and hold to it.
		// If there is no floor, then fall.

		if (MoZ == 0)
		{
			MoZ = 2;
		}
		else
		{
			MoZ = MoZ * 2;
		}

		PosZ = PosZ - MoZ;
	}

	public void Place(float X, float Y, float Z, byte Angle)
	{
		PosX = X;
		PosY = Y;
		PosZ = Z;
	}

	// Set X Position
	public void PosX(float X)
	{
		PosX = X;
	}

	// Get X position
	public float PosX()
	{
		return PosX;
	}

	// Set Y Position
	public void PosY(float Y)
	{
		PosY = Y;
	}

	// Get Y Position
	public float PosY()
	{
		return PosY;
	}

	// Set Z Position
	public void PosZ(float Z)
	{
		PosZ = Z;
	}

	// Get Z Position
	public float PosZ()
	{
		return PosZ;
	}

	public int Height()
	{
		return Height;
	}

	public int Radius()
	{
		return Radius;
	}
}

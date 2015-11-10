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

	public enum Names
	{
		Barrel, StimPack, MediPack, Chaingun, Pistol, AmmoClip, AmmoBox, Ak47,
		Shells, ShellBox, Rocket, RocketBox, Cells, Bullet, Plasma, Spawn,
		Flag, Unknown, Custom
	}

	Names Type;

	public Thing(String Sprite, float PosX, float PosY, float PosZ, int Light, int Radius, int Height, int Health)
	{
		Type = Names.Custom;
		this.Sprite = new Texture(Sprite, GL_NEAREST);

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
					Sprite = null; //new Texture("res/spawn/spawn1.png", GL_NEAREST);
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
		catch (IllegalArgumentException iae)
		{
			System.err.println("Map thing has a bad type: " + Type);
		}
		catch (Exception e)
		{
			// Should be done before we create the object
			System.err.println("Map thing failed to be determined over its type: " + e.toString()/*getMessage*/);
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

			Names Value = Names.valueOf(Type);
			this.Type = Value;

			switch (Value)
			{
				case Barrel:
					Radius = 16;
					Health = 25;
					Height = 24;
					Impassable = true;
					CanBePickedUp = false;
					Sprite = new Texture(ResPath + "Barrel.png", GL_NEAREST);
					break;
				case StimPack:
					Radius = 16;
					Height = 24;
					Health = 10;
					Impassable = false;
					CanBePickedUp = true;
					Sprite = new Texture(ResPath + "StimPack.png", GL_NEAREST);
					break;
				case MediPack:
					Radius = 16;
					Height = 24;
					Health = 10;
					Impassable = false;
					CanBePickedUp = true;
					Sprite = new Texture(ResPath + "MediPack.png", GL_NEAREST);
					break;
				case Chaingun:
					Health = 10;
					Radius = 16;
					Height = 24;
					Impassable = false;
					CanBePickedUp = true;
					Sprite = new Texture(ResPath + "Chaingun.png", GL_NEAREST);
					break;
				case Ak47:
					Health = 10;
					Radius = 16;
					Height = 24;
					Impassable = false;
					CanBePickedUp = true;
					Sprite = new Texture(ResPath + "Ak47.png", GL_NEAREST);
					break;
				case Pistol:
					Health = 10;
					Radius = 16;
					Height = 24;
					Impassable = false;
					CanBePickedUp = true;
					Sprite = new Texture(ResPath + "Pistol.png", GL_NEAREST);
					break;
				case AmmoClip:
					Radius = 16;
					Height = 24;
					Health = 10;
					Impassable = false;
					CanBePickedUp = true;
					Sprite = new Texture(ResPath + "AmmoClip.png", GL_NEAREST);
					break;
				case AmmoBox:
					Radius = 16;
					Height = 24;
					Health = 10;
					Impassable = false;
					CanBePickedUp = true;
					Sprite = new Texture(ResPath + "AmmoBox.png", GL_NEAREST);
					break;
				case Shells:
					Radius = 16;
					Height = 24;
					Health = 10;
					Impassable = false;
					CanBePickedUp = true;
					Sprite = new Texture(ResPath + "Shells.png", GL_NEAREST);
					break;
				case ShellBox:
					Radius = 16;
					Height = 24;
					Health = 10;
					Impassable = false;
					CanBePickedUp = true;
					Sprite = new Texture(ResPath + "ShellBox.png", GL_NEAREST);
					break;
				case Rocket:
					Radius = 16;
					Height = 24;
					Health = 10;
					Impassable = false;
					CanBePickedUp = true;
					Sprite = new Texture(ResPath + "Rocket.png", GL_NEAREST);
					break;
				case RocketBox:
					Radius = 16;
					Height = 24;
					Health = 10;
					Impassable = false;
					CanBePickedUp = true;
					Sprite = new Texture(ResPath + "RocketBox.png", GL_NEAREST);
					break;
				case Cells:
					Radius = 16;
					Height = 24;
					Health = 10;
					Impassable = false;
					CanBePickedUp = true;
					Sprite = new Texture(ResPath + "Cells.png", GL_NEAREST);
					break;
				case Bullet:
					Radius = 8;
					Height = 24;
					Health = 10;
					Impassable = false;
					CanBePickedUp = false;
					Sprite = new Texture(ResPath + "Bullet.png", GL_NEAREST);
					break;
				case Plasma:
					Radius = 12;
					Height = 24;
					Health = 10;
					Impassable = false;
					CanBePickedUp = false;
					Sprite = new Texture(ResPath + "Plasma.png", GL_NEAREST);
					break;
				case Flag:
					Radius = 16;
					Height = 48;
					Health = 10;
					Impassable = false;
					CanBePickedUp = true;
					Sprite = new Texture(ResPath + "Flag.png", GL_NEAREST);
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
		catch (IllegalArgumentException iae)
		{
			System.err.println("Map thing has a bad type: " + Type);
		}
		catch (Exception e)
		{
			// Should be done before we create the object
			System.err.println("Map thing failed to be determined over its type: " + e.toString()/*getMessage*/);
		}
	}

	public void Update(ArrayList<Plane> Planes, ArrayList<Thing> Things)
	{
		switch (Type)
		{
			case Barrel:
				UpdateBarrel();
				break;
			case Bullet:
				UpdateBullet();
				break;
			case Plasma:
				UpdatePlasma();
				break;

		}
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

	}

	public void UpdateBarrel()
	{

	}

	public void UpdateBullet()
	{

	}

	public void UpdatePlasma()
	{

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

	public void MakesNoise(String Sound)
	{
		this.Sound = Sound;
	}

	public String Noise()
	{
		return Sound;
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

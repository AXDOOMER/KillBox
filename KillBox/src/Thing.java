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
    float PosX;	// Horizontal position
    float PosY;	// Vertical position
    float PosZ;	// Height, do not mix with Y.
    float MoX = 0;	// Like DirX
    float MoY = 0;	// Like DirY
    float MoZ = 0;	// Like DirZ
    int Height = 24;
    int Health = 0;
    int Radius = 16;
    String Sound;	// A thing can make a sound
	Texture Sprite;	// What does it looks like?
	int Frame = 0;	// Object state

	public enum Names
	{
		Barrel, StimPack, MediPack, Chaingun, Pistol, AmmoClip, AmmoBox,
		Shells, ShellBox, Rocket, RocketBox, Cells, Bullet, Plasma,
		Unknown
	}
	Names Type;

    public Thing(String Type, float X, float Y, float Z)
    {
        try
        {
			Names Value = Names.valueOf(Type);

            switch(Value)
            {
				case Barrel:
                    Radius = 16;
                    Health = 25;
                    Height = 24;
					Sprite = new Texture("Barrel.png", GL_NEAREST);
                    break;
                case StimPack:
                    Radius = 16;
                    Height = 24;
					Health = 10;
					Sprite = new Texture("StimPack.png", GL_NEAREST);
                    break;
                case MediPack:
                    Radius = 16;
                    Height = 24;
					Health = 10;
					Sprite = new Texture("MediPack.png", GL_NEAREST);
                    break;
                case Chaingun:
					Health = 10;
                    Radius = 16;
                    Height = 24;
					Sprite = new Texture("Chaingun.png", GL_NEAREST);
                    break;
                case Pistol:
					Health = 10;
                    Radius = 16;
                    Height = 24;
					Sprite = new Texture("Pistol.png", GL_NEAREST);
                    break;
                case AmmoClip:
                    Radius = 16;
                    Height = 24;
					Health = 10;
					Sprite = new Texture("AmmoClip.png", GL_NEAREST);
                    break;
                case AmmoBox:
                    Radius = 16;
                    Height = 24;
					Health = 10;
					Sprite = new Texture("AmmoBox.png", GL_NEAREST);
                    break;
                case Shells:
                    Radius = 16;
                    Height = 24;
					Health = 10;
					Sprite = new Texture("Shells.png", GL_NEAREST);
                    break;
                case ShellBox:
                    Radius = 16;
                    Height = 24;
					Health = 10;
					Sprite = new Texture("ShellBox.png", GL_NEAREST);
                    break;
                case Rocket:
                    Radius = 16;
                    Height = 24;
					Health = 10;
					Sprite = new Texture("Rocket.png", GL_NEAREST);
                    break;
                case RocketBox:
                    Radius = 16;
                    Height = 24;
					Health = 10;
					Sprite = new Texture("RocketBox.png", GL_NEAREST);
                    break;
                case Cells:
                    Radius = 16;
                    Height = 24;
					Health = 10;
					Sprite = new Texture("Cells.png", GL_NEAREST);
                    break;
                case Bullet:
                    Radius = 8;
                    Height = 24;
					Health = 10;
					Sprite = new Texture("Bullet.png", GL_NEAREST);
                    break;
                case Plasma:
                    Radius = 12;
                    Height = 24;
					Health = 10;
					Sprite = new Texture("Plasma.png", GL_NEAREST);
                    break;

                default:
					Radius = 0;
					Height = 0;
					Health = 0;
					Sprite = null;
					this.Type = Names.Unknown;	// Don't know its type, so set it to 'Unknown'.
					break;
            }

            PosX = X;
            PosY = Y;
            PosZ = Z;

        }
		catch(IllegalArgumentException iae)
		{
			System.err.println("Map thing has a bad type: " + Type);
		}
        catch(Exception e)
        {
            // Should be done before we create the object
            System.err.println("Map thing failed to be determined over its type: " + e.toString()/*getMessage*/);
        }
    }

    public void Update(ArrayList<Plane> Planes, ArrayList<Thing> Things)
    {
        switch(Type)
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
    public void X(float X)
    {
        PosX = X;
    }

    // Get X position
    public float X()
    {
        return PosX;
    }

    // Set Y Position
    public void Y(float Y)
    {
        PosY = Y;
    }

    // Get Y Position
    public float Y()
    {
        return PosY;
    }

    // Set Z Position
    public void Z(float Z)
    {
        PosZ = Z;
    }

    // Get Z Position
    public float Z()
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
}

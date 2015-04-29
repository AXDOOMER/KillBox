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

import java.util.ArrayList;

public class Thing
{
    float PosX;	// Horizontal position
    float PosY;	// Vertical position
    float PosZ;	// Height, do not mix with Y.
    float MoX = 0;
    float MoY = 0;
    float MoZ = 0;
    int Height = 24;
    int Health = 0;
    int Radius;
    String Type;
    String Sound;	// A thing can make a sound
    String Sprite;	// What does it looks like?
	int Die = 0;

    public Thing(String Type, float X, float Y, float Z)
    {
        try
        {
			this.Type = Type;

            switch(Type)
            {
                case "Barrel":
                    Radius = 16;
                    Health = 25;
                    Height = 24;
					Sprite = "Barrel.png";
                    break;
                case "StimPack":
                    Radius = 16;
                    Height = 24;
					Sprite = "StimPack.png";
                    break;
                case "MediPack":
                    Radius = 16;
                    Height = 24;
					Sprite = "MediPack.png";
                    break;
                case "Chaingun":
                    Radius = 16;
                    Height = 24;
					Sprite = "Chaingun.png";
                    break;
                case "Pistol":
                    Radius = 16;
                    Height = 24;
					Sprite = "Pistol.png";
                    break;
                case "AmmoClip":
                    Radius = 16;
                    Height = 24;
					Sprite = "AmmoClip.png";
                    break;
                case "AmmoBox":
                    Radius = 16;
                    Height = 24;
					Sprite = "AmmoBox.png";
                    break;
                case "Shells":
                    Radius = 16;
                    Height = 24;
					Sprite = "Shells.png";
                    break;
                case "ShellBox":
                    Radius = 16;
                    Height = 24;
					Sprite = "ShellBox.png";
                    break;
                case "Rocket":
                    Radius = 16;
                    Height = 24;
					Sprite = "Rocket.png";
                    break;
                case "RocketBox":
                    Radius = 16;
                    Height = 24;
					Sprite = "RocketBox.png";
                    break;
                case "Cells":
                    Radius = 16;
                    Height = 24;
					Sprite = "Cells.png";
                    break;
                case "Bullet":
                    Radius = 8;
                    Height = 24;
					Sprite = "Bullet.png";
                    break;
                case "Plasma":
                    Radius = 12;
                    Height = 24;
					Sprite = "Plasma.png";
                    break;

                default: throw new Exception(Type);
            }

            PosX = X;
            PosY = Y;
            PosZ = Z;

        }
        catch(Exception e)
        {
            // Devrait se faire avant qu'on crée l'objet
            // Should be done before we create the object
            System.err.println("Map thing failed to be determined over its type: " + e.toString()/*getMessage*/);
        }
    }

    public void Update(ArrayList<Plane> Planes, ArrayList<Thing> Things)
    {
        switch(Type)
        {
            case "Barrel":
                UpdateBarrel();
                break;
            case "Bullet":
                UpdateBullet();
                break;
            case "Plasma":
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

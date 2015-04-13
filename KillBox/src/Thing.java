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

public class Thing
{
    float PosX_;	// Horizontal position
    float PosY_;	// Vertical position
    float PosZ_;	// Height, do not mix with Y.
    float MoX_ = 0;
    float MoY_ = 0;
    float MoZ_ = 0;
    int Height_ = 24;
    int Health_ = 0;
    int Radius_;
    String Type_;
    String Sound_;	// A thing can make a sound
    String Picture_;	// What does it looks like?

    public Thing(String Type, float X, float Y, float Z)
    {
        Type_ = Type;

        try
        {
            switch(Type)
            {
                case "Barrel":
                    Radius_ = 16;
                    Health_ = 25;
                    Height_ = 24;
                    Picture_ = "Barrel.png";
                    break;
                case "StimPack":
                    Radius_ = 16;
                    Height_ = 24;
                    Picture_ = "StimPack.png";
                    break;
                case "MediPack":
                    Radius_ = 16;
                    Height_ = 24;
                    Picture_ = "MediPack.png";
                    break;
                case "Chaingun":
                    Radius_ = 16;
                    Height_ = 24;
                    Picture_ = "Chaingun.png";
                    break;
                case "Pistol":
                    Radius_ = 16;
                    Height_ = 24;
                    Picture_ = "Pistol.png";
                    break;
                case "AmmoClip":
                    Radius_ = 16;
                    Height_ = 24;
                    Picture_ = "AmmoClip.png";
                    break;
                case "AmmoBox":
                    Radius_ = 16;
                    Height_ = 24;
                    Picture_ = "AmmoBox.png";
                    break;
                case "Shells":
                    Radius_ = 16;
                    Height_ = 24;
                    Picture_ = "Shells.png";
                    break;
                case "ShellBox":
                    Radius_ = 16;
                    Height_ = 24;
                    Picture_ = "ShellBox.png";
                    break;
                case "Rocket":
                    Radius_ = 16;
                    Height_ = 24;
                    Picture_ = "Rocket.png";
                    break;
                case "RocketBox":
                    Radius_ = 16;
                    Height_ = 24;
                    Picture_ = "RocketBox.png";
                    break;
                case "Cells":
                    Radius_ = 16;
                    Height_ = 24;
                    Picture_ = "Cells.png";
                    break;
                case "Bullet":
                    Radius_ = 8;
                    Height_ = 24;
                    Picture_ = "Bullet.png";
                    break;
                case "Plasma":
                    Radius_ = 12;
                    Height_ = 24;
                    Picture_ = "Plasma.png";
                    break;

                default: throw new Exception(Type);
            }

            PosX_ = X;
            PosY_ = Y;
            PosZ_ = Z;

        }
        catch(Exception e)
        {
            // Devrait se faire avant qu'on crée l'objet
            // Should be done before we create the object
            System.err.println("Map thing failed to be determined over its type: " + e.toString()/*getMessage*/);
        }
    }

    public void Update()
    {
        switch(Type_)
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

        if (MoZ_ == 0)
        {
            MoZ_ = 2;
        }
        else
        {
            MoZ_ = MoZ_ * 2;
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
        PosX_ = X;
        PosY_ = Y;
        PosZ_ = Z;
    }

    // Set X Position
    public void X(float X)
    {
        PosX_ = X;
    }

    // Get X position
    public float X()
    {
        return PosX_;
    }

    // Set Y Position
    public void Y(float Y)
    {
        PosY_ = Y;
    }

    // Get Y Position
    public float Y()
    {
        return PosY_;
    }

    // Set Z Position
    public void Z(float Z)
    {
        PosZ_ = Z;
    }

    // Get Z Position
    public float Z()
    {
        return PosZ_;
    }

    public void MakesNoise(String Sound)
    {
        Sound_ = Sound;
    }

    public String Noise()
    {
        return Sound_;
    }
}

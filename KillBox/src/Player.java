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

public class Player
{
    float PosX;	// Horizontal position
    float PosY;	// Vertical position
    float PosZ;	// Height, do not mix with Y.
    short Angle = 8192;	// Angles, from -16384 to 16383.

    final int MaxOwnedWeapons = 10;
    Boolean[] OwnedWeapons_ = new Boolean[MaxOwnedWeapons];

    float MoX = 0;
    float MoY = 0;
    float MoZ = 0;

    final int MaxWalkSpeed = 40;
    final int MaxRunSpeed = 70;
    final int ViewZ = 2;
    byte Damages = 0;	// Damage location indicator: 0=none, 1=both, 2=left, 3=right

    final int Acceleration = 2;
    final int Radius = 16;
    final int Height = 56;
    int Health = 100;	// The player's life condition
    int Armor = 100;	// Recharging Energy Shield
    byte ArmorClass = 0;

    int Kills = 0;
    int Deaths = 0;

    Sound Emitter = null;   // Must get the already initialized SndDriver

    public Player(Level Lvl, Sound Output)
    {
        Emitter = Output;

        for (int i = 0; i < MaxOwnedWeapons; i++)
        {
            OwnedWeapons_[i] = false;
        }
    }

    public String BuildNetCmd()
    {
        String Command = "";		//#!*/
        Command = Command + (char)((int)Angle + 32768);
        return Command;
    }

    public void ForwardMove(int Speed)
    {
        /*float AdjustedAngle = GetRadianAngle() + (float) Math.PI / 2;

        PosX += Speed * Math.cos(AdjustedAngle);
        PosY += Speed * Math.sin(AdjustedAngle);*/

        PosX += Speed * Math.cos(GetRadianAngle());
        PosY += Speed * Math.sin(GetRadianAngle());
    }

    public void LateralMove(int Speed)
    {
        /*PosX += Speed * Math.cos(GetRadianAngle());
        PosY += Speed * Math.sin(GetRadianAngle());*/

        float AdjustedAngle = GetRadianAngle() - (float) Math.PI / 2;

        PosX += Speed * Math.cos(AdjustedAngle);
        PosY += Speed * Math.sin(AdjustedAngle);
    }

    public void Throw(int Thrust)
    {
        PosX += Thrust * Math.cos(GetRadianAngle());
        PosY += Thrust * Math.sin(GetRadianAngle());
    }

    public float GetRadianAngle()
    {
        return Angle * (float)Math.PI * 2 / 32768;
    }

    public float GetDegreeAngle()
    {
        return Angle * 360f / 32768;
    }

    public void AngleTurn(short AngleChange)
    {
        // Our internal representation of angles goes from -16384 to 16383,
        // so there are 32768 different angles possible.

        // If you turn bigger than 180 degrees on one side,
        // why didn't you turn the other side?
        if (AngleChange < 16383 && AngleChange > -16384)
        {
            Angle += AngleChange;

            if (Angle  > 16383)
            {
                Angle = (short)((int)Angle - 32768);
            }
            else if (Angle < -16384)
            {
                Angle = (short)((int)Angle + 32768);
            }
        }
    }

    private int Height()
    {
        // Gives the height of the player
        return Height;
    }

    private float GetMiddlePosZ()
    {
        // Gives the middle coordinate of the player
        return PosZ + (float)Height / 2;
    }

    private int View()
    {
        // Gives the view's height
        return Height * 4 / 2;
    }

    private void HealthChange(int Change)
    {
        // Apply damages to the player
        Health = Health + Change;
    }

    public void Place(float X, float Y, float Z, short Angle)
    {
        PosX = X;
        PosY = Y;
        PosZ = Z;
        this.Angle = Angle;
    }

    public void Teleport(float X, float Y, float Z, short Angle)
    {
        // Update the player to the new coordinates
        PosX = X;
        PosY = Y;
        PosZ = Z;
        this.Angle = Angle;

        MoX = 0;
        MoY = 0;
        MoZ = 0;

    }

    public void Fall()
    {
        if (MoZ == 0)
        {
            MoZ = 2;
        }
        else
        {
            MoZ = MoZ * 2;
        }
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

    public void MoveUp()
    {
        PosZ++;
    }

    public void MoveDown()
    {
        PosZ--;
    }

    public void MakesNoise(String Sound)
    {
        Emitter.PlaySound(this);
    }
}

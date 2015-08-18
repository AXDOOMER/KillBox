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
import static org.lwjgl.opengl.GL11.GL_NEAREST;

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
	boolean HasMoved = false;

	final int MaxWalkSpeed = 40;
	final int MaxRunSpeed = 70;
	final int ViewZ = 42;
	
	public enum DamageIndicatorDirection
	{
		None, Front, Both, Left, Right, Back
	}
	DamageIndicatorDirection Damages = DamageIndicatorDirection.None;

	final int Rotations = 8;
	static ArrayList<Texture> WalkFrames = new ArrayList<Texture>();

	final int Acceleration = 50;
	final int Deceleration = 2;
	final int Radius = 16;
	final int Height = 56;
	int Health = 100;	// The player's life condition
	int Armor = 100;	// Recharging Energy Shield
	byte ArmorClass = 0;

	int Kills = 0;
	int Deaths = 0;

	int Frame = 0;
	Sound Emitter = null;	// Must get the already initialized SndDriver
	Level Lvl = null;		// The player must know where he is

	public Player(Level Lvl, Sound Output)
	{
		Emitter = Output;

		for (int i = 0; i < MaxOwnedWeapons; i++)
		{
			OwnedWeapons_[i] = false;
		}
	}

	public void LoadSprites()
	{
		// Load Sprites for the Players
		for (int Rotation = 1; Rotation <= Rotations; Rotation++)
		{
			Texture NextFrame = new Texture("Stuff/player/PLAY" + "A" + Rotation + ".png", GL_NEAREST);
			WalkFrames.add(NextFrame);
		}
	}

	public String BuildNetCmd()
	{
		String Command = "";        //#!*/
		Command = Command + (char) ((int) Angle + 32768);
		return Command;
	}
	
	public void DamageSelf(int Damage, float DmgSrcX, float DmgSrcY)
	{
		float Angle = (float) Math.atan2(DmgSrcY, DmgSrcX);
		
		HealthChange(-Damage);
		
		if (Angle >= Math.PI / 4 && Angle >= -Math.PI / 4)
		{
			// It's at the player's right
			Damages = DamageIndicatorDirection.Right;
		}
		else if (Angle >= Math.PI - Math.PI / 4 && Angle >= -Math.PI - Math.PI / 4)
		{
			// It's at the player's left
			Damages = DamageIndicatorDirection.Left;
		}
		else if (Angle <= -Math.PI - Math.PI / 4 && Angle <= -Math.PI / 4)
		{
			// It's at the player's back
			Damages = DamageIndicatorDirection.Back;
		}
		else if (Angle >= Math.PI / 4 && Angle <= Math.PI - Math.PI / 4)
		{
			// It's at the player's front
			Damages = DamageIndicatorDirection.Front;
		}
		else
		{
			// Unknown
			Damages = DamageIndicatorDirection.None;
		}
	}
	
	// Check if this coordinate is inside a player then return this player
	public Player PointInPlayer(float CoordX, float CoordY, float CoordZ)
	{
		for (int Player = 0; Player < Lvl.Players().size(); Player++)
		{
			if (Lvl.Players().get(Player) == this)
			{
				// Next!
				continue;
			}
			
			// Check if the coordinate is inside the player on the Z axis
			if (CoordZ >= Lvl.Players().get(Player).PosZ() && 
				CoordZ <= Lvl.Players().get(Player).PosZ() + 
				Lvl.Players().get(Player).Height)
			{
				// Now, check if it's inside the player's radius
				float Distance = (float)Math.sqrt(
						Math.pow(Lvl.Players().get(Player).PosX() - CoordX, 2) + 
						Math.pow(Lvl.Players().get(Player).PosY() - CoordY, 2));
				
				if (Distance <= Lvl.Players().get(Player).Radius)
				{
					return Lvl.Players().get(Player);
				}
			}
		}
		
		return null;
	}
	
	public void HitScan(float HorizontalAngle, float VerticalAngle, int Damage)
	{
		float Step = 2;		// Incremental steps at which the bullet checks for collision
		int MaxChecks = 2048;		// Max check for the reach of a bullet
		
		// Start scanning from the player's position
		float TravelX = this.PosX();
		float TravelY = this.PosY();
		float TravelZ = this.PosZ();

		// Move the bullet and check for collision
		for (int Point = 0; Point < MaxChecks; Point++)
		{
			// Increment bullet position
			TravelX = TravelX * Step * (float)Math.cos((float)HorizontalAngle);
			TravelY = TravelY * Step * (float)Math.sin((float)HorizontalAngle);
			TravelZ = TravelZ * Step * (float)Math.sin((float)VerticalAngle);
			
			Player Hit = PointInPlayer(TravelX, TravelY, TravelZ);
			
			// Check if something was really hit
			if (Hit != null)
			{
				// Damage him
				Hit.DamageSelf(Damage, this.PosX(), this.PosY());
			}
		}
		
	}

	public void ForwardMove(int Direction)
	{
		if (Direction > 0)
		{
			MoX += Acceleration * Math.cos(GetRadianAngle());
			MoY += Acceleration * Math.sin(GetRadianAngle());
		}
		else if (Direction < 0)
		{
			MoX -= Acceleration * Math.cos(GetRadianAngle());
			MoY -= Acceleration * Math.sin(GetRadianAngle());
		}
		// Don't do anything when 'Direction' is equal to zero

		// Flag so it is known that the player wants to move
		HasMoved = true;
	}

	public void LateralMove(int Direction)
	{
		float AdjustedAngle = GetRadianAngle() - (float) Math.PI / 2;

		if (Direction > 0)
		{
			MoX += Acceleration * Math.cos(AdjustedAngle);
			MoY += Acceleration * Math.sin(AdjustedAngle);
		}
		else if (Direction < 0)
		{
			MoX -= Acceleration * Math.cos(AdjustedAngle);
			MoY -= Acceleration * Math.sin(AdjustedAngle);
		}
		// Don't do anything when 'Direction' is equal to zero

		// Flag so it is known that the player wants to move
		HasMoved = true;
	}

	// Check collision against other players
	public float CheckPlayerToPlayerCollision()
	{

		// If a collision is not detected, return the angle that
		// signifies that the player is continuing in the same direction
		return (float)Math.atan2(MoY(), MoX());
	}

	// Check for collision against walls
	public float CheckWallCollision()
	{

		// The player doesn't deviate. Its movement is not divergeant.
		return (float)Math.atan2(MoY(), MoX());
	}

	// Searches for the floor that is under the player
	public float AdjustPlayerHeightToFloor()
	{
		// Check for floors

		// When it is found, change the player's Z.

		// Else, estimate.
		PosZ(FindClosestVertexZ());

		// Return the player's position. That's unnecessary.
		return PosZ();
	}

	public void Move()
	{
		// Constant deceleration
		if (MoX != 0)
		{
			MoX /= Deceleration;
		}
		if (MoY != 0)
		{
			MoY /= Deceleration;
		}

		if (MoX > MaxRunSpeed)
		{
			// Positive X movement limit
			MoX = MaxRunSpeed;
		}
		if (MoY > MaxRunSpeed)
		{
			// Positive Y movement limit
			MoY = MaxRunSpeed;
		}
		if (MoX < -MaxRunSpeed)
		{
			// Negative X movement limit
			MoX = -MaxRunSpeed;
		}
		if (MoY < -MaxRunSpeed)
		{
			// Positive Y movement limit
			MoY = -MaxRunSpeed;
		}

		// Change the postion according to the direction of the movement
		PosX += MoX;
		PosY += MoY;

		// Fix innacuracies
		if (MoX > 0 && MoX < 1 || MoX < 0 && MoX > -1)
		{
			MoX = 0;
		}
		if (MoY > 0 && MoY < 1 || MoY < 0 && MoY > -1)
		{
			MoY = 0;
		}

		// Reset
		HasMoved = false;
	}

	public void Throw(int Thrust, short Direction)
	{
		MoX += Thrust * Math.cos(Direction * (float) Math.PI * 2 / 32768);
		MoY += Thrust * Math.sin(Direction * (float) Math.PI * 2 / 32768);
	}

	public float GetRadianAngle()
	{
		return Angle * (float) Math.PI * 2 / 32768;
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

			if (Angle > 16383)
			{
				Angle = (short) ((int) Angle - 32768);
			}
			else if (Angle < -16384)
			{
				Angle = (short) ((int) Angle + 32768);
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
		return PosZ + (float) Height / 2;
	}

	private int View()
	{
		// Gives the view's height
		return Height * 3 / 4;
	}

	private void HealthChange(int Change)
	{
		// Apply damages to the player
		Health = Health + Change;
	}

	public void Place(float NewX, float NewY, float NewZ, short Angle)
	{
		PosX = NewX;
		PosY = NewY;
		PosZ = NewZ;
		this.Angle = Angle;
	}

	public void Teleport(float NewX, float NewY, float NewZ, short NewAngle)
	{
		// Update the player to the new coordinates
		PosX = NewX;
		PosY = NewY;
		PosZ = NewZ;
		this.Angle = NewAngle;

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

	public float FindClosestVertexZ()
	{
		float FoundZ = PosZ();
		float SmallestDistance = -1;

		for (int Plane = 0; Plane < Lvl.Planes.size(); Plane++)
		{
			for (int Vertex = 0; Vertex < Lvl.Planes.get(Plane).Vertices.size(); Vertex += 3)
			{
				float TempDistance = (float)Math.sqrt(
						Math.pow(Math.abs(PosX() - Lvl.Planes.get(Plane).Vertices.get(Vertex)), 2) +
						Math.pow(Math.abs(PosY() - Lvl.Planes.get(Plane).Vertices.get(Vertex + 1)), 2));

				if (SmallestDistance < 0)
				{
					// When it's not set, set it to something.
					SmallestDistance = TempDistance;
					// Set new Z height
					FoundZ = Lvl.Planes.get(Plane).Vertices.get(Vertex + 2);
				}
				else
				{
					if (TempDistance <= SmallestDistance)
					{
						SmallestDistance = TempDistance;

						// Set it to the lowest height
						if (FoundZ > Lvl.Planes.get(Plane).Vertices.get(Vertex + 2))
						{
							FoundZ = Lvl.Planes.get(Plane).Vertices.get(Vertex + 2);
						}
					}
				}

			}
		}

		// The function returns something, but nobody really cares.
		return FoundZ;
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
		PosZ = PosZ + 64;
	}

	public void MoveDown()
	{
		PosZ = PosZ - 64;
	}

	public void MakesNoise(String Sound)
	{
		Emitter.PlaySound(this, Sound);
	}

	public float MoX()
	{
		return MoX;
	}

	public float MoY()
	{
		return MoY;
	}

}

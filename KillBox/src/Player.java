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
	float PosX = -100;	// Horizontal position
	float PosY = -100;	// Vertical position
	float PosZ;	// Height, do not mix with Y.
	short Angle = 8192;	// Angles, from -16384 to 16383.

	final int MaxOwnedWeapons = 10;
	Boolean[] OwnedWeapons_ = new Boolean[MaxOwnedWeapons];
        
	float MoX = 0;
	float MoY = 0;
	float MoZ = 0;
	boolean HasMoved = false;

	final int MaxWalkSpeed = 40/8;	// BUG: At lower speed (e.g.: 10), the player does not move toward the good angle.
	final int MaxRunSpeed = 70/8;
	final int ViewZ = 42;
	
	public enum DamageIndicatorDirection
	{
		None, Front, Both, Left, Right, Back
	}
	DamageIndicatorDirection Damages = DamageIndicatorDirection.None;

	final int Rotations = 8;
	static public ArrayList<Texture> WalkFrames = new ArrayList<Texture>();

	final int Acceleration = 50/8;
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
		this.Lvl = Lvl;
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
			TravelX = TravelX * Step * (float)Math.cos(HorizontalAngle);
			TravelY = TravelY * Step * (float)Math.sin(HorizontalAngle);
			TravelZ = TravelZ * Step * (float)Math.sin(VerticalAngle);

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
			float NewX = MoX + Acceleration * (float)Math.cos(GetRadianAngle());
			float NewY = MoY + Acceleration * (float)Math.sin(GetRadianAngle());

			TryMove(NewX, NewY);
		}
		else if (Direction < 0)
		{
			float NewX = MoX - Acceleration * (float)Math.cos(GetRadianAngle());
			float NewY = MoY - Acceleration * (float)Math.sin(GetRadianAngle());

			TryMove(NewX, NewY);
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
			float NewX = MoX + Acceleration * (float)Math.cos(AdjustedAngle);
			float NewY = MoY + Acceleration * (float)Math.sin(AdjustedAngle);

			TryMove(NewX, NewY);
		}
		else if (Direction < 0)
		{
			float NewX = MoX - Acceleration * (float)Math.cos(AdjustedAngle);
			float NewY = MoY - Acceleration * (float)Math.sin(AdjustedAngle);

			TryMove(NewX, NewY);
		}
		// Don't do anything when 'Direction' is equal to zero

		// Flag so it is known that the player wants to move
		HasMoved = true;
	}

	// Try every type of collision.
	public float TryMove(float NewX, float NewY)
	{
		float Current = (float)Math.atan2(MoY(), MoX());
		// Should return the current angle if it's possible to move, else return another...

		boolean Clear = true;	// Can move
		float PushAngle = Float.NaN;

		// Fuck it when the player repetitively can't move
		int NumTests = 0;

		while (Clear && NumTests < this.Radius() * 2)
		{
			NumTests++;

			// Player against player collision. (Note: if(Float.NaN==Float.NaN) doesn't work)
			if (Float.isNaN(PushAngle = CheckPlayerToPlayerCollision(NewX + PosX(), NewY + PosY())))
			{
				if (Clear)
				{
					Clear = true;
				}
			}
			else
			{
				Clear = false;
			}

			// Player against thing collision
			if (Float.isNaN(PushAngle = CheckPlayerToThingsCollision(NewX + PosX(), NewY + PosY())))
			{
				if (Clear)
				{
					Clear = true;
				}
			}
			else
			{
				Clear = false;
			}

			// Player against wall collision
			if (Float.isNaN(PushAngle = CheckWallCollision(NewX + PosX(), NewY + PosY())))
			{
				if (Clear)
				{
					Clear = true;
				}
			}
			else
			{
				Clear = false;
			}


			if (!Clear)
			{
				// Devide movement, because we want to move less
				NewX /= 2;
				NewY /= 2;
				Clear = true;
			}
		}

		// Set corrected momentum that will be used to get the new possible position
		if (Clear)
		{
			MoX = NewX;
			MoY = NewY;
		}
		else if (!Float.isNaN(PushAngle))
		{
			// Try to push the player
			// If the player is alreay in another player, he's already fucked.

			// TO-DO: Make the player slide against the other's sides [DEVIATE THE MOMENTUM!!!]

			//MoX += (float)Math.cos(PushAngle);
			//MoY += (float)Math.sin(PushAngle);
		}

		// Move to the new position
		Move();

		return GetRadianAngle();
	}

	// Check collision against things
	public float CheckPlayerToThingsCollision(float NewX, float NewY)
	{
		for (int Thing = 0; Thing < Lvl.Things.size(); Thing++)
		{
			float Distance = (float)Math.sqrt(
					Math.pow(NewX - Lvl.Things.get(Thing).PosX(), 2) +
							Math.pow(NewY - Lvl.Things.get(Thing).PosY(), 2));

			// Test 2D collision
			if (Distance <= this.Radius() + Lvl.Things.get(Thing).Radius())
			{
				// Test the Z axis. Both players have the same height.
				if (Math.abs(this.PosZ() - Lvl.Things.get(Thing).PosZ()) <= Height())
				{
					// Collision! Return the angle toward the other player.

					float Glide = (float) Math.atan2(Lvl.Things.get(Thing).PosY() - PosY + NewY, Lvl.Things.get(Thing).PosX() - PosX + NewX);

					return Glide - GetRadianAngle();
				}
			}
		}

		// If there is no collision, don't return anything.
		return Float.NaN;
	}

	// Check collision against other players
	public float CheckPlayerToPlayerCollision(float NewX, float NewY)
	{
		for (int Player = 0; Player < Lvl.Players().size(); Player++)
		{
			if (Lvl.Players().get(Player) == this)
			{
				// Next!
				continue;
			}

			float Distance = (float)Math.sqrt(
					Math.pow(NewX - Lvl.Players().get(Player).PosX(), 2) +
							Math.pow(NewY - Lvl.Players().get(Player).PosY(), 2));

			// Test 2D collision
			if (Distance <= this.Radius() + Lvl.Players().get(Player).Radius())
			{
				// Test the Z axis. Both players have the same height.
				if (Math.abs(this.PosZ() - Lvl.Players().get(Player).PosZ()) <= Height())
				{
					// Collision! Return the angle toward the other player.

					float Glide = (float)Math.atan2(Lvl.Players().get(Player).PosY() - PosY + NewY, Lvl.Players().get(Player).PosX() - PosX + NewX);

					return Glide - GetRadianAngle();

/*
					// To the right
					if (Diff > 0)
					{
						Glide += Math.PI / 2;
						return Glide;
					}
					else if (Diff < 0)	// To the right
					{
						Glide -= Math.PI / 2;
						return Glide;
					}

					// Don't do anything
					return Float.NaN;
*/
				}
			}
		}

		// If there is no collision, don't return anything.
		return Float.NaN;
	}

	// Check for collision against walls
	public float CheckWallCollision(float NewX, float NewY)
	{
		for (int Plane = 0; Plane < Lvl.Planes.size(); Plane++)
		{
			// Add a function to the plane to see if it is valid instead
			if (Lvl.Planes.get(Plane).Vertices().size() >= 8 && Lvl.Planes.get(Plane).GetAngle() != Float.NaN)
			{
				// Find how the plane is placed in the environment
				float StartX = Lvl.Planes.get(Plane).Vertices().get(0);
				float StartY = Lvl.Planes.get(Plane).Vertices().get(1);
				float StartZ = Lvl.Planes.get(Plane).Vertices().get(2);

				float EndX = Lvl.Planes.get(Plane).Vertices().get(3);
				float EndY = Lvl.Planes.get(Plane).Vertices().get(4);
				float EndZ = Lvl.Planes.get(Plane).Vertices().get(5);

				// Find another point. Can't find the direction of a wall if poinnts are above each other.
				if (StartX == EndX && StartY == EndY)
				{
					EndX = Lvl.Planes.get(Plane).Vertices().get(6);
					EndY = Lvl.Planes.get(Plane).Vertices().get(7);
				}

				// We'd like to have the opposite Z so we know the height of the wall
				if (StartZ == EndZ)
				{
					EndZ = Lvl.Planes.get(Plane).Vertices().get(8);
				}

				// Find the orthogonal vector (Invert X and Y, then set a negative Y)
				float OrthX = EndY;		// The opposite is 'StartX'
				float OrthY = -EndX;	// The opposite is 'StartY'
				float OrthAngle = (float)Math.atan2(StartY - OrthY, StartX - OrthX);

				float OrthPlayerStartX = NewX - (float)Math.cos(OrthAngle) * (float)Radius();
				float OrthPlayerStartY = NewY - (float)Math.sin(OrthAngle) * (float)Radius();

				float OrthPlayerEndX = NewX + (float)Math.cos(OrthAngle) * (float)Radius();
				float OrthPlayerEndY = NewY + (float)Math.sin(OrthAngle) * (float)Radius();

				// Cramer's rule
				// get_line_intersection(float p0_x, float p0_y, float p1_x, float p1_y, float p2_x, float p2_y, float p3_x, float p3_y, float *i_x, float *i_y)
				float WallDiffX = EndX - StartX;	// Vector's X from (0,0)
				float WallDiffY = EndY - StartY;	// Vector's Y from (0,0)
				float PlayerWallOrthDiffX = OrthPlayerEndX - OrthPlayerStartX;	// Vector's X orthogonal to the wall for the player from (0,0)
				float PlayerWallOrthDiffY = OrthPlayerEndY - OrthPlayerStartY;	// Vector's Y orthogonal to the wall for the player from (0,0)

				float PointWall = (-WallDiffY * (StartX - OrthPlayerStartX) + WallDiffX * (StartY - OrthPlayerStartY)) / (-PlayerWallOrthDiffX * WallDiffY + WallDiffX * PlayerWallOrthDiffY);
				float PointPlayerOrth = (PlayerWallOrthDiffX * (StartY - OrthPlayerStartY) - PlayerWallOrthDiffY * (StartX - OrthPlayerStartX)) / (-PlayerWallOrthDiffX * WallDiffY + WallDiffX * PlayerWallOrthDiffY);

				// Check if a collision is detected
				if (PointWall >= 0 && PointWall <= 1 && PointPlayerOrth >= 0 && PointPlayerOrth <= 1)
				{
					// Collision detected
					float CollX = StartX + (PointPlayerOrth * WallDiffX);
					float CollY = StartY + (PointPlayerOrth * WallDiffY);

					float Distance = (float)Math.sqrt(Math.pow(NewX - CollX, 2) + Math.pow(NewY - CollY, 2));

					if (Distance <= Radius())
					{
						System.err.println(Distance);

						return Lvl.Planes.get(Plane).GetAngle();
					}
				}

				// Check for a collision on the edge of a wall
				float Distance = (float)Math.sqrt(Math.pow(NewX - StartX, 2) + Math.pow(NewY - StartY, 2));
				if (Distance <= Radius())
				{
					return Lvl.Planes.get(Plane).GetAngle();
				}
				else
				{
					Distance = (float)Math.sqrt(Math.pow(NewX - EndX, 2) + Math.pow(NewY - EndY, 2));

					if (Distance <= Radius())
					{
						return Lvl.Planes.get(Plane).GetAngle();
					}
				}

			}
		}

		// The player doesn't deviate. Its movement is not divergeant.
		//return (float)Math.atan2(MoY(), MoX());
		return Float.NaN;
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

	public float LimitPlayerSpeedX(float DirX)
	{
		// Test if the player is moving too fast
		float Velocity = 0;
		if ((Velocity = (float)Math.sqrt(Math.pow(MoX(), 2) + Math.pow(MoY(), 2))) > MaxRunSpeed)
		{
			//float MovementDirection = (float) Math.atan2(MoY(), MoX());

			//float X = Math.abs((float) Math.sin(MovementDirection));

			float Factor = Velocity / MaxRunSpeed;

			return MoX / Factor;    // X factor
		}

		return DirX;
	}

	public float LimitPlayerSpeedY(float DirY)
	{
		// Test if the player is moving too fast
		float Velocity = 0;
		if ((Velocity = (float)Math.sqrt(Math.pow(MoX(), 2) + Math.pow(MoY(), 2))) > MaxRunSpeed)
		{
			//float MovementDirection = (float) Math.atan2(MoY(), MoX());

			//float Y = Math.abs((float) Math.cos(MovementDirection));

			float Factor = Velocity / MaxRunSpeed;

			return MoY / Factor;    // Y factor
		}

		return DirY;
	}

	public void LimitPlayerSpeed()
	{
		// Test if the player is moving too fast
		float Velocity = 0;
		if ((Velocity = (float)Math.sqrt(Math.pow(MoX(), 2) + Math.pow(MoY(), 2))) > MaxRunSpeed)
		{
			float MovementDirection = (float)Math.atan2(MoY(), MoX());

			// Fix movement direction so we use a system that is compatible with our other angles
			/*if (MovementDirection < 0)
			{
				 MovementDirection += Math.PI * 2;
			}*/

			// Fraction du mouvement qui appartient à chaques directions

			//float X = Math.abs((float)Math.sin(MovementDirection));
			//float Y = Math.abs((float)Math.cos(MovementDirection));

			float Factor = Velocity / MaxRunSpeed;

			float MaxX = MoX / Factor;	// X factor
			float MaxY = MoY / Factor;	// Y factor

			MoX = MaxX;
			MoY = MaxY;
		}
	}

	public void Move()
	{
		// Max sure the player's speed is not bigger than the maximum allowed
		LimitPlayerSpeed();

		// Change the postion according to the direction of the movement
		PosX += MoX();
		PosY += MoY();

		// Constant deceleration
		if (MoX != 0)
		{
			MoX /= Deceleration;
		}
		if (MoY != 0)
		{
			MoY /= Deceleration;
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

	public int Height()
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

	public int Radius()
	{
		return Radius;
	}
}

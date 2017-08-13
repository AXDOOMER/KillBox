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

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

public class Player
{
	// Position
	float PosX;	// Horizontal position
	float PosY;	// Vertical position
	float PosZ;	// Height, do not mix with Y.
	short Angle = 8192;	// Angles, from -16384 to 16383.

	// Weapon
	final int MaxOwnedWeapons = 10;
	int SelectedWeapon = 1;
	int WeaponToSelect = 1;
	int WeaponToUse = 1;
	Thing.Names[] OwnedWeapons = new Thing.Names[MaxOwnedWeapons];
	Texture SelectedWeaponSprite;
	Texture GunFire;
	int[] MaxBulletsPerWeapon = {0, 10, 20, 30};
	float[] WeaponAccuracy = {0, 1 * (float)Math.PI / 180, 5 * (float)Math.PI / 180, 10 * (float)Math.PI / 180};
	int[] WeaponSpeed = {0, -1, 4, 2};
	int WeaponTimeSinceLastShot = GetHighestWeaponSpeedFromArray() + 1;
	int Bullets = MaxBulletsPerWeapon[1];
	public boolean TriggerAlreadyPressed = false;

	int Action = 0;
	int Tick = 0;
	int MinTickBeforeCanMove = 5;

	// Motion
	float MoX = 0;
	float MoY = 0;
	float MoZ = 0;
	boolean HasMoved = false;

	final int MaxWalkSpeed = 40/8;
	final int MaxRunSpeed = 70/8;
	final int DefaultViewZ = 42;
	final int HeadOnFloor = 12;
	int ViewZ = DefaultViewZ;

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
	int MaxHealth = 100;
	int Health = MaxHealth;

	// Scores
	int Kills = 0;
	int Deaths = 0;
	int FlagTime = 0;
	int Hits = 0;
	int Misses = 0;

	byte FrontMove = 0;
	byte SideMove = 0;
	short AngleDiff = 0;
	boolean Shot = false;
	boolean JustShot = false;	// Used in camera for the gun fire
	int WeaponHeight = 0;
	boolean Reloading = false;
	boolean RaisingWeapon = false;		// When the weapon is moving down on the screen
	boolean DroppingWeapon = false;		// When the weapon is moving up on the screen
	boolean JustSpawned = false;
	final int WeaponActionSpeed = 5;

	Player LastHit = null;		// Last player that shot this player

	boolean HasFlag = false;	// For the flagtag game mode
	int Frame = 0;
	int LastFrame = 0;
	Sound Emitter = null;	// Must get the already initialized SndDriver
	Level Lvl = null;		// The player must know where he is
	Random Randomizer = null;
	final int MaxTriesBeforeFreeSpawnIsFound = 30;
	boolean Ghost = false;

	public Player(Level Lvl, Sound Output)
	{
		this.Lvl = Lvl;
		Emitter = Output;

		// Initialize array
		for (int i = 0; i < MaxOwnedWeapons; i++)
		{
			OwnedWeapons[i] = null;
		}

		// The player always has a pistol (weapon index matches with the numerical key with which it is selected)
		OwnedWeapons[1] = Thing.Names.Pistol;
		SelectedWeaponSprite = new Texture("res/weapons/pistol.png", Game.WallsFilter);
		GunFire = new Texture("res/sprites/gunfire.png", Game.WallsFilter);

		// Create a reference to the pseudo random number generator
		Randomizer = new Random();
	}

	public int GetHighestWeaponSpeedFromArray()
	{
		int Max = 0;
		for (int Index = 0; Index < WeaponSpeed.length; Index++)
		{
			if (WeaponSpeed[Index] > Max)
			{
				Max = WeaponSpeed[Index];
			}
		}

		return Max;
	}

	public void LoadSprites()
	{
		// Load Sprites for the Players
		final int LastFrame = 'G';
		for (char Frame = 'A'; Frame <= LastFrame; Frame++)
		{
			for (int Rotation = 1; Rotation <= Rotations; Rotation++)
			{
				Texture NextFrame = new Texture("res/player/PLAY" + Frame + Rotation + ".png", GL_NEAREST);
				WalkFrames.add(NextFrame);
			}
		}

		// Death frames
		for (char Frame = 'H'; Frame <= 'N'; Frame++)
		{
			Texture NextFrame = new Texture("res/player/PLAY" + Frame + "0.png", GL_NEAREST);
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
		Emitter.PlaySound("hurt.wav", this);

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

	// For the network code only
	public int ActionIsHasShot()
	{
		// Check if player has shot
		if (Shot)
		{
			// Reset the state of this action
			Shot = false;

			return 100;
		}
		else
		{
			return 0;
		}
	}

	// For the network code only
	public int ActionIsHasReload()
	{
		// Check if player has reloaded
		if (Reloading)
		{
			// Reset the state of this action
			Reloading = false;

			return 1000;
		}
		else
		{
			return 0;
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

	public void SetShotTrue()
	{
		if (CanShot())
		{
			if (WeaponSpeed[SelectedWeapon] < 0)
			{
				// Pistol
				if (!TriggerAlreadyPressed)
				{
					Shot = true;
					WeaponTimeSinceLastShot = 0;
				}
			}
			else
			{
				// Tek9 and Ak47
				if (WeaponTimeSinceLastShot >= WeaponSpeed[SelectedWeapon])
				{
					// If it wasn't shot for sometime, it can shot again.
					Shot = true;
					WeaponTimeSinceLastShot = 0;
				}
			}
		}
	}

	public Player HitScan(float HorizontalAngle, float VerticalAngle, int Damage)
	{
		// Check if the weapon is ready to shot
		if (this.CanShot())
		{
			// Play the good sound for the selected weapon
			switch (SelectedWeapon)
			{
				case 1:
					Emitter.PlaySound("pistol.wav", this);
					break;
				case 2:
					Emitter.PlaySound("assaultrifle.wav", this);
					break;
				case 3:
					Emitter.PlaySound("ak47.wav", this);
					break;
			}

			WeaponTimeSinceLastShot = 0;
			Bullets--;

			float Step = 4;        // Incremental steps at which the bullet checks for collision
			float BulletRadius = Step;
			int MaxChecks = 2048;        // Max check for the reach of a bullet
			Shot = true;    // Set shot property tot he player so it's transmitted over the network
			JustShot = true;    // Set to true so the gun fire is displayed in the camera

			// Start scanning from the player's position
			float TravelX = this.PosX();
			float TravelY = this.PosY();
			float TravelZ = this.ViewZ;

			// Check if the bullet could hit a player
			Player Which = null;
			float Distance = 0;		// Zero is ok, because when the hit fails, the next calculations are not done.
			for (int Point = 0; Point < MaxChecks; Point++)
			{
				// Increment bullet position
				TravelX = TravelX + Step * (float) Math.cos(HorizontalAngle);
				TravelY = TravelY + Step * (float) Math.sin(HorizontalAngle);
				//TravelZ = TravelZ + Step * (float)Math.sin(VerticalAngle);

				Player Hit = PointInPlayer(TravelX, TravelY, TravelZ);

				if (Hit != null && Hit.Health > 0)
				{
					Which = Hit;
					Distance = Point * Step;
					break;
				}
			}

			// If a player could be hit, check if the bullet would hit a wall before it would hit the player.
			if (Which != null)
			{
				// Reset the position to scan from the player's position
				TravelX = this.PosX();
				TravelY = this.PosY();

				// Use bigger steps for walls
				if (Step <= 4)
				{
					Step *= 2;
				}

				// Move the bullet and check for collision
				for (int Point = 0; Point < MaxChecks; Point++)
				{
					// Increment bullet position
					TravelX = TravelX + Step * (float) Math.cos(HorizontalAngle);
					TravelY = TravelY + Step * (float) Math.sin(HorizontalAngle);
					//TravelZ = TravelZ + Step * (float)Math.sin(VerticalAngle);

					// Check if a wall was hit. Check for wall on a line between the player and the hit point.
					if (Point * Step >= Distance)
					{
						// The bullet has been able to go far enough (not hitting walls) so it could hit the player.
						Player Hit = Which;

						// Check if something was really hit
						if (Hit != null && Hit.Health > 0)
						{
							// Spawn blood
							Lvl.Things.add(new Thing("Blood",
									this.PosX() + ((Distance - Radius() / 4) * (float) Math.cos(HorizontalAngle)) + (Randomizer.GiveNumber() % 5) - 2,
									this.PosY() + ((Distance - Radius() / 4) * (float) Math.sin(HorizontalAngle)) + (Randomizer.GiveNumber() % 5) - 2,
									Hit.ViewZ - (Randomizer.GiveNumber() % 5)));

							// If the player who was hit is not dead
							if (Hit.Health > 0)
							{
								// Damage him
								Hit.DamageSelf(Damage, this.PosX(), this.PosY());

								// Bullet has hit
								Hits++;

								// If he's dead
								if (Hit.Health <= 0)
								{
									// Got a point
									Kills++;

									// Play death sound
									Emitter.PlaySound("death.wav", Hit);

									// Create a corpse
									Lvl.Things.add(new Thing("DeadPlayer", Hit.PosX(), Hit.PosY(), Hit.PosZ()));

									// Add one death to his counter
									Hit.Deaths++;
								}

								return Hit;
							}
						}
					}
					else if (CheckWallCollision(TravelX, TravelY, BulletRadius) != null)
					{
						// A wall was hit.
						break;
					}
				}
			}

			// Bullet missed
			Misses++;
		}

		return null;
	}

	public void ForwardMove(byte Direction)
	{
		if (ShouldMove())
		{
			// Cancels the opposite direction when both keys are held
			if (FrontMove == -Direction)
			{
				FrontMove = 0;
			}
			else
			{
				FrontMove = Direction;

				if (Frame - LastFrame <= 0)
				{
					Frame++;
				}
			}
		}
	}

	public void ExecuteMove(byte FrontDirection, byte SideDirection)
	{
		if (ShouldMove())
		{
			// Relative position for movement
			float NewX = MoX;
			float NewY = MoY;

			// Frontal or backward movement
			if (FrontDirection > 0)
			{
				if (Health > 0)
				{
					NewX = MoX + Acceleration * (float) Math.cos(GetRadianAngle());
					NewY = MoY + Acceleration * (float) Math.sin(GetRadianAngle());
				}
			}
			else if (FrontDirection < 0)
			{
				if (Health > 0)
				{
					NewX = MoX - Acceleration * (float) Math.cos(GetRadianAngle());
					NewY = MoY - Acceleration * (float) Math.sin(GetRadianAngle());
				}
			}
			// Don't do frontmove when 'Direction' is equal to zero

			// Lateral movement
			float AdjustedAngle = GetRadianAngle() - (float) Math.PI / 2;

			if (SideDirection > 0)
			{
				if (Health > 0)
				{
					NewX += MoX + Acceleration * (float) Math.cos(AdjustedAngle);
					NewY += MoY + Acceleration * (float) Math.sin(AdjustedAngle);
				}
			}
			else if (SideDirection < 0)
			{
				if (Health > 0)
				{
					NewX += MoX - Acceleration * (float) Math.cos(AdjustedAngle);
					NewY += MoY - Acceleration * (float) Math.sin(AdjustedAngle);
				}
			}
			// Don't sidemove when 'SideDirection' is equal to zero

			// Change the position
			float OldX = PosX;
			float OldY = PosY;

			if (FrontDirection != 0 || SideDirection != 0)
			{
				TryMove(NewX, NewY);
			}

			// Flag so it is known that the player wants to move
			HasMoved = true;
		}
	}

	public void LateralMove(byte Direction)
	{
		if (ShouldMove())
		{
			// Cancels the opposite direction
			if (SideMove == -Direction)
			{
				SideMove = 0;
			}
			else
			{
				SideMove = Direction;

				if (Frame - LastFrame <= 0)
				{
					Frame++;
				}
			}
		}
	}

	// Is the player a ghost that can walk through walls?
	public void SetNoClipping(boolean NoClipping)
	{
		Ghost = NoClipping;
	}

	// Try every type of collision.
	public boolean TryMove(float NewX, float NewY)
	{
		float Current = (float)Math.atan2(MoY(), MoX());
		// Should return the current angle if it's possible to move, else return another...

		boolean Clear = true;	// Can move
		float PushAngle = Float.NaN;
		Plane HitWall = null;

		// Fuck it when the player repetitively can't move
		int NumTests = 0;

		if (!Ghost)
		{
			while (Clear && NumTests < this.Radius() * 2)
			{
				NumTests++;

				// Player against player collision. (Note: if(Float.NaN==Float.NaN) doesn't work)
				if (!Float.isNaN(PushAngle = CheckPlayerToPlayerCollision(NewX + PosX(), NewY + PosY())))
				{
					Clear = false;
				}

				// Player against thing collision
				if (!Float.isNaN(PushAngle = CheckPlayerToThingsCollision(NewX + PosX(), NewY + PosY())))
				{
					Clear = false;
				}

				// Player against wall collision
				if (null != (HitWall = CheckWallCollision(NewX + PosX(), NewY + PosY(), this.Radius())))
				{
					// Slide against the wall (work in progress)
/*				float atan = (float)Math.atan2(MoY(), MoX());

				float cosaX = (float)Math.cos(atan);
				float sinaY = (float)Math.sin(atan);

				// HACK
				if (NewX < 1 && NewX > -1)
					NewX = 0.1f;
				if (NewY < 1 && NewY > -1)
					NewY = 0.1f;

				NewX = (float)Math.abs(cosaX) * NewX;
				NewY = (float)Math.abs(sinaY) * NewY;

				//NewX /= 1.3;
				//NewY /= 1.3;
//
//				if (NewX < 1 && NewX > -1)
//					NewX = 0;
//				if (NewY < 1 && NewY > -1)
//					NewY = 0;
//
				if (null == (HitWall = CheckWallCollision(NewX + PosX(), NewY + PosY(), this.Radius())))
				{
					if (Clear)
					{
						Clear = true;
					}
				}
				else
				{
					int Iteration = 0;
					while (null != (HitWall = CheckWallCollision(NewX + PosX(), NewY + PosY(), this.Radius())))
					{
						System.out.println("STUCK!!! Iteration: " + Iteration++);
						NewX /= 2;
						NewY /= 2;

						// This is a HACK so sliding works a bit on the Y axis
						if (NewX < 1 && NewX > -1)
							NewX = 0;
						if (NewY < 1 && NewY > -1)
							NewY = 0.1f;
					}

					Clear = true;
				}*/
					Clear = false;
				}

				if (!Clear)
				{
					// Divide movement, because we want to move less.
					NewX /= 2;
					NewY /= 2;
					Clear = true;
				}
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
			// If the player is already in another player, he's already fucked.

			// TO-DO: Make the player slide against the other's sides [DEVIATE THE MOMENTUM!!!]

			//MoX += (float)Math.cos(PushAngle);
			//MoY += (float)Math.sin(PushAngle);
		}

		// Move to the new position. This also limits the player's speed.
		Move();

		return Clear;
	}

	public int Frame()
	{
		return Frame;
	}

	// Check collision against things
	public float CheckPlayerToThingsCollision(float NewX, float NewY)
	{
		for (int Thingie = 0; Thingie < Lvl.Things.size(); Thingie++)
		{
			// Delete dead player corpses from memory while we're doing this
			// Delete objects with a negative frame (player corpses are set to -1)
			if (Lvl.Things.get(Thingie).Frame == -1)
			{
				Lvl.Things.remove(Thingie);

				// Go to the next iteration, because the element is deleted...
				continue;
			}

			// Distance calculation for the 2D collision test
			float Distance = (float) Math.sqrt(
					Math.pow(NewX - Lvl.Things.get(Thingie).PosX(), 2) +
							Math.pow(NewY - Lvl.Things.get(Thingie).PosY(), 2));

			// Test 2D collision
			if (Distance <= this.Radius() + Lvl.Things.get(Thingie).Radius())
			{
				// Test the Z axis. Both players have the same height.
				if (Math.abs(this.PosZ() - Lvl.Things.get(Thingie).PosZ()) <= Height())
				{
					// Check if the thing is set to block other things
					if (Lvl.Things.get(Thingie).Impassable)
					{
						// If the thing becomes invisible, it can't block.
						if (Lvl.Things.get(Thingie).Sprite != null)
						{
							// Collision! Return the angle toward the other player.

							float Glide = (float) Math.atan2(Lvl.Things.get(Thingie).PosY() - PosY + NewY, Lvl.Things.get(Thingie).PosX() - PosX + NewX);

							return Glide - GetRadianAngle();
						}
					}
					else
					{
						if (Lvl.Things.get(Thingie).Type != null)
						{
							// If it wasn't impassable, then may be it can be picked up.
							if (Lvl.Things.get(Thingie).Type.equals(Thing.Names.Ak47))
							{
								boolean Found = false;
								for (int Weapon = 0; Weapon < MaxOwnedWeapons; Weapon++)
								{
									if (OwnedWeapons[Weapon] != null && OwnedWeapons[Weapon].equals(Thing.Names.Ak47))
									{
										Found = true;
									}
								}
								if (!Found)
								{
									OwnedWeapons[3] = Thing.Names.Ak47;
									ChangeWeapon(3);
									Emitter.PlaySound("cocking.wav", this);
									//Lvl.Things.remove(Thingie);	// Delete the thingy
								}
							}
							else if (Lvl.Things.get(Thingie).Type.equals(Thing.Names.Tek9))
							{
								boolean Found = false;
								for (int Weapon = 0; Weapon < MaxOwnedWeapons; Weapon++)
								{
									if (OwnedWeapons[Weapon] != null && OwnedWeapons[Weapon].equals(Thing.Names.Tek9))
									{
										Found = true;
									}
								}
								if (!Found)
								{
									OwnedWeapons[2] = Thing.Names.Tek9;
									ChangeWeapon(2);
									Emitter.PlaySound("cocking.wav", this);
								}
							}
							else if (Lvl.Things.get(Thingie).Type.equals(Thing.Names.Flag))
							{
								if (Health > 0)
								{
									// Player has the flag!
									HasFlag = true;
									// Remove from the map
									Lvl.Things.remove(Thingie);
									// Play a special pickup sound
									Emitter.PlaySound("chat.wav", this);
								}
							}
						}
					}
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
				// Don't collide with self.
				continue;
			}

			if (Lvl.Players().get(Player).Health <= 0)
			{
				// Player is dead, so don't collide with it.
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
	public Plane CheckWallCollision(float NewX, float NewY, float RadiusToUse)
	{
		// Test collision against each planes
		for (int Plane = 0; Plane < Lvl.Planes.size(); Plane++)
		{
			// May be a floor or a straight ceiling, so don't care.
			if (!Lvl.Planes.get(Plane).CanBlock())
			{
				continue;
			}

			// Get the vector
			float StartX = Lvl.Planes.get(Plane).Coordinates[0];
			float StartY = Lvl.Planes.get(Plane).Coordinates[1];
			float EndX = Lvl.Planes.get(Plane).Coordinates[2];
			float EndY = Lvl.Planes.get(Plane).Coordinates[3];

			// Test the distance to one vertex and if there is a possibility that the player can hit the wall
			float WallLength = Lvl.Planes.get(Plane).FlatLength;
			float DistanceToOneWallVertex = (float)Math.sqrt(Math.pow(StartX - NewX, 2) + Math.pow(StartY - NewY, 2));

			// If the plane is close, it can possibly be collided with.
			if (DistanceToOneWallVertex <= WallLength + RadiusToUse)
			{
				// Get the orthogonal vector, so invert the use of 'sin' and 'cos' here. 
				float OrthPlayerStartX = NewX + (float) Math.sin(Lvl.Planes.get(Plane).GetAngle()) * RadiusToUse;
				float OrthPlayerStartY = NewY + (float) Math.cos(Lvl.Planes.get(Plane).GetAngle()) * RadiusToUse;
				float OrthPlayerEndX = NewX - (float) Math.sin(Lvl.Planes.get(Plane).GetAngle()) * RadiusToUse;
				float OrthPlayerEndY = NewY - (float) Math.cos(Lvl.Planes.get(Plane).GetAngle()) * RadiusToUse;

				// Cramer's rule, inspiration taken from here: https://stackoverflow.com/a/1968345
				float WallDiffX = EndX - StartX;    // Vector's X from (0,0)
				float WallDiffY = EndY - StartY;    // Vector's Y from (0,0)
				float PlayerWallOrthDiffX = OrthPlayerEndX - OrthPlayerStartX;
				float PlayerWallOrthDiffY = OrthPlayerEndY - OrthPlayerStartY;

				float Denominator = -PlayerWallOrthDiffX * WallDiffY + WallDiffX * PlayerWallOrthDiffY;
				float PointWall = (-WallDiffY * (StartX - OrthPlayerStartX) + WallDiffX * (StartY - OrthPlayerStartY)) / Denominator;
				float PointPlayerOrth = (PlayerWallOrthDiffX * (StartY - OrthPlayerStartY) - PlayerWallOrthDiffY * (StartX - OrthPlayerStartX)) / Denominator;

				// Check if a collision is detected (Also checking if equals to see if it's touching an endpoint)
				if (PointWall >= 0 && PointWall <= 1 && PointPlayerOrth >= 0 && PointPlayerOrth <= 1)
				{
					// Collision detected
					float CollX = StartX + (PointPlayerOrth * WallDiffX);
					float CollY = StartY + (PointPlayerOrth * WallDiffY);

					float Distance = (float) Math.sqrt(Math.pow(NewX - CollX, 2) + Math.pow(NewY - CollY, 2));

					if (Distance <= RadiusToUse)
					{
						return Lvl.Planes.get(Plane);
					}
				}

				// Check for a collision on the edge of a wall (using the radius of the player to the endpoints)
				float Distance = (float) Math.sqrt(Math.pow(NewX - StartX, 2) + Math.pow(NewY - StartY, 2));
				if (Distance <= RadiusToUse)
				{
					return Lvl.Planes.get(Plane);
				}
				else
				{
					// Second endpoint
					Distance = (float) Math.sqrt(Math.pow(NewX - EndX, 2) + Math.pow(NewY - EndY, 2));
					if (Distance <= RadiusToUse)
					{
						return Lvl.Planes.get(Plane);
					}
				}
			}
		}

		// No wall was hit
		return null;
	}

	public void LimitPlayerSpeed()
	{
		// Test if the player is moving too fast
		float Velocity = (float) Math.sqrt(Math.pow(MoX(), 2) + Math.pow(MoY(), 2));
		if (Velocity > MaxRunSpeed)
		{
			float Factor = Velocity / MaxRunSpeed;

			float MaxX = MoX / Factor;	// X factor
			float MaxY = MoY / Factor;	// Y factor

			MoX = MaxX;
			MoY = MaxY;
		}
	}

	public void Friction()
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

		// Restore current frame to zero because the player is not moving or not moving enough
		if (MoX < 1 && MoX > -1 && MoY < 1 && MoY > -1)
		{
			LastFrame = 0;
			Frame = 0;
		}
		else
		{
			LastFrame++;
		}

		TryMove(MoX, MoY);

	}

	// Bastard method
	public void Move()
	{
		// Max sure the player's speed is not bigger than the maximum allowed
		LimitPlayerSpeed();

		PosX += MoX();
		PosY += MoY();

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
		AngleDiff = AngleChange;
	}

	public void ExecuteAngleTurn(short AngleChange)
	{
		if (ShouldMove())
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

	public int GetDamageDoneBySelectedWeapon()
	{
		if (SelectedWeapon > 0)
		{
			if (OwnedWeapons[SelectedWeapon].equals(Thing.Names.Pistol))
			{
				return 10;
			}
			else if (OwnedWeapons[SelectedWeapon].equals(Thing.Names.Tek9))
			{
				return 5;
			}
			else if (OwnedWeapons[SelectedWeapon].equals(Thing.Names.Ak47))
			{
				return 15;
			}
			else
			{
				System.err.println("Player->GetDamageDoneBySelectedWeapon() says unknown weapon selected.");
				System.exit(1);
			}

			return 10;
		}
		else
		{
			System.err.println("Player->GetDamageDoneBySelectedWeapon() says no weapon selected.");
			System.exit(1);
		}

		return 0;
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

	// Game starts for player
	private void ResetPlayerForNewGame()
	{
		// Reset scores
		Kills = 0;
		Deaths = 0;
		FlagTime = 0;
		Hits = 0;
		Misses = 0;

		// Reset other properties
		ResetPlayerForRespawn();
	}

	// Come back to life
	private void ResetPlayerForRespawn()
	{
		Health = MaxHealth;
		ViewZ = DefaultViewZ;
		MoX = 0;
		MoY = 0;
		MoZ = 0;
		Bullets = MaxBulletsPerWeapon[1];
		Tick = 0;

		// Reset other stuff
		HasMoved = false;
		byte FrontMove = 0;
		byte SideMove = 0;
		short AngleDiff = 0;

		// Reset owned weapons
		for (int i = 0; i < MaxOwnedWeapons; i++)
		{
			OwnedWeapons[i] = null;
		}

		// The player always has a pistol (weapon index matches with the numerical key with which it is selected)
		OwnedWeapons[1] = Thing.Names.Pistol;
		SelectedWeapon = 1;
		WeaponToSelect = 1;
		SelectedWeaponSprite = new Texture("res/weapons/pistol.png", Game.WallsFilter);
	}

	public boolean SpawnAtRandomSpot(boolean MustBeFree)
	{
		Shot = true;	// Need this for the player to respawn in the other game

		// Spawn the player at a random location
		int RandomNumber = Randomizer.GiveNumber();
		int Tries = 0;

		Thing SomeSpawn = Lvl.Spawns.get(RandomNumber % Lvl.Spawns.size());

		while (!this.SpawnAtLocation(SomeSpawn.PosX(), SomeSpawn.PosY(), SomeSpawn.PosZ(), SomeSpawn.Angle, MustBeFree))
		{
			RandomNumber = Randomizer.GiveNumber();
			SomeSpawn = Lvl.Spawns.get(RandomNumber % Lvl.Spawns.size());

			if (Tries > MaxTriesBeforeFreeSpawnIsFound)
			{
				return false;
			}
			Tries++;
		}
		this.JustSpawned = true;
		return true;
	}

	private boolean SpawnAtLocation(float X, float Y, float Z, short Angle, boolean MustBeFree)
	{
		boolean FreeSpace = true;
		for (int Player = 0; Player < Lvl.Players.size(); Player++)
		{
			if (MustBeFree)
			{
				float Distance = (float) Math.sqrt(Math.pow(Lvl.Players.get(Player).PosX() - X, 2) + Math.pow(Lvl.Players.get(Player).PosY() - Y, 2));

				if (Distance <= this.Radius() * 2 && Math.abs(Lvl.Players.get(Player).PosZ() - Z) <= this.Height())
				{
					FreeSpace = false;
				}
			}
		}

		// "MustBeFree" is false when spawns are set for a new game
		if (!MustBeFree)
		{
			ResetPlayerForNewGame();
		}

		if (FreeSpace == true)
		{
			ResetPlayerForRespawn();

			this.Angle = Angle;
			PosX = X;
			PosY = Y;
			PosZ = Z;
		}

		return FreeSpace;
	}

	public void UpdateIfDead()
	{
		if (Health <= 0)
		{
			// Drop the flag if the player has it
			if (HasFlag)
			{
				Lvl.Things.add(new Thing("Flag", this.PosX(), this.PosY(), this.PosZ()));
				HasFlag = false;
			}

			// Drop the head on the ground
			if (ViewZ > HeadOnFloor)
			{
				ViewZ--;
			}
		}
	}

	public void UpdateFlagTime()
	{
		if (HasFlag)
		{
			FlagTime++;
		}
	}

	public void UpdateTickCount()
	{
		Tick++;
	}

	// Used to prevent the player from moving right after a respawn
	public boolean ShouldMove()
	{
		return Tick >= MinTickBeforeCanMove;
	}

	public void UpdateTimeSinceLastShot()
	{
		WeaponTimeSinceLastShot++;
	}

	public int DifferenceViewZ()
	{
		return DefaultViewZ - ViewZ;
	}

	public void ChangeWeapon(int Weapon)
	{
		if (Health > 0)
		{
			if (!RaisingWeapon)
			{
				if (Weapon >= 0 && Weapon < MaxOwnedWeapons)
				{
					if (OwnedWeapons[Weapon] != null)
					{
						// Only load the weapon texture if the player changed weapon. Else, no need to reload it again.
						if (Weapon != SelectedWeapon && Weapon != WeaponToSelect)
						{
							WeaponToUse = Weapon;
							WeaponToSelect = Weapon;
							Reloading = true;
							DroppingWeapon = true;

							// We don't want the player to shot
							Shot = false;
							JustShot = false;
							//Emitter.PlaySound("reload.wav", this);
						}
					}
				}
			}
		}
	}

	public boolean CanShot()
	{
		// Check if the weapon is at the height it should be when it's ready to fire
		// And check if the weapon is not being dropped or raised.
		if (WeaponHeight == 0 && !DroppingWeapon && !RaisingWeapon && Health > 0)
		{
			return true;
		}

		return false;
	}

	public void ExecuteChangeWeapon()
	{
		if (DroppingWeapon)
		{
			WeaponHeight -= WeaponActionSpeed;

			if (WeaponHeight <= -DefaultViewZ)
			{
				DroppingWeapon = false;
				RaisingWeapon = true;
			}
		}
		else if (RaisingWeapon)
		{
			WeaponHeight += WeaponActionSpeed;

			if (WeaponHeight >= 0)
			{
				WeaponHeight = 0;
				RaisingWeapon = false;
				Reloading = false;
			}
		}

		if (RaisingWeapon && WeaponHeight <= -DefaultViewZ)
		{
			if (WeaponToSelect != SelectedWeapon)
			{
				SelectedWeapon = WeaponToSelect;

				switch (SelectedWeapon)
				{
					case 1:
						SelectedWeaponSprite = new Texture("res/weapons/pistol.png", Game.WallsFilter);
						Bullets = MaxBulletsPerWeapon[1];
						break;
					case 2:
						SelectedWeaponSprite = new Texture("res/weapons/tek9rifle.png", Game.WallsFilter);
						Bullets = MaxBulletsPerWeapon[2];
						break;
					case 3:
						SelectedWeaponSprite = new Texture("res/weapons/ak47.png", Game.WallsFilter);
						Bullets = MaxBulletsPerWeapon[3];
						break;
				}
			}
		}
	}

	public void ReloadWeapon()
	{
		// Check if the weapon is already being reloaded or if its being changed
		// Use the "CanShot" method.
		if (CanShot())
		{
			if (Bullets != MaxBulletsPerWeapon[SelectedWeapon])
			{
				Bullets = MaxBulletsPerWeapon[SelectedWeapon];
				Emitter.PlaySound("reload.wav", this);

				// This activates the weapon reload animation
				DroppingWeapon = true;
			}
		}
	}

	public boolean JustShot()
	{
		if (JustShot)
		{
			JustShot = false;
			return true;
		}

		return false;
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
		PosZ = PosZ + 8;
	}

	public void MoveDown()
	{
		PosZ = PosZ - 8;
	}

	public void MakesNoise(String Sound)
	{
		Emitter.PlaySound(Sound);
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

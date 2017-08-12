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

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

public class Camera
{
	// Attribute
	Player Plyr;
	Menu Menu = new Menu();

	// Positions inside the camera are as represented as in OpenGL
	// The axis are inverted, thus a positive value is a negative value
	private float PosX;
	private float PosY;
	private float PosZ;
	private float RotX;
	private float RotY;
	private float RotZ;

	// Controls the mouse sensitivity
	public float MouseSensitivity = 1.0f;

	private float FOV;
	private float Aspect;
	private float Near;
	private float Far;

	public Texture CurrentTexture = null;
	private Texture Crosshair = new Texture("res/sprites/crosshair.png", Game.WallsFilter);
	private final int[] TextXcoords = {0, 1, 1, 0};    // CLEAN ME
	private final int[] TextYcoords = {1, 1, 0, 0};    // CLEAN ME
	public boolean TextureFiltered = false;
	public boolean DisplayModeChanged = false;

	private boolean HasControl = false;
	private boolean MenuKeyPressed = false;
	public boolean DemoMode = false;
	public boolean TestingMap = false;

	// Key presses
	private boolean JustPressedFilterKey = false;
	private boolean JustPressedMouseGrabKey = false;
	private boolean JustPressedMenuKey = false;
	private boolean JustPressedFireKey = false;

	// Mouse movement
	private short MouseTurnH;
	private short MouseVertical;

	public Camera(Player Plyr, float FOV, float Aspect, float Near, float Far)
	{
		this.Plyr = Plyr;

		PosX = Plyr.PosY;
		PosY = Plyr.PosZ + Plyr.ViewZ;   // Internally, the player's Z is the height.
		PosZ = Plyr.PosX;   // But in OpenGL, Z is vertical like the internal Y.
		RotX = 0;
		RotY = Plyr.GetDegreeAngle();
		RotZ = 0;

		this.FOV = FOV;
		this.Aspect = Aspect;
		this.Near = Near;
		this.Far = Far;
		InitProjection();
	}

	// Sets the perspective without glu, so let's call it glPerspective. 
	private void glPerspective(float FOV, float Aspect, float Near, float Far)
	{
		// This replaces 'gluPerspective'. 
		float FH = (float) Math.tan(FOV / 360 * Math.PI) * Near;
		float FW = FH * Aspect;

		// Sets the Frustum to perspective mode. 
		glFrustum(-FW, FW, -FH, FH, Near, Far);
	}

	private void InitProjection()
	{
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glPerspective(FOV, Aspect, Near, Far);
		glMatrixMode(GL_MODELVIEW);
	}

	public void UseView()
	{
		//glRotatef(RotX, 1, 0, 0);
		glRotatef(RotY, 0, -1, 0);
		//glRotatef(RotZ, 0, 0, 1);
		//glTranslatef(PosX, PosY, PosZ);
	}

	public void UpdateCamera()
	{
		PosX = Plyr.PosY;
		PosY = Plyr.PosZ + Plyr.ViewZ;
		PosZ = Plyr.PosX;
		RotY = Plyr.GetDegreeAngle();
	}

	public void ChangePlayer(Player Plyr, boolean CanControl)
	{
		this.Plyr = Plyr;
		HasControl = CanControl;
	}

	public void ChangeProperties(float FOV, float Aspect, float Near, float Far)
	{
		this.FOV = FOV;
		this.Aspect = Aspect;
		this.Near = Near;
		this.Far = Far;
	}

	public Player CurrentPlayer()
	{
		return Plyr;
	}

	public void RunLogic(Level Lvl, ArrayList<Player> Players)
	{
		// Grab mouse is controller by F1
		if (Keyboard.isKeyDown(Keyboard.KEY_F1) && !JustPressedMouseGrabKey)
		{
			JustPressedMouseGrabKey = true;

			if (!Mouse.isGrabbed())
			{
				Mouse.setGrabbed(true); // Hide mouse
				Menu.GrabMouse(true);
			}
			else
			{
				Mouse.setGrabbed(false);
				Menu.GrabMouse(false);
			}

			Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
		}
		else if (Keyboard.isKeyDown(Keyboard.KEY_F1))
		{
			JustPressedMouseGrabKey = true;
		}
		else
		{
			JustPressedMouseGrabKey = false;
		}

		if (Mouse.isGrabbed())
		{
			MouseTurnH = (short) (Mouse.getDX() * MouseSensitivity);
			MouseVertical = (short) (Mouse.getDY() * MouseSensitivity);
		}
		else
		{
			MouseTurnH = 0;
			MouseVertical = 0;
		}

		// This will only be changed the next time a level will load
		if (Keyboard.isKeyDown(Keyboard.KEY_F5) && !JustPressedFilterKey)
		{
			if (TextureFiltered)
			{
				//Door = new Texture("res/DOOR9_1.bmp", GL_NEAREST);    // Only to test
				TextureFiltered = false;
			}
			else
			{
				//Door = new Texture("res/DOOR9_1.bmp", GL_LINEAR);    // Only to test
				TextureFiltered = true;
			}

			//Door.Bind();
			JustPressedFilterKey = true;
		}
		else if (Keyboard.isKeyDown(Keyboard.KEY_F5))
		{

			JustPressedFilterKey = true;
		}
		else
		{
			JustPressedFilterKey = false;
		}

		if (HasControl && !Menu.Active())    // If I am this player
		{
			if (Plyr.ShouldMove())
			{
				//CurrentPlayer().ForwardMove((byte) (MouseVertical / 5));

				// If keys for opposite movements are held, don't do anything.
				if (!((Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_UP))
						&& (Keyboard.isKeyDown(Keyboard.KEY_S) || Keyboard.isKeyDown(Keyboard.KEY_DOWN))))
				{
					if (Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_UP))
					{
						CurrentPlayer().ForwardMove((byte) 1);
					}
					if (Keyboard.isKeyDown(Keyboard.KEY_S) || Keyboard.isKeyDown(Keyboard.KEY_DOWN))
					{
						CurrentPlayer().ForwardMove((byte) -1);
					}
				}

				// If keys for opposite movements are held, don't do anything.
				if (!(Keyboard.isKeyDown(Keyboard.KEY_A) && Keyboard.isKeyDown(Keyboard.KEY_D)))
				{
					if (Keyboard.isKeyDown(Keyboard.KEY_A))
					{
						CurrentPlayer().LateralMove((byte) -1);
					}
					if (Keyboard.isKeyDown(Keyboard.KEY_D))
					{
						CurrentPlayer().LateralMove((byte) 1);
					}
				}

				// If both keys are held at the same time, don't do anything.
				if (!(Keyboard.isKeyDown(Keyboard.KEY_LEFT) && Keyboard.isKeyDown(Keyboard.KEY_RIGHT)))
				{
					if (Plyr.ShouldMove())
					{
						if (Keyboard.isKeyDown(Keyboard.KEY_LEFT))
						{
							CurrentPlayer().AngleTurn((short) 500);
						}
						else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
						{
							CurrentPlayer().AngleTurn((short) -500);
						}
						else
						{
							CurrentPlayer().AngleTurn((short) -(MouseTurnH * 20));
						}
					}
				}
				if (Keyboard.isKeyDown(Keyboard.KEY_SPACE))
				{
					//CurrentPlayer().MoveUp();
				}
				if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
				{
					//CurrentPlayer().MoveDown();
				}

				// Right now, it can only shot like a pistol...
				if ((Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) ||
						Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || (Mouse.isGrabbed() && Mouse.isButtonDown(0))) && !JustPressedFireKey)
				{
					if (CurrentPlayer().Health > 0 && CurrentPlayer().Bullets > 0)
					{
						//CurrentPlayer().HitScan(CurrentPlayer().GetRadianAngle(), 0, 10);
						CurrentPlayer().SetShotTrue();
						Plyr.TriggerAlreadyPressed = true;
					}
					else
					{
						JustPressedFireKey = true;

						// Check if the player has completely dropped on the floor
						if (CurrentPlayer().ViewZ == CurrentPlayer().HeadOnFloor)
						{
							// Spawn the player
							if (!CurrentPlayer().SpawnAtRandomSpot(true))
							{
								System.err.println("Can't find a free spot to respawn. The map may not have enough of them.");
								System.exit(1);
							}
							else
							{
								CurrentPlayer().Emitter.PlaySound("respawn.wav", CurrentPlayer());
							}
						}
					}
				}
				else if (Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) ||
						Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || (Mouse.isGrabbed() && Mouse.isButtonDown(0)))
				{
					JustPressedFireKey = true;
					Plyr.TriggerAlreadyPressed = true;
				}
				else
				{
					JustPressedFireKey = false;
					Plyr.TriggerAlreadyPressed = false;
				}

				if (Keyboard.isKeyDown(Keyboard.KEY_F10))
				{
					Menu.UserWantsToExit = true;
				}

				if (Keyboard.isKeyDown(Keyboard.KEY_R))
				{
					CurrentPlayer().Reloading = true;
				}
				// Change weapon
				if (Keyboard.isKeyDown(Keyboard.KEY_1))
				{
					CurrentPlayer().WeaponToUse = 1;
				}
				else if (Keyboard.isKeyDown(Keyboard.KEY_2))
				{
					CurrentPlayer().WeaponToUse = 2;
				}
				else if (Keyboard.isKeyDown(Keyboard.KEY_3))
				{
					CurrentPlayer().WeaponToUse = 3;
				}
				else if (Keyboard.isKeyDown(Keyboard.KEY_4))
				{
					CurrentPlayer().WeaponToUse = 4;
				}
			}
		}

		// Update player's position even if it hasn't moved
		for (int Player = 0; Player < Lvl.Players.size(); Player++)
		{
			if (Lvl.Players.get(Player) != null)
			{
				// Do this for every player
				Lvl.Players.get(Player).Friction();
			}
		}

		// Update things
		Lvl.UpdateLevel();

		// If menu is lock
		if ((Menu.Locked() || Menu.HaveWindowActive()))
		{
			// Send input to Action method
			Menu.Action(MenuKeyPressed);
		}
		// If menu is active
		if (Menu.Active() && !Menu.HaveWindowActive() && !MenuKeyPressed)
		{
			// Up Key
			if (Keyboard.isKeyDown(Keyboard.KEY_UP))
			{
				Menu.CursorUp();
			}
			// Down Key
			if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))
			{
				Menu.CursorDown();
			}
			// Left Key
			if (Keyboard.isKeyDown(Keyboard.KEY_LEFT))
			{
				Menu.CursorLeft();
			}
			// Right Key
			if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
			{
				Menu.CursorRight();
			}
			// Enter Key
			if (Keyboard.isKeyDown(Keyboard.KEY_RETURN))
			{
				Menu.Locking();
			}
		}

		// Show/Hide menu
		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) && !JustPressedMenuKey && (!Menu.HaveWindowActive() || !Menu.Locked()))
		{
			if (TestingMap)
			{
				Menu.ExitGame();
			}
			else
			{
				// Remove control to player
				HasControl = !HasControl;

				Menu.Active(!Menu.Active());

				JustPressedMenuKey = true;
			}
		}
		else if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
		{
			JustPressedMenuKey = true;
		}
		else
		{
			JustPressedMenuKey = false;
		}

		// Check input for Menu
		if (Menu.Active())
		{
			while (Keyboard.next())
			{
				MenuKeyPressed = Keyboard.getEventKeyState();
			}

			// Check if key been pressed on Loop 1
			if (MenuKeyPressed)
			{
				MenuKeyPressed = true;
			}
			// No key pressed
			else
			{
				MenuKeyPressed = false;
			}
		}
	}

	public void Render(Level Lvl, ArrayList<Player> Players)
	{
		if (Display.wasResized() || DisplayModeChanged)
		{
			// Set the camera's properties
			this.ChangeProperties(this.FOV, (float) Display.getWidth() / (float) Display.getHeight(), this.Near, this.Far);
			// Set the view's canvas
			glViewport(0, 0, Display.getWidth(), Display.getHeight());

			DisplayModeChanged = false;
		}

		glClearColor(0.0f, 0.0f, 0.5f, 0.0f);   // RGBA background color
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Clear the color buffer and the depth buffer
		glLoadIdentity();
		this.UseView(); // Rotation matrices for the view

		if (Menu.ShowDebug())
		{
			// Print DEBUG stats
			System.out.println("X: " + (int) CurrentPlayer().PosX() + "	Y: " + (int) CurrentPlayer().PosY() + "	Z: " + (int) CurrentPlayer().PosZ()
					+ "	Ra: " + CurrentPlayer().GetRadianAngle() + "	Cam: " + this.RotY()/* * (float) Math.PI * 2 / 360*/
					+ "	dX: " + MouseTurnH + "	dY: " + MouseVertical + "	MoX: " + CurrentPlayer().MoX() + "	MoY: " + CurrentPlayer().MoY
					+ "	MoA: " + (float) Math.atan2(CurrentPlayer().MoY(), CurrentPlayer().MoX()));
		}

		// Enable translucidity before it starts drawing stuff
		glEnable(GL_BLEND);	// Enable OpenGL's blending functionality

		// Alpha sorting
		glEnable(GL_SAMPLE_ALPHA_TO_COVERAGE);
		glAlphaFunc(GL_GREATER, 0.9f);		// This value is for the walls
		glEnable(GL_ALPHA_TEST);

		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);	// Tells how to calculate the color of blended pixels

		if (Lvl != null)
		{
			// Sets the wireframe mode if activated
			if (Menu.Wireframe())
			{
				glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
			}

			// Draw world geometry (planes that make up the static world)
			Texture previousTexture = null;
			for (int Plane = 0; Plane < Lvl.Planes.size(); Plane++)
			{
				if (Lvl.Planes.get(Plane).Reference == null)
				{
					// This skips invisible textures
					continue;
				}

				if (Lvl.Planes.get(Plane).Reference != previousTexture)
				{
					previousTexture = Lvl.Planes.get(Plane).Reference;
					// Texture switches must be minimized as it is very expensive
					Lvl.Planes.get(Plane).Reference.Bind();
				}

				if (Lvl.Planes.get(Plane).TwoSided())
				{
					glDisable(GL_CULL_FACE);
				}

				glPushMatrix();
				{
					// Apply color to polygons
					glColor3f(1.0f * ((float)((int)Lvl.Planes.get(Plane).Light + 128) / 255),
							1.0f * ((float)((int)Lvl.Planes.get(Plane).Light + 128) / 255),
							1.0f * ((float)((int)Lvl.Planes.get(Plane).Light + 128) / 255));
					// Draw polygons according to the camera position
					glTranslatef(this.PosX(), this.PosY(), this.PosZ());
					glBegin(GL_QUADS);
					{
						for (int Vertex = 0; Vertex < Lvl.Planes.get(Plane).Vertices.size(); Vertex += 3)
						{
							glTexCoord2f(TextXcoords[Vertex / 3], TextYcoords[Vertex / 3]);
							// (Ypos, Zpos, Xpos)
							glVertex3f(-Lvl.Planes.get(Plane).Vertices.get(Vertex + 1),	// There a minus here to flip the Y axis
									Lvl.Planes.get(Plane).Vertices.get(Vertex + 2),	// The Z axis is not flipped because the world will be upside down
									-Lvl.Planes.get(Plane).Vertices.get(Vertex));	// There a minus here to flip the X axis
						}
					}
					glEnd();
				}
				glPopMatrix();

				if (Lvl.Planes.get(Plane).TwoSided())
				{
					glEnable(GL_CULL_FACE);
				}
			}

			// Draw sprites (things)
			for (int Thing = 0; Thing < Lvl.Things.size(); Thing++)
			{
				// Only check for the first five weapons
				if (Lvl.Things.get(Thing).Type == CurrentPlayer().OwnedWeapons[1] ||
					Lvl.Things.get(Thing).Type == CurrentPlayer().OwnedWeapons[2] ||
					Lvl.Things.get(Thing).Type == CurrentPlayer().OwnedWeapons[3] ||
					Lvl.Things.get(Thing).Type == CurrentPlayer().OwnedWeapons[4] ||
					Lvl.Things.get(Thing).Type == CurrentPlayer().OwnedWeapons[5])
				{
					// Don't draw the weapon on the map if it's owned
					continue;
				}

				if (Lvl.Things.get(Thing).Sprite != null)
				{
					Lvl.Things.get(Thing).Sprite.Bind();

					glPushMatrix();
					{
						// Apply color to polygons
						glColor3f(1.0f * ((float)((int)Lvl.Things.get(Thing).Light + 128) / 255),
								1.0f * ((float)((int)Lvl.Things.get(Thing).Light + 128) / 255),
								1.0f * ((float)((int)Lvl.Things.get(Thing).Light + 128) / 255));
						// Draw polygons according to the camera position
						glTranslatef(this.PosX(), this.PosY(), this.PosZ());
						glBegin(GL_QUADS);
						{
							float LookAt = this.RotY * (float) Math.PI * 2 / 360;
							float Divergent = LookAt - (float) Math.PI / 2;
							float CosDivergent = (float)Math.cos(Divergent);
							float SinDivergent = (float)Math.sin(Divergent);

							float[] SpriteX = {	Lvl.Things.get(Thing).PosX() - CosDivergent * (float)Lvl.Things.get(Thing).Radius(),
									Lvl.Things.get(Thing).PosX() + CosDivergent * (float)Lvl.Things.get(Thing).Radius(),
									Lvl.Things.get(Thing).PosX() + CosDivergent * (float)Lvl.Things.get(Thing).Radius(),
									Lvl.Things.get(Thing).PosX() - CosDivergent * (float)Lvl.Things.get(Thing).Radius() };

							float[] SpriteY = {	Lvl.Things.get(Thing).PosY() - SinDivergent * (float)Lvl.Things.get(Thing).Radius(),
									Lvl.Things.get(Thing).PosY() + SinDivergent * (float)Lvl.Things.get(Thing).Radius(),
									Lvl.Things.get(Thing).PosY() + SinDivergent * (float)Lvl.Things.get(Thing).Radius(),
									Lvl.Things.get(Thing).PosY() - SinDivergent * (float)Lvl.Things.get(Thing).Radius() };

							float[] SpriteZ = {	Lvl.Things.get(Thing).PosZ(),
									Lvl.Things.get(Thing).PosZ(),
									Lvl.Things.get(Thing).PosZ() + Lvl.Things.get(Thing).Height(),
									Lvl.Things.get(Thing).PosZ() + Lvl.Things.get(Thing).Height() };

							for (int Corner = 0; Corner < 4; Corner++)
							{
								glTexCoord2f(TextXcoords[Corner], TextYcoords[Corner]);
								// (Ypos, Zpos, Xpos)
								glVertex3f(-SpriteY[Corner], SpriteZ[Corner], -SpriteX[Corner]);
							}
						}
						glEnd();
					}
					glPopMatrix();
				}
			}

			// Draw the Players
			for (int Player = 0; Player < Lvl.Players.size(); Player++)
			{
				if (Lvl.Players.get(Player) == Plyr)
				{
					// Don't draw the player's own sprite in his own screen
					continue;
				}

				if (Lvl.Players.get(Player).Health <= 0)
				{
					// Don't draw dead players
					continue;
				}

				float FrameWidth = Lvl.Players.get(Player).Radius();

				if (Lvl.Players.get(Player).WalkFrames.get(0) != null)
				{
					float LookAngleDiff = (float)Math.atan2(this.CurrentPlayer().PosY() - Lvl.Players.get(Player).PosY(), 
															this.CurrentPlayer().PosX() - Lvl.Players.get(Player).PosX());
					// Convert to degrees
					LookAngleDiff = ((LookAngleDiff * 180 / (float)Math.PI) + (180 - Lvl.Players.get(Player).GetDegreeAngle())) % 360;

					// Chose the correct frame for the movement
					Lvl.Players.get(Player).WalkFrames.get(0).Bind();	// Default
					int CurrentFrameIndex = 0;

					if (Lvl.Players.get(Player).JustShot() || Lvl.Players.get(Player).WeaponTimeSinceLastShot < 5)
					{
						CurrentFrameIndex = 40;
					}
					else
					{
						if (Lvl.Players.get(Player).Frame() % 16 >= 12)
						{
							CurrentFrameIndex = 24;
						}
						else if (Lvl.Players.get(Player).Frame() % 16 >= 8)
						{
							CurrentFrameIndex = 16;
						}
						else if (Lvl.Players.get(Player).Frame() % 16 >= 4)
						{
							CurrentFrameIndex = 8;
						}
					}

					// Find the good frame to display the player correctly
					int RotationFrame = 4;
					for (float RotationAngle = -337.5f; RotationAngle <= 382.5f; RotationAngle += 45)
					{
						if (LookAngleDiff <= RotationAngle)
						{
							Lvl.Players.get(Player).WalkFrames.get(RotationFrame % 8 + CurrentFrameIndex).Bind();
							FrameWidth = Lvl.Players.get(Player).Height() /
									Lvl.Players.get(Player).WalkFrames.get(RotationFrame % 8 + CurrentFrameIndex).Height() *
									Lvl.Players.get(Player).WalkFrames.get(RotationFrame % 8 + CurrentFrameIndex).Width() / 2;
							break;
						}
						RotationFrame++;
					}
				}

				glPushMatrix();
				{
					// Apply color to polygons
					glColor3f(1.0f, 1.0f, 1.0f);
					// Draw polygons according to the camera position
					glTranslatef(this.PosX(), this.PosY(), this.PosZ());
					glBegin(GL_QUADS);
					{
						float LookAt = this.RotY * (float) Math.PI * 2 / 360;
						float Divergent = LookAt - (float) Math.PI / 2;
						float CosDivergent = (float)Math.cos(Divergent);
						float SinDivergent = (float)Math.sin(Divergent);

						float[] SpriteX = {	Lvl.Players.get(Player).PosX() - CosDivergent * FrameWidth,
								Lvl.Players.get(Player).PosX() + CosDivergent * FrameWidth,
								Lvl.Players.get(Player).PosX() + CosDivergent * FrameWidth,
								Lvl.Players.get(Player).PosX() - CosDivergent * FrameWidth };

						float[] SpriteY = {	Lvl.Players.get(Player).PosY() - SinDivergent * FrameWidth,
								Lvl.Players.get(Player).PosY() + SinDivergent * FrameWidth,
								Lvl.Players.get(Player).PosY() + SinDivergent * FrameWidth,
								Lvl.Players.get(Player).PosY() - SinDivergent * FrameWidth };

						float[] SpriteZ = {	Lvl.Players.get(Player).PosZ(),
								Lvl.Players.get(Player).PosZ(),
								Lvl.Players.get(Player).PosZ() + Lvl.Players.get(Player).Height(),
								Lvl.Players.get(Player).PosZ() + Lvl.Players.get(Player).Height() };

						for (int Corner = 0; Corner < 4; Corner++)
						{
							glTexCoord2f(TextXcoords[Corner], TextYcoords[Corner]);
							// (Ypos, Zpos, Xpos)
							glVertex3f(-SpriteY[Corner], SpriteZ[Corner], -SpriteX[Corner]);
						}
					}
					glEnd();
				}
				glPopMatrix();
			}

			// Change the alpha for the sprites that are drawn on the screen
			glAlphaFunc(GL_GREATER, 0.0f);

			// Unsets the wireframe mode from the menu and HUD only if the wireframe mode was set
			if (Menu.Wireframe())
			{
				glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
			}

			// If menu is Show
			if(Menu.Active())
			{
				// To 2d
				glMatrixMode(GL_PROJECTION);
				glLoadIdentity();

				glMatrixMode(GL_MODELVIEW);
				glLoadIdentity();

				// Disable depth so that element are written on the same level
				glDisable(GL_DEPTH_TEST);

				if (!Menu.InGame)
				{
					if (!DemoMode && !TestingMap)
					{
						Menu.DrawTexture(Menu.TitleScreen, 0, 0, 100, 100);
					}
					Menu.DrawText(Menu.GameVersion, 90, 0, 2, 2);
				}

				// Set Draw cursor to Top-Left
				glTranslatef(-1f, 1f, 0.0f);

				// Draw menu
				Menu.GridWidth(Display.getWidth());
				Menu.GridHeight(Display.getHeight());

				glDisable(GL_TEXTURE_2D);

				Menu.DrawMenu();

				glFlush();

				glEnable(GL_DEPTH_TEST);
				InitProjection();
				glEnable(GL_TEXTURE_2D);
			}
			else // Draw 2D texture (weapon and HUD)
			{
				// To 2d
				glMatrixMode(GL_PROJECTION);
				glLoadIdentity();

				glMatrixMode(GL_MODELVIEW);
				glLoadIdentity();

				// Disable depth so that element are written on the same level
				glDisable(GL_DEPTH_TEST);

				glDisable(GL_TEXTURE_2D);

				if (Menu.InGame)
				{
					// HUD
					if (Menu.ShowHud.Bool())
					{
						Menu.ShowHUD(Plyr, Plyr.CanShot());
					}

					// Temporary solution to draw the gun fire
					if (CurrentPlayer().JustShot())
					{
						Menu.DrawTexture(CurrentPlayer().GunFire, 34, 34, 32, 16);
					}

					// Draw a weapon
					switch (CurrentPlayer().SelectedWeapon)
					{
						case 1:
							Menu.DrawTexture(CurrentPlayer().SelectedWeaponSprite, 30, CurrentPlayer().WeaponHeight - CurrentPlayer().DifferenceViewZ() * 2, 310 / 9.5, 358 / 8);
							break;
						case 2:
							Menu.DrawTexture(CurrentPlayer().SelectedWeaponSprite, 30.5, CurrentPlayer().WeaponHeight - CurrentPlayer().DifferenceViewZ() * 2, 310 / 9.5, 358 / 8);
							break;
						case 3:
							Menu.DrawTexture(CurrentPlayer().SelectedWeaponSprite, 36.5, CurrentPlayer().WeaponHeight - CurrentPlayer().DifferenceViewZ() * 2, 241 / 8, 293 / 7);
							break;
					}

					// Show the scores on the screen
					if (Lvl.Players.size() > 1 && Menu.InGame)
					{
						if (Keyboard.isKeyDown(Keyboard.KEY_TAB))
						{
							Menu.ShowScoreTable(Lvl.Players());
						}
					}

					if (Menu.AimingCursor() && Plyr.CanShot())
					{
						Menu.DrawTexture(Crosshair, 47, 46, 6, 8);
					}

					if (Menu.ShowDebug())
					{
						Menu.DrawText("Angle:" + CurrentPlayer().GetDegreeAngle() + "'", 0, 4, 3, 3);
						Menu.DrawText("X:" + CurrentPlayer().PosX(), 0, 0, 3, 3);
						Menu.DrawText("Y:" + CurrentPlayer().PosY(), 50, 0, 3, 3);
					}
				}
				else
				{
					// Draw the title screen
					if (!DemoMode && !TestingMap)
					{
						Menu.DrawTexture(Menu.TitleScreen, 0, 0, 100, 100);
					}

					if (!Menu.InGame && !Menu.IsServer && !Menu.IsClient && !Menu.MessageIsOnScreen())
					{
						if (!TestingMap)
						{
							Menu.DrawText("Press 'escape' to access the menu", 9, 50, 2, 2);
						}
					}

					if (!TestingMap)
					{
						Menu.DrawText(Menu.GameVersion, 90, 0, 2, 2);
					}
				}

				// Always display the debug info while testing the map
				if (TestingMap)
				{
					Menu.DrawText("Angle:" + CurrentPlayer().GetDegreeAngle() + "'", 0, 4, 3, 3);
					Menu.DrawText("X:" + CurrentPlayer().PosX(), 0, 0, 3, 3);
					Menu.DrawText("Y:" + CurrentPlayer().PosY(), 50, 0, 3, 3);
				}

				// Draw a message if there is one
				Menu.DrawMessage();

				// Draw all element
				glFlush();

				// Enable for the 3D element
				glEnable(GL_DEPTH_TEST);
				InitProjection();
				glEnable(GL_TEXTURE_2D);
			}

			// If the player is playing (in-game), then enable the mouse. Else, don't.
			if (!Menu.Active() && !HasControl)
			{
				// Check if mouse should be grabbed lastly so the mouse input is not destroyed
				if (Menu.GrabMouse() && !Mouse.isGrabbed() && (Menu.InGame || TestingMap))
				{
					Mouse.setGrabbed(true);
				}
				else if (!Menu.GrabMouse() && Mouse.isGrabbed())
				{
					Mouse.setGrabbed(false);
				}
			}
			else if (Menu.Active() || !Menu.InGame && !TestingMap)	// Never activate the mouse while there is a menu
			{
				if (Mouse.isGrabbed())
				{
					Mouse.setGrabbed(false);
				}
			}
			else if (!Menu.Active() && HasControl)	// If the menu is not active and the player is playing, make sure to grab if set.
			{
				if (Menu.GrabMouse() && !Mouse.isGrabbed())
				{
					Mouse.setGrabbed(true);
				}
			}

			// This line must be after the things get drawn else they will be at an inaccurate angle when the player turns.
			this.UpdateCamera();
		}

		Display.update();
	}

	public float PosX()
	{
		return PosX;
	}

	public float PosY()
	{
		return -PosY;
	}

	public float PosZ()
	{
		return PosZ;
	}

	public void PosX(float PosX)
	{
		this.PosX = PosX;
	}

	public void PosY(float PosY)
	{
		this.PosY = -PosY;
	}

	public void PosZ(float PosZ)
	{
		this.PosZ = PosZ;
	}

	public float RotX()
	{
		return RotX;
	}

	public float RotY()
	{
		return RotY;
	}

	public float RotZ()
	{
		return RotZ;
	}

	public void RotX(float RotX)
	{
		this.RotX = RotX;
	}

	public void RotY(float RotY)
	{
		this.RotY = RotY;
	}

	public void RotZ(float RotZ)
	{
		this.RotZ = RotZ;
	}
}

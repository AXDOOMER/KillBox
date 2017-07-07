//Copyright (C) 2014-2017 Alexandre-Xavier Labonté-Lamoureux
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

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import static org.lwjgl.opengl.GL11.*;

import java.io.*;
import java.util.HashMap;

public class Game
{
	static int TicksCount = 0;
	static int WallsFilter = GL_NEAREST;

	public static void main(String[] args)
	{
		System.out.println("			KillBox v2.0");
		System.out.println("			============");

		System.out.println("This program is free software distributed under the GNU GPL version 3.");

		// Nodes are computers where there is a player
		int Nodes = 1;
		String Demo = null;
		Level Lvl = null;
		int View = 0;
		String ConfigFileName = "default.cfg";
		boolean InGame = false;
		String DefaultMap = "demo.txt";
		HashMap<String, Integer> Parameters = new HashMap<String, Integer>();

		// The frame rate and the time limit to execute a frame
		final int FrameRate = 30;
		final double TimeFrameLimit = (1 / (double)FrameRate) * 1000;

		Netplay NetplayInfo = null;
		NetplayInfo.FrameRate = FrameRate;

		// Populate the parameters list
		for (int i = 0; i < args.length; i++)
		{
			// Parameter arguments are also added, but since we access them directly from 'args', 
			// they are not lowercased. This way, we don't need to do any special treatment here. 
			Parameters.put(args[i].toLowerCase(), i);
		}

		//System.out.print("Enter a level's name (including the extension): ");
		//BufferedReader Reader = new BufferedReader(new InputStreamReader(System.in));

		long TimeStart = System.currentTimeMillis();
		long TimeEnd = System.currentTimeMillis();
		long DeltaTime = 1;

		try
		{
			Lvl = new Level(/*Reader.readLine()*/ /*"res/test.txt"*/);

			// Continues here if a the level is found and loaded (no exception)
			if (Parameters.containsKey("-playdemo"))
			{
				Demo = args[Parameters.get("-playdemo") + 1];

				if (Demo.charAt(0) == '-')
				{
					// It's just another parameter...
					Demo = null;
				}
			}

			// Check if we specify the number of players. It will be a normal game.
			if (Parameters.containsKey("-nodes") && Demo == null)
			{
				try
				{
					Nodes = Integer.parseInt(args[Parameters.get("-nodes") + 1]);

					if (Nodes < 1 || Nodes > 4)
					{
						Nodes = 2;
					}
				}
				catch (Exception e)
				{
					Nodes = 2;
				}

				System.out.println("Up to " + Nodes + " nodes can join.");

				NetplayInfo = new Netplay(Nodes, 0, 10, 25, DefaultMap);

				// So the game starts.
				InGame = true;
			}

			// Check if the player sent an IP. He wants to join the game!
			if (Parameters.containsKey("-connect"))
			{
				String HostIP = null;

				try
				{
					HostIP = args[Parameters.get("-connect") + 1];
					System.out.println("Host IP : " + HostIP);
				}
				catch (Exception e)
				{
					System.out.println("Bad IP address specified: " + HostIP);
				}

				NetplayInfo = new Netplay(Nodes, HostIP);

				// Change the number of nodes
				Nodes = NetplayInfo.Nodes;

				// Change the player's view
				View = NetplayInfo.View;

				// So the game starts
				InGame = true;
			}

			// The game is all set up. Open the window.
			try
			{
				if (Parameters.containsKey("-fullscreen"))
				{
					Display.setDisplayMode(Display.getDesktopDisplayMode());
					Display.setFullscreen(true);
				}
				else
				{
					Display.setDisplayMode(new DisplayMode(640, 480));
					Display.setResizable(true);
				}
				Display.setTitle("KillBox");
				Display.setVSyncEnabled(true);
				Display.create();
			}
			catch (LWJGLException ex)
			{
				System.out.println("Error while creating the Display: ");
				ex.printStackTrace();
			}

			// Sound (SFX)
			Sound SndDriver = new Sound();

			// Whoa! That's an ugly way to do things...
			for (int Player = 0; Player < Nodes; Player++)
			{
				Lvl.Players.add(new Player(Lvl, SndDriver));
			}

			Camera HeadCamera = new Camera(Lvl.Players.get(View), 90, 640f/480f, 0.1f, 8192f);
			HeadCamera.ChangePlayer(Lvl.Players.get(View), true);   // Gives the control over the player
			HeadCamera.Menu.InGame = InGame;
			HeadCamera.Menu.SetSoundOut(SndDriver);

			// Set Listener
			SndDriver.SetNewListener(Lvl.Players.get(View));

			glEnable(GL_TEXTURE_2D);
			glEnable(GL_DEPTH_TEST);    // CLEANUP PLEASE!!!

			if (Parameters.containsKey("-wireframe"))
			{
				HeadCamera.Menu.Wireframe(true);
				HeadCamera.Menu.AddWireframeVideoOption();
			}

			// Key presses
			boolean JustPressedSpyKey = false;

			//Mouse.setGrabbed(true);     // Grab the mouse when the game has started.

			// Change the texture filter for the walls and other types of surface
			if (Parameters.containsKey("-near"))
			{
				// Doesn't do anything, but is here in case the default is changed.
				HeadCamera.Menu.Filtering(false);
				WallsFilter = GL_NEAREST;
			}
			else if (Parameters.containsKey("-bi"))
			{
				// This is bilinear filtering
				HeadCamera.Menu.Filtering(true);
				WallsFilter = GL_LINEAR;
			}
			else
			{
				// Else it's set by the config file
				if (HeadCamera.Menu.Filtering())
				{
					WallsFilter = GL_LINEAR;
				}
				else
				{
					WallsFilter = GL_NEAREST;
				}
			}

			if (Parameters.containsKey("-test"))
			{
				HeadCamera.TestingMap = true;

				// Parse the command line parameter to see if the user has specified a level to test
				if (Parameters.get("-test") + 1 < args.length && args[Parameters.get("-test") + 1].charAt(0) != '-')
				{
					// Change the default level
					DefaultMap = args[Parameters.get("-test") + 1];
				}
			}

			if (Parameters.containsKey("-level"))
			{
				// Parse the command line parameter to avoid mistakes
				if (Parameters.get("-level") + 1 >= args.length)
				{
					System.out.println("Error: Nothing specified after the parameter '-level'");
					System.exit(1);
				}
				else if (args[Parameters.get("-level") + 1].charAt(0) == '-')
				{
					System.out.println("Error: The argument given to the parameter '-level' starts with a '-'");
					System.exit(1);
				}
				else
				{
					// Change the default level
					DefaultMap = args[Parameters.get("-level") + 1];
				}
			}

			if (Parameters.containsKey("-demo"))
			{
				HeadCamera.DemoMode = true;
				DefaultMap = "demo.txt";
			}

			Lvl.LoadLevel("res/maps/" + DefaultMap, WallsFilter);		// Load the level

			// Players will spawn at random locations
			for (int Player = 0; Player < Lvl.Players.size(); Player++)
			{
				if (!Lvl.Players.get(Player).SpawnAtRandomSpot(true))
				{
					System.err.println("Can't find a free spot to spawn player " + (Player + 1) + ". Your map may not have enough of them.");
					System.exit(1);
				}
			}

			// Load the texture "sprites" that will be used to represent the players in the game
			Lvl.Players.get(0).LoadSprites();

			if (NetplayInfo != null && !Parameters.containsKey("-showframetime"))
			{
				if (NetplayInfo.Server != null)
				{
					Display.setTitle("KillBox (Server)");
				}
				else
				{
					Display.setTitle("KillBox (Client)");
				}
			}

			// Deactivate de menu and give back the control to the player
			//HeadCamera.Menu.Active(false);
			HeadCamera.ChangePlayer(Lvl.Players.get(View), true);

			// Load the configuration file
			LoadConfigFile(ConfigFileName, HeadCamera.Menu, SndDriver);

			// The main game loop
			while (!Display.isCloseRequested() && !HeadCamera.Menu.UserWantsToExit)
			{
				TimeStart = System.currentTimeMillis();

				if (HeadCamera.Menu.Fullscreen() && !(Display.getDisplayMode().getWidth() == Display.getDesktopDisplayMode().getWidth()))
				{
					// Changing to fullscreen
					HeadCamera.DisplayModeChanged = true;
					Display.setDisplayMode(Display.getDesktopDisplayMode());
					Display.setFullscreen(true);
					Display.setVSyncEnabled(true);

				}
				else if (!HeadCamera.Menu.Fullscreen() && (Display.getDisplayMode().getWidth() == Display.getDesktopDisplayMode().getWidth()))
				{
					// Changing to windowed mode
					HeadCamera.DisplayModeChanged = true;
					Display.setDisplayMode(new DisplayMode(640, 480));
					Display.setFullscreen(false);
					Display.setResizable(true);		// FIXME: This is broken for some reason (LWJGL's fault?)
					Display.setVSyncEnabled(true);
				}

				// Get mouse sensitivity
				HeadCamera.MouseSensitivity = (((float)HeadCamera.Menu.MouseSensitivity.Int())/100);
				// Get sound volume
				SndDriver.VolumeMultiplier = ((float)(HeadCamera.Menu.SFXVolume.Int()))/100;

				// Give the right hear to the right players
				if (NetplayInfo != null)
				{
					SndDriver.SetNewListener(Lvl.Players.get(NetplayInfo.View));
				}

				// Run game logic then draw the screen
				HeadCamera.RunLogic(Lvl, Lvl.Players);
				if (!Parameters.containsKey("-frameskip"))
				{
					HeadCamera.Render(Lvl, Lvl.Players);
				}
				else
				{
					if (DeltaTime < 1000 / FrameRate)
					{
						HeadCamera.Render(Lvl, Lvl.Players);
					}
				}


				if (HeadCamera.Menu.InGame)
				{
					// Update players
					for (int Player = 0; Player < Lvl.Players.size(); Player++)
					{
						Lvl.Players.get(Player).UpdateIfDead();
						Lvl.Players.get(Player).UpdateTimeSinceLastShot();
						Lvl.Players.get(Player).UpdateFlagTime();
						Lvl.Players.get(Player).UpdateTickCount();
					}

					// Look for a winner and end the game if needed
					NetplayInfo.TestEndGame(NetplayInfo, Lvl, HeadCamera);

					// Check if we're in a multiplayer game
					if (Nodes > 1)
					{
						// FIXME: fakenet still requires a network game to be started and it will freeze
						if (!Parameters.containsKey("-fakenet"))
						{
							// Empty the command that's gonna be sent over the network
							NetplayInfo.PlayerCommand.Reset();
							for (int Player = 0; Player < NetplayInfo.OtherPlayersCommand.size(); Player++)
							{
								NetplayInfo.OtherPlayersCommand.get(Player).Reset();
							}

							// Encode an action on this variable. Init to 0.
							int Action = 0;

							// Encode...
							Action += Lvl.Players.get(View).ActionIsHasShot();
							Action += Lvl.Players.get(View).ActionIsHasReload();
							Action += Lvl.Players.get(View).WeaponToUse;

							// Put the players' move in a command
							NetplayInfo.PlayerCommand.UpdateAngleDiff(Lvl.Players.get(View).AngleDiff);
							NetplayInfo.PlayerCommand.UpdateForwardMove(Lvl.Players.get(View).FrontMove);
							NetplayInfo.PlayerCommand.UpdateSideMove(Lvl.Players.get(View).SideMove);
							NetplayInfo.PlayerCommand.UpdateAction(Action);

							Lvl.Players.get(View).Action = NetplayInfo.PlayerCommand.Actions;

							// Do the network communication through the socket
							NetplayInfo.AllTheCommunication(Lvl, HeadCamera, TicksCount);

							for (int Player = 0; Player < Lvl.Players.size(); Player++)
							{
								// BUG: Cheap fix player strafing not reset. FUCK!
								Lvl.Players.get(Player).WeaponToUse = 0;
								Lvl.Players.get(Player).SideMove = 0;
								Lvl.Players.get(Player).FrontMove = 0;
								Lvl.Players.get(Player).AngleDiff = 0;
								Lvl.Players.get(Player).Shot = false;
								Lvl.Players.get(Player).Reloading = false;
							}
						}
					}

					// Spy view
					if (Keyboard.isKeyDown(Keyboard.KEY_F12) && !JustPressedSpyKey)
					{
						boolean Control = false;

						do
						{
							View = (View + 1) % Lvl.Players.size();

							if (View == NetplayInfo.View)
							{
								Control = true;
							}
							else
							{
								Control = false;
							}
						}
						while (Lvl.Players.get(View) == null);

						System.out.println("Spying view " + View);
						HeadCamera.ChangePlayer(Lvl.Players.get(View), Control);

						JustPressedSpyKey = true;
					}
					else if (Keyboard.isKeyDown(Keyboard.KEY_F12))
					{
						JustPressedSpyKey = true;
					}
					else
					{
						JustPressedSpyKey = false;
					}
				}
				else // Single player
				{
					if (!Parameters.containsKey("-showframetime"))
					{
						Display.setTitle("KillBox");    // In single player mode, use this window title.
					}

					if (TicksCount > 1)
					{
						Lvl.Players.get(View).UpdateTickCount();

						if (!HeadCamera.DemoMode || HeadCamera.TestingMap)
						{
							// Make the player move even if it's not a multiplayer game
							Lvl.Players.get(View).ExecuteMove(Lvl.Players.get(View).FrontMove, Lvl.Players.get(View).SideMove);
							Lvl.Players.get(View).ExecuteAngleTurn(Lvl.Players.get(View).AngleDiff);
						}
						else
						{
							if (Lvl.Players.get(View).PosY() > 100)
							{
								Lvl.Players.get(View).PosY(Lvl.Players.get(View).PosY() - 2);
								Lvl.Players.get(View).PosZ(Lvl.Players.get(View).PosZ() - 0.1f);
							}
							else if (Lvl.Players.get(View).GetDegreeAngle() < 50 || Lvl.Players.get(View).GetDegreeAngle() >= 270)
							{
								Lvl.Players.get(View).Angle = (short)((Lvl.Players.get(View).Angle + 100) % Short.MAX_VALUE);
								Lvl.Players.get(View).PosZ(Lvl.Players.get(View).PosZ() + 0.1f);
							}
							else if (Lvl.Players.get(View).PosX() > -860)
							{
								Lvl.Players.get(View).PosX(Lvl.Players.get(View).PosX() - 2);
								Lvl.Players.get(View).PosZ(Lvl.Players.get(View).PosZ() + 0.1f);
							}
							else if (Lvl.Players.get(View).PosZ() > 0)
							{
								Lvl.Players.get(View).PosZ(Lvl.Players.get(View).PosZ() - 0.1f);
							}
						}
					}

					// Close sockets
					if (NetplayInfo != null)
					{
						if (NetplayInfo.Server != null)
						{
							NetplayInfo.Server.close();
							NetplayInfo.Server = null;
						}

						for (int ConnectionSocket = 0; ConnectionSocket < NetplayInfo.Connections.size(); ConnectionSocket++)
						{
							if (NetplayInfo.Connections.get(ConnectionSocket) != null)
							{
								NetplayInfo.Connections.get(ConnectionSocket).close();
								NetplayInfo.Connections.remove(ConnectionSocket);
							}
						}
					}

					// Game is not started. Start one using the menu.
					if (HeadCamera.Menu.IsServer)
					{
						if (!Parameters.containsKey("-showframetime"))
						{
							Display.setTitle("KillBox (Server)");
						}
						Nodes = 2;

						NetplayInfo = new Netplay(Nodes, HeadCamera.Menu.GameMode,
								HeadCamera.Menu.TimeLimit, HeadCamera.Menu.KillLimit, HeadCamera.Menu.Map);
						if (!NetplayInfo.ServerSocketIsNull())
						{
							Lvl = new Level();

							for (int Player = 0; Player < Nodes; Player++)
							{
								Lvl.Players.add(new Player(Lvl, SndDriver));

								if (HeadCamera.Menu.GameMode == 1)
								{
									// Gamemode is one-shot-kill, so set health to 1.
									Lvl.Players.get(Player).MaxHealth = 1;
									Lvl.Players.get(Player).Health = 1;
								}
								else
								{
									Lvl.Players.get(Player).MaxHealth = 100;
									Lvl.Players.get(Player).Health = 100;
								}
							}

							// Set Listener
							SndDriver.SetNewListener(Lvl.Players.get(View));

							Lvl.LoadLevel("res/maps/" + HeadCamera.Menu.Map + ".txt", WallsFilter);
							HeadCamera.Menu.InGame = true;
							TicksCount = 0;

							// If we are not playing flagtag
							if (HeadCamera.Menu.GameMode != 2)
							{
								Lvl.RemoveTypeOfThingsFromLevel(Thing.Names.Flag);
							}

							// The game will start, don't need this anymore. Reset to default value.
							HeadCamera.Menu.IsServer = false;

							// Create more players if there is not enough for every nodes
							for (int Player = Lvl.Players.size(); Player < Nodes; Player++)
							{
								Lvl.Players.add(new Player(Lvl, SndDriver));
							}

							View = NetplayInfo.View;

							// Players will spawn at random locations
							Random Rand = new Random();
							Rand.Reset();

							for (int Player = 0; Player < Lvl.Players.size(); Player++)
							{
								if (!Lvl.Players.get(Player).SpawnAtRandomSpot(false))
								{
									System.err.println("Can't find a free spot to spawn player "
											+ (Player + 1) + ". Your map may not have enough of them.");
									System.exit(1);
								}
							}
						}
						else
						{
							NetplayInfo = null;
							HeadCamera.Menu.IsServer = false;
							HeadCamera.Menu.NewMessageToShow("Couldn't create a server.");
						}

					}
					else if (HeadCamera.Menu.IsClient)
					{
						if (!Parameters.containsKey("-showframetime"))
						{
							Display.setTitle("KillBox (Client)");
						}
						String IpAddress = HeadCamera.Menu.Address;
						Nodes = 2;
						NetplayInfo = new Netplay(Nodes, IpAddress);

						if (NetplayInfo.Connections.get(0).isConnected())
						{
							// Get the game condition
							HeadCamera.Menu.GameMode = NetplayInfo.GameMode;
							HeadCamera.Menu.TimeLimit = NetplayInfo.TimeLimit;
							HeadCamera.Menu.KillLimit = NetplayInfo.KillLimit;
							HeadCamera.Menu.Map = NetplayInfo.Map;

							// Change the player view
							View = NetplayInfo.View;

							HeadCamera.Menu.InGame = true;
							TicksCount = 0;

							Lvl = new Level();

							for (int Player = 0; Player < Nodes; Player++)
							{
								Lvl.Players.add(new Player(Lvl, SndDriver));

								if (HeadCamera.Menu.GameMode == 1)
								{
									// Gamemode is one-shot-kill, so set health to 1.
									Lvl.Players.get(Player).MaxHealth = 1;
									Lvl.Players.get(Player).Health = 1;
								}
								else
								{
									Lvl.Players.get(Player).MaxHealth = 100;
									Lvl.Players.get(Player).Health = 100;
								}
							}

							// Set Listener
							SndDriver.SetNewListener(Lvl.Players.get(View));

							Lvl.LoadLevel("res/maps/" + HeadCamera.Menu.Map + ".txt", WallsFilter);

							// If we are not playing flagtag
							if (HeadCamera.Menu.GameMode != 2)
							{
								Lvl.RemoveTypeOfThingsFromLevel(Thing.Names.Flag);
							}
						}
						else
						{
							System.err.println("Can't connect to server. Check if the server works.");
						}

						// The game will start, don't need this anymore. Reset to default value.
						HeadCamera.Menu.IsClient = false;

						// Create more players if there is not enough for every nodes
						for (int Player = Lvl.Players.size(); Player < Nodes; Player++)
						{
							Lvl.Players.add(new Player(Lvl, SndDriver));
						}

						// Players will spawn at random locations
						Random Rand = new Random();
						Rand.Reset();

						for (int Player = 0; Player < Lvl.Players.size(); Player++)
						{
							if (!Lvl.Players.get(Player).SpawnAtRandomSpot(false))
							{
								System.err.println("Can't find a free spot to spawn player "
										+ (Player + 1) + ". Your map may not have enough of them.");
								System.exit(1);
							}
						}
					}
				}

				for (int Player = 0; Player < Lvl.Players.size(); Player++)
				{
					// BUG: Cheap fix player strafing not reset. FUCK!
					Lvl.Players.get(Player).SideMove = 0;
					Lvl.Players.get(Player).FrontMove = 0;
					Lvl.Players.get(Player).AngleDiff = 0;
					Lvl.Players.get(Player).Shot = false;
					Lvl.Players.get(Player).Reloading = false;

					// Activating the no clipping cheat
					if (Parameters.containsKey("-noclip"))
					{
						Lvl.Players.get(Player).SetNoClipping(true);
					}
				}

				if (NetplayInfo != null)
				{
					// This may be repetitive
					if (View == NetplayInfo.View)
					{
						// Set the control to its own view
						HeadCamera.ChangePlayer(Lvl.Players.get(View), true);
					}
				}

				if (Parameters.containsKey("-gc"))
				{
					// Asks for the garbage collector every 5 seconds.
					// Java may choose not to do the garbage collection anyway.
					if (TicksCount % (FrameRate * 5) == 0)
					{
						System.gc();
						//System.runFinalization();
					}
				}

				try
				{
					// Timer for sleep
					TimeEnd = System.currentTimeMillis();
					DeltaTime = TimeEnd - TimeStart;

					if ((Parameters.containsKey("-showframetime") || Parameters.containsKey("-showframerate")) && DeltaTime != 0)
					{
						Display.setTitle(DeltaTime + "ms (" + 1000/DeltaTime + "FPS)");
					}

					// Make sure the time is not negative
					// We want to run the game as fast as possible
					if (DeltaTime < 0)
					{
						DeltaTime = 0;
					}
					else if (DeltaTime > TimeFrameLimit)
					{
						DeltaTime = (long)TimeFrameLimit;
					}

					// Make the game sleep depending on how much time it took to execute one tick
					Thread.sleep((long) TimeFrameLimit - DeltaTime);
					TicksCount++;
				}
				catch (InterruptedException ie)
				{
					System.err.print("The game failed to sleep: " + ie.getStackTrace());
					System.exit(1);
				}
			}

			// Save configs to file
			SaveConfigFile(ConfigFileName, HeadCamera.Menu, SndDriver);

			// Close OpenAL
			SndDriver.CloseOpenAL();

			// Close the display
			Display.destroy();
		}
		catch (Exception e)
		{
			System.err.println(e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void LoadConfigFile(String Name, Menu MenuSystem, Sound SoundSystem)
	{
		// Load the config file and assign every value to their respective variables
		BufferedReader ConfigFile = null;

		try
		{
			HashMap<String, String> Map = new HashMap<String, String>();

			MenuSystem.Active(true);
			// Load the file
			ConfigFile = new BufferedReader(new FileReader("res/" + Name));

			// Load the user's settings in the same order that they were saved.
			String Line;
			while ((Line = ConfigFile.readLine()) != null)
			{
				Map.put(Line.split("\t", 2)[0].trim().toLowerCase(),
						Line.split("\t", 2)[1].trim().toLowerCase());
			}

			MenuSystem.AimingCursor(Boolean.parseBoolean(Map.get("use_freelook")));
			MenuSystem.ShowHud.Bool(Boolean.parseBoolean(Map.get("show_hud")));
			MenuSystem.ShowDebug(Boolean.parseBoolean(Map.get("show_debug")));

			MenuSystem.GrabMouse(Boolean.parseBoolean(Map.get("grab_mouse")));
			MenuSystem.MouseSensibility(Integer.parseInt(Map.get("mouse_sensitivity")));
			MenuSystem.SFXVolume(Integer.parseInt(Map.get("sfx_volume")));

			MenuSystem.Fullscreen(Boolean.parseBoolean(Map.get("fullscreen")));
			MenuSystem.Filtering(Boolean.parseBoolean(Map.get("enable_filtering")));
		}
		catch (FileNotFoundException fnfe)
		{
			System.err.println("Cannot load your settings. Configuration file not found.");
		}
		catch (IOException ioe)
		{
			System.err.println("Error while reading from the configuration file.");
		}
		catch (NullPointerException npe)
		{
			System.err.println("Error while reading the value of an item from the configuration file.");
		}
		catch (NumberFormatException nfe)
		{
			System.err.println("A setting from the configuration file is invalid or missing.");
		}

		if (ConfigFile != null)
		{
			try
			{
				ConfigFile.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		MenuSystem.Active(false);
	}

	public static void SaveConfigFile(String Name, Menu MenuSystem, Sound SoundSystem)
	{
		// Save the configuration variables in a specific order in a config file
		PrintWriter ConfigFile = null;

		try
		{
			// Writer for the config file
			ConfigFile = new PrintWriter(new BufferedWriter(new FileWriter("res/" + Name)));

			// Double tabs
			final String Spacing = "\t\t";

			ConfigFile.println("use_freelook" + Spacing + MenuSystem.AimingCursor.Bool());
			ConfigFile.println("show_hud" + Spacing + MenuSystem.ShowHud.Bool());
			ConfigFile.println("show_debug" + Spacing + MenuSystem.ShowDebug());

			ConfigFile.println("grab_mouse" + Spacing + MenuSystem.GrabMouse.Bool());
			ConfigFile.println("mouse_sensitivity" + Spacing + MenuSystem.MouseSensitivity.Int());

			ConfigFile.println("sfx_volume" + Spacing + MenuSystem.SFXVolume.Int());

			ConfigFile.println("fullscreen" + Spacing + MenuSystem.Fullscreen.Bool());
			ConfigFile.println("enable_filtering" + Spacing + MenuSystem.Filtering.Bool());
		}
		catch (FileNotFoundException fnfe)
		{
			System.err.println("Could not save your settings to a configuration file.");
			System.err.println(fnfe.getStackTrace());
		}
		catch (IOException ioe)
		{
			System.err.println("File error.");
		}

		if (ConfigFile != null)
		{
			ConfigFile.close();
		}
	}
}


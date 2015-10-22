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

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import static org.lwjgl.opengl.GL11.*;

import java.io.*;

public class Game
{
	static int TicksCount = 0;

	public static void main(String[] args)
	{
		System.out.println("			KillBox v2.??? (Bêta)");
		System.out.println("			======================");

		// Nodes are computers where there is a player
		int Nodes = 1;
		String Demo = null;
		Level Lvl = null;
		int View = 0;
		String ConfigFileName = "default.cfg";
		boolean InGame = false;

		// The frame rate and the time limit to execute a frame
		final int FrameRate = 30;
		final double TimeFrameLimit = (1 / (double)FrameRate) * 1000;

		Netplay NetplayInfo = null;

		System.out.print("Enter a level's name (including the extension): ");
		BufferedReader Reader = new BufferedReader(new InputStreamReader(System.in));

		long TimeStart = System.currentTimeMillis();
		long TimeEnd = System.currentTimeMillis();

		try
		{
			Lvl = new Level(/*Reader.readLine()*/ /*"Stuff/test.txt"*/);

			// Continues here if a the level is found and loaded (no exception)
			if (CheckParm(args, "-playdemo") >= 0)
			{
				Demo = args[CheckParm(args, "-playdemo") + 1];

				if (Demo.charAt(0) == '-')
				{
					// It's just another parameter...
					Demo = null;
				}

			}

			// Check if we specify the number of players. It will be a normal game.
			if (CheckParm(args, "-nodes") >= 0 && Demo == null)
			{
				try
				{
					Nodes = Integer.parseInt(args[CheckParm(args, "-nodes") + 1]);

					if (Nodes < 1 || Nodes > 16)
					{
						Nodes = 2;
					}
				}
				catch (Exception e)
				{
					Nodes = 2;
				}

				System.out.println("Up to " + Nodes + " nodes can join.");

				NetplayInfo = new Netplay(Nodes, 0, 10, 25);

				// So the game starts.
				InGame = true;
			}

			// Check if the player sent an IP. He wants to join the game!
			if(CheckParm(args, "-connect") >= 0)
			{
				String HostIP = null;

				try
				{
					HostIP = args[CheckParm(args, "-connect") + 1];
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

			// Select sound mode
			Sound.SoundModes SoundMode = null;
			if (CheckParm(args, "-sound") >= 0)
			{
				if(args[CheckParm(args, "-sound") + 1].equalsIgnoreCase("2d"))
				{
					// Sound will be bi-dimensional
					SoundMode = Sound.SoundModes.Bi;
				}
				else if(args[CheckParm(args, "-sound") + 1].equalsIgnoreCase("3d"))
				{
					// Sound will be in 3D
					SoundMode = Sound.SoundModes.Three;
				}
				else if(args[CheckParm(args, "-sound") + 1].equalsIgnoreCase("doppler"))
				{
					// Sound will be in 3D with the doppler effect
					SoundMode = Sound.SoundModes.Duppler;
				}
			}

			// Sound (SFX)
			Sound SndDriver = null;
			// Whoa! That's an ugly way to do things...
			for (int Player = 0; Player < Nodes; Player++)
			{
				Lvl.Players.add(new Player(Lvl, SndDriver));
			}

			SndDriver = new Sound(CheckParm(args, "-pcs") >= 0, Lvl.Players, SoundMode);

			// The game is all set up. Open the window.
			try
			{
				Display.setDisplayMode(new DisplayMode(640, 480));
				Display.setResizable(true);
				Display.setTitle("KillBox");
				Display.setVSyncEnabled(true);
				Display.create();
			}
			catch (LWJGLException ex)
			{
				System.out.println("Error while creating the Display: " + ex.getMessage());
			}

			Camera HeadCamera = new Camera(Lvl.Players.get(View), 90, (float) Display.getWidth() / (float) Display.getHeight(), 0.1f, 65536f);
			HeadCamera.ChangePlayer(Lvl.Players.get(View), true);   // Gives the control over the player
			HeadCamera.Menu.InGame = InGame;

			glEnable(GL_TEXTURE_2D);
			glEnable(GL_DEPTH_TEST);    // CLEANUP PLEASE!!!

			// Key presses
			boolean JustPressedSpyKey = false;

			//Mouse.setGrabbed(true);     // Grab the mouse when the game has started.

			int WallsFilter = GL_NEAREST;

			// Change the texture filter for the walls and other types of surface
			if (CheckParm(args, "-near") >= 0)
			{
				// Doesn't do anything, but is here in case the default is changed.
				WallsFilter = GL_NEAREST;
			}
			else if (CheckParm(args, "-bi") >= 0)
			{
				// This is bilinear filtering
				WallsFilter = GL_LINEAR;
			}

			Lvl.LoadLevel("Stuff/maps/" + /*Reader.readLine()*/"genlevel.txt", WallsFilter);

			// Players will spawn at random locations
			for (int Player = 0; Player < Lvl.Players.size(); Player++)
			{
				if (!Lvl.Players.get(Player).SpawnAtRandomSpot())
				{
					System.err.println("Can't find a free spot to spawn player " + (Player + 1) + ". Your map may not have enough of them.");
					System.exit(1);
				}
			}

			// Load the texture "sprites" that will be used to represent the players in the game
			Lvl.Players.get(0).LoadSprites();

			if(NetplayInfo != null)
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

				// Get mouse sensitivity
				HeadCamera.MouseSensitivity = (((float)HeadCamera.Menu.MouseSensitivity.Int())/100);
				// Get sound volume
				SndDriver.VolumeMultiplier = ((float)(HeadCamera.Menu.SFXVolume.Int()))/100;

				// Get sound mode
				if(HeadCamera.Menu.SoundMode.Int() == 0)
				{
					SndDriver.SndMode = SoundMode.Bi;
				}
				else if(HeadCamera.Menu.SoundMode.Int() == 1)
				{
					SndDriver.SndMode = SoundMode.Three;
				}
				else
				{
					SndDriver.SndMode = SoundMode.Duppler;
				}

				// Draw the screen
				HeadCamera.Render(Lvl, Lvl.Players);

				if (HeadCamera.Menu.InGame)
				{
					// Update players
					for (int Player = 0; Player < Lvl.Players.size(); Player++)
					{
						Lvl.Players.get(Player).UpdateIfDead();
					}

					if (Nodes > 1)
					{
						if (CheckParm(args, "-fakenet") < 0)
						{
							// Empty the command that's gonna be sent over the network
							NetplayInfo.PlayerCommand.Reset();
							for (int Player = 0; Player < NetplayInfo.OtherPlayersCommand.size(); Player++)
							{
								NetplayInfo.OtherPlayersCommand.get(Player).Reset();
							}

							// Put the players' move in a command
							NetplayInfo.PlayerCommand.UpdateAngleDiff(Lvl.Players.get(View).AngleDiff);
							NetplayInfo.PlayerCommand.UpdateForwardMove(Lvl.Players.get(View).FrontMove);
							NetplayInfo.PlayerCommand.UpdateSideMove(Lvl.Players.get(View).SideMove);
							NetplayInfo.PlayerCommand.UpdateAction(Lvl.Players.get(View).ActionIsHasShot());

							// Do the network communication through the socket
							NetplayInfo.Update();

							// Update the other player movements
							for (int Player = 0; Player < NetplayInfo.OtherPlayersCommand.size(); Player++)
							{
								int Number = NetplayInfo.OtherPlayersCommand.get(Player).PlayerNumber;

								Lvl.Players.get(Number).ForwardMove(NetplayInfo.OtherPlayersCommand.get(Player).FaceMove);
								Lvl.Players.get(Number).LateralMove(NetplayInfo.OtherPlayersCommand.get(Player).SideMove);
								Lvl.Players.get(Number).AngleTurn(NetplayInfo.OtherPlayersCommand.get(Player).AngleDiff);

								if (NetplayInfo.OtherPlayersCommand.get(Player).Actions == 1)
								{
									if (Lvl.Players.get(Number).Health > 0) {
										// BUG: Don't shot at the first tick, the player shots for no reason.
										if (TicksCount > 0)
										{
											Lvl.Players.get(Number).HitScan(Lvl.Players.get(Number).GetRadianAngle(), 0, 10);
										}
									}
									else
									{
										// Check if the player has completely dropped on the floor
										if (Lvl.Players.get(Number).ViewZ == Lvl.Players.get(Number).HeadOnFloor)
										{
											// Spawn the player
											if (!Lvl.Players.get(Number).SpawnAtRandomSpot()) {
												System.err.println("Can't find a free spot to respawn. The map may not have enough of them.");
												System.exit(1);
											}
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
							}
						}
					}

					// Sound test!!
					if (Keyboard.isKeyDown(Keyboard.KEY_1))
					{
						SndDriver.PlaySound(Lvl.Players.get(View), "button.wav",
								Lvl.Players.get(View).PosX(), Lvl.Players.get(View).PosY(), Lvl.Players.get(View).PosZ());
					}
					if (Keyboard.isKeyDown(Keyboard.KEY_2))
					{
						SndDriver.PlaySound(Lvl.Players.get(View), "chat.wav",
								Lvl.Players.get(View).PosX(), Lvl.Players.get(View).PosY(), Lvl.Players.get(View).PosZ());
					}
					if (Keyboard.isKeyDown(Keyboard.KEY_3))
					{
						SndDriver.PlaySound(Lvl.Players.get(View), "cocking.wav",
								Lvl.Players.get(View).PosX(), Lvl.Players.get(View).PosY(), Lvl.Players.get(View).PosZ());
					}
					if (Keyboard.isKeyDown(Keyboard.KEY_4))
					{
						SndDriver.PlaySound(Lvl.Players.get(View), "death.wav",
								Lvl.Players.get(NetplayInfo.OtherPlayersCommand.get(0).PlayerNumber).PosX(), Lvl.Players.get(NetplayInfo.OtherPlayersCommand.get(0).PlayerNumber).PosY(), Lvl.Players.get(NetplayInfo.OtherPlayersCommand.get(0).PlayerNumber).PosZ());
					}
					if (Keyboard.isKeyDown(Keyboard.KEY_5))
					{
						SndDriver.PlaySound(Lvl.Players.get(View), "respawn.wav",
								Lvl.Players.get(View).PosX(), Lvl.Players.get(View).PosY(), Lvl.Players.get(View).PosZ());
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
				else
				{
					if (NetplayInfo != null)
					{
						if (NetplayInfo.Server != null)
						{
							NetplayInfo.Server.close();
						}

						for (int ConnectionSocket = 0; ConnectionSocket < NetplayInfo.Connections.size(); ConnectionSocket++)
						{
							if (NetplayInfo.Connections.get(ConnectionSocket) != null)
							{
								NetplayInfo.Connections.get(ConnectionSocket).close();
							}
						}
					}

					// Game is not started. Start one using the menu.
					if (HeadCamera.Menu.IsServer)
					{
						Nodes = 2;
						NetplayInfo = new Netplay(Nodes, HeadCamera.Menu.GameMode, HeadCamera.Menu.TimeLimit, HeadCamera.Menu.KillLimit);
						HeadCamera.Menu.InGame = true;

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
							if (!Lvl.Players.get(Player).SpawnAtRandomSpot())
							{
								System.err.println("Can't find a free spot to spawn player " + (Player + 1) + ". Your map may not have enough of them.");
								System.exit(1);
							}
						}
					}
					else if (HeadCamera.Menu.IsClient)
					{
						String IpAddress = HeadCamera.Menu.Address;
						Nodes = 2;
						NetplayInfo = new Netplay(Nodes, IpAddress);

						if (NetplayInfo.Connections.get(0).isConnected())
						{
							// Get the game condition
							HeadCamera.Menu.GameMode = NetplayInfo.GameMode;
							HeadCamera.Menu.TimeLimit = NetplayInfo.TimeLimit;
							HeadCamera.Menu.KillLimit = NetplayInfo.KillLimit;

							// Change the player view
							View = NetplayInfo.View;

							HeadCamera.Menu.InGame = true;
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
							if (!Lvl.Players.get(Player).SpawnAtRandomSpot())
							{
								System.err.println("Can't find a free spot to spawn player " + (Player + 1) + ". Your map may not have enough of them.");
								System.exit(1);
							}
						}
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

				try
				{
					// Timer for sleep
					TimeEnd = System.currentTimeMillis();
					long DeltaTime = TimeEnd - TimeStart;

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
					Thread.sleep((long)TimeFrameLimit - DeltaTime);
					TicksCount++;
				}
				catch (InterruptedException ie)
				{
					System.err.print("The game failed to sleep: " + ie.getMessage());
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
			System.exit(1);
		}
	}

	public static int CheckParm(String[] ArgsList, String Arg)
	{
		for (int i = 0; i < ArgsList.length; i++)
		{
			if (ArgsList[i].equalsIgnoreCase(Arg))
			{
				return i;
			}
		}

		return -1;
	}

	public static void LoadConfigFile(String Name, Menu MenuSystem, Sound SoundSystem)
	{
		// Load the config file and assign every value to their respective variables

		try
		{
			// Load the file
			BufferedReader ConfigFile = new BufferedReader(new FileReader("Stuff/" + Name));

			// Load the user's settings in the same order that they were saved.
			ConfigFile.readLine();
		}
		catch (FileNotFoundException fnfe)
		{
			System.err.println("Cannot load your settings. Configuration file not found.");
		}
		catch (IOException ioe)
		{
			System.err.println("Error while reading from the configuration file.");
		}

	}

	public static void SaveConfigFile(String Name, Menu MenuSystem, Sound SoundSystem)
	{
		// Save the configuration variables in a specific order in a config file

		try
		{
			// Writer for the config file
			PrintWriter ConfigFile = new PrintWriter(new BufferedWriter(new FileWriter("Stuff/" + Name)));

			// Double tabs
			final String Spacing = "\t\t";

			ConfigFile.println("use_freelook" + Spacing + "true");
			ConfigFile.println("show_messages" + Spacing + "true");
			ConfigFile.println("show_hud" + Spacing + "true");
			ConfigFile.println("show_debug" + Spacing + "true");

			ConfigFile.println("grab_mouse" + Spacing + "false");
			ConfigFile.println("enable_chat" + Spacing + "true");
			ConfigFile.println("mouse_sensitivity" + Spacing + "3");

			ConfigFile.println("sfx_volume" + Spacing + "3");
			ConfigFile.println("sound_mode" + Spacing + "2d");	// Can be '2d', '3d' or '3d+'.

			ConfigFile.println("fullscreen" + Spacing + "false");
			ConfigFile.println("enable_filtering" + Spacing + "false");
			ConfigFile.println("view_depth" + Spacing + "100");

			ConfigFile.close();
		}
		catch (FileNotFoundException fnfe)
		{
			System.err.println("Could not save your settings to a configuration file.");
			System.err.println(fnfe.getMessage());
		}
		catch (IOException ioe)
		{
			System.err.println("File error.");
		}
	}
}


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
	static int WallsFilter = GL_NEAREST;

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
		String DefaultMap = "test2.txt";

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
			Lvl = new Level(/*Reader.readLine()*/ /*"res/test.txt"*/);

			// Continues here if a the level is found and loaded (no exception)
			if (CheckParam(args, "-playdemo") >= 0)
			{
				Demo = args[CheckParam(args, "-playdemo") + 1];

				if (Demo.charAt(0) == '-')
				{
					// It's just another parameter...
					Demo = null;
				}

			}

			// Check if we specify the number of players. It will be a normal game.
			if (CheckParam(args, "-nodes") >= 0 && Demo == null)
			{
				try
				{
					Nodes = Integer.parseInt(args[CheckParam(args, "-nodes") + 1]);

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

				NetplayInfo = new Netplay(Nodes, 0, 10, 25, DefaultMap);

				// So the game starts.
				InGame = true;
			}

			// Check if the player sent an IP. He wants to join the game!
			if(CheckParam(args, "-connect") >= 0)
			{
				String HostIP = null;

				try
				{
					HostIP = args[CheckParam(args, "-connect") + 1];
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
			if (CheckParam(args, "-sound") >= 0)
			{
				if(args[CheckParam(args, "-sound") + 1].equalsIgnoreCase("2d"))
				{
					// Sound will be bi-dimensional
					SoundMode = Sound.SoundModes.Bi;
				}
				else if(args[CheckParam(args, "-sound") + 1].equalsIgnoreCase("3d"))
				{
					// Sound will be in 3D
					SoundMode = Sound.SoundModes.Three;
				}
				else if(args[CheckParam(args, "-sound") + 1].equalsIgnoreCase("doppler"))
				{
					// Sound will be in 3D with the doppler effect
					SoundMode = Sound.SoundModes.Duppler;
				}
			}

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
				System.out.println("Error while creating the Display: ");
				ex.printStackTrace();
			}

			// Sound (SFX)
			Sound SndDriver = new Sound(CheckParam(args, "-pcs") >= 0, Lvl.Players, SoundMode);;

			// Whoa! That's an ugly way to do things...
			for (int Player = 0; Player < Nodes; Player++)
			{
				Lvl.Players.add(new Player(Lvl, SndDriver));
			}

			Camera HeadCamera = new Camera(Lvl.Players.get(View), 90, (float) Display.getWidth() / (float) Display.getHeight(), 0.1f, 65536f);
			HeadCamera.ChangePlayer(Lvl.Players.get(View), true);   // Gives the control over the player
			HeadCamera.Menu.InGame = InGame;
			HeadCamera.Menu.SetSoundOut(SndDriver);

			// Set Listener
			SndDriver.SetNewListener(Lvl.Players.get(View));

			glEnable(GL_TEXTURE_2D);
			glEnable(GL_DEPTH_TEST);    // CLEANUP PLEASE!!!

			// Key presses
			boolean JustPressedSpyKey = false;

			//Mouse.setGrabbed(true);     // Grab the mouse when the game has started.

			// Load the configuration file
			LoadConfigFile(ConfigFileName, HeadCamera.Menu, SndDriver);

			// Change the texture filter for the walls and other types of surface
			if (CheckParam(args, "-near") >= 0)
			{
				// Doesn't do anything, but is here in case the default is changed.
				HeadCamera.Menu.Filtering(false);
				WallsFilter = GL_NEAREST;
			}
			else if (CheckParam(args, "-bi") >= 0)
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

			Lvl.LoadLevel("res/maps/" + DefaultMap, WallsFilter);

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

			if (NetplayInfo != null)
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
				else if (HeadCamera.Menu.SoundMode.Int() == 1)
				{
					SndDriver.SndMode = SoundMode.Three;
				}
				else
				{
					SndDriver.SndMode = SoundMode.Duppler;
				}

				// Give the right hear to the right players
				if(NetplayInfo != null)
				{
					SndDriver.SetNewListener(Lvl.Players.get(NetplayInfo.View));
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

					// Look for a winner
					int Winner = 0;
					int PlayerWithMostKills = 0;

					// Find the player with the most kills.
					for (int Player = 0; Player < Lvl.Players.size(); Player++)
					{
						if (Lvl.Players().get(Player).Kills > Lvl.Players().get(0).Kills)
						{
							PlayerWithMostKills = Player;
						}
					}

					// Check if someone may have won. First check if the game is infinite.
					if (NetplayInfo.TimeLimit != 0 || NetplayInfo.KillLimit != 0)
					{
						// Check if we have one of the two cases where the game would end
						if (TicksCount / FrameRate >= NetplayInfo.GetTimeLimit() || Lvl.Players().get(PlayerWithMostKills).Kills >= NetplayInfo.GetKillLimit())
						{
							// Check if GameMode is "FlagTag"
							if (NetplayInfo.GameMode == 2)
							{
								// Search for the player that had the flag for the longest time
								for (int Player = 0; Player < Lvl.Players.size(); Player++)
								{
									if (Lvl.Players().get(Player).FlagTime > Lvl.Players().get(Winner).FlagTime)
									{
										Winner = Player;

										if (Player == Lvl.Players.size() - 1)
										{
											// Search a player that has the same score
											for (int PlayerScoreEqual = 0; PlayerScoreEqual < Lvl.Players.size(); PlayerScoreEqual++)
											{
												if (Winner != -1 && PlayerScoreEqual != Winner)
												{
													if (Lvl.Players().get(PlayerScoreEqual).FlagTime == Lvl.Players().get(Winner).FlagTime)
													{
														Winner = -1;
													}
												}
											}
										}
									}
								}
							}
							else	// A player wins when he has the most kills
							{
								// get the player that has the most kills
								Winner = PlayerWithMostKills;

								// Search for a player that has the same score
								for (int PlayerScoreEqual = 0; PlayerScoreEqual < Lvl.Players.size(); PlayerScoreEqual++)
								{
									if (Winner != -1 && PlayerScoreEqual != Winner)
									{
										if (Lvl.Players().get(PlayerScoreEqual).Kills == Lvl.Players().get(Winner).Kills)
										{
											// Tie !
											Winner = -1;
										}
									}
								}
							}

							// End the game
							HeadCamera.Menu.InGame = false;

							// Someone wins. Tell if it's a tie.
							if (Winner == -1)
							{
								HeadCamera.Menu.NewMessageToShow("Game is a tie.");
							}
							else
							{
								if (NetplayInfo.View == Winner)
								{
									HeadCamera.Menu.NewMessageToShow("You win!");
								}
								else
								{
									HeadCamera.Menu.NewMessageToShow("The other player wins.");
								}
							}
						}
					}

					// Check if we're in a multiplayer game
					if (Nodes > 1)
					{
						if (CheckParam(args, "-fakenet") < 0)
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
							Action += Lvl.Players.get(View).SelectedWeapon;

							// Put the players' move in a command
							NetplayInfo.PlayerCommand.UpdateAngleDiff(Lvl.Players.get(View).AngleDiff);
							NetplayInfo.PlayerCommand.UpdateForwardMove(Lvl.Players.get(View).FrontMove);
							NetplayInfo.PlayerCommand.UpdateSideMove(Lvl.Players.get(View).SideMove);
							NetplayInfo.PlayerCommand.UpdateAction(Action);

							Lvl.Players.get(View).Action = NetplayInfo.PlayerCommand.Actions;

							// Do the network communication through the socket
							if (NetplayInfo.Update())
							{
								// Update the other player movements
								for (int Player = 0; Player < NetplayInfo.OtherPlayersCommand.size(); Player++)
								{
									int Number = NetplayInfo.OtherPlayersCommand.get(Player).PlayerNumber;

									if (TicksCount > 1)
									{
										// Not allowing movements on the first tick prevents a bug when player may move before the game has even started.
										Lvl.Players.get(Number).ForwardMove(NetplayInfo.OtherPlayersCommand.get(Player).FaceMove);
										Lvl.Players.get(Number).LateralMove(NetplayInfo.OtherPlayersCommand.get(Player).SideMove);
										Lvl.Players.get(Number).AngleTurn(NetplayInfo.OtherPlayersCommand.get(Player).AngleDiff);
										Lvl.Players.get(Number).Action = NetplayInfo.PlayerCommand.Actions;

										// Move the server(player1) first and then the client(player2)
										for (int PlayerToMove = 0; PlayerToMove < Lvl.Players.size(); PlayerToMove++)
										{
											Lvl.Players.get(PlayerToMove).ExecuteForwardMove(Lvl.Players.get(PlayerToMove).FrontMove);
											Lvl.Players.get(PlayerToMove).ExecuteLateralMove(Lvl.Players.get(PlayerToMove).SideMove);
											Lvl.Players.get(PlayerToMove).ExecuteAngleTurn(Lvl.Players.get(PlayerToMove).AngleDiff);
										}
									}
									else // To enable the first shot at the start
									{
										for (int a = 0; a < Lvl.Players.size(); a++)
										{
											Lvl.Players.get(a).Spawned = false;
										}
									}

									// Check the action of each player in order (Player1 then Player2)
									if (View == 0) // If I'm the server
									{
										// The server first (me)
										// Reload
										if (NetplayInfo.PlayerCommand.Actions / 1000 == 1)
										{
											Lvl.Players.get(View).ReloadWeapon();
											NetplayInfo.PlayerCommand.Actions -= 1000;
										}
										// Command to shot
										if (NetplayInfo.PlayerCommand.Actions / 100 == 1 || NetplayInfo.PlayerCommand.Actions / 100 == 11) // Do my command first
										{
											if (Lvl.Players.get(View).Health > 0 && !Lvl.Players.get(View).Spawned)
											{
												Lvl.Players.get(View).HitScan(Lvl.Players.get(View).GetRadianAngle(), 0, 10);
											}
											else if(Lvl.Players.get(View).Spawned)
											{
												Lvl.Players.get(View).Spawned = false;
											}
											else
											{
												// Check if the player has completely dropped on the floor
												if (Lvl.Players.get(View).ViewZ == Lvl.Players.get(View).HeadOnFloor)
												{
													// Spawn the player
													if (!Lvl.Players.get(View).SpawnAtRandomSpot(true))
													{
														System.err.println("Can't find a free spot to respawn. The map may not have enough of them.");
														System.exit(1);
													}
												}
											}

											NetplayInfo.PlayerCommand.Actions -= 100;
										}
										if(Lvl.Players.get(View).Spawned)
										{
											Lvl.Players.get(View).Spawned = false;
										}
										System.err.println("1: " + NetplayInfo.PlayerCommand.Actions + "		Lvl.Players.get(View) (the if)i'm the server");
										// Change weapon 1
										if (NetplayInfo.PlayerCommand.Actions == 1)
										{
											Lvl.Players.get(View).ChangeWeapon(1);
											Lvl.Players.get(View).ExecuteChangeWeapon();
										}
										else if(NetplayInfo.PlayerCommand.Actions == 2)
										{
											Lvl.Players.get(View).ChangeWeapon(2);
											Lvl.Players.get(View).ExecuteChangeWeapon();
										}
										else if(NetplayInfo.PlayerCommand.Actions == 3)
										{
											Lvl.Players.get(View).ChangeWeapon(3);
											Lvl.Players.get(View).ExecuteChangeWeapon();
										}
										else if(NetplayInfo.PlayerCommand.Actions == 4)
										{
											Lvl.Players.get(View).ChangeWeapon(4);
											Lvl.Players.get(View).ExecuteChangeWeapon();
										}

										// The client second
										// Reload
										if (NetplayInfo.OtherPlayersCommand.get(Player).Actions / 1000 == 1)
										{
											Lvl.Players.get(Number).ReloadWeapon();
											NetplayInfo.OtherPlayersCommand.get(Player).Actions -= 1000;
										}
										if (NetplayInfo.OtherPlayersCommand.get(Player).Actions / 100 == 1 || NetplayInfo.OtherPlayersCommand.get(Player).Actions / 100 == 11) // Do the client command second
										{
											if (Lvl.Players.get(Number).Health > 0 && !Lvl.Players.get(Number).Spawned)
											{
												Lvl.Players.get(Number).HitScan(Lvl.Players.get(Number).GetRadianAngle(), 0, 10);
											}
											else if(Lvl.Players.get(Number).Spawned)
											{
												Lvl.Players.get(Number).Spawned = false;
											}
											else
											{
												// Check if the player has completely dropped on the floor
												if (Lvl.Players.get(Number).ViewZ == Lvl.Players.get(Number).HeadOnFloor)
												{
													// Spawn the player
													if (!Lvl.Players.get(Number).SpawnAtRandomSpot(true))
													{
														System.err.println("Can't find a free spot to respawn. The map may not have enough of them.");
														System.exit(1);
													}
												}
											}

											NetplayInfo.OtherPlayersCommand.get(Player).Actions -= 100;
										}
										if(Lvl.Players.get(Number).Spawned)
										{
											Lvl.Players.get(Number).Spawned = false;
										}
										System.err.println("2: " + NetplayInfo.PlayerCommand.Actions + "		Lvl.Players.get(Number) (the if)i'm the server");
										// Change weapon 1
										if (NetplayInfo.OtherPlayersCommand.get(Player).Actions == 1)
										{
											Lvl.Players.get(Number).ChangeWeapon(1);
											Lvl.Players.get(Number).ExecuteChangeWeapon();
										}
										else if(NetplayInfo.OtherPlayersCommand.get(Player).Actions == 2)
										{
											Lvl.Players.get(Number).ChangeWeapon(2);
											Lvl.Players.get(Number).ExecuteChangeWeapon();
										}
										else if(NetplayInfo.OtherPlayersCommand.get(Player).Actions == 3)
										{
											Lvl.Players.get(Number).ChangeWeapon(3);
											Lvl.Players.get(Number).ExecuteChangeWeapon();
										}
										else if(NetplayInfo.OtherPlayersCommand.get(Player).Actions == 4)
										{
											Lvl.Players.get(Number).ChangeWeapon(4);
											Lvl.Players.get(Number).ExecuteChangeWeapon();
										}
									}
									else // If I'm the client
									{
										// The server first
										// Reload
										if (NetplayInfo.OtherPlayersCommand.get(Player).Actions / 1000 == 1)
										{
											Lvl.Players.get(Number).ReloadWeapon();
											NetplayInfo.OtherPlayersCommand.get(Player).Actions -= 1000;
										}
										if (NetplayInfo.OtherPlayersCommand.get(Player).Actions / 100 == 1 || NetplayInfo.OtherPlayersCommand.get(Player).Actions / 100 == 11) // Do the client command second
										{
											if (Lvl.Players.get(Number).Health > 0 && !Lvl.Players.get(Number).Spawned)
											{
												Lvl.Players.get(Number).HitScan(Lvl.Players.get(Number).GetRadianAngle(), 0, 10);
											}
											else if(Lvl.Players.get(Number).Spawned)
											{
												Lvl.Players.get(Number).Spawned = false;
											}
											else
											{
												// Check if the player has completely dropped on the floor
												if (Lvl.Players.get(Number).ViewZ == Lvl.Players.get(Number).HeadOnFloor)
												{
													// Spawn the player
													if (!Lvl.Players.get(Number).SpawnAtRandomSpot(true))
													{
														System.err.println("Can't find a free spot to respawn. The map may not have enough of them.");
														System.exit(1);
													}
												}
											}

											NetplayInfo.OtherPlayersCommand.get(Player).Actions -= 100;
										}
										if(Lvl.Players.get(Number).Spawned)
										{
											Lvl.Players.get(Number).Spawned = false;
										}
										System.err.println("3: " + NetplayInfo.PlayerCommand.Actions + "		Lvl.Players.get(Number) (the else)I'm the client");
										// Change weapon 1
										if (NetplayInfo.OtherPlayersCommand.get(Player).Actions == 1)
										{
											Lvl.Players.get(Number).ChangeWeapon(1);
											Lvl.Players.get(Number).ExecuteChangeWeapon();
										}
										else if(NetplayInfo.OtherPlayersCommand.get(Player).Actions == 2)
										{
											Lvl.Players.get(Number).ChangeWeapon(2);
											Lvl.Players.get(Number).ExecuteChangeWeapon();
										}
										else if(NetplayInfo.OtherPlayersCommand.get(Player).Actions == 3)
										{
											Lvl.Players.get(Number).ChangeWeapon(3);
											Lvl.Players.get(Number).ExecuteChangeWeapon();
										}
										else if(NetplayInfo.OtherPlayersCommand.get(Player).Actions == 4)
										{
											Lvl.Players.get(Number).ChangeWeapon(4);
											Lvl.Players.get(Number).ExecuteChangeWeapon();
										}
										// The client second (me)
										// Reload
										if (NetplayInfo.PlayerCommand.Actions / 1000 == 1)
										{

											Lvl.Players.get(View).ReloadWeapon();
											NetplayInfo.PlayerCommand.Actions -= 1000;
										}
										// Command to shot
										if (NetplayInfo.PlayerCommand.Actions / 100 == 1 || NetplayInfo.PlayerCommand.Actions / 100 == 11) // Do my command first
										{
											if (Lvl.Players.get(View).Health > 0 && !Lvl.Players.get(View).Spawned)
											{
												Lvl.Players.get(View).HitScan(Lvl.Players.get(View).GetRadianAngle(), 0, 10);
											}
											else if(Lvl.Players.get(View).Spawned)
											{
												Lvl.Players.get(View).Spawned = false;
											}
											else
											{
												// Check if the player has completely dropped on the floor
												if (Lvl.Players.get(View).ViewZ == Lvl.Players.get(View).HeadOnFloor)
												{
													// Spawn the player
													if (!Lvl.Players.get(View).SpawnAtRandomSpot(true))
													{
														System.err.println("Can't find a free spot to respawn. The map may not have enough of them.");
														System.exit(1);
													}
												}
											}
											NetplayInfo.PlayerCommand.Actions -= 100;
										}
										if(Lvl.Players.get(View).Spawned)
										{
											Lvl.Players.get(View).Spawned = false;
										}
										System.err.println("4: " + NetplayInfo.PlayerCommand.Actions + "		Lvl.Players.get(View) (the else)I'm the client");
										// Change weapon 1
										if (NetplayInfo.PlayerCommand.Actions == 1)
										{
											Lvl.Players.get(View).ChangeWeapon(1);
											Lvl.Players.get(View).ExecuteChangeWeapon();
										}
										else if(NetplayInfo.PlayerCommand.Actions == 2)
										{
											Lvl.Players.get(View).ChangeWeapon(2);
											Lvl.Players.get(View).ExecuteChangeWeapon();
										}
										else if(NetplayInfo.PlayerCommand.Actions == 3)
										{
											Lvl.Players.get(View).ChangeWeapon(3);
											Lvl.Players.get(View).ExecuteChangeWeapon();
										}
										else if(NetplayInfo.PlayerCommand.Actions == 4)
										{
											Lvl.Players.get(View).ChangeWeapon(4);
											Lvl.Players.get(View).ExecuteChangeWeapon();
										}
									}
								}
							}
							else
							{
								if (HeadCamera.Menu.InGame)
								{
									HeadCamera.Menu.NewMessageToShow("Multi-player game ended.");
								}
								HeadCamera.Menu.InGame = false;
							}
							/*else
							{
								// Couldn't get the command
								for (int Player = 0; Player < Lvl.Players.size(); Player++)
								{
									if (Player != View)
									{
										Lvl.Players.get(Player).Health = 0;
									}
								}
							}*/

							for (int Player = 0; Player < Lvl.Players.size(); Player++)
							{
								// BUG: Cheap fix player strafing not reset. FUCK!
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
					if (TicksCount > 1)
					{
						// Make the player move even if it's not a multiplayer game
						Lvl.Players.get(View).ExecuteForwardMove(Lvl.Players.get(View).FrontMove);
						Lvl.Players.get(View).ExecuteLateralMove(Lvl.Players.get(View).SideMove);
						Lvl.Players.get(View).ExecuteAngleTurn(Lvl.Players.get(View).AngleDiff);

						if (Lvl.Players.get(View).ActionIsHasShot() == 100)
						{
							if (Lvl.Players.get(View).Health > 0)
							{
								Lvl.Players.get(View).HitScan(Lvl.Players.get(View).GetRadianAngle(), 0, 10);
							}
						}
					}
					// Kill inactive players
					/*for (int Player = 0; Player < Lvl.Players.size(); Player++)
					{
						if (Lvl.Players.get(Player) != HeadCamera.CurrentPlayer())
						{
							Lvl.Players.get(Player).Health = 0;
						}
					}*/

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
						Display.setTitle("KillBox (Server)");
						Nodes = 2;

						NetplayInfo = new Netplay(Nodes, HeadCamera.Menu.GameMode, HeadCamera.Menu.TimeLimit, HeadCamera.Menu.KillLimit, HeadCamera.Menu.Map);
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
									System.err.println("Can't find a free spot to spawn player " + (Player + 1) + ". Your map may not have enough of them.");
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
						Display.setTitle("KillBox (Client)");
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
								System.err.println("Can't find a free spot to spawn player " + (Player + 1) + ". Your map may not have enough of them.");
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

	public static int CheckParam(String[] ArgsList, String Arg)
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
			MenuSystem.Active(true);
			// Load the file
			BufferedReader ConfigFile = new BufferedReader(new FileReader("res/" + Name));

			// Load the user's settings in the same order that they were saved.
			MenuSystem.FreeLook(Boolean.parseBoolean((ConfigFile.readLine()).split("\t", 2)[1].trim()));
			MenuSystem.ShowMessage(Boolean.parseBoolean((ConfigFile.readLine()).split("\t", 2)[1].trim()));
			MenuSystem.ShowHud.Bool(Boolean.parseBoolean((ConfigFile.readLine()).split("\t", 2)[1].trim()));
			MenuSystem.ShowDebug(Boolean.parseBoolean((ConfigFile.readLine()).split("\t", 2)[1].trim()));

			MenuSystem.GrabMouse(Boolean.parseBoolean((ConfigFile.readLine()).split("\t", 2)[1].trim()));
			MenuSystem.EnableChat(Boolean.parseBoolean((ConfigFile.readLine()).split("\t", 2)[1].trim()));
			MenuSystem.MouseSensibility(Integer.parseInt((ConfigFile.readLine()).split("\t", 2)[1].trim()));
			MenuSystem.SFXVolume(Integer.parseInt((ConfigFile.readLine()).split("\t", 2)[1].trim()));

			String SoundMode = (ConfigFile.readLine()).split("\t", 2)[1].trim();
			if (SoundMode.equals("3d+"))
			{
				MenuSystem.SoundMode(2);
			}
			else if (SoundMode.equals("3d"))
			{
				MenuSystem.SoundMode(1);
			}
			else
			{
				MenuSystem.SoundMode(0);
			}

			MenuSystem.Fullscreen(Boolean.parseBoolean((ConfigFile.readLine()).split("\t", 2)[1].trim()));
			MenuSystem.Filtering(Boolean.parseBoolean((ConfigFile.readLine()).split("\t", 2)[1].trim()));
			MenuSystem.ViewDepth(Integer.parseInt((ConfigFile.readLine()).split("\t", 2)[1].trim()));

			MenuSystem.Active(false);

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

	}

	public static void SaveConfigFile(String Name, Menu MenuSystem, Sound SoundSystem)
	{
		// Save the configuration variables in a specific order in a config file

		try
		{
			// Writer for the config file
			PrintWriter ConfigFile = new PrintWriter(new BufferedWriter(new FileWriter("res/" + Name)));

			// Double tabs
			final String Spacing = "\t\t";

			ConfigFile.println("use_freelook" + Spacing + MenuSystem.FreeLook.Bool());
			ConfigFile.println("show_messages" + Spacing + MenuSystem.ShowMessage.Bool());
			ConfigFile.println("show_hud" + Spacing + MenuSystem.ShowHud.Bool());
			ConfigFile.println("show_debug" + Spacing + MenuSystem.ShowDebug());

			ConfigFile.println("grab_mouse" + Spacing + MenuSystem.GrabMouse.Bool());
			ConfigFile.println("enable_chat" + Spacing + MenuSystem.EnableChat.Bool());
			ConfigFile.println("mouse_sensitivity" + Spacing + MenuSystem.MouseSensitivity.Int());

			ConfigFile.println("sfx_volume" + Spacing + MenuSystem.SFXVolume.Int());

			// Can be '2d', '3d' or '3d+'.
			if (SoundSystem.SndMode == Sound.SoundModes.Bi)
			{
				ConfigFile.println("sound_mode" + Spacing + "2d");
			}
			else if (SoundSystem.SndMode == Sound.SoundModes.Three)
			{
				ConfigFile.println("sound_mode" + Spacing + "3d");
			}
			else
			{
				ConfigFile.println("sound_mode" + Spacing + "3d+");
			}

			ConfigFile.println("fullscreen" + Spacing + MenuSystem.Fullscreen.Bool());
			ConfigFile.println("enable_filtering" + Spacing + MenuSystem.Filtering.Bool());
			ConfigFile.println("view_depth" + Spacing + MenuSystem.ViewDepth.Int());

			ConfigFile.close();
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
	}
}


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
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import static org.lwjgl.opengl.GL11.*;

import java.io.*;
import java.util.*;

public class Game
{
	static int TicksCount = 0;

	public static void main(String[] args)
	{
		System.out.println("			KillBox v2.??? (alpha)");
		System.out.println("			======================");

		// Nodes are computers where there is a player
		int Nodes = 1;
		String Demo = null;
		Level Lvl = null;
		int View = 0;

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

				NetplayInfo = new Netplay(Nodes);
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
					System.out.println("Bad IP address specified to '-connect' argument.");
				}

				NetplayInfo = new Netplay(Nodes, HostIP);

				// Changer le nombre de Nodes pour le joueur
				Nodes = NetplayInfo.Nodes;

				// Changer la view du joueur
				View = NetplayInfo.View;
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
			for (int i = 1; i <= Nodes; i++)
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
			Random Rand = new Random();
			int TiresBeforeFreeSpawnIsFound = 0;
			if(Lvl.Players.size() > 0)
			{
				int RandomNumber = Rand.GiveNumber();
				Thing SomeSpawn = Lvl.Spawns.get(RandomNumber % Lvl.Spawns.size());

				while (!Lvl.Players.get(0).Spawn(SomeSpawn.PosX(), SomeSpawn.PosY(), SomeSpawn.PosZ(), SomeSpawn.Angle))
				{
					RandomNumber = Rand.GiveNumber();
					SomeSpawn = Lvl.Spawns.get(RandomNumber % Lvl.Spawns.size());

					if (TiresBeforeFreeSpawnIsFound > 30)
					{
						System.out.println("Can't find a free spot to spawn. Your map may not have enough of them.");
						System.exit(1);
					}
					TiresBeforeFreeSpawnIsFound++;
				}

				TiresBeforeFreeSpawnIsFound = 0;
			}

			if(Lvl.Players.size() > 1)
			{
				int RandomNumber = Rand.GiveNumber();
				Thing SomeSpawn = Lvl.Spawns.get(RandomNumber % Lvl.Spawns.size());

				while (!Lvl.Players.get(1).Spawn(SomeSpawn.PosX(), SomeSpawn.PosY(), SomeSpawn.PosZ(), SomeSpawn.Angle))
				{
					RandomNumber = Rand.GiveNumber();
					SomeSpawn = Lvl.Spawns.get(RandomNumber % Lvl.Spawns.size());

					if (TiresBeforeFreeSpawnIsFound > 30)
					{
						System.out.println("Can't find a free spot to spawn. Your map may not have enough of them.");
						System.exit(1);
					}
					TiresBeforeFreeSpawnIsFound++;
				}

				TiresBeforeFreeSpawnIsFound = 0;
			}

			if(Lvl.Players.size() > 2)
			{
				int RandomNumber = Rand.GiveNumber();
				Thing SomeSpawn = Lvl.Spawns.get(RandomNumber % Lvl.Spawns.size());

				while (!Lvl.Players.get(2).Spawn(SomeSpawn.PosX(), SomeSpawn.PosY(), SomeSpawn.PosZ(), SomeSpawn.Angle))
				{
					RandomNumber = Rand.GiveNumber();
					SomeSpawn = Lvl.Spawns.get(RandomNumber % Lvl.Spawns.size());

					if (TiresBeforeFreeSpawnIsFound > 30)
					{
						System.out.println("Can't find a free spot to spawn. Your map may not have enough of them.");
						System.exit(1);
					}
					TiresBeforeFreeSpawnIsFound++;
				}

				TiresBeforeFreeSpawnIsFound = 0;
			}

			if(Lvl.Players.size() > 3)
			{
				int RandomNumber = Rand.GiveNumber();
				Thing SomeSpawn = Lvl.Spawns.get(RandomNumber % Lvl.Spawns.size());

				while (!Lvl.Players.get(3).Spawn(SomeSpawn.PosX(), SomeSpawn.PosY(), SomeSpawn.PosZ(), SomeSpawn.Angle))
				{
					RandomNumber = Rand.GiveNumber();
					SomeSpawn = Lvl.Spawns.get(RandomNumber % Lvl.Spawns.size());

					if (TiresBeforeFreeSpawnIsFound > 30)
					{
						System.out.println("Can't find a free spot to spawn. Your map may not have enough of them.");
						System.exit(1);
					}
					TiresBeforeFreeSpawnIsFound++;
				}

				TiresBeforeFreeSpawnIsFound = 0;
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

			// The main game loop
			while (!Display.isCloseRequested() && !HeadCamera.Menu.UserWantsToExit)
			{
				TimeStart = System.currentTimeMillis();

				// Draw the screen
				HeadCamera.Render(Lvl, Lvl.Players);

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

						// Do the network communication through the socket
						NetplayInfo.Update();

						// Update the other player movements
						for (int Player = 0; Player < NetplayInfo.OtherPlayersCommand.size(); Player++)
						{
							Lvl.Players.get(NetplayInfo.OtherPlayersCommand.get(Player).PlayerNumber).ForwardMove(NetplayInfo.OtherPlayersCommand.get(Player).FaceMove);
							Lvl.Players.get(NetplayInfo.OtherPlayersCommand.get(Player).PlayerNumber).LateralMove(NetplayInfo.OtherPlayersCommand.get(Player).SideMove);
							Lvl.Players.get(NetplayInfo.OtherPlayersCommand.get(Player).PlayerNumber).AngleTurn(NetplayInfo.OtherPlayersCommand.get(Player).AngleDiff);
						}

						for (int Player = 0; Player < Lvl.Players.size(); Player++)
						{
							// BUG: Cheap fix player strafing not reset. FUCK!
							Lvl.Players.get(Player).SideMove = 0;
							Lvl.Players.get(Player).FrontMove = 0;
							Lvl.Players.get(Player).AngleDiff = 0;
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

			// Close the display
			Display.destroy();
		}
		catch (/*IO*/Exception ioe)
		{
			System.err.println(ioe.getMessage());
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
}


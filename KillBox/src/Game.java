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

		Netplay NetplayInfo = null;

		System.out.print("Enter a level's name (including the extension): ");
		BufferedReader Reader = new BufferedReader(new InputStreamReader(System.in));
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

				NetplayInfo = new Netplay(true,Nodes);
			}

			// Check if the player sent an IP. He wants to join the game!
			// IP : -connect 127.0.0.1
			if(CheckParm(args, "-connect") >= 0)
			{
				try
				{
					String PlayerIp = args[CheckParm(args, "-connect") + 1];
					System.out.println("IP : " + PlayerIp);
				}
				catch (Exception e)
				{
					System.out.println("Mistake in IP argu");
				}

				NetplayInfo = new Netplay(false,Nodes/*useless*/);

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

			// ENLEVER REMPLACER PAR LES SPAWM
			// Modifier la position initial des joueurs
			//--------------------------------------------------------------------------------------------------------------------------------------
			if(Lvl.Players.size() > 0)
			{
				Lvl.Players.get(0).PosX(-50);
				Lvl.Players.get(0).PosY(0);
			}

			if(Lvl.Players.size() > 1)
			{
				Lvl.Players.get(1).PosX(-100);
				Lvl.Players.get(1).PosY(-100);
			}

			if(Lvl.Players.size() > 2)
			{
				Lvl.Players.get(2).PosX(50);
				Lvl.Players.get(2).PosY(50);
			}

			if(Lvl.Players.size() > 3)
			{
				Lvl.Players.get(3).PosX(0);
				Lvl.Players.get(3).PosY(50);
			}

			SndDriver = new Sound(CheckParm(args, "-pcs") >= 0, Lvl.Players, SoundMode);

			// The game is all setted up. Open the window.
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

			Lvl.LoadLevel("Stuff/test.txt", WallsFilter);

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
			while (!Display.isCloseRequested())
			{
				// Draw the screen
				HeadCamera.Render(Lvl, Lvl.Players);

				if (Nodes > 1)
				{
					NetplayInfo.PlayerCommand.Reset();
					for(int Player = 0; Player < NetplayInfo.OtherPlayersCommand.size();Player++)
					{
						NetplayInfo.OtherPlayersCommand.get(Player).Reset();
					}

					NetplayInfo.PlayerCommand.UpdateAngleDiff(Lvl.Players.get(View).AngleDiff);
					NetplayInfo.PlayerCommand.UpdateForwardMove(Lvl.Players.get(View).FrontMove);
					NetplayInfo.PlayerCommand.UpdateSideMove(Lvl.Players.get(View).SideMove);

					NetplayInfo.Update();

					// Print the number of command sent
					// System.out.println("PlyrCmd: " + NetplayInfo.PlayerCommand.Number);
					// System.out.println("OtherPlyrCmd: " + NetplayInfo.OtherPlayersCommand.get(0).Number);

					// Update the other player movements
					for(int Player = 0; Player < NetplayInfo.OtherPlayersCommand.size();Player++)
					{
						Lvl.Players.get(NetplayInfo.OtherPlayersCommand.get(Player).PlayerNumber).ForwardMove(NetplayInfo.OtherPlayersCommand.get(Player).FaceMove);
						Lvl.Players.get(NetplayInfo.OtherPlayersCommand.get(Player).PlayerNumber).LateralMove(NetplayInfo.OtherPlayersCommand.get(Player).SideMove);
						Lvl.Players.get(NetplayInfo.OtherPlayersCommand.get(Player).PlayerNumber).AngleTurn(NetplayInfo.OtherPlayersCommand.get(Player).AngleDiff);
					}

					for(int Player = 0; Player < Lvl.Players.size(); Player++)
					{
						// BUG: Cheap fix player strafing not reset. FUCK!
						Lvl.Players.get(Player).SideMove = 0;
					}
				}
/*
				// player 2 turns in circles
				Lvl.Players.get(1).ForwardMove(1);
				Lvl.Players.get(1).AngleTurn((short) -200);
				Lvl.Players.get(1).Move();

				// player 3 turns in circles
				Lvl.Players.get(2).ForwardMove(-1);
				Lvl.Players.get(2).LateralMove(1);
				Lvl.Players.get(2).AngleTurn((short) 500);
				Lvl.Players.get(2).Move();

				// player 4 turns in circles
				Lvl.Players.get(3).ForwardMove(1);
				Lvl.Players.get(3).LateralMove(-1);
				Lvl.Players.get(3).AngleTurn((short) -500);
				Lvl.Players.get(3).Move();
*/
				if (Keyboard.isKeyDown(Keyboard.KEY_F12) && !JustPressedSpyKey)
				{
					boolean Control = false;

					do
					{
						View = (View + 1) % Lvl.Players.size();

						if (View == 0)
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
					Thread.sleep(30);
				}
				catch (InterruptedException ie)
				{
					System.out.print("The game failed to sleep: " + ie.getMessage());
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


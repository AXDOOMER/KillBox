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
		int Nodes = 4;
		String Demo = null;
		Level Lvl = null;
        int View = 0;

		System.out.print("Enter a level's name (including the extension): ");
		BufferedReader Reader = new BufferedReader(new InputStreamReader(System.in));
		//try
		//{
			Lvl = new Level(/*Reader.readLine()*/ "Stuff/test.txt");

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
			}

			// Sound (SFX)
			Sound SndDriver = null;
			// Whoa! That's an ugly way to do things...
			for (int i = 1; i <= Nodes; i++) {
				Lvl.Players.add(new Player(Lvl, SndDriver));
			}
			SndDriver = new Sound(CheckParm(args, "-pcs") >= 0, Lvl.Players);

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

			Camera HeadCamera = new Camera(Lvl.Players.get(0), 90, (float)Display.getWidth() / (float)Display.getHeight(), 0.1f, 8192f);
            HeadCamera.ChangePlayer(Lvl.Players.get(View), true);   // Gives the control over the player

			glEnable(GL_TEXTURE_2D);
			glEnable(GL_DEPTH_TEST);    // CLEANUP PLEASE!!!

            // Key presses
            boolean JustPressedSpyKey = false;

            //Mouse.setGrabbed(true);     // Grab the mouse when the game has started.

			while (!Display.isCloseRequested())
			{
				// Draw the screen
				HeadCamera.Render(Lvl, Lvl.Players);

                // player 2 turns in circles
				Lvl.Players.get(1).ForwardMove(1);
				Lvl.Players.get(1).AngleTurn((short) -200);

                // player 3 turns in circles
				Lvl.Players.get(2).ForwardMove(-1);
				Lvl.Players.get(2).LateralMove(1);
				Lvl.Players.get(2).AngleTurn((short) 500);

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
		/*}
		catch (IOException ioe)
		{
			System.err.println(ioe.getMessage());
			System.exit(1);
		}*/
	}

	public void Error(String Message)
	{
		System.out.println("I_Error: " + Message);
		System.exit(1);
	}

	public static int CheckParm(String[] ArgsList, String Arg)
	{
		for(int i = 0; i < ArgsList.length; i++)
		{
			if(ArgsList[i].equals(Arg))
			{
				return i;
			}
		}

		return -1;
	}
}


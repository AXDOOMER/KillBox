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
import java.net.*;
import java.util.*;

public class Game
{
	public static void main(String[] args)
	{
		System.out.println("			KillBox v2.??? (alpha)");
		System.out.println("			======================");
		int Ticks = 0;

		// Sound (SFX)
		Sound SndDriver = new Sound(CheckParm(args, "-pcs") >= 0);

		// Create players list
		ArrayList<Player> Players = new ArrayList<>();
		int Nodes = 4;
		String Demo = null;
		Level Lvl = null;

		System.out.print("Enter a level's name (including the extension): ");
		BufferedReader Reader = new BufferedReader(new InputStreamReader(System.in));
		//try
		//{
			Lvl = new Level(/*Reader.readLine()*/ "Stuff/test.txt");

			// Continues here if a the level is found and loaded (no exception)
			if (CheckParm(args, "-playdemo") >= 0) {
				Demo = args[CheckParm(args, "-playdemo") + 1];

				if (Demo.charAt(0) == '-') {
					// It's just another parameter...
					Demo = null;
				}

			}

			// Check if we specify the number of players. It will be a normal game.
			if (CheckParm(args, "-nodes") >= 0 && Demo == null) {
				try {
					Nodes = Integer.parseInt(args[CheckParm(args, "-nodes") + 1]);

					if (Nodes < 1 || Nodes > 16) {
						Nodes = 2;
					}
				} catch (Exception e) {
					Nodes = 2;
				}

				System.out.println("Up to " + Nodes + " nodes can join.");
			}

			for (int i = 1; i <= Nodes; i++) {
				Players.add(new Player(Lvl));
			}

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

			Camera HeadCamera = new Camera(Players.get(0), 90, (float)Display.getWidth() / (float)Display.getHeight(), 0.1f, 1000f);

			glEnable(GL_TEXTURE_2D);
			glEnable(GL_DEPTH_TEST);    // CLEANUP PLEASE!!!
			Texture Door = new Texture("Stuff/DOOR9_1.bmp", GL_NEAREST);	// Only to test
			Door.Bind();

			int[] TextXcoords = {0, 1, 1 ,0};
			int[] TextYcoords = {0, 0, 1 ,1};

			while (!Display.isCloseRequested())
			{
				if (Display.wasResized())
				{
					// Doesn't work
					HeadCamera.ChangeProperties(90, (float)Display.getWidth() / (float)Display.getHeight(), 0.1f, 1000f);

					glViewport(0, 0, Display.getWidth(), Display.getHeight());
				}

				glClearColor(0.0f, 0.0f, 0.5f, 0.0f);
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
				glLoadIdentity();
				HeadCamera.UseView();

				if (!Mouse.isGrabbed())
				{
					Mouse.setGrabbed(true); // Hide mouse
				}
				short MouseTurnH = (short)Mouse.getDX();
				Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);

				// Print DEBUG stats
				System.out.println("    X: " + (int) Players.get(0).PosX + "  Y: " + (int) Players.get(0).PosY + "   Z: " + (int) Players.get(0).PosZ
						+ " Ra: " + Players.get(0).GetRadianAngle() + " Cam: " + HeadCamera.RotY() +
						"   mX: " + Mouse.getX() + "   mY: " + Mouse.getY() + "   dX: " + MouseTurnH + " dY: " + Mouse.getDY());
				Players.get(0).AngleTurn( (short)-(MouseTurnH * 20));


				if (Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_UP))
				{
					Players.get(0).ForwardMove(1);
				}
				if (Keyboard.isKeyDown(Keyboard.KEY_S) || Keyboard.isKeyDown(Keyboard.KEY_DOWN))
				{
					Players.get(0).ForwardMove(-1);
				}
				if (Keyboard.isKeyDown(Keyboard.KEY_A))
				{
					Players.get(0).LateralMove(-1);
				}
				if (Keyboard.isKeyDown(Keyboard.KEY_D))
				{
					Players.get(0).LateralMove(1);
				}
				if (Keyboard.isKeyDown(Keyboard.KEY_LEFT))
				{
					Players.get(0).AngleTurn((short) 500);
				}
				if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
				{
					Players.get(0).AngleTurn((short)-500);
				}
				if (Keyboard.isKeyDown(Keyboard.KEY_SPACE))
				{
					Players.get(0).MoveUp();
				}
				if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
				{
					Players.get(0).MoveDown();
				}
				if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
				{
					System.exit(0);
				}
				HeadCamera.UpdateCamera();

				if (Lvl != null)
				{
					for (int i = 0; i < Lvl.Planes.size(); i++)
					{
						if (Lvl.Planes.get(i).TwoSided_)
						{
							glDisable(GL_CULL_FACE);
						}

						glPushMatrix();
						{
							// Apply color to polygons
							glColor3f(0.5f, 0.5f, 0.5f);
							// Draw polygons according to the camera position
							glTranslatef(HeadCamera.PosX(), HeadCamera.PosY(), HeadCamera.PosZ());
							glBegin(GL_QUADS);
							{
								for (int j = 0; j < Lvl.Planes.get(i).Vertices.size(); j += 3)
								{
									glTexCoord2f(TextXcoords[j/3], TextYcoords[j/3]);
									// (Ypos, Zpos, Xpos)
									glVertex3f(-Lvl.Planes.get(i).Vertices.get(j + 1),   // There a minus here to flip the Y axis
											Lvl.Planes.get(i).Vertices.get(j + 2),
											-Lvl.Planes.get(i).Vertices.get(j));    // There a minus here to flip the X axis

									/*
									glVertex3f(Lvl.Planes.get(i).Vertices.get(j) *64,
											Lvl.Planes.get(i).Vertices.get(j + 2)*64,
											Lvl.Planes.get(i).Vertices.get(j + 1)*64);
									 */
								}
							}
							glEnd();
						}
						glPopMatrix();

						if (Lvl.Planes.get(i).TwoSided_)
						{
							glEnable(GL_CULL_FACE);
						}

						//glPopMatrix();
					}
				}

				Display.update();

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


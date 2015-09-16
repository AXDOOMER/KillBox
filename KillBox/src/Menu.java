//Copyright (C) 2014-2015 Alexandre-Xavier Labonté-Lamoureux
//Copyright (C) 2015 Francis Bourgault
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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import static org.lwjgl.opengl.GL11.*;

public class Menu
{
	// Inner class for menu items
	class MenuItem
	{
		// Attributes
		private String ItemText; // Text for the MenuItem
		private boolean Enabled; // Define wether it can be modify or not.
		private double Occupied_Width; // Initialize on Draw. Tell how much large is the item.

		// Constants
		public static final int MarginPixels = 5; // Magin between item.
		private final int FontSize = 3; // Mutiply Font image by this number
		private final int SpaceSize = 2; // Multiply magin by this number to create space
		final double FontWidth = 1.34375d; // Value in %, must be converted to GL unit
		final double FontHeight = 3.2083333333333333333333333333333d; // Value in %, must be converted to GL unit

		// Default constructor
		public MenuItem()
		{
			// Initialize component to default
			ItemText("");
			Enabled(false);
			Occupied_Width(0);
		}

		// Parametered constructor
		public MenuItem(String Text, boolean Bool)
		{
			// Initialize Parameters
			ItemText(Text);
			Enabled(Bool);
			Occupied_Width(0);
		}

		// Set the value of ItemText to the String value given.
		private void ItemText(String Text)
		{
			// Set value to ItemText
			ItemText = Text;
		}

		// Set value of Occupied Width
		protected void Occupied_Width(double Width)
		{
			Occupied_Width = Width;
		}

		// Set value of Enabled to boolean
		public void Enabled(boolean Enable)
		{
			Enabled = Enable;
		}

		// Set value of Enabled to boolean
		public boolean Enabled()
		{
			return Enabled;
		}

		// Return value of ItemText attribute
		public String ItemText()
		{
			// Return value of ItemText attribute
			return ItemText;
		}

		// Return value of Occupied_Width
		public double Occupied_Width()
		{
			return Occupied_Width;
		}

		// Draw text of MenuItem at position
		public void Draw(double PosX, double PosY, int GridWidth, int GridHeight, boolean Highlight)
		{
			// Get Item Text
			String Text = ItemText();
			// Convert pixel to GL
			double PosXGL = ConvertPxToGL(PosX,true,GridWidth,GridHeight);
			double PosYGL = ConvertPxToGL(PosY, false, GridWidth, GridHeight);
			double MaginGL = ConvertPxToGL(MarginPixels,true,GridWidth,GridHeight);

			if(Highlight)
			{
				glColor3f(255,255,0);
			}
			else
			{
				glColor3f(255,255,255);
			}

			// Loop that check each letter of ItemText and load corresponding image and then draw it
			for(int Letter = 0; Letter < Text.length(); Letter++)
			{
				if(Text.charAt(Letter) == ' ')
				{
					// Adjusting next PosX for the next image
					PosXGL = PosXGL + MaginGL * SpaceSize;
					// Adding width to total width
					Occupied_Width(Occupied_Width() + MarginPixels * SpaceSize + PosX);
				}
				else
				{
					glEnable(GL_TEXTURE_2D);


					// Initialize Texture
					Texture Texture = new Texture(("Stuff/smallchars/" + (String.valueOf(Text.charAt(Letter)).toUpperCase()) + ".png"),GL_NEAREST);
					// Binding Texture
					Texture.Bind();
					// Converting image height and widht
					double ImgWidthGL  = ConvertPourcentToGL(FontWidth,true,GridWidth,GridHeight);
					double ImgHeightGL  = ConvertPourcentToGL(FontHeight,false,GridWidth,GridHeight);
					double ImgWidthPX = ConvertPourcentToPx(FontWidth,true,GridWidth,GridHeight);

					// Drawing image
					glBegin(GL_QUADS);
					glTexCoord2d(0.0d, 1.0d);
					glVertex2d(PosXGL, PosYGL);
					glTexCoord2d(1.0d, 1.0d);
					glVertex2d(PosXGL + ImgWidthGL, PosYGL);
					glTexCoord2d(1.0d,0.0d);
					glVertex2d(PosXGL + ImgWidthGL, PosYGL + ImgHeightGL);
					glTexCoord2d(0.0d, 0.0d);
					glVertex2d(PosXGL, PosYGL + ImgHeightGL);
					glEnd();

					// Adding width to total width
					Occupied_Width(Occupied_Width() + ImgWidthPX + MarginPixels); //

					// Adjusting next PosX for the next image
					PosXGL = PosXGL + ImgWidthGL + MaginGL;

					glDisable(GL_TEXTURE_2D);
				}
			}
		}
	}

	// Derived from MenuItem, contains attribute and function to create and modify a CheckBox
	class MenuItem_CheckBox extends MenuItem
	{
		// Attribute
		protected boolean IsChecked;

		// Constants
		final double BoxSizeWidth = 1.34375d; // Value in %, must be converted to GL unit
		final double BoxSizeHeight = 3.2083333333333333333333333333333d; // Value in %, must be converted to GL unit
		final int BorderX = 2; // Size of border for the box
		final int BorderY = BorderX + (BorderX * 2 / 3); // Size of border for the box
		final Color BoxColorChecked = new Color(255,255,0,255); // Yellow color
		final Color BoxColorUnchecked = new Color(0,0,0,255); // Black

		// Default constructor
		public MenuItem_CheckBox()
		{
			// Calling MenuItem constructor to initialize inherited attribute
			super("",false);
			// Initialize Attribute
			IsChecked(false);
		}
		// Parametered constructor
		public MenuItem_CheckBox(String Text, boolean Enabled,boolean Checked)
		{
			// Calling MenuItem constructor to initialize inherited attribute
			super(Text,Enabled);
			// Initialize Attribute
			IsChecked = Checked;
		}

		// Change the values of IsChecked to the value of the In.
		public void IsChecked(boolean Checked)
		{
			// Set value of IsChecked to parameter's value
			IsChecked = Checked;
		}

		// Return value of attribute IsChecked
		public boolean IsChecked()
		{
			// Return value of IsChecked
			return IsChecked;
		}

		// Draw Checkbox control and text
		public void Draw(double PosX, double PosY, int GridWidth, int GridHeight, boolean Highlight)
		{
			// Set draw mode to fill
			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

			// Convert point to GL unit
			double PosXGL = ConvertPxToGL(PosX,true,GridWidth,GridHeight);
			double PosYGL = ConvertPxToGL(PosY,false,GridWidth,GridHeight);
			double BoxWidthGL = ConvertPourcentToGL(BoxSizeWidth,true,GridWidth,GridHeight);
			double BoxHeightGL = ConvertPourcentToGL(BoxSizeHeight,false,GridWidth,GridHeight);
			double MargeX = ConvertPxToGL(BorderX,true,GridWidth,GridHeight);
			double MargeY = ConvertPxToGL(BorderY, false, GridWidth, GridHeight);
			double BoxWidthPixels = ConvertPourcentToPx(BoxSizeWidth, true, GridWidth, GridHeight);

			glColor4f(BoxColorUnchecked.getRed(), BoxColorUnchecked.getGreen(), BoxColorUnchecked.getBlue(), BoxColorUnchecked.getAlpha());

			// Draw border
			glRectd(PosXGL - MargeX, PosYGL - MargeY, PosXGL + MargeX + BoxWidthGL, PosYGL + MargeY + BoxHeightGL);

			// Change color to the color of the checkbox
			if(IsChecked())
			{
				glColor4f(BoxColorChecked.getRed(), BoxColorChecked.getGreen(), BoxColorChecked.getBlue(), BoxColorChecked.getAlpha());
			}

			// Draw rectangle
			glRectd(PosXGL, PosYGL, PosXGL + BoxWidthGL, PosYGL + BoxHeightGL);

			// Set Occupied_Width
			Occupied_Width(BoxWidthPixels + BorderX + MarginPixels);

			// Draw Text next to Checkbox
			super.Draw(PosX + BoxWidthPixels + BorderX + MarginPixels,PosY,GridWidth,GridHeight, Highlight);

		}
	}

	// Derived from MenuItem, has method to create or modify a Horizontal Slider
	final class MenuItem_HorSlider extends MenuItem
	{
		// Attribute
		int CursorPos; // Position of the slider. Must be between MinValue and MaxValue(NumberOfSquare)
		int MaxValue; // Maximum value of HorSlider.
		int MinValue; // Minimum value of HorSlider.

		// Constant
		final int NumberOfSquare = 5; // Number of Square to draw for the slider (20,40,60,80,100)%
		final double SquareWidth = 1; // % value, must be converted to either px or gl
		final double SquareHeight = 3; // % value, must be converted to either px or gl
		final double BorderX = 2; // Size of border for the box
		final double BorderY = BorderX + (BorderX * 2 / 3); // Size of border for the box
		final Color SquareColor = new Color(255,255,0,255); // Yellow color
		final Color DarkSquareColor = new Color(0,0,0,255); // Black color

		// Parametered constructor
		public MenuItem_HorSlider(String Text,boolean Enabled, int Max,int Min, int Pos)
		{
			// Initialize Text of Item
			super(Text,Enabled);

			// Initialize Max value of slider
			MaxValue(Max);

			// Initialize Min value of slider
			MinValue(Min);

			// Initialize Cursor initial position
			CursorPos(Pos);
		}

		// Set MaxValue to Max
		private void MaxValue(int Max)
		{
			// Set MaxValue to Max
			MaxValue = Max;
		}

		// Set MinValue to Min
		private void MinValue(int Min)
		{
			// Set MaxValue to Min
			MinValue = Min;
		}

		// Set Cursor position to Pos
		public void CursorPos(int Pos)
		{
			// Check if Pos invalid
			if(Pos < 0 || Pos > NumberOfSquare)
			{
				// Adjust Pos
				if(Pos < 0)
				{
					Pos = 0;
				}
				else
				{
					Pos = NumberOfSquare;
				}
			}
			// Set Cursor position to Pos
			CursorPos = Pos;
		}

		// Get current value of HorSlider
		public int GetCurValue()
		{
			// Return the value of the slider
			return CursorPos * (MaxValue - MinValue) / NumberOfSquare;
		}

		// Draw the horizontal slider and its text
		public void Draw(double PosX, double PosY, int GridWidth, int GridHeight, boolean Highlight)
		{
			// Convert Px and Pourcent to GL unit
			double PosXGL = ConvertPxToGL(PosX, true, GridWidth, GridHeight);
			double PosYGL = ConvertPxToGL(PosY, false, GridWidth, GridHeight);
			double SquareWidthGL = ConvertPourcentToGL(SquareWidth, true, GridWidth, GridHeight);
			double SquareHeightGL = ConvertPourcentToGL(SquareHeight, false, GridWidth, GridHeight);
			double MargeX = ConvertPxToGL(BorderX, true, GridWidth, GridHeight);
			double MargeY = ConvertPxToGL(BorderY, false, GridWidth, GridHeight);
			double SquareWidthPixels = ConvertPourcentToPx(SquareWidth, true, GridWidth, GridHeight);
			double SquareMargin = ConvertPxToGL(MarginPixels, true, GridWidth, GridHeight) + MargeX; // Margin between square

			// Loop to draw all square of the slider
			for(int SquareIndex = 0; SquareIndex < NumberOfSquare; SquareIndex++)
			{
				// Change color to black
				glColor4f(DarkSquareColor.getRed(), DarkSquareColor.getGreen(), DarkSquareColor.getBlue(), DarkSquareColor.getAlpha());

				// Draw border
				glRectd(PosXGL - MargeX, PosYGL - MargeY, PosXGL + MargeX + SquareWidthGL, PosYGL + MargeY + SquareHeightGL);

				// Check cursor position
				if(!(SquareIndex <= CursorPos))
				{
					// Change color
					glColor4f(SquareColor.getRed(), SquareColor.getGreen(), SquareColor.getBlue(), SquareColor.getAlpha());
				}
				// Draw rectangle
				glRectd(PosXGL, PosYGL, PosXGL + SquareWidthGL, PosYGL + SquareHeightGL);

				// Add value to X
				PosXGL = PosXGL + SquareMargin + SquareWidthGL;
			}

			// Set Occupied_Width
			Occupied_Width((SquareWidthPixels + BorderX + MarginPixels) * NumberOfSquare);

			// Draw Text next to Checkbox
			super.Draw(PosX + (SquareWidthPixels + BorderX + MarginPixels) * NumberOfSquare,PosY,GridWidth,GridHeight,Highlight);
		}
	}

	// Derived from MenuItem, contains a method to call a specific dialog
	final class MenuItem_Windows extends MenuItem
	{
		// Attribute
		// JFrame

		// Constructor
		// Default constructor
		public MenuItem_Windows()
		{
			// Call the default super constructor
			super();
		}

		// Parametred constructor
		public MenuItem_Windows(String Text, boolean Enabled)
		{
			// Initialize attribute
			super(Text,Enabled);
		}

		// Private Function
		// SetJPanel (Make class that contains JPanel to set in the JFrame)

		// Public Function
		// Open Window

		// Close Window

	}
	// Attribute
	List<List<MenuItem>> Items;

	boolean Active = false;		// Show or hide the menu
	int MenuBarCursor = 0; // Cursor of menu bar
	int SubMenuCursor = 0; // Sub menu cursor

	// Constant ////////////////////
	private final double RowHeight = 6.25d; // Number of px per row of MenuItem
	private final Color BarColor = new Color(0.5f,0.0f,0.0f,1.0f);
	private final int BigMargin = 4; // Multiply normal margin by this number for a bigger margin
	private static final double GlWinWidth = 2.73f; // Value of the windows width with GL unit (auto ajust to windows size)
	private static final double GlWinHeight = 1.66f; // Value of the windows height with GL unit (auto ajust to windows size)

	// Screen coordinates are represented as a fixed resolution
	int GridWidth = 640;
	int GridHeight = 480;

	public Menu()
	{
		// Initialize Cursor
		MenuBarCursor = 0;

		// Initialize array of array
		Items = new ArrayList<List<MenuItem>>();

		// Game array
		List<MenuItem> Game = new ArrayList<MenuItem>();
		Game.add(new MenuItem("Game", true));
		Game.add(new MenuItem_Windows("New Game", true));
		Game.add(new MenuItem_Windows("Join Game", true));
		Game.add(new MenuItem_Windows("Quit Game", true));
		// Seperator
		Game.add(new MenuItem_Windows("Play Demo", true));
		Game.add(new MenuItem_Windows("About", true));
		Game.add(new MenuItem_Windows("Exit", true));

		// Option Array
		List<MenuItem> Option = new ArrayList<MenuItem>();
		Option.add(new MenuItem("Option", true));
		Option.add(new MenuItem_CheckBox("Use freelook", true, true));
		Option.add(new MenuItem_CheckBox("Show messages", true, true));
		Option.add(new MenuItem_CheckBox("Show HUD", true, true));
		// Seperator
		Option.add(new MenuItem_CheckBox("Show Debug", true, true));

		// Control Array
		List<MenuItem> Control = new ArrayList<MenuItem>();
		Control.add(new MenuItem("Control", true));
		Control.add(new MenuItem_CheckBox("Grab mouse", true, true));
		Control.add(new MenuItem_CheckBox("Enable chat", true, true));
		Control.add(new MenuItem_HorSlider("Mouse Sensibility", true, 0, 100, 2));

		// Sound Array
		List<MenuItem> Sound = new ArrayList<MenuItem>();
		Sound.add(new MenuItem("Sound", true));
		Sound.add(new MenuItem_HorSlider("SFX Volume", true, 0, 100, 2));
		// Video Array
		List<MenuItem> Video = new ArrayList<MenuItem>();
		Video.add(new MenuItem("Video", true));
		Video.add(new MenuItem_CheckBox("Fullscreen", true, true));
		Video.add(new MenuItem_CheckBox("Enable Filtering", true, true));

		// Adding array to menu
		Items.add(Game);
		Items.add(Option);
		Items.add(Control);
		Items.add(Sound);
		Items.add(Video);
	}

	// Set grid width value
	public void GridWidth(int Width)
	{
		GridWidth = Width;
	}

	// Set grid height value
	public void GridHeight(int Height)
	{
		GridHeight = Height;
	}

	// Get grid width value
	public int GridWidth()
	{
		return GridWidth;
	}

	// Get grid height value
	public int GridHeight()
	{
		return GridHeight;
	}

	// Draw menu by calling other function
	public void DrawMenu()
	{
		// Initialize PosX and PosY
		double PosX = 0;
		double PosY = 0;
		double OccupiedWidth = 0;

		// Draw menu bar on the top of the screen
		DrawMenuBar();
		// Draw all menu items
		for(int MenuIndex = 0; MenuIndex < Items.size(); MenuIndex++)
		{
			for(int SubMenuIndex = 0; SubMenuIndex < Items.get(MenuIndex).size(); SubMenuIndex++)
			{
				// Move Y from constant
				PosY = PosY - ConvertPourcentToPx(RowHeight, false, GridWidth, GridHeight);

				// if first item (title)
				if(SubMenuIndex == 0)
				{
					// Draw
					if(MenuIndex == MenuBarCursor)
					{
						// Draw highlight if cursor is on it
						Items.get(MenuIndex).get(SubMenuIndex).Draw(PosX, PosY, GridWidth(), GridHeight(),true);
					}
					else
					{
						// Draw normal
						Items.get(MenuIndex).get(SubMenuIndex).Draw(PosX, PosY, GridWidth(), GridHeight(),false);
					}
				}
				// Check if MenuCursor on MenuIndex
				else if(MenuIndex == MenuBarCursor)
				{
					// Draw items. Check if subCursor on it.
					if(SubMenuIndex == SubMenuCursor)
					{
						// Call draw function with highlight
						Items.get(MenuIndex).get(SubMenuIndex).Draw(PosX, PosY, GridWidth(), GridHeight(),true);
					}
					else
					{
						// Call draw function without highlight
						Items.get(MenuIndex).get(SubMenuIndex).Draw(PosX, PosY, GridWidth(), GridHeight(),false);
					}
				}

				// Move X from Occupied Width
				if(SubMenuIndex == 0)
				{
					OccupiedWidth = Items.get(MenuIndex).get(SubMenuIndex).Occupied_Width();
					// Reset Occupied_Width after drawing back rectangle
					Items.get(MenuIndex).get(SubMenuIndex).Occupied_Width(0);
				}
			}
			// Reset Y to default
			PosY = 0;
			// Move X to the right
			PosX = PosX + OccupiedWidth + MenuItem.MarginPixels * BigMargin;
		}

		glEnable(GL_TEXTURE_2D);
	}

	// Draw equally the menu bar and its item
	private void DrawMenuBar()
	{
		// Draw bar that cover the top of the screen. Convert Point to Gl unit.
		double PosXGL = ConvertPxToGL(0, true, GridWidth, GridHeight);
		double PosYGL = ConvertPourcentToGL(-RowHeight, false, GridWidth, GridHeight);
		double GridWidthGL = ConvertPxToGL(GridWidth, true, GridWidth, GridHeight);
		double TopYGL = ConvertPxToGL(0, false, GridWidth, GridHeight);

		// Change Color
		glColor4f(BarColor.getRed() / 255.0f, BarColor.getGreen() / 255.0f, BarColor.getBlue() / 255.0f, BarColor.getAlpha() / 255.0f);

		// Change polygon Mode
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

		// Draw rectangle
		glRectd(PosXGL,PosYGL,PosXGL + GridWidthGL, TopYGL);
	}

	public boolean Active()
	{
		return Active;
	}

	public void Show()
	{
		Active = true;
	}

	public void Hide()
	{
		Active = false;
	}

	// Move the cursor from 1 to the right
	public void CursorRight()
	{
		// Check if cursor can move to right
		if(MenuBarCursor < Items.size() - 1)
		{
			MenuBarCursor++;
			// Check if SubMenuCursor to big to fit next subMenu
			if(SubMenuCursor >= Items.get(MenuBarCursor).size() - 1)
			{
				SubMenuCursor = Items.get(MenuBarCursor).size() - 1;
			}
		}
	}

	// Move cursor from 1 to the left
	public void CursorLeft()
	{
		// Check if cursor can move to left
		if(MenuBarCursor > 0)
		{
			MenuBarCursor--;
			// Check if SubMenuCursor to big to fit next subMenu
			if(SubMenuCursor >= Items.get(MenuBarCursor).size() - 1)
			{
				SubMenuCursor = Items.get(MenuBarCursor).size() - 1;
			}
		}
	}

	// Move cursor down from 1
	public void CursorDown()
	{
		// Check if cursor can move down
		if(SubMenuCursor < Items.get(MenuBarCursor).size() - 1)
		{
			SubMenuCursor++;
		}
	}

	// Move cursor up from 1
	public void CursorUp()
	{
		// Check if cursor can move down
		if(SubMenuCursor > 0)
		{
			SubMenuCursor--;
		}
	}

	// Convert pourcent to Px
	public static double ConvertPourcentToPx(double Poucent, boolean IsX, int GridWidth, int GridHeight)
	{
		// Declare local int
		double Pixel = 0;

		// Check if is X coordonate
		if(IsX)
		{
			Pixel = (Poucent * GridWidth) / 100;
		}
		else // Is Y
		{
			Pixel = (Poucent * GridHeight) / 100;
		}

		// Return converted mesure
		return Pixel;
	}

	// Convert pourcent to GL
	public static double ConvertPourcentToGL(double Poucent, boolean IsX, int GridWidth, int GridHeight)
	{
		// Declare local int
		double GLUnit = 0;

		// Check if is X coordonate
		if(IsX)
		{
			GLUnit = (Poucent * GridWidth) / 100;
		}
		else // Is Y
		{
			GLUnit = (Poucent * GridHeight) / 100;
		}

		// Convert the obtains pixel to GL
		GLUnit = ConvertPxToGL(GLUnit,IsX, GridWidth, GridHeight);

		// Return converted mesure
		return GLUnit;
	}

	// Convert pixel to GL
	public static double ConvertPxToGL(double Pixel, boolean IsX, int GridWidth, int GridHeight)
	{
		// Declare local int
		double GLUnit = 0;

		// Check if is X coordonate
		if(IsX)
		{
			GLUnit = (Pixel * GlWinWidth) / GridWidth;
		}
		else // Is Y
		{
			GLUnit = (Pixel * GlWinHeight) / GridHeight;
		}

		// Return converted mesure
		return GLUnit;
	}
}

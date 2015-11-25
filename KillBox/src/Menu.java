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

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import static org.lwjgl.opengl.GL11.*;

public class Menu
{
	public boolean UserWantsToExit = false;

	String GameVersion = "v2.??? (Beta)";
	String LastUpdate = "november 22th 2015";

	public String Address = null;
	public int GameMode = 0;
	public int TimeLimit = 0;
	public int KillLimit = 0;
	public boolean IsServer = false;
	public boolean IsClient = false;
	public boolean InGame = false;
	public String Map = "";

	private String Message = "";
	private int MessageTime = 0;
	private int MaxMessageTime = 100;

	// Inner class to have "pointer" of primitive type
	class Menu_Boolean
	{
		// Attribute
		boolean Bool;

		// Get value of Bool
		public boolean Bool()
		{
			return Bool;
		}

		// Set value of Bool
		public void Bool(boolean Value)
		{
			Bool = Value;
		}
	}

	class Menu_Integer
	{
		// Attribute
		int Int;

		// Get value of Int
		public int Int()
		{
			return Int;
		}

		// Set value of Int
		public void Int(int Value)
		{
			Int = Value;
		}
	}

	// Inner class for menu items
	class MenuItem
	{
		// Attributes
		private String ItemText; // Text for the MenuItem
		private boolean Enabled; // Define whether it can be modify or not.
		private boolean KeepActiveColor; // Define if the controler stay white despite being unable
		private double Occupied_Width; // Initialize on Draw. Tell how much large is the item

		// Constants
		public static final float MarginPercent = 0.4f; // Margin between item.
		private final int FontSize = 3; // Multiply Font image by this number
		private final int SpaceSize = 2; // Multiply margin by this number to create space
		final double FontWidth = 1.34375d; // Value in %, must be converted to GL unit
		final double FontHeight = 3.2083333333333333333333333333333d; // Value in %, must be converted to GL unit
		final Color FontColor = new Color(255, 255, 255, 255);
		final Color FontHighlightColor = new Color(0, 183, 0, 255);
		final Color FontFirstItemHighlightColor = new Color(255, 255, 0, 255);
		final Color FontUnableColor = new Color(127, 127, 127, 255);

		// Default constructor
		public MenuItem()
		{
			// Initialize component to default
			ItemText("");
			Enabled(false);
			KeepActiveColor(false);
			Occupied_Width(0);
		}

		// Parameterized constructor
		public MenuItem(String Text, boolean Active,boolean Keep)
		{
			// Initialize Parameters
			ItemText(Text);
			Enabled(Active);
			KeepActiveColor(Keep);
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

		// Set value of KeepColor
		private void KeepActiveColor(boolean Keep)
		{
			KeepActiveColor = Keep;
		}

		// Get value of KeepColor
		public boolean KeepActiveColor()
		{
			return KeepActiveColor;
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

		// Get Draw width. Will not draw the item, only count how much width in px it takes.
		public double GetDrawWidth(int GridWidth, int GridHeight, boolean WindowCoor, double AdditionalWidth)
		{
			// Get Item Text
			String Text = ItemText();

			double TotalWidthPixels = AdditionalWidth;

			// Convert to pixel
			double MarginPixels = ConvertPercentToPx(MarginPercent, true, GridWidth, GridHeight);

			// Loop that check each letter of ItemText and load corresponding image and then draw it
			for(int Letter = 0; Letter < Text.length(); Letter++)
			{
				if(Text.charAt(Letter) == ' ')
				{
					// Adding width to total width
					TotalWidthPixels = TotalWidthPixels +  (MarginPixels * SpaceSize);
				}
				else
				{
					// Converting image width
					double ImgWidthPX = ConvertPercentToPx(FontWidth, true, GridWidth, GridHeight);

					// Adding width to total width
					TotalWidthPixels = TotalWidthPixels + ImgWidthPX + MarginPixels;
				}
			}

			// Return total width
			return TotalWidthPixels;
		}

		// Draw text of MenuItem at position
		public void Draw(double PosX, double PosY, int GridWidth, int GridHeight, boolean Highlight, boolean NotFirstItem, boolean WindowColor)
		{
			// Get Item Text
			String Text = ItemText();
			// Convert pixel to GL
			double PosXGL = ConvertPxToGL(PosX, true, GridWidth, GridHeight, WindowColor);
			double PosYGL = ConvertPxToGL(PosY, false, GridWidth, GridHeight, WindowColor);
			double MarginGL = ConvertPercentToGL(MarginPercent, true, GridWidth, GridHeight, WindowColor);
			double MarginPixels = ConvertPercentToPx(MarginPercent, true, GridWidth, GridHeight);

			if(!Enabled() && !KeepActiveColor)
			{
				glColor4f(FontUnableColor.getRed() / 255.0f, FontUnableColor.getGreen() / 255.0f,FontUnableColor.getBlue() / 255.0f,FontUnableColor.getAlpha() / 255.0f);
			}
			else if(Highlight && NotFirstItem)
			{
				glColor4f(FontFirstItemHighlightColor.getRed() / 255.0f, FontFirstItemHighlightColor.getGreen() / 255.0f,FontFirstItemHighlightColor.getBlue() / 255.0f,FontFirstItemHighlightColor.getAlpha() / 255.0f);
			}
			else if (Highlight)
			{
				glColor4f(FontHighlightColor.getRed() / 255.0f, FontHighlightColor.getGreen() / 255.0f,FontHighlightColor.getBlue() / 255.0f,FontHighlightColor.getAlpha() / 255.0f);
			}
			else
			{
				glColor4f(FontColor.getRed() / 255.0f, FontColor.getGreen() / 255.0f,FontColor.getBlue() / 255.0f,FontColor.getAlpha() / 255.0f);
			}

			// Loop that check each letter of ItemText and load corresponding image and then draw it
			for (int Letter = 0; Letter < Text.length(); Letter++)
			{
				if(Text.charAt(Letter) == ' ')
				{
					// Adjusting next PosX for the next image
					PosXGL = PosXGL + MarginGL * SpaceSize;
					// Adding width to total width
					Occupied_Width(Occupied_Width() + MarginPixels * SpaceSize);
				}
				else
				{
					glEnable(GL_TEXTURE_2D);

					// Get int value of char
					int CharValue = (String.valueOf(Text.charAt(Letter)).toUpperCase()).charAt(0);
					int TextureIndex = 0;
					// Check if number
					if(CharValue >= Char0Index && CharValue <= Char9Index)
					{
						// Initialize Texture Index in font List
						TextureIndex = CharValue - Char0Index;
					}
					// Check if letter
					else if(CharValue >= CharAIndex && CharValue <= CharZIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = CharValue - CharAIndex + 10;
					}
					else if (CharValue == CharAsteriskIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = 40;
					}
					else if (CharValue == CharLeftParaIndex || CharValue == CharLeftSquareIndex || CharValue == CharLeftBraceIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = 41;
					}
					else if (CharValue == CharRightParaIndex || CharValue == CharRightSquareIndex || CharValue == CharRightBraceIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = 42;
					}
					// Special character
					else if (CharValue == CharMinusIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = 44;
					}
					else if (CharValue == ChaPeriodIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = 36;
					}
					else if(CharValue == CharColonIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = 37;
					}
					else // Character is not defined. Write '?' instead.
					{
						// Initialize Texture Index in font List
						TextureIndex = 48;
					}

					// Initialize and Bind Texture
					FontArray.get(TextureIndex).Bind();

					// Converting image height and widht
					double ImgWidthGL  = ConvertPercentToGL(FontWidth, true, GridWidth, GridHeight, WindowColor);
					double ImgHeightGL  = ConvertPercentToGL(FontHeight, false, GridWidth, GridHeight, WindowColor);
					double ImgWidthPX = ConvertPercentToPx(FontWidth, true, GridWidth, GridHeight);
					glEnable(GL_DEPTH_TEST);
					// Drawing image
					glBegin(GL_QUADS);
					glTexCoord2d(0.0d, 1.0d);
					glVertex2d(PosXGL, PosYGL);
					glTexCoord2d(1.0d, 1.0d);
					glVertex2d(PosXGL + ImgWidthGL, PosYGL);
					glTexCoord2d(1.0d, 0.0d);
					glVertex2d(PosXGL + ImgWidthGL, PosYGL + ImgHeightGL);
					glTexCoord2d(0.0d, 0.0d);
					glVertex2d(PosXGL, PosYGL + ImgHeightGL);
					glEnd();
					// Adding width to total width
					Occupied_Width(Occupied_Width() + ImgWidthPX  + MarginPixels); //

					// Adjusting next PosX for the next image
					PosXGL = PosXGL + ImgWidthGL + MarginGL;

					// Disable so that the next element are written on the same level
					glDisable(GL_TEXTURE_2D);
				}
			}
		}

		// Must be redefine by child class<
		void Action(boolean MenuKeyPressed)
		{
			// Will not do anything
		}
	}

	// Derived from MenuItem, contains attribute and function to create and modify a CheckBox
	class MenuItem_CheckBox extends MenuItem
	{
		// Attribute
		protected Menu_Boolean IsChecked;

		// Constants
		final double BoxSizeWidth = 1.34375d; // Value in %, must be converted to GL unit
		final double BoxSizeHeight = 3.2083333333333333333333333333333d; // Value in %, must be converted to GL unit
		final int BorderX = 2; // Size of border for the box
		final int BorderY = BorderX + (BorderX * 2 / 3); // Size of border for the box
		final Color BoxColorChecked = new Color(255, 183, 0, 255); // Orange color
		final Color BoxColorUnchecked = new Color(0, 0, 0, 255); // Black

		// Default constructor
		public MenuItem_CheckBox()
		{
			// Calling MenuItem constructor to initialize inherited attribute
			super("", false, false);
			// Initialize Attribute
			IsChecked.Bool(false);
		}
		// Parameterized constructor
		public MenuItem_CheckBox(String Text, boolean Enabled,boolean Keep,Menu_Boolean Checked)
		{
			// Calling MenuItem constructor to initialize inherited attribute
			super(Text,Enabled,Keep);
			// Initialize Attribute
			IsChecked = Checked;
		}

		// Change the values of IsChecked to the value of the In.
		public void IsChecked(boolean Checked)
		{
			// Set value of IsChecked to parameter's value
			IsChecked.Bool(Checked);
		}

		// Return value of attribute IsChecked
		public boolean IsChecked()
		{
			// Return value of IsChecked
			return IsChecked.Bool();
		}

		// Get Draw width. Will not draw the item, only count how much width in px it takes.
		public double GetDrawWidth(int GridWidth,int GridHeight,boolean WindowCoor,double AdditionalWidth)
		{
			double TotalWidthPixels = 0;

			// Convert to pixel
			double MarginPixels = ConvertPercentToPx(MarginPercent, true, GridWidth, GridHeight);
			double BoxWidthPixels = ConvertPercentToPx(BoxSizeWidth, true, GridWidth, GridHeight);

			TotalWidthPixels = BoxWidthPixels + BorderX + MarginPixels;

			// Return total width
			return super.GetDrawWidth(GridWidth,GridHeight,WindowCoor,TotalWidthPixels);
		}

		// Draw Checkbox control and text
		public void Draw(double PosX, double PosY, int GridWidth, int GridHeight, boolean Highlight,boolean NotFirstItem,boolean WindowColor)
		{
			// Disable Depth Test so that the border of the CheckBox is writen over rectangle. (The MenuBar and Alpha Rectangle)
			glDisable(GL_DEPTH_TEST);

			// Set draw mode to fill
			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

			// Convert point to GL unit
			double PosXGL = ConvertPxToGL(PosX,true,GridWidth,GridHeight, WindowColor);
			double PosYGL = ConvertPxToGL(PosY,false,GridWidth,GridHeight, WindowColor);
			double BoxWidthGL = ConvertPercentToGL(BoxSizeWidth, true, GridWidth, GridHeight, WindowColor);
			double BoxHeightGL = ConvertPercentToGL(BoxSizeHeight, false, GridWidth, GridHeight, WindowColor);
			double MargeX = ConvertPxToGL(BorderX, true, GridWidth, GridHeight, WindowColor);
			double MargeY = ConvertPxToGL(BorderY, false, GridWidth, GridHeight, WindowColor);
			double BoxWidthPixels = ConvertPercentToPx(BoxSizeWidth, true, GridWidth, GridHeight);
			double MarginPixels = ConvertPercentToPx(MarginPercent, true, GridWidth, GridHeight);

			glColor4f(BoxColorUnchecked.getRed(), BoxColorUnchecked.getGreen(), BoxColorUnchecked.getBlue(), BoxColorUnchecked.getAlpha());

			// Draw border
			glRectd(PosXGL - MargeX, PosYGL - MargeY, PosXGL + MargeX + BoxWidthGL, PosYGL + MargeY + BoxHeightGL);

			// Change color to the color of the checkbox
			if(IsChecked())
			{
				glColor4f(BoxColorChecked.getRed() / 255.0f, BoxColorChecked.getGreen() / 255.0f, BoxColorChecked.getBlue() / 255.0f, BoxColorChecked.getAlpha() / 255.0f);
			}
			// Enable so that the CheckBox is written over the Border
			glEnable(GL_DEPTH_TEST);

			// Draw rectangle
			glRectd(PosXGL, PosYGL, PosXGL + BoxWidthGL, PosYGL + BoxHeightGL);

			// Set Occupied_Width
			Occupied_Width(BoxWidthPixels + BorderX + MarginPixels);

			// Draw Text next to Checkbox
			super.Draw(PosX + BoxWidthPixels + BorderX + MarginPixels, PosY, GridWidth, GridHeight, Highlight,NotFirstItem, WindowColor);

		}

		// Change checkbox if right or left is pressed
		void Action(boolean MenuKeyPressed)
		{
			if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT) || Keyboard.isKeyDown(Keyboard.KEY_LEFT) && !MenuKeyPressed)
			{
				IsChecked(!IsChecked.Bool());
			}
		}
	}

	// Derived from MenuItem, has method to create or modify a Horizontal Slider
	final class MenuItem_HorSlider extends MenuItem
	{
		// Attribute
		int CursorPos; // Position of the slider. Must be between MinValue and MaxValue(NumberOfSquare)
		int MaxValue; // Maximum value of HorSlider.
		int MinValue; // Minimum value of HorSlider.
		Menu_Integer CurrentValue; // Current value
		int SquareValue; // Value for each square

		// Constant
		final int NumberOfSquare = 5; // Number of Square to draw for the slider (20,40,60,80,100)%
		final double SquareWidth = 1; // % value, must be converted to either px or gl
		final double SquareHeight = 3; // % value, must be converted to either px or gl
		final double BorderX = 2; // Size of border for the box
		final double BorderY = BorderX + (BorderX * 2 / 3); // Size of border for the box
		final Color SquareColor = new Color(0, 142, 221, 255); // Blue color
		final Color DarkSquareColor = new Color(0, 0, 0, 255); // Black color

		// Parameterized constructor
		public MenuItem_HorSlider(String Text,boolean Enabled,boolean Keep, int Min,int Max, Menu_Integer Value)
		{
			// Initialize Text of Item
			super(Text,Enabled,Keep);

			// Initialize Max value of slider
			MaxValue(Max);

			// Initialize Min value of slider
			MinValue(Min);

			// Initialize Current value
			CurrentValue = Value;

			// Initialize value for each square
			SquareValue = MaxValue / NumberOfSquare;

			// Set Cursor position
			CursorPos = CurrentValue.Int() * NumberOfSquare / Max;
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
			CurrentValue.Int(SquareValue * Pos);
		}

		// Get current value of HorSlider
		public int GetCurValue()
		{
			// Return the value of the slider
			return CursorPos * (MaxValue - MinValue) / NumberOfSquare;
		}

		// Get Draw width. Will not draw the item, only count how much width in px it takes.
		public double GetDrawWidth(int GridWidth,int GridHeight,boolean WindowCoor,double AdditionalWidth)
		{
			double TotalWidthPixels = 0;

			// Convert to pixel
			double MarginPixels = ConvertPercentToPx(MarginPercent, true, GridWidth, GridHeight);
			double SquareWidthPixels = ConvertPercentToPx(SquareWidth, true, GridWidth, GridHeight);

			TotalWidthPixels = (SquareWidthPixels + BorderX + MarginPixels) * NumberOfSquare;

			// Return total width
			return super.GetDrawWidth(GridWidth,GridHeight,WindowCoor,TotalWidthPixels);
		}

		// Draw the horizontal slider and its text
		public void Draw(double PosX, double PosY, int GridWidth, int GridHeight, boolean Highlight,boolean NotFirstItem,boolean WindowColor)
		{
			// Convert Px and Pourcent to GL unit
			double PosXGL = ConvertPxToGL(PosX, true, GridWidth, GridHeight, WindowColor);
			double PosYGL = ConvertPxToGL(PosY, false, GridWidth, GridHeight, WindowColor);
			double SquareWidthGL = ConvertPercentToGL(SquareWidth, true, GridWidth, GridHeight, WindowColor);
			double SquareHeightGL = ConvertPercentToGL(SquareHeight, false, GridWidth, GridHeight, WindowColor);
			double MargeX = ConvertPxToGL(BorderX, true, GridWidth, GridHeight, WindowColor);
			double MargeY = ConvertPxToGL(BorderY, false, GridWidth, GridHeight, WindowColor);
			double SquareWidthPixels = ConvertPercentToPx(SquareWidth, true, GridWidth, GridHeight);
			double SquareMargin = ConvertPercentToGL(MarginPercent, true, GridWidth, GridHeight, WindowColor) + MargeX; // Margin between square
			double MarginPixels = ConvertPercentToPx(MarginPercent, true, GridWidth, GridHeight);

			// Loop to draw all square of the slider
			for(int SquareIndex = 0; SquareIndex < NumberOfSquare; SquareIndex++)
			{
				// Disable Depth Test so that the border of the Square is writen over rectangle. (The MenuBar and Alpha Rectangle)
				glDisable(GL_DEPTH_TEST);

				// Change color to black
				glColor4f(DarkSquareColor.getRed(), DarkSquareColor.getGreen(), DarkSquareColor.getBlue(), DarkSquareColor.getAlpha());

				// Draw border
				glRectd(PosXGL - MargeX, PosYGL - MargeY, PosXGL + MargeX + SquareWidthGL, PosYGL + MargeY + SquareHeightGL);

				// Check cursor position
				if(SquareIndex < CursorPos)
				{
					// Change color
					glColor4f(SquareColor.getRed(), SquareColor.getGreen(), SquareColor.getBlue(), SquareColor.getAlpha());
				}
				// Enable so that the Square is written over the Border
				glEnable(GL_DEPTH_TEST);
				// Draw rectangle
				glRectd(PosXGL, PosYGL, PosXGL + SquareWidthGL, PosYGL + SquareHeightGL);

				// Add value to X
				PosXGL = PosXGL + SquareMargin + SquareWidthGL;
			}

			// Set Occupied_Width
			Occupied_Width((SquareWidthPixels + BorderX + MarginPixels) * NumberOfSquare);

			// Draw Text next to Checkbox
			super.Draw(PosX + (SquareWidthPixels + BorderX + MarginPixels) * NumberOfSquare,PosY,GridWidth,GridHeight,Highlight,NotFirstItem, WindowColor);
		}

		// Change Slider if right or left is pressed
		void Action(boolean MenuKeyPressed)
		{
			if(Keyboard.isKeyDown(Keyboard.KEY_RETURN)  && !MenuKeyPressed)
			{
				// Increase pos
				int NextPos = CursorPos + 1;
				// if pos too big, return to pos 0
				if(NextPos > NumberOfSquare)
				{
					NextPos = 0;
				}
				CursorPos(NextPos);
			}
		}
	}

	// Contains attribute and function to create or modify a RadioButtonGroup
	final class MenuItem_RadioButtonGroup
	{
		// Attribute
		public MenuItem_RadioButton RadioButtons[]; // RadioButton that belong to this group. Are actually round Checkbox.
		private Menu_Integer CheckedIndex;

		// Constructor
		public MenuItem_RadioButtonGroup(String[] RadioText, Menu_Integer SelectedIndex)
		{
			// Check validity of CheckIndex
			if(SelectedIndex.Int() < 0 || SelectedIndex.Int() > RadioText.length)
			{
				// Adjust to 0
				SelectedIndex.Int(0);
			}
			// Initialize table with the number of String given
			RadioButtons = new MenuItem_RadioButton[RadioText.length];
			// Create all "Checkbox" in the table
			for(int Radio = 0; Radio < RadioText.length; Radio++)
			{
				RadioButtons[Radio] = new MenuItem_RadioButton(RadioText[Radio],true,true, Radio == SelectedIndex.Int(), this,Radio);
			}

			// Initialize CheckedIndex
			CheckedIndex = SelectedIndex;
		}

		// Private Function

		// Change value of CheckedIndex
		private void CheckedIndex(int SelectedIndex)
		{
			CheckedIndex.Int(SelectedIndex);
		}

		// Public Function

		// Return value of CheckedIndex
		public int CheckedIndex()
		{
			return CheckedIndex.Int();
		}

		// Uncheck button at old checkIndex and set new checkIndex
		public void ChangeCheck(int NewIndex)
		{
			// Check if the index has change
			if(NewIndex != CheckedIndex.Int())
			{
				// Uncheck button at old checkIndex
				RadioButtons[CheckedIndex.Int()].IsChecked(false);
				// Set new checkIndex
				CheckedIndex(NewIndex);
			}
		}
	}

	// Derived from MenuItem.
	final class MenuItem_RadioButton extends MenuItem
	{
		// Private Attribute
		private MenuItem_RadioButtonGroup Group; // Point at an existing RadioGroup
		private int Index; // Index of the RadioButton in the group
		private boolean IsChecked;
		// Constant
		private final double CircleRadius = 1.0d;
		private final int NumberOfLine = 100; // Number of line drawn to draw a "circle"
		private final double CircleTheta = 2.0d * 3.1415926d / NumberOfLine;
		private final Color RadioCheckedColor = new Color(0, 255, 0, 255);
		private final Color RadioUnCheckedColor = new Color(0, 85, 0, 255);
		private final double BorderRadius = 0.33d;
		// Default constructor
		public MenuItem_RadioButton()
		{
			// Initialize attribute to default value
			Group = null;
			Index = 0;
		}

		// Parametered constructor
		public MenuItem_RadioButton(String Text,boolean Enable,boolean Keep, boolean Checked, MenuItem_RadioButtonGroup OwnGroup, int NewIndex)
		{
			// Call super to initialize Text
			super(Text,Enable,Keep);
			// initialize check
			IsChecked = Checked;
			// Initialize group
			Group = OwnGroup;
			// Initialize index
			Index = NewIndex;
		}

		// Private Function

		// Change the values of IsChecked to the value of the In. Also call
		public void IsChecked(boolean Checked)
		{
			// Set the value of IsChecked to Checked value
			IsChecked = Checked;

			// Check if it has been Checked
			if(Checked)
			{
				// Call ChangeCheck function of it's RadioGroup with its index has parameters
				Group.ChangeCheck(Index);
			}
		}

		// Return value of attribute IsChecked
		public boolean IsChecked()
		{
			// Return value of IsChecked
			return IsChecked;
		}

		// Get Draw width. Will not draw the item, only count how much width in px it takes.
		public double GetDrawWidth(int GridWidth,int GridHeight,boolean WindowCoor,double AdditionalWidth)
		{
			double TotalWidthPixels = 0;

			// Convert to pixel
			double MarginPixels = ConvertPercentToPx(MarginPercent, true, GridWidth, GridHeight);
			double CircleRadiusPixels = ConvertPercentToPx(CircleRadius, true, GridWidth, GridHeight);

			TotalWidthPixels = CircleRadiusPixels * 2 + MarginPixels;

			// Return total width
			return super.GetDrawWidth(GridWidth,GridHeight,WindowCoor,TotalWidthPixels);
		}

		// Draw RadioButton control and text
		public void Draw(double PosX, double PosY, int GridWidth, int GridHeight, boolean Highlight, boolean NotFirstItem, boolean WindowColor)
		{
			// Initialize variable needed to draw circle Border
			double CircleBorderRadiusGL = ConvertPercentToGL(CircleRadius + BorderRadius, true, GridWidth, GridHeight, WindowColor);
			double CircleBorderLineX = CircleBorderRadiusGL;
			double CircleLineY = 0;

			// Initialize variable needed to draw circle
			double CircleRadiusGL = ConvertPercentToGL(CircleRadius, true, GridWidth, GridHeight, WindowColor);
			double CircleRadiusPixels = ConvertPercentToPx(CircleRadius, true, GridWidth, GridHeight);
			double CircleX = ConvertPxToGL(PosX + CircleRadiusPixels, true, GridWidth, GridHeight, WindowColor); // Center of circle X
			double CircleY = ConvertPxToGL(PosY + CircleRadiusPixels, false, GridWidth, GridHeight, WindowColor); // Center of circle Y
			double MarginPixels = ConvertPercentToPx(MarginPercent, true, GridWidth, GridHeight);

			// Variable to rotate point
			double CircleCos = Math.cos(CircleTheta);// precalculate the sine and cosine
			double CircleSin = Math.sin(CircleTheta);
			double CircleTan;

			// Change color to black
			glColor4f(0.0f,0.0f,0.0f,1.0f);

			// Disable so that the border are draw below the circle
			glDisable(GL_DEPTH_TEST);

			glBegin(GL_POLYGON);
			for(int CircleLines = 0; CircleLines < NumberOfLine; CircleLines++)
			{
				glVertex2d(CircleBorderLineX + CircleX, CircleLineY + CircleY);//output vertex

				//apply the rotation matrix
				CircleTan = CircleBorderLineX;
				CircleBorderLineX = CircleCos * CircleBorderLineX - CircleSin * CircleLineY;
				CircleLineY = CircleSin * CircleTan + CircleCos * CircleLineY;
			}
			glEnd();

			// Initialize local int
			// Set Mode to Filled Circle or Line Only
			if (IsChecked())
			{
				// Filled
				glColor4f((float)RadioCheckedColor.getRed() / 255.0f,(float)RadioCheckedColor.getGreen() / 255.0f,(float)RadioCheckedColor.getBlue() / 255.0f,(float)RadioCheckedColor.getAlpha() / 255.0f);
			}
			else
			{
				// Line Only
				glColor4f((float)RadioUnCheckedColor.getRed() / 255.0f,(float)RadioUnCheckedColor.getGreen() / 255.0f,(float)RadioUnCheckedColor.getBlue() / 255.0f,(float)RadioUnCheckedColor.getAlpha() / 255.0f);
			}


			// Coordonate of drawn line
			double CircleLineX = CircleRadiusGL;// we start at angle = 0

			// Enable so that the circle is draw over the border
			glEnable(GL_DEPTH_TEST);

			// Draw from origin point and contant size
			// Begin Draw
			glBegin(GL_POLYGON);
			for(int CircleLines = 0; CircleLines < NumberOfLine; CircleLines++)
			{
				glVertex2d(CircleLineX + CircleX, CircleLineY + CircleY);//output vertex

				//apply the rotation matrix
				CircleTan = CircleLineX;
				CircleLineX = CircleCos * CircleLineX - CircleSin * CircleLineY;
				CircleLineY = CircleSin * CircleTan + CircleCos * CircleLineY;
			}
			glEnd();

			// Set occupied width
			Occupied_Width(CircleRadiusPixels * 2 + MarginPixels); // Radius * 2 = Diameter
			// Draw Text next to RadioButton
			super.Draw(PosX + CircleRadiusPixels * 2 + MarginPixels, PosY, GridWidth, GridHeight, Highlight,NotFirstItem, WindowColor);

		}

		// Activate radio button if Right or left is pressed
		void Action(boolean MenuKeyPressed)
		{
			if(Keyboard.isKeyDown(Keyboard.KEY_LEFT) || Keyboard.isKeyDown(Keyboard.KEY_RIGHT)  && !MenuKeyPressed)
			{
				IsChecked(true);
			}
		}
	}

	// Derived from MenuItem, contains method to create or modify a NumberBox
	final class MenuItem_NumberBox extends MenuItem
	{
		// Attribute
		Menu_Integer CurValue; // Current Value
		int MinValue; // Minimum possible value
		int MaxValue; // Maximum possible value
		int Step; // In/Decrease curValue by value of step when In/Decreased
		int HoldTimeCounter; // Time key was hold

		// Constant
		private final double NumBoxBorderX = 0.1d;
		private final double NumBoxBorderY = NumBoxBorderX  + (NumBoxBorderX * 2 / 3);
		private final Color NumBoxHighlightColor = new Color(255, 255 ,0 ,255);
		private final Color NumBoxNeutralColor = new Color(255, 255, 255, 255);
		private final Color NumBoxBorderColor = new Color(0, 0, 0, 255);
		private int TimeRepeatEvent = 5; // Time before event is repeated

		// Parameterized constructor
		public MenuItem_NumberBox(String Text, boolean Enable, boolean Keep, Menu_Integer Value, int Min, int Max, int Stp)
		{
			super(Text, Enable, Keep);
			// Initialize component
			MaxValue(Max);
			MinValue(Min);
			CurValue = Value;
			Step(Stp);
			HoldTimeCounter(0);
		}

		// Private Function

		// Set amount of time the key is hold
		private void HoldTimeCounter(int Time)
		{
			if(Time >= TimeRepeatEvent)
			{
				Time = 0;
			}
			HoldTimeCounter = Time;
		}

		// Set maximum value to int
		private void MaxValue(int Max)
		{
			// Set maximum value to int
			MaxValue = Max;
		}

		// Set minimum value to int
		private void MinValue(int Min)
		{
			MinValue = Min;
		}

		// Set value of step
		private void Step(int Stp)
		{
			// Set value of step to int
			Step = Stp;
		}

		// Set current value to int
		private void CurValue(int Value)
		{
			// Check if value is invalid
			if(Value < MinValue || Value > MaxValue)
			{
				// Adjust value
				if(Value < MinValue)
				{
					Value = MinValue;
				}
				else
				{
					Value = MaxValue;
				}
			}
			// Set current value to int
			CurValue.Int(Value);
		}

		private int HoldTimeCounter()
		{
			return HoldTimeCounter;
		}

		// Public Function

		// Return current value
		public int CurValue()
		{
			// Return current value
			return CurValue.Int();
		}

		// Increse or Decrease the value of the NumberBox by one step
		public void De_Increase(boolean Increase)
		{
			// Check value of bool
			if(Increase)
			{
				// Set value to value + Step
				CurValue(CurValue.Int() + Step);
			}
			else
			{
				// Set value to value - Step
				CurValue(CurValue.Int() - Step);
			}
		}

		// Get Draw width. Will not draw the item, only count how much width in px it takes.
		public double GetDrawWidth(int GridWidth,int GridHeight,boolean WindowCoor,double AdditionalWidth)
		{
			// Get text
			String Value = Integer.toString(CurValue());

			// Initialize total width
			double TotalWidthPixels = 0;

			// Convert to pixel
			double MarginPixels = ConvertPercentToPx(MarginPercent, true, GridWidth, GridHeight);


			// Draw all image
			for(int Images = 0; Images < Value.length();Images++)
			{
				// Converting image height and width
				double ImgWidthPixels = ConvertPercentToPx(FontWidth, true, GridWidth, GridHeight);

				// Adding width to total width
				TotalWidthPixels = TotalWidthPixels + ImgWidthPixels  + MarginPixels;
			}

			// Return total width
			return super.GetDrawWidth(GridWidth, GridHeight, WindowCoor, TotalWidthPixels);
		}

		// Draw NumberBox and text
		public void Draw(double PosX, double PosY, int GridWidth, int GridHeight,boolean Highlight,boolean NotFirstItem,boolean WindowColor)
		{
			// Draw NumberBox
			// Convert current value to string
			String Value = Integer.toString(CurValue());
			int[] NumberImageIndex = new int[Value.length()];

			// Initialize variable to draw border
			double PosXGL = ConvertPxToGL(PosX, true, GridWidth, GridHeight, WindowColor);
			double PosYGL = ConvertPxToGL(PosY, false, GridWidth, GridHeight, WindowColor);
			double BorderXGL = ConvertPercentToGL(NumBoxBorderX, true, GridWidth, GridHeight, WindowColor);
			double BorderYGL = ConvertPercentToGL(NumBoxBorderY, false, GridWidth, GridHeight, WindowColor);
			double BorderXPixels = ConvertPercentToPx(NumBoxBorderX, true, GridWidth, GridHeight);
			double ImageWidthGL = ConvertPercentToGL(FontWidth, true, GridWidth, GridHeight, WindowColor);
			double ImageHeightGL = ConvertPercentToGL(FontHeight, true, GridWidth, GridHeight, WindowColor);
			double TotalImageWidthGL = 0;
			double TotalImageWidthPixels = 0;
			double MarginPixels = ConvertPercentToPx(MarginPercent, true, GridWidth, GridHeight);
			double MarginGL = ConvertPercentToGL(MarginPercent, true, GridWidth, GridHeight, WindowColor);

			// Load texture for Value
			for(int Textures = 0; Textures < Value.length();Textures++)
			{
				// Load image into array
				NumberImageIndex[Textures] = ((String.valueOf(Value.charAt(Textures)).toUpperCase()).charAt(0) - Char0Index);

				// Add pixels width of image
				TotalImageWidthGL = TotalImageWidthGL + ImageWidthGL;
			}
			// Enable so that the circle is draw over the border
			glDisable(GL_DEPTH_TEST);

			glColor4f((float) NumBoxBorderColor.getRed() / 255.0f, (float) NumBoxBorderColor.getGreen() / 255.0f, (float) NumBoxBorderColor.getBlue() / 255.0f, (float) NumBoxBorderColor.getAlpha() / 255.0f);
			// Draw border
			glRectd(PosXGL - BorderXGL, PosYGL - BorderYGL, PosXGL + BorderXGL + TotalImageWidthGL, PosYGL + BorderYGL + ImageHeightGL);

			// Enable so that the circle is draw over the border
			glEnable(GL_DEPTH_TEST);

			// Draw all image
			for (int Images = 0; Images < NumberImageIndex.length;Images++)
			{
				glEnable(GL_TEXTURE_2D);
				// Binding Texture
				FontArray.get(NumberImageIndex[Images]).Bind();
				// Converting image height and widht
				double ImgWidthPixels = ConvertPercentToPx(FontWidth, true, GridWidth, GridHeight);
				glEnable(GL_DEPTH_TEST);
				if(Highlight)
				{
					glColor4f((float)NumBoxHighlightColor.getRed() / 255.0f, (float)NumBoxHighlightColor.getGreen() / 255.0f, (float)NumBoxHighlightColor.getBlue() / 255.0f, (float)NumBoxHighlightColor.getAlpha() / 255.0f);
				}
				else
				{
					glColor4f((float)NumBoxNeutralColor.getRed() / 255.0f, (float)NumBoxNeutralColor.getGreen() / 255.0f, (float)NumBoxNeutralColor.getBlue() / 255.0f, (float)NumBoxNeutralColor.getAlpha() / 255.0f);
				}

				// Drawing image
				glBegin(GL_QUADS);
				glTexCoord2d(0.0d, 1.0d);
				glVertex2d(PosXGL, PosYGL);
				glTexCoord2d(1.0d, 1.0d);
				glVertex2d(PosXGL + ImageWidthGL, PosYGL);
				glTexCoord2d(1.0d, 0.0d);
				glVertex2d(PosXGL + ImageWidthGL, PosYGL + ImageHeightGL);
				glTexCoord2d(0.0d, 0.0d);
				glVertex2d(PosXGL, PosYGL + ImageHeightGL);
				glEnd();
				// Adding width to total width
				Occupied_Width(Occupied_Width() + ImgWidthPixels  + MarginPixels); //

				// Adjusting next PosX for the next image
				PosXGL = PosXGL + ImageWidthGL;
				TotalImageWidthPixels = TotalImageWidthPixels + ImgWidthPixels;
				// Disable so that the next element are written on the same level
				glDisable(GL_DEPTH_TEST);
				glDisable(GL_TEXTURE_2D);
			}

			// Draw Text
			super.Draw(PosX + BorderXPixels + MarginPixels + TotalImageWidthPixels, PosY,GridWidth,GridHeight,Highlight,NotFirstItem, WindowColor);
		}

		// Increase if Right or Decrease left is pressed
		void Action(boolean MenuKeyPressed)
		{
			if(!MenuKeyPressed)
			{
				HoldTimeCounter(0);
			}
			else
			{
				HoldTimeCounter(HoldTimeCounter() + 1);
			}

			if((Keyboard.isKeyDown(Keyboard.KEY_LEFT) || Keyboard.isKeyDown(Keyboard.KEY_DOWN)) && HoldTimeCounter == 0)
			{
				De_Increase(false);
			}
			else if((Keyboard.isKeyDown(Keyboard.KEY_RIGHT) || Keyboard.isKeyDown(Keyboard.KEY_UP)) && HoldTimeCounter == 0)
			{
				De_Increase(true);
			}
		}
	}

	final class MenuItem_TextBox extends MenuItem
	{
		// Attribute
		String TextInside;

		// Constant
		private final int MaxLength = 21;
		private final Color BorderColor = new Color(0, 0, 0, 255);
		private final Color InsideColor = new Color(255, 255, 255, 255);
		private final Color FontColor = new Color(0, 0, 0, 255);
		final int BorderX = 2; // Size of border for the box
		final int BorderY = BorderX + (BorderX * 2 / 3); // Size of border for the box

		// Constructor
		public MenuItem_TextBox(String Text, boolean Enabled,boolean Keep)
		{
			// Initialize super attribute
			super(Text,Enabled,Keep);

			// Initialize text inside
			TextInside = "";
		}

		// Set value of text inside the textbox
		private void TextInside(String Text)
		{
			if(Text.length() < MaxLength)
			{
				if(TextInside().length() > Text.length())
				{
					Text = TextInside().substring(0,TextInside().length() - 1);
				}
				TextInside = Text;
			}
		}

		// Get value of text inside the textbox
		public String TextInside()
		{
			return  TextInside;
		}

		// Get Draw width. Will not draw the item, only count how much width in px it takes.
		public double GetDrawWidth(int GridWidth, int GridHeight, boolean WindowColor, double AdditionalWidth)
		{
			// Initialize total width
			double TotalWidthPixels = 0;

			// Convert to pixel
			double MarginPixels = ConvertPercentToPx(MarginPercent, true, GridWidth, GridHeight);
			double BoxWidthPixels = ConvertPercentToPx((FontWidth + MarginPercent) * MaxLength, true, GridWidth, GridHeight);

			// Adding width to total width
			TotalWidthPixels = TotalWidthPixels + BoxWidthPixels + BorderX + MarginPixels;

			// Return total width
			return super.GetDrawWidth(GridWidth, GridHeight, WindowColor, TotalWidthPixels);
		}

		// Draw the textbox and its text
		public void Draw(double PosX, double PosY, int GridWidth, int GridHeight,boolean Highlight, boolean NotFirstItem, boolean WindowColor)
		{
			// Draw text before (MenuItem Text)
			super.Draw(PosX,PosY,GridWidth,GridHeight,Highlight,NotFirstItem, WindowColor);

			// Disable Depth Test so that the border of the TextBox is writen over rectangle. (The MenuBar and Alpha Rectangle)
			glDisable(GL_DEPTH_TEST);

			// Set draw mode to fill
			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

			// Convert variable to Gl
			double PosXGL = ConvertPxToGL(PosX + Occupied_Width(),true,GridWidth,GridHeight, WindowColor);
			double PosYGL = ConvertPxToGL(PosY,false,GridWidth,GridHeight, WindowColor);
			double BorderXGL = ConvertPxToGL(BorderX, true, GridWidth, GridHeight, WindowColor);
			double BorderYGL = ConvertPxToGL(BorderY, false, GridWidth, GridHeight, WindowColor);
			double MarginGL = ConvertPercentToGL(MarginPercent, true, GridWidth, GridHeight, WindowColor);
			double MarginPixels = ConvertPercentToPx(MarginPercent, true, GridWidth, GridHeight);
			double BoxWidthGL = ConvertPercentToGL((FontWidth + MarginPercent) * MaxLength, true, GridWidth, GridHeight, WindowColor);
			double BoxHeightGL = ConvertPercentToGL(FontHeight + MarginPercent, false, GridWidth, GridHeight, WindowColor);
			double BoxWidthPixels = ConvertPercentToPx((FontWidth + MarginPercent) * MaxLength, true, GridWidth, GridHeight);

			// Draw black border
			glColor4f((float) BorderColor.getRed() / 255.0f, (float) BorderColor.getGreen() / 255.0f, (float) BorderColor.getBlue() / 255.0f,(float)BorderColor.getAlpha() / 255.0f);
			// Draw border
			glRectd(PosXGL - BorderXGL, PosYGL - BorderYGL - MarginGL, PosXGL + BorderXGL + BoxWidthGL + MarginGL, PosYGL + BorderYGL + BoxHeightGL);

			// Draw Pale inside
			// Change color to the color of the inside
			glColor4f(InsideColor.getRed(), InsideColor.getGreen(), InsideColor.getBlue(), InsideColor.getAlpha());

			// Enable so that the textbox is written over the Border

			glEnable(GL_DEPTH_TEST);

			// Draw rectangle
			glRectd(PosXGL, PosYGL - MarginGL, PosXGL + BoxWidthGL + MarginGL, PosYGL + BoxHeightGL);

			// Set Occupied_Width
			Occupied_Width(Occupied_Width() + BoxWidthPixels + BorderX + MarginPixels);

			PosXGL  = PosXGL+ MarginGL;


			// Draw Text inside
			for(int Image = 0; Image < TextInside.length(); Image++)
			{
				glEnable(GL_TEXTURE_2D);

				// Get int value of char
				int CharValue = (String.valueOf(TextInside().charAt(Image)).toUpperCase()).charAt(0);
				int TextureIndex = 0;
				// Check if number
				if(CharValue >= Char0Index && CharValue <= Char9Index)
				{
					// Initialize Texture Index in font List
					TextureIndex = CharValue - Char0Index;
				}
				// Check if letter
				else if(CharValue >= CharAIndex && CharValue <= CharZIndex)
				{
					// Initialize Texture Index in font List
					TextureIndex = CharValue - CharAIndex + 10;
				}
				// Special character
				else if (CharValue == ChaPeriodIndex)
				{
					// Initialize Texture Index in font List
					TextureIndex = 36;
				}
				else if(CharValue == CharColonIndex)
				{
					// Initialize Texture Index in font List
					TextureIndex = 37;
				}

				// Initialize and Bind Texture
				FontArray.get(TextureIndex).Bind();

				// Invert Color

				//glBlendFunc(GL_ZERO,GL_ONE_MINUS_SRC_COLOR);
				// Converting image height and widht
				double ImgWidthGL  = ConvertPercentToGL(FontWidth, true, GridWidth, GridHeight, WindowColor);
				double ImgHeightGL  = ConvertPercentToGL(FontHeight, false, GridWidth, GridHeight, WindowColor);
				double ImgWidthPX = ConvertPercentToPx(FontWidth, true, GridWidth, GridHeight);
				glDisable(GL_DEPTH_TEST);
				// Drawing image
				glBegin(GL_QUADS);
				glTexCoord2d(0.0d, 1.0d);
				glVertex2d(PosXGL, PosYGL);
				glTexCoord2d(1.0d, 1.0d);
				glVertex2d(PosXGL + ImgWidthGL, PosYGL);
				glTexCoord2d(1.0d, 0.0d);
				glVertex2d(PosXGL + ImgWidthGL, PosYGL + ImgHeightGL);
				glTexCoord2d(0.0d, 0.0d);
				glVertex2d(PosXGL, PosYGL + ImgHeightGL);
				glEnd();

				// Adjusting next PosX for the next image
				PosXGL = PosXGL + ImgWidthGL + MarginGL;

				// Reverse color

				glEnable(GL_DEPTH_TEST);

				glDisable(GL_TEXTURE_2D);
			}

		}

		// Draw receive key if its a number.
		public void Action(boolean MenuKeyPressed)
		{
			// Switch of keyboard accepted key (if version)

			if(!MenuKeyPressed)
			{
				// 0
				if(Keyboard.isKeyDown(Keyboard.KEY_0) || Keyboard.isKeyDown(Keyboard.KEY_NUMPAD0))
				{
					// Add 0 to Text inside
					TextInside(TextInside() + '0');
				}
				// 1
				else if(Keyboard.isKeyDown(Keyboard.KEY_1) || Keyboard.isKeyDown(Keyboard.KEY_NUMPAD1))
				{
					// Add 1 to Text inside
					TextInside(TextInside() + '1');
				}
				// 2
				else if(Keyboard.isKeyDown(Keyboard.KEY_2) || Keyboard.isKeyDown(Keyboard.KEY_NUMPAD2))
				{
					// Add 2 to Text inside
					TextInside(TextInside() + '2');
				}
				// 3
				else if(Keyboard.isKeyDown(Keyboard.KEY_3) || Keyboard.isKeyDown(Keyboard.KEY_NUMPAD3))
				{
					// Add 3 to Text inside
					TextInside(TextInside() + '3');
				}
				// 4
				else if(Keyboard.isKeyDown(Keyboard.KEY_4) || Keyboard.isKeyDown(Keyboard.KEY_NUMPAD4))
				{
					// Add 4 to Text inside
					TextInside(TextInside() + '4');
				}
				// 5
				else if(Keyboard.isKeyDown(Keyboard.KEY_5) || Keyboard.isKeyDown(Keyboard.KEY_NUMPAD5))
				{
					// Add 5 to Text inside
					TextInside(TextInside() + '5');
				}
				// 6
				else if(Keyboard.isKeyDown(Keyboard.KEY_6) || Keyboard.isKeyDown(Keyboard.KEY_NUMPAD6))
				{
					// Add 6 to Text inside
					TextInside(TextInside() + '6');
				}
				// 7
				else if(Keyboard.isKeyDown(Keyboard.KEY_7) || Keyboard.isKeyDown(Keyboard.KEY_NUMPAD7))
				{
					// Add 7 to Text inside
					TextInside(TextInside() + '7');
				}
				// 8
				else if(Keyboard.isKeyDown(Keyboard.KEY_8) || Keyboard.isKeyDown(Keyboard.KEY_NUMPAD8))
				{
					// Add 8 to Text inside
					TextInside(TextInside() + '8');
				}
				// 9
				else if(Keyboard.isKeyDown(Keyboard.KEY_9) || Keyboard.isKeyDown(Keyboard.KEY_NUMPAD9))
				{
					// Add 9 to Text inside
					TextInside(TextInside() + '9');
				}
				// a
				else if(Keyboard.isKeyDown((Keyboard.KEY_A)))
				{
					// Add a to Text inside
					TextInside(TextInside() + 'a');
				}
				// b
				else if(Keyboard.isKeyDown((Keyboard.KEY_B)))
				{
					// Add b to Text inside
					TextInside(TextInside() + 'b');
				}
				// c
				else if(Keyboard.isKeyDown((Keyboard.KEY_C)))
				{
					// Add c to Text inside
					TextInside(TextInside() + 'c');
				}
				// d
				else if(Keyboard.isKeyDown((Keyboard.KEY_D)))
				{
					// Add d to Text inside
					TextInside(TextInside() + 'd');
				}
				// e
				else if(Keyboard.isKeyDown((Keyboard.KEY_E)))
				{
					// Add e to Text inside
					TextInside(TextInside() + 'e');
				}
				// f
				else if(Keyboard.isKeyDown((Keyboard.KEY_F)))
				{
					// Add f to Text inside
					TextInside(TextInside() + 'f');
				}
				// g
				else if(Keyboard.isKeyDown((Keyboard.KEY_G)))
				{
					// Add g to Text inside
					TextInside(TextInside() + 'g');
				}
				// h
				else if(Keyboard.isKeyDown((Keyboard.KEY_H)))
				{
					// Add H to Text inside
					TextInside(TextInside() + 'h');
				}
				// i
				else if(Keyboard.isKeyDown((Keyboard.KEY_I)))
				{
					// Add i to Text inside
					TextInside(TextInside() + 'i');
				}
				// j
				else if(Keyboard.isKeyDown((Keyboard.KEY_J)))
				{
					// Add j to Text inside
					TextInside(TextInside() + 'j');
				}
				// k
				else if(Keyboard.isKeyDown((Keyboard.KEY_K)))
				{
					// Add k to Text inside
					TextInside(TextInside() + 'k');
				}
				// l
				else if(Keyboard.isKeyDown((Keyboard.KEY_L)))
				{
					// Add l to Text inside
					TextInside(TextInside() + 'l');
				}
				// m
				else if(Keyboard.isKeyDown((Keyboard.KEY_M)))
				{
					// Add m to Text inside
					TextInside(TextInside() + 'm');
				}
				// n
				else if(Keyboard.isKeyDown((Keyboard.KEY_N)))
				{
					// Add n to Text inside
					TextInside(TextInside() + 'n');
				}
				// o
				else if(Keyboard.isKeyDown((Keyboard.KEY_O)))
				{
					// Add o to Text inside
					TextInside(TextInside() + 'o');
				}
				// p
				else if(Keyboard.isKeyDown((Keyboard.KEY_P)))
				{
					// Add p to Text inside
					TextInside(TextInside() + 'p');
				}
				// q
				else if(Keyboard.isKeyDown((Keyboard.KEY_Q)))
				{
					// Add q to Text inside
					TextInside(TextInside() + 'q');
				}
				// r
				else if(Keyboard.isKeyDown((Keyboard.KEY_R)))
				{
					// Add r to Text inside
					TextInside(TextInside() + 'r');
				}
				// s
				else if(Keyboard.isKeyDown((Keyboard.KEY_S)))
				{
					// Add s to Text inside
					TextInside(TextInside() + 's');
				}
				// t
				else if(Keyboard.isKeyDown((Keyboard.KEY_T)))
				{
					// Add t to Text inside
					TextInside(TextInside() + 't');
				}
				// u
				else if(Keyboard.isKeyDown((Keyboard.KEY_U)))
				{
					// Add u to Text inside
					TextInside(TextInside() + 'u');
				}
				// v
				else if(Keyboard.isKeyDown((Keyboard.KEY_V)))
				{
					// Add v to Text inside
					TextInside(TextInside() + 'v');
				}
				// w
				else if(Keyboard.isKeyDown((Keyboard.KEY_W)))
				{
					// Add w to Text inside
					TextInside(TextInside() + 'w');
				}
				// x
				else if(Keyboard.isKeyDown((Keyboard.KEY_X)))
				{
					// Add x to Text inside
					TextInside(TextInside() + 'x');
				}
				// y
				else if(Keyboard.isKeyDown((Keyboard.KEY_Y)))
				{
					// Add y to Text inside
					TextInside(TextInside() + 'y');
				}
				// z
				else if(Keyboard.isKeyDown((Keyboard.KEY_Z)))
				{
					// Add z to Text inside
					TextInside(TextInside() + 'z');
				}
				// .
				else if(Keyboard.isKeyDown((Keyboard.KEY_PERIOD)))
				{
					// Add . to Text inside
					TextInside(TextInside() + '.');
				}
				// :
				else if(Keyboard.isKeyDown((Keyboard.KEY_SEMICOLON)))
				{
					// Add : to Text inside
					TextInside(TextInside() + ':');
				}
				// If backspace
				else if(Keyboard.isKeyDown(Keyboard.KEY_BACK))
				{
					// Remove last char (except if last)
					if(TextInside().length() > 0)
					{
						TextInside(TextInside().substring(0,TextInside().length() - 1));
					}
				}
			}

		}
	}

	// Derived from MenuItem, contains method to draw a Image
	final class MenuItem_PictureBox extends  MenuItem
	{
		// Attribute
		private int TextureIndex;

		// Constant
		final double ImageWidth = 10.0d;
		final double ImageHeight = RowHeight * 3;
		final double BorderX = 0.8d;
		final double BorderY = BorderX + 2.0d/3.0d;
		final Color BorderColor = new Color(0,0,0,255);

		// Constructor
		public MenuItem_PictureBox(int ImageIndex)
		{
			// call super
			super("",false,true);

			// initialize index
			TextureIndex(ImageIndex);
		}

		// Set value of Texture index
		private void TextureIndex(int Index)
		{
			// Check if valid index
			if(Index >= 0 && Index < SpecialImageArray.size())
			{
				TextureIndex = Index;
			}
		}

		// Get value of Texture Index
		public int TextureIndex()
		{
			return TextureIndex;
		}

		// Get Draw width. Will not draw the item, only count how much width in px it takes.
		public double GetDrawWidth(int GridWidth,int GridHeight,boolean WindowColor,double AdditionalWidth)
		{
			// Initialize total width
			double TotalWidthPixels = 0;

			// Convert to pixel
			double BorderXPixels = ConvertPercentToPx(BorderX, true, GridWidth, GridHeight);
			double ImageWidthPixels = ConvertPercentToPx(ImageWidth, true, GridWidth, GridHeight);

			// Adding width to total width
			TotalWidthPixels = BorderXPixels + ImageWidthPixels;

			// Return total width
			return super.GetDrawWidth(GridWidth, GridHeight, WindowColor, TotalWidthPixels);
		}

		// Draw image with border
		public void Draw(double PosX, double PosY, int GridWidth, int GridHeight,boolean Highlight, boolean NotFirstItem, boolean WindowColor)
		{
			// Initialize coordonate
			double PosXGL = ConvertPxToGL(PosX, true, GridWidth, GridHeight, WindowColor);
			double PosYGL = ConvertPxToGL(PosY, false, GridWidth, GridHeight, WindowColor);
			double ImageWidthGL = ConvertPercentToGL(ImageWidth, true, GridWidth, GridHeight, WindowColor);
			double ImageHeightGL = ConvertPercentToGL(ImageHeight, false, GridWidth, GridHeight, WindowColor);
			double ImageWidthPixels = ConvertPercentToPx(ImageWidth, true, GridWidth, GridHeight);
			double BorderXGL = ConvertPercentToGL(BorderX, true, GridWidth, GridHeight, WindowColor);
			double BorderXPixels = ConvertPercentToPx(BorderX, true, GridWidth, GridHeight);
			double BorderYGL = ConvertPercentToGL(BorderY, false, GridWidth, GridHeight, WindowColor);

			// Draw border
			glColor4f(BorderColor.getRed() / 255.0f, BorderColor.getGreen() / 255.0f, BorderColor.getBlue() / 255.0f, BorderColor.getAlpha() / 255.0f);

			glRectd(PosXGL, PosYGL - BorderYGL, PosXGL + ImageWidthGL + BorderXGL  * 2, PosYGL + ImageHeightGL + BorderYGL);

			// Draw image
			glDisable(GL_DEPTH_TEST);
			glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			glEnable(GL_TEXTURE_2D);

			SpecialImageArray.get(TextureIndex).Bind();

			glBegin(GL_QUADS);
			glTexCoord2d(0.0d, 1.0d);
			glVertex2d(PosXGL + BorderXGL , PosYGL);
			glTexCoord2d(1.0d, 1.0d);
			glVertex2d(PosXGL + BorderXGL + ImageWidthGL, PosYGL);
			glTexCoord2d(1.0d, 0.0d);
			glVertex2d(PosXGL + BorderXGL + ImageWidthGL, PosYGL + ImageHeightGL);
			glTexCoord2d(0.0d, 0.0d);
			glVertex2d(PosXGL + BorderXGL, PosYGL + ImageHeightGL);
			glEnd();

			glDisable(GL_TEXTURE_2D);
			glEnable(GL_DEPTH_TEST);
			// Set occupied width
			Occupied_Width(ImageWidthPixels + BorderXPixels);
		}
	}

	final class MenuItem_ComboBox extends MenuItem
	{
		// Attribute
		List<String> Items;
		int ItemsIndex;

		// Constant
		final Color BorderColor = new Color(0, 0, 0, 255);
		final Color BackGroundColor = new Color(255, 255, 255, 255);
		final double BorderX = 2.0d;
		final double BorderY = BorderX * (2.0d / 3.0d);
		final int MaxLength = 15;

		// Constructor
		public MenuItem_ComboBox(String Text,boolean Enabled,boolean Keep,List<String> ItemsText)
		{
			// Initialize super attribute
			super(Text,Enabled,Keep);

			// Initialize Item
			Items = ItemsText;

			// Initialize Index
			ItemsIndex(0);
		}

		// Set value of ItemsIndex
		private void ItemsIndex(int Index)
		{
			ItemsIndex = Index;
		}

		// Get value of current item
		public String CurrentItem()
		{
			return Items.get(ItemsIndex);
		}

		// Get value of Index
		public int ItemsIndex()
		{
			return ItemsIndex;
		}

		public void AddItem(String Item)
		{
			Items.add(Item);
		}

		// Get Draw width. Will not draw the item, only count how much width in px it takes.
		public double GetDrawWidth(int GridWidth,int GridHeight,boolean WindowCoor,double AdditionalWidth)
		{
			// Initialize total width
			double TotalWidthPixels = 0;

			// Convert to pixel
			double MarginPixels = ConvertPercentToPx(MarginPercent, true, GridWidth, GridHeight);
			double BoxWidthPixels = ConvertPercentToPx((FontWidth + MarginPercent) * MaxLength, true, GridWidth, GridHeight);

			// Adding width to total width
			TotalWidthPixels = BoxWidthPixels + MarginPixels + BorderX;

			// Return total width
			return super.GetDrawWidth(GridWidth,GridHeight,WindowCoor,TotalWidthPixels);
		}

		// Draw combo box
		public void Draw(double PosX, double PosY, int GridWidth, int GridHeight,boolean Highlight,boolean NotFirstItem,boolean WindowColor)
		{
			// Draw text before (MenuItem Text)
			super.Draw(PosX,PosY,GridWidth,GridHeight,Highlight,NotFirstItem, WindowColor);

			// Disable Depth Test so that the border of the Combo Box is writen over background.
			glDisable(GL_DEPTH_TEST);

			// Set draw mode to fill
			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

			// Convert variable to Gl
			double PosXGL = ConvertPxToGL(PosX + Occupied_Width(),true,GridWidth,GridHeight, WindowColor);
			double PosYGL = ConvertPxToGL(PosY,false,GridWidth,GridHeight, WindowColor);
			double BorderXGL = ConvertPxToGL(BorderX, true, GridWidth, GridHeight, WindowColor);
			double BorderYGL = ConvertPxToGL(BorderY, false, GridWidth, GridHeight, WindowColor);
			double MarginGL = ConvertPercentToGL(MarginPercent, true, GridWidth, GridHeight, WindowColor);
			double MarginPixels = ConvertPercentToPx(MarginPercent, true, GridWidth, GridHeight);
			double BoxWidthGL = ConvertPercentToGL((FontWidth + MarginPercent) * MaxLength, true, GridWidth, GridHeight, WindowColor);
			double BoxHeightGL = ConvertPercentToGL(FontHeight + MarginPercent, false, GridWidth, GridHeight, WindowColor);
			double BoxWidthPixels = ConvertPercentToPx((FontWidth + MarginPercent) * MaxLength, true, GridWidth, GridHeight);

			// Draw black border
			glColor4f((float) BorderColor.getRed() / 255.0f, (float) BorderColor.getGreen() / 255.0f, (float) BorderColor.getBlue() / 255.0f,(float)BorderColor.getAlpha() / 255.0f);
			// Draw border
			glRectd(PosXGL - BorderXGL, PosYGL - BorderYGL - MarginGL, PosXGL + BorderXGL + BoxWidthGL + MarginGL, PosYGL + BorderYGL + BoxHeightGL);

			// Draw Pale inside
			// Change color to the color of the inside
			glColor4f(BackGroundColor.getRed(), BackGroundColor.getGreen(), BackGroundColor.getBlue(), BackGroundColor.getAlpha());

			// Enable so that the textbox is written over the Border
			glEnable(GL_DEPTH_TEST);

			// Draw rectangle
			glRectd(PosXGL, PosYGL - MarginGL, PosXGL + BoxWidthGL + MarginGL, PosYGL + BoxHeightGL);

			// Set Occupied_Width
			Occupied_Width(Occupied_Width() + BoxWidthPixels + BorderX + MarginPixels);

			double ImagePosXGL  = PosXGL + MarginGL;

			// Draw Text inside
			for (int Image = 0; Image < MaxLength && Image < Items.get(ItemsIndex).length(); Image++)
			{
				glEnable(GL_TEXTURE_2D);


				// Get int value of char
				int CharValue = (String.valueOf(Items.get(ItemsIndex).charAt(Image)).toUpperCase()).charAt(0);
				int TextureIndex = 0;
				if (CharValue == ' ')
				{
					// Adding width to total width
					ImagePosXGL = ImagePosXGL + ConvertPercentToGL(FontWidth, true, GridWidth, GridHeight, WindowColor) + MarginGL;
				}
				else
				{
					// Check if it's a number
					if (CharValue >= Char0Index && CharValue <= Char9Index)
					{
						// Initialize Texture Index in font List
						TextureIndex = CharValue - Char0Index;
					}
					else if (CharValue == CharCommercialAIndex)
					{
						TextureIndex = 10;
					}
					// Check if letter
					else if (CharValue >= CharAIndex && CharValue <= CharZIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = CharValue - CharAIndex + 10;
					}
					// Special character
					else if (CharValue == ChaPeriodIndex || CharValue == CharCommaIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = 36;
					}
					else if (CharValue == CharColonIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = 37;
					}
					else if (CharValue == CharUnderScoreIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = 38;
					}
					else if (CharValue == CharApostropheIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = 39;
					}
					else if (CharValue == CharAsteriskIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = 40;
					}
					else if (CharValue == CharLeftParaIndex || CharValue == CharLeftSquareIndex || CharValue == CharLeftBraceIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = 41;
					}
					else if (CharValue == CharRightParaIndex || CharValue == CharRightSquareIndex || CharValue == CharRightBraceIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = 42;
					}
					else if (CharValue == CharPlusIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = 43;
					}
					else if (CharValue == CharMinusIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = 44;
					}
					else if (CharValue == CharNumberIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = 45;
					}
					else if (CharValue == CharExclamationIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = 46;
					}
					else // Character not define. Write ?.
					{
						// Initialize Texture Index in font List
						TextureIndex = 47;
					}

					// Initialize and Bind Texture
					FontArray.get(TextureIndex).Bind();

					// Converting image height and widht
					double ImgWidthGL  = ConvertPercentToGL(FontWidth, true, GridWidth, GridHeight, WindowColor);
					double ImgHeightGL  = ConvertPercentToGL(FontHeight, false, GridWidth, GridHeight, WindowColor);
					double ImgWidthPX = ConvertPercentToPx(FontWidth, true, GridWidth, GridHeight);
					glDisable(GL_DEPTH_TEST);
					// Drawing image
					glBegin(GL_QUADS);
					glTexCoord2d(0.0d, 1.0d);
					glVertex2d(ImagePosXGL, PosYGL);
					glTexCoord2d(1.0d, 1.0d);
					glVertex2d(ImagePosXGL + ImgWidthGL, PosYGL);
					glTexCoord2d(1.0d, 0.0d);
					glVertex2d(ImagePosXGL + ImgWidthGL, PosYGL + ImgHeightGL);
					glTexCoord2d(0.0d, 0.0d);
					glVertex2d(ImagePosXGL, PosYGL + ImgHeightGL);
					glEnd();

					// Adjusting next PosX for the next image
					ImagePosXGL = ImagePosXGL + ImgWidthGL + MarginGL;

					glEnable(GL_DEPTH_TEST);

					glDisable(GL_TEXTURE_2D);
				}
			}

			// Draw selector image at the end of the Combo Box
			glEnable(GL_TEXTURE_2D);

			// Bind texture
			SpecialImageArray.get(3).Bind();

			// Draw image
			// Converting image height and widht
			double ImgWidthGL  = ConvertPercentToGL(FontWidth, true, GridWidth, GridHeight, WindowColor);
			double ImgHeightGL  = ConvertPercentToGL(FontHeight, false, GridWidth, GridHeight, WindowColor);
			glDisable(GL_DEPTH_TEST);
			// Drawing image
			glBegin(GL_QUADS);
			glTexCoord2d(0.0d, 1.0d);
			glVertex2d(PosXGL + BorderXGL + BoxWidthGL + MarginGL, PosYGL - BorderYGL - MarginGL);
			glTexCoord2d(1.0d, 1.0d);
			glVertex2d(PosXGL + BorderXGL + BoxWidthGL + MarginGL + ImgWidthGL, PosYGL - BorderYGL - MarginGL);
			glTexCoord2d(1.0d, 0.0d);
			glVertex2d(PosXGL + BorderXGL + BoxWidthGL + MarginGL + ImgWidthGL, PosYGL + ImgHeightGL + BorderYGL + MarginGL);
			glTexCoord2d(0.0d, 0.0d);
			glVertex2d(PosXGL + BorderXGL + BoxWidthGL + MarginGL, PosYGL + ImgHeightGL + BorderYGL + MarginGL) ;
			glEnd();

			glDisable(GL_TEXTURE_2D);
		}

		// Change Item index
		public void Action(boolean MenuKeyPressed)
		{
			// Up
			if(Keyboard.isKeyDown(Keyboard.KEY_UP) && !MenuKeyPressed)
			{
				// Check if index can go "up"
				if(ItemsIndex > 0)
				{
					ItemsIndex--;
				}
			}
			// Down
			else if(Keyboard.isKeyDown(Keyboard.KEY_DOWN) && !MenuKeyPressed)
			{
				// Check if item index can go down
				if(ItemsIndex < Items.size() - 1)
				{
					ItemsIndex++;
				}
			}
		}
	}

	// Derived from MenuItem, contains a method to call a specific dialog
	final class MenuItem_Windows extends MenuItem
	{
		// Attribute
		MenuWindows Window;
		private boolean WindowActive; // Define if window is draw or not

		public void WindowActive(boolean Active)
		{
			WindowActive = Active;
		}

		public boolean WindowActive()
		{
			return WindowActive;
		}
		// Constructor
		// Default constructor
		public MenuItem_Windows()
		{
			// Call the default super constructor
			super();
		}

		// Parametred constructor
		public MenuItem_Windows(String Text, boolean Enabled,boolean Keep,MenuWindows Windows)
		{
			// Initialize attribute
			super(Text,Enabled,Keep);

			// Initialize window
			Window = Windows;

			WindowActive = false;
		}

		// Public Function

		// Get Draw width. Will not draw the item, only count how much width in px it takes.
		public double GetDrawWidth(double PosX, int GridWidth, int GridHeight, boolean WindowColor, double AdditionalWidth)
		{
			double TotalWidthPixels = 0;

			// Return total width
			return super.GetDrawWidth(GridWidth, GridHeight, WindowColor, TotalWidthPixels);
		}

		// Draw window at the center of the screen
		public void Draw(double PosX, double PosY, int GridWidth, int GridHeight, boolean Highlight, boolean NotFirstItem, boolean WindowColor) {
			// Call draw of window if it's active
			if (WindowActive())
			{
				Window.Draw(GridWidth, GridHeight);
			}
			else
			{
				// Call draw of text
				super.Draw(PosX, PosY, GridWidth, GridHeight, Highlight, NotFirstItem, WindowColor);
			}
		}

		// Action receive input from menu
		public void Action(boolean MenuKeyPressed)
		{
			// Check if window is active
			if(WindowActive())
			{
				// Transfert them to window action
				if(Window.Action(MenuKeyPressed))
				{
					// Close window
					WindowActive(false);
					ExitWindow = true;
				}
			}
		}
	}

	abstract class MenuWindows
	{
		// Attribute
		protected String TitleText;
		protected double WindowsWidth;
		protected double WindowsHeight;
		protected boolean WindowLock;

		// Constant
		final int BorderX = 2; // Size of border for the box
		final int BorderY = BorderX + (BorderX * 2 / 3); // Size of border for the box
		final float MarginPercent = 0.4f; // Margin between text.
		final int SpaceSize = 2; // Multiply margin by this number to create space
		final double TitleFontWidth = 1.34375d; // Value in %, must be converted to GL unit
		final double TitleFontHeight = 3.2083333333333333333333333333333d; // Value in %, must be converted to GL unit
		// Constructor
		public MenuWindows(String Title,double Width,double Height)
		{
			// Initialize attribute
			TitleText(Title);
			WindowsWidth(Width);
			WindowsHeight(Height);

		}

		// Protected function

		// Set title of window
		protected void TitleText(String Title)
		{
			TitleText = Title;
		}

		// Set window's width
		protected void WindowsWidth(double Width)
		{
			WindowsWidth = Width;
		}

		// Set window's height
		protected void WindowsHeight(double Height)
		{
			WindowsHeight = Height;
		}

		// Set lock value
		protected void WindowLock(boolean Lock)
		{
			WindowLock = Lock;
		}

		// Public function

		// Get title
		public String TitleText()
		{
			return TitleText;
		}

		// Get width
		public double WindowsWidth()
		{
			return WindowsWidth;
		}

		// Get Height
		public double WindowsHeight()
		{
			return WindowsHeight;
		}

		// Get Lock
		public boolean WindowLock()
		{
			return WindowLock;
		}

		// Abstract function
		abstract void Draw(int GridWidth,int GridHeight);

		abstract boolean Action(boolean MenuKeyPressed);
	}

	class MenuWindows_NewGame extends MenuWindows
	{
		// Attribute
		List<List<MenuItem>> Items = new ArrayList<List<MenuItem>>();

		//Menu_Boolean RecordDemo;
		Menu_Integer GameMode;
		Menu_Integer TimeLimit;
		Menu_Integer KillLimit;

		int RowCursor;
		int ColumnCursor;

		int[] PosStart;
		int[] PosClose;

		int[] PosFirstRadio;

		int[] PosPictureBox;

		int[] PosComboBox;

		//int[] PosTextBox;

		// Constant
		final int RadioButtonNumber = 3;
		final double TitleHeight = 13.25d; // Smaller = Bigger.
		final int TitleHeightAdjust = 5;
		final Color WindowColor = new Color(160, 160, 160, 255);
		// Constructor
		public MenuWindows_NewGame(String Title,int Width,int Height)
		{
			// Initialize super attribute
			super(Title, Width, Height);

			// Initialize gamemode
			GameMode = new Menu_Integer();
			GameMode.Int(0);

			// Initialize record demo
			//RecordDemo = new Menu_Boolean();
			//RecordDemo.Bool(false);

			// Initialize limits
			TimeLimit = new Menu_Integer();
			TimeLimit.Int(0);

			KillLimit = new Menu_Integer();
			KillLimit.Int(25);

			// Initialize radio Text
			String[] RadioText = new String[3];
			RadioText[0] = "Free for all";
			RadioText[1] = "One Shot Kill";
			RadioText[2] = "Flag Tag";
			// Initialize radio group
			MenuItem_RadioButtonGroup RadioGroup = new MenuItem_RadioButtonGroup(RadioText, GameMode);

			// Initialize position of Start and Close
			PosStart = new int[2];
			PosStart[0] = 0;
			PosStart[1] = 9;

			PosClose = new int[2];
			PosClose[0] = 1;
			PosClose[1] = 9;

			// Initialize Pos of textbox
			//PosTextBox = new int[2];
			//PosTextBox[0] = 0;
			//PosTextBox[1] = 6;

			// Initialize position of first radio and Picture box
			PosFirstRadio = new int[2];
			PosFirstRadio[0] = 0;
			PosFirstRadio[1] = 1;

			PosPictureBox = new int[2];
			PosPictureBox[0] = 1;
			PosPictureBox[1] = 3;

			PosComboBox = new int[2];
			PosComboBox[0] = 0;
			PosComboBox[1] = 6;

			// Initialize cursor
			ColumnCursor = 0;
			RowCursor = 1;

			// Add item to window
			List<String> Maps = new ArrayList<String>();
			// Load all map from map folder
			try
			{
				List<File> FilesInFolder = Files.walk(Paths.get("res/maps/"))
						.filter(Files::isRegularFile)
						.map(Path::toFile)
						.collect(Collectors.toList());

				for (int Map = 0; Map < FilesInFolder.size();Map++)
				{
					Maps.add(FilesInFolder.get(Map).getName().substring(0,FilesInFolder.get(Map).getName().indexOf(".")));
				}
			}
			catch (IOException ex)
			{
				System.out.println("Error while loading file from maps folder");
			}

			// Column with GameMode Radio buttons, Record Demo Checkbox, KillLimit and Connect Button
			List<MenuItem> Column1 = new ArrayList<MenuItem>();
			Column1.add(new MenuItem("GameMode", false, true));
			Column1.add(RadioGroup.RadioButtons[0]);
			Column1.add(RadioGroup.RadioButtons[1]);
			Column1.add(RadioGroup.RadioButtons[2]);
			Column1.add(new MenuItem("", false, false)); // Spacer
			//Column1.add(new MenuItem_CheckBox("Record Demo",true,false,RecordDemo));
			//Column1.add(new MenuItem_TextBox("Demo Name",true,false));
			Column1.add(new MenuItem_NumberBox("Kill Limit", true, false, KillLimit, 1, 50, 1));
			Column1.add(new MenuItem_ComboBox("Map:", true, false, Maps));
			Column1.add(new MenuItem_Dialog("Browse a Map", true, false, "Choose a Map", "txt", "Text File"));
			Column1.add(new MenuItem("", false, false)); // Spacer
			Column1.add(new MenuItem("Start", true, false));

			// Column with Image ("special Texture"), Save as TextBox, TimeLimit and close button
			List<MenuItem> Column2 = new ArrayList<MenuItem>();
			Column2.add(new MenuItem("", false, false)); // Spacer
			Column2.add(new MenuItem("", false, false)); // Spacer
			Column2.add(new MenuItem("", false, false)); // Spacer
			Column2.add(new MenuItem_PictureBox(0));
			//Column2.add(new MenuItem("",false,false)); // Spacer
			//Column2.add(new MenuItem("",false,false)); // Spacer
			Column2.add(new MenuItem("", false, false)); // Spacer
			Column2.add(new MenuItem_NumberBox("Time Limit", true, false, TimeLimit, 0, 60, 1));
			Column2.add(new MenuItem("", false, false)); // Spacer
			Column2.add(new MenuItem("", false, false)); // Spacer
			Column2.add(new MenuItem("", false, false)); // Spacer
			Column2.add(new MenuItem("Close", true, false));

			Items.add(Column1);
			Items.add(Column2);
		}

		// Private function

		// Set value of ColumnCursor
		// Move the cursor from 1 to the right
		private void CursorRight()
		{
			// Check if cursor can move to right
			if (ColumnCursor < Items.size() - 1)
			{
				ColumnCursor++;
				// Check if SubMenuCursor to big to fit next column
				if (RowCursor >= Items.get(ColumnCursor).size() - 1)
				{
					RowCursor = Items.get(ColumnCursor).size() - 1;
				}
				// Check if Item is not enable
				while (RowCursor < Items.get(ColumnCursor).size() && !Items.get(ColumnCursor).get(RowCursor).Enabled())
				{
					RowCursor++;
				}

				// Adjust Row if out of range
				if (RowCursor >= Items.get(ColumnCursor).size())
				{
					RowCursor = Items.get(ColumnCursor).size() - 1;
				}

				// If not item enable down, go up
				while (RowCursor >= 0 && !Items.get(ColumnCursor).get(RowCursor).Enabled())
				{
					RowCursor--;
				}
			}
		}

		// Move cursor from 1 to the left
		public void CursorLeft()
		{
			// Check if cursor can move to left
			if (ColumnCursor > 0)
			{
				ColumnCursor--;
				// Check if SubMenuCursor to big to fit next column
				if (RowCursor >= Items.get(ColumnCursor).size() - 1)
				{
					RowCursor = Items.get(ColumnCursor).size() - 1;
				}
				// Check if Item is not enable
				while (RowCursor >= 0 && !Items.get(ColumnCursor).get(RowCursor).Enabled())
				{
					RowCursor--;
				}

				// Adjust Row if out of range
				if (RowCursor < 0)
				{
					RowCursor = 0;
				}

				// If not item enable up, go down
				while (RowCursor < Items.get(ColumnCursor).size() && !Items.get(ColumnCursor).get(RowCursor).Enabled())
				{
					RowCursor++;
				}
			}
		}

		// Move cursor down from 1
		public void CursorDown()
		{
			// Check if cursor can move down
			if (RowCursor < Items.get(ColumnCursor).size() - 1)
			{
				RowCursor++;
			}
			// Check if Item is not enable
			while (RowCursor < Items.get(ColumnCursor).size() && !Items.get(ColumnCursor).get(RowCursor).Enabled())
			{
				RowCursor++;
			}

			// Adjust Row if out of range
			if (RowCursor >= Items.get(ColumnCursor).size())
			{
				RowCursor = Items.get(ColumnCursor).size() - 1;
			}

			// If not item enable down, go up
			while (RowCursor >= 0 && !Items.get(ColumnCursor).get(RowCursor).Enabled())
			{
				RowCursor--;
			}
		}

		// Move cursor up from 1
		public void CursorUp()
		{
			// Check if cursor can move up
			if (RowCursor > 0)
			{
				RowCursor--;
			}
			// Check if Item is not enable
			while (RowCursor >= 0 && !Items.get(ColumnCursor).get(RowCursor).Enabled())
			{
				RowCursor--;
			}

			// Adjust Row if out of range
			if (RowCursor < 0)
			{
				RowCursor = 0;
			}

			// If not item enable up, go down
			while (RowCursor < Items.get(ColumnCursor).size() && !Items.get(ColumnCursor).get(RowCursor).Enabled())
			{
				RowCursor++;
			}
		}

		// Public function

		// Get ColumnCursor value
		public int ColumnCursor()
		{
			return ColumnCursor;
		}

		// Get RowCursor value
		public int RowCursor()
		{
			return RowCursor;
		}

		// Override function
		void Draw(int GridWidth,int GridHeight)
		{
			// Disable Depth Test so that the border of the Window is written over background. (The MenuBar and Alpha Rectangle)
			glDisable(GL_DEPTH_TEST);

			// Set draw mode to fill
			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

			// Use center to find border of windows
			double BottomLeftXGL = ConvertPxToGL(((float)GridWidth / 2.0f) - ConvertPercentToPx(WindowsWidth() / 2.0f, true, GridWidth, GridHeight),true,GridWidth,GridHeight,true);
			double BottomLeftYGL = ConvertPxToGL(((float)GridHeight / 2.0f) - ConvertPercentToPx(WindowsHeight() / 2.0f, false, GridWidth, GridHeight),false,GridWidth,GridHeight,true);
			double TopRightXGL = ConvertPxToGL(((float)GridWidth / 2.0f) + ConvertPercentToPx(WindowsWidth() / 2.0f, true, GridWidth, GridHeight),true,GridWidth,GridHeight,true);
			double TopRightYGL = ConvertPxToGL(((float)GridHeight / 2.0f) + ConvertPercentToPx(WindowsHeight() / 2.0f, false, GridWidth, GridHeight),false,GridWidth,GridHeight,true);
			double MargeX = ConvertPxToGL(BorderX, true, GridWidth, GridHeight,false);
			double MargeY = ConvertPxToGL(BorderY, false, GridWidth, GridHeight,false);

			// Move cursor to bottom of window
			glTranslatef(0, -2, 0);

			// Draw border of window
			glColor4f(0.0f, 0.0f, 0.0f, 1.0f);

			glRectd(BottomLeftXGL - MargeX, BottomLeftYGL - MargeY, TopRightXGL + MargeX, TopRightYGL + MargeY);

			// Draw window
			glColor4f(WindowColor.getRed() / 255.0f, WindowColor.getGreen() / 255.0f, WindowColor.getBlue() / 255.0f, WindowColor.getAlpha() / 255.0f);
			glRectd(BottomLeftXGL, BottomLeftYGL, TopRightXGL, TopRightYGL);

			glDisable(GL_DEPTH_TEST);

			// Draw Title
			double TitleBottomYGL = ConvertPxToGL(((float) GridHeight / 2.0f) + (ConvertPercentToPx(WindowsHeight() / 2.0f, false, GridWidth, GridHeight)) + ConvertPercentToPx(TitleHeight, false, GridWidth, GridHeight),false,GridWidth,GridHeight,false);

			glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
			glRectd(BottomLeftXGL, TitleBottomYGL, TopRightXGL, TopRightYGL);

			// Draw Text of Title

			String Text = TitleText();
			// Convert % to GL
			double MarginGL = ConvertPercentToGL(MarginPercent, true, GridWidth, GridHeight, false);

			glColor4d(255, 255, 255, 255);

			// Initialize X coordonate
			double StartTextXGL = ConvertPxToGL(((float)GridWidth / 2.0f) - (ConvertPercentToPx(WindowsWidth() / 2.0f, true, GridWidth, GridHeight)),true,GridWidth,GridHeight,true);

			// Loop that check each letter of ItemText and load corresponding image and then draw it
			for (int Letter = 0; Letter < Text.length(); Letter++)
			{
				if(Text.charAt(Letter) == ' ')
				{
					// Adjusting next PosX for the next image
					StartTextXGL = StartTextXGL + MarginGL * SpaceSize;
				}
				else
				{
					glEnable(GL_TEXTURE_2D);

					// Get int value of char
					int CharValue = (String.valueOf(Text.charAt(Letter)).toUpperCase()).charAt(0);
					int TextureIndex = 0;
					// Check if number
					if (CharValue >= Char0Index && CharValue <= Char9Index)
					{
						// Initialize Texture Index in font List
						TextureIndex = CharValue - Char0Index;
					}
					// Check if letter
					else if (CharValue >= CharAIndex && CharValue <= CharZIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = CharValue - CharAIndex + 10;
					}
					// Special character
					else if (CharValue == ChaPeriodIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = 36;
					}
					else if (CharValue == CharColonIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = 37;
					}

					// Initialize and Bind Texture
					FontArray.get(TextureIndex).Bind();

					// Converting image height and widht
					double ImgWidthGL  = ConvertPercentToGL(TitleFontWidth, true, GridWidth, GridHeight, false);
					double ImgHeightGL  = ConvertPercentToGL(TitleFontHeight, false, GridWidth, GridHeight, false);

					glDisable(GL_DEPTH_TEST);
					// Drawing image
					glBegin(GL_QUADS);
					glTexCoord2d(0.0d, 1.0d);
					glVertex2d(StartTextXGL, TitleBottomYGL + MarginGL);
					glTexCoord2d(1.0d, 1.0d);
					glVertex2d(StartTextXGL + ImgWidthGL, TitleBottomYGL + MarginGL);
					glTexCoord2d(1.0d, 0.0d);
					glVertex2d(StartTextXGL + ImgWidthGL, TitleBottomYGL + MarginGL + ImgHeightGL);
					glTexCoord2d(0.0d, 0.0d);
					glVertex2d(StartTextXGL, TitleBottomYGL + ImgHeightGL + MarginGL);
					glEnd();
					// Adjusting next PosX for the next image
					StartTextXGL = StartTextXGL + ImgWidthGL + MarginGL;

					glDisable(GL_TEXTURE_2D);
				}
			}

			double MarginPixels = ConvertPercentToPx(MarginPercent, true, GridWidth, GridHeight);
			double PosX = (((float)GridWidth / 2.0f) - ConvertPercentToPx(WindowsWidth() / 2.0f, true, GridWidth, GridHeight)) + BorderX + MarginPixels;
			double PosY = ((float) GridHeight / 2.0f) + (ConvertPercentToPx(WindowsHeight() / 2.0f, false, GridWidth, GridHeight)) - ConvertPercentToPx(TitleHeight - TitleHeightAdjust, false, GridWidth, GridHeight);
			double BiggestItemWidth = 0;

			glDisable(GL_DEPTH_TEST);
			// Draw all control on a grid system (like menu)
			for (int ItemArray = 0; ItemArray < Items.size(); ItemArray++)
			{
				for (int Item = 0;Item < Items.get(ItemArray).size();Item++)
				{

					// Check if Cursor is on this item
					if (ItemArray == ColumnCursor && Item == RowCursor)
					{
						// Draw item
						Items.get(ItemArray).get(Item).Draw(PosX, PosY, GridWidth, GridHeight, true, true, true);
					}
					else
					{
						// Draw item
						Items.get(ItemArray).get(Item).Draw(PosX, PosY, GridWidth, GridHeight, false, false, true);
					}
					// Adjust next Y
					PosY = PosY - ConvertPercentToPx(RowHeight, false, GridWidth, GridHeight);

					// Check if occupied width of the drawn items is bigger that the previous
					if (Items.get(ItemArray).get(Item).Occupied_Width() > BiggestItemWidth && !Items.get(ItemArray).get(Item).getClass().equals(Menu.MenuItem_ComboBox.class) && !Items.get(ItemArray).get(Item).getClass().equals(Menu.MenuItem_TextBox.class))
					{
						BiggestItemWidth = Items.get(ItemArray).get(Item).Occupied_Width();
					}

					// Reset occupied width to 0
					Items.get(ItemArray).get(Item).Occupied_Width(0);
				}
				PosY = ((float) GridHeight / 2.0f) + (ConvertPercentToPx(WindowsHeight() / 2.0f, false, GridWidth, GridHeight)) - ConvertPercentToPx(TitleHeight - TitleHeightAdjust, false, GridWidth, GridHeight);
				PosX = (((float)GridWidth / 2.0f) - ConvertPercentToPx(WindowsWidth() / 2.0f, true, GridWidth, GridHeight)) + BorderX + BiggestItemWidth + MarginPixels * 3;
			}

			// Move to the top of window
			glTranslatef(0, 2, 0);
		}

		// Use input to change state of window
		public boolean Action(boolean MenuKeyPressed)
		{
			// Declare local boolean
			boolean CloseWindow = false;
			// If lock, send action to actual item
			if (WindowLock())
			{
				Items.get(ColumnCursor).get(RowCursor).Action(MenuKeyPressed);
			}
			if (!MenuKeyPressed)
			{
				// Enter
				if (Keyboard.isKeyDown(Keyboard.KEY_RETURN))
				{
					if (Items.get(ColumnCursor).get(RowCursor).getClass().equals(MenuItem_CheckBox.class)) // Checkbox
					{
						// De/Activate checkbox at this index
						((MenuItem_CheckBox)(Items.get(ColumnCursor).get(RowCursor))).IsChecked(!((MenuItem_CheckBox) (Items.get(ColumnCursor).get(RowCursor))).IsChecked());
					}
					else if (Items.get(ColumnCursor).get(RowCursor).getClass().equals(MenuItem_RadioButton.class)) // Radio
					{
						// De/Activate Radio Button at this index
						((MenuItem_RadioButton)(Items.get(ColumnCursor).get(RowCursor))).IsChecked(true);
						// Change picture box image
						ChangeImage();
					}
					else if (ColumnCursor == PosStart[0] && RowCursor == PosStart[1]) // Create
					{
						// Quit the game
						QuitGame();

						// Connect to server
						CreateServer();
						InGame = false;

						// Close the window
						CloseWindow = true;
						Active = false;

						// Message saying it's waiting for other players
						NewMessageToShow("Waiting for other players...");
						MessageTime = MaxMessageTime - 1;
					}
					else if (ColumnCursor == PosClose[0] && RowCursor == PosClose[1]) // Close
					{
						// Close window
						CloseWindow = true;
					}
					else if (Items.get(ColumnCursor).get(RowCursor).getClass().equals(MenuItem_Dialog.class))
					{
						((MenuItem_Dialog)Items.get(ColumnCursor).get(RowCursor)).Action(MenuKeyPressed);

						if (!((MenuItem_Dialog)Items.get(ColumnCursor).get(RowCursor)).FileName().equals(""))
						{
							// Add map to combo box
							((MenuItem_ComboBox)(Items.get(PosComboBox[0]).get(PosComboBox[1]))).AddItem(((MenuItem_Dialog)Items.get(ColumnCursor).get(RowCursor)).FileName());

							// Reset FileName
							((MenuItem_Dialog)Items.get(ColumnCursor).get(RowCursor)).FileName("");
						}
					}
					if (!Items.get(ColumnCursor).get(RowCursor).getClass().equals(MenuItem_RadioButton.class) &&
							!Items.get(ColumnCursor).get(RowCursor).getClass().equals(MenuItem_CheckBox.class) &&
							!Items.get(ColumnCursor).get(RowCursor).getClass().equals(MenuItem_HorSlider.class) &&
							!Items.get(ColumnCursor).get(RowCursor).getClass().equals(MenuItem_Dialog.class) &&
							!Items.get(ColumnCursor).get(RowCursor).getClass().equals(MenuItem.class))
					{
						WindowLock(!WindowLock());
					}
				}
				if (Keyboard.isKeyDown(Keyboard.KEY_F10))
				{
					// Close window
					CloseWindow = true;
					WindowLock(false);
				}
				// Up
				if (Keyboard.isKeyDown(Keyboard.KEY_UP) && !WindowLock())
				{
					// Move cursor up
					CursorUp();
				}

				// Left
				else if (Keyboard.isKeyDown(Keyboard.KEY_LEFT) && !WindowLock())
				{
					// Move cursor left
					CursorLeft();
				}
				// Right
				else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT) && !WindowLock())
				{
					// Move cursor Right
					CursorRight();
				}
				// Down
				else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN) && !WindowLock())
				{
					// Move cursor down
					CursorDown();
				}

				// If we close window, reset cursor and disable lock
				if (CloseWindow)
				{
					ColumnCursor = 0;
					RowCursor = 1;
				}
			}

			return CloseWindow;
		}

		// Change picture box image when radio check change
		private void ChangeImage()
		{
			// Check all radio button
			for (int ItemIndex = PosFirstRadio[1]; ItemIndex <= RadioButtonNumber; ItemIndex++)
			{
				if (((MenuItem_RadioButton)Items.get(PosFirstRadio[0]).get(ItemIndex)).IsChecked())
				{
					((MenuItem_PictureBox)Items.get(PosPictureBox[0]).get(PosPictureBox[1])).TextureIndex(ItemIndex - PosFirstRadio[1]);
				}
			}
		}

		// Create server
		public void CreateServer()
		{
			// Local constant
			final int Gamemode_Free = 0;
			final int Gamemode_One = 1;
			final int Gamemode_Flag = 2;

			// To implement
			//String DemoName = "";
			//if(RecordDemo.Bool())
			//{
			// Prepare to record demo
			//DemoName = ((MenuItem_TextBox)Items.get(PosTextBox[0]).get(PosTextBox[1])).TextInside();
			// Name of demo
			//if(DemoName.equals(""))
			//{
			//	// Make default name
			//}
			//else
			//{
			//	// Use text inside
			//}
			//}

			// Get GameMode, TimeLimit and KillLimit
			Menu.this.GameMode = this.GameMode.Int();
			Menu.this.TimeLimit = this.TimeLimit.Int();
			Menu.this.KillLimit = this.KillLimit.Int();

			// Get Map name
			Map = ((MenuItem_ComboBox)Items.get(PosComboBox[0]).get(PosComboBox[1])).CurrentItem();
			IsServer = true;
		}
	}

	class MenuWindows_JoinGame extends MenuWindows
	{
		// Attribute
		List<List<MenuItem>> Items;

		int RowCursor;
		int ColumnCursor;

		int[] PosConnect;
		int[] PosClose;
		int[] PosTextBox;

		// Constant
		final double TitleHeight = 8.25d; // Smaller = Bigger.
		final Color WindowColor = new Color(160, 160, 160, 255);
		// Constructor
		public MenuWindows_JoinGame(String Title, int Width, int Height)
		{
			// Initialize super attribute
			super(Title, Width, Height);

			// Initialize Items array
			Items = new ArrayList<List<MenuItem>>();

			// Initialize cursor
			RowCursor = 0;
			ColumnCursor = 0;

			// Initialize connect position
			PosConnect = new int[2];
			PosConnect[0] = 0;
			PosConnect[1] = 1;

			// Initialize close position
			PosClose = new int[2];
			PosClose[0] = 1;
			PosClose[1] = 1;

			// Initialize Textbox position
			PosTextBox = new int[2];
			PosTextBox[0] = 0;
			PosTextBox[1] = 0;

			// Initialize first column
			List<MenuItem> Column1 = new ArrayList<MenuItem>();
			Column1.add(new MenuItem_TextBox("Ip", true, false));
			Column1.add(new MenuItem("Connect", true, false));

			// Initialize second column
			List<MenuItem> Column2 = new ArrayList<MenuItem>();
			Column2.add(new MenuItem("", false, false));
			Column2.add(new MenuItem("Close", true, false));

			// Add column to item
			Items.add(Column1);
			Items.add(Column2);
		}

		// Set value of ColumnCursor
		// Move the cursor from 1 to the right
		private void CursorRight()
		{
			// Check if cursor can move to right
			if (ColumnCursor < Items.size() - 1)
			{
				ColumnCursor++;
				// Check if SubMenuCursor to big to fit next column
				if (RowCursor >= Items.get(ColumnCursor).size() - 1)
				{
					RowCursor = Items.get(ColumnCursor).size() - 1;
				}
				// Check if Item is not enable
				while (RowCursor < Items.get(ColumnCursor).size() && !Items.get(ColumnCursor).get(RowCursor).Enabled())
				{
					RowCursor++;
				}

				// Adjust Row if out of range
				if (RowCursor >= Items.get(ColumnCursor).size())
				{
					RowCursor = Items.get(ColumnCursor).size() - 1;
				}

				// If not item enable down, go up
				while (RowCursor >= 0 && !Items.get(ColumnCursor).get(RowCursor).Enabled())
				{
					RowCursor--;
				}
			}
		}

		// Move cursor from 1 to the left
		public void CursorLeft()
		{
			// Check if cursor can move to left
			if (ColumnCursor > 0)
			{
				ColumnCursor--;
				// Check if SubMenuCursor to big to fit next column
				if (RowCursor >= Items.get(ColumnCursor).size() - 1)
				{
					RowCursor = Items.get(ColumnCursor).size() - 1;
				}
				// Check if Item is not enable
				while (RowCursor >= 0 && !Items.get(ColumnCursor).get(RowCursor).Enabled())
				{
					RowCursor--;
				}

				// Adjust Row if out of range
				if (RowCursor < 0)
				{
					RowCursor = 0;
				}

				// If not item enable up, go down
				while (RowCursor < Items.get(ColumnCursor).size() && !Items.get(ColumnCursor).get(RowCursor).Enabled())
				{
					RowCursor++;
				}
			}
		}

		// Move cursor down from 1
		public void CursorDown()
		{
			// Check if cursor can move down
			if (RowCursor < Items.get(ColumnCursor).size() - 1)
			{
				RowCursor++;
			}
			// Check if Item is not enable
			while (RowCursor < Items.get(ColumnCursor).size() && !Items.get(ColumnCursor).get(RowCursor).Enabled())
			{
				RowCursor++;
			}

			// Adjust Row if out of range
			if (RowCursor >= Items.get(ColumnCursor).size())
			{
				RowCursor = Items.get(ColumnCursor).size() - 1;
			}

			// If not item enable down, go up
			while (RowCursor >= 0 && !Items.get(ColumnCursor).get(RowCursor).Enabled())
			{
				RowCursor--;
			}
		}

		// Move cursor up from 1
		public void CursorUp()
		{
			// Check if cursor can move up
			if (RowCursor > 0)
			{
				RowCursor--;
			}
			// Check if Item is not enable
			while (RowCursor >= 0 && !Items.get(ColumnCursor).get(RowCursor).Enabled())
			{
				RowCursor--;
			}

			// Adjust Row if out of range
			if (RowCursor < 0)
			{
				RowCursor = 0;
			}

			// If not item enable up, go down
			while (RowCursor < Items.get(ColumnCursor).size() && !Items.get(ColumnCursor).get(RowCursor).Enabled())
			{
				RowCursor++;
			}
		}

		// Draw window on the screen
		public void Draw(int GridWidth,int GridHeigth)
		{
			// Disable Depth Test so that the border of the Window is writen over background. (The MenuBar and Alpha Rectangle)
			glDisable(GL_DEPTH_TEST);

			// Set draw mode to fill
			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

			// Use center to find border of windows
			double BottomLeftXGL = ConvertPxToGL(((float)GridWidth / 2.0f) - ConvertPercentToPx(WindowsWidth() / 2.0f, true, GridWidth, GridHeight),true,GridWidth,GridHeight,true);
			double BottomLeftYGL = ConvertPxToGL(((float)GridHeight / 2.0f) - ConvertPercentToPx(WindowsHeight() / 2.0f, false, GridWidth, GridHeight),false,GridWidth,GridHeight,true);
			double TopRightXGL = ConvertPxToGL(((float)GridWidth / 2.0f) + ConvertPercentToPx(WindowsWidth() / 2.0f, true, GridWidth, GridHeight),true,GridWidth,GridHeight,true);
			double TopRightYGL = ConvertPxToGL(((float)GridHeight / 2.0f) + ConvertPercentToPx(WindowsHeight() / 2.0f, false, GridWidth, GridHeight),false,GridWidth,GridHeight,true);
			double MargeX = ConvertPxToGL(BorderX, true, GridWidth, GridHeight,false);
			double MargeY = ConvertPxToGL(BorderY, false, GridWidth, GridHeight,false);

			// Move cursor to bottom of window
			glTranslatef(0, -2, 0);

			// Draw border of window
			glColor4f(0.0f, 0.0f, 0.0f, 1.0f);

			glRectd(BottomLeftXGL - MargeX, BottomLeftYGL - MargeY, TopRightXGL + MargeX, TopRightYGL + MargeY);

			// Draw window
			glColor4f(WindowColor.getRed() / 255.0f, WindowColor.getGreen() / 255.0f, WindowColor.getBlue() / 255.0f, WindowColor.getAlpha() / 255.0f);
			glRectd(BottomLeftXGL, BottomLeftYGL, TopRightXGL, TopRightYGL);

			glDisable(GL_DEPTH_TEST);

			// Draw Title
			double TitleBottomYGL = ConvertPxToGL(((float) GridHeight / 2.0f) + (ConvertPercentToPx(WindowsHeight() / 2.0f, false, GridWidth, GridHeight)) + ConvertPercentToPx(TitleHeight, false, GridWidth, GridHeight),false,GridWidth,GridHeight,false);

			glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
			glRectd(BottomLeftXGL, TitleBottomYGL, TopRightXGL, TopRightYGL);

			// Draw Text of Title
			String Text = TitleText();
			// Convert % to GL
			double MarginGL = ConvertPercentToGL(MarginPercent, true, GridWidth, GridHeight, false);

			glColor4d(255, 255, 255, 255);

			// Initialize X coordonate
			double StartTextXGL = ConvertPxToGL(((float)GridWidth / 2.0f) - (ConvertPercentToPx(WindowsWidth() / 2.0f, true, GridWidth, GridHeight)),true,GridWidth,GridHeight,true);

			// Loop that check each letter of Title and load corresponding image and then draw it
			for (int Letter = 0; Letter < Text.length(); Letter++)
			{
				if (Text.charAt(Letter) == ' ')
				{
					// Adjusting next PosX for the next image
					StartTextXGL = StartTextXGL + MarginGL * SpaceSize;
				}
				else
				{
					glEnable(GL_TEXTURE_2D);

					// Get int value of char
					int CharValue = (String.valueOf(Text.charAt(Letter)).toUpperCase()).charAt(0);
					int TextureIndex = 0;
					// Check if number
					if (CharValue >= Char0Index && CharValue <= Char9Index)
					{
						// Initialize Texture Index in font List
						TextureIndex = CharValue - Char0Index;
					}
					// Check if letter
					else if (CharValue >= CharAIndex && CharValue <= CharZIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = CharValue - CharAIndex + 10;
					}
					// Special character
					else if (CharValue == ChaPeriodIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = 36;
					}
					else if (CharValue == CharColonIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = 37;
					}

					// Initialize and Bind Texture
					FontArray.get(TextureIndex).Bind();

					// Converting image height and width
					double ImgWidthGL = ConvertPercentToGL(TitleFontWidth, true, GridWidth, GridHeight, false);
					double ImgHeightGL = ConvertPercentToGL(TitleFontHeight, false, GridWidth, GridHeight, false);

					glDisable(GL_DEPTH_TEST);
					// Drawing image
					glBegin(GL_QUADS);
					glTexCoord2d(0.0d, 1.0d);
					glVertex2d(StartTextXGL, TitleBottomYGL + MarginGL);
					glTexCoord2d(1.0d, 1.0d);
					glVertex2d(StartTextXGL + ImgWidthGL, TitleBottomYGL + MarginGL);
					glTexCoord2d(1.0d, 0.0d);
					glVertex2d(StartTextXGL + ImgWidthGL, TitleBottomYGL + MarginGL + ImgHeightGL);
					glTexCoord2d(0.0d, 0.0d);
					glVertex2d(StartTextXGL, TitleBottomYGL + ImgHeightGL + MarginGL);
					glEnd();
					// Adjusting next PosX for the next image
					StartTextXGL = StartTextXGL + ImgWidthGL + MarginGL;

					glDisable(GL_TEXTURE_2D);
				}
			}

			double MarginPixels = ConvertPercentToPx(MarginPercent, true, GridWidth, GridHeight);
			double PosX = (((float)GridWidth / 2.0f) - ConvertPercentToPx(WindowsWidth() / 2.0f, true, GridWidth, GridHeight)) + BorderX + MarginPixels;
			double PosY = ((float) GridHeight / 2.0f) + (ConvertPercentToPx(WindowsHeight() / 2.0f, false, GridWidth, GridHeight)) - ConvertPercentToPx(TitleHeight, false, GridWidth, GridHeight);
			double BiggestItemWidth = 0;

			glDisable(GL_DEPTH_TEST);
			// Draw all control on a grid system (like menu)
			for (int ItemArray = 0; ItemArray < Items.size(); ItemArray++)
			{
				for (int Item = 0;Item < Items.get(ItemArray).size();Item++)
				{

					// Check if Cursor is on this item
					if (ItemArray == ColumnCursor && Item == RowCursor)
					{
						// Draw item
						Items.get(ItemArray).get(Item).Draw(PosX, PosY, GridWidth, GridHeight, true, true, true);
					}
					else
					{
						// Draw item
						Items.get(ItemArray).get(Item).Draw(PosX, PosY, GridWidth, GridHeight, false, false, true);
					}
					// Adjust next Y
					PosY = PosY - ConvertPercentToPx(RowHeight, false, GridWidth, GridHeight);

					// Check if occupied width of the drawn items is bigger that the previous
					if (Items.get(ItemArray).get(Item).Occupied_Width() > BiggestItemWidth && !Items.get(ItemArray).get(Item).getClass().equals(Menu.MenuItem_ComboBox.class) && !Items.get(ItemArray).get(Item).getClass().equals(Menu.MenuItem_TextBox.class))
					{
						BiggestItemWidth = Items.get(ItemArray).get(Item).Occupied_Width();
					}

					// Reset occupied width to 0
					Items.get(ItemArray).get(Item).Occupied_Width(0);
				}
				PosY = ((float) GridHeight / 2.0f) + (ConvertPercentToPx(WindowsHeight() / 2.0f, false, GridWidth, GridHeight)) - ConvertPercentToPx(TitleHeight, false, GridWidth, GridHeight);
				PosX = (((float)GridWidth / 2.0f) - ConvertPercentToPx(WindowsWidth() / 2.0f, true, GridWidth, GridHeight)) + BorderX + BiggestItemWidth + MarginPixels * 10;
			}

			// Move to the top of window
			glTranslatef(0, 2, 0);
		}

		// Change window state with input. Return true if must be close
		public boolean Action(boolean MenuKeyPressed)
		{
			// Declare local boolean
			boolean CloseWindow = false;

			// If lock, send action to actual item
			if (WindowLock())
			{
				Items.get(ColumnCursor).get(RowCursor).Action(MenuKeyPressed);
			}
			if (!MenuKeyPressed)
			{
				// Enter
				if (Keyboard.isKeyDown(Keyboard.KEY_RETURN))
				{
					if (ColumnCursor == PosConnect[0] && RowCursor == PosConnect[1])
					{
						if (!InGame)
						{
							// Quit the game first
							QuitGame();

							// Connect to server
							ConnectToServer();
							InGame = false;
						}
						else
						{
							NewMessageToShow("Quit this game first.");
						}

						// Close window
						CloseWindow = true;
						Active = false;
					}
					else if (ColumnCursor == PosClose[0] && RowCursor == PosClose[1])
					{
						// Close window
						CloseWindow = true;
					}
					if (Items.get(ColumnCursor).get(RowCursor).getClass().equals(MenuItem_TextBox.class))
					{
						WindowLock(!WindowLock());
					}
				}
				if (Keyboard.isKeyDown(Keyboard.KEY_F10))
				{
					// Close window
					CloseWindow = true;
					WindowLock(false);
				}
				// Up
				if (Keyboard.isKeyDown(Keyboard.KEY_UP) && !WindowLock())
				{
					// Move cursor up
					CursorUp();
				}

				// Left
				else if (Keyboard.isKeyDown(Keyboard.KEY_LEFT) && !WindowLock())
				{
					// Move cursor left
					CursorLeft();
				}
				// Right
				else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT) && !WindowLock())
				{
					// Move cursor Right
					CursorRight();
				}
				// Down
				else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN) && !WindowLock())
				{
					// Move cursor down
					CursorDown();
				}

				// If we close window, reset cursor and disable lock
				if (CloseWindow)
				{
					ColumnCursor = 0;
					RowCursor = 1;
				}
			}

			// Return local boolean
			return CloseWindow;
		}

		// Try to connect to ip
		public void ConnectToServer()
		{
			// Try to connect to address
			Address = ((MenuItem_TextBox)Items.get(PosTextBox[0]).get(PosTextBox[1])).TextInside();
			IsClient = true;
		}
	}

	class MenuWindow_About extends MenuWindows
	{
		// Attribute
		List<List<MenuItem>> Items;

		// Constant
		final double TitleHeight = 10.25d; // Smaller = Bigger.
		final Color WindowColor = new Color(160,160,160,255);
		// Constructor
		public MenuWindow_About(String Title, double Width,double Height)
		{
			// Initialize super attribute
			super(Title, Width, Height);

			// Add text item to window
			Items = new ArrayList<List<MenuItem>>();

			// Column 1
			List<MenuItem> Column1 = new ArrayList<MenuItem>();
			Column1.add(new MenuItem("Developer:", false, true));
			Column1.add(new MenuItem("", false, true)); // Spacer
			Column1.add(new MenuItem("", false, true)); // Spacer
			Column1.add(new MenuItem("Version:", false, true));
			Column1.add(new MenuItem("Last Update:", false, true));
			Column1.add(new MenuItem("", false, true)); // Spacer

			// Column 2
			List<MenuItem> Column2 = new ArrayList<MenuItem>();
			Column2.add(new MenuItem("Alexandre-Xavier Labonte-Lamoureux", false, true));
			Column2.add(new MenuItem("Francis Bourgault", false, true)); // Spacer
			Column2.add(new MenuItem("Andy Sergerie", false, true)); // Spacer
			Column2.add(new MenuItem(GameVersion, false, true));
			Column2.add(new MenuItem(LastUpdate, false, true));
			Column2.add(new MenuItem("Close", true, true));

			Items.add(Column1);
			Items.add(Column2);
		}

		// Draw window
		public void Draw(int GridWidth,int GridHeight)
		{
			// Disable Depth Test so that the border of the Window is writen over background. (The MenuBar and Alpha Rectangle)
			glDisable(GL_DEPTH_TEST);

			// Set draw mode to fill
			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

			// Use center to find border of windows
			double BottomLeftXGL = ConvertPxToGL(((float)GridWidth / 2.0f) - ConvertPercentToPx(WindowsWidth() / 2.0f, true, GridWidth, GridHeight), true,GridWidth,GridHeight, true);
			double BottomLeftYGL = ConvertPxToGL(((float)GridHeight / 2.0f) - ConvertPercentToPx(WindowsHeight() / 2.0f, false, GridWidth, GridHeight), false,GridWidth,GridHeight, true);
			double TopRightXGL = ConvertPxToGL(((float)GridWidth / 2.0f) + ConvertPercentToPx(WindowsWidth() / 2.0f, true, GridWidth, GridHeight), true,GridWidth,GridHeight, true);
			double TopRightYGL = ConvertPxToGL(((float)GridHeight / 2.0f) + ConvertPercentToPx(WindowsHeight() / 2.0f, false, GridWidth, GridHeight), false,GridWidth,GridHeight, true);
			double MargeX = ConvertPxToGL(BorderX, true, GridWidth, GridHeight, false);
			double MargeY = ConvertPxToGL(BorderY, false, GridWidth, GridHeight, false);

			// Move cursor to bottom of window
			glTranslatef(0, -2, 0);

			// Draw border of window
			glColor4f(0.0f, 0.0f, 0.0f, 1.0f);

			glRectd(BottomLeftXGL - MargeX, BottomLeftYGL - MargeY, TopRightXGL + MargeX, TopRightYGL + MargeY);

			// Draw window
			glColor4f(WindowColor.getRed() / 255.0f, WindowColor.getGreen() / 255.0f, WindowColor.getBlue() / 255.0f, WindowColor.getAlpha() / 255.0f);
			glRectd(BottomLeftXGL, BottomLeftYGL, TopRightXGL, TopRightYGL);

			glDisable(GL_DEPTH_TEST);

			// Draw Title
			double TitleBottomYGL = ConvertPxToGL(((float) GridHeight / 2.0f) + (ConvertPercentToPx(WindowsHeight() / 2.0f, false, GridWidth, GridHeight)) + ConvertPercentToPx(TitleHeight, false, GridWidth, GridHeight),false,GridWidth,GridHeight,false);

			glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
			glRectd(BottomLeftXGL, TitleBottomYGL, TopRightXGL, TopRightYGL);

			// Draw Text of Title
			String Text = TitleText();
			// Convert % to GL
			double MarginGL = ConvertPercentToGL(MarginPercent, true, GridWidth, GridHeight, false);

			glColor4d(255, 255, 255, 255);

			// Initialize X coordonate
			double StartTextXGL = ConvertPxToGL(((float)GridWidth / 2.0f) - (ConvertPercentToPx(WindowsWidth() / 2.0f, true, GridWidth, GridHeight)),true,GridWidth,GridHeight,true);

			// Loop that check each letter of Title and load corresponding image and then draw it
			for (int Letter = 0; Letter < Text.length(); Letter++)
			{
				if (Text.charAt(Letter) == ' ')
				{
					// Adjusting next PosX for the next image
					StartTextXGL = StartTextXGL + MarginGL * SpaceSize;
				}
				else
				{
					glEnable(GL_TEXTURE_2D);

					// Get int value of char
					int CharValue = (String.valueOf(Text.charAt(Letter)).toUpperCase()).charAt(0);
					int TextureIndex = 0;
					// Check if number
					if (CharValue >= Char0Index && CharValue <= Char9Index)
					{
						// Initialize Texture Index in font List
						TextureIndex = CharValue - Char0Index;
					}
					// Check if letter
					else if (CharValue >= CharAIndex && CharValue <= CharZIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = CharValue - CharAIndex + 10;
					}
					// Special character
					else if (CharValue == ChaPeriodIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = 36;
					}
					else if (CharValue == CharColonIndex)
					{
						// Initialize Texture Index in font List
						TextureIndex = 37;
					}

					// Initialize and Bind Texture
					FontArray.get(TextureIndex).Bind();

					// Converting image height and width
					double ImgWidthGL = ConvertPercentToGL(TitleFontWidth, true, GridWidth, GridHeight, false);
					double ImgHeightGL = ConvertPercentToGL(TitleFontHeight, false, GridWidth, GridHeight, false);

					glDisable(GL_DEPTH_TEST);
					// Drawing image
					glBegin(GL_QUADS);
					glTexCoord2d(0.0d, 1.0d);
					glVertex2d(StartTextXGL, TitleBottomYGL + MarginGL);
					glTexCoord2d(1.0d, 1.0d);
					glVertex2d(StartTextXGL + ImgWidthGL, TitleBottomYGL + MarginGL);
					glTexCoord2d(1.0d, 0.0d);
					glVertex2d(StartTextXGL + ImgWidthGL, TitleBottomYGL + MarginGL + ImgHeightGL);
					glTexCoord2d(0.0d, 0.0d);
					glVertex2d(StartTextXGL, TitleBottomYGL + ImgHeightGL + MarginGL);
					glEnd();
					// Adjusting next PosX for the next image
					StartTextXGL = StartTextXGL + ImgWidthGL + MarginGL;

					glDisable(GL_TEXTURE_2D);
				}
			}

			double MarginPixels = ConvertPercentToPx(MarginPercent, true, GridWidth, GridHeight);
			double PosX = (((float)GridWidth / 2.0f) - ConvertPercentToPx(WindowsWidth() / 2.0f, true, GridWidth, GridHeight)) + BorderX + MarginPixels;
			double PosY = ((float) GridHeight / 2.0f) + (ConvertPercentToPx(WindowsHeight() / 2.0f, false, GridWidth, GridHeight)) - ConvertPercentToPx(TitleHeight, false, GridWidth, GridHeight);
			double BiggestItemWidth = 0;

			glDisable(GL_DEPTH_TEST);
			// Draw all control on a grid system (like menu)
			for (int ItemArray = 0; ItemArray < Items.size(); ItemArray++)
			{
				for (int Item = 0;Item < Items.get(ItemArray).size();Item++)
				{
					// Check if item is close 'button'
					if (Items.get(ItemArray).get(Item).ItemText().equals("Close"))
					{
						// Draw item
						Items.get(ItemArray).get(Item).Draw(PosX, PosY, GridWidth, GridHeight, true, true, true);
					}
					else
					{
						// Draw item
						Items.get(ItemArray).get(Item).Draw(PosX, PosY, GridWidth, GridHeight, false, false, true);
					}

					// Adjust next Y
					PosY = PosY - ConvertPercentToPx(RowHeight, false, GridWidth, GridHeight);

					// Check if occupied width of the drawn items is bigger that the previous
					if (Items.get(ItemArray).get(Item).Occupied_Width() > BiggestItemWidth && !Items.get(ItemArray).get(Item).getClass().equals(Menu.MenuItem_ComboBox.class) && !Items.get(ItemArray).get(Item).getClass().equals(Menu.MenuItem_TextBox.class))
					{
						BiggestItemWidth = Items.get(ItemArray).get(Item).Occupied_Width();
					}

					// Reset occupied width to 0
					Items.get(ItemArray).get(Item).Occupied_Width(0);
				}
				PosY = ((float) GridHeight / 2.0f) + (ConvertPercentToPx(WindowsHeight() / 2.0f, false, GridWidth, GridHeight)) - ConvertPercentToPx(TitleHeight, false, GridWidth, GridHeight);
				PosX = (((float)GridWidth / 2.0f) - ConvertPercentToPx(WindowsWidth() / 2.0f, true, GridWidth, GridHeight)) + BorderX + BiggestItemWidth + MarginPixels * 10;
			}

			// Move to the top of window
			glTranslatef(0, 2, 0);

		}

		// Close window if enter is pressed
		public boolean Action(boolean MenuKeyPressed)
		{
			// Initialize local bool
			boolean CloseWindow = false;

			if ((Keyboard.isKeyDown(Keyboard.KEY_RETURN) || Keyboard.isKeyDown(Keyboard.KEY_F10)) && !MenuKeyPressed)
			{
				CloseWindow = true;
			}

			return CloseWindow;
		}
	}

	class MenuItem_Dialog extends MenuItem
	{
		// Attribute
		JFileChooser Dialog;
		String DialogFilter;
		String DialogTitle;
		String FileTypeName;
		String FileName;

		// Set value of DialogTitle
		private void DialogTitle(String Title)
		{
			DialogTitle = Title;
		}

		// Set value of DialogFilter
		private void DialogFilter(String Filter)
		{
			DialogFilter = Filter;
		}

		// Set value of FileTypeName
		private void FileTypeName(String FileName)
		{
			FileTypeName = FileName;
		}

		// Set value of FileName
		private void FileName(String Name)
		{
			FileName = Name;
		}

		// Get value of FileName
		public String FileName()
		{
			return  FileName;
		}

		// Constructor
		public MenuItem_Dialog(String Text,boolean Enable,boolean KeepActiveColor,String Title,String Filter,String FileType)
		{
			// Initialize super attribute
			super(Text, Enable, KeepActiveColor);

			// Initialize attribute
			DialogTitle(Title);
			DialogFilter(Filter);
			FileTypeName(FileType);
			FileName("");
		}

		public void Action(boolean MenuKeyPressed)
		{
			if (Keyboard.isKeyDown(Keyboard.KEY_RETURN) && !MenuKeyPressed)
			{
				// Initialize dialog
				Dialog = new JFileChooser();
				Dialog.setDialogTitle(DialogTitle);
				Dialog.setAcceptAllFileFilterUsed(true);
				Dialog.setFileFilter(new FileNameExtensionFilter(FileTypeName,DialogFilter));
				// Open dialog
				int Choice = Dialog.showOpenDialog(null);
				// Check if yes selected
				if (Choice == JFileChooser.APPROVE_OPTION)
				{
					// Save file into map folder
					File ChosenFile = Dialog.getSelectedFile();

					// Put fileName in attribute
					FileName = ChosenFile.getName().substring(0, ChosenFile.getName().indexOf("."));
				}
				else
				{
					FileName = "";
				}
			}
		}
	}

	// Attribute for control
	// Checkbox
	Menu_Boolean Fullscreen; // Define if full screen or window
	Menu_Boolean FreeLook; // Define if the use of free look is used
	Menu_Boolean ShowMessage; // Define if message from chat are shown
	Menu_Boolean ShowHud; // Define if Hud is draw
	Menu_Boolean ShowDebug; // Define if debug parameter are shown
	Menu_Boolean GrabMouse; // Define if the game take mouse input or ignore them
	Menu_Boolean EnableChat; // Define if the chat is active
	Menu_Boolean Filtering; // Define if filtering is enable
	// Radio
	Menu_Integer SoundMode; // Define sound mode
	// Slider
	Menu_Integer SFXVolume; // Define sound volume
	Menu_Integer MouseSensitivity; // Define mouse sensitivity
	// NumericUpDown
	Menu_Integer ViewDepth; // Define Depth of view

	// Attribute for menu
	List<List<MenuItem>> Items;

	private boolean Active = false;		// Show or hide the menu
	private int MenuBarCursor = 0; // Cursor of menu bar
	private int SubMenuCursor = 0; // Sub menu cursor
	private boolean Locked; // Define whether the cursor can move or not
	public List<Texture> FontArray; // Font for the menu
	public List<Texture> SpecialImageArray; // Image for gamemode and other special image
	private boolean ExitWindow = false; // Use when exiting window to ignore lock

	int[] PosQuit; // Define where the MenuItem with QuitGame function is
	int[] PosExit; // Define where the MenuItem with Exit function is

	// Constant
	private final double RowHeight = 6.25d; // Number of px per row of MenuItem
	private final Color BarColor = new Color(0.5f, 0.0f, 0.0f, 1.0f);
	private final Color AlphaRectColor = new Color(0.0f, 0.0f, 0.3f, 0.5f);
	private final float BigMargin = 3.5f; // Multiply normal margin by this number for a bigger margin
	private static final double GlWinWidth = 2.73f; // Value of the windows width with GL unit (auto adjust to windows size)
	private static final double GlWinHeight = 1.66f; // Value of the windows height with GL unit (auto adjust to windows size)

	// Char constant
	final int Char0Index = 48;
	final int Char9Index = 57;
	final int CharAIndex = 65;
	final int CharZIndex = 90;
	final int ChaPeriodIndex = 46;
	final int CharColonIndex = 58;
	final int CharUnderScoreIndex = 95;
	final int CharApostropheIndex = 39;
	final int CharAsteriskIndex = 42;
	final int CharLeftParaIndex = 40;
	final int CharRightParaIndex = 41;
	final int CharPlusIndex = 43;
	final int CharMinusIndex = 45;
	final int CharNumberIndex = 35;
	final int CharExclamationIndex = 33;
	final int CharCommaIndex = 44;
	final int CharCommercialAIndex = 64;
	final int CharLeftSquareIndex = 91;
	final int CharRightSquareIndex = 93;
	final int CharLeftBraceIndex = 123;
	final int CharRightBraceIndex = 125;
	final int CharQuestionIndex = 63;
	final int CharPercentIndex = 37;

	// Screen coordinates are represented as a fixed resolution
	int GridWidth = 640;
	int GridHeight = 480;

	// Constructor
	public Menu()
	{
		// Initialize attribute for control
		Fullscreen = new Menu_Boolean();
		Fullscreen.Bool(true);

		FreeLook = new Menu_Boolean();
		FreeLook.Bool(true);

		ShowMessage = new Menu_Boolean();
		ShowMessage.Bool(true);

		ShowHud = new Menu_Boolean();
		ShowHud.Bool(true);

		ShowDebug = new Menu_Boolean();
		ShowDebug.Bool(true);

		GrabMouse = new Menu_Boolean();
		GrabMouse.Bool(false);

		EnableChat = new Menu_Boolean();
		EnableChat.Bool(true);

		Filtering = new Menu_Boolean();
		Filtering.Bool(true);

		SoundMode = new Menu_Integer();
		SoundMode.Int(0);

		SFXVolume = new Menu_Integer();
		SFXVolume.Int(60);

		MouseSensitivity = new Menu_Integer();
		MouseSensitivity.Int(60);

		ViewDepth = new Menu_Integer();
		ViewDepth.Int(100);

		// Loading all Font Texture
		FontArray = new ArrayList<Texture>();
		LoadFont("res/smallchars", FontArray);

		SpecialImageArray = new ArrayList<Texture>();
		Texture GameModeFree = new Texture("res/sprites/FreeForAll.png", GL_NEAREST);
		Texture GameModeOne = new Texture("res/sprites/OneHitKill.png", GL_NEAREST);
		Texture GameModeFlag = new Texture("res/sprites/FlagTag.png", GL_NEAREST);
		Texture ImageSelect = new Texture("res/sprites/Select.png", GL_NEAREST);
		SpecialImageArray.add(GameModeFree);
		SpecialImageArray.add(GameModeOne);
		SpecialImageArray.add(GameModeFlag);
		SpecialImageArray.add(ImageSelect);

		// Initialize Cursor
		MenuBarCursor = 0;

		// Initialize position of Quit and Exit
		PosQuit = new int[2];
		PosQuit[0] = 0;
		PosQuit[1] = 3;

		PosExit = new int[2];
		PosExit[0] = 0;
		PosExit[1] = 5;

		// Initialize array of array
		Items = new ArrayList<List<MenuItem>>();

		// Game array
		List<MenuItem> Game = new ArrayList<MenuItem>();
		Game.add(new MenuItem("Game", true, false));
		MenuWindows_NewGame GameWin = new MenuWindows_NewGame("New Game", 55, 67);
		Game.add(new MenuItem_Windows("New Game", true, false,GameWin));
		MenuWindows_JoinGame JoinWin = new MenuWindows_JoinGame("Join Game", 50, 17);
		Game.add(new MenuItem_Windows("Join Game", true, false, JoinWin));
		Game.add(new MenuItem("Quit Game", true, false));
		// Separator
		//Game.add(new MenuItem_Dialog("Play Demo", true,false,"Choose a Demo","txt","Text File"));
		MenuWindow_About AboutWin = new MenuWindow_About("About", 85, 45);
		Game.add(new MenuItem_Windows("About", true, false, AboutWin));
		Game.add(new MenuItem("Exit", true, false));

		// Option Array
		List<MenuItem> Option = new ArrayList<MenuItem>();
		Option.add(new MenuItem("Option", true, false));
		Option.add(new MenuItem_CheckBox("Use freelook", true, false, FreeLook));
		Option.add(new MenuItem_CheckBox("Show messages", true, false, ShowMessage));
		Option.add(new MenuItem_CheckBox("Show HUD", true, false, ShowHud));
		// Seperator
		Option.add(new MenuItem_CheckBox("Show Debug", true, false, ShowDebug));

		// Control Array
		List<MenuItem> Control = new ArrayList<MenuItem>();
		Control.add(new MenuItem("Control", true, false));
		Control.add(new MenuItem_CheckBox("Grab mouse", true, false, GrabMouse));
		Control.add(new MenuItem_CheckBox("Enable chat", true, false, EnableChat));
		Control.add(new MenuItem_HorSlider("Mouse Sensitivity", true ,false, 0, 100, MouseSensitivity));

		// Sound Array
		List<MenuItem> Sound = new ArrayList<MenuItem>();
		Sound.add(new MenuItem("Sound", true, false));
		Sound.add(new MenuItem_HorSlider("SFX Volume", true, false, 0, 100, SFXVolume));
		Sound.add(new MenuItem("Mode ",false, true));
		String[] RadioText = new String[3];
		RadioText[0] = "2D";
		RadioText[1] = "3D";
		RadioText[2] = "3D Doppler Effect";
		MenuItem_RadioButtonGroup RadioGroup = new MenuItem_RadioButtonGroup(RadioText,SoundMode);

		for (int Radio = 0; Radio < RadioGroup.RadioButtons.length; Radio++)
		{
			Sound.add(RadioGroup.RadioButtons[Radio]);
		}

		// Video Array
		List<MenuItem> Video = new ArrayList<MenuItem>();
		Video.add(new MenuItem("Video", true,false));
		Video.add(new MenuItem_CheckBox("Fullscreen", true, false,Fullscreen));
		Video.add(new MenuItem_CheckBox("Enable Filtering", true, false,Filtering));
		Video.add(new MenuItem_NumberBox("View depth",true,false,ViewDepth,5,100,5));

		// Adding array to menu
		Items.add(Game);
		Items.add(Option);
		Items.add(Control);
		Items.add(Sound);
		Items.add(Video);
	}

	// Load image file into array at startup to avoid having to load font each time we need them
	private void LoadFont(String Folder, List<Texture> FontList)
	{
		// Loading number
		for (int Number = Char0Index; Number <= Char9Index; Number++)
		{
			// Initialize letter
			char FileName = (char)Number;
			// Load Texture
			Texture Texture = new Texture((Folder + "/" + FileName + ".png"),GL_NEAREST);
			// Put Texture into Array
			FontList.add(Texture);
		}

		// Loading letter
		for (int Letter = CharAIndex; Letter <= CharZIndex; Letter++)
		{
			// Initialize letter
			char FileName = (char)Letter;
			// Load Texture
			Texture Texture = new Texture((Folder + "/" + FileName + ".png"),GL_NEAREST);
			// Put Texture into Array
			FontList.add(Texture);
		}

		// Loading punctuation
		Texture TexturePeriod = new Texture((Folder + "/" + "PERIOD" + ".png"),GL_NEAREST);
		Texture TextureColon = new Texture((Folder + "/" + "COLON" + ".png"),GL_NEAREST);
		Texture TextureUnderScore = new Texture((Folder + "/" + "UNDERSCORE" + ".png"),GL_NEAREST);
		Texture TextureApostrophe = new Texture((Folder + "/" + "APOSTROPHE" + ".png"),GL_NEAREST);
		Texture TextureAsterisk = new Texture((Folder + "/" + "ASTERISK" + ".png"),GL_NEAREST);
		Texture TextureLeftPara = new Texture((Folder + "/" + "LEFTPARENTHESIS" + ".png"),GL_NEAREST);
		Texture TextureRightPara = new Texture((Folder + "/" + "RIGHTPARENTHESIS" + ".png"),GL_NEAREST);
		Texture TexturePlus = new Texture((Folder + "/" + "PLUS" + ".png"),GL_NEAREST);
		Texture TextureMinus = new Texture((Folder + "/" + "MINUS" + ".png"),GL_NEAREST);
		Texture TextureNumber = new Texture((Folder + "/" + "NUMBERSIGN" + ".png"),GL_NEAREST);
		Texture TextureExclamation = new Texture((Folder + "/" + "EXCLAMATIONMARK" + ".png"),GL_NEAREST);
		Texture TexturePercent = new Texture((Folder + "/" + "PERCENT" + ".png"),GL_NEAREST);
		Texture TextureQuestion = new Texture((Folder + "/" + "QUESTIONMARK" + ".png"),GL_NEAREST);

		FontList.add(TexturePeriod);
		FontList.add(TextureColon);
		FontList.add(TextureUnderScore);
		FontList.add(TextureApostrophe);
		FontList.add(TextureAsterisk);
		FontList.add(TextureLeftPara);
		FontList.add(TextureRightPara);
		FontList.add(TexturePlus);
		FontList.add(TextureMinus);
		FontList.add(TextureNumber);
		FontList.add(TextureExclamation);
		FontList.add(TexturePercent);
		FontList.add(TextureQuestion);
	}

	// Set locked value
	private void Locked(boolean Lock)
	{
		Locked = Lock;
	}

	// Function for attribute of control

	// Get Fullscreen value
	public boolean Fullscreen()
	{
		return Fullscreen.Bool();
	}

	// Get FreeLook value
	public boolean FreeLook()
	{
		return FreeLook.Bool();
	}

	// Get ShowMessage value
	public boolean ShowMessage()
	{
		return ShowMessage.Bool();
	}

	// Get ShowDebug value
	public boolean ShowDebug()
	{
		return ShowDebug.Bool();
	}

	// Get GrabMouse value
	public boolean GrabMouse()
	{
		return GrabMouse.Bool();
	}

	// Get EnableChat value
	public boolean EnableChat()
	{
		return EnableChat.Bool();
	}

	// Get Filtering value
	public boolean Filtering()
	{
		return Filtering.Bool();
	}

	// Set Fullscreen value
	public void Fullscreen(boolean Value)
	{
		Fullscreen.Bool(Value);
	}

	// Set FreeLook value
	public void FreeLook(boolean Value)
	{
		FreeLook.Bool(Value);
	}

	// Set ShowMessage value
	public void ShowMessage(boolean Value)
	{
		ShowMessage.Bool(Value);
	}

	// Set ShowDebug value
	public void ShowDebug(boolean Value)
	{
		ShowDebug.Bool(Value);
	}

	// Set GrabMouse value
	public void GrabMouse(boolean Value)
	{
		GrabMouse.Bool(Value);
	}

	// Set EnableChat value
	public void EnableChat(boolean Value)
	{
		EnableChat.Bool(Value);
	}

	// Set Filtering value
	public void Filtering(boolean Value)
	{
		Filtering.Bool(Value);
	}

	// Set SoundMode value
	public void SoundMode(int Mode)
	{
		// 0 = 2d
		// 1 = 3d
		// 2 = 3d + Doppler effect
		SoundMode.Int(Mode);
	}

	// Set SFXVolume value
	public void SFXVolume(int Volume)
	{
		if (Volume >= 0 && Volume <= 5)
		{
			SFXVolume.Int(Volume);
		}
	}

	// Set MouseSensibility value
	public void MouseSensibility(int Sensibility)
	{
		if (Sensibility >= 0 && Sensibility <= 5)
		{
			MouseSensitivity.Int(Sensibility);
		}
	}

	// Set ViewDepth value
	public void ViewDepth(int Depth)
	{
		if (Depth >= 0 && Depth <= 100)
		{
			ViewDepth.Int(Depth);
		}
	}

	// Function for menu attribute

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

	// Get locked value
	public boolean Locked()
	{
		return Locked;
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

	// Get value of window Active
	public boolean HaveWindowActive()
	{
		return Items.get(MenuBarCursor).get(SubMenuCursor).getClass().equals(MenuItem_Windows.class) && ((MenuItem_Windows)Items.get(MenuBarCursor).get(SubMenuCursor)).WindowActive();
	}


	// Draw menu by calling other function
	public void DrawMenu()
	{
		ExitWindow = false;

		// Initialize PosX and PosY
		double PosX = 0;
		double PosY = 0;
		double OccupiedWidth = 0;
		double MarginPixels = ConvertPercentToPx(MenuItem.MarginPercent, true, GridWidth, GridHeight);

		boolean WindowDraw = false;
		// Draw menu bar on the top of the screen
		DrawMenuBar();

		// Draw alpha rectangle
		if (!HaveWindowActive())
		{
			DrawAlphaRectangle();
		}

		// Draw all menu items
		for (int MenuIndex = 0; MenuIndex < Items.size(); MenuIndex++)
		{
			for (int SubMenuIndex = 0; SubMenuIndex < Items.get(MenuIndex).size(); SubMenuIndex++)
			{
				// Move Y from constant
				PosY = PosY - ConvertPercentToPx(RowHeight, false, GridWidth, GridHeight);

				// if first item (title)
				if (SubMenuIndex == 0)
				{
					// Raise Y by 1/5 of RowHeight
					PosY = PosY + Math.abs(PosY * 1.0f/5.0f);
					// Draw
					if (MenuIndex == MenuBarCursor)
					{
						// Draw highlight if cursor is on it
						Items.get(MenuIndex).get(SubMenuIndex).Draw(PosX, PosY, GridWidth(), GridHeight(), true, false, false);
					}
					else
					{
						// Draw normal
						Items.get(MenuIndex).get(SubMenuIndex).Draw(PosX, PosY, GridWidth(), GridHeight(), false, false, false);
					}

					// Keep track of first item width
					OccupiedWidth = Items.get(MenuIndex).get(SubMenuIndex).Occupied_Width();
				}
				// Check if MenuCursor on MenuIndex
				else if (MenuIndex == MenuBarCursor)
				{
					// Check if window is active
					if (HaveWindowActive())
					{
						// Check if the item is a window
						if (Items.get(MenuIndex).get(SubMenuIndex).getClass().equals(MenuItem_Windows.class))
						{
							// Check if the window is active
							if (((MenuItem_Windows)Items.get(MenuIndex).get(SubMenuIndex)).WindowActive())
							{
								((MenuItem_Windows)Items.get(MenuIndex).get(SubMenuIndex)).Draw(PosX, PosY, GridWidth(), GridHeight(), false, false,true);
							}
						}
						// Don't draw if not window
					}
					// Draw items. Check if subCursor on it.
					else if (SubMenuIndex == SubMenuCursor)
					{
						// Call draw function with highlight
						Items.get(MenuIndex).get(SubMenuIndex).Draw(PosX, PosY, GridWidth(), GridHeight(), true, true, false);
					}
					else
					{
						// Call draw function without highlight
						Items.get(MenuIndex).get(SubMenuIndex).Draw(PosX, PosY, GridWidth(), GridHeight(), false, true, false);
					}
				}
				// Reset Occupied_Width to avoid adding ancient Occupied_Width to new Width
				Items.get(MenuIndex).get(SubMenuIndex).Occupied_Width(0);
			}

			// Reset Y to default
			PosY = 0;

			// Move X to the right
			PosX = PosX + OccupiedWidth + (MarginPixels * BigMargin);
			OccupiedWidth = 0;
		}

		glEnable(GL_TEXTURE_2D);
	}

	// Draw equally the menu bar and its item
	private void DrawMenuBar()
	{
		// Draw bar that cover the top of the screen. Convert Point to Gl unit.
		double PosXGL = ConvertPxToGL(0, true, GridWidth, GridHeight,false);
		double PosYGL = ConvertPercentToGL(-RowHeight, false, GridWidth, GridHeight, false);
		double GridWidthGL = ConvertPxToGL(GridWidth, true, GridWidth, GridHeight, false);
		double TopYGL = ConvertPxToGL(0, false, GridWidth, GridHeight,false);

		// Change Color
		glColor4f(BarColor.getRed() / 255.0f, BarColor.getGreen() / 255.0f, BarColor.getBlue() / 255.0f, BarColor.getAlpha() / 255.0f);

		// Change polygon Mode
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

		// Draw rectangle
		glRectd(PosXGL, PosYGL, PosXGL + GridWidthGL, TopYGL);
	}

	// Draw alpha rectangle where MenuBarCursor is active
	private void DrawAlphaRectangle()
	{
		double BiggestSubItemWidth = 0;
		double StartPosX = 0;
		double MarginPixels = ConvertPercentToPx(MenuItem.MarginPercent, true, GridWidth, GridHeight);

		// Get X. Check all first item
		for (int Item = 0; Item < MenuBarCursor;Item++)
		{
			// Get Start X
			StartPosX = StartPosX + Items.get(Item).get(0).GetDrawWidth(GridWidth(), GridHeight(), false, 0) + MarginPixels * BigMargin;
		}

		// Check all item where menu cursor is
		for (int Item = 1;Item < Items.get(MenuBarCursor).size();Item++)
		{
			// Get width
			double SubItemWidth = Items.get(MenuBarCursor).get(Item).GetDrawWidth(GridWidth(),GridHeight(),false,0);

			// Check if bigger than the biggest
			if (SubItemWidth > BiggestSubItemWidth)
			{
				BiggestSubItemWidth = SubItemWidth;
			}
		}

		// Draw rectangle

		// Initialize point
		double StartRectPosXGL = ConvertPxToGL(StartPosX - MarginPixels, true, GridWidth(), GridHeight(), false);
		double EndRectPosXGL = ConvertPxToGL(StartPosX + BiggestSubItemWidth + MarginPixels, true, GridWidth(), GridHeight(), false);
		double EndRectPosYGL = ConvertPxToGL(0 - ConvertPercentToPx(RowHeight, false, GridWidth, GridHeight), false, GridWidth(), GridHeight(), false);
		double StartRectPosYGL = ConvertPxToGL(0 - (ConvertPercentToPx(RowHeight, false, GridWidth, GridHeight) * Items.get(MenuBarCursor).size()),false,GridWidth(),GridHeight(),false);

		// Initialize polygon mode to FIll
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

		// Initialize Color
		glColor4f((float) AlphaRectColor.getRed() / 255.0f, (float) AlphaRectColor.getGreen() / 255.0f, (float) AlphaRectColor.getBlue() / 255.0f, (float) AlphaRectColor.getAlpha() / 255.0f);

		glRectd(StartRectPosXGL, StartRectPosYGL, EndRectPosXGL, EndRectPosYGL);
	}

	// Get value of Active
	public boolean Active()
	{
		return Active;
	}

	// Set value of Active
	public void Active(boolean IsActive)
	{
		// Remove old message when the menu gets activated
		if (IsActive)
		{
			// Make the message expire
			MessageTime = MaxMessageTime;
		}
		else
		{
			// Close opened windows when closing the menu
			if (Items != null)
			{
				// The game menu only. It is the only one that has windows right now.
				int MenuInMenuBar = 0;

				if (Items.get(MenuInMenuBar) != null)
				{
					for (int MenuItem = 0; MenuItem < this.Items.size(); MenuItem++)
					{
						if (Items.get(MenuInMenuBar).get(MenuItem) != null)
						{
							// Check if the item is part of the window class so it can be closed.
							if (Items.get(MenuInMenuBar).get(MenuItem).getClass().equals(MenuItem_Windows.class))
							{
								MenuItem_Windows WindowRef = (MenuItem_Windows) Items.get(MenuInMenuBar).get(MenuItem);
								WindowRef.WindowActive(false);
							}
						}
					}
				}
			}
		}

		// Change menu state
		Active = IsActive;
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
		if (MenuBarCursor < Items.size() - 1 && !Locked())
		{
			MenuBarCursor++;
			// Check if SubMenuCursor to big to fit next subMenu
			if (SubMenuCursor >= Items.get(MenuBarCursor).size() - 1)
			{
				SubMenuCursor = Items.get(MenuBarCursor).size() - 1;
			}
			// Check if Item is not enable
			else if (!Items.get(MenuBarCursor).get(SubMenuCursor).Enabled())
			{
				SubMenuCursor--;
			}
		}
	}

	// Move cursor from 1 to the left
	public void CursorLeft()
	{
		// Check if cursor can move to left
		if (MenuBarCursor > 0 && !Locked() && !HaveWindowActive())
		{
			MenuBarCursor--;
			// Check if SubMenuCursor to big to fit next subMenu
			if (SubMenuCursor >= Items.get(MenuBarCursor).size() - 1)
			{
				SubMenuCursor = Items.get(MenuBarCursor).size() - 1;
			}
			// Check if Item is not enable
			else if (!Items.get(MenuBarCursor).get(SubMenuCursor).Enabled())
			{
				SubMenuCursor--;
			}
		}
	}

	// Move cursor down from 1
	public void CursorDown()
	{
		// Check if cursor can move down
		if (SubMenuCursor < Items.get(MenuBarCursor).size() - 1 && !Locked() && !HaveWindowActive())
		{
			SubMenuCursor++;
		}
		// If Item is not enabled, skip
		if (!Items.get(MenuBarCursor).get(SubMenuCursor).Enabled() && !Locked() && !HaveWindowActive())
		{
			SubMenuCursor++;
		}
	}

	// Move cursor up from 1
	public void CursorUp()
	{
		// Check if cursor can move down
		if (SubMenuCursor > 0 && !Locked() && !HaveWindowActive())
		{
			SubMenuCursor--;
		}
		// If Item is not enabled, skip
		if (!Items.get(MenuBarCursor).get(SubMenuCursor).Enabled() && !Locked() && !HaveWindowActive())
		{
			SubMenuCursor--;
		}
	}

	// Lock/Unlock current cursor position if not the first item of menu
	public void Locking()
	{
		if (Items.get(MenuBarCursor).get(SubMenuCursor).getClass().equals(MenuItem_Windows.class) && !ExitWindow) // Window
		{
			// Activate windows at this index
			((MenuItem_Windows)(Items.get(MenuBarCursor).get(SubMenuCursor))).WindowActive(true);
		}

		else if (Items.get(MenuBarCursor).get(SubMenuCursor).getClass().equals(MenuItem_CheckBox.class)) // Checkbox
		{
			// De/Activate checkbox at this index
			((MenuItem_CheckBox)(Items.get(MenuBarCursor).get(SubMenuCursor))).IsChecked(!((MenuItem_CheckBox)(Items.get(MenuBarCursor).get(SubMenuCursor))).IsChecked());
		}
		else if (Items.get(MenuBarCursor).get(SubMenuCursor).getClass().equals(MenuItem_RadioButton.class)) // Radio button
		{
			// De/Activate Radio Button at this index
			((MenuItem_RadioButton)(Items.get(MenuBarCursor).get(SubMenuCursor))).IsChecked(true);
		}
		else if (Items.get(MenuBarCursor).get(SubMenuCursor).getClass().equals(MenuItem_HorSlider.class)) // Horizontal slider
		{
			((MenuItem_HorSlider)(Items.get(MenuBarCursor).get(SubMenuCursor))).Action(false);
		}
		else if (MenuBarCursor == PosQuit[0] && SubMenuCursor == PosQuit[1]) // Quit Game
		{
			// Can't quit a game if not in a game
			if (InGame)
			{
				QuitGame();
			}
			else
			{
				// Display a message
				this.NewMessageToShow("You're not in a game.");
			}

			// BUG: Still can't close the menu when 'quit game' is pressed without locking the game
		}
		else if (MenuBarCursor == PosExit[0] && SubMenuCursor == PosExit[1]) // Exit Game
		{
			ExitGame();
		}
		if (SubMenuCursor > 0 && !Items.get(MenuBarCursor).get(SubMenuCursor).getClass().equals(MenuItem_RadioButton.class) &&
				!Items.get(MenuBarCursor).get(SubMenuCursor).getClass().equals(MenuItem_CheckBox.class) &&
				!Items.get(MenuBarCursor).get(SubMenuCursor).getClass().equals(MenuItem_HorSlider.class) &&
				!Items.get(MenuBarCursor).get(SubMenuCursor).getClass().equals(MenuItem_Windows.class) &&
				!Items.get(MenuBarCursor).get(SubMenuCursor).getClass().equals(MenuItem_Dialog.class) &&
				!(MenuBarCursor == PosQuit[0] && SubMenuCursor == PosQuit[1]) &&
				!(MenuBarCursor == PosExit[0] && SubMenuCursor == PosExit[1]))
		{
			Locked(!Locked());
		}
	}

	// Call action of item
	public void Action(boolean MenuKeyPressed)
	{
		// Send input to current menuItem
		Items.get(MenuBarCursor).get(SubMenuCursor).Action(MenuKeyPressed);
	}

	// Load demo into system
	//public void LoadDemo(String DemoName)
	//{
	// To do
	//}

	// Close game without closing app
	public void QuitGame()
	{
		// Quit playing
		this.Active(false);
		IsClient = false;
		IsServer = false;
		InGame = false;
	}

	// Close app
	public void ExitGame()
	{
		UserWantsToExit = true;
	}

	// Convert percent to Px
	public static double ConvertPercentToPx(double Percent, boolean IsX, int GridWidth, int GridHeight)
	{
		// Declare local int
		double Pixel = 0;

		// Check if is X coordonate
		if (IsX)
		{
			Pixel = (Percent * GridWidth) / 100;
		}
		else // Is Y
		{
			Pixel = (Percent * GridHeight) / 100;
		}

		// Return converted measure
		return Pixel;
	}

	// Convert percent to GL
	public static double ConvertPercentToGL(double Percent, boolean IsX, int GridWidth, int GridHeight, boolean IsWindow)
	{
		// Declare local int
		double GLUnit = 0;

		// Check if is X coordonate
		if (IsX)
		{
			GLUnit = (Percent * GridWidth) / 100;
		}
		else // Is Y
		{
			GLUnit = (Percent * GridHeight) / 100;
		}

		// Convert the obtains pixel to GL
		GLUnit = ConvertPxToGL(GLUnit, IsX, GridWidth, GridHeight, IsWindow);

		// Return converted measure
		return GLUnit;
	}

	// Convert pixel to GL
	public static double ConvertPxToGL(double Pixel, boolean IsX, int GridWidth, int GridHeight, boolean IsWindow)
	{
		// Declare local int
		double GLUnit = 0;

		// Check if it a window coordonate
		if (!IsWindow)
		{
			// Check if is X coordonate
			if (IsX)
			{
				GLUnit = (Pixel * GlWinWidth) / GridWidth;
			}
			else // Is Y
			{
				GLUnit = (Pixel * GlWinHeight) / GridHeight;
			}
		}
		else
		{
			// Check if is X coordonate
			if (IsX)
			{
				GLUnit = (Pixel * 2.0f) / GridWidth;
			}
			else // Is Y
			{
				GLUnit = (Pixel * 2.0f) / GridHeight;
			}
		}

		// Return converted measure
		return GLUnit;
	}

	// Draw image from the bottom corner of the window (not draw at the same time as menu
	public static void DrawTexture(Texture Image, double LeftXPercent, double BottomYPercent, double WidthPercent, double HeightPercent)
	{
		// Move drawing cursor to bottom of window
		glTranslatef(-1,-1,0);

		// Initialize coordonate
		double PosXGL = ConvertPercentToGL(LeftXPercent, true, Display.getWidth(), Display.getHeight(), true);
		double PosYGL = ConvertPercentToGL(BottomYPercent, false, Display.getWidth(), Display.getHeight(), true);
		double ImageWidthGL = ConvertPercentToGL(WidthPercent, true, Display.getWidth(), Display.getHeight(), true);
		double ImageHeightGL = ConvertPercentToGL(HeightPercent, false, Display.getWidth(), Display.getHeight(), true);

		// Draw image
		glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		//glEnable(GL_TEXTURE_2D);

		Image.Bind();

		glBegin(GL_QUADS);
		glTexCoord2d(0.0d, 1.0d);
		glVertex2d(PosXGL, PosYGL);
		glTexCoord2d(1.0d, 1.0d);
		glVertex2d(PosXGL + ImageWidthGL, PosYGL);
		glTexCoord2d(1.0d, 0.0d);
		glVertex2d(PosXGL + ImageWidthGL, PosYGL + ImageHeightGL);
		glTexCoord2d(0.0d, 0.0d);
		glVertex2d(PosXGL, PosYGL + ImageHeightGL);
		glEnd();

		// Move drawing cursor back to center of window
		glTranslatef(1, 1, 0);

		//glDisable(GL_TEXTURE_2D);
	}

	public void NewMessageToShow(String Text)
	{
		//PlaySound("chat.wav");
		Message = Text;
		MessageTime = 0;
	}

	public void DrawMessage()
	{
		// Draw a "normal" message
		if (Message.length() > 0)
		{
			if (MessageTime < MaxMessageTime)
			{
				DrawText(Message, 2, 50, 2, 2);
				MessageTime++;
			}
		}
	}

	// Can be used to draw messages on the screen (2D texture or menu must be initialized or it will cause glitches)
	public void DrawText(String Text, double LeftXPercent, double BottomYPercent, double LetterWidthPercent, double LetterHeightPercent)
	{
		// Declare local variable
		double ActualX = LeftXPercent;
		// Declare local constant
		final double MarginPercent = 0.5d;
		// Draw letter one by one
		for (int Letter = 0; Letter < Text.length(); Letter++)
		{
			// Get int value of char
			int CharValue = (String.valueOf(Text.charAt(Letter)).toUpperCase()).charAt(0);
			int TextureIndex = 0;
			// Check if space
			if (CharValue == ' ')
			{
				// Adding width to total width
				ActualX = ActualX + LetterWidthPercent + MarginPercent;
			}
			else
			{
				// Check if number
				if (CharValue >= Char0Index && CharValue <= Char9Index)
				{
					// Initialize Texture Index in font List
					TextureIndex = CharValue - Char0Index;
				}
				else if (CharValue == CharCommercialAIndex)
				{
					TextureIndex = 10;
				}
				// Check if letter
				else if (CharValue >= CharAIndex && CharValue <= CharZIndex)
				{
					// Initialize Texture Index in font List
					TextureIndex = CharValue - CharAIndex + 10;
				}
				// Special character
				else if (CharValue == ChaPeriodIndex || CharValue == CharCommaIndex)
				{
					// Initialize Texture Index in font List
					TextureIndex = 36;
				}
				else if (CharValue == CharColonIndex)
				{
					// Initialize Texture Index in font List
					TextureIndex = 37;
				}
				else if (CharValue == CharUnderScoreIndex)
				{
					// Initialize Texture Index in font List
					TextureIndex = 38;
				}
				else if (CharValue == CharApostropheIndex)
				{
					// Initialize Texture Index in font List
					TextureIndex = 39;
				}
				else if (CharValue == CharAsteriskIndex)
				{
					// Initialize Texture Index in font List
					TextureIndex = 40;
				}
				else if (CharValue == CharLeftParaIndex || CharValue == CharLeftSquareIndex || CharValue == CharLeftBraceIndex)
				{
					// Initialize Texture Index in font List
					TextureIndex = 41;
				}
				else if (CharValue == CharRightParaIndex || CharValue == CharRightSquareIndex || CharValue == CharRightBraceIndex)
				{
					// Initialize Texture Index in font List
					TextureIndex = 42;
				}
				else if (CharValue == CharPlusIndex)
				{
					// Initialize Texture Index in font List
					TextureIndex = 43;
				}
				else if (CharValue == CharMinusIndex)
				{
					// Initialize Texture Index in font List
					TextureIndex = 44;
				}
				else if (CharValue == CharNumberIndex)
				{
					// Initialize Texture Index in font List
					TextureIndex = 45;
				}
				else if (CharValue == CharExclamationIndex)
				{
					// Initialize Texture Index in font List
					TextureIndex = 46;
				}
				else if (CharValue == CharPercentIndex)
				{
					TextureIndex = 47;
				}
				else // Character not define. Write ?.
				{
					// Initialize Texture Index in font List
					TextureIndex = 48;
				}

				// Initialize and Bind Texture
				Texture Image = FontArray.get(TextureIndex);

				// Draw image
				DrawTexture(Image, ActualX, BottomYPercent, LetterWidthPercent, LetterHeightPercent);

				// Add Width to X
				ActualX = ActualX + LetterWidthPercent + MarginPercent;
			}
		}
	}
}
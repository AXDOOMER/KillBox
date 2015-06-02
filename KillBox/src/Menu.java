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

public class Menu
{
	boolean Active = false;		// Show or hide the menu

	// Build arrays for the menus that can drop from the menu bar
	Strings[] BarItems;
	Strings[] GameItems;
	Strings[] Options;
	Strings[] Sound;
	Strings[] Video;

	// Don't mix 'Strings' with 'String'.
	public enum Strings
	{
		Game, Option, Control, Sound, Video,
		New_Game, Join_Game, Quit_Game, Play_Demo, About, Exit,
		Use_Freelook, Show_Messages, Show_HUD, Show_Debug,
		Grab_Mouse, Enable_Chat, Mouse_Sensitivity,
		SFX_Volume, Mode, Two_D, Three_D, Duppler,
		FullScreen, Filtering, View_Depth
	}

	// Screen coordinates are represented as a fixed resolution
	int GridWidth = 640;
	int GridHeight = 480;

	final String HRchar = "--";		// Horizontal rule  (<hr /> in HTML)

	public Menu()
	{
		BarItems = new Strings[5];
		BarItems[0] = Strings.Game;
		BarItems[1] = Strings.Option;
		BarItems[2] = Strings.Control;
		BarItems[3] = Strings.Sound;
		BarItems[4] = Strings.Video;

	}

	public void DrawMenuItemText(Strings Item, int PosX, int PosY)
	{
		// String where item's text is stored
		String Text = "NONE";

		// Do a swtich-case to set the good text using the item's name



		// Draw the text
		// Use: DrawText(Text, PosX, PosY);
	}

	public void DrawText(String Text, int PosX, int PosY)
	{

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

}

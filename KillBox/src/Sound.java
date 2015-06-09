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

import java.util.ArrayList;

public class Sound
{
    final int SFX_VOICES = 32;
    boolean Preload_ = false;

	// How sound directions get calculated
	// 'Bi' is two-dimensionnal, 'Three' is 3D and 'Doppler' is 3D + Doppler effect
	public enum SoundModes
	{
		Bi, Three, Duppler
	}
	SoundModes SndMode = SoundModes.Bi;

    public Sound(boolean Preload, ArrayList<Player> Listeners, SoundModes Mode)
    {
        System.out.print("SFX initialisation");

        Preload_ = Preload;
        if(Preload_)
        {
            // Load sound effects in memory
            System.out.print(" (with precache)");
        }

        ArrayList<Player> Players = Listeners;

        System.out.println(". ");

		if (Mode != null)
		{
			SndMode = Mode;
		}
		else
		{
			SndMode = SoundModes.Three;
		}

    }

    public Boolean PlaySound(Player Source, String Name)
    {
        // Get the sound from a Player and the source will be at its X,Y,Z.


        // If the sound was not found, return false:
        return false;
    }
}

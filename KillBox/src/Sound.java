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

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;

public class Sound
{
    final int SfxVoices = 32;
    boolean Preload = false;

	// ArrayLists to store information about the sounds
	ArrayList<String> LoadedFiles = new ArrayList<String>();
	ArrayList<Integer> SoundBuffers = new ArrayList<Integer>();

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

        this.Preload = Preload;
        if(Preload)
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

    public boolean PlaySound(Player Source, String Name)
    {
        // Get the sound from a Player and the source will be at its X,Y,Z using its velocity.


        // If the sound was not found, return false:
        return false;
    }

	public void ChangeSoundMode(SoundModes Mode)
	{
		SndMode = Mode;
	}

	public void LoadSoundFromFile(String File)
	{
		// Load the sound, create a buffer and save the filename.


	}
}

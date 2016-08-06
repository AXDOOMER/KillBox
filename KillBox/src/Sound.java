//Copyright (C) 2014-2016 Alexandre-Xavier Labonté-Lamoureux
//Copyright (C) 2015 Andy Sergerie
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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.*;
import org.lwjgl.opengl.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL10.alGenBuffers;

import org.lwjgl.util.WaveData;

public class Sound
{
	final int SfxVoices = 32;
	final int Attenuator = 25; // A smaller value makes the players further
	boolean Preload = false;
	int IntSource;

	public float VolumeMultiplier = 1.0f;
	final int MaxSoundPlayed = 60;
	int NumberSoundPlayed = 0;

	Player Hear = null;

	// ArrayLists to store information about the sounds
	ArrayList<String> LoadedFiles = new ArrayList<String>();
	ArrayList<Integer> SoundBuffers = new ArrayList<Integer>();
	ArrayList<Integer> IntSources = new ArrayList<Integer>();

	// How sound directions get calculated
	// 'Bi' is two-dimensional, 'Three' is 3D and 'Doppler' is 3D + Doppler effect
	public enum SoundModes
	{
		Bi, Three, Duppler
	}
	SoundModes SndMode = SoundModes.Bi;

	public void CloseOpenAL()
	{
		// Don't just close. DESTROY!
		AL.destroy();
	}

	public void SetNewListener(Player Hear)
	{
		this.Hear = Hear;
	}

	public Sound(boolean Preload, ArrayList<Player> Listeners, SoundModes Mode)
	{
		try
		{
			AL.create();
			IntSource = alGenSources();
		}
		catch (LWJGLException e)
		{
			e.printStackTrace();
		}

		System.out.print("SFX initialisation");

		this.Preload = Preload;
		if(Preload)
		{
			// Load sound effects in memory
			this.LoadSoundFromFile("button.wav");
			this.LoadSoundFromFile("chat.wav");
			this.LoadSoundFromFile("cocking.wav");
			this.LoadSoundFromFile("death.wav");
			this.LoadSoundFromFile("respawn.wav");
			System.out.print(" (with precache)");

		}

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

	public void PlaySound(String Name, Player Emitter)
	{
		boolean RightBufferIndex = false;
		int SoundBuffersIndex = 0;
		float Orientation = 1.0f;
		boolean SoundIsThere = true;


		Orientation = ConvertAngle(Hear.GetRadianAngle());

		if (!LoadedFiles.contains(Name))
		{
			SoundIsThere = this.LoadSoundFromFile(Name);
		}

		if (SoundIsThere)
		{
			// Reset all sound buffers
			if (NumberSoundPlayed == MaxSoundPlayed)
			{
				NumberSoundPlayed = 0;

				// Delete all buffers
				/*for(int i = 0; i < SoundBuffers.size(); i++)
				{
					alDeleteBuffers(SoundBuffers.get(i));
				}
				SoundBuffers.clear();*/
				// Delete all sources
				for (int i = 0; i < IntSources.size(); i++)
				{
					alDeleteSources(IntSources.get(i));
				}
				IntSources.clear();

				// LoadSoundFromFile will recreate all the buffers and all the sources
				for (int j = 0; j < LoadedFiles.size(); j++)
				{
					this.LoadSoundFromFile(LoadedFiles.get(j));
				}
			}
			NumberSoundPlayed++;
			// Find the right buffer for the sound
			do
			{
				if (LoadedFiles.get(SoundBuffersIndex).equals(Name))
				{
					RightBufferIndex = true;
				}
				else
				{
					SoundBuffersIndex++;
				}
			} while (!RightBufferIndex && SoundBuffersIndex < LoadedFiles.size());

			// Position of the source sound.
			FloatBuffer SourcePos;
			// Velocity of the source sound.
			FloatBuffer SourceVel;
			// Position of the listener.
			FloatBuffer ListenerPos;
			// Velocity of the listener.
			FloatBuffer ListenerVel;
			//Orientation of the listener. (first 3 elements are "at", second 3 are "up") */
			FloatBuffer ListenerOri;

			switch (SndMode)
			{
				case Bi:
					// Position of the source sound.
					SourcePos = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] {Emitter.PosX() / Attenuator, Emitter.PosY() / Attenuator, 0.0f }).rewind();

					// Position of the listener.
					ListenerPos = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] {Hear.PosX() / Attenuator, Hear.PosY() / Attenuator, 0.0f }).rewind();

					// Orientation of the listener. (first 3 elements are "at" (nose), second 3 are "up" (hair top) )
					ListenerOri = (FloatBuffer)BufferUtils.createFloatBuffer(6).put(new float[]{(float)Math.cos(Hear.GetRadianAngle()), (float)Math.sin(Hear.GetRadianAngle()), 0.0f, 0.0f, 0.0f, 1.0f }).rewind();

					// Get the sound from a Player and the source will be at its X,Y,Z using its velocity.
					alSourcei(IntSources.get(SoundBuffersIndex), AL_SOURCE_RELATIVE, AL_FALSE);
					alSourcei(IntSources.get(SoundBuffersIndex), AL_BUFFER, SoundBuffers.get(SoundBuffersIndex));
					alSource(IntSources.get(SoundBuffersIndex), AL_POSITION, SourcePos);
					// AL_GAIN controls the volume
					alSourcef(IntSources.get(SoundBuffersIndex), AL_GAIN, 1.0f * VolumeMultiplier);

					alListener(AL_POSITION, ListenerPos);
					alListener(AL_ORIENTATION, ListenerOri);
					alSourcePlay(IntSources.get(SoundBuffersIndex));
					break;

				case Three:
					// Position of the source sound.
					SourcePos = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] {Emitter.PosX() / Attenuator, Emitter.PosY() / Attenuator, Emitter.PosZ() / Attenuator }).rewind();

					// Position of the listener.
					ListenerPos = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] {Hear.PosX() / Attenuator, Hear.PosY() / Attenuator, Hear.PosZ() / Attenuator }).rewind();

					// Orientation of the listener. (first 3 elements are "at" (nose), second 3 are "up" (hair top) )
					ListenerOri = (FloatBuffer)BufferUtils.createFloatBuffer(6).put(new float[]{(float)Math.cos(Hear.GetRadianAngle()), (float)Math.sin(Hear.GetRadianAngle()), 0.0f, 0.0f, 0.0f, 1.0f }).rewind();

					// Get the sound from a Player and the source will be at its X,Y,Z using its velocity.
					alSourcei(IntSources.get(SoundBuffersIndex), AL_SOURCE_RELATIVE, AL_FALSE);
					alSourcei(IntSources.get(SoundBuffersIndex), AL_BUFFER, SoundBuffers.get(SoundBuffersIndex));
					alSource(IntSources.get(SoundBuffersIndex), AL_POSITION, SourcePos);
					// AL_GAIN controls the volume
					alSourcef(IntSources.get(SoundBuffersIndex), AL_GAIN, 1.0f * VolumeMultiplier);

					alListener(AL_POSITION, ListenerPos);
					alListener(AL_ORIENTATION, ListenerOri);
					alSourcePlay(IntSources.get(SoundBuffersIndex));
					break;

				case Duppler:
					// Same code as 3D for now!
					// Position of the source sound.
					SourcePos = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] {Emitter.PosX() / Attenuator, Emitter.PosY() / Attenuator, Emitter.PosZ() / Attenuator }).rewind();
					// Velocity of the source sound.
					SourceVel = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
					// Position of the listener.
					ListenerPos = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] {Hear.PosX() / Attenuator, Hear.PosY() / Attenuator, Hear.PosZ() / Attenuator }).rewind();
					// Velocity of the listener.
					ListenerVel = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f}).rewind();
					//Orientation of the listener. (first 3 elements are "at", second 3 are "up") */
					ListenerOri = (FloatBuffer)BufferUtils.createFloatBuffer(6).put(new float[]{(float)Math.cos(Hear.GetRadianAngle()), (float)Math.sin(Hear.GetRadianAngle()), 0.0f, 0.0f, 0.0f, 1.0f }).rewind();

					// Get the sound from a Player and the source will be at its X,Y,Z using its velocity.
					alSourcei(IntSources.get(SoundBuffersIndex), AL_SOURCE_RELATIVE, AL_FALSE);
					alSourcei(IntSources.get(SoundBuffersIndex), AL_BUFFER, SoundBuffers.get(SoundBuffersIndex));
					alSource(IntSources.get(SoundBuffersIndex), AL_POSITION, SourcePos);
					alSource(IntSources.get(SoundBuffersIndex), AL_VELOCITY, SourceVel);
					// AL_GAIN controls the volume
					alSourcef(IntSources.get(SoundBuffersIndex), AL_GAIN, 1.0f * VolumeMultiplier);

					alListener(AL_POSITION, ListenerPos);
					alListener(AL_VELOCITY, ListenerVel);
					alListener(AL_ORIENTATION, ListenerOri);
					alSourcePlay(IntSources.get(SoundBuffersIndex));
					break;
			}
		}
	}

	// Play a Sound without a Source and Location (Ex: Sound from the menu)
	public void PlaySound(String Name)
	{
		boolean RightBufferIndex = false;
		int SoundBuffersIndex = 0;
		boolean SoundIsThere = true;

		if (!LoadedFiles.contains(Name))
		{
			SoundIsThere = this.LoadSoundFromFile(Name);
		}

		if (SoundIsThere)
		{
			// Find the right buffer for the sound
			do
			{
				if (LoadedFiles.get(SoundBuffersIndex).equals(Name))
				{
					RightBufferIndex = true;
				}
				else
				{
					SoundBuffersIndex++;
				}
			} while (!RightBufferIndex && SoundBuffersIndex < LoadedFiles.size());

			// Position of the source sound.
			FloatBuffer SourcePos;
			// Velocity of the source sound.
			FloatBuffer SourceVel;
			// Position of the listener.
			FloatBuffer ListenerPos;
			// Velocity of the listener.
			FloatBuffer ListenerVel;
			//Orientation of the listener. (first 3 elements are "at", second 3 are "up") */
			FloatBuffer ListenerOri;

			// Position of the source sound.
			SourcePos = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[]{0.0f, 0.0f, 0.0f}).rewind();
			// Velocity of the source sound.
			SourceVel = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[]{0.0f, 0.0f, 0.0f}).rewind();
			// Position of the listener.
			ListenerPos = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[]{0.0f, 0.0f, 0.0f}).rewind();
			// Velocity of the listener.
			ListenerVel = (FloatBuffer) BufferUtils.createFloatBuffer(3).put(new float[]{0.0f, 0.0f, 0.0f}).rewind();
			//Orientation of the listener. (first 3 elements are "at", second 3 are "up")
			ListenerOri = (FloatBuffer) BufferUtils.createFloatBuffer(6).put(new float[]{0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f}).rewind();

			alSourceStop(IntSource);
			// Get the sound from a Player and the source will be at its X,Y,Z using its velocity.
			alSourcei(IntSource, AL_BUFFER, SoundBuffers.get(SoundBuffersIndex));
			alSource(IntSource, AL_POSITION, SourcePos);
			alSource(IntSource, AL_VELOCITY, SourceVel);
			// AL_GAIN controls the volume
			alSourcef(IntSource, AL_GAIN, 0.3f * VolumeMultiplier);

			alListener(AL_POSITION, ListenerPos);
			alListener(AL_VELOCITY, ListenerVel);
			alListener(AL_ORIENTATION, ListenerOri);
			alSourcePlay(IntSource);
		}
	}

	private float ConvertAngle(float AngleRad)
	{
		return AngleRad * (float)(1.0f / Math.PI);
	}

	public void ChangeSoundMode(SoundModes Mode)
	{
		SndMode = Mode;
	}

	public boolean LoadSoundFromFile(String File)
	{
		try
		{
			// Load the sound, create a buffer and save the filename.
			WaveData data  = WaveData.create(new BufferedInputStream(new FileInputStream("res/sounds/" + File)));
			int buffer = alGenBuffers();
			alBufferData(buffer, data.format, data.data, data.samplerate);
			data.dispose();

			if (!LoadedFiles.contains(File))
			{
				LoadedFiles.add(File);
				SoundBuffers.add(buffer);
			}

			IntSources.add(alGenSources());
		}
		catch (Exception ex)
		{
			return false;
		}

		return true;
	}
}

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
	boolean Preload = false;

	public float VolumeMultiplier = 1.0f;

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

		try
		{
			AL.create();
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

	public void PlaySound(Player Hear, String Name, float SoundPosX, float SoundPosY, float SoundPosZ)
	{
		boolean RightBufferIndex = false;
		int SoundBuffersIndex = 0;
		float Orientation = 1.0f;
		boolean SoundIsThere = true;

		Orientation = ConvertAngle(Hear.GetRadianAngle());

		if(!LoadedFiles.contains(Name))
		{
			SoundIsThere = this.LoadSoundFromFile(Name);
		}

		if(SoundIsThere)
		{
			// Find the right buffer for the sound
			do
			{
				if(LoadedFiles.get(SoundBuffersIndex).equals(Name))
				{
					RightBufferIndex = true;
				}
				else
				{
					SoundBuffersIndex++;
				}
			} while(!RightBufferIndex && SoundBuffersIndex < LoadedFiles.size());

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
			int IntSource;

			switch (SndMode)
			{
				case Bi:
					// Position of the source sound.
					SourcePos = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] { SoundPosX, SoundPosY, 0.0f }).rewind();
					// Velocity of the source sound.
					SourceVel = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();

					// Position of the listener.
					ListenerPos = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] {Hear.PosX(), Hear.PosY(), 0.0f }).rewind();
					// Velocity of the listener.
					ListenerVel = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
					//Orientation of the listener. (first 3 elements are "at", second 3 are "up")
					ListenerOri = (FloatBuffer)BufferUtils.createFloatBuffer(6).put(new float[]{Hear.PosX(), Hear.PosY(), 0.0f, (float)Math.cos(Hear.GetRadianAngle()), (float)Math.sin(Hear.GetRadianAngle()), 0.0f }).rewind();


					// Get the sound from a Player and the source will be at its X,Y,Z using its velocity.
					IntSource = alGenSources();
					alSourcei(IntSource, AL_SOURCE_RELATIVE, AL_FALSE);
					alSourcei(IntSource, AL_BUFFER, SoundBuffers.get(SoundBuffersIndex));
					alSource(IntSource, AL_POSITION, SourcePos);
					alSource(IntSource, AL_VELOCITY, SourceVel);
					// AL_GAIN Controle le volume
					alSourcef(IntSource, AL_GAIN, 1.0f * VolumeMultiplier);

					alListener(AL_POSITION, ListenerPos);
					alListener(AL_VELOCITY,ListenerVel);
					alListener(AL_ORIENTATION, ListenerOri);
					alSourcePlay(IntSource);
					break;

				case Three:
					// Position of the source sound.
					SourcePos = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] { SoundPosX,SoundPosY, SoundPosZ  }).rewind();
					// Velocity of the source sound.
					SourceVel = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
					// Position of the listener.
					ListenerPos = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] {Hear.PosX(),Hear.PosY() , Hear.PosZ() }).rewind();
					// Velocity of the listener.
					ListenerVel = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
					//Orientation of the listener. (first 3 elements are "at", second 3 are "up") */
					ListenerOri = (FloatBuffer)BufferUtils.createFloatBuffer(6).put(new float[]{0.0f, 0.0f,-Orientation , 0.0f, 1.0f, 0.0f }).rewind();
					// Get the sound from a Player and the source will be at its X,Y,Z using its velocity.
					IntSource = alGenSources();
					alSourcei(IntSource, AL_BUFFER, SoundBuffers.get(SoundBuffersIndex));
					alSource(IntSource, AL_POSITION, SourcePos);
					alSource(IntSource, AL_VELOCITY, SourceVel);
					// AL_GAIN Controle le volume
					alSourcef(IntSource, AL_GAIN, 0.5f * VolumeMultiplier);

					alListener(AL_POSITION, ListenerPos);
					alListener(AL_VELOCITY,ListenerVel);
					alListener(AL_ORIENTATION, ListenerOri);
					alSourcePlay(IntSource);
					break;

				case Duppler:
					// Same code as 3D for now!
					// Position of the source sound.
					SourcePos = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[]{SoundPosX, SoundPosY, SoundPosZ}).rewind();
					// Velocity of the source sound.
					SourceVel = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }).rewind();
					// Position of the listener.
					ListenerPos = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] {Hear.PosX(), Hear.PosY(), Hear.PosZ() }).rewind();
					// Velocity of the listener.
					ListenerVel = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f}).rewind();
					//Orientation of the listener. (first 3 elements are "at", second 3 are "up") */
					ListenerOri = (FloatBuffer)BufferUtils.createFloatBuffer(6).put(new float[]{0.0f, 0.0f, -Orientation, 0.0f, 1.0f, 0.0f }).rewind();
					// Get the sound from a Player and the source will be at its X,Y,Z using its velocity.
					IntSource = alGenSources();
					alSourcei(IntSource, AL_BUFFER, SoundBuffers.get(SoundBuffersIndex));
					alSource(IntSource, AL_POSITION, SourcePos);
					alSource(IntSource, AL_VELOCITY, SourceVel);
					// AL_GAIN controls the volume
					alSourcef(IntSource, AL_GAIN, 0.5f * VolumeMultiplier);

					alListener(AL_POSITION, ListenerPos);
					alListener(AL_VELOCITY, ListenerVel);
					alListener(AL_ORIENTATION, ListenerOri);
					alSourcePlay(IntSource);
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
			int IntSource;

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
			// Get the sound from a Player and the source will be at its X,Y,Z using its velocity.
			IntSource = alGenSources();
			alSourcei(IntSource, AL_BUFFER, SoundBuffers.get(SoundBuffersIndex));
			alSource(IntSource, AL_POSITION, SourcePos);
			alSource(IntSource, AL_VELOCITY, SourceVel);
			// AL_GAIN Controle le volume
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
			WaveData data  = WaveData.create(new BufferedInputStream(new FileInputStream("Stuff/sounds/"+File)));
			int buffer = alGenBuffers();
			alBufferData(buffer, data.format, data.data, data.samplerate);
			data.dispose();

			LoadedFiles.add(File);
			SoundBuffers.add(buffer);
		}
		catch (Exception ex)
		{
			return false;
		}

		return true;
	}
}

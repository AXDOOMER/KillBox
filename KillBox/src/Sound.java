// Copyright (C) 2014-2017 Alexandre-Xavier Labonté-Lamoureux
// Copyright (C) 2015 Andy Sergerie
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;

public class Sound
{
	final int Attenuator = 25; // A smaller value makes the players further
	int IntSource;

	private long alcDevice;
	private long alcContext;

	public float VolumeMultiplier = 1.0f;
	final int MaxSounds = 60;
	int NumberSoundPlayed = 0;

	Player Hear = null;

	// ArrayLists to store information about the sounds
	ArrayList<String> LoadedFiles = new ArrayList<String>();
	ArrayList<Integer> SoundBuffers = new ArrayList<Integer>();
	ArrayList<Integer> IntSources = new ArrayList<Integer>();

	public void CloseOpenAL()
	{
		// Don't just close. DESTROY!
		alcMakeContextCurrent(0);
		alcDestroyContext(alcContext);
		alcCloseDevice(alcDevice);
	}

	public void SetNewListener(Player Hear)
	{
		this.Hear = Hear;
	}

	public Sound()
	{
		System.out.print("SFX initialisation");

		try
		{
			alcDevice = alcOpenDevice((ByteBuffer) null);
			if (alcDevice == 0) throw new RuntimeException("Failed to open default OpenAL device");
			ALCCapabilities alcCapabilities = ALC.createCapabilities(alcDevice);
			alcContext = alcCreateContext(alcDevice, (java.nio.IntBuffer) null);
			alcMakeContextCurrent(alcContext);
			AL.createCapabilities(alcCapabilities);
			IntSource = alGenSources();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		System.out.println(".");
	}

	public void PlaySound(String Name, Player Emitter)
	{
		//float Orientation = 1.0f;

		//Orientation = ConvertAngle(Hear.GetRadianAngle());

		// Cleanup the sound sources when too much sounds have been played
		int SourceIndex = NumberSoundPlayed % MaxSounds;
		if (IntSources.size() < SourceIndex && IntSources.get(SourceIndex) != null)
		{
			alDeleteSources(IntSources.get(SourceIndex));
			IntSources.set(SourceIndex, alGenSources());
		}
		else
		{
			IntSources.add(alGenSources());
		}

		if (CacheSound(Name))
		{
			NumberSoundPlayed++;

			// Find the right buffer for the sound
			int SoundBuffersIndex = 0;
			while (!LoadedFiles.get(SoundBuffersIndex).equals(Name))
			{
				SoundBuffersIndex++;
			}

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

			// This is the stereo sound. Play the sound according to the different parameters.
			// TODO: Do the 3D sound and the 3D sound with the duppler effect

			// Position of the source sound.
			SourcePos = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] {Emitter.PosX() / Attenuator, Emitter.PosY() / Attenuator, 0.0f }).rewind();

			// Position of the listener.
			ListenerPos = (FloatBuffer)BufferUtils.createFloatBuffer(3).put(new float[] {Hear.PosX() / Attenuator, Hear.PosY() / Attenuator, 0.0f }).rewind();

			// Orientation of the listener. (first 3 elements are "at" (nose), second 3 are "up" (hair top) )
			ListenerOri = (FloatBuffer)BufferUtils.createFloatBuffer(6).put(new float[]{(float)Math.cos(Hear.GetRadianAngle()), (float)Math.sin(Hear.GetRadianAngle()), 0.0f, 0.0f, 0.0f, 1.0f }).rewind();

			// Get the sound from a Player and the source will be at its X,Y,Z using its velocity.
			alSourcei(IntSources.get(SourceIndex), AL_SOURCE_RELATIVE, AL_FALSE);
			alSourcei(IntSources.get(SourceIndex), AL_BUFFER, SoundBuffers.get(SoundBuffersIndex));
			alSourcefv(IntSources.get(SourceIndex), AL_POSITION, SourcePos);
			// AL_GAIN controls the volume
			alSourcef(IntSources.get(SourceIndex), AL_GAIN, 1.0f * VolumeMultiplier);

			alListenerfv(AL_POSITION, ListenerPos);
			alListenerfv(AL_ORIENTATION, ListenerOri);
			alSourcePlay(IntSources.get(SourceIndex));
		}
	}

	// Play a Sound without a Source and Location (Ex: Sound from the menu)
	public void PlaySound(String Name)
	{
		if (CacheSound(Name))
		{
			// Find the right buffer for the sound
			int SoundBuffersIndex = 0;
			while (!LoadedFiles.get(SoundBuffersIndex).equals(Name))
			{
				SoundBuffersIndex++;
			}

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
			alSourcefv(IntSource, AL_POSITION, SourcePos);
			alSourcefv(IntSource, AL_VELOCITY, SourceVel);
			// AL_GAIN controls the volume
			alSourcef(IntSource, AL_GAIN, 0.3f * VolumeMultiplier);

			alListenerfv(AL_POSITION, ListenerPos);
			alListenerfv(AL_VELOCITY, ListenerVel);
			alListenerfv(AL_ORIENTATION, ListenerOri);
			alSourcePlay(IntSource);
		}
	}

	private float ConvertAngle(float AngleRad)
	{
		return AngleRad * (float)(1.0f / Math.PI);
	}

	// Check is the sound file is cached. If not, cache the file. Returns false on failure.
	public boolean CacheSound(String File)
	{
		if (!LoadedFiles.contains(File))
		{
			try
			{
				// Load the WAV file using javax.sound.sampled
				InputStream rawStream = Sound.class.getClassLoader().getResourceAsStream("res/sounds/" + File);
				if (rawStream == null) {
					rawStream = new FileInputStream("res/sounds/" + File);
				}
				AudioInputStream audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(rawStream));
				AudioFormat srcFmt = audioStream.getFormat();

				// Ensure we have signed little-endian 16-bit PCM
				if (srcFmt.isBigEndian() && srcFmt.getSampleSizeInBits() == 16) {
					AudioFormat pcmFmt = new AudioFormat(
							srcFmt.getEncoding(),
							srcFmt.getSampleRate(),
							16,
							srcFmt.getChannels(),
							srcFmt.getChannels() * 2,
							srcFmt.getSampleRate(),
							false);
					audioStream = AudioSystem.getAudioInputStream(pcmFmt, audioStream);
					srcFmt = pcmFmt;
				}

				byte[] audioBytes = audioStream.readAllBytes();
				audioStream.close();

				ByteBuffer audioBuffer = ByteBuffer.allocateDirect(audioBytes.length);
				audioBuffer.put(audioBytes);
				audioBuffer.flip();

				int alFmt = srcFmt.getChannels() == 1
						? (srcFmt.getSampleSizeInBits() == 8 ? AL_FORMAT_MONO8   : AL_FORMAT_MONO16)
						: (srcFmt.getSampleSizeInBits() == 8 ? AL_FORMAT_STEREO8 : AL_FORMAT_STEREO16);
				int sampleRate = (int) srcFmt.getSampleRate();

				int Buffer = alGenBuffers();
				alBufferData(Buffer, alFmt, audioBuffer, sampleRate);

				if (!LoadedFiles.contains(File))
				{
					LoadedFiles.add(File);
					SoundBuffers.add(Buffer);
				}

				//IntSources.add(alGenSources());
			}
			catch (Exception ex)
			{
				return false;
			}
		}

		return true;
	}
}

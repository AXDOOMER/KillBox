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

import static org.lwjgl.opengl.GL11.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;
import java.io.*;   // File Object

import org.lwjgl.BufferUtils;

public class Texture {
    //public static Texture MyTexture = new Texture("DOOR9_1.png", GL_NEAREST);   // temp for test

    private String Name = "DOOR9_1.png";

    private int id;
    private int Width;
    private int Height;

    public Texture(String Path, int Filter) //GL_NEAREST GL_LINEAR
    {
        int[] Pixels = null;
        try
        {
            BufferedImage Image = ImageIO.read(new File(Path));
            Width = Image.getWidth();
            Height = Image.getHeight();
            Pixels = new int[Width * Height];
            Image.getRGB(0, 0, Width, Height, Pixels, 0, Width);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        int[] Data = new int[Pixels.length];
        for (int i = 0; i < Pixels.length; i++) {
            int A = (Pixels[i] & 0xff000000) >> 24;
            int R = (Pixels[i] & 0xff0000) >> 16;
            int G = (Pixels[i] & 0xff00) >> 8;
            int B = (Pixels[i] & 0xff);

            Data[i] = A << 24 | B << 16 | G << 8 | R;
        }

        int id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);

        // Gives the filter to the texture
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, Filter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, Filter);


        IntBuffer Buffer = BufferUtils.createIntBuffer(Data.length);
        Buffer.put(Data);
        Buffer.flip();

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, Width, Height, 0, GL_RGBA, GL_UNSIGNED_BYTE, Buffer);

        this.id = id;
    }

    public int Width()
    {
        return Width;
    }

    public int Height()
    {
        return Height;
    }

    public void Bind()
    {
        glBindTexture(GL_TEXTURE_2D, id);
    }
}
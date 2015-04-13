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
import static org.lwjgl.util.glu.GLU.*;

public class Camera
{
    Player Plyr;

    // Postions inside the camera are as represented in OpenGL
    // The axis are inverted, thus a positive value is a negative value
    private float PosX;
    private float PosY;
    private float PosZ;
    private float RotX;
    private float RotY;
    private float RotZ;

    private float FOV;
    private float Aspect;
    private float Near;
    private float Far;

    public Camera(Player Plyr, float FOV, float Aspect, float Near, float Far)
    {
        this.Plyr = Plyr;

        PosX = Plyr.PosY;
        PosY = Plyr.PosZ + Plyr.ViewZ;   // Internally, the player's Z is the height.
        PosZ = Plyr.PosX;   // But in OpenGL, Z is vertical like the internal Y.
        RotX = 0;
        RotY = Plyr.GetDegreeAngle();
        RotZ = 0;

        this.FOV = FOV;
        this.Aspect = Aspect;
        this.Near = Near;
        this.Far = Far;
        InitProjection();
    }

    private void InitProjection()
    {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(FOV,Aspect,Near,Far);
        glMatrixMode(GL_MODELVIEW);
    }

    public void UseView()
    {
        //glRotatef(RotX, 1, 0, 0);
        glRotatef(RotY, 0, -1, 0);
        //glRotatef(RotZ, 0, 0, 1);
        //glTranslatef(PosX, PosY, PosZ);
    }

    public void UpdateCamera()
    {
        PosX = Plyr.PosY;
        PosY = Plyr.PosZ + Plyr.ViewZ;
        PosZ = Plyr.PosX;
        RotY = Plyr.GetDegreeAngle();
    }

    public void ChangeProperties(float FOV, float Aspect, float Near, float Far)
    {
        this.FOV = FOV;
        this.Aspect = Aspect;
        this.Near = Near;
        this.Far = Far;
    }

    public float PosX()
    {
        return PosX;
    }

    public float PosY()
    {
        return -PosY;
    }

    public float PosZ()
    {
        return PosZ;
    }

    public void PosX(float PosX)
    {
        this.PosX = PosX;
    }

    public void PosY(float PosY)
    {
        this.PosY = -PosY;
    }

    public void PosZ(float PosZ)
    {
        this.PosZ = PosZ;
    }

    public float RotX()
    {
        return RotX;
    }

    public float RotY()
    {
        return RotY;
    }

    public float RotZ()
    {
        return RotZ;
    }

    public void RotX(float RotX)
    {
        this.RotX = RotX;
    }

    public void RotY(float RotY)
    {
        this.RotY = RotY;
    }

    public void RotZ(float RotZ)
    {
        this.RotZ = RotZ;
    }
}
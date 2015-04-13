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

public class Bullet
{
	// A bullet fire texture that will be streched over the traced bullet path when it is shoot
	static Texture Fire = new Texture("Stuff/bullet.png", GL_LINEAR);

	float PosX;
	float PosY;
	float PosZ;
	float DirX;
	float DirY;
	float DirZ;

}

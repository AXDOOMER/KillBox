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

public class Random
{
    static int Index = 0;

    // An array of 256 numbers
    final short[] PRNG = {
            41, 35, 190, 132, 225, 108, 214, 174, 82, 144, 73, 241, 187, 233, 235, 179,
            166, 219, 60, 135, 12, 62, 153, 36, 94, 13, 28, 6, 183, 71, 222, 18,
            77, 200, 67, 139, 31, 3, 90, 125, 9, 56, 37, 93, 212, 203, 252, 150,
            245, 69, 59, 19, 137, 10, 50, 32, 154, 80, 238, 64, 120, 54, 253, 246,
            158, 220, 173, 79, 20, 242, 68, 102, 208, 107, 196, 48, 161, 34, 145, 157,
            218, 176, 202, 2, 185, 114, 44, 128, 126, 197, 213, 178, 234, 201, 204, 83,
            191, 103, 45, 142, 131, 239, 87, 97, 255, 105, 143, 205, 209, 30, 156, 22,
            230, 29, 240, 74, 119, 215, 232, 57, 51, 116, 244, 159, 164, 89, 53, 207,
            211, 72, 117, 217, 42, 229, 192, 247, 43, 129, 14, 95, 0, 141, 123, 5,
            21, 7, 130, 24, 112, 146, 100, 84, 206, 177, 133, 248, 70, 106, 4, 115,
            47, 104, 118, 250, 17, 136, 121, 254, 216, 40, 11, 96, 61, 151, 39, 138,
            194, 8, 165, 193, 140, 169, 149, 155, 168, 167, 134, 181, 231, 85, 78, 113,
            226, 180, 101, 122, 99, 38, 223, 109, 98, 224, 52, 63, 227, 65, 15, 27,
            243, 160, 127, 170, 91, 184, 58, 16, 76, 236, 49, 66, 124, 228, 33, 147,
            175, 111, 1, 23, 86, 198, 249, 55, 189, 110, 92, 195, 163, 152, 199, 182,
            81, 25, 46, 188, 148, 75, 88, 210, 172, 26, 162, 237, 251, 221, 186, 171
    };

    public int Gimme()
    {
        // Check if the index doesn't overflow
        Index = Index % 256;

        return (int)PRNG[Index++];
    }
}



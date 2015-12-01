//Copyright (C) 2015 Alexandre-Xavier Labonté-Lamoureux
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

#include <iostream>		// Console input-output
#include <string>		// Strings
#include <fstream>		// Read from file
#include <vector>		// Vectors for storage
#include <stdexcept>	// Exceptions

using namespace std;

int main(int argc, char *argv[])
{
	if (argc > 3)
	{
		cout << "Grid Level Maker, a tool to be used with KillBox." << endl;

		cout << "Creating containers to store the information." << endl;
		string Line;						// Stores the lines read fromthe file
		vector<char> Symbols;				// Stores the symbols
		vector<string> Elements;			// Describes the symbols on the map
		int Width;							// Map width
		int Height;							// Map Height
		int BlockSize;						// Size of a block
		int Elevation;						// Height of the walls (not used)
		const char Tab = '\t';				// Use to insert tabs

		cout << "Opening the 'def' file..." << endl;
		ifstream DefinitionFile(argv[1]);

		if (DefinitionFile.is_open())
		{
			cout << "Definition file opened." << endl;

			cout << "Reading definitions" << endl;
			while (getline(DefinitionFile, Line))
			{
				if (Line != "")
				{
					// Add symbol, a symbol should always be the first char on a line.
					Symbols.push_back(Line.at(0));

					// Find were the string defining the element starts.
					int ElementDefStartsAt = 1;

					if (Line.at(1) == ':')
					{
						ElementDefStartsAt++;
					}
					if (Line.at(2) == ' ')
					{
						ElementDefStartsAt++;
					}

					// Get the type of the element. 
					Elements.push_back(Line.substr(ElementDefStartsAt, Line.size() - ElementDefStartsAt));
				}
			}

			cout << "Closing definition file... ";
			DefinitionFile.close();
			cout << "Closed." << endl;

			if (Symbols.size() == 0 || Symbols.size() == 0)
			{
				cout << "ERROR: No definitions" << endl;
			}
			else if (Symbols.size() == Elements.size())
			{
				// Open the grid
				ifstream MapGridFile(argv[2]);

				cout << "Opening the map file..." << endl;
				if (MapGridFile.is_open())
				{
					cout << "Map file opened." << endl;

					cout << "Reading the first line..." << endl;
					getline(MapGridFile, Line);

					try
					{
						// Find the first number in this string
						int Pos = Line.find("x");
						Width = stoi(Line.substr(0, Pos));
						Line.erase(0, Pos + 1);

						// Find the second number
						Pos = Line.find("x");
						Height = stoi(Line.substr(0, Pos));
						Line.erase(0, Pos + 1);

						// Get the third number
						BlockSize = stoi(Line);

						// Create the level file to write to
						ofstream OutputMap;
						OutputMap.open(argv[3]);

						// Read line by line to load the level
						int NumberOfReadLine = 0;
						while (getline(MapGridFile, Line))
						{
							NumberOfReadLine++;

							if (NumberOfReadLine <= Height)
							{
								if (Line.size() >= Width)
								{
									for (int HorizontalPos = 0; HorizontalPos < Width; HorizontalPos++)
									{
										// Get a character from the grid and create a 3D block.
										int Search = 0;
										bool Found = false;
										string Element = "";

										if (Line[HorizontalPos] == ' ')
										{
											// This is an empty block
											continue;
										}

										while (Search < Symbols.size())
										{
											// Search for the symbol in our symbol list
											if (Line[HorizontalPos] == Symbols.at(Search))
											{
												Found = true;

												// Get the element's name
												Element = Elements.at(Search);

												break;
											}

											Search++;
										}

										if (!Found)
										{
											// Symbol was not found...
											cout << "ERROR: A symbol was not defined. X=" << HorizontalPos << " Y=" << NumberOfReadLine << endl;
										}
										// Check if the element name has an extension. 
										else if (Element.size() > 4 && Element.at(Element.size() - 4) == '.')
										{
											// It's a wall

											// Say which element it is
											OutputMap << "wall_forblock_x" << HorizontalPos << "_y" << NumberOfReadLine << "_left" << ":" << endl;
											OutputMap << "{" << endl;

											// Set wall's properties
											OutputMap << Tab << "2sided: false;" << endl;
											OutputMap << Tab << "impassable: true;" << endl;
											OutputMap << Tab << "texture: " << Element << ";" << endl;

											// Left side of a block
											// Vertex #1
											OutputMap << Tab << "x: " << HorizontalPos * BlockSize << ";" << endl;
											OutputMap << Tab << "y: -" << NumberOfReadLine * BlockSize << ";" << endl;
											OutputMap << Tab << "z: " << 0 << ";" << endl;

											// Vertex #2
											OutputMap << Tab << "x: " << HorizontalPos * BlockSize << ";" << endl;
											OutputMap << Tab << "y: -" << NumberOfReadLine * BlockSize + BlockSize << ";" << endl;
											OutputMap << Tab << "z: " << 0 << ";" << endl;

											// Vertex #3
											OutputMap << Tab << "x: " << HorizontalPos * BlockSize << ";" << endl;
											OutputMap << Tab << "y: -" << NumberOfReadLine * BlockSize + BlockSize << ";" << endl;
											OutputMap << Tab << "z: " << BlockSize << ";" << endl;

											// Vertex #4
											OutputMap << Tab << "x: " << HorizontalPos * BlockSize << ";" << endl;
											OutputMap << Tab << "y: -" << NumberOfReadLine * BlockSize << ";" << endl;
											OutputMap << Tab << "z: " << BlockSize << ";" << endl;

											// Close the element. 
											OutputMap << "}" << endl;
											OutputMap << endl;

											// Upper side of a block
											// Describe the element
											OutputMap << "wall_forblock_x" << HorizontalPos << "_y" << NumberOfReadLine << "_top" << ":" << endl;
											OutputMap << "{" << endl;

											// Set wall's properties
											OutputMap << Tab << "2sided: false;" << endl;
											OutputMap << Tab << "impassable: true;" << endl;
											OutputMap << Tab << "texture: " << Element << ";" << endl;

											// Vertex #1
											OutputMap << Tab << "x: " << HorizontalPos * BlockSize + BlockSize << ";" << endl;
											OutputMap << Tab << "y: -" << NumberOfReadLine * BlockSize << ";" << endl;
											OutputMap << Tab << "z: " << 0 << ";" << endl;

											// Vertex #2
											OutputMap << Tab << "x: " << HorizontalPos * BlockSize << ";" << endl;
											OutputMap << Tab << "y: -" << NumberOfReadLine * BlockSize << ";" << endl;
											OutputMap << Tab << "z: " << 0 << ";" << endl;

											// Vertex #3
											OutputMap << Tab << "x: " << HorizontalPos * BlockSize << ";" << endl;
											OutputMap << Tab << "y: -" << NumberOfReadLine * BlockSize << ";" << endl;
											OutputMap << Tab << "z: " << BlockSize << ";" << endl;

											// Vertex #4
											OutputMap << Tab << "x: " << HorizontalPos * BlockSize + BlockSize << ";" << endl;
											OutputMap << Tab << "y: -" << NumberOfReadLine * BlockSize << ";" << endl;
											OutputMap << Tab << "z: " << BlockSize << ";" << endl;

											// Close the element. 
											OutputMap << "}" << endl;
											OutputMap << endl;

											// Right side of a block
											// Describe the element
											OutputMap << "wall_forblock_x" << HorizontalPos << "_y" << NumberOfReadLine << "_right" << ":" << endl;
											OutputMap << "{" << endl;

											// Set wall's properties
											OutputMap << Tab << "2sided: false;" << endl;
											OutputMap << Tab << "impassable: true;" << endl;
											OutputMap << Tab << "texture: " << Element << ";" << endl;

											// Vertex #1
											OutputMap << Tab << "x: " << HorizontalPos * BlockSize + BlockSize << ";" << endl;
											OutputMap << Tab << "y: -" << NumberOfReadLine * BlockSize + BlockSize << ";" << endl;
											OutputMap << Tab << "z: " << 0 << ";" << endl;

											// Vertex #2
											OutputMap << Tab << "x: " << HorizontalPos * BlockSize + BlockSize << ";" << endl;
											OutputMap << Tab << "y: -" << NumberOfReadLine * BlockSize << ";" << endl;
											OutputMap << Tab << "z: " << 0 << ";" << endl;

											// Vertex #3
											OutputMap << Tab << "x: " << HorizontalPos * BlockSize + BlockSize << ";" << endl;
											OutputMap << Tab << "y: -" << NumberOfReadLine * BlockSize << ";" << endl;
											OutputMap << Tab << "z: " << BlockSize << ";" << endl;

											// Vertex #4
											OutputMap << Tab << "x: " << HorizontalPos * BlockSize + BlockSize << ";" << endl;
											OutputMap << Tab << "y: -" << NumberOfReadLine * BlockSize + BlockSize << ";" << endl;
											OutputMap << Tab << "z: " << BlockSize << ";" << endl;

											// Close the element. 
											OutputMap << "}" << endl;
											OutputMap << endl;

											// Bottom side of a block
											// Describe the element
											OutputMap << "wall_forblock_x" << HorizontalPos << "_y" << NumberOfReadLine << "_bottom" << ":" << endl;
											OutputMap << "{" << endl;

											// Set wall's properties
											OutputMap << Tab << "2sided: false;" << endl;
											OutputMap << Tab << "impassable: true;" << endl;
											OutputMap << Tab << "texture: " << Element << ";" << endl;

											// Vertex #1
											OutputMap << Tab << "x: " << HorizontalPos * BlockSize << ";" << endl;
											OutputMap << Tab << "y: -" << NumberOfReadLine * BlockSize + BlockSize << ";" << endl;
											OutputMap << Tab << "z: " << 0 << ";" << endl;

											// Vertex #2
											OutputMap << Tab << "x: " << HorizontalPos * BlockSize + BlockSize << ";" << endl;
											OutputMap << Tab << "y: -" << NumberOfReadLine * BlockSize + BlockSize << ";" << endl;
											OutputMap << Tab << "z: " << 0 << ";" << endl;

											// Vertex #3
											OutputMap << Tab << "x: " << HorizontalPos * BlockSize + BlockSize << ";" << endl;
											OutputMap << Tab << "y: -" << NumberOfReadLine * BlockSize + BlockSize << ";" << endl;
											OutputMap << Tab << "z: " << BlockSize << ";" << endl;

											// Vertex #4
											OutputMap << Tab << "x: " << HorizontalPos * BlockSize << ";" << endl;
											OutputMap << Tab << "y: -" << NumberOfReadLine * BlockSize + BlockSize << ";" << endl;
											OutputMap << Tab << "z: " << BlockSize << ";" << endl;
										}
										// Check if were are not seeking for an invalid item
										else if (Search < Elements.size())
										{
											// It's a thing

											// Say which element it is
											OutputMap << "# Thing at X=" << HorizontalPos << " and Y=" << NumberOfReadLine << endl;
											OutputMap << Elements.at(Search) << ":" << endl;
											OutputMap << "{" << endl;

											// Set thing's properties
											OutputMap << Tab << "x: " << HorizontalPos * BlockSize + BlockSize / 2 << ";" << endl;

											OutputMap << Tab << "y: -" << NumberOfReadLine * BlockSize + BlockSize / 2 << ";" << endl;

											OutputMap << Tab << "z: 0;" << endl;

											if (Element.find("spawn"))
											{
												OutputMap << Tab << "angle: 90;" << endl;
											}
										}

										// Close the element. 
										OutputMap << "}" << endl;
										OutputMap << endl;
									}
								}
								else
								{
									cout << "ERROR: Not enough elements on line for the specified grid width." << endl;
								}
							}
							else
							{
								cout << "ERROR: Read to much lines for the specified map height." << endl;
							}
						}

						// Do the floors and ceilings
						// But first, do the floors
						if (argc > 4)
						{
							ifstream FloorGrid(argv[4]);

							// Open the floors file
							if (FloorGrid.is_open())
							{
								cout << "Building floors..." << endl;

								// Read line by line the floors to add to the level
								int NumberOfReadLine = 0;
								while (getline(FloorGrid, Line))
								{
									NumberOfReadLine++;

									if (NumberOfReadLine <= Height)
									{
										if (Line.size() >= Width)
										{
											for (int HorizontalPos = 0; HorizontalPos < Width; HorizontalPos++)
											{
												// Get a character from the grid and create a floor.
												int Search = 0;
												bool Found = false;
												string Element = "";

												if (Line[HorizontalPos] == ' ')
												{
													// This is an empty block
													continue;
												}

												while (Search < Symbols.size())
												{
													// Search for the symbol in our symbol list
													if (Line[HorizontalPos] == Symbols.at(Search))
													{
														Found = true;

														// Get the element's name
														Element = Elements.at(Search);

														break;
													}

													Search++;
												}

												if (!Found)
												{
													// Symbol was not found...
													cout << "ERROR: A symbol was not defined. X=" << HorizontalPos << " Y=" << NumberOfReadLine << endl;
												}
												// Check if the element name has an extension. 
												else if (Element.size() > 4 && Element.at(Element.size() - 4) == '.')
												{
													// It's a floor

													// Say which element it is
													OutputMap << "floor_forblock_x" << HorizontalPos << "_y" << NumberOfReadLine << ":" << endl;
													OutputMap << "{" << endl;

													// Set floor's properties
													OutputMap << Tab << "2sided: false;" << endl;
													OutputMap << Tab << "impassable: false;" << endl;
													OutputMap << Tab << "texture: " << Element << ";" << endl;

													// Vertex #1
													OutputMap << Tab << "x: " << HorizontalPos * BlockSize << ";" << endl;
													OutputMap << Tab << "y: -" << NumberOfReadLine * BlockSize + BlockSize << ";" << endl;
													OutputMap << Tab << "z: " << 0 << ";" << endl;

													// Vertex #2
													OutputMap << Tab << "x: " << HorizontalPos * BlockSize + BlockSize << ";" << endl;
													OutputMap << Tab << "y: -" << NumberOfReadLine * BlockSize + BlockSize << ";" << endl;
													OutputMap << Tab << "z: " << 0 << ";" << endl;

													// Vertex #3
													OutputMap << Tab << "x: " << HorizontalPos * BlockSize + BlockSize << ";" << endl;
													OutputMap << Tab << "y: -" << NumberOfReadLine * BlockSize << ";" << endl;
													OutputMap << Tab << "z: " << 0 << ";" << endl;

													// Vertex #4
													OutputMap << Tab << "x: " << HorizontalPos * BlockSize << ";" << endl;
													OutputMap << Tab << "y: -" << NumberOfReadLine * BlockSize << ";" << endl;
													OutputMap << Tab << "z: " << 0 << ";" << endl;

													// Close the element. 
													OutputMap << "}" << endl;
													OutputMap << endl;
												}
												// Check if were are not seeking for an invalid item
												else
												{
													cout << "ERROR: Bad element read from floors file." << endl;
												}
											}
										}
										else
										{
											cout << "ERROR: Not enough elements on line for the specified grid width." << endl;
										}
									}
									else
									{
										cout << "ERROR: Read to much lines for the specified map height." << endl;
									}
								}

								// Close the file
								FloorGrid.close();
							}
						}

						// Now, do the ceilings
						if (argc > 5)
						{
							ifstream CeilingGrid(argv[5]);

							// Open the floors file
							if (CeilingGrid.is_open())
							{
								cout << "Building ceilings..." << endl;

								// Read line by line the floors to add to the level
								int NumberOfReadLine = 0;
								while (getline(CeilingGrid, Line))
								{
									NumberOfReadLine++;

									if (NumberOfReadLine <= Height)
									{
										if (Line.size() >= Width)
										{
											for (int HorizontalPos = 0; HorizontalPos < Width; HorizontalPos++)
											{
												// Get a character from the grid and create a ceiling.
												int Search = 0;
												bool Found = false;
												string Element = "";

												if (Line[HorizontalPos] == ' ')
												{
													// This is an empty block
													continue;
												}

												while (Search < Symbols.size())
												{
													// Search for the symbol in our symbol list
													if (Line[HorizontalPos] == Symbols.at(Search))
													{
														Found = true;

														// Get the element's name
														Element = Elements.at(Search);

														break;
													}

													Search++;
												}

												if (!Found)
												{
													// Symbol was not found...
													cout << "ERROR: A symbol was not defined. X=" << HorizontalPos << " Y=" << NumberOfReadLine << endl;
												}
												// Check if the element name has an extension. 
												else if (Element.size() > 4 && Element.at(Element.size() - 4) == '.')
												{
													// It's a ceiling

													// Say which element it is
													OutputMap << "ceiling_forblock_x" << HorizontalPos << "_y" << NumberOfReadLine << ":" << endl;
													OutputMap << "{" << endl;

													// Set ceiling's properties
													OutputMap << Tab << "2sided: false;" << endl;
													OutputMap << Tab << "impassable: false;" << endl;
													OutputMap << Tab << "texture: " << Element << ";" << endl;

													// Vertex #1
													OutputMap << Tab << "x: " << HorizontalPos * BlockSize + BlockSize << ";" << endl;
													OutputMap << Tab << "y: -" << NumberOfReadLine * BlockSize + BlockSize << ";" << endl;
													OutputMap << Tab << "z: " << 64 << ";" << endl;

													// Vertex #2
													OutputMap << Tab << "x: " << HorizontalPos * BlockSize << ";" << endl;
													OutputMap << Tab << "y: -" << NumberOfReadLine * BlockSize + BlockSize << ";" << endl;
													OutputMap << Tab << "z: " << 64 << ";" << endl;

													// Vertex #3
													OutputMap << Tab << "x: " << HorizontalPos * BlockSize << ";" << endl;
													OutputMap << Tab << "y: -" << NumberOfReadLine * BlockSize << ";" << endl;
													OutputMap << Tab << "z: " << 64 << ";" << endl;

													// Vertex #4
													OutputMap << Tab << "x: " << HorizontalPos * BlockSize + BlockSize << ";" << endl;
													OutputMap << Tab << "y: -" << NumberOfReadLine * BlockSize << ";" << endl;
													OutputMap << Tab << "z: " << 64 << ";" << endl;

													// Close the element. 
													OutputMap << "}" << endl;
													OutputMap << endl;
												}
												// Check if were are not seeking for an invalid item
												else
												{
													cout << "ERROR: Bad element read from ceilings file." << endl;
												}
											}
										}
										else
										{
											cout << "ERROR: Not enough elements on line for the specified grid width." << endl;
										}
									}
									else
									{
										cout << "ERROR: Read to much lines for the specified map height." << endl;
									}
								}

								// Close the file
								CeilingGrid.close();
							}
						}

						// Close the output map
						OutputMap.close();
					}
					catch (...)
					{
						cout << "ERROR: An exception has occured. Your text files don't follow the good format." << endl;
					}

					cout << "Closing the map file... ";
					MapGridFile.close();
					cout << "Closed. " << endl;
				}
				else
				{
					cout << "Unable to open the map file.";
				}
			}
			else
			{
				cout << "ERROR: " << Symbols.size() << " Symbols != " << Elements.size() << " Definitions" << endl;
			}
		}
		else
		{
			cout << "Unable to open the definition file.";
		}
	}
	else
	{
		cout << "Grid Level Maker" << endl << endl;
		cout << "This tool converts grid level to a mesh a polygons for KillBox." << endl;
		cout << "How to use: Open a console or a terminal and invoke the program." << endl << endl;
		cout << "GridLevelMaker.exe [def] [lvl] [out] [flr] [cel]" << endl << endl;
		cout << "The 'def' file contains the definitions of objects on the grid." << endl;
		cout << "The 'lvl' file contains the objects in a grid." << endl;
		cout << "The 'out' file is were the resulting polygonal level will go."  << endl;
		cout << "The 'flr' file is optinal and is used for the floors." << endl;
		cout << "The 'cel' file is optinal and is used for the ceilings." << endl << endl;
	}
}
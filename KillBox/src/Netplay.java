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

import java.io.*;
import java.net.*;
import java.util.*;

public class Netplay
{
    int Nodes_;
    final int PORT = 8167;
    final int WAIT = 120000;	// 2 minutes
    ServerSocket Server = null;
    ArrayList<Socket> Connections = new ArrayList<Socket>();

    public Netplay(boolean IamServer, int Nodes)
    {
        if (IamServer)
        {
            Nodes_ = Nodes;

            // Check if we are in a mutliplayer game
            if (Nodes > 1)
            {
                try
                {
                    Server = new ServerSocket(PORT);
                }
                catch (IOException ioe)
                {
                    System.err.println(ioe);
                }

                try
                {
                    // Wait for others to connect
                    Server.setSoTimeout(WAIT);
                }
                catch (SocketException se)
                {
                    System.err.println(se);
                }

                try
                {
                    for (int i = 1; i < Nodes_; i++)
                    {
                        Socket Client = Server.accept();
                        System.err.println("A client has connected");
                        Connections.add(Client);
                    }
                }
                catch (IOException ioe)
                {
                    System.err.println(ioe);
                }
            }
        }
        else
        {
            // Connect to a server

        }
    }
}

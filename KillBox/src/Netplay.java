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
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.ServerSocket;

public class Netplay
{
    public class NetCommand
    {
        public int Number;
        public byte PlayerNumber;
        public short AngleDiff;
        public byte FaceMove;
        public byte SideMove;
        public int Actions;
        public int CheckSum;
        public String Chat;

        public NetCommand(byte PlayerNumber)
        {
            this.Number = 0;
            this.AngleDiff = 0;
            this.FaceMove = 0;
            this.SideMove = 0;
            this.Actions = 0;
            this.CheckSum = 0;
            this.Chat = " ";
            this.PlayerNumber = PlayerNumber;
        }

        public void Update(short AngleDiff, byte FaceMove, byte SideMove, int Actions, int CheckSum, String Chat)
        {
            this.AngleDiff = AngleDiff;
            this.FaceMove = FaceMove;
            this.SideMove = SideMove;
            this.Actions = Actions;
            this.CheckSum = CheckSum;
            this.Chat = Chat;
        }

        public void UpdateAngleDiff(short AngleDiff)
        {
            this.AngleDiff = AngleDiff;
        }

        public void UpdateForwardMove(byte FaceMove)
        {
            this.FaceMove = FaceMove;
        }

        public void UpdateSideMove(byte SideMove)
        {
            this.SideMove = SideMove;
        }

        public void UpdateAction(int Actions)
        {
            this.Actions = Actions;
        }

        public void UpdateCheckSum(int CheckSum)
        {
            this.CheckSum = CheckSum;
        }

        public void UpdateChat(String Chat)
        {
            this.Chat = Chat;
        }

        public void UpdatePlayerViaNetCommand(Player Plyr)
        {
            Plyr.ForwardMove(FaceMove);

            Plyr.LateralMove(SideMove);
        }

        public void Reset()
        {
            this.AngleDiff = 0;
            this.FaceMove = 0;
            this.SideMove = 0;
            this.Actions = 0;
            this.CheckSum = 0;
            this.Chat = " ";
        }

        // Print NetCommand
        public String toString()
        {
            return  Number + Separator + PlayerNumber + Separator + AngleDiff + Separator + FaceMove +
                    Separator + SideMove + Separator + Actions + Separator + CheckSum + Separator + Chat;
        }
    }

    // Object NetCommand
    NetCommand PlayerCommand = null;

    // For now, only for one player
    ArrayList<NetCommand> OtherPlayersCommand = new ArrayList<NetCommand>();

    int Nodes = 1;
    String ServerAddress = "127.0.0.1";
    final int Port = 8167;
    final int Wait = 120000;	// 2 minutes

    private String Chat = null;
    String Line = null;

    public int View = 0;

    ServerSocket Server = null;
    ArrayList<Socket> Connections = new ArrayList<Socket>();

    public BufferedReader Reader = null;
    public PrintWriter Writer = null;

    private String Separator = ";";

	// Constructor for the clients
	public Netplay(int Nodes, String IpAddress)
	{
		ServerAddress = IpAddress;

		// Connect to a server, because I'm not a server. I'm a client.
		InetSocketAddress AdressSocket = null;
		int NombreJoueurRestant, NombreJoueur, NumeroJoueur = 0;

		try
		{
			AdressSocket = new InetSocketAddress(IpAddress, Port);
			// Add his socket to his array lsit
			Connections.add(new Socket());
			// Connect to the server
			Connections.get(0).connect(AdressSocket);

			Writer = new PrintWriter(new OutputStreamWriter(Connections.get(0).getOutputStream()));
			Reader = new BufferedReader(new InputStreamReader(Connections.get(0).getInputStream()));

			// The player receive a string which is the number of player in the game
			// and the number of player we need to start the game.
			String Message = Reader.readLine();
			String[] strings = Message.split(Separator);

			NombreJoueur = Integer.parseInt(strings[0]);
			NombreJoueurRestant = Integer.parseInt(strings[1]);
			NumeroJoueur = Integer.parseInt(strings[2]);

			// It gives the right view to the right player
			View = NumeroJoueur;

			this.Nodes = NombreJoueur;

			System.out.println("Nombrejoueur: " + NombreJoueur);
			System.out.println("NombreJoueurRestant: " + NombreJoueurRestant);
		}
		catch(IOException e)
		{
			System.err.println(e);
			System.err.println("Problem to connect a client. Try to start a server!");
		}

		PlayerCommand = new NetCommand((byte)NumeroJoueur/*Player number*/);
		OtherPlayersCommand.add(new NetCommand((byte)1));
	}

	// Constructor for the server
    public Netplay(int Nodes)
    {
		this.Nodes = Nodes;

		// Check if we are in a mutliplayer game
		if (Nodes > 1)
		{
			try
			{
				Server = new ServerSocket(Port);
			}
			catch (IOException ioe)
			{
				System.err.println(ioe);
			}

			try
			{
				// Wait for others to connect
				Server.setSoTimeout(Wait);
			}
			catch (SocketException se)
			{
				System.err.println(se);
			}

			try
			{
				// Server is 0
				PlayerCommand = new NetCommand((byte)0/*Player number*/);
				// The other player is 1
				OtherPlayersCommand.add(new NetCommand((byte)1));

				for (int Player = 1; Player < this.Nodes; Player++)
				{

					System.err.println("Waiting for " + (this.Nodes - Player) + " more nodes");
					Socket Client = Server.accept();


					System.err.println("A client has connected");
					Connections.add(Client);

					Writer = new PrintWriter(new OutputStreamWriter(Connections.get(0).getOutputStream()));
					Reader = new BufferedReader(new InputStreamReader(Connections.get(0).getInputStream()));

					// After a player is connected, we send the number of connections.
					// This way, we can start all the players at the same time.
					SendNodesToPlayer(this.Nodes - Player, this.Nodes,Player/*Player's number*/);
				}
			}
			catch (IOException ioe)
			{
				System.err.println(ioe);
			}
		}
	}

    public NetCommand GetReferenceNetCommand()
    {
        return PlayerCommand;
    }

    public void Update()
    {
        // Send his command to players
        try
        {
            // Write his command
            Writer.println(PlayerCommand);
            Writer.flush();

            // Read the server command
            this.PlayerCommand.Number++;
            Line = Reader.readLine();

            String[] strings = Line.split(Separator);
            // System.out.println(OtherPlayersCommand.get(0));
            OtherPlayersCommand.get(0).Number = Integer.parseInt(strings[0]);
            OtherPlayersCommand.get(0).PlayerNumber = Byte.parseByte(strings[1]);
            OtherPlayersCommand.get(0).AngleDiff = Short.parseShort(strings[2]);
            OtherPlayersCommand.get(0).FaceMove = Byte.parseByte(strings[3]);
            OtherPlayersCommand.get(0).SideMove = Byte.parseByte(strings[4]);
            OtherPlayersCommand.get(0).Actions = Integer.parseInt(strings[5]);
            OtherPlayersCommand.get(0).CheckSum = Integer.parseInt(strings[6]);
            OtherPlayersCommand.get(0).Chat = strings[7];

            // print the command (for debug)!

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void SendNodesToPlayer(int NombreJoueurRestant, int NombreJoueur,int PlayerNumber)
    {
        String Message = Integer.toString(NombreJoueur) + Separator + Integer.toString(NombreJoueurRestant) + Separator + Integer.toString(PlayerNumber);

        for(int Player = 0; Player < Connections.size(); Player++)
        {
            try
            {
                if (Connections.get(Player).isConnected())
                {
                    PrintWriter PlayerWriter = new PrintWriter(new OutputStreamWriter(Connections.get(Player).getOutputStream()));

                    PlayerWriter.println(Message);
                    PlayerWriter.flush();
                }
            }
            catch(Exception e)
            {
                System.err.println(e.getMessage());
            }
        }
    }

    public void SendMessageToPlayer(String message)
    {
        for(int Player = 0; Player < Connections.size(); Player++)
        {
            Socket player = Connections.get(Player);
            try
            {
                if (player.isConnected())
                {
                    System.out.println("It's comming!");
                    PrintWriter PlayerWriter = new PrintWriter(new OutputStreamWriter(player.getOutputStream()));

                    PlayerWriter.println(message);
                    PlayerWriter.flush();
                }
            }
            catch(Exception e)
            {
                System.err.println(e.getMessage());
            }
        }
    }
}

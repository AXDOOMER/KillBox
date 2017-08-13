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

import java.io.*;
import java.net.*;
import java.util.*;

public class Netplay
{
	public static int FrameRate = Game.FrameRate;

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

	// Game conditions
	int GameMode = 0;
	int TimeLimit = 0;
	int KillLimit = 0;

	String Map = "";

	public int GetTimeLimit()
	{
		if (TimeLimit <= 0)
		{
			return Integer.MAX_VALUE;
		}

		return TimeLimit * 60;
	}

	public int GetKillLimit()
	{
		if (KillLimit <= 0)
		{
			return Integer.MAX_VALUE;
		}

		return KillLimit;
	}

	// For now, only for one player
	ArrayList<NetCommand> OtherPlayersCommand = new ArrayList<NetCommand>();

	int Nodes = 1;
	String ServerAddress = "127.0.0.1";
	final int Port = 8167;
	final int Wait = 120000;	// 2 minutes
	final int WaitLag = 5000;

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
		int RemainingNumberOfPlayers, NumberOfPlayers, PlayerNumber = 0;

		try
		{
			AdressSocket = new InetSocketAddress(IpAddress, Port);
			// Add his socket to his array list
			Connections.add(new Socket());
			// Connect to the server
			Connections.get(0).connect(AdressSocket);

			Writer = new PrintWriter(new OutputStreamWriter(Connections.get(0).getOutputStream()));
			Reader = new BufferedReader(new InputStreamReader(Connections.get(0).getInputStream()));

			// The player receive a string which is the number of player in the game
			// and the number of player we need to start the game.
			String Message = Reader.readLine();
			String[] strings = Message.split(Separator);

			NumberOfPlayers = Integer.parseInt(strings[0]);
			RemainingNumberOfPlayers = Integer.parseInt(strings[1]);
			PlayerNumber = Integer.parseInt(strings[2]);
			this.GameMode = Integer.parseInt(strings[3]);
			this.TimeLimit = Integer.parseInt(strings[4]);
			this.KillLimit = Integer.parseInt(strings[5]);
			this.Map = strings[6];

			Connections.get(0).setSoTimeout(WaitLag);

			// It gives the right view to the right player
			View = PlayerNumber;

			this.Nodes = NumberOfPlayers;

			if (NumberOfPlayers < 2)
			{
				System.out.println("Number of player: " + NumberOfPlayers);
			}
			else
			{
				System.out.println("Number of players: " + NumberOfPlayers);
			}

			if (RemainingNumberOfPlayers < 2)
			{
				System.out.println("Remaining player: " + RemainingNumberOfPlayers);
			}
			else
			{
				System.out.println("Remaining players: " + RemainingNumberOfPlayers);
			}
		}
		catch (SocketTimeoutException sto)
		{
			System.err.println("Other player may have disconnected or connection was interrupted.");
		}
		catch(IOException e)
		{
			System.err.println(e.getStackTrace());
			System.err.println("Problem when trying to connect to a client. Try to start a server first!");
		}

		PlayerCommand = new NetCommand((byte)PlayerNumber/*Player number*/);
		OtherPlayersCommand.add(new NetCommand((byte)0));
	}

	// Constructor for the server
	public Netplay(int Nodes)
	{
		this.Nodes = Nodes;

		// Check if we are in a multiplayer game
		if (Nodes > 1)
		{
			try
			{
				Server = new ServerSocket(Port);
			}
			catch (IOException ioe)
			{
				System.err.println(ioe.getStackTrace());
			}

			try
			{
				// Wait for others to connect
				Server.setSoTimeout(Wait);
			}
			catch (SocketException se)
			{
				System.err.println(se.getStackTrace());
			}

			try
			{
				// Server is 0
				PlayerCommand = new NetCommand((byte)0/*Player number*/);
				// The other player is 1
				OtherPlayersCommand.add(new NetCommand((byte)1));

				for (int Player = 1; Player < this.Nodes; Player++)
				{

					System.err.println("Waiting for " + (this.Nodes - Player) + " more node(s)...");
					Socket Client = Server.accept();

					Client.setSoTimeout(WaitLag);

					System.err.println("A client has connected.");
					Connections.add(Client);

					Writer = new PrintWriter(new OutputStreamWriter(Connections.get(0).getOutputStream()));
					Reader = new BufferedReader(new InputStreamReader(Connections.get(0).getInputStream()));

					// After a player is connected, we send the number of connections.
					// This way, we can start all the players at the same time.
					SendNodesToPlayer(this.Nodes - Player, this.Nodes, Player/*Player's number*/);
				}
			}
			catch (SocketTimeoutException sto)
			{
				System.out.println("Other player may have disconnected or connection was interrupted.");
			}
			catch (IOException ioe)
			{
				System.err.println(ioe);
			}
		}
	}

	// Constructor for the server with game condition
	public Netplay(int Nodes, int NewGameMode, int NewTimeLimit, int NewKillLimit, String NewMap)
	{
		this.Nodes = Nodes;

		// Initialize game limits
		this.TimeLimit = NewTimeLimit;
		this.KillLimit = NewKillLimit;
		this.GameMode = NewGameMode;

		// Check if we are in a multiplayer game
		if (Nodes > 1)
		{
			try
			{
				if (Server != null)
				{
					if (!Server.isClosed())
					{
						Server.close();
					}
				}

				// Close any connection that may remain open
				for (int Connection = 0; Connection < Connections.size(); Connection++)
				{
					if (Connections.get(Connection) != null)
					{
						if (!Connections.get(Connection).isClosed())
						{
							Connections.get(Connection).close();
						}
					}
				}

				Server = new ServerSocket(Port);

				// Wait for others to connect
				Server.setSoTimeout(Wait);
			}
			catch (SocketException se)
			{
				System.err.println("Socket problem. This port may already be in use. ");
				System.err.println(se.getMessage());
			}
			catch (IOException ioe)
			{
				System.err.println("Server Socket Input-Output Exception. ");
				System.err.println(ioe.getStackTrace());
				System.err.println(ioe.getMessage());
			}

			try
			{
				// Server is 0
				PlayerCommand = new NetCommand((byte)0/*Player number*/);
				// The other player is 1
				OtherPlayersCommand.add(new NetCommand((byte) 1));

				for (int Player = 1; Player < this.Nodes; Player++)
				{
					System.out.println("Waiting for " + (this.Nodes - Player) + " more nodes");

					Socket Client;

					if (Server != null)
					{
						Client = Server.accept();
						Client.setSoTimeout(WaitLag);

						System.out.println("A client has connected");
						Connections.add(Client);

						Writer = new PrintWriter(new OutputStreamWriter(Connections.get(0).getOutputStream()));
						Reader = new BufferedReader(new InputStreamReader(Connections.get(0).getInputStream()));

						// After a player is connected, we send the number of connections.
						// This way, we can start all the players at the same time.

						SendFirstMessageToPlayers(this.Nodes - Player, this.Nodes, Player, NewGameMode, NewTimeLimit, NewKillLimit, NewMap);
					}
					else
					{
						System.err.println("Server socket still 'null'. This means the connection wasn't established.");
					}
				}
			}
			catch (SocketTimeoutException sto)
			{
				System.err.println("Other player may have disconnected or connection was interrupted.");
			}
			catch (IOException ioe)
			{
				System.err.println("Client Socket Input-Output Exception.");
				System.err.println(ioe.getMessage());
				System.err.println(ioe);
			}
		}
	}

	public NetCommand GetReferenceNetCommand()
	{
		return PlayerCommand;
	}

	public boolean ServerSocketIsNull()
	{
		return (Server == null);
	}

	public boolean Update()
	{
		// Send his command to players
		try
		{
			// Write his command
			if (Writer != null)
			{
				Writer.println(PlayerCommand);
				Writer.flush();
			}
			else
			{
				return false;
			}

			// Read the other player's command
			if (Reader != null)
			{
				Line = Reader.readLine();
			}
			else
			{
				return false;
			}

			if (Line != null)
			{
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
			}
			else
			{
				return false;
			}
		}
		catch (SocketTimeoutException sto)
		{
			System.out.println("Other player may have disconnected or connection was interrupted.");
			sto.printStackTrace();
			return false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public void SendNodesToPlayer(int NumberOfPlayersRemaining, int NumberOfPlayers, int PlayerNumber)
	{
		String Message = Integer.toString(NumberOfPlayers) + Separator + Integer.toString(NumberOfPlayersRemaining) + Separator + Integer.toString(PlayerNumber);

		for (int Player = 0; Player < Connections.size(); Player++)
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
			catch (Exception e)
			{
				System.err.println(e.getStackTrace());
			}
		}
	}

	public void SendMessageToPlayer(String message)
	{
		for (int Player = 0; Player < Connections.size(); Player++)
		{
			Socket player = Connections.get(Player);
			try
			{
				if (player.isConnected())
				{
					PrintWriter PlayerWriter = new PrintWriter(new OutputStreamWriter(player.getOutputStream()));

					PlayerWriter.println(message);
					PlayerWriter.flush();
				}
			}
			catch (Exception e)
			{
				System.err.println(e.getStackTrace());
			}
		}
	}

	public void SendFirstMessageToPlayers(int NumberOfPlayersRemaining, int NumberOfPlayers, int PlayerNumber, int NewGameMode, int NewTimeLimit, int NewKillLimit, String NewMap)
	{
		String Message = Integer.toString(NumberOfPlayers) + Separator + Integer.toString(NumberOfPlayersRemaining) + Separator + Integer.toString(PlayerNumber) + Separator + Integer.toString(NewGameMode) + Separator + Integer.toString(NewTimeLimit) + Separator + Integer.toString(NewKillLimit)  + Separator + NewMap;

		for (int Player = 0; Player < Connections.size(); Player++)
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
			catch (Exception e)
			{
				System.err.println(e.getStackTrace());
			}
		}
	}

	public void TheServerScreen(Level Lvl, int Number, int Player)
	{
		// The server first (me)
		// Reload
		if (PlayerCommand.Actions / 1000 == 1)
		{
			Lvl.Players.get(View).ReloadWeapon();
			PlayerCommand.Actions -= 1000;
		}
		// Command to shot
		if (PlayerCommand.Actions / 100 == 1 || PlayerCommand.Actions / 100 == 11) // Do my command first
		{
			if (Lvl.Players.get(View).Health > 0 && !Lvl.Players.get(View).JustSpawned)
			{
				Lvl.Players.get(View).HitScan(Lvl.Players.get(View).GetRadianAngle(), 0, 10);
			}
			else if (Lvl.Players.get(View).JustSpawned)
			{
				Lvl.Players.get(View).JustSpawned = false;
			}
			else
			{
				// Check if the player has completely dropped on the floor
				if (Lvl.Players.get(View).ViewZ == Lvl.Players.get(View).HeadOnFloor)
				{
					// Spawn the player
					if (!Lvl.Players.get(View).SpawnAtRandomSpot(true))
					{
						System.err.println("Can't find a free spot to respawn. The map may not have enough of them.");
						System.exit(1);
					}
				}
			}

			PlayerCommand.Actions -= 100;
		}

		Lvl.Players.get(View).JustSpawned = false;

		// Change weapon 1
		Lvl.Players.get(View).ChangeWeapon(PlayerCommand.Actions);
		Lvl.Players.get(View).ExecuteChangeWeapon();
		PlayerCommand.Actions -= PlayerCommand.Actions;

		// The client second
		// Reload
		if (OtherPlayersCommand.get(Player).Actions / 1000 == 1)
		{
			Lvl.Players.get(Number).ReloadWeapon();
			OtherPlayersCommand.get(Player).Actions -= 1000;
		}
		if (OtherPlayersCommand.get(Player).Actions / 100 == 1 || OtherPlayersCommand.get(Player).Actions / 100 == 11) // Do the client command second
		{
			if (Lvl.Players.get(Number).Health > 0 && !Lvl.Players.get(Number).JustSpawned)
			{
				Lvl.Players.get(Number).HitScan(Lvl.Players.get(Number).GetRadianAngle(), 0, 10);
			}
			else if (Lvl.Players.get(Number).JustSpawned)
			{
				Lvl.Players.get(Number).JustSpawned = false;
			}
			else
			{
				// Check if the player has completely dropped on the floor
				if (Lvl.Players.get(Number).ViewZ == Lvl.Players.get(Number).HeadOnFloor)
				{
					// Spawn the player
					if (!Lvl.Players.get(Number).SpawnAtRandomSpot(true))
					{
						System.err.println("Can't find a free spot to respawn. The map may not have enough of them.");
						System.exit(1);
					}
				}
			}

			OtherPlayersCommand.get(Player).Actions -= 100;
		}

		Lvl.Players.get(Number).JustSpawned = false;

		// Change weapon 1
		Lvl.Players.get(Number).ChangeWeapon(OtherPlayersCommand.get(Player).Actions);
		Lvl.Players.get(Number).ExecuteChangeWeapon();
		OtherPlayersCommand.get(Player).Actions -= OtherPlayersCommand.get(Player).Actions;
	}

	public void TheClientScreen(Level Lvl, int Number, int Player)
	{
		// The server first
		// Reload
		if (OtherPlayersCommand.get(Player).Actions / 1000 == 1)
		{
			Lvl.Players.get(Number).ReloadWeapon();
			OtherPlayersCommand.get(Player).Actions -= 1000;
		}
		if (OtherPlayersCommand.get(Player).Actions / 100 == 1 || OtherPlayersCommand.get(Player).Actions / 100 == 11) // Do the client command second
		{
			if (Lvl.Players.get(Number).Health > 0 && !Lvl.Players.get(Number).JustSpawned)
			{
				Lvl.Players.get(Number).HitScan(Lvl.Players.get(Number).GetRadianAngle(), 0, 10);
			}
			else if (Lvl.Players.get(Number).JustSpawned)
			{
				Lvl.Players.get(Number).JustSpawned = false;
			}
			else
			{
				// Check if the player has completely dropped on the floor
				if (Lvl.Players.get(Number).ViewZ == Lvl.Players.get(Number).HeadOnFloor)
				{
					// Spawn the player
					if (!Lvl.Players.get(Number).SpawnAtRandomSpot(true))
					{
						System.err.println("Can't find a free spot to respawn. The map may not have enough of them.");
						System.exit(1);
					}
				}
			}

			OtherPlayersCommand.get(Player).Actions -= 100;
		}

		Lvl.Players.get(Number).JustSpawned = false;

		// Change weapon 1
		Lvl.Players.get(Number).ChangeWeapon(OtherPlayersCommand.get(Player).Actions);
		Lvl.Players.get(Number).ExecuteChangeWeapon();
		OtherPlayersCommand.get(Player).Actions -= OtherPlayersCommand.get(Player).Actions;

		// The client second (me)
		// Reload
		if (PlayerCommand.Actions / 1000 == 1)
		{

			Lvl.Players.get(View).ReloadWeapon();
			PlayerCommand.Actions -= 1000;
		}
		// Command to shot
		if (PlayerCommand.Actions / 100 == 1 || PlayerCommand.Actions / 100 == 11) // Do my command first
		{
			if (Lvl.Players.get(View).Health > 0 && !Lvl.Players.get(View).JustSpawned)
			{
				Lvl.Players.get(View).HitScan(Lvl.Players.get(View).GetRadianAngle(), 0, 10);
			}
			else if (Lvl.Players.get(View).JustSpawned)
			{
				Lvl.Players.get(View).JustSpawned = false;
			}
			else
			{
				// Check if the player has completely dropped on the floor
				if (Lvl.Players.get(View).ViewZ == Lvl.Players.get(View).HeadOnFloor)
				{
					// Spawn the player
					if (!Lvl.Players.get(View).SpawnAtRandomSpot(true))
					{
						System.err.println("Can't find a free spot to respawn. The map may not have enough of them.");
						System.exit(1);
					}
				}
			}
			PlayerCommand.Actions -= 100;
		}

		Lvl.Players.get(View).JustSpawned = false;

		// Change weapon 1
		Lvl.Players.get(View).ChangeWeapon(PlayerCommand.Actions);
		Lvl.Players.get(View).ExecuteChangeWeapon();
		PlayerCommand.Actions -= PlayerCommand.Actions;
	}

	public void AllTheCommunication(Level Lvl, Camera HeadCamera, int TicksCount)
	{
		if (Update())
		{
			// Update the other player movements
			for (int Player = 0; Player < OtherPlayersCommand.size(); Player++)
			{
				int Number = OtherPlayersCommand.get(Player).PlayerNumber;

				if (TicksCount > 1)
				{
					// Not allowing movements on the first tick prevents a bug when player may move before the game has even started.
					Lvl.Players.get(Number).ForwardMove(OtherPlayersCommand.get(Player).FaceMove);
					Lvl.Players.get(Number).LateralMove(OtherPlayersCommand.get(Player).SideMove);
					Lvl.Players.get(Number).AngleTurn(OtherPlayersCommand.get(Player).AngleDiff);
					Lvl.Players.get(Number).Action = PlayerCommand.Actions;

					// Move the server(player1) first and then the client(player2)
					for (int PlayerToMove = 0; PlayerToMove < Lvl.Players.size(); PlayerToMove++)
					{
						Lvl.Players.get(PlayerToMove).ExecuteMove(Lvl.Players.get(PlayerToMove).FrontMove, Lvl.Players.get(PlayerToMove).SideMove);
						Lvl.Players.get(PlayerToMove).ExecuteAngleTurn(Lvl.Players.get(PlayerToMove).AngleDiff);
					}
				}
				else // To enable the first shot at the start
				{
					Lvl.Players.get(Player).JustSpawned = false;
				}

				// Check the action of each player in order (Player1 then Player2)
				if (View == 0) // If I'm the server
				{
					TheServerScreen(Lvl, Number, Player);
				}
				else // If I'm the client
				{
					TheClientScreen(Lvl, Number, Player);
				}

				PlayerCommand.Actions = 0;
				OtherPlayersCommand.get(0).Actions = 0;

			}
		}
		else
		{
			if (HeadCamera.Menu.InGame)
			{
				HeadCamera.Menu.NewMessageToShow("Multi-player game ended.");
				TestEndGame(this, Lvl, HeadCamera);
			}
			HeadCamera.Menu.InGame = false;
		}
	}

	static void TestEndGame(Netplay NetContext, Level Lvl, Camera Cam)
	{
		// Look for a winner
		int Winner = 0;     // Player that wins any of the game modes
		int PlayerWithMostKills = 0;    // Player that has the most kills
		boolean Tie = true;    // If some players have the same score

		// Find the player with the most kills.
		for (int Player = 0; Player < Lvl.Players.size(); Player++)
		{
			if (Lvl.Players().get(Player).Kills > Lvl.Players().get(0).Kills)
			{
				PlayerWithMostKills = Player;
			}
		}

		// Check if someone may have won. First check if the game is infinite.
		if (NetContext.TimeLimit != 0 || NetContext.KillLimit != 0)
		{
			// Check if we have one of the two cases where the game would end
			if (Game.TicksCount / FrameRate >= NetContext.GetTimeLimit() || Lvl.Players().get(PlayerWithMostKills).Kills >= NetContext.GetKillLimit())
			{
				// Check if GameMode is "FlagTag"
				if (NetContext.GameMode == 2)
				{
					// Search for the player that had the flag for the longest time
					for (int Player = 0; Player < Lvl.Players.size(); Player++)
					{
						// Search for player with most flag time
						if (Lvl.Players().get(Player).FlagTime >= Lvl.Players().get(Winner).FlagTime)
						{
							Winner = Player;

							// Search a player that has the same score
							for (int PlayerScoreEqual = 0; PlayerScoreEqual < Lvl.Players.size(); PlayerScoreEqual++)
							{
								// Don't test the winner against himself
								if (PlayerScoreEqual != Winner)
								{
									if (Lvl.Players().get(PlayerScoreEqual).FlagTime != Lvl.Players().get(Winner).FlagTime)
									{
										Tie = false;
									}
								}
							}
						}
					}
				}
				else	// A player wins when he has the most kills
				{
					// Get the player that has the most kills
					Winner = PlayerWithMostKills;

					// Search for a player that has the same score
					for (int PlayerScoreEqual = 0; PlayerScoreEqual < Lvl.Players.size(); PlayerScoreEqual++)
					{
						if (PlayerScoreEqual != Winner)
						{
							// If another player has the same score
							if (Lvl.Players().get(PlayerScoreEqual).Kills != Lvl.Players().get(Winner).Kills)
							{
								// Tie !
								Tie = false;
							}
						}
					}
				}

				// End the game
				Cam.Menu.InGame = false;

				// Someone wins. Tell if it's a tie.
				if (Tie)
				{
					Cam.Menu.NewMessageToShow("Game is a tie.");
				}
				else
				{
					if (NetContext.View == Winner)
					{
						Cam.Menu.NewMessageToShow("You win!");
					}
					else
					{
						Cam.Menu.NewMessageToShow("The other player wins.");
					}
				}
			}
		}
	}
}

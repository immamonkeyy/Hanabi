package server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import shared.Commands;
import shared.HandCard;

public class ServerPlayer extends Thread {

	private Hanabi game;

	private Socket socket;
	private BufferedReader clientIn;
	private PrintWriter clientOut;
	
	private List<HandCard> hand;
	private ServerPlayer nextPlayer;
	private String playerName;

	public ServerPlayer(Socket socket, Hanabi game) throws ConnectException {
		if (game.started()) {
			throw new ConnectException("Game under way! Cannot accept more players.");
		}
		
		this.socket = socket;
		this.game = game;

		try {
			clientIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			clientOut = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			System.out.println("Player died: " + e);
		}
		
		hand = new ArrayList<HandCard>();
	}
	
	public void draw(ServerPlayer p, Card card) {
		if (p == this) hand.add(new HandCard(card));
		clientOut.println(Commands.DRAW_CARD + p.getPlayerName() + ":" + card.toString());
	}

	public void setNextPlayer(ServerPlayer p) {
		nextPlayer = p;
	}

	public ServerPlayer getNextPlayer() {
		return nextPlayer;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void startGame(String startingPlayerName) {
		clientOut.println(Commands.START_GAME + startingPlayerName);
	}

	public void newPlayerJoined(String name) {
		clientOut.println(Commands.PLAYER_JOINED + name);
	}
	
	public void addExistingPlayer(String name) {
		clientOut.println(Commands.ADD_EXISTING_PLAYER + name);
	}

	public void run() {
		try {

			boolean accepted = false;
			while (!accepted) {
				clientOut.println(Commands.ENTER_NAME);
				playerName = clientIn.readLine();
				accepted = game.addPlayer(this);
			}

			while (true) {
				String command = clientIn.readLine();
				if (command == null) continue;
				
				else if (command.startsWith(Commands.CHOOSE_STARTING_PLAYER)) {
					String startingPlayer = command.substring(Commands.CHOOSE_STARTING_PLAYER.length());
					if (game.existingName(startingPlayer)) {
						game.startGame(startingPlayer);
					} else clientOut.println(Commands.CHOOSE_STARTING_PLAYER);
					
				} else if (command.startsWith(Commands.PLAY)) {
					String card = command.substring(Commands.PLAY.length());
					System.out.println("PLAYING " + card);
					
				} else if (command.startsWith(Commands.DISCARD)) {
					String card = command.substring(Commands.DISCARD.length());
					System.out.println("DISCARDING " + card);
					
				} else if (command.startsWith(Commands.CLUE)) {
					String player = command.substring(Commands.CLUE.length());
					System.out.println("CLUE to " + player);
				}

			}


			//            // The thread is only started after everyone connects.
			//            output.println("MESSAGE All players connected");
			//
			//            // Tell the first player that it is their turn.
			//            if (mark == 'X') {
			//                output.println("MESSAGE Your move");
			//            }
			//
			//            // Repeatedly get commands from the client and process them.
			//            while (true) {
			//                String command = input.readLine();
			//                if (command.startsWith("MOVE")) {
			//                    int location = Integer.parseInt(command.substring(5));
			//                    if (legalMove(location, this)) {
			//                        output.println("VALID_MOVE");
			//                        output.println(hasWinner() ? "VICTORY" : boardFilledUp() ? "TIE" : "");
			//                    } else {
			//                        output.println("MESSAGE ?");
			//                    }
			//                } else if (command.startsWith("QUIT")) {
			//                    return;
			//                }
			//            }
		} catch (IOException e) {
			System.out.println("Player died: " + e);
		} finally {
			try {socket.close();} catch (IOException e) {}
		}
	}

}

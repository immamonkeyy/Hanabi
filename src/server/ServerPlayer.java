package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import color.CardColor;
import shared.Card;
import shared.Commands;
import shared.HandCard;
import shared.Util;

public class ServerPlayer extends Thread {

    private Hanabi game;

    private Socket socket;
    private BufferedReader clientIn;
    private PrintWriter clientOut;

    private List<HandCard> hand;
    private ServerPlayer nextPlayer;
    private String myName;

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

    public List<HandCard> getHand() {
        return hand;
    }

    public void draw(ServerPlayer p, Card card) {
        if (p == this) {
            hand.add(new HandCard(card, game.multicolor()));
        }
        
        clientOut.println(Commands.CARDS_LEFT + game.cardsLeft());
        clientOut.println(Commands.DRAW_CARD + p.getPlayerName() + ":" + card.toString());
    }

    public void validPlay(ServerPlayer p, int position) {
        clientOut.println(Commands.VALID_PLAY + p.getPlayerName() + ":" + position);
    }

    public void invalidPlay(ServerPlayer p, int position) {
        clientOut.println(Commands.INVALID_PLAY + p.getPlayerName() + ":" + position);
    }

    public void discard(ServerPlayer p, int position) {
        clientOut.println(Commands.VALID_DISCARD + p.getPlayerName() + ":" + position);
    }
    
    public void gameOverKeepPlaying(String why) {
        clientOut.println(Commands.GAME_OVER_KEEP_PLAYING + why);
    }

    public void nextTurn() {
        clientOut.println(Commands.NEXT_TURN);
    }

    public void setNextPlayer(ServerPlayer p) {
        nextPlayer = p;
    }

    public ServerPlayer getNextPlayer() {
        return nextPlayer;
    }

    public String getPlayerName() {
        return myName;
    }

    public void startGame(String startingPlayerName, boolean multi, int clues, int fuckups) {
        clientOut.println(Commands.SET_MULTI + multi);
        clientOut.println(Commands.SET_CLUES + clues);
        clientOut.println(Commands.SET_FUCKUPS + fuckups);
        clientOut.println(Commands.START_GAME + startingPlayerName);
    }

    public void newPlayerJoined(String name) {
        clientOut.println(Commands.PLAYER_JOINED + name);
    }

    public void addExistingPlayer(String name) {
        clientOut.println(Commands.ADD_EXISTING_PLAYER + name);
    }

    public void fireworkComplete(CardColor color) {
        clientOut.println(Commands.FIREWORK_COMPLETE + color);
    }

    public void clueTo(String playerName, String clue) {
        if (playerName.equals(myName)) {
            for (HandCard card : hand)
                card.addClue(clue);
        }
        clientOut.println(Commands.CLUE + playerName + ":" + clue);
    }
    
    public void gameOverPlayAgain(String message) {
        clientOut.println(Commands.GAME_OVER_PLAY_AGAIN + message);
    }
    
    public void quit() {
        clientOut.println(Commands.QUIT);
    }
    
    public void reset() {
        clientOut.println(Commands.RESET);
    }

    public void run() {
        try {

            boolean accepted = false;
            while (!accepted) {
                clientOut.println(Commands.ENTER_NAME);
                myName = clientIn.readLine();
                accepted = game.addPlayer(this);
            }

            while (true) {
                String command = clientIn.readLine();
                if (command == null)
                    continue;

                Util.handleResponse(Commands.CHOOSE_STARTING_PLAYER, command, startingPlayer -> {
                    String correctedCase = game.getExistingName(startingPlayer); // null if player not found
                    if (correctedCase != null)
                        game.startGame(correctedCase);
                    else
                        clientOut.println(Commands.CHOOSE_STARTING_PLAYER);
                });

                Util.handleResponse(Commands.PLAY, command, position -> {
                    game.play(Integer.parseInt(position));
                });

                Util.handleResponse(Commands.DISCARD, command, position -> {
                    game.discard(Integer.parseInt(position));
                });

                Util.handleResponse(Commands.CLUE, command, input -> {
                    Util.handlePlayerCard(input, (playerName, clue) -> game.clueTo(playerName, clue));
                });
                
                Util.handleResponse(Commands.KEEP_PLAYING, command, () -> {
                    game.keepPlaying();
                });
                
                Util.handleResponse(Commands.GAME_OVER_PLAY_AGAIN, command, () -> {
                    game.gameOverPlayAgain();
                });
                
                Util.handleResponse(Commands.PLAY_AGAIN_SHUFFLE, command, () -> {
                    game.reset(true);
                });
                
                Util.handleResponse(Commands.PLAY_AGAIN_DONT_SHUFFLE, command, () -> {
                    game.reset(false);
                });
                
                Util.handleResponse(Commands.QUIT, command, () -> {
                    game.quit();
                });
            }

            // // The thread is only started after everyone connects.
            // output.println("MESSAGE All players connected");
            //
            // // Tell the first player that it is their turn.
            // if (mark == 'X') {
            // output.println("MESSAGE Your move");
            // }
            //
            // // Repeatedly get commands from the client and process them.
            // while (true) {
            // String command = input.readLine();
            // if (command.startsWith("MOVE")) {
            // int location = Integer.parseInt(command.substring(5));
            // if (legalMove(location, this)) {
            // output.println("VALID_MOVE");
            // output.println(hasWinner() ? "VICTORY" : boardFilledUp() ? "TIE" : "");
            // } else {
            // output.println("MESSAGE ?");
            // }
            // } else if (command.startsWith("QUIT")) {
            // return;
            // }
            // }
        } catch (IOException e) {
            System.out.println("Player died: " + e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

}

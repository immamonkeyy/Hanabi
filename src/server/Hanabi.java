package server;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import color.CardColor;
import shared.Card;
import shared.ColorMap;
import shared.Util;

public class Hanabi {

    private ServerPlayer currentPlayer;
    private ServerPlayer startingPlayer;
    
    private Integer turnsLeft;
    
    private List<ServerPlayer> players;
    private boolean started;
    private Deck deck;
    private boolean keepPlaying;

    private ColorMap<Card> played;
    private ColorMap<List<Card>> discarded;
    
    private int initialClues;
    private int initialFuckups;

    private int remainingClues;
    private int remainingFuckups;

    private boolean multicolor;

    private static final Card NOT_PLAYED = null; // placeholder in Map before a color has been played

    public Hanabi(boolean multi, int clues, int fuckups) {
        multicolor = multi;
        initialClues = clues;
        initialFuckups = fuckups;

        players = new ArrayList<ServerPlayer>();
        deck = new Deck(multicolor);

        reset(false);
    }
    
    public void reset(boolean shuffle) {
        deck.reset(shuffle);
        
        played = new ColorMap<Card>(multicolor, () -> NOT_PLAYED);
        discarded = new ColorMap<List<Card>>(multicolor, () -> new ArrayList<Card>());

        started = false;
        remainingClues = initialClues;
        remainingFuckups = initialFuckups;
        turnsLeft = null;
        keepPlaying = false;
        
        forEachPlayer(p -> p.reset());
        currentPlayer = startingPlayer;
        deal();
    }

    public boolean multicolor() {
        return multicolor;
    }

    // Get existing name that matches the given name, null if not found
    public String getExistingName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        for (ServerPlayer p : players) {
            if (p.getPlayerName().toLowerCase().trim().equals(name.toLowerCase().trim())) {
                return p.getPlayerName();
            }
        }
        return null;
    }

    public boolean addPlayer(ServerPlayer newPlayer) {
        String name = newPlayer.getPlayerName();
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        if (getExistingName(name) != null)
            return false;

        System.out.println("Adding player with name: " + name);

        for (ServerPlayer p : players) {
            newPlayer.addExistingPlayer(p.getPlayerName());
            p.newPlayerJoined(name);
        }

        newPlayer.newPlayerJoined(name);
        players.add(newPlayer);

        if (currentPlayer != null)
            currentPlayer.setNextPlayer(newPlayer);
        currentPlayer = newPlayer;
        return true;
    }

    public void startGame(String playerName) {
        if (players.size() < 2 || players.size() > 5) {
            throw new IllegalStateException("Invalid number of players.");
        }
        currentPlayer.setNextPlayer(players.get(0));

        for (ServerPlayer p : players) {
            p.startGame(playerName, multicolor, remainingClues, remainingFuckups);
            if (playerName.equals(p.getPlayerName()))
                currentPlayer = startingPlayer = p;
        }

        started = true;

        deal();
    }

    private void deal() {
        System.out.println("Starting game!");
        int cardsPerPlayer = players.size() > 3 ? 4 : 5;

        for (int c = 0; c < cardsPerPlayer; c++) {
            forEachPlayer(player -> {
                draw(player, 50);
            });
        }
    }

    public boolean isValidPlay(Card c) {
        // Color not started, playing 1
        if (played.get(c.color()) == NOT_PLAYED && c.value() == 1) {
            return true;
        }
        // Color started, playing next number correctly
        if (played.get(c.color()) != NOT_PLAYED && played.get(c.color()).value() + 1 == c.value()) {
            return true;
        }
        return false;
    }

    public void play(int position) {
        Card card = currentPlayer.getHand().remove(position).getCard();
        boolean keepPlaying;
        
        int turnPause = 500;
        if (isValidPlay(card)) {
            keepPlaying = validPlay(position, card);
        } else {
            keepPlaying = invalidPlay(position, card);
            turnPause = 2000;
        }

        if (keepPlaying) {
            draw(currentPlayer, 500);
            nextTurn(turnPause);
        }
    }

    public void discard(int position) {
        remainingClues++; // TODO if discard when not allowed
        Card card = currentPlayer.getHand().remove(position).getCard();

        discarded.get(card.color()).add(card);
        forEachPlayer(p -> p.discard(currentPlayer, position));

        draw(currentPlayer, 500);
        nextTurn(2000);
    }

    public void clueTo(String playerName, String clue) {
        if (remainingClues == 0) {
            throw new RuntimeException("No clues to give!");
        } else
            remainingClues--;

        forEachPlayer(p -> p.clueTo(playerName, clue));
        nextTurn(2000);
    }

    private boolean validPlay(int position, Card card) {
        played.put(card.color(), card);
        forEachPlayer(p -> p.validPlay(currentPlayer, position));

        if (card.value() == 5) {
            remainingClues++;
            forEachPlayer(p -> p.fireworkComplete(card.color()));
            
            if (didWeWin()) {
                String message = "Hooray! We've won the game!";
                if (keepPlaying) message += "\nWe kind of cheated though.";
                final String finalMessage = message;
                System.out.println(finalMessage);
                forEachPlayer(p -> p.gameOverPlayAgain(finalMessage));
                return false;
            }
        }
        return true;
    }
    
    public void quit() {
        forEachPlayer(p -> p.quit());
        Util.pauseMillis(5000);
        System.exit(0);
    }
    
    public void restart(boolean shuffle) {
        
    }
    
    private boolean didWeWin() {
        for (CardColor c : played.keySet()) {
            if (played.get(c) == null || played.get(c).value() < 5) {
                return false;
            }
        }
        return true;
    }

    private boolean invalidPlay(int position, Card card) {
        discarded.get(card.color()).add(card);
        forEachPlayer(p -> p.invalidPlay(currentPlayer, position));
        remainingFuckups--;
        if (remainingFuckups == 0) {
            // GAME OVER! YOU SUCK
            // TODO: Ask them if they want to keep playing?
            System.out.println("Game over. You fucked it up.");
            Util.pauseMillis(1500);
            forEachPlayer(p -> p.gameOverKeepPlaying("Too many invalid plays."));
            return false;
        }
        return true;
    }

    private void nextTurn(int pauseMillis) {   
        if (turnsLeft != null) {
            if (turnsLeft == 0) {
                System.out.println("Game over. Cards ran out.");
                forEachPlayer(p -> p.gameOverKeepPlaying("Cards ran out."));
                turnsLeft = null;
                return;
            } else if (turnsLeft > 0) {
               turnsLeft--;
            }
        }
        
        Util.pauseMillis(pauseMillis);
        currentPlayer = currentPlayer.getNextPlayer();
        forEachPlayer(p -> p.nextTurn());
    }

    private void draw(ServerPlayer player, int pauseMillis) {
        if (deck.cardsLeft() > 0) {
            Util.pauseMillis(pauseMillis);
            Card card = deck.draw();
            
            if (deck.cardsLeft() == 0) { //last card!
                turnsLeft = players.size();
            }
            
            forEachPlayer(p -> p.draw(player, card));
        }
    }
    
    public void keepPlaying() {
        keepPlaying = true;
        draw(currentPlayer, 500);
        nextTurn(2000);
    }
    
    public void gameOverPlayAgain() {
        forEachPlayer(p -> p.gameOverPlayAgain(""));
    }

    // Pass each player to the given consumer in turn order, starting with the
    // current player
    private void forEachPlayer(Consumer<ServerPlayer> c) {
        ServerPlayer player = currentPlayer;
        for (int i = 0; i < players.size(); i++) {
            c.accept(player);
            player = player.getNextPlayer();
        }
    }

    public boolean started() {
        return started;
    }

    public int cardsLeft() {
        return deck.cardsLeft();
    }
}

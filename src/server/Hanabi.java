package server;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import shared.Card;
import shared.ColorMap;
import shared.Util;

public class Hanabi {
	
	private ServerPlayer currentPlayer;
	private List<ServerPlayer> players;
	private boolean started;
	private Deck deck;
	
	private ColorMap<Card> played;
	private ColorMap<List<Card>> discarded;
	
	private int remainingClues;
	private int remainingFuckups;
	
	private boolean multicolor;
	
	private static final Card NOT_PLAYED = null; // placeholder in Map before a color has been played
	
	public Hanabi(boolean multi, int clues, int fuckups) {	
		multicolor = multi;
		
		players = new ArrayList<ServerPlayer>();
		deck = new Deck(multicolor);
		
		played = new ColorMap<Card>(multicolor, () -> NOT_PLAYED);
		discarded = new ColorMap<List<Card>>(multicolor, () -> new ArrayList<Card>());
		
		started = false;
		remainingClues = clues;
		remainingFuckups = fuckups;
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
			if(p.getPlayerName().toLowerCase().trim().equals(name.toLowerCase().trim())) {
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
		
		if (getExistingName(name) != null) return false;
		
		System.out.println("Adding player with name: " + name);
		
		for (ServerPlayer p : players) {
			newPlayer.addExistingPlayer(p.getPlayerName());
			p.newPlayerJoined(name);
		}
		
		newPlayer.newPlayerJoined(name);
		players.add(newPlayer);
		
		if (currentPlayer != null) currentPlayer.setNextPlayer(newPlayer);
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
				currentPlayer = p;
		}
		
		System.out.println("Starting game!");
		started = true;
		
		deal();
	}
	
	private void deal() {
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
		
		int turnPause = 500;
		if (isValidPlay(card)) {
			validPlay(position, card);
		} else {
			invalidPlay(position, card);
			turnPause = 2000;
		}
		
		draw(currentPlayer, 500);
		nextTurn(turnPause);
	}
	
	public void clueTo(String playerName, String clue) {
		if (remainingClues == 0) {
			throw new RuntimeException("No clues to give!");
		} else remainingClues--;
		
		forEachPlayer(p -> p.clueTo(playerName, clue));
		nextTurn(2000);
	}
	
	private void validPlay(int position, Card card) {
		played.put(card.color(), card);
		forEachPlayer(p -> p.validPlay(currentPlayer, position));
		
		if (card.value() == 5) {
			forEachPlayer(p -> p.fireworkComplete(card.color()));
		}
	}
	
	private void invalidPlay(int position, Card card) {
		discarded.get(card.color()).add(card);
		forEachPlayer(p -> p.invalidPlay(currentPlayer, position));
		remainingFuckups--;
		if (remainingFuckups == 0) {
			// GAME OVER! YOU SUCK
			System.out.println("Game over. You suck.");
		}
	}
	
	private void nextTurn(int pauseMillis) {
		Util.pauseMillis(pauseMillis);
		currentPlayer = currentPlayer.getNextPlayer();
		forEachPlayer(p -> p.nextTurn());
	}
	
	private void draw(ServerPlayer player, int pauseMillis) {
		Util.pauseMillis(pauseMillis);
		Card card = deck.draw();
		forEachPlayer(p -> p.draw(player, card));
	}
	
	// Pass each player to the given consumer in turn order, starting with the current player
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

package server;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Hanabi {
	
	private ServerPlayer currentPlayer;
	private List<ServerPlayer> players;
	private boolean started = false;
	private Deck deck;
	
	private int remainingClues;
	private int remainingFuckups;
	
	public Hanabi() {
		players = new ArrayList<ServerPlayer>();
		deck = new Deck(true); // with rainbow
		remainingClues = 8;
		remainingFuckups = 3;
	}
	
	public boolean existingName(String name) {
		if (name == null || name.trim().isEmpty()) {
			return false;
		}
		for (ServerPlayer p : players) {
			if(p.getPlayerName().toLowerCase().trim().equals(name.toLowerCase().trim())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean addPlayer(ServerPlayer newPlayer) {
		String name = newPlayer.getPlayerName();
		if (name == null || name.trim().isEmpty()) {
			return false;
		}
		
		if (existingName(name)) return false;
		
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
			p.startGame(playerName);
			if (playerName.equals(p.getPlayerName())) {
				currentPlayer = p;
			}
		}
		
		System.out.println("Starting game!");
		started = true;
		
		deal();
	}
	
	public void deal() {
		int cardsPerPlayer = 5;
		if (players.size() > 3) {
			cardsPerPlayer = 4;
		}
		
		ServerPlayer player = currentPlayer;
		for (int c = 0; c < cardsPerPlayer; c++) {
			for (int i = 0; i < players.size(); i++) {
				Card card = deck.draw();
				for (ServerPlayer p : players) {
					p.draw(player, card);
				}
				player = player.getNextPlayer();
				try {
					TimeUnit.MILLISECONDS.sleep(50);
				} catch (InterruptedException e) { }
			}
		}
	}
	
	public boolean started() {
		return started;
	}

}

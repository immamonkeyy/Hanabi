package client;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class AllPlayers {
	
    private Map<String, ClientPlayer> players;
    private ClientPlayer firstPlayerAdded;
    private ClientPlayer lastPlayerAdded;
    private ClientPlayer turn;
    
    public AllPlayers() {
    		players = new LinkedHashMap<String, ClientPlayer>();
    		firstPlayerAdded = null;
    		lastPlayerAdded = null;
    }
    
    public void addPlayer(ClientPlayer newPlayer) {
    		if (firstPlayerAdded == null) firstPlayerAdded = newPlayer;
    		if (lastPlayerAdded != null) lastPlayerAdded.setNextPlayer(newPlayer);
    		players.put(newPlayer.getPlayerName(), newPlayer);
    		lastPlayerAdded = newPlayer;
    }
    
    public void startGame(String playerName) {
    		lastPlayerAdded.setNextPlayer(firstPlayerAdded);
    		turn = players.get(playerName);
    }
    
    public Set<String> names() {
    		return players.keySet();
    }
    
    public ClientPlayer get(String name) {
    		return players.get(name);
    }
    
    public void printPlayers() {
    		for (String n : names()) {
    			System.out.println(get(n));
    		}
    }
    
    public ClientPlayer getTurn() {
    		return turn;
    }

}

package client;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class AllPlayers {

    private Map<String, ClientPlayer> players;
    private ClientPlayer firstPlayerAdded;
    private ClientPlayer lastPlayerAdded;
    private ClientPlayer turn;
    private ClientPlayer startingPlayer;

    public AllPlayers() {
        players = new LinkedHashMap<String, ClientPlayer>();
        firstPlayerAdded = null;
        lastPlayerAdded = null;
    }

    public void addPlayer(ClientPlayer newPlayer) {
        if (firstPlayerAdded == null)
            firstPlayerAdded = newPlayer;
        if (lastPlayerAdded != null)
            lastPlayerAdded.setNextPlayer(newPlayer);
        players.put(newPlayer.getPlayerName(), newPlayer);
        lastPlayerAdded = newPlayer;
    }

    public void startGame(String playerName) {
        lastPlayerAdded.setNextPlayer(firstPlayerAdded);
        turn = startingPlayer = players.get(playerName);
        turn.highlight(true);
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

    public boolean isTurn(String name) {
        return name.equals(turn.getPlayerName());
    }

    public void nextTurn() {
        turn.highlight(false);
        turn = turn.getNextPlayer();
        turn.highlight(true);
    }

    public ClientPlayer turn() {
        return turn;
    }
    
    public void reset() {
        for (ClientPlayer p : players.values()) {
            p.reset();
        }
        turn = startingPlayer;
        turn.highlight(true);
    }

}

package client;

import java.util.ArrayList;
import java.util.List;

import server.ColorMap;

public class ClientBoard {
	
	private int remainingClues;
	private int remainingFuckups;
	private ColorMap<ClientCard> played;
	private ColorMap<List<ClientCard>> discarded;
	private static final ClientCard NOT_PLAYED = null;
	
	public ClientBoard() {
		boolean multicolor = true;
		
		remainingClues = 8;
		remainingFuckups = 3;
		
		played = new ColorMap<ClientCard>(multicolor, () -> NOT_PLAYED);
		discarded = new ColorMap<List<ClientCard>>(multicolor, () -> new ArrayList<ClientCard>());
	}
	
	public void validPlay(ClientCard c) {
		played.put(c.color(), c);
	}
	
	public void invalidPlay(ClientCard c) {
		List<ClientCard> colorList = discarded.get(c.color());
		colorList.add(c);
		remainingFuckups--;
	}
	
	public void discard(ClientCard c) {
		discarded.get(c.color()).add(c);
		remainingClues++;
	}
	
	public void useClue() {
		remainingClues--;
	}

}

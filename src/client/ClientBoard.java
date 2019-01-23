package client;

import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import color.CardColor;
import server.ColorMap;

public class ClientBoard extends JPanel {
	
	private int remainingClues;
	private int remainingFuckups;
	private ColorMap<ClientCard> played;
	private ColorMap<List<ClientCard>> discarded;
	private static final ClientCard NOT_PLAYED = null;
	
	private JPanel cardPanel;
	
	private boolean multicolor = true;
	
	public ClientBoard() {
		super();

		// GridBagLayout to be centered vertically
		this.setLayout(new GridBagLayout());
		this.setOpaque(false);
				
		remainingClues = 8;
		remainingFuckups = 3;
		
		played = new ColorMap<ClientCard>(multicolor, () -> NOT_PLAYED);
		discarded = new ColorMap<List<ClientCard>>(multicolor, () -> new ArrayList<ClientCard>());
		
		cardPanel = Client.invisiblePanel();
		for (CardColor c : played.keySet()) {
			cardPanel.add(ClientCard.getEmptySpot());
		}
		this.add(cardPanel);
	}
	
	public void validPlay(ClientCard card) {
		played.put(card.color(), card);
		int index = played.indexOf(card.color());
		cardPanel.remove(index);
		cardPanel.add(card, index);
		card.display(true);
		cardPanel.revalidate();
		cardPanel.repaint();
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

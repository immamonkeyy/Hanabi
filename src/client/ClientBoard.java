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
	}
	
	public void update() {		
		// FlowLayout for horizontal spaces between cards
		JPanel p = new JPanel();
		p.setOpaque(false);
				
		for (CardColor color : CardColor.getAllColors(multicolor)) {
			ClientCard card = played.get(color);
			if (card == NOT_PLAYED)
				p.add(ClientCard.getEmptySpot());
			else {
				p.add(card);
				card.display(true);
			}
		}
		
		this.removeAll();
		this.add(p);
		this.revalidate();
		this.repaint();
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

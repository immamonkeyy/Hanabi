package clientboard;

import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import client.InvisiblePanel;
import color.CardColor;
import shared.ColorMap;


@SuppressWarnings("serial")
public class ClientBoard extends JPanel {
	
	private int remainingClues;
	private int remainingFuckups;
	private ColorMap<JPanel> played;
	private ColorMap<List<ClientCard>> discarded;
	private Map<CardColor, Point> locations;
	
	private JPanel cardPanel;
	
	public Point getLocation(CardColor c) {
		return locations.get(c);
	}
	
	public void saveCardLocationsRelativeTo(JPanel view) {
		for (CardColor color : played.keySet()) {
			JPanel card = played.get(color);
			Point p = SwingUtilities.convertPoint(cardPanel, card.getLocation(), view); // top left corner
			p.translate(card.getWidth() / 2, card.getHeight() / 2); // center of card
			locations.put(color, p);
		}
	}
	
	public ClientBoard(boolean multicolor) {
		super();

		// GridBagLayout to be centered vertically
		this.setLayout(new GridBagLayout());
		this.setOpaque(false);
				
		remainingClues = 8;
		remainingFuckups = 3;
		
		played = new ColorMap<JPanel>(multicolor, () -> ClientCard.getEmptySpot());
		discarded = new ColorMap<List<ClientCard>>(multicolor, () -> new ArrayList<ClientCard>());
		locations = new HashMap<CardColor, Point>();
		
		cardPanel = InvisiblePanel.create();
		for (CardColor c : played.keySet()) {
			cardPanel.add(played.get(c));
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

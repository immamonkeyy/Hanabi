package clientboard;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import color.CardColor;
import shared.ColorMap;

public class ClientBoard {

	private int totalClues;
	private int remainingClues;
	private int remainingFuckups;
	private int remainingCards;

	private ColorMap<JPanel> played;
	private ColorMap<List<ClientCard>> discarded;
	private Map<CardColor, Point> locations;

	private DeckPanel deckPanel;
	private PlayPanel playPanel;
	
	private ClientCard lastPlayed;

	public ClientBoard(boolean multicolor, int clueCount, int fuckupCount) {	
		remainingClues = totalClues = clueCount;
		remainingFuckups = fuckupCount;

		played = new ColorMap<JPanel>(multicolor, () -> ClientCard.getEmptySpot());
		discarded = new ColorMap<List<ClientCard>>(multicolor, () -> new ArrayList<ClientCard>());
		locations = new HashMap<CardColor, Point>();

		playPanel = new PlayPanel(played);
		deckPanel = new DeckPanel(clueCount, fuckupCount);
		
		lastPlayed = null;
	}

	public PlayPanel getPlayPanel() { return playPanel; }
	public DeckPanel getDeckPanel() { return deckPanel; }

	public Point getLocation(CardColor c) {
		return locations.get(c);
	}

	public void saveCardLocationsRelativeTo(JPanel view) {
		for (CardColor color : played.keySet()) {
			JPanel card = played.get(color);
			Point p = SwingUtilities.convertPoint(playPanel.getCardPanel(), card.getLocation(), view); // top left corner
			p.translate(card.getWidth() / 2, card.getHeight() / 2); // center of card
			locations.put(color, p);
		}
	}
	
	public void clearSelected() {
		if (lastPlayed != null) {
			lastPlayed.setSelected(false);
			lastPlayed = null;
		}
	}

	public void validPlay(ClientCard card) {
		lastPlayed = card;
		card.setSelected(true);
		played.put(card.color(), card);
		int index = played.indexOf(card.color());
		playPanel.addCard(card, index);
	}
	
	private void addToDiscards(ClientCard c) {
		String tabs = "\t\t\t\t\t";
		if (c.color() != CardColor.MULTI) tabs += "\t";
		String message = "Discarding " + c.toMessageString();
		System.out.println(message + tabs + message);
		
		discarded.get(c.color()).add(c);
	}

	public void invalidPlay(ClientCard c) {
		addToDiscards(c);
		remainingFuckups--;
		deckPanel.useFuckup(remainingFuckups);
	}

	// TODO: Can't discard if clues full
	public void discard(ClientCard c) {
		addToDiscards(c);
		getAClueBack();
	}
	
	public void getAClueBack() {
		remainingClues++;
		deckPanel.getAClueBack(remainingClues);
	}

	public void useClue() {
		remainingClues--;
		deckPanel.useClue(remainingClues);
	}

	public boolean hasClues() {
		return remainingClues > 0;
	}
	
	public boolean cluesFull() {
		return remainingClues == totalClues;
	}

	public void setRemainingCards(int cardsLeft) {
		remainingCards = cardsLeft;
        deckPanel.remainingCards(cardsLeft);
	}
}

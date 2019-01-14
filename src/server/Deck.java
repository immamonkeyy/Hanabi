package server;
import java.util.Random;

import color.CardColor;
import color.*;


public class Deck {
	
	private static final int[] ALL_VALUES = new int[] {1, 1, 1, 2, 2, 3, 3, 4, 4, 5};
	
	private Card[] cards;
	private int next;
	
	public Deck(boolean rainbow) {
		next = 0;
		
		int size = ALL_VALUES.length * CardColor.ALL_COLORS.length;
		if (rainbow) size += ALL_VALUES.length; // make room for rainbow cards
		
		cards = new Card[size];
		populateCards(rainbow);
		shuffle();
	}
	
	private void populateCards(boolean rainbow) {
		for (CardColor color : CardColor.ALL_COLORS) {
			populateColor(color);
		}
		if (rainbow) populateColor(new Rainbow());
		next = 0;
	}
	
	private void populateColor(CardColor color) {
		for (int value : ALL_VALUES) {
			cards[next++] = new Card(color, value);
		}
	}
	
	private void shuffle() {
		Random rand = new Random();
		shuffleHelper(rand);
		shuffleHelper(rand);
	}
	
	private void shuffleHelper(Random rand) {
		for (int i = 0; i < cards.length; i++) {
			int randomIdx = rand.nextInt(cards.length);
			Card temp = cards[i];
			cards[i] = cards[randomIdx];
			cards[randomIdx] = temp;
		}
	}
	
	// Throws IndexOutOfBounds if deck has been exhausted
	public Card draw() {
		return cards[next++];
	}
	
	public boolean hasNext() {
		return next < cards.length;
	}
	
	public void reset() {
		next = 0;
		shuffle();
	}

}

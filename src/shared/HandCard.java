package shared;
import java.util.HashMap;
import java.util.Map;

import color.CardColor;
import server.Card;

public class HandCard {

	private Map<CardColor, Boolean> possibleColors;
	private Map<Integer, Boolean> possibleValues;
	private boolean hasClues;
	
	private Card actualCard;
	
	public HandCard(Card card) {
		possibleColors = new HashMap<CardColor, Boolean>();
		for (CardColor color : CardColor.ALL_COLORS) {
			possibleColors.put(color, null);
		}
		
		possibleValues = new HashMap<Integer, Boolean>();
		for (int i = 1; i <= 5; i++) {
			possibleValues.put(i, null);
		}
		
		hasClues = false;
		
		actualCard = card;
	}
	
	public boolean hasClues() {
		return hasClues;
	}
	
	public Card getCard() {
		return actualCard;
	}
	
	public String toString() {
		return actualCard.toString();
	}
	
	public int value() {
		return actualCard.value();
	}
	
	public CardColor color() {
		return actualCard.color();
	}
}

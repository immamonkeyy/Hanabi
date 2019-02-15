package shared;
import java.util.HashMap;
import java.util.Map;

import color.CardColor;

public class HandCard {

	private ColorMap<Boolean> possibleColors;
	private Map<Integer, Boolean> possibleValues;
	private boolean hasClues;
	
	private Card actualCard;
	
	public HandCard(Card card, boolean multicolor) {
		possibleColors = new ColorMap<Boolean>(multicolor, () -> null);
		
		possibleValues = new HashMap<Integer, Boolean>();
		for (int i = 1; i <= 5; i++) {
			possibleValues.put(i, null);
		}
		
		hasClues = false;
		
		actualCard = card;
	}

	public boolean matches(String clue) {
		return actualCard.matches(clue, (a, b) -> {}, (a, b) -> {});
	}
	
	public void addClue(String clue) {
		actualCard.matches(clue,
				(i, target) -> recordClueGiven(possibleValues, i, target),
				(c, target) -> recordClueGiven(possibleColors, c, target));
	}
	
	// Will flip "hasClues" to true the first time this card
	// is the target of a given clue.
	public <T> void recordClueGiven(Map<T, Boolean> possiblesMap, T clue, boolean target) {
		possiblesMap.put(clue, target);
		hasClues = !hasClues && target;
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

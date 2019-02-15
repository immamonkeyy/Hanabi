package shared;

import java.util.function.BiConsumer;

import color.CardColor;

public class Card {
	
	private CardColor color;
	private int value;
	
	public Card(CardColor color, int value) {
		super();
		
		this.color = color;
		this.value = value;
	}
	
	public String toString() {
		return value + color.toString();
	}
	
	public CardColor color() {
		return color;
	}
	
	public int value() {
		return value;
	}
	
	// Returns if this card matches the given clue, and
	// provides ability to run some handlers
	public boolean matches(String clue, 
			BiConsumer<Integer, Boolean> handleValueClue, 
			BiConsumer<CardColor, Boolean> handleColorClue) {
		
		boolean target;
		if (Character.isDigit(clue.charAt(0))) {
			int i = Integer.parseInt(clue);
			target = value() == i;
			handleValueClue.accept(i, target);
		}
		else {
			CardColor c = CardColor.fromString(clue);
			target = color().matchesColor(c);
			handleColorClue.accept(c, target);
		}
		return target;
	}
}
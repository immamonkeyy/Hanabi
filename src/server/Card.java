package server;

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
}
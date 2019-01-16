package color;

import java.awt.Color;

public abstract class CardColor {
	
	public static final int[][] PATTERNS = new int[][]{
		  { 0, 0, 0, 0, 1, 0, 0, 0, 0 },
		  { 0, 0, 1, 0, 0, 0, 1, 0, 0 },
		  { 1, 0, 1, 0, 0, 0, 0, 1, 0 },
		  { 0, 1, 0, 1, 0, 1, 0, 1, 0 },
		  { 1, 0, 1, 0, 1, 0, 1, 0, 1 }
		};
	
	public static final Color LIGHT_BLUE_COLOR = new Color(135,206,250);
	
	// Make these private?
	public static final CardColor RED = new Red();
	public static final CardColor YELLOW = new Yellow();
	public static final CardColor GREEN = new Green();
	public static final CardColor BLUE = new Blue();
	public static final CardColor WHITE = new White();
	public static final CardColor MULTI = new Rainbow();
	
	private static final CardColor[] ALL_COLORS = new CardColor[] { RED, YELLOW, GREEN, BLUE, WHITE };
	private static final CardColor[] ALL_COLORS_WITH_MULTI = new CardColor[] { RED, YELLOW, GREEN, BLUE, WHITE, MULTI };
	
	public static final CardColor[] getAllColors(boolean multi) {
		return multi ? ALL_COLORS_WITH_MULTI : ALL_COLORS;
	}
	
	public abstract Color getColor();
	
	public static CardColor fromString(String str) {
		switch (str.toLowerCase()) {
			case "red" : return RED;
			case "yellow" : return YELLOW;
			case "blue" : return BLUE;
			case "green" : return GREEN;
			case "white" : return WHITE;
			default : return MULTI;
		}
	}

}

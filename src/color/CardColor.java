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
	
	public static final CardColor RED = new Red();
	public static final CardColor YELLOW = new Yellow();
	public static final CardColor GREEN = new Green();
	public static final CardColor BLUE = new Blue();
	public static final CardColor WHITE = new White();
	public static final CardColor MULTI = new Rainbow();
	
	public static final CardColor[] ALL_COLORS = new CardColor[] { RED, YELLOW, GREEN, BLUE, WHITE };
	
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

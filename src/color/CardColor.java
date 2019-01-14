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
	
	public static final Color LIGHT_BLUE = new Color(135,206,250);
	
	public static final CardColor[] ALL_COLORS = new CardColor[] { 
			new Red(), new Yellow(), new Green(), new Blue(), new White() };
	
	public abstract Color getColor();
	
	public static CardColor fromString(String str) {
		switch (str.toLowerCase()) {
			case "red" : return new Red();
			case "yellow" : return new Yellow();
			case "blue" : return new Blue();
			case "green" : return new Green();
			case "white" : return new White();
			default : return new Rainbow();
		}
	}

}

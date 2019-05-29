package color;

import java.awt.Color;

public class CardColor {

    private Color color;
    private String name;

    public CardColor(Color c, String s) {
        color = c;
        name = s;
    }

    public static final int[][] PATTERNS = new int[][]{
        { 0, 0, 0, 0, 1, 0, 0, 0, 0 },
        { 0, 0, 1, 0, 0, 0, 1, 0, 0 },
        { 1, 0, 1, 0, 0, 0, 0, 1, 0 },
        { 0, 1, 0, 1, 0, 1, 0, 1, 0 },
        { 1, 0, 1, 0, 1, 0, 1, 0, 1 }
      };

    public static final Color LIGHT_BLUE_COLOR = new Color(135, 206, 250);

    // Make these private?
    public static final CardColor RED = new CardColor(Color.RED, "red");
    public static final CardColor YELLOW = new CardColor(Color.YELLOW, "yellow");
    public static final CardColor GREEN = new CardColor(Color.GREEN, "green");
    public static final CardColor BLUE = new CardColor(LIGHT_BLUE_COLOR, "blue");
    public static final CardColor WHITE = new CardColor(Color.WHITE, "white");
    public static final CardColor MULTI = new Multicolor(Color.MAGENTA, "multicolor");

    private static final CardColor[] ALL_COLORS = new CardColor[] { RED, YELLOW, GREEN, BLUE, WHITE };
    private static final CardColor[] ALL_COLORS_WITH_MULTI = new CardColor[] { RED, YELLOW, GREEN, BLUE, WHITE, MULTI };

    public static final CardColor[] getAllColors(boolean multi) {
        return multi ? ALL_COLORS_WITH_MULTI : ALL_COLORS;
    }

    public Color getColor() {
        return color;
    }

    public String toString() {
        return name;
    }

    public static CardColor fromString(String str) {
        for (CardColor c : getAllColors(true)) {
            if (c.toString().equals(str.toLowerCase()))
                return c;
        }
        throw new RuntimeException("Invalid color: " + str);
    }

    public boolean matchesColor(CardColor c) {
        return this == c;
    }
}

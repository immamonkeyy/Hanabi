package color;

import java.awt.Color;

public class Multicolor extends CardColor {

    public Multicolor(Color c, String s) {
        super(c, s);
    }

    @Override
    public boolean matchesColor(CardColor c) {
        return true;
    }
}

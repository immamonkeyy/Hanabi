package shared;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

import color.CardColor;

@SuppressWarnings("serial")
public class ColorMap<T> extends LinkedHashMap<CardColor, T> {

    private List<CardColor> colorList;
    private Supplier<T> supp;

    public ColorMap(boolean multicolor, Supplier<T> supp) {
        super();

        this.supp = supp;
        colorList = Arrays.asList(CardColor.getAllColors(multicolor));

        reset();
    }

    public int indexOf(CardColor color) {
        return colorList.indexOf(color);
    }
    
    public void reset() {
        for (CardColor c : colorList) {
            this.put(c, supp.get());
        }
    }

}

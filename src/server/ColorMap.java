package server;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

import color.CardColor;
import color.Rainbow;

@SuppressWarnings("serial")
public class ColorMap<T> extends LinkedHashMap<CardColor, T> {
	
	public ColorMap(boolean multicolor, Supplier<T> supp) {
		super();
		
		for (CardColor c : CardColor.ALL_COLORS) {
			this.put(c, supp.get());
		}
		
		if (multicolor) this.put(CardColor.MULTI, supp.get());
	}

}

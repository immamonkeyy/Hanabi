package server;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

import color.CardColor;
import color.Rainbow;

@SuppressWarnings("serial")
public class ColorMap<T> extends LinkedHashMap<CardColor, T> {
	
	public ColorMap(boolean multicolor, Supplier<T> supp) {
		super();
		
		for (CardColor c : CardColor.getAllColors(multicolor)) {
			this.put(c, supp.get());
		}
	}

}

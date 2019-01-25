package server;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

import color.CardColor;
import color.Multicolor;

@SuppressWarnings("serial")
public class ColorMap<T> extends LinkedHashMap<CardColor, T> {
	
	private List<CardColor> colorList;
	
	public ColorMap(boolean multicolor, Supplier<T> supp) {
		super();
		
		colorList = Arrays.asList(CardColor.getAllColors(multicolor));
		
		for (CardColor c : colorList) {
			this.put(c, supp.get());
		}
	}
	
	public int indexOf(CardColor color) {
		return colorList.indexOf(color);
	}

}

package token;

import java.awt.Color;

@SuppressWarnings("serial")
public class ClueToken extends Token {

	public ClueToken() {
		super(Color.BLUE);
	}
	
	public void getBack() {
		used = false;
		repaint();
	}
}

package client;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import color.CardColor;
import server.Card;
import shared.HandCard;

// Wrapper class for Card
// Adds functionality to display Card
@SuppressWarnings("serial")
public class ClientCard extends JPanel {
	
	private static Dimension CARD_DIMENSION = new Dimension(90, 150);
	
	private HandCard card;
	private boolean selected;
	private int position;
	
	private MouseListener mouseListener;
	
	public ClientCard(Card card, int pos) {
		super();
		this.card = new HandCard(card);
		this.position = pos;
		this.selected = false;
		
		this.setPreferredSize(CARD_DIMENSION);
		this.setBorder(new LineBorder(Color.WHITE, 5));
		this.setBackground(Color.BLUE);
		this.setLayout(new BorderLayout());
	}
	
	public static JPanel getEmptySpot() {
		JPanel p = Client.invisiblePanel();
		p.setPreferredSize(CARD_DIMENSION);
		return p;
	}
	
	public int getPosition() {
		return position;
	}
	
	public void decrementPosition() {
		position--;
	}
	
	public String toString() {
		return card.toString();
	}
	
	public void setSelected(boolean b) {
		selected = b;
		Color c = selected ? Color.BLACK : Color.BLUE;
		this.setBackground(c);
		this.repaint();
	}
	
	public void display(boolean showFront) {		
		if (showFront) {
			this.add(getNumberPanel(), BorderLayout.NORTH);
			this.add(getNumberPanel(), BorderLayout.SOUTH);
		
			this.add(getCenterPanel(), BorderLayout.CENTER);
		} else {
			//TODO
		}
	}
	
	 /*
	  *  ________
	  * |5      5| <- getNumberPanel produces this horizontal panel
	  * |        |
	  * |        |
	  * |        |
	  * |5      5| <- also this horizontal panel
	  *  --------
	  */
	private JPanel getNumberPanel() {
		JPanel panel = Client.invisiblePanel(new BorderLayout());
				
		panel.add(getLabel(card.value() + "", 30), BorderLayout.WEST);
		panel.add(getLabel(card.value() + "", 30), BorderLayout.EAST);
		return panel;
	}
	
	public CardColor color() {
		return card.color();
	}
	
	// Makes a text label and sets it to the card's color, with the corret font and stuff
	private JLabel getLabel(String text, int size) {
		JLabel label = new JLabel(text);
		label.setForeground(this.color().getColor());
		label.setFont(new Font("Impact", Font.BOLD, size));
		return label;
	}
	
	// This is the panel in the middle of the card with the "fireworks" on it
	private JPanel getCenterPanel() {
		JPanel panel = Client.invisiblePanel(new GridLayout(3, 3));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		for (int i : CardColor.PATTERNS[card.value() - 1]) {
			if (i == 1) {
				JPanel p = Client.invisiblePanel(new GridBagLayout());
				p.add(getLabel("*", 75));
				panel.add(p);
			}
			else panel.add(new JLabel());
		}
		return panel;
	}
	
	public void addMouseListener(MouseListener m) {
		super.addMouseListener(m);
		mouseListener = m;
	}
	
	public void clean() {
		super.removeMouseListener(mouseListener);
		setSelected(false);
	}
}

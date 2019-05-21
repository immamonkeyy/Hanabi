package clientboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.Comparator;
import java.util.List;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import client.InvisiblePanel;
import color.CardColor;
import shared.ColorMap;

@SuppressWarnings("serial")
public class DiscardPanel extends JPanel {

	public DiscardPanel() {
		super(new GridBagLayout());
		this.setOpaque(false);
	}

	public void refresh(ColorMap<List<ClientCard>> oops, ColorMap<List<ClientCard>> ok) {
		this.removeAll();
		int hgap = 0;
		int vgap = 5;
		JPanel oopsPanel = InvisiblePanel.create(new GridLayout(0, 1, hgap, vgap));
		
		for (CardColor color : oops.keySet()) {
			List<ClientCard> cards = oops.get(color);
			if (!cards.isEmpty()) {
				cards.sort(Comparator.comparingInt(ClientCard::value).reversed());
				JLayeredPane pane = new JLayeredPane();
				
				Dimension preferredSize = new Dimension(ClientCard.SMALL_CARD_DIMENSION);
				preferredSize.width += 17 * cards.size();
				preferredSize.height += 4 * cards.size();
				pane.setPreferredSize(preferredSize);
		
				Rectangle bounds = new Rectangle(ClientCard.SMALL_CARD_DIMENSION);
				for (int i = 0; i < cards.size(); i++) {
					ClientCard c = cards.get(i);
					c.displaySmall();
					c.setBounds(bounds);
					pane.add(c, Integer.valueOf(i));
					bounds.translate(17, 4);
				}
				
				oopsPanel.add(pane);
			}
		}
		this.add(oopsPanel);
	}

	
}

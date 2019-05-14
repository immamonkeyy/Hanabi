package clientboard;

import java.awt.GridBagLayout;

import javax.swing.JPanel;

import client.InvisiblePanel;
import color.CardColor;
import shared.ColorMap;

@SuppressWarnings("serial")
public class PlayPanel extends JPanel {
	
	private JPanel cardPanel;
	
	// TODO: Two rows of play cards?
	public PlayPanel(ColorMap<JPanel> played) {
		super();

		// GridBagLayout to be centered vertically
		this.setLayout(new GridBagLayout());
		this.setOpaque(false);
		
		cardPanel = InvisiblePanel.create();
		for (CardColor c : played.keySet()) {
			cardPanel.add(played.get(c));
		}
		add(cardPanel);
	}

	public void addCard(ClientCard card, int index) {
		cardPanel.remove(index);
		cardPanel.add(card, index);
		card.display(true);
		cardPanel.revalidate();
		cardPanel.repaint();
	}
	
	// TODO: Make less ugly
	public JPanel getCardPanel() {
		return cardPanel;
	}

}

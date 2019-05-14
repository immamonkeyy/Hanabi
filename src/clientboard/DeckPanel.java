package clientboard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import client.InvisiblePanel;

@SuppressWarnings("serial")
public class DeckPanel extends JPanel {
	
	private JLayeredPane layeredDeck;
	private ClueToken[] clues;
	
	public DeckPanel(int clueCount) {
		super(new GridBagLayout());
		this.setOpaque(false);

		int height = clueCount;
		int width = 1;

		if (clueCount > 10) {
			height = 10;
			width = clueCount / 10;
			if (clueCount % 10 > 0) width++;
		}

		JPanel clueTokenGrid = InvisiblePanel.create(new GridLayout(height, width));
		clues = new ClueToken[clueCount];
		for (int i = clues.length - 1; i >= 0; i--) {
			clues[i] = new ClueToken();
			clueTokenGrid.add(clues[i]);
		}

		layeredDeck = new JLayeredPane();
		Dimension preferredSize = new Dimension(ClientCard.CARD_DIMENSION);
		preferredSize.width += 40;
		preferredSize.height += 40;
		layeredDeck.setPreferredSize(preferredSize);

		Rectangle bounds = new Rectangle(ClientCard.CARD_DIMENSION);
		for (int i = 2; i >= 0; i--) {
			JPanel c = ClientCard.getBlankCard();
			bounds.translate(10, 10);
			c.setBounds(bounds);
			layeredDeck.add(c, Integer.valueOf(i));
		}

		JPanel clueTokenPanel = InvisiblePanel.create(new GridBagLayout());
		clueTokenPanel.add(clueTokenGrid);

		JPanel cluesAndDeck = InvisiblePanel.create(new BorderLayout());
		cluesAndDeck.add(clueTokenPanel, BorderLayout.WEST);
		cluesAndDeck.add(layeredDeck, BorderLayout.CENTER);
		this.add(cluesAndDeck);
	}

	public void useClue(int i) {
		clues[i].use();
	}

	public void getAClueBack(int i) {
		clues[i - 1].getBack();
	}

	public void remainingCards(int cardsLeft) {
		layeredDeck.setToolTipText(cardsLeft + " cards left");
		if (cardsLeft < 3 && layeredDeck.getComponentCount() > 0) {
			layeredDeck.remove(layeredDeck.lowestLayer());
			layeredDeck.repaint();
			layeredDeck.revalidate();
		}
	}
}

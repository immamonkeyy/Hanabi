package clientboard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.function.Supplier;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import client.InvisiblePanel;
import token.ClueToken;
import token.FuckupToken;
import token.Token;

@SuppressWarnings("serial")
public class DeckPanel extends JPanel {
	
	private JLayeredPane layeredDeck;
	private ClueToken[] clues;
	private FuckupToken[] fuckups;
	
	public DeckPanel(int clueCount, int fuckupCount) {
		super(new GridBagLayout());
		this.setOpaque(false);
		
		clues = new ClueToken[clueCount];
		fuckups = new FuckupToken[fuckupCount];

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

		JPanel deckAndTokens = InvisiblePanel.create(new BorderLayout());
		add(deckAndTokens);
		
		deckAndTokens.add(createTokenPanel(10, clues, () -> new ClueToken()), BorderLayout.WEST);
		deckAndTokens.add(layeredDeck, BorderLayout.CENTER);
		deckAndTokens.add(createTokenPanel(4, fuckups, () -> new FuckupToken()), BorderLayout.EAST);
	}

	public void useClue(int i) {
		clues[i].use();
	}

	public void getAClueBack(int i) {
		clues[i - 1].getBack();
	}
	
	private JPanel createTokenPanel(int maxHeight, Token[] tokens, Supplier<Token> gen) {
		int height = Math.min(tokens.length, maxHeight);
		int width = (tokens.length - 1) / 10 + 1;
		
		JPanel tokenPanel = InvisiblePanel.create(new GridLayout(1, width));
		int tokenCounter = tokens.length - 1;
		
		for (int c = 0; c < width; c++) {
			JPanel columnPanel = InvisiblePanel.create(new GridLayout(height, 1));
			tokenPanel.add(columnPanel);
			for (int r = 0; r < height; r++) {
				if (tokenCounter >= 0) {
					tokens[tokenCounter] = gen.get();
					columnPanel.add(tokens[tokenCounter]);
					tokenCounter--;
				}
			}
		}
		
		JPanel outerPanel = InvisiblePanel.create(new GridBagLayout());
		outerPanel.add(tokenPanel);
		return outerPanel;
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

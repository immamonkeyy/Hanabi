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
    public PlayPanel() {
        super();

        // GridBagLayout to be centered vertically
        this.setLayout(new GridBagLayout());
        this.setOpaque(false);

        cardPanel = InvisiblePanel.create();
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
    
    // Assume reset() has already been called on played
    // Should this be called refresh() to match the discardPanel?
    public void reset(ColorMap<JPanel> played) {
        cardPanel.removeAll();
        
        for (CardColor c : played.keySet()) {
            cardPanel.add(played.get(c));
        }
        
        cardPanel.revalidate();
        cardPanel.repaint();
    }

}

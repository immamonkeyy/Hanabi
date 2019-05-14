package clientboard;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Rectangle;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class DeckPanel extends JPanel {
	
	private JLayeredPane layer;
	
	public DeckPanel() {
		super(new GridBagLayout());
		setOpaque(false);
		
		layer = new JLayeredPane();
		Dimension preferredSize = new Dimension(ClientCard.CARD_DIMENSION);
		preferredSize.width += 40;
		preferredSize.height += 40;
		layer.setPreferredSize(preferredSize);
		add(layer);
		
		Rectangle bounds = new Rectangle(ClientCard.CARD_DIMENSION);
		for (int i = 2; i >= 0; i--) {
			JPanel c = ClientCard.getBlankCard();
			bounds.translate(10, 10);
			c.setBounds(bounds);
			layer.add(c, Integer.valueOf(i));
		}
	}

	public void removeCard() {
		if (layer.getComponentCount() > 0) {
			layer.remove(layer.lowestLayer());
			layer.repaint();
			layer.revalidate();
		}
	}

}

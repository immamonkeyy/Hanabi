package clientboard;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ClueToken extends JPanel {

	private static final int CLUE_DIAM = 15;
	private static final int LEFT_PADDING = 10;
	private static final int RIGHT_PADDING = 5;
	private static final int VERTICAL_PADDING = 2;

	private boolean used;

	public ClueToken() {
		super();
		setOpaque(false);
		used = false;

		int width = LEFT_PADDING + CLUE_DIAM + RIGHT_PADDING;
		int height = VERTICAL_PADDING * 2 + CLUE_DIAM;
		setPreferredSize(new Dimension(width, height));
	}

	public void use() {
		used = true;
		repaint();
	}

	public void getBack() {
		used = false;
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		Ellipse2D circle = new Ellipse2D.Double(LEFT_PADDING, VERTICAL_PADDING, CLUE_DIAM, CLUE_DIAM);

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (used) {
			g2d.setColor(Color.BLACK);
			g2d.setStroke(new BasicStroke((float) 0.5));
			g2d.draw(circle);
		} else {
			g2d.setColor(Color.BLUE);
			g2d.fill(circle);
		}
	}
}

package clientboard;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.util.Iterator;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import color.CardColor;
import shared.HandCard;

@SuppressWarnings("serial")
public class CluePanel extends JPanel {
	
	private static final Color CLUE_COLOR = Color.GRAY;
	
	private HandCard card;
	private int valueIdx;
	private int colorIdx;
	
	public CluePanel(HandCard card) {
		super(new GridLayout(5, 2));
		this.setOpaque(false);
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.card = card;
		
		valueIdx= 0;
		colorIdx = 0;
		
		repopulate();
	}
	
	public void repopulate() {
		this.removeAll();
		forValuesAndColors(addValueClueLabel(), addColorClueLabel());
		this.revalidate();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(CLUE_COLOR);
		g2d.setStroke(new BasicStroke((float) 1.5));
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

		forValuesAndColors(markValueClue(g2d), markColorClue(g2d));
	}
	
	private void forValuesAndColors(Consumer<Integer> valueConsumer, Consumer<CardColor> colorConsumer) {
		Iterator<Integer> values = card.getPossibleValues().keySet().iterator();
		Iterator<CardColor> colors = card.getPossibleColors().keySet().iterator();
		
		while (values.hasNext() && colors.hasNext()) {
			valueConsumer.accept(values.next());
			colorConsumer.accept(colors.next());
		}
	}
	
	private Consumer<Integer> markValueClue(Graphics2D g2d) {
		return i -> {
			if (card.getPossibleValues().get(i) == Boolean.TRUE) {
				double xPos = 12;
				markClue(g2d, valueIdx, xPos);
			}
			valueIdx++;
		};
	}
	
	private Consumer<CardColor> markColorClue(Graphics2D g2d) {
		return c -> {
			if (card.getPossibleColors().get(c) == Boolean.TRUE) {
				double xPos = 47;
				markClue(g2d, colorIdx, xPos);
			}
			colorIdx++;
		};
	}
	
	private void markClue(Graphics2D g2d, int counter, double xPos) {
		double ySpacing = 25.5;
		int idx = counter % 5;
		double yPos = ySpacing * idx + 9;
		g2d.draw(new Ellipse2D.Double(xPos, yPos, 20, 20));
		
//		else {
//			double startX = xPos + 3;
//			double startY = yPos + 3;
//			g2d.draw(new Line2D.Double(startX, startY, startX + 14, startY + 14));
////			g2d.draw(new Line2D.Double(startX + 14, startY, startX, startY + 14));
//		}
	}
	
	private Consumer<Integer> addValueClueLabel() {
		return i -> {
			Boolean target = card.getPossibleValues().get(i);
			addClueLabel(String.valueOf(i), target);
		};
	}
	
	private Consumer<CardColor> addColorClueLabel() {
		return c -> {
			Boolean target = card.getPossibleColors().get(c);
			String letter = String.valueOf(c.toString().toUpperCase().charAt(0));
			addClueLabel(letter, target);
		};
	}
	
	private void addClueLabel(String text, Boolean target) {
		JLabel label = new JLabel("", SwingConstants.CENTER);
		if (target == null || target) {
			label.setText(text);
		}
		label.setForeground(CLUE_COLOR);
		this.add(label);
	}
}

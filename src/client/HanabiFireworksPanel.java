package client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

import color.CardColor;

// TODO: Fireworks at end are smaller/shorter?

@SuppressWarnings("serial")
public class HanabiFireworksPanel extends JPanel {
	
	private LinkedList<Spark> sparks;
	private boolean paintSparks;
	
	public HanabiFireworksPanel() {
		super();
		this.setOpaque(false);
		
		sparks = new LinkedList<Spark>();
		paintSparks = false;
	}
	
	public void fireworkComplete(Point location, CardColor color) {
		int delay = 0;
		int distance = 35;
		
		List<Point> translator = Arrays.asList(
				new Point(-1, -1), 
				new Point(-1, 1), 
				new Point(1, -1), 
				new Point(1, 1), 
				new Point(0, 0));
		
		Collections.shuffle(translator); // random order of fireworks
		for (Point t : translator) {
			int deltaX = distance * t.x;
			int deltaY = distance * t.y;
			explode(new Point(location.x + deltaX, location.y + deltaY), color.getColor(), delay);
			delay += 150;
		}
		
		triggerRepaint();
	}
	
	private void explode(Point p, Color c, int delay) {
		Random random = new Random();
		
		int sparkCount = 50 + random.nextInt(20);
		long lifespan = 1000 + random.nextInt(1000);
		
		paintSparks = false;
		for (int i = 0; i < sparkCount; i++)
			sparks.add(new Spark(this, p, c, lifespan, delay));
		paintSparks = true;
	}
	
	private void triggerRepaint() {
		repaint();
		while (!sparks.isEmpty()) {
			pauseMillis(33);
			repaint();
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (paintSparks && !sparks.isEmpty()) {
			Graphics2D g2d = (Graphics2D) g;
			Spark[] array = sparks.toArray(new Spark[0]);
			
			for(Spark s : array) {
				s.draw(g2d);
			}
		}
	}
	
	public int sparksLeft() {
		return sparks.size();
	}
	
	public boolean removeSpark(Spark s) {
		return sparks.remove(s);
	}
	
	private void pauseMillis(int m) {
		try {
			TimeUnit.MILLISECONDS.sleep(m);
		} catch (InterruptedException e) { }	
	}
}

package clientboard;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.Random;

// Original author Brad Zacher
// From https://github.com/bradzacher/JavaFireworks

public class Spark {
    public double MAX_SPEED = 10;
    public double ACCELERATION;

    private final int MAX_RADIUS = 4;
    private final int MAX_DIAMETER = 2 * MAX_RADIUS;

    private double direction;
    private long spawnTime;
    private Color c;

    private final double x;
    private final double y;

    private Ellipse2D.Double shape;
    private HanabiFireworksPanel parent;

    private long LIFESPAN;

    private int delay;

    public Spark(HanabiFireworksPanel parent, Point p, Color c, long lifespan, int delay) {
        Random random = new Random();

        direction = 360 * random.nextDouble();
        MAX_SPEED = 10 * random.nextDouble() + 5;

        this.parent = parent;
        this.c = c;
        this.LIFESPAN = lifespan + delay;

        this.x = p.x - MAX_RADIUS;
        this.y = p.y - MAX_RADIUS;
        this.shape = new Ellipse2D.Double(0, 0, MAX_DIAMETER, MAX_DIAMETER);

        this.spawnTime = System.currentTimeMillis();

        this.delay = delay;

        this.ACCELERATION = -1.0 / LIFESPAN * MAX_SPEED * 1.1;
    }

    private long currentLifeLength() {
        long currentTime = System.currentTimeMillis();
        return currentTime - spawnTime;
    }

    private void step() {
        long life = currentLifeLength();

        if (life < delay) {
            // Do literally nothing

        } else if (life < LIFESPAN) { // still within life span
            // calculate new speed
            double currentSpeed = MAX_SPEED + ACCELERATION * life;

            // calculate movement
            double dx = currentSpeed * Math.cos(Math.toRadians(direction));
            double dy = currentSpeed * Math.sin(Math.toRadians(direction));

            // move spark
            shape.x += dx;
            shape.y += dy;

            // shrink spark
            double shrink = 1 - (life / (double) LIFESPAN);

            shape.height = MAX_DIAMETER * shrink;
            shape.width = MAX_DIAMETER * shrink;
        } else {
            if (parent.sparksLeft() == 1) {
                parent.repaint();
            }
            parent.removeSpark(this);
        }
    }

    public void draw(Graphics2D g2d) {
        if (currentLifeLength() < delay)
            return;

        step();
        g2d.setColor(c);

        int loops = 80;
        for (int i = loops; i > 0; i--) {
            double scale = i / (double) loops;
            AffineTransform at = AffineTransform.getTranslateInstance(x, y);
            at.scale(scale, scale);

            int a = Math.min((int) Math.round(255 * (1 / scale)), 255);
            Color newColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), a);

            g2d.setColor(newColor);
            g2d.fill(at.createTransformedShape(shape));
        }
    }
}

package clientboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;

import client.InvisiblePanel;
import color.CardColor;
import shared.Card;
import shared.HandCard;

// Wrapper class for Card
// Adds functionality to display Card
@SuppressWarnings("serial")
public class ClientCard extends JPanel {

    private static final int BORDER_SIZE = 5;
    private static final int SMALL_BORDER_SIZE = 3;
    private static final int BORDER_OUTLINE_SIZE = 3;

    public static final Dimension CARD_DIMENSION = new Dimension(90, 150);
    public static final Dimension SMALL_CARD_DIMENSION = new Dimension(60, 100);

    private static Border NORMAL_BORDER = new LineBorder(Color.WHITE, BORDER_SIZE);
    private static Border SMALL_BORDER = new LineBorder(Color.WHITE, SMALL_BORDER_SIZE);

    private HandCard card;
    private boolean selected;
    private int position;

    private MouseListener mouseListener;
    private CluePanel cluePanel;

    private boolean isSmall;
    private boolean hover;

    public ClientCard(Card card, int pos, boolean multi) {
        super();
        this.card = new HandCard(card, multi);
        this.position = pos;
        this.selected = false;

        this.setPreferredSize(CARD_DIMENSION);
        this.setBorder(NORMAL_BORDER);
        this.setBackground(Color.BLUE);
        this.setLayout(new BorderLayout());

        isSmall = false;
        hover = false;
    }

    public int value() {
        return card.value();
    }

    public CardColor color() {
        return card.color();
    }

    public void addClue(String clue, boolean isMe) {
        boolean target = card.addClue(clue);
        if (cluePanel != null)
            cluePanel.repopulate();
        if (!isMe && card.hasClues()) { // don't set border on your own cards
            this.setBorder(clueBorder());
        }
        if (target)
            setSelected(true);
    }

    public boolean matches(String clue) {
        return card.matches(clue);
    }

    public int getPosition() {
        return position;
    }

    public void decrementPosition() {
        position--;
    }

    public String toString() {
        return card.toString();
    }

    public String toMessageString() {
        return card.toMessageString();
    }

    public void setSelected(boolean b) {
        selected = b;
        Color c = selected ? Color.BLACK : Color.BLUE;
        this.setBackground(c);
        this.repaint();
    }

    public void display(boolean showFront) {
        this.removeAll();
        if (showFront) {
            this.add(getNumberPanel(), BorderLayout.NORTH);
            this.add(getNumberPanel(), BorderLayout.SOUTH);
            this.add(getFrontCenterPanel(), BorderLayout.CENTER);
        } else {
            cluePanel = new CluePanel(card);
            this.add(cluePanel, BorderLayout.CENTER);
        }
    }
    
    //this won't update
    public JPanel getCluePanel() {
        return new CluePanel(card);
    }

    public void displaySmall() {
        isSmall = true;
        this.setPreferredSize(SMALL_CARD_DIMENSION);
        this.setBorder(SMALL_BORDER);
        display(true);
    }

    /*
     * ________ |5 5| <- getNumberPanel produces this horizontal panel | | | | | |
     * |5 5| <- also this horizontal panel --------
     */
    private JPanel getNumberPanel() {
        int size = isSmall ? 20 : 30;
        JPanel panel = InvisiblePanel.create(new BorderLayout());

        panel.add(getCardLabel(card.value() + "", size), BorderLayout.WEST);
        panel.add(getCardLabel(card.value() + "", size), BorderLayout.EAST);
        return panel;
    }

    // Makes a text label and sets it to the card's color, with the corret font and
    // stuff
    private JLabel getCardLabel(String text, int size) {
        JLabel label = new JLabel(text);
        label.setForeground(this.color().getColor());
        label.setFont(new Font("Impact", Font.BOLD, size));
        return label;
    }

    // This is the panel in the middle of the card with the "fireworks" on it
    private JPanel getFrontCenterPanel() {
        int size = isSmall ? 45 : 75;
        JPanel panel = InvisiblePanel.create(new GridLayout(3, 3));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        for (int i : CardColor.PATTERNS[card.value() - 1]) {
            if (i == 1) {
                JPanel p = InvisiblePanel.create(new GridBagLayout());
                p.add(getCardLabel("*", size));
                panel.add(p);
            } else
                panel.add(new JLabel());
        }
        return panel;
    }

    public void addMouseListener(MouseListener m) {
        super.addMouseListener(m);
        mouseListener = m;
    }

    public void clean() {
        super.removeMouseListener(mouseListener);
        this.setBorder(NORMAL_BORDER);
    }

    private Border clueBorder() {
        return new CompoundBorder(matteBorder(BORDER_OUTLINE_SIZE, Color.BLACK), matteBorder(BORDER_SIZE, Color.WHITE));
    }

    private Border matteBorder(int size, Color c) {
        return BorderFactory.createMatteBorder(size, size, size, size, c);
    }

    public static JPanel getEmptySpot() {
        JPanel p = InvisiblePanel.create();
        p.setPreferredSize(CARD_DIMENSION);
        p.setBorder(new LineBorder(Color.BLACK, 1));
        return p;
    }

    public static JPanel getBlankCard() {
        JPanel p = new JPanel();
        p.setPreferredSize(CARD_DIMENSION);
        p.setBorder(NORMAL_BORDER);
        p.setBackground(Color.BLUE);
        return p;
    }

    public void setHover(boolean b) {
        hover = b;
    }
    
    public boolean getHover() {
        return hover;
    }
}

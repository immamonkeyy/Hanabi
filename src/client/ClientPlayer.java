package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import clientboard.ClientCard;
import shared.Card;

public class ClientPlayer {
	
	private List<ClientCard> hand;
	private ClientPlayer nextPlayer;
	private String playerName;
	private boolean isMe;
	
	private JPanel buttonPanel;
	private JButton[] buttons;
	
	private JPanel turnPanel;
	private JPanel cardPanel;
	
	private boolean buttonPanelSizeSet;
	private boolean multicolor;

	public ClientPlayer(String name, boolean isMe, JButton[] buttons, JPanel buttonPanel, boolean multi) {
		playerName = name;
		this.isMe = isMe;
		hand = new ArrayList<ClientCard>();
		buttonPanelSizeSet = false;
		multicolor = multi;
		
		this.buttons = buttons;
		this.buttonPanel = buttonPanel;
		
		cardPanel = InvisiblePanel.create();
		
		turnPanel = InvisiblePanel.create(new BorderLayout());
		turnPanel.add(new JLabel(playerName), BorderLayout.NORTH);
		turnPanel.add(cardPanel, BorderLayout.CENTER);
		turnPanel.setBackground(Color.GREEN);
	}
	
	// ClientPlayer must instantiate the card to get its position
	public ClientCard addCard(Card card) {
		ClientCard c = new ClientCard(card, hand.size(), multicolor);
		hand.add(c);
		cardPanel.add(c);

		hideButtons();

		boolean showCards = isMe ? false : true;
		c.display(showCards);
		
		cardPanel.revalidate();
		cardPanel.repaint();
		
		return c;
	}

	public void setNextPlayer(ClientPlayer p) {
		nextPlayer = p;
	}

	public ClientPlayer getNextPlayer() {
		return nextPlayer;
	}

	public String getPlayerName() {
		return playerName;
	}
	
	public List<ClientCard> getHand() {
		return hand;
	}
	
	public String toString() {
		return playerName;
	}
	
	public void buttonsVisible(boolean visible, boolean cluesFull) {
		if (buttonPanel.getHeight() == 0) return;

		// TODO: Figure out this ish
		if (!buttonPanelSizeSet) {
			buttonPanel.setPreferredSize(buttonPanel.getSize());
			buttonPanelSizeSet = true;
		}
		
		for (JButton b : buttons) {
			if (b.getText().toLowerCase().equals("discard")) {
				if (!cluesFull) b.setVisible(visible);
			}
			else b.setVisible(visible);
		}
	}
	
	public void showButtons(boolean cluesFull) {
		buttonsVisible(true, cluesFull);
	}
	
	// When hiding buttons, doesn't matter if clues are full or not
	public void hideButtons() {
		buttonsVisible(false, false);
	}
	
	public void highlight(boolean isMyTurn) {
		turnPanel.setOpaque(isMyTurn);
		turnPanel.repaint();
	}
    
    public JPanel getPlayerPanel() {
		JPanel playerPanel = InvisiblePanel.create(new BorderLayout());
		playerPanel.add(turnPanel, BorderLayout.CENTER);
		
		String buttonPosition = isMe ? BorderLayout.NORTH : BorderLayout.SOUTH;
		playerPanel.add(buttonPanel, buttonPosition);
    		return playerPanel;
    }

	public ClientCard removeCard(int position) {
		ClientCard removed = hand.remove(position);
		for (ClientCard c : hand) {
			if (c.getPosition() > position)
				c.decrementPosition();
		}
		
		cardPanel.remove(position);
		cardPanel.revalidate();
		cardPanel.repaint();
		
		return removed;
	}

	public void clueGiven(String clue) {
		for (ClientCard card : hand) {
			card.addClue(clue, isMe);
		}
	}

}

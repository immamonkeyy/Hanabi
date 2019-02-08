package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import server.Card;

public class ClientPlayer {
	
	private List<ClientCard> hand;
	private ClientPlayer nextPlayer;
	private String playerName;
	private boolean isMe;
	
	private JPanel buttonPanel;
	private JPanel turnPanel;
	private JPanel cardPanel;
	
	private boolean buttonPanelSizeSet;

	public ClientPlayer(String name, boolean isMe, JPanel givenButtonPanel) {
		playerName = name;
		this.isMe = isMe;
		hand = new ArrayList<ClientCard>();
		buttonPanelSizeSet = false;
		
		buttonPanel = givenButtonPanel;
		
		cardPanel = Client.invisiblePanel();
		
		turnPanel = Client.invisiblePanel(new BorderLayout());
		turnPanel.add(new JLabel(playerName), BorderLayout.NORTH);
		turnPanel.add(cardPanel, BorderLayout.CENTER);
		turnPanel.setBackground(Color.GREEN);
	}
	
	// ClientPlayer must instantiate the card to get its position
	public ClientCard addCard(Card card) {
		ClientCard c = new ClientCard(card, hand.size());
		hand.add(c);
		cardPanel.add(c);

		buttonsVisible(false);

		boolean showCards = true; //isMe ? false : true;
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
	
	public void buttonsVisible(boolean b) {
		if (buttonPanel.getHeight() == 0) return;

		if (!buttonPanelSizeSet) {
			buttonPanel.setPreferredSize(buttonPanel.getSize());
			buttonPanelSizeSet = true;
		}
		
		for (Component c : buttonPanel.getComponents()) c.setVisible(b);
	}
	
	public void highlight(boolean turn) {
		if (turn) turnPanel.setOpaque(true);
		else turnPanel.setOpaque(false);
		turnPanel.repaint();
	}
    
    public JPanel getPlayerPanel() {
		JPanel playerPanel = Client.invisiblePanel(new BorderLayout());
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

}

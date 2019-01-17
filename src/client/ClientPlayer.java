package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
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
	
	private JPanel playerPanel;
	private JPanel buttonPanel;
	private JPanel turnPanel;
	private JPanel cardPanel;
	
	private Dimension buttonPanelPreferredSize;

	public ClientPlayer(String name, boolean isMe, JPanel givenButtonPanel) {
		playerName = name;
		this.isMe = isMe;
		hand = new ArrayList<ClientCard>();
		
		buttonPanel = givenButtonPanel;
		
		cardPanel = getInvisiblePanel(null);
		
		turnPanel = new JPanel(new BorderLayout());
		turnPanel.add(new JLabel(playerName), BorderLayout.NORTH);
		turnPanel.add(cardPanel, BorderLayout.CENTER);
		turnPanel.setBackground(Color.GREEN);
		
		playerPanel = getInvisiblePanel(new BorderLayout());
		playerPanel.add(turnPanel, BorderLayout.CENTER);
		
		String position = isMe ? BorderLayout.NORTH : BorderLayout.SOUTH;
		playerPanel.add(buttonPanel, position);
	}
	
	public ClientCard addCard(Card card) {
		ClientCard c = new ClientCard(card, hand.size());
		hand.add(c);
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
		if (buttonPanelPreferredSize == null) {
			buttonPanelPreferredSize = buttonPanel.getSize();
			buttonPanel.setPreferredSize(buttonPanelPreferredSize);
		}
		
		for (Component c : buttonPanel.getComponents()) c.setVisible(b);
	}
	
    public JPanel getPlayerPanel(boolean turn) {
		if (turn) turnPanel.setOpaque(true);
		else turnPanel.setOpaque(false);
		
		cardPanel.removeAll();
		boolean showCards = isMe ? false : true;
	    	for (ClientCard card : hand) {
	    		cardPanel.add(card);
			card.display(true);
	    }
	    	cardPanel.revalidate();
	    	cardPanel.repaint();
		
		return playerPanel;
	}
    
    private JPanel getInvisiblePanel(LayoutManager l) {
		JPanel p = new JPanel();
		p.setOpaque(false);
		if (l != null) p.setLayout(l);
		return p;
    }

	public ClientCard removeCard(int position) {
		ClientCard removed = hand.remove(position);
		for (ClientCard c : hand) {
			if (c.getPosition() > position)
				c.decrementPosition();
		}
		return removed;
	}

}

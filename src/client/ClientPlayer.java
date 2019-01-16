package client;

import java.awt.BorderLayout;
import java.awt.Color;
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
	private JPanel buttonPanel;
	private boolean isMe;

	public ClientPlayer(String name, boolean isMe, JPanel buttonPanel) {
		playerName = name;
		this.isMe = isMe;
		hand = new ArrayList<ClientCard>();
		
		this.buttonPanel = buttonPanel;
		this.buttonPanel.setOpaque(false);
		this.buttonPanel.setVisible(false);
	}
	
	public void draw(Card card) {
		hand.add(new ClientCard(card, hand.size()));
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
		buttonPanel.setVisible(b);
	}
	
    public JPanel getPlayerPanel(boolean turn) {
		JPanel playerPanel = getInvisiblePanel(new BorderLayout());
		if (turn) {
			playerPanel.setOpaque(true);
			playerPanel.setBackground(Color.GREEN);
		}
		
		playerPanel.add(new JLabel(playerName), BorderLayout.NORTH);
		
		JPanel cardPanel = getInvisiblePanel(null);
		
		boolean showCards = isMe ? false : true;
	    	for (ClientCard card : hand) {
	    		cardPanel.add(card);
			card.display(showCards);
	    }
    	
		playerPanel.add(cardPanel, BorderLayout.CENTER);
		
		if (!isMe) {
			playerPanel.add(buttonPanel, BorderLayout.SOUTH);
			return playerPanel;
		}
		
		JPanel largerPanel = getInvisiblePanel(new BorderLayout());
		largerPanel.add(playerPanel, BorderLayout.CENTER);
		largerPanel.add(buttonPanel, BorderLayout.NORTH);
		return largerPanel;
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

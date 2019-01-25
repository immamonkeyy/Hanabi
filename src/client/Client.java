package client;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import color.CardColor;
import server.Card;
import shared.Commands;

/*
 * Client needs to know:
 * -How many clues there are left
 * -How many fuckups there are left
 * -Opponents cards
 * -What opponents have been told
 * -State of fireworks
 * -What cards have been discarded (safe vs unsafe)
 * -What clues they know
 * -Whose turn it is, whose turn is next
 */


//TODO: Let fireworks animations finish before card animations

public class Client {
	
    private static int PORT = 8901;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    
    private AllPlayers players;
    ClientPlayer selectedPlayer;
    List<ClientCard> selectedCards;
	
	private JPanel playersCards;
	private JPanel myCards;
	
	private String myName;
	private JDialog dialog;
	private ClientBoard board;
	
	private boolean freeze;
	
	private HanabiFireworksPanel fireworksPanel;
    
    private static final Color BOARD_COLOR = new Color(0, 153, 0);
	
	public Client(String serverAddress) throws Exception {
        socket = new Socket(serverAddress, PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        
        players = new AllPlayers();
        selectedPlayer = null;
        selectedCards = new ArrayList<ClientCard>();
        
		playersCards = Client.invisiblePanel();
		((FlowLayout)playersCards.getLayout()).setHgap(20);
		
		myCards = Client.invisiblePanel();
		
		fireworksPanel = new HanabiFireworksPanel();
		
		myName = null;
		dialog = null;
		board = new ClientBoard();
		freeze = false;
	}

    public void play() throws Exception {
        String response;
        try {
            while (true) {
                response = in.readLine();
                if (response == null) continue;

                if (handleResponse(Commands.ENTER_NAME, response, () -> {
	            		if (myName == null) myName = getName();
	            		if (myName == null) return;
	        			out.println(myName);
                })) continue;

                if (handleResponse(Commands.ADD_EXISTING_PLAYER, response, name -> {
                		addPlayer(name);
                })) continue;
                
                if (handleResponse(Commands.PLAYER_JOINED, response, name -> {
	                	addPlayer(name);
	                	introThread();
                })) continue;
                
                if (handleResponse(Commands.CHOOSE_STARTING_PLAYER, response, () -> {
                		introThread();
                })) continue;
                
                if (handleResponse(Commands.START_GAME, response, startingPlayer -> {
	                	players.startGame(startingPlayer);
	                	closeDialog();
	                	try {
	                		SwingUtilities.invokeAndWait(() -> drawBoard());
	                	} catch (InvocationTargetException | InterruptedException e) {
	                		e.printStackTrace();
	                	}
                })) continue;
                
                if (handleResponse(Commands.VALID_PLAY, response, input -> {
	                	handlePlayerCard(input, (playerName, position) -> {
	            			freeze = true;
	            			ClientCard played = removePlayerCard(playerName, position);
	            			board.validPlay(played);
	            		});
                })) continue;
                
                if (handleResponse(Commands.INVALID_PLAY, response, input -> {
	                handlePlayerCard(input, (playerName, position) -> {
	            			freeze = true;
	            			ClientCard played = removePlayerCard(playerName, position);
	            			board.invalidPlay(played);
	            		});
                })) continue;

                if (handleResponse(Commands.DRAW_CARD, response, input -> {
                		handlePlayerCard(input, (playerName, cardStr) -> {
                			addPlayerCard(playerName, cardStr);
                		});
                })) continue;

                if (handleResponse(Commands.NEXT_TURN, response, () -> {
	                freeze = false;
	                players.nextTurn();
                })) continue;
                
                if (handleResponse(Commands.FIREWORK_COMPLETE, response, input -> {
		            CardColor color = CardColor.fromString(input);
		            Point location = board.getLocation(color);
		            fireworksPanel.fireworkComplete(location, color);
                })) continue;
            }
        }
        finally {
            socket.close();
        }
    }
    
    private boolean handleResponse(String command, String response, Runnable handler) {
    		if (!response.startsWith(command)) return false;
    		handler.run();
    		return true;
    }
    
    private boolean handleResponse(String command, String response, Consumer<String> handler) {
    		if (!response.startsWith(command)) return false;
    		String input = response.substring(command.length());
    		handler.accept(input);
    		return true;
    }
    
    //To handle responses like "PlayerName:Value"
    private void handlePlayerCard(String input, BiConsumer<String, String> handler) {
    		int split = input.indexOf(':');
    		String playerName = input.substring(0, split);
    		String value = input.substring(split + 1);
    		handler.accept(playerName, value);
    }
    
    private void clearSelected() {
		if (selectedPlayer != null) {
	    		selectedPlayer.buttonsVisible(false);
			selectedPlayer = null;
			selectedCards.clear();
		}
    }
    
    private void addPlayer(String name) {
    		JPanel buttonPanel = Client.invisiblePanel();
    		boolean isMe = name.equals(myName);
    	
    		if (isMe) {
    			JButton play = new JButton("Play");
    			buttonPanel.add(play);
    			play.addActionListener(e -> out.println(Commands.PLAY + selectedCards.get(0).getPosition()));
    			
    			JButton discard = new JButton("Discard");
    			buttonPanel.add(discard);
    			discard.addActionListener(e -> out.println(Commands.DISCARD + selectedCards.get(0).getPosition()));
    			
    		} else {
    			JButton clue = new JButton("Give Clue");
    			buttonPanel.add(clue);
    			clue.addActionListener(e -> out.println(Commands.CLUE + selectedPlayer));
    		}

    		ClientPlayer player = new ClientPlayer(name, isMe, buttonPanel);
    		players.addPlayer(player);
    }
    
    private void introThread() {
    		if (players.names().size() == 1) {
    			waitingForMorePlayersThread();
    		} else chooseStartingPlayerThread();
    }
    
    private void waitingForMorePlayersThread() {
		new Thread(() -> waitingForMorePlayers()).start();
    }
    
    private void chooseStartingPlayerThread() {
		new Thread(() -> {
			String startingPlayer = chooseStartingPlayer();

			if (startingPlayer != null) {
				out.println(Commands.CHOOSE_STARTING_PLAYER + startingPlayer);
			}
		}).start();
    }
    
    private ClientCard removePlayerCard(String playerName, String position) {
    		ClientPlayer player = players.get(playerName);
		ClientCard card = player.removeCard(Integer.parseInt(position));
		card.clean();
		clearSelected();
		return card;
    }
    
    private void addPlayerCard(String playerName, String cardStr) {
    		ClientPlayer player = players.get(playerName);
		ClientCard card = player.addCard(getCard(cardStr));
		card.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (validClick(player)) {
					if (selectedPlayer == null) {
						selectedPlayer = player;
						selectedPlayer.buttonsVisible(true);
					}
					
					if (!players.get(myName).equals(selectedPlayer) || // selected another player's cards
							selectedCards.isEmpty() || // selected my own card, first one
							selectedCards.contains(card)) { // deselected my own card
						
						if (selectedCards.contains(card)) {
							card.setSelected(false);
							selectedCards.remove(card);
							if (selectedCards.isEmpty()) {
								selectedPlayer.buttonsVisible(false);
								selectedPlayer = null;
							}
						} else {
							card.setSelected(true);
							selectedCards.add(card);
						}
					}
				}
			}
		});
    }
    
    private Card getCard(String cardStr) {
    		int value = Integer.parseInt(cardStr.substring(0, 1));
		String color = cardStr.substring(1);
		return new Card(CardColor.fromString(color), value);
    }
    
    private void populateCards() {
    		ClientPlayer me = players.get(myName);
    		
    		myCards.add(me.getPlayerPanel());
    		myCards.revalidate();
    		myCards.repaint();
    		
		ClientPlayer player = me;
		while (!player.getNextPlayer().equals(me)) {
			player = player.getNextPlayer();
			playersCards.add(player.getPlayerPanel());
		}

		playersCards.revalidate();
		playersCards.repaint();
    }

    private boolean validClick(ClientPlayer player) {
    		// Board is frozen (in between turns)
    		if (freeze) return false;
    	
    		// Not my turn
    		if (!players.isTurn(myName))
    			return false;
    		
    		// Already selected a different player first
    		if (selectedPlayer != null && !selectedPlayer.equals(player))
    			return false;
    		
    		return true;
    }
    
    private void drawBoard() {
    		JFrame window = new JFrame(myName);
    		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    		window.setSize(600, 670);
    		
    		JPanel view = new JPanel(new BorderLayout());
    		view.setBackground(BOARD_COLOR);
    		window.add(view);
    		
    		view.add(playersCards, BorderLayout.NORTH);
    		view.add(myCards, BorderLayout.SOUTH);
    		view.add(board, BorderLayout.CENTER);
    		
    		JPanel glass = (JPanel) window.getGlassPane();
    		glass.setLayout(new BorderLayout());
    		glass.add(fireworksPanel, BorderLayout.CENTER);
    		glass.setVisible(true);
    		
    		window.setVisible(true);
    		
    		populateCards();
    		board.saveCardLocationsRelativeTo(view);
    }

    private void closeDialog() {
		if (dialog != null) dialog.dispose();
    }
    
    private void waitingForMorePlayers() {
    		closeDialog();
    		String message = "Waiting for more players...";    		
    		JOptionPane pane = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
    		
    		dialog = pane.createDialog(null, myName);
    		
    		pane.selectInitialValue();
    		dialog.setVisible(true);
    		dialog.dispose();
    }
    
    private String chooseStartingPlayer() {
    		closeDialog();
    		
    		String message = "Who is the most colorful? ";
    		for (String name : players.names()) {
    			message += name + " ";
    		}
    	        
        JOptionPane pane = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION);
		pane.setWantsInput(true);
		
		dialog = pane.createDialog(null, myName);
		
		pane.selectInitialValue();
		dialog.setVisible(true);
		dialog.dispose();
		
		Object value = pane.getInputValue();
		
		if (value == JOptionPane.UNINITIALIZED_VALUE) {
			return null;
		}
		return (String) value;
    }
    
    private String getName() {
    		return JOptionPane.showInputDialog(
	            null,
	            "Choose a screen name:",
	            "Screen name selection",
	            JOptionPane.PLAIN_MESSAGE);
    }
    
    public static void main(String[] args) throws Exception {
        new Client("localhost").play();
    }
    
    ////////////////////////////////////////////////////////////////
    
	public static JPanel invisiblePanel() {
		return invisiblePanel(new FlowLayout());
	}

	public static JPanel invisiblePanel(LayoutManager layout) {
		JPanel p = new JPanel(layout);
		p.setOpaque(false);
		return p;
	}
}

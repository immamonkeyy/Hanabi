package client;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

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
    
    private static final Color BOARD_COLOR = new Color(0, 153, 0);
	
	public Client(String serverAddress) throws Exception {
        socket = new Socket(serverAddress, PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        
        players = new AllPlayers();
        selectedPlayer = null;
        selectedCards = new ArrayList<ClientCard>();
        
		playersCards = invisiblePanel();
		((FlowLayout)playersCards.getLayout()).setHgap(20);
		
		myCards = invisiblePanel();
		
		myName = null;
		dialog = null;
		board = new ClientBoard();
	}
	
	public Client(String serverAddress, String name) throws Exception {
        this(serverAddress);
        myName = name;
	}

    public void play() throws Exception {
        String response;
        try {
            while (true) {
                response = in.readLine();
                if (response == null) continue;
                else if (response.startsWith(Commands.ENTER_NAME)) {
                		if (myName == null) myName = getName();
                		if (myName == null) return;
            			out.println(myName);

                } else if (response.startsWith(Commands.ADD_EXISTING_PLAYER)) {
            			String playerJoined = response.substring(Commands.ADD_EXISTING_PLAYER.length());
            			addPlayer(playerJoined);
            			
                } else if (response.startsWith(Commands.PLAYER_JOINED)) {
                		String playerJoined = response.substring(Commands.PLAYER_JOINED.length());                		
                		addPlayer(playerJoined);
                		chooseStartingPlayerThread();
                		
                } else if(response.startsWith(Commands.CHOOSE_STARTING_PLAYER)) {
                		chooseStartingPlayerThread();

                } else if (response.startsWith(Commands.START_GAME)) {
                		String startingPlayer = response.substring(Commands.START_GAME.length());
                		players.startGame(startingPlayer);
                		closeDialog();
                		SwingUtilities.invokeAndWait(new Runnable() {
                			public void run() {
                				drawBoard();
                			}
                		});
                		
                } else if(response.startsWith(Commands.VALID_PLAY)) {
                		handleResponse(response, Commands.VALID_PLAY, (playerName, position) -> {
                			ClientCard played = removePlayerCard(playerName, position);
                			board.validPlay(played);
                			board.update();
                		});
            			
                } else if(response.startsWith(Commands.INVALID_PLAY)) {
	            		handleResponse(response, Commands.INVALID_PLAY, (playerName, position) -> {
	            			ClientCard played = removePlayerCard(playerName, position);
	            			board.invalidPlay(played);
	            		});
                		
                } else if (response.startsWith(Commands.DRAW_CARD)) {
	            		handleResponse(response, Commands.DRAW_CARD, (playerName, cardStr) -> {
	                		addPlayerCard(playerName, cardStr);
	            		});
	            		
                } else if(response.startsWith(Commands.NEXT_TURN)) {
                		players.nextTurn();
                		clearSelected();

                		if (players.getTurn().getPlayerName().equals(myName)) {
                			updatePlayersCards();
                    		updateMyCards();
                		} else {
                			updateMyCards();
                    		updatePlayersCards();
                		}
                }
            }
        }
        finally {
            socket.close();
        }
    }
    
    private void clearSelected() {
    		if (selectedPlayer != null) {
	    		selectedPlayer.buttonsVisible(false);
			selectedPlayer = null;
			selectedCards.clear();
    		}
    }
    
    //To handle responses like "COMMAND PlayerName:Value"
    private void handleResponse(String response, String command, BiConsumer<String, String> handler) {
    		String input = response.substring(command.length());
    		int split = input.indexOf(':');
    		String playerName = input.substring(0, split);
    		String value = input.substring(split + 1);
    		
    		handler.accept(playerName, value);
    }
    
    private void addPlayer(String name) {
    		JPanel buttonPanel = invisiblePanel();
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
    
    private void chooseStartingPlayerThread() {
		new Thread(() -> {
			String startingPlayer = chooseStartingPlayer();

			if (startingPlayer != null) {
				out.println(Commands.CHOOSE_STARTING_PLAYER + startingPlayer);
			}
		}).start();
    }
    
    private ClientCard removePlayerCard(String playerName, String position) {
		ClientCard card = players.get(playerName).removeCard(Integer.parseInt(position));
		card.removeMouseListener();
		if (playerName.equals(myName)) updateMyCards();
		else updatePlayersCards();
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
						
						card.click();
						if (selectedCards.contains(card)) {
							selectedCards.remove(card);
							if (selectedCards.isEmpty()) {
								selectedPlayer.buttonsVisible(false);
								selectedPlayer = null;
							}
						} else selectedCards.add(card);
					}
				}
			}
		});
		if (playerName.equals(myName)) {
			updateMyCards();
		}
		else {
			updatePlayersCards();
		}
    }
    
    private Card getCard(String cardStr) {
    		int value = Integer.parseInt(cardStr.substring(0, 1));
		String color = cardStr.substring(1);
		return new Card(CardColor.fromString(color), value);
    }
    
    private void updatePlayersCards() {
//    		playersCards.setMinimumSize(playersCards.getSize());
    		playersCards.removeAll();
		
		ClientPlayer me = players.get(myName);
		ClientPlayer player = me;
		while (!player.getNextPlayer().equals(me)) {
			player = player.getNextPlayer();
			playersCards.add(getPlayerPanel(player));
			player.buttonsVisible(false);
		}

		playersCards.revalidate();
		playersCards.repaint();
    }
    
    private JPanel getPlayerPanel(ClientPlayer player) {
    		boolean turn = players.getTurn().equals(player);
    		return player.getPlayerPanel(turn);
    }
    
    private void updateMyCards() {
    		//myCards.setMinimumSize(myCards.getSize());
    		myCards.removeAll();
    		
    		myCards.add(getPlayerPanel(players.get(myName)));
    		players.get(myName).buttonsVisible(false);
    		myCards.revalidate();
    		myCards.repaint();
    }
    
    private boolean validClick(ClientPlayer player) {
    		// Not my turn
    		if (!players.getTurn().getPlayerName().equals(myName))
    			return false;
    		
    		// Already selected a different player first
    		if (selectedPlayer != null && !selectedPlayer.equals(player))
    			return false;
    		
    		return true;
    }
    
    private void drawBoard() {
    		JFrame frame = new JFrame(myName);
    		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    		frame.setSize(600, 670);
    		
    		JPanel view = new JPanel();
    		view.setBackground(BOARD_COLOR);
    		view.setLayout(new BorderLayout());
    		frame.add(view);
    		
    		view.add(playersCards, BorderLayout.NORTH);
    		view.add(myCards, BorderLayout.SOUTH);
    		view.add(board, BorderLayout.CENTER);
    		
    		frame.setVisible(true);
    }
    
    private JPanel invisiblePanel() {
		JPanel p = new JPanel();
		p.setOpaque(false);
		return p;
    }

    private void closeDialog() {
		if (dialog != null) {
			dialog.dispose();
		}
    }
    
    private String chooseStartingPlayer() {
    		closeDialog();
    		
    		String names = "";
    		for (String name : players.names()) {
    			names += name + " ";
    		}
    	
        return showInputDialog("Who is the most colorful? " + names);
    }
    
    private String showInputDialog(String message) {
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
        Client client;
        
        if (args.length == 1) {
        		System.out.println("Starting client with name " + args[0]);
        		client = new Client("localhost", args[0]);
        }
        else if (args.length == 2) {
        		client = new Client(args[1], args[2]);
        }
        else client = new Client("localhost");
        		
        client.play();
    }
}

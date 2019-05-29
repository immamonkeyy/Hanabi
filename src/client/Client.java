package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingUtilities;

import clientboard.ClientBoard;
import clientboard.ClientCard;
import clientboard.GameLog;
import clientboard.HanabiFireworksPanel;
import color.CardColor;
import shared.Card;
import shared.Commands;
import shared.Util;

/*
 * Client needs to know:
 * -How many clues there are left DONE
 * -How many fuckups there are left
 * -Opponents cards DONE
 * -What opponents have been told NOT NEEDED
 * -State of fireworks DONE
 * -What cards have been discarded (safe vs unsafe)
 * -What clues they know DONE
 * -Whose turn it is, whose turn is next DONE
 */

//TODO: Let fireworks animations finish before card animations
//TODO: Display discards
//TODO: Displaying multi or maybe pictures for cards? Also pictures for tokens
//TODO: If you resize the window, the fireworks animation is in the wrong spot :(
//TODO: Set minimum size so can't resize too small
//TODO: Fix organization, this class is huge
//TODO: If clues are filled, disable discard button
//TODO: End game if use up all fuckup tokens
//TODO: Win game with all 5s
//TODO: Ability to repeat game with same deck order
//TODO: Hover over other players cards to show what they know?
//TODO: Game log?
//TODO: Show other clues
//TODO: Fix layout issues
//TODO: End game when fuckups run out
//TODO: End game on all 5s, end game when you run out of cards, let players have extra turns after cards end
//TODO: Add in variants...OMG WHAT?!

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
	
	private boolean multicolor;
	private int clueCount;
	private int fuckupCount;
	
	private HanabiFireworksPanel fireworksPanel;
	private JFrame window;
	
	GameLog log;
    
    private static final Color BOARD_COLOR = new Color(0, 153, 0);
	
	public Client(String serverAddress) throws Exception {
        socket = new Socket(serverAddress, PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        
        players = new AllPlayers();
        selectedPlayer = null;
        selectedCards = new ArrayList<ClientCard>();
        
		playersCards = InvisiblePanel.create();
		((FlowLayout)playersCards.getLayout()).setHgap(20);
		
		myCards = InvisiblePanel.create();
		
		fireworksPanel = new HanabiFireworksPanel();
		log = new GameLog();
		
		myName = null;
		dialog = null;
		freeze = false;
	}

    public void play() throws Exception {
        String response;
        try {
            while (true) {
                response = in.readLine();
                if (response == null) continue;

                Util.handleResponse(Commands.ENTER_NAME, response, () -> {
	            		if (myName == null) myName = getName();
	            		if (myName == null) return;
	        			out.println(myName);
                });

                Util.handleResponse(Commands.ADD_EXISTING_PLAYER, response, name -> {
                		addPlayer(name);
                });
                
                Util.handleResponse(Commands.PLAYER_JOINED, response, name -> {
	                	addPlayer(name);
	                	intro();
                });
                
                Util.handleResponse(Commands.CHOOSE_STARTING_PLAYER, response, () -> {
                		intro();
                });
                
                Util.handleResponse(Commands.SET_MULTI, response, multi -> {
                		multicolor = Boolean.parseBoolean(multi);
                });
                
                Util.handleResponse(Commands.SET_CLUES, response, clues -> {
            			clueCount = Integer.parseInt(clues);
                });
                
                Util.handleResponse(Commands.SET_FUCKUPS, response, fuckups -> {
            			fuckupCount = Integer.parseInt(fuckups);
                });
                
                Util.handleResponse(Commands.START_GAME, response, startingPlayer -> {
                		log.append(action(startingPlayer, "start", "the game!"));
	                	players.startGame(startingPlayer);
	                	closeDialog();
	                	try {
	                		SwingUtilities.invokeAndWait(() -> drawBoard());
	                	} catch (InvocationTargetException | InterruptedException e) {
	                		e.printStackTrace();
	                	}
                });
                
                Util.handleResponse(Commands.VALID_PLAY, response, input -> {
	                	Util.handlePlayerCard(input, (playerName, position) -> {
	            			freeze = true;
	            			ClientCard played = removePlayerCard(playerName, position);
	            			board.validPlay(played);
	            			log.append(action(playerName, "play", played.toMessageString()));
	            		});
                });
                
                Util.handleResponse(Commands.VALID_DISCARD, response, input -> {
                	Util.handlePlayerCard(input, (playerName, position) -> {
            			freeze = true;
            			ClientCard played = removePlayerCard(playerName, position);
            			String message = action(playerName, "discard", played.toMessageString());
            			showAutoCloseMessageDialog(message);
            			board.discard(played);
            			log.append(message);
            		});
            });
                
                Util.handleResponse(Commands.INVALID_PLAY, response, input -> {
	                Util.handlePlayerCard(input, (playerName, position) -> {
	            			freeze = true;
	            			ClientCard played = removePlayerCard(playerName, position);
	            			String message = "INVALID: " + action(playerName, "play", played.toMessageString());
	            			showAutoCloseMessageDialog(message);
	            			board.invalidPlay(played);
	            			log.append(action(playerName, "play", played.toMessageString() + " (invalid)"));
	            		});
                });

                Util.handleResponse(Commands.DRAW_CARD, response, input -> {
                		Util.handlePlayerCard(input, (playerName, cardStr) -> {
                			addPlayerCard(playerName, cardStr);
                		});
                });

                Util.handleResponse(Commands.NEXT_TURN, response, () -> {
                		closeDialog();
                		clearSelected();
                		freeze = false;
                		players.nextTurn();
                });
                
                Util.handleResponse(Commands.FIREWORK_COMPLETE, response, input -> {
                		board.getAClueBack();
		            CardColor color = CardColor.fromString(input);
		            Point location = board.getLocation(color);
		            fireworksPanel.fireworkComplete(location, color);
		            log.append(color.toString() + " firework complete!");
                });
                
                Util.handleResponse(Commands.CLUE, response, input -> {
	            		Util.handlePlayerCard(input, (playerName, clue) -> {
	            			freeze = true;
	            			board.useClue();
	            			selectedPlayer = players.get(playerName);
	            			announceClueGiven(playerName, clue);
	            			selectedPlayer.clueGiven(clue);
	            		});
                });
                
                Util.handleResponse(Commands.CARDS_LEFT, response, input -> {
		            int cardsLeft = Integer.parseInt(input);
		            board.setRemainingCards(cardsLeft);
                });
            }
        }
        finally {
            socket.close();
        }
    }
    
    private String recipient(String n) {
    		return n.equals(myName) ? "you" : n;
    }
    
    private String action(String name, String verb, String rest) {
    		return action(name, verb, verb + "s", rest);
    }
    
    private String action(String name, String verbSecond, String verbThird, String rest) {
		String str;
		if (name.equals(myName)) {
			str = "you " + verbSecond;
		} else str = name + " " + verbThird;
		return str + " " + rest;
}
    
    private void announceClueGiven(String playerName, String clue) {
    		String message = action(players.turn().getPlayerName(), "tell", recipient(playerName) + " about: " + clue);
    		showAutoCloseMessageDialog(message);
    		log.append(message);
    }
    
    private void clearSelected() {
    		board.clearSelected();
		if (selectedPlayer != null) {
			// when clue was given, their selected cards won't be in the
			// selectedCards list so we just deselect all the cards in their hand
			for (ClientCard c : selectedPlayer.getHand()) {
				c.setSelected(false);
			}
	    		selectedPlayer.hideButtons();;
			selectedPlayer = null;
			selectedCards.clear();
		}
    }
    
    private void addPlayer(String name) {
    		JPanel buttonPanel = InvisiblePanel.create();
    		JButton[] buttons;
    		boolean isMe = name.equals(myName);
    	
    		if (isMe) {
    			buttons = new JButton[2];
    			buttons[0] = new JButton("Play");
    			buttons[0].addActionListener(e -> out.println(Commands.PLAY + selectedCards.get(0).getPosition()));
    			
    			buttons[1] = new JButton("Discard");
    			buttons[1].addActionListener(e -> out.println(Commands.DISCARD + selectedCards.get(0).getPosition()));
    			
    		} else {
    			buttons = new JButton[1];
    			buttons[0] = new JButton("Give Clue");
    			buttons[0].addActionListener(e -> giveClue(buttonPanel));
    		}

    		for (JButton b : buttons) buttonPanel.add(b);
    		ClientPlayer player = new ClientPlayer(name, isMe, buttons, buttonPanel, multicolor);
    		players.addPlayer(player);
    		log.append("Adding player \"" + name + "\"" + (isMe ? " (you!)" : ""));
    }
    
    private void giveClue(JPanel p) {
    		Set<String> possibleColors = new HashSet<>();
    		Set<String> possibleValues = new HashSet<>();
    		
    		for (ClientCard c : selectedCards) {
    			possibleValues.add(String.valueOf(c.value()));
    			if (c.color() != CardColor.MULTI) {
    				possibleColors.add(c.color().toString());
    			}
    		}
    		
    		if (possibleColors.size() > 1 && possibleValues.size() > 1) {
    			showMessageDialog("Invalid card selection.");
    			return;
    		}
    		
    		List<String> options = new ArrayList<>();
    		if (possibleValues.size() == 1) {
    			options.addAll(possibleValues);
    		}
    		if (possibleColors.size() == 1) {
    			options.addAll(possibleColors);
    		}
    		if (possibleColors.isEmpty()) { // all multicolor
    			for (CardColor c : CardColor.getAllColors(false)) {
    				options.add(c.toString());
    			}
    		}
    		
    		String defaultOption = null;
    		if (options.size() > 1) {
    			defaultOption = "Select a clue";
    			options.add(0, defaultOption);
    		}
    		
    		String clue;
    		String message = "Give clue to "  + selectedPlayer + ":";
    		boolean validClue = false;
    		do {
    			clue = (String) JOptionPane.showInputDialog(p, message, myName, JOptionPane.PLAIN_MESSAGE, 
    					null, options.toArray(new String[options.size()]), defaultOption);
    			
    			if (clue == null) return; // user hit cancel
    			
        		if (clue.equals(defaultOption)) continue;
        		if (!options.contains(clue.toLowerCase())) continue;
        		if (nonComprehensiveClue(clue)) {
        			showMessageDialog("Invalid card selection for clue \"" + clue + "\"");
        			return;
        		}
        		
        		validClue = true;
    		} while (!validClue);
    		
    		out.println(Commands.CLUE + selectedPlayer + ":" + clue);
    }
    
    // returns true if the clue applies to any non-selected cards
    private boolean nonComprehensiveClue(String clue) {
    		for (ClientCard card : selectedPlayer.getHand()) {	
			if (!selectedCards.contains(card) && card.matches(clue))
				return true;
		}
    		return false;
    }
    
    private void showMessageDialog(String message) {
		JOptionPane.showMessageDialog(window, message, myName, JOptionPane.PLAIN_MESSAGE, null);
    }
    
    private void intro() {
    		if (players.names().size() == 1) {
    			showAutoCloseMessageDialog("Waiting for more players...");
    		} else chooseStartingPlayerThread();
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
    		clearSelected();
    		ClientPlayer player = players.get(playerName);
		ClientCard card = player.removeCard(Integer.parseInt(position));
		card.clean();
		return card;
    }
    
    private void addPlayerCard(String playerName, String cardStr) {
    		ClientPlayer chosenPlayer = players.get(playerName);
		ClientCard card = chosenPlayer.addCard(getCard(cardStr));
		card.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (validClick(chosenPlayer)) {
					if (!players.get(myName).equals(chosenPlayer) && !board.hasClues()) {
						showTimedCloseMessageDialog("No clues to give!");
						return;
					}

					if (selectedPlayer == null) {
						selectedPlayer = chosenPlayer;
						selectedPlayer.showButtons(board.cluesFull());
					}
					
					if (!players.get(myName).equals(selectedPlayer) || // selected another player's cards
							selectedCards.isEmpty() || // selected my own card, first one
							selectedCards.contains(card)) { // deselected my own card
						
						if (selectedCards.contains(card)) {
							card.setSelected(false);
							selectedCards.remove(card);
							if (selectedCards.isEmpty()) {
								selectedPlayer.hideButtons();
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
    		if (freeze) return false; //TODO: Do I actually need this?
    	
    		// Not my turn
    		if (!players.isTurn(myName))
    			return false;
    		
    		// Already selected a different player first
    		if (selectedPlayer != null && !selectedPlayer.equals(player))
    			return false;
    		
    		return true;
    }
    
    private void drawBoard() {
    		window = new JFrame(myName);
    		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    		window.setSize(650, 670);
    		
    		board = new ClientBoard(multicolor, clueCount, fuckupCount);
    		
    		JPanel innerView = InvisiblePanel.create(new BorderLayout());
    		
    		innerView.add(playersCards, BorderLayout.NORTH);
    		innerView.add(myCards, BorderLayout.SOUTH);
    		innerView.add(board.getPlayPanel(), BorderLayout.CENTER);
    		innerView.add(board.getDeckPanel(), BorderLayout.WEST);
    		
    		JPanel rightPanel = InvisiblePanel.create(new BorderLayout());
    		rightPanel.add(board.getDiscardPanel(), BorderLayout.CENTER);
    		rightPanel.add(log, BorderLayout.SOUTH);
    		
    		JPanel outerView = new JPanel(new BorderLayout());
    		outerView.setBackground(BOARD_COLOR);
    		outerView.add(innerView, BorderLayout.CENTER);
    		outerView.add(rightPanel, BorderLayout.EAST);
    		window.add(outerView);
    		
    		JPanel glass = (JPanel) window.getGlassPane();
    		glass.setLayout(new BorderLayout());
    		glass.add(fireworksPanel, BorderLayout.CENTER);
    		glass.setVisible(true);
    		
    		window.setVisible(true);
    		
    		populateCards();
    		board.saveCardLocationsRelativeTo(innerView);
    }

    private void closeDialog() {
		if (dialog != null) dialog.dispose();
    }
    
    // Must be started in new thread in order to close
    private void showAutoCloseMessageDialog(String message) {
    		new Thread(() -> {
			closeDialog();
			JOptionPane pane = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
			
			dialog = pane.createDialog(window, myName);
			pane.selectInitialValue();
			
			dialog.setVisible(true);
    		}).start();
    }
    
    private void showTimedCloseMessageDialog(String message) {
    		showAutoCloseMessageDialog(message);
    		new Thread(() -> {
    			Util.pauseMillis(800);
    			closeDialog();
    		}).start();
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
    		String server = "localhost";
    		if (args.length > 0) server = args[0];
        new Client(server).play();
    }
}

package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import javax.swing.SwingUtilities;

import clientboard.ClientBoard;
import clientboard.ClientCard;
import clientboard.GameLog;
import clientboard.HanabiFireworksPanel;
import color.CardColor;
import shared.Card;
import shared.Commands;
import shared.Util;

//TODO: End game if use up all fuckup tokens
//TODO: Win game with all 5s
//TODO: Handle running out of cards
//TODO: Undo button?
//TODO: If you resize the window, the fireworks animation is in the wrong spot :(
//TODO: Display discards
//TODO: If play while hover, go back to front of card

//TODO: JList instead of combo box when giving clues
//TODO: Let fireworks animations finish before card animations
//TODO: Displaying multi or maybe pictures for cards? Also pictures for tokens
//TODO: Set minimum size so can't resize too small
//TODO: Fix organization, this class is huge
//TODO: Ability to repeat game with same deck order
//TODO: Hover over other players cards to show what they know?
//TODO: Game log?
//TODO: Show other clues
//TODO: Fix layout issues
//TODO: End game on all 5s, end game when you run out of cards, let players have extra turns after cards end
//TODO: Add in variants...OMG WHAT?!

//TODO: Game log when a player says to keep playing?
//TODO: When the players legitimately run out of cards

public class Client {

    private static int PORT = 8901;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private AllPlayers players;
    private ClientPlayer selectedPlayer;
    private List<ClientCard> selectedCards;

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

    private GameLog log;

    private static final Color BOARD_COLOR = new Color(0, 153, 0);

    public Client(String serverAddress, String name) throws Exception {
        this(serverAddress);
        myName = name;
    }
    
    public Client(String serverAddress) throws Exception {
        socket = new Socket(serverAddress, PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        players = new AllPlayers();
//        selectedPlayer = null;
//        selectedCards = new ArrayList<ClientCard>();

//        playersCards = InvisiblePanel.create();
//        ((FlowLayout) playersCards.getLayout()).setHgap(20);
//
//        myCards = InvisiblePanel.create();

        fireworksPanel = new HanabiFireworksPanel();
        log = new GameLog();

        myName = null;
//        dialog = null;
//        freeze = false;
    }
    
    public void reset() {
        //TODO: Whose turn is it?
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        playersCards = InvisiblePanel.create(layout);

        myCards = InvisiblePanel.create();
        dialog = null;
        freeze = false;
        
        selectedPlayer = null;
        selectedCards = new ArrayList<ClientCard>();
        
        players.reset();
        resetBoard();
    }

    public void play() throws Exception {
        String response;
        try {
            while (true) {
                response = in.readLine();
                if (response == null)
                    continue;

                Util.handleResponse(Commands.ENTER_NAME, response, () -> {
                    if (myName == null)
                        myName = getName();
                    if (myName == null)
                        return;
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
                    players.startGame(startingPlayer);
                    log.append(action(startingPlayer, "start", "the game!"));
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
                    closeDialog();
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
//                    Point location = board.getLocation(color);
//                    fireworksPanel.fireworkComplete(location, color);
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
                
                Util.handleResponse(Commands.GAME_OVER_PLAY_AGAIN, response, input -> {
                    new Thread(() -> {
                        gameOverPlayAgain(input);
                    }).start();
                });
                
                Util.handleResponse(Commands.GAME_OVER_KEEP_PLAYING, response, input -> {
                    new Thread(() -> {
                        gameOverKeepPlaying("Game over: " + input + "\nDo you want to keep playing anyway?");
                    }).start();
                });
                
                Util.handleResponse(Commands.QUIT, response, () -> {
                    System.exit(0);
                });
                
                Util.handleResponse(Commands.RESET, response, () -> {
                    closeDialog();
                    reset();
                });
            }
        } finally {
            socket.close();
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

        for (JButton b : buttons) {
            buttonPanel.add(b);
        }
        
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
        String message = "Give clue to " + selectedPlayer + ":";
        boolean validClue = false;
        do {
            clue = (String) JOptionPane.showInputDialog(p, message, myName, JOptionPane.PLAIN_MESSAGE, null,
                    options.toArray(new String[options.size()]), defaultOption);

            if (clue == null)
                return; // user hit cancel

            if (clue.equals(defaultOption))
                continue;
            if (!options.contains(clue.toLowerCase()))
                continue;
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
    
    private boolean validClick(ClientPlayer player) {
        // Board is frozen (in between turns)
        if (freeze)
            return false; // TODO: Do I actually need this?

        // Not my turn
        if (!players.isTurn(myName))
            return false;

        // Already selected a different player first
        if (selectedPlayer != null && !selectedPlayer.equals(player))
            return false;

        return true;
    }

    private void addPlayerCard(String playerName, String cardStr) {
        ClientPlayer chosenPlayer = players.get(playerName);
        ClientCard card = chosenPlayer.addCard(Card.getCard(cardStr));
        card.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                //System.out.println("CLICK!!");
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
            
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!players.get(myName).equals(chosenPlayer)) {
                    card.setHover(true);
                    new Thread(() -> {
                        Util.pauseMillis(1100);
                        if (card.getHover()) {
                            card.display(false);
                            card.revalidate();
                            card.repaint();
                        }
                    }).start();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (card.getHover()) {
                    card.setHover(false);
                    card.display(true);
                    card.revalidate();
                    card.repaint();
                }
            }
            
        });
    }

    private ClientCard removePlayerCard(String playerName, String position) {
        clearSelected();
        ClientPlayer player = players.get(playerName);
        ClientCard card = player.removeCard(Integer.parseInt(position));
        card.clean();
        return card;
    }
    
    //************************************************************************************** GUI
    //*************************************************************************************************

    private void clearSelected() {
        board.clearSelected();
        if (selectedPlayer != null) {
            // when clue was given, their selected cards won't be in the
            // selectedCards list so we just deselect all the cards in their hand
            for (ClientCard c : selectedPlayer.getHand()) {
                c.setSelected(false);
            }
            selectedPlayer.hideButtons();
            
            selectedPlayer = null;
            selectedCards.clear();
        }
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

    private void drawBoard() {        
        window = new JFrame(myName);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(650, 670);

        reset();
    }
    
    private void resetBoard() {
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
        
        window.getContentPane().removeAll();
        window.getContentPane().add(outerView);

        //JPanel glass = (JPanel) window.getGlassPane();
        //glass.setLayout(new BorderLayout());
        //glass.add(fireworksPanel, BorderLayout.CENTER);
        //glass.setVisible(true);

        window.setVisible(true);

        populateCards();
//        board.saveCardLocationsRelativeTo(innerView);
    }
    
    //************************************************************************************** String methods
    //*************************************************************************************************
    
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
        } else
            str = name + " " + verbThird;
        return str + " " + rest;
    }
        
  //************************************************************************************** Dialogs
  //*************************************************************************************************

    private String getName() {
        return JOptionPane.showInputDialog(null, "Choose a screen name:", "Screen name selection",
                JOptionPane.PLAIN_MESSAGE);
    }
    
    private void announceClueGiven(String playerName, String clue) {
        String message = action(players.turn().getPlayerName(), "tell", recipient(playerName) + " about: " + clue);
        showAutoCloseMessageDialog(message);
        log.append(message);
    }
    
    private void showMessageDialog(String message) {
        JOptionPane.showMessageDialog(window, message, myName, JOptionPane.PLAIN_MESSAGE, null);
    }

    private void intro() {
        if (players.names().size() == 1) {
            showAutoCloseMessageDialog("Waiting for more players...");
        } else {
            chooseStartingPlayerThread();
        }
    }

    private void chooseStartingPlayerThread() {
        new Thread(() -> {
            String startingPlayer = chooseStartingPlayer();

            if (startingPlayer != null) {
                out.println(Commands.CHOOSE_STARTING_PLAYER + startingPlayer);
            }
        }).start();
    }
    
    private void closeDialog() {
        if (dialog != null) dialog.dispose();
    }

    // Must be started in new thread in order to close
    private void showAutoCloseMessageDialog(String message) {
        new Thread(() -> {
            JOptionPane pane = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
                    new Object[] {}, null);

            closeDialog();
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

        String message = "Who is the most colorful? ";
        for (String name : players.names()) {
            message += name + " ";
        }

        JOptionPane pane = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION);
        pane.setWantsInput(true);

        closeDialog();
        dialog = pane.createDialog(null, myName);

        pane.selectInitialValue();
        dialog.setVisible(true);
        closeDialog();

        Object value = pane.getInputValue();

        if (value == JOptionPane.UNINITIALIZED_VALUE) {
            return null;
        }

        return (String) value;
    }
    
    private void gameOverPlayAgain(String message) {
        String yesShuffle = "Yes - shuffle deck";
        String yesDontShuffle = "Yes - don't shuffle deck";
        String no = "No - fuck this game";

        message += "\nDo you want to play again?";
        JOptionPane pane = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
                new Object[] {no, yesDontShuffle, yesShuffle}, null);
        
        closeDialog();
        dialog = pane.createDialog(window, myName);

        pane.selectInitialValue();
        dialog.setVisible(true);
        closeDialog();

        Object value = pane.getValue();
        
        if (value == null) {
            System.out.println("VALUE WAS NULL WHAT DO I DO?!"); //TODO
        }
        else if (value.equals(yesShuffle)) {
            System.out.println("Play again! Shuffle");
            out.println(Commands.PLAY_AGAIN_SHUFFLE);
        } 
        else if (value.equals(yesDontShuffle)) {
            System.out.println("Play again! Don't shuffle");
            //out.println(Commands.KEEP_PLAYING);
            out.println(Commands.PLAY_AGAIN_DONT_SHUFFLE);
        }
        else if (value.equals(no)) {
            System.out.println("Don't play again!");
            out.println(Commands.QUIT);
        }
    }
    
    private void gameOverKeepPlaying(String message) {
        JOptionPane pane = new JOptionPane(message, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION);

        closeDialog();
        dialog = pane.createDialog(window, myName);

        pane.selectInitialValue();
        dialog.setVisible(true);
        closeDialog();

        Object value = pane.getValue();
        
        if (value == null) {
            //TODO
        } else if (value.equals(JOptionPane.YES_OPTION)) {
            System.out.println("Keep playing!!");
            out.println(Commands.KEEP_PLAYING);
        } else if (value.equals(JOptionPane.NO_OPTION)) {
            System.out.println("Don't keep playing. Play again?");
            out.println(Commands.GAME_OVER_PLAY_AGAIN);
        }
    }
    
    //************************************************************************************** Misc.
    //*************************************************************************************************
    
    // For Main testing only
    public void selectStartingPlayer(String name) {
        out.println(Commands.CHOOSE_STARTING_PLAYER + name);
    }
    
    public static void main(String[] args) throws Exception {
        String server = "localhost";
        if (args.length > 0) server = args[0];
        new Client(server).play();
    }
}

package server;

import java.util.Random;

import color.CardColor;
import shared.Card;

public class Deck {

    private static final int[] ALL_VALUES = new int[] { 1, 1, 1, 2, 2, 3, 3, 4, 4, 5 };

    private Card[] cards;
    private int next;

    public Deck(boolean multicolor) {
        next = 0;

        int size = ALL_VALUES.length * CardColor.getAllColors(multicolor).length;

        cards = new Card[size];
        populateCards(multicolor);
        shuffle();
    }

    private void populateCards(boolean multicolor) {
        for (CardColor color : CardColor.getAllColors(multicolor)) {
            populateColor(color);
        }
        next = 0;
    }

    private void populateColor(CardColor color) {
        for (int value : ALL_VALUES) {
            cards[next++] = new Card(color, value);
        }
    }

    private void shuffle() {
        Random rand = new Random();
        shuffleHelper(rand);
        shuffleHelper(rand);
    }

    private void shuffleHelper(Random rand) {
        for (int i = 0; i < cards.length; i++) {
            int randomIdx = rand.nextInt(cards.length);
            Card temp = cards[i];
            cards[i] = cards[randomIdx];
            cards[randomIdx] = temp;
        }
    }

    // Throws IndexOutOfBounds if deck has been exhausted
    public Card draw() {
        return cards[next++];
    }

    public int cardsLeft() {
        return cards.length - next;
    }

    public void reset() {
        next = 0;
        shuffle();
    }

}

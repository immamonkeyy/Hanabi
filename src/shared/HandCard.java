package shared;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import color.CardColor;

public class HandCard {

    private ColorMap<Boolean> possibleColors;
    private Map<Integer, Boolean> possibleValues;
    private boolean hasClues;

    private Card actualCard;

    public HandCard(Card card, boolean multicolor) {
        possibleColors = new ColorMap<Boolean>(multicolor, () -> null);

        possibleValues = new LinkedHashMap<Integer, Boolean>();
        for (int i = 1; i <= 5; i++) {
            possibleValues.put(i, null);
        }

        hasClues = false;

        actualCard = card;
    }

    public boolean matches(String clue) {
        return actualCard.matches(clue, (a, b) -> {
        }, (a, b) -> {
        });
    }

    public boolean addClue(String clue) {
        return actualCard.matches(clue, recordValueClueGiven(), recordColorClueGiven());
    }

    // Following two methods:
    // Will flip "hasClues" to true the first time this card
    // is the target of a given clue.

    public BiConsumer<CardColor, Boolean> recordColorClueGiven() {
        return (colorClue, target) -> {
            possibleColors.put(colorClue, target);
            hasClues = !hasClues && target;
            if (possibleColors.containsValue(Boolean.TRUE) && possibleColors.containsValue(Boolean.FALSE)) {
                for (CardColor c : possibleColors.keySet()) {
                    if (possibleColors.get(c) == null)
                        possibleColors.put(c, Boolean.FALSE);
                }
            }
        };
    }

    public BiConsumer<Integer, Boolean> recordValueClueGiven() {
        return (valueClue, target) -> {
            possibleValues.put(valueClue, target);
            hasClues = !hasClues && target;
            if (target) {
                for (Integer v : possibleValues.keySet()) {
                    if (possibleValues.get(v) == null)
                        possibleValues.put(v, Boolean.FALSE);
                }
            }
        };
    }

    public boolean hasClues() {
        return hasClues;
    }

    public Card getCard() {
        return actualCard;
    }

    public String toString() {
        return actualCard.toString();
    }

    public String toMessageString() {
        return actualCard.toMessageString();
    }

    public int value() {
        return actualCard.value();
    }

    public CardColor color() {
        return actualCard.color();
    }

    public ColorMap<Boolean> getPossibleColors() {
        return possibleColors;
    }

    public Map<Integer, Boolean> getPossibleValues() {
        return possibleValues;
    }
}

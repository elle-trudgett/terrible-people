package com.elletrudgett.cards.cah.game;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class Deck {
    private final List<Card> cards = new ArrayList<>();

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card draw() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("No cards to draw!");
        }

        Card topCard = cards.get(0);
        cards.remove(0);

        return topCard;
    }
}

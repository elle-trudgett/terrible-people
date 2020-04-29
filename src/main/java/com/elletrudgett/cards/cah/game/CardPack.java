package com.elletrudgett.cards.cah.game;

import lombok.Data;

import java.util.List;

@Data
public class CardPack {
    private final String name;
    private final List<Card> blackCards;
    private final List<Card> whiteCards;
}

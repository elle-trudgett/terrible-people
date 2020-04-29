package com.elletrudgett.cards.cah.game;

import lombok.Data;

import java.util.EnumSet;

@Data
public class Card {
    private final CardType cardType;
    private final String content;
    private final EnumSet<CardSpecialEffect> effects = EnumSet.noneOf(CardSpecialEffect.class);
}

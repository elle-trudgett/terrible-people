package com.elletrudgett.cards.cah.game;

import lombok.Data;

import java.util.EnumSet;
import java.util.UUID;

@Data
public class Card {
    private final CardType cardType;
    private String content;
    private final String pack;
    private final EnumSet<CardSpecialEffect> effects = EnumSet.noneOf(CardSpecialEffect.class);
    private boolean isImage = false;

    // Makes each card instance unique.
    private final UUID uuid;

    public Card(CardType cardType, String pack, String content) {
        this.cardType = cardType;
        this.pack = pack;
        this.content = content;
        this.uuid = UUID.randomUUID();
    }
}

package com.elletrudgett.cards.cah.game;

import com.elletrudgett.cards.cah.game.messages.AbstractGameMessage;

public class CardSkippedMessage extends AbstractGameMessage {
    @Override
    public MessageType getMessageType() {
        return MessageType.CardSkipped;
    }
}

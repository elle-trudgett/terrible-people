package com.elletrudgett.cards.cah.game;

import com.elletrudgett.cards.cah.game.messages.AbstractGameMessage;

public class PlayerKickedMessage extends AbstractGameMessage {
    @Override
    public MessageType getMessageType() {
        return MessageType.PlayerKicked;
    }
}

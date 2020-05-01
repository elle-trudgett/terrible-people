package com.elletrudgett.cards.cah.game;

import com.elletrudgett.cards.cah.game.messages.AbstractGameMessage;

public class LobbyUpdateMessage extends AbstractGameMessage {
    @Override
    public MessageType getMessageType() {
        return MessageType.LobbyUpdate;
    }
}

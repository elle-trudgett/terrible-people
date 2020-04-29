package com.elletrudgett.cards.cah.game.messages;

public class ResetGameMessage extends AbstractGameMessage {
    @Override
    public MessageType getMessageType() {
        return MessageType.ResetGame;
    }
}

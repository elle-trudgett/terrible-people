package com.elletrudgett.cards.cah.game.messages;

public abstract class AbstractGameMessage {
    public enum MessageType {
        GameStateUpdate,
        PlayerKicked,
        CardSkipped,
        ResetGame,
        WaitingOn
    }

    public abstract MessageType getMessageType();
}

package com.elletrudgett.cards.cah.game.messages;

public abstract class AbstractGameMessage {
    public enum MessageType {
        GameUpdate,
        PlayerKicked,
        CardSkipped,
        ResetGame,
        LobbyUpdate, WaitingOn
    }

    public abstract MessageType getMessageType();
}

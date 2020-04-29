package com.elletrudgett.cards.cah.game.messages;

import com.elletrudgett.cards.cah.game.GameState;
import lombok.Data;

@Data
public class GameStateUpdateMessage extends AbstractGameMessage {
    private final GameState gameState;

    @Override
    public MessageType getMessageType() {
        return MessageType.GameStateUpdate;
    }
}

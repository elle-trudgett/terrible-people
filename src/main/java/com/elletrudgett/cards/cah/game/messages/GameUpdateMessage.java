package com.elletrudgett.cards.cah.game.messages;

import com.elletrudgett.cards.cah.game.Room;
import lombok.Data;

@Data
public class GameUpdateMessage extends AbstractGameMessage {
    private final Room room;

    @Override
    public MessageType getMessageType() {
        return MessageType.GameUpdate;
    }
}

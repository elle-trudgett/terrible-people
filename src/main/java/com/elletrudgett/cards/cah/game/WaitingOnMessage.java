package com.elletrudgett.cards.cah.game;

import com.elletrudgett.cards.cah.game.messages.AbstractGameMessage;
import lombok.Data;

import java.util.List;

@Data
public class WaitingOnMessage extends AbstractGameMessage {
    private final List<String> names;

    @Override
    public MessageType getMessageType() {
        return MessageType.WaitingOn;
    }
}

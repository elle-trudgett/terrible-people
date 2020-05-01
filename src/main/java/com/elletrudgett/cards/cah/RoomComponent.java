package com.elletrudgett.cards.cah;

import com.elletrudgett.cards.cah.game.GameStatus;
import com.elletrudgett.cards.cah.game.Player;
import com.elletrudgett.cards.cah.game.Room;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H5;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class RoomComponent extends Div {
    private final Div playersListText;
    private final Div gameStatusText;
    private final H5 header;

    public RoomComponent(Room room) {
        addClassName("tp-room");

        header = new H5();
        add(header);

        playersListText = new Div();
        add(playersListText);
        gameStatusText = new Div();
        add(gameStatusText);

        update(room);
    }

    public void update(Room room) {
        header.setText(room.getName());

        List<Player> players = room.getPlayers();
        if (players.isEmpty()) {
            playersListText.setText("No players yet");
        } else {
            playersListText.setText(players.size() + " players: " + StringUtils.join(players.stream().map(Player::getName).collect(Collectors.toList()), ", "));
        }
        if (room.getStatus() == GameStatus.WAITING) {
            gameStatusText.setText("Waiting to start");
        } else if (room.getStatus() == GameStatus.PLAYING) {
            gameStatusText.setText("Game in progress (Round " + room.getRound() + ")");
        } else if (room.getStatus() == GameStatus.GAME_OVER) {
            if (room.getWinner() != null) {
                gameStatusText.setText("Game finished. Winner: " + room.getWinner().getName());
            } else {
                gameStatusText.setText("Game finished.");
            }
        }
    }
}

package com.elletrudgett.cards.cah;

import com.elletrudgett.cards.cah.game.GameState;
import com.elletrudgett.cards.cah.game.GameStatus;
import com.elletrudgett.cards.cah.game.Player;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H5;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class RoomComponent extends Div {
    private final Div playersListText;
    private final Div gameStatusText;
    private final H5 header;

    public RoomComponent() {
        addClassName("tp-room");

        header = new H5();
        add(header);

        playersListText = new Div();
        add(playersListText);
        gameStatusText = new Div();
        add(gameStatusText);

        update(GameState.getInstance());
    }

    public void update(GameState gameState) {
        header.setText(gameState.getName());

        List<Player> players = gameState.getPlayers();
        if (players.isEmpty()) {
            playersListText.setText("No players yet");
        } else {
            playersListText.setText(players.size() + " players: " + StringUtils.join(players.stream().map(Player::getName).collect(Collectors.toList()), ", "));
        }
        if (gameState.getStatus() == GameStatus.WAITING) {
            gameStatusText.setText("Waiting to start");
        } else if (gameState.getStatus() == GameStatus.PLAYING) {
            gameStatusText.setText("Game in progress (Round " + gameState.getRound() + ")");
        } else if (gameState.getStatus() == GameStatus.GAME_OVER) {
            if (gameState.getWinner() != null) {
                gameStatusText.setText("Game finished. Winner: " + gameState.getWinner().getName());
            } else {
                gameStatusText.setText("Game finished.");
            }
        }
    }
}

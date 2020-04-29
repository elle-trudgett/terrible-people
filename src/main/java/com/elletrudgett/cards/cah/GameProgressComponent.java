package com.elletrudgett.cards.cah;

import com.elletrudgett.cards.cah.game.GameState;
import com.elletrudgett.cards.cah.game.GameStatus;
import com.elletrudgett.cards.cah.game.Player;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H5;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class GameProgressComponent extends Div {
    private final Div playersListText;
    private final Div gameStatusText;

    public GameProgressComponent() {
        add(new H5("Current game"));

        playersListText = new Div();
        add(playersListText);
        gameStatusText = new Div();
        add(gameStatusText);

        update(GameState.getInstance());
    }

    public void update(GameState gameState) {
        List<Player> players = gameState.getPlayers();
        if (players.isEmpty()) {
            playersListText.setText("No players yet");
        } else {
            playersListText.setText(players.size() + " players: " + StringUtils.join(players.stream().map(Player::getName).collect(Collectors.toList()), ", "));
        }
        if (gameState.getStatus() == GameStatus.WAITING) {
            gameStatusText.setText("Waiting to start");
        } else if (gameState.getStatus() == GameStatus.PLAYING) {
            gameStatusText.setText("Game in progress");
        } else if (gameState.getStatus() == GameStatus.GAME_OVER) {
            gameStatusText.setText("Game finished");
        }
    }
}

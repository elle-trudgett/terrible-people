package com.elletrudgett.cards.cah;

import com.elletrudgett.cards.cah.game.GameState;
import com.elletrudgett.cards.cah.game.Player;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class PlayersComponent extends ScrollPanel {
    public static final String GRAVATAR_URL = "https://www.gravatar.com/avatar/";

    public PlayersComponent() {
        getStyle().set("overflow", "hidden");
        update(GameState.getInstance().getPlayers());
    }

    public void update(List<Player> players) {
        removeAll();
        List<Player> sortedPlayers = new ArrayList<>(players);
        sortedPlayers.sort(Comparator.comparing(Player::getScore));
        Collections.reverse(sortedPlayers);

        for (Player player : sortedPlayers) {
            add(new PlayerComponent(player));
        }
    }

    @Data
    private class PlayerComponent extends VerticalLayout {
        private final Player player;

        public PlayerComponent(Player player) {
            this.player = player;

            if (!player.isActive()) {
                addClassName("tp-player-inactive");
            }

            setPadding(false);

            Image image = new Image(GRAVATAR_URL + player.getEmailHash(), player.getName());
            image.getElement().getStyle().set("margin", "0");

            Div nameDiv = new Div();
            nameDiv.setText(player.getName());
            nameDiv.addClassName("tp-name-overlay");

            Div scoreDiv = new Div();
            scoreDiv.setText(String.valueOf(player.getScore()));
            scoreDiv.addClassName("tp-score-overlay");

            Div vipDiv = new Div();
            FontAwesomeIcon crown = new FontAwesomeIcon("fa-crown");
            vipDiv.addClassName("tp-vip-overlay");
            vipDiv.add(crown);

            Div container = new Div();
            container.addClassName("tp-player-container");
            container.add(image);
            container.add(nameDiv);
            container.add(scoreDiv);
            if (player.isVip()) {
                container.add(vipDiv);
            }

            ContextMenu contextMenu = new ContextMenu();
            contextMenu.setTarget(container);
            Player me = GameState.getCurrentPlayer();
            if (me != null) {
                MenuItem kickMenuItem = contextMenu.addItem("Kick", event -> {
                    GameState.getInstance().kickPlayer(me, player);
                });
                kickMenuItem.setEnabled(me != player);
                MenuItem giveVipMenuItem = contextMenu.addItem("Give room control", event -> {
                    GameState.getInstance().giveVip(me, player);
                });
                giveVipMenuItem.setEnabled(me.isVip() && me != player);
            }

            add(container);
        }
    }
}

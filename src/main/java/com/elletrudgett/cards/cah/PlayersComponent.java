package com.elletrudgett.cards.cah;

import com.elletrudgett.cards.cah.game.GameStatus;
import com.elletrudgett.cards.cah.game.Player;
import com.elletrudgett.cards.cah.game.Statics;
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

import static com.elletrudgett.cards.cah.game.Statics.getRoom;

@Data
public class PlayersComponent extends ScrollPanel {
    public static final String GRAVATAR_URL = "https://www.gravatar.com/avatar/";

    public PlayersComponent() {
        addClassName("tp-players-component");
        update(Collections.emptyList());
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

            Div czarDiv = new Div();
            FontAwesomeIcon bullhorn = new FontAwesomeIcon("fa-bullhorn");
            czarDiv.addClassName("tp-czar-overlay");
            czarDiv.add(bullhorn);

            Div container = new Div();
            container.addClassName("tp-player-container");
            container.add(image);
            container.add(nameDiv);
            container.add(scoreDiv);
            if (player.isVip()) {
                container.add(vipDiv);
            }
            if (getRoom().isCzar(player) && getRoom().getStatus() != GameStatus.WAITING) {
                container.add(czarDiv);
            }

            ContextMenu contextMenu = new ContextMenu();
            contextMenu.setTarget(container);
            Statics.getPlayerOptional().ifPresent(me -> {
                MenuItem kickMenuItem = contextMenu.addItem("Kick", event -> getRoom().kickPlayer(me, player));
                kickMenuItem.setEnabled(me != player);
                MenuItem giveVipMenuItem = contextMenu.addItem("Give room control", event -> {
                    getRoom().giveVip(me, player);
                });
                giveVipMenuItem.setEnabled(me.isVip() && me != player);
            });

            add(container);
        }
    }
}

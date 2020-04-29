package com.elletrudgett.cards.cah;

import com.elletrudgett.cards.cah.game.GameState;
import com.elletrudgett.cards.cah.game.Player;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;

import static com.elletrudgett.cards.cah.PlayersComponent.GRAVATAR_URL;

public class WinnerDisplayComponent extends Div {
    private final H2 h2;
    private final Image winnerImage;

    public WinnerDisplayComponent() {
        addClassName("tp-winner-display-component");

        winnerImage = new Image();
        winnerImage.getElement().getStyle().set("margin", "0");
        add(winnerImage);

        h2 = new H2("");
        add(h2);
    }

    public void update(GameState gs) {
        Player winner = gs.getWinner();
        setVisible(winner != null);
        if (winner != null) {
            winnerImage.setSrc(GRAVATAR_URL + winner.getEmailHash() + "?s=240");
            winnerImage.setAlt(winner.getName());
            winnerImage.addClassName("tp-winner-image");
            h2.setText(winner.getName() + " wins the game!");
        }
    }
}

package com.elletrudgett.cards.cah;

import com.elletrudgett.cards.cah.game.Player;
import com.vaadin.flow.server.VaadinSession;

import java.io.Serializable;

public class GlobalHelper implements Serializable {
    public static void savePlayer(Player player) {
        VaadinSession.getCurrent().getSession().setAttribute("player", player);
    }
}
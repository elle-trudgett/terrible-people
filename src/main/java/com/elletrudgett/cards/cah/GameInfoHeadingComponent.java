package com.elletrudgett.cards.cah;

import com.elletrudgett.cards.cah.game.Room;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Span;

public class GameInfoHeadingComponent extends H5 {
    public GameInfoHeadingComponent() {
    }

    public void update(Room gs) {
        removeAll();

        add(new Span(gs.getName()));
        add(new Spacer());

        FontAwesomeIcon usersIcon = new FontAwesomeIcon("fa-users");
        usersIcon.getElement().setAttribute("title", "Players");
        add(usersIcon);
        add(new Span(" " + gs.getPlayers().size()));
        add(new Spacer());

        FontAwesomeIcon trophyIcon = new FontAwesomeIcon("fa-trophy");
        trophyIcon.getElement().setAttribute("title", "Points needed to win");
        add(trophyIcon);
        add(new Span(" " + gs.getScoreLimit()));
    }

    private class Spacer extends Span {
        public Spacer() {
            addClassName("tp-game-info-spacer");
        }
    }
}
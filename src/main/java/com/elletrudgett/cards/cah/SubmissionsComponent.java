package com.elletrudgett.cards.cah;

import com.elletrudgett.cards.cah.game.Card;
import com.elletrudgett.cards.cah.game.GameState;
import com.elletrudgett.cards.cah.game.Player;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@Data
public class SubmissionsComponent extends VerticalLayout {
    private List<Pair<Player, List<Card>>> submissions = null;
    private H3 winnerHeader;

    public SubmissionsComponent() {
    }

    public void update(Player roundWinner, boolean iAmCzar) {
        if (submissions == null) {
            return;
        }

        removeAll();

        String czarName = GameState.getInstance().getCzarName();
        String headerText = iAmCzar ? "Choose the winner:" : czarName + " is choosing...";
        if (roundWinner != null) {
            headerText = roundWinner.getName() + " wins the round!";
        }
        winnerHeader = new H3(headerText);
        winnerHeader.getStyle().set("margin", "0");
        add(winnerHeader);

        Div submissionsDiv = new Div();
        submissionsDiv.setWidthFull();
        for (Pair<Player, List<Card>> submission : submissions) {
            Player player = submission.getKey();
            List<Card> submissionList = submission.getValue();
            PlayerSubmission playerSubmission = new PlayerSubmission(submissionList);

            // Give czar clicking ability if winner hasn't been chosen yet
            if (roundWinner == null && iAmCzar) {
                playerSubmission.setSubmissionCardClickedConsumer(e -> {
                    GameState.getInstance().selectAnswer(player);
                });
                playerSubmission.update(submissionList, submissionList.size());
            }

            if (roundWinner == player) {
                // Rainbow winner
                playerSubmission.addClassName("tp-winner");
            } else if (roundWinner != null) {
                // Hide losers
                playerSubmission.setVisible(false);
            }
            submissionsDiv.add(playerSubmission);
        }
        add(submissionsDiv);
    }

}

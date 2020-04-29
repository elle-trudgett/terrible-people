package com.elletrudgett.cards.cah;

import com.elletrudgett.cards.cah.game.Card;
import com.elletrudgett.cards.cah.game.CardSpecialEffect;
import com.elletrudgett.cards.cah.game.GameState;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class RoundDisplayComponent extends HorizontalLayout {

    private final CzarDisplayAreaComponent czarDisplayAreaComponent;

    public RoundDisplayComponent(PlayerSubmission mySubmissionComponent) {
        czarDisplayAreaComponent = new CzarDisplayAreaComponent();
        add(czarDisplayAreaComponent);
        add(mySubmissionComponent);
    }

    public void update(GameState gs) {
        czarDisplayAreaComponent.update(gs);
    }

    private class CzarDisplayAreaComponent extends VerticalLayout {

        private final Text cardCzarText;
        private final CardDisplayComponent cardDisplayComponent;

        public CzarDisplayAreaComponent() {
            cardCzarText = new Text("");
            add(new Div(cardCzarText));
            cardDisplayComponent = new CardDisplayComponent();
            add(cardDisplayComponent);

            cardDisplayComponent.update(null);
        }

        private void update(GameState gs) {
            if (gs.getPlayers().size() > 0) {
                cardCzarText.setText("Card Czar: " + gs.getCzarName());
            } else {
                cardCzarText.setText("No players");
            }
            cardDisplayComponent.update(gs.getCurrentBlackCard());
        }
    }

    private class CardDisplayComponent extends Div {
        private final Text cardContentText;

        public CardDisplayComponent() {
            setClassName("tp-black-card");
            cardContentText = new Text("");
            add(cardContentText);
            setVisible(false);
        }

        public void update(Card card) {
            setVisible(card != null);
            if (card != null) {
                String cardContent = card.getContent();
                if (card.getEffects().contains(CardSpecialEffect.PICK_2)) {
                    cardContent += " (Pick 2)";
                } else if (card.getEffects().contains(CardSpecialEffect.PICK_3)) {
                    cardContent += " (Pick 3)";
                }
                cardContentText.setText(cardContent);
            }
        }
    }
}

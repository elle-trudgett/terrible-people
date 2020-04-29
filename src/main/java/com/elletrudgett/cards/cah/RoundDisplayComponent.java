package com.elletrudgett.cards.cah;

import com.elletrudgett.cards.cah.game.*;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.Timer;
import java.util.TimerTask;

public class RoundDisplayComponent extends HorizontalLayout {

    private final CzarDisplayAreaComponent czarDisplayAreaComponent;
    private final WinnerDisplayComponent winnerDisplayComponent;
    private Button skipButton;

    public RoundDisplayComponent(PlayerSubmission mySubmissionComponent, Runnable skipRunnable) {
        addClassName("tp-round-display-component");
        czarDisplayAreaComponent = new CzarDisplayAreaComponent();
        add(czarDisplayAreaComponent);
        mySubmissionComponent.addClassName("tp-my-submission-component");
        add(mySubmissionComponent);
        skipButton = new Button("Skip this card");
        skipButton.getStyle().set("margin-left", "-4em");
        add(skipButton);
        skipButton.addClickListener(event -> {
            skipRunnable.run();
        });

        winnerDisplayComponent = new WinnerDisplayComponent();
        winnerDisplayComponent.setVisible(false);
        add(winnerDisplayComponent);
    }

    public void update(GameState gs, boolean iAmCzar) {
        if (gs.getStatus() == GameStatus.PLAYING) {
            czarDisplayAreaComponent.setVisible(true);
            czarDisplayAreaComponent.update(gs);

            if (iAmCzar && gs.getSubmissions().isEmpty() && !gs.isSkippedOnce()) {
                skipButton.setVisible(true);
            } else {
                skipButton.setVisible(false);
            }
        } else {
            czarDisplayAreaComponent.setVisible(false);
        }

        winnerDisplayComponent.update(gs);
    }

    private class CzarDisplayAreaComponent extends VerticalLayout {
        private final BlackCardDisplayComponent blackCardDisplayComponent;
        private final Div cardFlipCardDiv;
        private Card currentBlackCard;
        private final Div cardFlipContainerDiv;

        public CzarDisplayAreaComponent() {
            addClassName("tp-black-card-display-area-component");
            blackCardDisplayComponent = new BlackCardDisplayComponent();

            Div backOfCard = new Div();
            backOfCard.addClassNames("tp-black-card", "tp-cah-card", "tp-black-card-back");
            backOfCard.add(new H2("Terrible People"));

            cardFlipContainerDiv = new Div();
            cardFlipContainerDiv.addClassName("tp-black-card-container");

            cardFlipCardDiv = new Div();
            cardFlipCardDiv.addClassName("tp-black-card-card");
            cardFlipCardDiv.getStyle().set("transform", "translateX(-150%) rotateY(180deg)");

            cardFlipContainerDiv.add(cardFlipCardDiv);

            cardFlipCardDiv.add(blackCardDisplayComponent); // front of card
            cardFlipCardDiv.add(backOfCard); // back of card

            add(cardFlipContainerDiv);

            blackCardDisplayComponent.update(null);
        }

        private void update(GameState gs) {
            Card gsBlackCard = gs.getCurrentBlackCard();
            blackCardDisplayComponent.update(gsBlackCard);
            if (gs.getPlayPhase() == PlayPhase.DELIBERATING) {
                cardFlipCardDiv.getStyle().set("transform", "translateX(50vw) translateX(-6.5em)");
            }

            if (gsBlackCard != currentBlackCard) {
                cardFlipCardDiv.getStyle().set("transform", "translateX(-150%) rotateY(180deg)");
                cardFlipContainerDiv.remove(cardFlipCardDiv);
                cardFlipContainerDiv.add(cardFlipCardDiv);
                currentBlackCard = gsBlackCard;

                // Slide in
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        getUI().ifPresent(ui -> ui.access(() -> {
                            cardFlipCardDiv.getStyle().set("transform", "rotateY(180deg)");
                        }));
                    }
                }, 100);

                // Flip over
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        getUI().ifPresent(ui -> ui.access(() -> {
                            cardFlipCardDiv.getStyle().set("transform", "none");
                        }));
                    }
                }, 1250);
            }
        }
    }

    private class BlackCardDisplayComponent extends Div {
        private final Text cardContentText;
        private final Div specialEffectOverlayDiv;

        public BlackCardDisplayComponent() {
            addClassNames("tp-black-card", "tp-cah-card");
            cardContentText = new Text("");
            add(cardContentText);
            setVisible(false);
            specialEffectOverlayDiv = new Div();
            specialEffectOverlayDiv.addClassName("tp-card-overlay-special-effects");
            specialEffectOverlayDiv.setText("");
            add(specialEffectOverlayDiv);
        }

        public void update(Card card) {
            setVisible(card != null);
            if (card != null) {
                String cardContent = card.getContent();
                specialEffectOverlayDiv.removeAll();
                if (card.getEffects().contains(CardSpecialEffect.PICK_2)) {
                    specialEffectOverlayDiv.add("PICK");
                    Span numberSpan = new Span("2");
                    numberSpan.addClassName("tp-pick-number");
                    specialEffectOverlayDiv.add(numberSpan);
                } else if (card.getEffects().contains(CardSpecialEffect.PICK_3)) {
                    specialEffectOverlayDiv.add("PICK");
                    Span numberSpan = new Span("3");
                    numberSpan.addClassName("tp-pick-number");
                    specialEffectOverlayDiv.add(numberSpan);
                }
                cardContentText.setText(cardContent);
            }
        }
    }
}

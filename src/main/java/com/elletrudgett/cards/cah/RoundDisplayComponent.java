package com.elletrudgett.cards.cah;

import com.elletrudgett.cards.cah.game.*;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.Timer;
import java.util.TimerTask;

public class RoundDisplayComponent extends HorizontalLayout {

    private final CzarDisplayAreaComponent czarDisplayAreaComponent;
    private final WinnerDisplayComponent winnerDisplayComponent;
    private final H5 czarNameComponent;
    private Button skipButton;

    public RoundDisplayComponent(PlayerSubmission mySubmissionComponent, Runnable skipRunnable) {
        addClassName("tp-round-display-component");

        czarNameComponent = new H5("");
        czarNameComponent.addClassName("tp-czar-name-component");
        add(czarNameComponent);

        czarDisplayAreaComponent = new CzarDisplayAreaComponent();
        add(czarDisplayAreaComponent);
        mySubmissionComponent.addClassName("tp-my-submission-component");
        add(mySubmissionComponent);
        skipButton = new Button("Skip this card");
        skipButton.getStyle().set("margin-left", "-4em");
        skipButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add(skipButton);
        skipButton.addClickListener(event -> {
            skipRunnable.run();
        });

        winnerDisplayComponent = new WinnerDisplayComponent();
        winnerDisplayComponent.setVisible(false);
        add(winnerDisplayComponent);
    }

    public void update(Room gs, boolean iAmCzar) {
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

        if (!iAmCzar) {
            czarNameComponent.setText(gs.getCzarName() + "'s Black Card");
        } else {
            czarNameComponent.setText("Your black card!");
        }
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

        private void update(Room gs) {
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
        private final Div packDiv;

        public BlackCardDisplayComponent() {
            addClassNames("tp-black-card", "tp-cah-card");
            cardContentText = new Text("");
            add(cardContentText);
            setVisible(false);

            specialEffectOverlayDiv = new Div();
            specialEffectOverlayDiv.addClassName("tp-card-overlay-special-effects");
            add(specialEffectOverlayDiv);

            packDiv = new Div();
            packDiv.addClassName("tp-card-overlay-pack");
            add(packDiv);
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

                packDiv.removeAll();
                Image cardsImage = new Image("/frontend/img/cards-inv.png", "Terrible People");
                cardsImage.setClassName("tp-card-cards-icon");
                packDiv.add(cardsImage);
                packDiv.add(new Text(card.getPack()));

                cardContentText.setText(cardContent);
            }
        }
    }
}

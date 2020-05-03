package com.elletrudgett.cards.cah;

import com.elletrudgett.cards.cah.game.Card;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.Setter;

import java.util.List;
import java.util.function.Consumer;

class PlayerSubmission extends HorizontalLayout {
    @Setter
    private Consumer<Card> submissionCardClickedConsumer;
    @Setter
    private boolean mySubmission;

    public PlayerSubmission(List<Card> submission) {
        update(submission, submission.size());
    }

    public void update(List<Card> submission, int spaces) {
        removeAll();

        addClassName("tp-player-submission-" + spaces);

        setVisible(!submission.isEmpty());

        int i = 0;
        for (Card card : submission) {
            Span cardSpan = new Span(card.isImage() ? "" : card.getContent());
            if (card.isImage()) {
                Image image = new Image("/frontend/" + card.getImage(), card.getContent());
                image.addClassName("tp-white-card-image");
                cardSpan.add(image);
            }
            if (mySubmission) {
                Div tapToRemove = new Div();
                tapToRemove.addClassName("tp-tap-to-remove");
                tapToRemove.add(new FontAwesomeIcon("fa-trash-alt"));
                cardSpan.add(tapToRemove);
            }
            cardSpan.addClassName("tp-submission");
            cardSpan.addClassName("tp-cah-card");
            cardSpan.addClassName("tp-white-card");

            Div packDiv = new Div();
            packDiv.addClassName("tp-card-overlay-pack");
            Image cardsImage = new Image("/frontend/img/cards.png", "Terrible People");
            cardsImage.setClassName("tp-card-cards-icon");
            packDiv.add(cardsImage);
            packDiv.add(new Text(card.getPack()));
            cardSpan.add(packDiv);

            if (!mySubmission) {
                if (i > 0) {
                    cardSpan.getStyle().set("margin-left", "-2em");
                } else {
                    cardSpan.getStyle().set("margin-left", "-0.25em");
                }
            }

            if (spaces == 2) {
                if (i == 0) {
                    cardSpan.getStyle().set("transform", "rotate(-3deg)");
                    if (mySubmission) {
                        cardSpan.getStyle().set("margin-left", "-0.5em");
                        cardSpan.getStyle().set("z-index", "20");
                    }
                } else {
                    cardSpan.getStyle().set("transform", "rotate(3deg)");
                    if (mySubmission) {
                        cardSpan.getStyle().set("margin-left", "-9.5em");
                        cardSpan.getStyle().set("z-index", "10");
                    }
                }
            } else if (spaces == 3) {
                if (i == 0) {
                    cardSpan.getStyle().set("transform", "rotate(-5deg)");
                    if (mySubmission) {
                        cardSpan.getStyle().set("margin-left", "-1em");
                        cardSpan.getStyle().set("z-index", "30");
                    }
                } else if (i == 1) {
                    cardSpan.getStyle().set("margin-top", "-1em");
                    if (mySubmission) {
                        cardSpan.getStyle().set("margin-left", "-9.5em");
                        cardSpan.getStyle().set("z-index", "20");
                    }
                } else if (i == 2) {
                    cardSpan.getStyle().set("transform", "rotate(5deg)");
                    if (mySubmission) {
                        cardSpan.getStyle().set("margin-left", "-9.5em");
                        cardSpan.getStyle().set("z-index", "10");
                    }
                }
            }

            // this really should be in css but bite me
            if (!mySubmission) {
                cardSpan.getStyle().set("margin-bottom", "1.5em");
            }
            if (submissionCardClickedConsumer != null) {
                cardSpan.addClickListener(spanClickEvent -> submissionCardClickedConsumer.accept(card));
            }
            add(cardSpan);
            i++;
        }

        while (i < spaces) {
            Span cardSpan = new Span();
            cardSpan.addClassNames("tp-submission-blank", "blank");
            cardSpan.getStyle().set("margin-bottom", "1.5em");
            add(cardSpan);
            i++;
        }
    }
}

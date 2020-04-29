package com.elletrudgett.cards.cah;

import com.elletrudgett.cards.cah.game.Card;
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
                Image image = new Image("/frontend/" + card.getContent(), card.getContent());
                image.addClassName("tp-white-card-image");
                cardSpan.add(image);
            }
            if (mySubmission) {
                Div tapToRemove = new Div();
                tapToRemove.setText("(tap to remove)");
                tapToRemove.addClassName("tp-tap-to-remove");
                cardSpan.add(tapToRemove);
            }
            cardSpan.addClassName("tp-submission");
            cardSpan.addClassName("tp-cah-card");
            cardSpan.addClassName("tp-white-card");

            if (i > 0 && !mySubmission) {
                cardSpan.getStyle().set("margin-left", "-2em");
            }

            if (spaces == 2) {
                if (i == 0) {
                    cardSpan.getStyle().set("transform", "rotate(-3deg)");
                    if (mySubmission) {
                        cardSpan.getStyle().set("margin-left", "-0.5em");
                    }
                } else {
                    cardSpan.getStyle().set("transform", "rotate(3deg)");
                    if (mySubmission) {
                        cardSpan.getStyle().set("margin-left", "-9.5em");
                    }
                }
            } else if (spaces == 3) {
                if (i == 0) {
                    cardSpan.getStyle().set("transform", "rotate(-5deg)");
                    if (mySubmission) {
                        cardSpan.getStyle().set("margin-left", "-1em");
                    }
                } else if (i == 1) {
                    if (mySubmission) {
                        cardSpan.getStyle().set("margin-left", "-9.5em");
                    }
                } else if (i == 2) {
                    cardSpan.getStyle().set("transform", "rotate(5deg)");
                    if (mySubmission) {
                        cardSpan.getStyle().set("margin-left", "-9.5em");
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

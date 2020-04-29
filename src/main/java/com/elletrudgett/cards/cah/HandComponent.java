package com.elletrudgett.cards.cah;

import com.elletrudgett.cards.cah.game.Card;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class HandComponent extends Div {
    private final Runnable selectedChanged;
    private final PlayerSubmission mySubmission;

    @Getter
    private List<Card> selected = new ArrayList<>();
    @Setter
    private List<Card> hand = new ArrayList<>();
    @Setter
    private int cardLimit = 0;

    private final Div handDiv;
    private boolean enabled = true;

    public HandComponent(Runnable selectedChanged, PlayerSubmission mySubmission) {
        this.selectedChanged = selectedChanged;
        this.mySubmission = mySubmission;
        this.mySubmission.setMySubmission(true);
        this.mySubmission.setSubmissionCardClickedConsumer(this::submissionCardClicked);

        setWidthFull();
        handDiv = new Div();
        handDiv.setClassName("tp-hand");
        handDiv.setWidthFull();

        add(handDiv);
    }

    private void submissionCardClicked(Card cardCliened) {
        selected.removeIf(card -> card.equals(cardCliened));
        selectedChanged.run();
        update();
    }

    private void removeAllSelection() {
        selected.clear();
        selectedChanged.run();
        update();
    }

    public void update() {
        mySubmission.update(selected, cardLimit);

        handDiv.removeAll();
        for (Card card : hand) {
            Span handCard = new Span(card.isImage() ? "" : card.getContent());
            if (card.isImage()) {
                Image image = new Image("/frontend/" + card.getContent(), card.getContent());
                image.addClassName("tp-white-card-image");
                handCard.add(image);
            }
            handCard.addClassName("tp-hand-card");
            handCard.addClassName("tp-cah-card");
            handCard.addClassName("tp-white-card");
            if (selected.contains(card)) {
                handCard.addClassName("tp-hand-card-selected");
            } else {
                handCard.addClickListener(cardClickEvent -> {
                    if (selected.size() < cardLimit && enabled) {
                        addToSelection(card);
                        selectedChanged.run();
                    }
                });
            }

            handDiv.add(handCard);
        }

        if (selected.size() >= cardLimit) {
            handDiv.setVisible(false);
        } else {
            handDiv.setVisible(true);
        }
    }

    private void addToSelection(Card card) {
        selected.add(card);
        update();
    }

    public void enable() {
        handDiv.removeClassName("disabled");
        enabled = true;
    }

    public void disable() {
        handDiv.addClassName("disabled");
        enabled = false;
    }

    public void clearSelected() {
        selected.clear();
    }
}

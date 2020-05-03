package com.elletrudgett.cards.cah;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Span;

import java.util.List;

public class ConfirmAnswerDialog extends Dialog {
    public ConfirmAnswerDialog(List<String> cardContents, Runnable confirmationRunnable) {
        getElement().getStyle().set("min-width", "20em");
        add("Select this answer?");
        for (String cardContent : cardContents) {
            add(new ListItem(cardContent));
        }

        Span buttonSpan = new Span();
        buttonSpan.addClassName("tp-confirm-answer-dialog-button-area");

        Div cancelButtonDiv = new Div();
        cancelButtonDiv.getStyle().set("flex-grow", "1");
        Button no = new Button("Cancel", event -> close());
        no.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelButtonDiv.add(no);
        buttonSpan.add(cancelButtonDiv);

        Div selectButtonDiv = new Div();
        Button yes = new Button("Select", event -> {
            confirmationRunnable.run();
            close();
        });
        yes.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        selectButtonDiv.add(yes);
        buttonSpan.add(selectButtonDiv);

        add(buttonSpan);
    }
}

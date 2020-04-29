package com.elletrudgett.cards.cah;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.ListItem;

import java.util.Collections;
import java.util.List;

public class WaitingOnComponent extends Div {
    public WaitingOnComponent() {
        updateNames(Collections.emptyList());
    }

    public void updateNames(List<String> names) {
        removeAll();
        add(new H4("Still waiting for..."));
        for (String name : names) {
            add(new ListItem(name));
        }
    }
}

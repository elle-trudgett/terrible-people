package com.elletrudgett.cards.cah;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;

@Tag("i")
public class FontAwesomeIcon extends Component {
    public FontAwesomeIcon(String iconClass) {
        getElement().getClassList().set("fas", true);
        getElement().getClassList().set(iconClass, true);
    }
}

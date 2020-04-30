package com.elletrudgett.cards.cah;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PageConfigurator;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@Theme(value = Lumo.class, variant = Lumo.DARK)
@StyleSheet("https://fonts.googleapis.com/css2?family=Fredoka+One&family=Rubik:wght@300&family=Roboto")
@StyleSheet("./my-styles/game.css")
@JavaScript("https://kit.fontawesome.com/829bed66ec.js")
@Push
public class MainLayout extends Div implements RouterLayout, PageConfigurator {
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        UI.getCurrent().getElement().getThemeList().add(Lumo.DARK);
        UI.getCurrent().getPage().executeJs("document.getElementsByTagName(\"html\")[0].setAttribute(\"theme\", \"dark\")");
    }

    @Override
    public void configurePage(InitialPageSettings s) {
        s.addMetaTag("og:title", "Terrible People");
        s.addMetaTag("og:type", "website");
        s.addMetaTag("og:url", "http://terriblepeople.cards/");
        s.addLink("shortcut icon", "frontend/icons/favicon.ico");
        s.addFavIcon("icon", "frontend/icons/favicon.ico", "16x16");

    }
}

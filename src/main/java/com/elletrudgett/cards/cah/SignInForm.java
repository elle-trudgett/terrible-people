package com.elletrudgett.cards.cah;

import com.elletrudgett.cards.cah.game.GameState;
import com.elletrudgett.cards.cah.game.Player;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.VaadinService;

import javax.servlet.http.Cookie;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class SignInForm extends FormLayout {
    public static final String TERRIBLE_PEOPLE_NAME = "terrible-people-name";
    public static final String TERRIBLE_PEOPLE_EMAIL = "terrible-people-email";

    public SignInForm(Runnable onJoinRunnable) {
        add(new H1("Who are you?"));

        setResponsiveSteps(new ResponsiveStep("0em", 1));
        TextField nameField = new TextField("Name");
        nameField.setRequired(true);
        nameField.setRequiredIndicatorVisible(true);
        nameField.setErrorMessage("Please enter a name 2-12 characters.");
        nameField.setMaxLength(12);
        nameField.setMinLength(2);
        nameField.setWidthFull();
        add(nameField);

        EmailField emailField = new EmailField("Email");
        emailField.setClearButtonVisible(true);
        emailField.setErrorMessage("Please enter a valid email address");
        emailField.setWidthFull();
        add(emailField);

        Button join = new Button("Join");
        join.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        join.setDisableOnClick(true);
        join.addClickListener(event -> {
            // Save cookie
            String playerName = nameField.getValue().trim();
            try {
                Cookie nameCookie = new Cookie(TERRIBLE_PEOPLE_NAME, URLEncoder.encode(playerName, "UTF-8"));
                nameCookie.setMaxAge(31556952);
                nameCookie.setPath(VaadinService.getCurrentRequest().getContextPath());
                VaadinService.getCurrentResponse().addCookie(nameCookie);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String email = emailField.getValue().trim();
            try {
                Cookie emailCookie = new Cookie(TERRIBLE_PEOPLE_EMAIL, URLEncoder.encode(email, "UTF-8"));
                emailCookie.setMaxAge(31556952);
                emailCookie.setPath(VaadinService.getCurrentRequest().getContextPath());
                VaadinService.getCurrentResponse().addCookie(emailCookie);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            getUI().ifPresent(ui -> {
                onJoinRunnable.run();

                Player player = new Player(
                        playerName,
                        MD5Helper.md5Hex(email),
                        UI.getCurrent().getSession().getBrowser()
                );
                GlobalHelper.savePlayer(player);
                GameState.getInstance().addPlayer(player);

                ui.navigate("game");
            });
        });
        join.addClickShortcut(Key.ENTER);
        add(join);

        // Load cookie values
        Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(TERRIBLE_PEOPLE_NAME)) {
                    try {
                        nameField.setValue(URLDecoder.decode(cookie.getValue(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else if (cookie.getName().equals(TERRIBLE_PEOPLE_EMAIL)) {
                    try {
                        emailField.setValue(URLDecoder.decode(cookie.getValue(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

package com.elletrudgett.cards.cah;

import com.elletrudgett.cards.cah.game.Statics;
import com.elletrudgett.cards.cah.game.User;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import lombok.extern.log4j.Log4j2;

import java.io.UnsupportedEncodingException;

@Log4j2
public class SignInForm extends FormLayout {
    public static final int MAX_EMAIL_LENGTH = 50;
    public static final int MAX_NAME_LENGTH = 12;

    public SignInForm(Runnable onLoginRunnable) {
        add(new H1("Who are you?"));

        setResponsiveSteps(new ResponsiveStep("0em", 1));
        TextField nameField = new TextField("Name");
        nameField.setRequired(true);
        nameField.setRequiredIndicatorVisible(true);
        nameField.setErrorMessage("Please enter a name 2-" + MAX_NAME_LENGTH + " characters.");
        nameField.setMaxLength(MAX_NAME_LENGTH);
        nameField.setMinLength(2);
        nameField.setWidthFull();
        add(nameField);

        EmailField emailField = new EmailField("Email");
        emailField.setClearButtonVisible(true);
        emailField.setErrorMessage("Please enter a valid email address");
        emailField.setWidthFull();
        emailField.setMaxLength(MAX_EMAIL_LENGTH);
        add(emailField);

        Button submitButton = new Button("Submit");
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitButton.setDisableOnClick(true);
        submitButton.addClickListener(event -> {
            try {
                String name = nameField.getValue().trim();
                String email = emailField.getValue().trim();
                if (name.length() > MAX_NAME_LENGTH || email.length() > MAX_EMAIL_LENGTH) {
                    throw new IllegalArgumentException("Input too large");
                }
                Statics.saveUser(new User(name, email));
            } catch (UnsupportedEncodingException e) {
                log.error("Unable to save user.");
            }

            getUI().ifPresent(ui -> onLoginRunnable.run());
        });
        submitButton.addClickShortcut(Key.ENTER);
        add(submitButton);

        // Load cookie values
        Statics.loadUser().ifPresent(user -> {
            nameField.setValue(user.getName());
            emailField.setValue(user.getEmail());
        });
    }
}

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

import java.io.UnsupportedEncodingException;

public class SignInForm extends FormLayout {
    public SignInForm(Runnable onLoginRunnable) {
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

        Button submitButton = new Button("Submit");
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitButton.setDisableOnClick(true);
        submitButton.addClickListener(event -> {
            try {
                Statics.saveUser(new User(nameField.getValue().trim(), emailField.getValue().trim()));
            } catch (UnsupportedEncodingException e) {
                // Couldn't save cookies, whatever.
                e.printStackTrace();
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

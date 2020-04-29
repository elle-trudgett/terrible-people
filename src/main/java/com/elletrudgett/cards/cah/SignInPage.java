package com.elletrudgett.cards.cah;

import com.elletrudgett.cards.cah.game.GameState;
import com.elletrudgett.cards.cah.game.GameStatus;
import com.elletrudgett.cards.cah.game.Player;
import com.elletrudgett.cards.cah.game.messages.AbstractGameMessage;
import com.elletrudgett.cards.cah.game.messages.GameStateUpdateMessage;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Terrible People")
@Log4j2
public class SignInPage extends VerticalLayout {
    private final GameProgressComponent gameProgressComponent;
    private final SignInForm signInForm;
    private Registration broadcasterRegistration;

    public SignInPage() {
        add(new H2("Terrible People"));
        add(new H4("A CAH knockoff by Elle❤️"));
        gameProgressComponent = new GameProgressComponent();
        add(gameProgressComponent);
        signInForm = new SignInForm();
        signInForm.setVisible(GameState.getInstance().getStatus() != GameStatus.PLAYING);
        add(signInForm);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        UI ui = attachEvent.getUI();
        broadcasterRegistration = Broadcaster.register(newMessage -> {
            handleMessage(ui, newMessage);
        });

        Player player = (Player) VaadinSession.getCurrent().getSession().getAttribute("player");
        if (player != null) {
            // We have an existing player cookie, check if they are in the current game
            Optional<Player> validatedPlayer = GameState.getInstance().validatePlayer(player);
            if (validatedPlayer.isPresent()) {
                // We are in the game, go ahead to the game page.
                log.info("Already in game, redirecting to game page.");
                ui.navigate("game");
            } else {
                // Not in any game. Stay on this page.
            }
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        broadcasterRegistration.remove();
        broadcasterRegistration = null;
        super.onDetach(detachEvent);
    }

    private void handleMessage(UI ui, AbstractGameMessage message) {
        if (message.getMessageType() == AbstractGameMessage.MessageType.GameStateUpdate) {
            GameStateUpdateMessage gameStateUpdateMessage = (GameStateUpdateMessage) message;
            ui.access(() -> {
                gameProgressComponent.update(gameStateUpdateMessage.getGameState());
                if (gameStateUpdateMessage.getGameState().getStatus() == GameStatus.PLAYING) {
                    signInForm.setVisible(false);
                } else {
                    signInForm.setVisible(true);
                }
            });
        }
    }
}

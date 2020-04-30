package com.elletrudgett.cards.cah;

import com.elletrudgett.cards.cah.game.GameState;
import com.elletrudgett.cards.cah.game.Player;
import com.elletrudgett.cards.cah.game.messages.AbstractGameMessage;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Terrible People")
@Log4j2
public class LobbyPage extends VerticalLayout {
    private Registration broadcasterRegistration;
    private final Div rooms;
    private UUID selectedRoom;

    public LobbyPage() {
        addClassName("tp-lobby");

        add(new H2("Terrible People"));
        add(new H4("by Elle ❤️"));

        rooms = new Div();
        add(rooms);

        update();
    }

    private String chooseMotto() {
        long index = Instant.now().getEpochSecond() / 10;

        String[] mottos = new String[] {
                "A terrible game for terrible people.",
                "Warning, this game is offensive.",
                "Your password is safe. Promise.",
                "*shifty eyes*"
        };

        return mottos[(int)(index % mottos.length)];
    }

    /**
     * Note: This Will Not Scale! It will receive every event from every game in existence!!
     */
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        UI ui = attachEvent.getUI();
        broadcasterRegistration = Broadcaster.register(newMessage -> {
            handleMessage(ui, newMessage);
        });

        checkIfAlreadyInGame(ui);
    }

    private void checkIfAlreadyInGame(UI ui) {
        Player player = (Player) VaadinSession.getCurrent().getSession().getAttribute("player");
        if (player != null) {
            if (player.getRoom() != null) {
                // We have an existing player in a room, check if they are still in that room
                Optional<GameState> roomOptional = GameState.getInstance(player.getRoom());
                if (roomOptional.isPresent()) {
                    Optional<Player> validatedPlayer = roomOptional.get().validatePlayer(player);
                    if (validatedPlayer.isPresent()) {
                        // We are in the game, go ahead to the game page.
                        log.info("Already in game, redirecting to game page.");
                        ui.navigate("game");
                    }
                }
            }
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        broadcasterRegistration.remove();
        broadcasterRegistration = null;
        super.onDetach(detachEvent);
    }

    private void update() {
        rooms.removeAll();
        for (GameState room : GameState.getGameStateInstances().values()) {
            RoomComponent progress = new RoomComponent();
            progress.update(room);
            progress.addClickListener(event -> {
                selectedRoom = room.getUuid();

                Dialog loginDialog = new Dialog();
                loginDialog.add(new SignInForm(loginDialog::close));
                loginDialog.open();
            });
            add(progress);
        }
    }

    private void handleMessage(UI ui, AbstractGameMessage message) {
        if (message.getMessageType() == AbstractGameMessage.MessageType.GameStateUpdate) {
            ui.access(this::update);
        }
    }
}

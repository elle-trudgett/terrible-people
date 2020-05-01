package com.elletrudgett.cards.cah;

import com.elletrudgett.cards.cah.game.Player;
import com.elletrudgett.cards.cah.game.Room;
import com.elletrudgett.cards.cah.game.Statics;
import com.elletrudgett.cards.cah.game.User;
import com.elletrudgett.cards.cah.game.messages.AbstractGameMessage;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import lombok.extern.log4j.Log4j2;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.elletrudgett.cards.cah.game.Statics.loadUser;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Terrible People")
@Log4j2
public class LobbyPage extends VerticalLayout {
    private Registration broadcasterRegistration;
    private final Div rooms;
    private final Dialog loginDialog;
    private final H4 whoamI;

    public LobbyPage() {
        addClassName("tp-lobby");

        add(new H2("Terrible People"));
        add(new H4("by Elle ❤️"));

        whoamI = new H4("");
        add(whoamI);

        loginDialog = new Dialog();
        loginDialog.setCloseOnEsc(false);
        loginDialog.setCloseOnOutsideClick(false);
        loginDialog.add(new SignInForm(this::onLogin));
        loginIfNecessary();

        Button createRoomButton = new Button("Create room");
        createRoomButton.setWidthFull();
        createRoomButton.addClickListener(event -> {
            try {
                loginIfNecessary();
                loadUser().ifPresent(user -> {
                    Room newRoom = Room.create(loadUser().map(u -> u.getName() + "'s room").orElse("Unnamed room"));
                    joinRoom(newRoom.getUuid());
                });
            } catch (Exception e) {
                Notification.show("Couldn't create room: " + e.getMessage(), 3000, Notification.Position.MIDDLE);
            }
        });
        add(createRoomButton);

        rooms = new Div();
        add(rooms);

        update();
    }

    private void onLogin() {
        loginDialog.close();
        UI.getCurrent().getPage().reload();
    }

    private void loginIfNecessary() {
        if (!loadUser().isPresent()) {
            loginDialog.open();
        }
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

        Statics.loadUser().ifPresent(user -> {
            if (user.getEmail().isEmpty()) {
                whoamI.add("Logged in as " + user.getName() + ". ");
            } else {
                whoamI.add("Logged in as " + user.getName() + " (" + user.getEmail() + "). ");
            }
            Anchor logOutLink = new Anchor("#", "Log out?");
            logOutLink.getElement().addEventListener("click", e -> {
                try {
                    Statics.clearUser();
                    UI.getCurrent().getPage().reload();
                } catch (UnsupportedEncodingException ex) {
                    log.error("Unable to clear user");
                }
            });
            whoamI.add(logOutLink);
        });
    }

    private void checkIfAlreadyInGame(UI ui) {
        if (Statics.hasPlayer()) {
            Player player = Statics.getPlayer();
            if (player.getRoomUuid() != null) {
                // We have an existing player in a roomUuid, check if they are still in that roomUuid
                Optional<Room> roomOptional = Room.get(player.getRoomUuid());
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
        for (Room room : Room.getRooms().values()) {
            RoomComponent progress = new RoomComponent(room);
            progress.addClickListener(event -> {
                UUID selectedRoomUuid = room.getUuid();
                Optional<User> userOptional = loadUser();

                loginIfNecessary();
                joinRoom(selectedRoomUuid);
            });
            rooms.add(progress);
        }
    }

    private void joinRoom(UUID selectedRoomUuid) {
        UI ui = UI.getCurrent();
        ui.access(() -> {
            loadUser().ifPresent(user -> {
                Player player = new Player(
                        user.getName(),
                        MD5Helper.md5Hex(user.getEmail()),
                        ui.getSession().getBrowser()
                );
                player.setRoomUuid(selectedRoomUuid);
                Statics.setPlayer(ui, player);
                Optional<Room> roomOptional = Room.get(selectedRoomUuid);
                if (roomOptional.isPresent()) {
                    Room room = roomOptional.get();
                    room.addPlayer(player);
                    Statics.setRoom(ui, room);
                    ui.navigate("game");
                } else {
                    Notification.show("Failed to join the room.", 3000, Notification.Position.MIDDLE);
                }
            });
        });
    }

    /**
     * Todo: Make me scalable
     */
    private void handleMessage(UI ui, AbstractGameMessage message) {
        ui.access(this::update);
    }
}

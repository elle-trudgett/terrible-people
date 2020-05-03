package com.elletrudgett.cards.cah;

import com.elletrudgett.cards.cah.game.*;
import com.elletrudgett.cards.cah.game.messages.AbstractGameMessage;
import com.elletrudgett.cards.cah.game.messages.GameUpdateMessage;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import static com.elletrudgett.cards.cah.game.Statics.*;

@Route(value = "game", layout = MainLayout.class)
@PageTitle("Terrible People")
@StyleSheet("./my-styles/game.css")
@JavaScript("./js/game.js")
@Log4j2
//@PreserveOnRefresh
public class GamePage extends VerticalLayout {
    private final PlayersComponent playersComponent;
    private final RoundDisplayComponent roundDisplayComponent;
    private final HandComponent handComponent;
    private final SubmissionsComponent submissionsComponent;
    private final GameMenuBar gameMenuBar;
    private final WaitingOnComponent waitingOnComponent;
    private final Button submitButton;
    private final H5 nextRoundHeader;
    private final GameInfoHeadingComponent gameInfoHeadingComponent = new GameInfoHeadingComponent();
    Registration broadcasterRegistration;
    private PlayerSubmission mySubmission = new PlayerSubmission(Collections.emptyList());

    public GamePage() {
        addClassName("tp-game-component");

        gameMenuBar = new GameMenuBar();
        add(gameMenuBar);
        playersComponent = new PlayersComponent();
        add(gameInfoHeadingComponent);
        add(playersComponent);

        roundDisplayComponent = new RoundDisplayComponent(mySubmission, this::skipCard);
        add(roundDisplayComponent);

        handComponent = new HandComponent(this::handSelectionChanged, mySubmission);
        handComponent.setVisible(false);
        add(handComponent);

        submitButton = new Button("Submit your Entry");
        submitButton.setWidthFull();
        submitButton.addClickListener(buttonClickEvent -> {
            getRoom().submit(getPlayer(), handComponent.getSelected());
            handComponent.clearSelected();
        });
        submitButton.setVisible(false);
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitButton.addClassNames("tp-submit-button", "breathing");
        add(submitButton);

        submissionsComponent = new SubmissionsComponent();
        submissionsComponent.setVisible(false);
        add(submissionsComponent);

        nextRoundHeader = new H5("Next round will start shortly.");
        nextRoundHeader.setVisible(false);
        add(nextRoundHeader);

        waitingOnComponent = new WaitingOnComponent();
        waitingOnComponent.setVisible(false);
        add(waitingOnComponent);

        // Load decks if they haven't been loaded already?
        CardPackRepository.loadPacks();
    }

    private void skipCard() {
        if (amICzar() && getRoom().getSubmissions().isEmpty()) {
            getRoom().skip();
        }
    }

    @ClientCallable
    public void heartbeat() {
        getRoomOptional().map(r -> {
            r.heartbeat(getPlayer().getUuid());
            return r;
        });
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        UI ui = attachEvent.getUI();
        broadcasterRegistration = Broadcaster.register(newMessage -> {
            Statics.establishSession(ui.getSession().getSession());
            handleMessage(ui, newMessage);
        });

        if (!hasPlayer()) {
            // Not signed in
            ui.navigate("");
            return;
        }

        // Validate our getPlayer() and roomUuid state

        boolean matchingRoomUuid = getRoom().getUuid().equals(getPlayer().getRoomUuid());

        if (matchingRoomUuid) {
            // Get the latest room object from the server
            Optional<Room> roomOptional = Room.get(getPlayer().getRoomUuid());
            if (roomOptional.isPresent()) {
                Room room = roomOptional.get();
                Optional<Player> playerOptional = room.validatePlayer(getPlayer());
                if (playerOptional.isPresent()) {
                    // We are part of this game, update getPlayer() state and set us as active
                    setPlayer(ui, playerOptional.get());
                    getPlayer().setLastHeartbeat(Instant.now());
                    room.setPlayerActive(getPlayer(), true);
                    setRoom(ui, room);

                    gameInfoHeadingComponent.update(room);

                    updateUI();
                    return;
                }
            }
        }

        // We are not part of this game. Drop out to the sign-in page.
        log.info("Not part of a game, redirecting to sign in.");
        Statics.unsetPlayer(ui);
        Statics.unsetRoom(ui);
        ui.navigate("");
    }

    private void handSelectionChanged() {
        updateUI();
    }

    private void updateUI() {
        getUI().ifPresent(ui -> ui.access(() -> {
            if (!getRoomOptional().isPresent() || !getPlayerOptional().isPresent()) {
                // Nothing to do here if the state is destroyed.
                return;
            }

            GameStatus gameStatus = getRoom().getStatus();
            boolean startGameButtonEnabled = gameStatus != GameStatus.PLAYING;
            startGameButtonEnabled &= getRoom().getPlayers().size() > 1;
            startGameButtonEnabled &= getPlayer().isVip();
            gameMenuBar.getStartGameMenuItem().setEnabled(startGameButtonEnabled);
            gameMenuBar.getEndGameMenuItem().setEnabled(getPlayer().isVip() && gameStatus == GameStatus.PLAYING);

            roundDisplayComponent.setVisible(gameStatus == GameStatus.PLAYING || gameStatus == GameStatus.GAME_OVER);

            boolean iAmCzar = amICzar();
            gameInfoHeadingComponent.update(getRoom());
            playersComponent.update(getRoom().getPlayers());
            roundDisplayComponent.update(getRoom(), iAmCzar);

            nextRoundHeader.setVisible(false);

            for (Player gsPlayer : getRoom().getPlayers()) {
                if (gsPlayer.equals(getPlayer())) {
                    handComponent.setHand(getPlayer().getHand());
                    handComponent.setCardLimit(getRoom().getCardsToPick());
                    if (iAmCzar) {
                        handComponent.disable();
                    } else {
                        handComponent.enable();
                    }

                    handComponent.update();
                }
            }

            boolean showHandComponent = false;

            if (gameStatus == GameStatus.PLAYING) {
                switch (getRoom().getPlayPhase()) {
                    case ANSWERING_QUESTION:
                        submissionsComponent.setVisible(false);
                        if (getRoom().playerHasSubmitted(getPlayer()) || iAmCzar) {
                            waitingOnComponent.setVisible(true);
                        } else {
                            waitingOnComponent.setVisible(false);
                            showHandComponent = true;
                        }
                        break;
                    case DELIBERATING:
                        waitingOnComponent.setVisible(false);
                        submissionsComponent.setSubmissions(getRoom().getSubmissions());
                        submissionsComponent.update(null, iAmCzar);
                        submissionsComponent.setVisible(true);

                        break;
                    case WINNING_ANSWER_PRESENTED:
                        waitingOnComponent.setVisible(false);
                        submissionsComponent.setSubmissions(getRoom().getSubmissions());
                        submissionsComponent.update(getRoom().getRoundWinner(), iAmCzar);
                        submissionsComponent.setVisible(true);
                        nextRoundHeader.setVisible(true);
                        break;
                }
            } else {
                submissionsComponent.setVisible(false);
                waitingOnComponent.setVisible(false);
            }

            handComponent.setVisible(showHandComponent);
            boolean submitButtonVisibility = showHandComponent && handComponent.getSelected().size() == getRoom().getCardsToPick();
            submitButton.setVisible(submitButtonVisibility);
            submitButton.setEnabled(true);
        }));
    }

    private boolean amICzar() {
        return getRoom().isCzar(getPlayer());
    }

    private void handleMessage(UI ui, AbstractGameMessage message) {
        if (message.getMessageType() == AbstractGameMessage.MessageType.GameUpdate) {
            GameUpdateMessage gameUpdateMessage = (GameUpdateMessage) message;
            // Is it for me?
            if (gameUpdateMessage.getRoom().getUuid().equals(getRoom().getUuid())) {
                setRoom(ui, gameUpdateMessage.getRoom());
            }
        } else if (message.getMessageType() == AbstractGameMessage.MessageType.WaitingOn) {
            WaitingOnMessage waitingOnMessage = (WaitingOnMessage) message;
            ui.access(() -> waitingOnComponent.updateNames(waitingOnMessage.getNames()));
        } else if (message.getMessageType() == AbstractGameMessage.MessageType.PlayerKicked) {
            // check if it was me that got kicked!
            if (!getRoom().validatePlayer(getPlayer()).isPresent()) {
                // Time to go.
                ui.access(() -> Notification.show("You have been kicked from the game.", 1500, Notification.Position.MIDDLE));
                removeBroadcasterRegistration();
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        ui.getPage().reload();
                    }
                }, 1500);
            }
        } else if (message.getMessageType() == AbstractGameMessage.MessageType.CardSkipped) {
            ui.access(() -> Notification.show("The card was skipped.", 1000, Notification.Position.MIDDLE));
            handComponent.clearSelected();
        } else if (message.getMessageType() == AbstractGameMessage.MessageType.ResetGame) {
            handComponent.clearSelected();
        }

        updateUI();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        removeBroadcasterRegistration();
        super.onDetach(detachEvent);
    }

    private void removeBroadcasterRegistration() {
        if (broadcasterRegistration != null) {
            broadcasterRegistration.remove();
            broadcasterRegistration = null;
        }
    }

    @Getter
    private class GameMenuBar extends MenuBar {
        private final MenuItem startGameMenuItem;
        private final MenuItem endGameMenuItem;

        public GameMenuBar() {
            startGameMenuItem = addItem("Start game", e -> {
                getRoom().startGame();
            });
            endGameMenuItem = addItem("End game", e -> {
                getRoom().endGame();
            });

            addItem("Leave room", e -> {
                if (getPlayer() != null) {
                    getRoom().remove(getPlayer());
                    getUI().ifPresent(ui -> {
                        Statics.unsetPlayer(ui);
                        Statics.unsetRoom(ui);
                    });
                }
                getUI().ifPresent(ui -> ui.navigate(""));
            });
        }
    }
}

package com.elletrudgett.cards.cah;

import com.elletrudgett.cards.cah.game.*;
import com.elletrudgett.cards.cah.game.messages.AbstractGameMessage;
import com.elletrudgett.cards.cah.game.messages.GameStateUpdateMessage;
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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "game", layout = MainLayout.class)
@PageTitle("Terrible People")
@StyleSheet("./my-styles/game.css")
@JavaScript("./js/game.js")
@Log4j2
@PreserveOnRefresh
public class GamePage extends VerticalLayout {
    private final PlayersComponent playersComponent;
    private final RoundDisplayComponent roundDisplayComponent;
    private final HandComponent handComponent;
    private final SubmissionsComponent submissionsComponent;
    private final GameMenuBar gameMenuBar;
    private final WaitingOnComponent waitingOnComponent;
    private final Button submitButton;
    private final H5 gameInfoHeading;
    private final H5 nextRoundHeader;
    Registration broadcasterRegistration;
    private Player player;
    private GameState currentGameState;
    private PlayerSubmission mySubmission = new PlayerSubmission(Collections.emptyList());

    public GamePage() {
        addClassName("tp-game-component");

        gameMenuBar = new GameMenuBar();
        add(gameMenuBar);
        playersComponent = new PlayersComponent();
        gameInfoHeading = new H5("");
        add(gameInfoHeading);
        add(playersComponent);

        roundDisplayComponent = new RoundDisplayComponent(mySubmission, this::skipCard);
        add(roundDisplayComponent);

        handComponent = new HandComponent(this::handSelectionChanged, mySubmission);
        handComponent.setVisible(false);
        add(handComponent);

        submitButton = new Button("Submit");
        submitButton.setWidthFull();
        submitButton.addClickListener(buttonClickEvent -> {
            List<Card> selectedCards = handComponent.getSelected();
            GameState.getInstance().submit(player, selectedCards.stream().map(Card::getContent).collect(Collectors.toList()));
            handComponent.clearSelected();
        });
        submitButton.setVisible(false);
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add(submitButton);

        submissionsComponent = new SubmissionsComponent();
        submissionsComponent.setVisible(false);
        add(submissionsComponent);

        nextRoundHeader = new H5("Next round will start in 8 seconds.");
        nextRoundHeader.setVisible(false);
        add(nextRoundHeader);

        waitingOnComponent = new WaitingOnComponent();
        waitingOnComponent.setVisible(false);
        add(waitingOnComponent);
        currentGameState = GameState.getInstance();

        // Load decks if they haven't been loaded already?
        CardPackRepository.loadPacks();
    }

    private void skipCard() {
        if (amICzar() && currentGameState.getSubmissions().isEmpty()) {
            GameState.getInstance().skip();
        }
    }

    @ClientCallable
    public void heartbeat() {
        GameState.getInstance().heartbeat(player.getUuid());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        UI ui = attachEvent.getUI();
        broadcasterRegistration = Broadcaster.register(newMessage -> {
            handleMessage(ui, newMessage);
        });

        player = (Player) VaadinSession.getCurrent().getSession().getAttribute("player");
        if (player == null) {
            // Not signed in
            ui.navigate("");
            return;
        }

        // Validate our player state
        Optional<Player> validatedPlayer = GameState.getInstance().validatePlayer(this.player);
        if (validatedPlayer.isPresent()) {
            // We are part of this game, get our state
            player = validatedPlayer.get();
            player.setLastHeartbeat(Instant.now());
            GameState.getInstance().setPlayerActive(player, true);
            GlobalHelper.savePlayer(player);
        } else {
            // We are not part of this game. Drop out to the sign-in page.
            log.info("Not part of a game, redirecting to sign in.");
            player = null;
            GlobalHelper.savePlayer(null);
            ui.navigate("");
        }

        updateGameInfoHeader();

        if (currentGameState != null) {
            updateUI();
        }
    }

    private void handSelectionChanged() {
        updateUI();
    }

    private void updateUI() {
        getUI().ifPresent(ui -> ui.access(() -> {
            boolean startGameButtonEnabled = currentGameState.getStatus() != GameStatus.PLAYING;
            startGameButtonEnabled &= currentGameState.getPlayers().size() > 1;
            startGameButtonEnabled &= player.isVip();
            gameMenuBar.getStartGameMenuItem().setEnabled(startGameButtonEnabled);
            gameMenuBar.getEndGameMenuItem().setEnabled(player.isVip() && currentGameState.getStatus() == GameStatus.PLAYING);

            boolean iAmCzar = amICzar();
            updateGameInfoHeader();
            playersComponent.update(currentGameState.getPlayers());
            roundDisplayComponent.update(currentGameState, iAmCzar);

            nextRoundHeader.setVisible(false);

            for (Player gsPlayer : currentGameState.getPlayers()) {
                if (gsPlayer == player) {
                    handComponent.setHand(player.getHand());
                    handComponent.setCardLimit(currentGameState.getCardsToPick());
                    if (iAmCzar) {
                        handComponent.disable();
                    } else {
                        handComponent.enable();
                    }

                    handComponent.update();
                }
            }

            boolean showHandComponent = false;

            if (currentGameState.getStatus() == GameStatus.PLAYING) {
                roundDisplayComponent.setVisible(true);
                switch (currentGameState.getPlayPhase()) {
                    case ANSWERING_QUESTION:
                        submissionsComponent.setVisible(false);
                        if (currentGameState.playerHasSubmitted(player) || iAmCzar) {
                            waitingOnComponent.setVisible(true);
                        } else {
                            waitingOnComponent.setVisible(false);
                            showHandComponent = true;
                        }
                        break;
                    case DELIBERATING:
                        waitingOnComponent.setVisible(false);
                        submissionsComponent.setSubmissions(currentGameState.getSubmissions());
                        submissionsComponent.update(null, iAmCzar);
                        submissionsComponent.setVisible(true);

                        break;
                    case WINNING_ANSWER_PRESENTED:
                        waitingOnComponent.setVisible(false);
                        submissionsComponent.setSubmissions(currentGameState.getSubmissions());
                        submissionsComponent.update(currentGameState.getRoundWinner(), iAmCzar);
                        submissionsComponent.setVisible(true);
                        nextRoundHeader.setVisible(true);
                        break;
                }
            } else {
                submissionsComponent.setVisible(false);
                waitingOnComponent.setVisible(false);
                roundDisplayComponent.setVisible(false);
            }

            handComponent.setVisible(showHandComponent);
            boolean submitButtonVisibility = showHandComponent && handComponent.getSelected().size() == currentGameState.getCardsToPick();
            submitButton.setVisible(submitButtonVisibility);
            submitButton.setEnabled(true);
        }));
    }

    private boolean amICzar() {
        return currentGameState.getCardCzarPlayer() == player;
    }

    private void handleMessage(UI ui, AbstractGameMessage message) {
        if (message.getMessageType() == AbstractGameMessage.MessageType.GameStateUpdate) {
            GameStateUpdateMessage gameStateUpdateMessage = (GameStateUpdateMessage) message;
            currentGameState = gameStateUpdateMessage.getGameState();
            ui.access(() -> {
                VaadinSession.getCurrent().getSession().setAttribute("currentGameState", currentGameState);
            });
        } else if (message.getMessageType() == AbstractGameMessage.MessageType.WaitingOn) {
            WaitingOnMessage waitingOnMessage = (WaitingOnMessage) message;
            ui.access(() -> {
                waitingOnComponent.updateNames(waitingOnMessage.getNames());
            });
        } else if (message.getMessageType() == AbstractGameMessage.MessageType.PlayerKicked) {
            // check if it was me that got kicked!
            Optional<Player> validatedPlayer = GameState.getInstance().validatePlayer(this.player);
            if (!validatedPlayer.isPresent()) {
                // Time to go.
                ui.access(() -> Notification.show("You have been kicked from the game.", 3000, Notification.Position.MIDDLE));
                removeBroadcasterRegistration();
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        ui.access(() -> ui.navigate(""));
                    }
                }, 3000);
            }
        } else if (message.getMessageType() == AbstractGameMessage.MessageType.CardSkipped) {
            ui.access(() -> Notification.show("The card was skipped.", 1000, Notification.Position.MIDDLE));
            handComponent.clearSelected();
        }

        updateUI();
    }

    private void updateGameInfoHeader() {
        gameInfoHeading.removeAll();
        FontAwesomeIcon usersIcon = new FontAwesomeIcon("fa-users");
        usersIcon.getElement().setAttribute("title", "Players");
        gameInfoHeading.add(usersIcon);
        gameInfoHeading.add(new Span(" " + currentGameState.getPlayers().size()));
        Span spacer = new Span();
        spacer.addClassName("tp-game-info-spacer");
        gameInfoHeading.add(spacer);
        FontAwesomeIcon trophyIcon = new FontAwesomeIcon("fa-trophy");
        trophyIcon.getElement().setAttribute("title", "Points needed to win");
        gameInfoHeading.add(trophyIcon);
        gameInfoHeading.add(new Span(" " + currentGameState.getScoreLimit()));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        removeBroadcasterRegistration();
        super.onDetach(detachEvent);
    }

    private void removeBroadcasterRegistration() {
        broadcasterRegistration.remove();
        broadcasterRegistration = null;
    }

    @Getter
    private class GameMenuBar extends MenuBar {
        private final MenuItem startGameMenuItem;
        private final MenuItem endGameMenuItem;

        public GameMenuBar() {
            startGameMenuItem = addItem("Start game", e -> {
                GameState.getInstance().startGame();
            });
            endGameMenuItem = addItem("End game", e -> {
                GameState.getInstance().endGame();
            });

            addItem("Leave game", e -> {
                if (player != null) {
                    GameState.getInstance().remove(player);
                }
                getUI().ifPresent(ui -> ui.navigate(""));
            });
        }
    }
}

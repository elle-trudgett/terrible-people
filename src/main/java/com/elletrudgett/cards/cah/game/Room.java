package com.elletrudgett.cards.cah.game;

import com.elletrudgett.cards.cah.Broadcaster;
import com.elletrudgett.cards.cah.game.messages.GameUpdateMessage;
import com.elletrudgett.cards.cah.game.messages.ResetGameMessage;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class Room {
    private static final Random RNG = new Random();

    @Getter
    private static final Map<UUID, Room> rooms = new HashMap<>();

    private String name = "Unnamed room";
    private final UUID uuid;
    private List<Player> players = new ArrayList<>();
    private int cardCzar = 0;

    private GameStatus status = GameStatus.WAITING;
    private PlayPhase playPhase = PlayPhase.DRAWING_WHITE_CARDS;
    private Instant playTimestamp = Instant.now();
    private Instant startedAt = Instant.now();
    private int round = 1;
    private int scoreLimit = 5;

    private Deck whiteCardDrawPile = new Deck();
    private Deck blackCardDrawPile = new Deck();
    private Deck discardedWhiteCards = new Deck();
    private Deck winningCards = new Deck();

    private Card currentBlackCard = null;
    private List<Pair<Player, List<Card>>> submissions = new ArrayList<>();
    private Player roundWinner = null;
    private boolean skippedOnce = false;
    private Player winner = null;

    private Room() {
        uuid = UUID.randomUUID();
        setupDecks();
        PlayerActivityChecker.monitor(this);
    }

    public static Optional<Room> get(UUID uuid) {
        return Optional.ofNullable(rooms.get(uuid));
    }

    public static Room create(String roomName) {
        if (rooms.size() >= 5) {
            throw new UnsupportedOperationException("Too many rooms");
        }
        Room newRoom = new Room();
        newRoom.setName(roomName);
        rooms.put(newRoom.getUuid(), newRoom);
        Broadcaster.broadcast(new LobbyUpdateMessage());
        return newRoom;
    }

    private static void destroy(UUID roomUuid) {
        if (rooms.containsKey(roomUuid)) {
            rooms.remove(roomUuid);
            Broadcaster.broadcast(new LobbyUpdateMessage());
        }
    }

    public void endGame() {
        status = GameStatus.WAITING;
        playPhase = PlayPhase.DRAWING_WHITE_CARDS;
        playTimestamp = Instant.now();

        startedAt = Instant.now();
        resetPlayers();
        prepareForNewGame();
        broadcast();
    }

    private void setupDecks() {
        // Just add in all packs for now
        for (CardPack pack : CardPackRepository.packs) {
            whiteCardDrawPile.getCards().addAll(pack.getWhiteCards());
            blackCardDrawPile.getCards().addAll(pack.getBlackCards());
        }

        whiteCardDrawPile.shuffle();
        blackCardDrawPile.shuffle();
    }

    private void resetPlayers() {
        for (Player player : players) {
            player.setScore(0);
            player.getHand().clear();
        }
    }

    public void startGame() {
        endGame();
        prepareForNewGame();
        Broadcaster.broadcast(new ResetGameMessage());
        status = GameStatus.PLAYING;
        startedAt = Instant.now();
        playTimestamp = Instant.now();
        startNewRound();
    }

    private void prepareForNewGame() {
        whiteCardDrawPile = new Deck();
        blackCardDrawPile = new Deck();
        discardedWhiteCards = new Deck();
        winningCards = new Deck();

        submissions = new ArrayList<>();
        currentBlackCard = null;
        roundWinner = null;
        winner = null;
        if (players.isEmpty()) {
            cardCzar = 0;
        } else {
            cardCzar = RNG.nextInt(players.size());
        }
        round = 1;

        computeScoreLimit();

        setupDecks();
    }

    private void computeScoreLimit() {
        if (players.size() < 5) {
            scoreLimit = 8;
        } else if (players.size() < 8) {
            scoreLimit = 5;
        } else {
            scoreLimit = 3;
        }
    }

    private void startNewRound() {
        skippedOnce = false;
        Broadcaster.broadcast(new WaitingOnMessage(getWaitingOnNames()));
        drawWhiteCards();
    }

    private void drawWhiteCards() {
        playTimestamp = Instant.now();
        playPhase = PlayPhase.DRAWING_WHITE_CARDS;
        for (Player player : players) {
            while (player.getHand().size() < 10) {
                player.getHand().add(whiteCardDrawPile.draw());
            }
        }
        drawBlackCard();
    }

    private void drawBlackCard() {
        currentBlackCard = blackCardDrawPile.draw();

        playPhase = PlayPhase.ANSWERING_QUESTION;
        playTimestamp = Instant.now();
        broadcast();
    }

    public void remove(Player player) {
        if (players.contains(player)) {
            if (getStatus() == GameStatus.WAITING) {
                computeScoreLimit();
            }

            players.remove(player);
            if (player.isVip()) {
                assignNewVip();
            }

            Iterator<Pair<Player, List<Card>>> iterator = submissions.iterator();
            while (iterator.hasNext()) {
                Pair<Player, List<Card>> submission = iterator.next();
                Player submissionPlayer = submission.getKey();
                // Remove submissions from this player who left
//                if (submissionPlayer.equals(player)) {
//                    iterator.remove();
//                }
                // This is confusing as it can shuffle the cards right under the Czar's fingertips.
                // leave it in for now.

                // Return cards that were submitted by the new Czar to their hand
                if (getCardCzarPlayer().isPresent() && submissionPlayer.equals(getCardCzarPlayer().get())) {
                    for (Card card : submission.getValue()) {
                        if (player.getHand().size() < 10) {
                            player.getHand().add(card);
                        }
                    }
                }
            }

            checkIfMoreSubmissionsNeeded();

            if (players.size() < 2) {
                endGame(); // this will broadcast the game state
            } else {
                broadcast(); // otherwise it needs to be broadcasted
            }

            if (players.isEmpty()) {
                destroy(uuid);
            }
        }
    }

    private void assignNewVip() {
        if (!players.isEmpty()) {
            for (Player player : players) {
                player.setVip(false);
            }
            for (Player player : players) {
                if (player.isActive()) {
                    player.setVip(true);
                    break;
                }
            }
        }
        broadcast();
    }

    public String getCzarName() {
        return getCardCzarPlayer().map(Player::getName).orElse("");
    }

    private void validateCardCzar() {
        if (cardCzar >= players.size()) {
            cardCzar = 0;
        }
    }

    public void addPlayer(Player player) {
        // First check if they already exist and are valid
        Optional<Player> validatedPlayer = validatePlayer(player);

        if (validatedPlayer.isPresent()) {
            // They are already added
        } else {
            if (players.isEmpty()) {
                player.setVip(true);
            }
            if (getStatus() == GameStatus.PLAYING) {
                while (player.getHand().size() < 10) {
                    player.getHand().add(whiteCardDrawPile.draw());
                }
            } else if (getStatus() == GameStatus.WAITING) {
                computeScoreLimit();
            }
            players.add(player);
            broadcast();
        }
    }

    public int getCardsToPick() {
        if (currentBlackCard == null) {
            return 0;
        }

        if (currentBlackCard.getEffects().contains(CardSpecialEffect.PICK_2)) {
            return 2;
        } else if (currentBlackCard.getEffects().contains(CardSpecialEffect.PICK_3)) {
            return 3;
        }

        return 1;
    }

    public void submit(Player player, List<Card> submission) {
        if (playerHasSubmitted(player)) {
            return;
        }

        submissions.add(Pair.of(player, new ArrayList<>(submission)));
        player.getHand().removeIf(submission::contains);

        // If we are still waiting on more submissions...
        checkIfMoreSubmissionsNeeded();
    }

    private void checkIfMoreSubmissionsNeeded() {
        if (status != GameStatus.PLAYING) {
            return;
        }

        if (playPhase != PlayPhase.ANSWERING_QUESTION) {
            return;
        }

        List<String> waitingOnNames = getWaitingOnNames();
        if (waitingOnNames.size() > 0) {
            Broadcaster.broadcast(new WaitingOnMessage(waitingOnNames));
        } else {
            // Else ready to proceed.
            playPhase = PlayPhase.DELIBERATING;
            playTimestamp = Instant.now();
            Collections.shuffle(submissions); // make sure we can't tell whose is whose.
            broadcast();
        }
    }

    private void broadcast() {
        Broadcaster.broadcast(new GameUpdateMessage(this));
    }

    private List<String> getWaitingOnNames() {
        return players.stream()
                .filter(p -> !playerHasSubmitted(p) && !isCzar(p))
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    public boolean isCzar(Player p) {
        return !getCardCzarPlayer().isPresent() || p.equals(getCardCzarPlayer().get());
    }

    public boolean playerHasSubmitted(Player player) {
        return submissions.stream().map(Pair::getKey).collect(Collectors.toSet()).contains(player);
    }

    public void selectAnswer(Player player) {
        if (playPhase == PlayPhase.DELIBERATING) {
            playPhase = PlayPhase.WINNING_ANSWER_PRESENTED;
            playTimestamp = Instant.now();
            int newPlayerScore = player.getScore() + 1;
            player.setScore(newPlayerScore);
            roundWinner = player;

            broadcast();

            new Timer().schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            if (newPlayerScore >= scoreLimit) {
                                finishGameWithWinner(player);
                            } else {
                                // Proceed to next round
                                endRound();
                            }
                        }
                    }, 8000
            );
        }
    }

    private void finishGameWithWinner(Player winner) {
        status = GameStatus.GAME_OVER;
        this.winner = winner;
        broadcast();
    }

    private void endRound() {
        submissions.clear();
        round += 1;
        cardCzar += 1;
        validateCardCzar();
        startNewRound();
    }

    public Optional<Player> getCardCzarPlayer() {
        validateCardCzar();
        if (players.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(players.get(cardCzar));
    }

    public Optional<Player> validatePlayer(Player player) {
        for (Player player1 : players) {
            // Match by UUID
            if (player1.getUuid().equals(player.getUuid())) {
                return Optional.of(player1);
            }

            // Match by IP, browser, and name
            if (player1.getIpAddress().equals(player.getIpAddress()) &&
                player1.getBrowser().equals(player.getBrowser()) &&
                player1.getName().equals(player.getName())) {
                return Optional.of(player1);
            }
        }

        return Optional.empty();
    }

    public void setPlayerActive(Player player, boolean active) {
        if (player == null) {
            return;
        }

        boolean broadcastNeeded = false;

        for (Player player1 : players) {
            if (player.getUuid() != null && player1.getUuid() != null && player1.getUuid().equals(player.getUuid())) {
                broadcastNeeded |= player1.isActive() != active;
                player1.setActive(active);
            }
        }

        player.setActive(active);

        if (active) {
            if (noVip()) {
                assignNewVip();
            }
        } else if (player.isVip()) {
            // Inactive players should not be VIP
            assignNewVip();
        }

        broadcastNeeded |= player.isActive() != active;

        if (broadcastNeeded) {
            broadcast();
        }
    }

    private boolean noVip() {
        return players.stream().noneMatch(Player::isVip);
    }

    public void heartbeat(UUID playerUuid) {
        for (Player player : players) {
            if (player.getUuid().equals(playerUuid)) {
                player.setLastHeartbeat(Instant.now());
            }
        }
    }

    public void kickPlayer(Player kicker, Player kickee) {
        if (kicker.isVip()) {
            remove(kickee);
            Broadcaster.broadcast(new PlayerKickedMessage());
        }
    }

    public void giveVip(Player giver, Player taker) {
        if (giver.isVip()) {
            giver.setVip(false);
            taker.setVip(true);
            broadcast();
        }
    }

    public void skip() {
        if (!skippedOnce) {
            skippedOnce = true;
            Broadcaster.broadcast(new CardSkippedMessage());
            drawBlackCard();
        }
    }
}

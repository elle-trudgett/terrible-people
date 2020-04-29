package com.elletrudgett.cards.cah.game;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerActivityChecker {
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(5);

    public static void monitor(GameState gameState) {
        EXECUTOR_SERVICE.submit(() -> {
            while (true) {
                for (Player player : gameState.getPlayers()) {
                    if (Instant.now().isAfter(player.getLastHeartbeat().plusSeconds(5))) {
                        gameState.setPlayerActive(player, false);
                    } else {
                        gameState.setPlayerActive(player, true);
                    }
                }
                Thread.sleep(1000);
            }
        });
    }
}

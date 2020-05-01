package com.elletrudgett.cards.cah.game;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerActivityChecker {
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(5);

    public static void monitor(Room room) {
        EXECUTOR_SERVICE.submit(() -> {
            while (true) {
                for (Player player : room.getPlayers()) {
                    if (Instant.now().isAfter(player.getLastHeartbeat().plusSeconds(5))) {
                        room.setPlayerActive(player, false);
                    } else {
                        room.setPlayerActive(player, true);
                    }
                }
                Thread.sleep(1000);
            }
        });
    }
}

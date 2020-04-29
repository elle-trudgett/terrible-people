package com.elletrudgett.cards.cah;

import com.elletrudgett.cards.cah.game.messages.AbstractGameMessage;
import com.vaadin.flow.shared.Registration;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Broadcaster {
    static Executor executor = Executors.newSingleThreadExecutor();

    static LinkedList<Consumer<AbstractGameMessage>> listeners = new LinkedList<>();

    public static synchronized Registration register(
            Consumer<AbstractGameMessage> listener) {
        listeners.add(listener);

        return () -> {
            synchronized (Broadcaster.class) {
                listeners.remove(listener);
            }
        };
    }

    public static synchronized void broadcast(AbstractGameMessage message) {
        for (Consumer<AbstractGameMessage> listener : listeners) {
            executor.execute(() -> listener.accept(message));
        }
    }
}

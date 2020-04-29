package com.elletrudgett.cards.cah.game;

import com.vaadin.flow.server.WebBrowser;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class Player {
    private final String name;
    private final String emailHash;
    private final UUID uuid;
    private int score = 0;
    private final List<Card> hand = new ArrayList<>();
    private boolean vip;
    private boolean active = true;
    private Instant lastHeartbeat = Instant.now();
    private String ipAddress;
    private String browser;

    public Player(String name, String emailHash, WebBrowser browser) {
        this.name = name;
        this.emailHash = emailHash;
        this.ipAddress = browser.getAddress();
        this.browser = browser.getBrowserApplication();
        this.uuid = UUID.randomUUID();
    }
}

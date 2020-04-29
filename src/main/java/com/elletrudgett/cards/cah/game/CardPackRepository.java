package com.elletrudgett.cards.cah.game;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CardPackRepository {
    public static final List<CardPack> packs = new ArrayList<>();

    private CardPackRepository() {
    }

    public static void loadPacks() {
        if (packs.isEmpty()) {
            packs.add(CardPackReader.loadCardPack("base_game"));
        }
    }
}

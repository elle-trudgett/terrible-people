package com.elletrudgett.cards.cah.game;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CardPackRepository {
    public static List<CardPack> packs = new ArrayList<>();

    private CardPackRepository() {
    }

    public static void loadPacks() {
        packs = new ArrayList<>();
        packs.add(CardPackReader.loadCardPack("base_game"));
    }
}

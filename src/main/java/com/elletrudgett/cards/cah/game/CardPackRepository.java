package com.elletrudgett.cards.cah.game;

import com.helger.commons.string.util.LevenshteinDistance;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Log4j2
public class CardPackRepository {
    public static List<CardPack> packs = new ArrayList<>();

    private CardPackRepository() {
    }

    public static void loadPacks() {
        packs = new ArrayList<>();
        packs.add(CardPackReader.loadCardPack("base_game"));
        packs.add(CardPackReader.loadCardPack("absurd_box"));
        packs.add(CardPackReader.loadCardPack("red_box"));

        checkPacks();
        logPackInfo();
    }

    private static void checkPacks() {
        List<Card> blackCards = packs.stream().flatMap(p -> p.getBlackCards().stream()).collect(Collectors.toList());
        List<Card> whiteCards = packs.stream().flatMap(p -> p.getWhiteCards().stream()).collect(Collectors.toList());

        findDuplicates(blackCards);
        findDuplicates(whiteCards);
    }

    private static void findDuplicates(List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            for (int j = i + 1; j < cards.size(); j++) {
                Card otherCard = cards.get(j);

                int distance = LevenshteinDistance.getDistance(card.getContent(), otherCard.getContent());
                if (distance < (card.getContent().length() / 5)) {
                    log.warn("Potential duplicate cards: " + card.getContent() + " / " + otherCard.getContent());
                }
            }
        }
    }

    private static void logPackInfo() {
        log.info("Packs Loaded: " + StringUtils.join(packs.stream().map(CardPack::getName).collect(Collectors.toList()), ", "));
        log.info("Total cards loaded: " + packs.stream().map(p -> p.getBlackCards().size() + p.getWhiteCards().size()).reduce(0, Integer::sum));
    }
}

package com.elletrudgett.cards.cah.game;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class CardPackReader {
    public static CardPack loadCardPack(String packFilename) {
        log.info("Loading " + packFilename);
        String packName = "Unnamed pack";
        List<Card> blackCards = new ArrayList<>();
        List<Card> whiteCards = new ArrayList<>();

        try {
            String packPath = "static/cards/" + packFilename + ".pack";
            try (InputStream resource = CardPackReader.class.getClassLoader().getResourceAsStream(packPath)) {
                if (resource == null) {
                    throw new IllegalStateException("Unable to find " + packPath);
                }

                List<String> lines =
                        new BufferedReader(new InputStreamReader(resource,
                                StandardCharsets.UTF_8)).lines().collect(Collectors.toList());
                CardType currentCardType = null;

                for (String line : lines) {
                    if (line.startsWith("***VETO***")) {
                        log.warn("Skipping vetoed card: " + line);
                        continue;
                    }

                    if (line.contains("### Pack")) {
                        line = line.replace("### Pack:", "");
                        line = line.replace("###", "");
                        packName = line.trim();
                        continue;
                    } else if (line.contains("### Black")) {
                        currentCardType = CardType.BLACK;
                        continue;
                    } else if (line.contains("### White")) {
                        currentCardType = CardType.WHITE;
                        continue;
                    } else if (line.contains("###")) {
                        throw new IllegalArgumentException("Malformed line: " + line);
                    }

                    if (currentCardType == null) {
                        throw new IllegalStateException("Expected card type to be set.");
                    } else if (currentCardType == CardType.BLACK) {
                        if (!line.contains("|||")) {
                            throw new IllegalArgumentException("Black card didn't have options splitter: " + line);
                        }

                        String[] split = line.split("\\|\\|\\|");

                        String cardContent = split[0];
                        Card newCard = new Card(currentCardType, packName, cardContent);

                        if (split.length > 1) {
                            if (split[1].contains("PICK 2")) {
                                newCard.getEffects().add(CardSpecialEffect.PICK_2);
                            } else if (split[1].contains("PICK 3")) {
                                newCard.getEffects().add(CardSpecialEffect.PICK_3);
                            } else if (split[1].contains("DRAW 2")) {
                                newCard.getEffects().add(CardSpecialEffect.DRAW_2);
                            }
                        }

                        // Double check the right number of picks
                        int numSpaces = StringUtils.countMatches(cardContent.replaceAll("_+", "_"), "_");
                        if (numSpaces == 2) {
                            if (!newCard.getEffects().contains(CardSpecialEffect.PICK_2)) {
                                throw new IllegalArgumentException("Expected PICK 2 for card: " + newCard.getContent());
                            }
                        } else if (numSpaces == 3) {
                            if (!newCard.getEffects().contains(CardSpecialEffect.PICK_3)) {
                                throw new IllegalArgumentException("Expected PICK 3 for card: " + newCard.getContent());
                            }
                        } else if (numSpaces > 3) {
                            throw new IllegalArgumentException("Too many spaces (" + numSpaces + ") for card: " + newCard.getContent());
                        }

                        blackCards.add(newCard);
                    } else if (currentCardType == CardType.WHITE) {
                        Card newCard = new Card(currentCardType, packName, line);

                        if (line.startsWith("img") && line.contains("|||")) {
                            String[] split = line.split("\\|\\|\\|");
                            newCard.setImage(split[0]);
                            newCard.setContent(split[1]);
                        }

                        whiteCards.add(newCard);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        log.info("Loaded " + packName + " from " + packFilename + ". " + blackCards.size() + " black cards, " + whiteCards.size() + " white cards.");
        return new CardPack(packName, blackCards, whiteCards);
    }
}

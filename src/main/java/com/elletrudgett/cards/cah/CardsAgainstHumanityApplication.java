package com.elletrudgett.cards.cah;

import com.elletrudgett.cards.cah.game.CardPackRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Log4j2
public class CardsAgainstHumanityApplication {
    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack" , "true");
        log.info("Preference for IPv4 stack: " + System.getProperty("java.net.preferIPv4Stack"));
        CardPackRepository.loadPacks();
        SpringApplication.run(CardsAgainstHumanityApplication.class, args);
    }
}

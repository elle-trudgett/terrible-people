package com.elletrudgett.cards.cah;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.info.BuildProperties;

@SpringBootApplication
@Log4j2
public class CardsAgainstHumanityApplication {
    @Autowired
    public static BuildProperties buildProperties;

    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack" , "true");
        log.info("Preference for IPv4 stack: " + System.getProperty("java.net.preferIPv4Stack"));
        SpringApplication.run(CardsAgainstHumanityApplication.class, args);
    }
}

package com.olma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OlmaApplication {

    public static void main(String[] args) {
        SpringApplication.run(OlmaApplication.class, args);
    }
}

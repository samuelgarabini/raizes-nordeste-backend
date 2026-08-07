package com.raizes.nordeste;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling; // 1. Adicionado este import

@SpringBootApplication
@EnableCaching
@EnableScheduling // 2. Adicionada esta anotação para o Outbox Scheduler funcionar
public class RaizesNordesteApplication {

    public static void main(String[] args) {
        SpringApplication.run(RaizesNordesteApplication.class, args);
    }
}
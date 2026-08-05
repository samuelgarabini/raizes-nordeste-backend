package com.raizes.nordeste;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching; // 1. Adicione este import

@SpringBootApplication
@EnableCaching // 2. Adicione esta anotação aqui
public class RaizesNordesteApplication {

    public static void main(String[] args) {
        SpringApplication.run(RaizesNordesteApplication.class, args);
    }
}
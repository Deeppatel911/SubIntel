package com.example.subintel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SubIntelApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubIntelApplication.class, args);
    }
}

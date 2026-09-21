package com.example.ttsbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TtsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TtsBackendApplication.class, args);
    }
}
package com.resolvyx.resolvyx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ResolvyxApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResolvyxApplication.class, args);
    }
}
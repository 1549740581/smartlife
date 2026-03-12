package com.yxtech.smartlife;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SmartLifeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartLifeApplication.class, args);
    }
}

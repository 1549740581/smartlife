package com.yxtech.smartlife.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AdminAuthConfiguration {

    @Bean
    public Clock adminAuthClock() {
        return Clock.systemDefaultZone();
    }
}

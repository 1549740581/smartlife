package com.yxtech.smartlife.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class CoreTimeConfiguration {

    @Bean("systemClock")
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }
}

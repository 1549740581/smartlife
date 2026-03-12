package com.yxtech.smartlife.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.admin-auth")
public class AdminAuthProperties {

    private long sessionTtlMinutes = 120;
    private String sessionKeyPrefix = "smart-life:admin:session:";
}

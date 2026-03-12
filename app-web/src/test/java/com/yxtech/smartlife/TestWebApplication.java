package com.yxtech.smartlife;

import com.yxtech.smartlife.config.FileStorageProperties;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableAutoConfiguration
@EnableConfigurationProperties(FileStorageProperties.class)
public class TestWebApplication {
}

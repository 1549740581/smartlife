package com.yxtech.smartlife.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.file-storage")
public class FileStorageProperties {

    private String uploadDir = "uploads";
    private String publicPath = "/uploads/**";
    private String publicUrlPrefix = "/uploads/";
    private long maxFileSize = 5 * 1024 * 1024;
}

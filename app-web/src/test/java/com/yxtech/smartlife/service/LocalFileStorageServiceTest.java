package com.yxtech.smartlife.service;

import com.yxtech.smartlife.config.FileStorageProperties;
import com.yxtech.smartlife.dto.FileUploadResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalFileStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void storeImageShouldPersistFileAndReturnUrl() throws Exception {
        FileStorageProperties properties = new FileStorageProperties();
        properties.setUploadDir(tempDir.resolve("uploads").toString());
        LocalFileStorageService service = new LocalFileStorageService(properties);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "house.png",
                "image/png",
                "image-content".getBytes()
        );

        FileUploadResponse response = service.storeImage(file, "http://localhost:8080");

        assertTrue(response.getUrl().startsWith("http://localhost:8080/uploads/"));
        assertTrue(Files.exists(tempDir.resolve("uploads").resolve(response.getFileName())));
    }
}

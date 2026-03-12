package com.yxtech.smartlife.controller;

import com.yxtech.smartlife.TestWebApplication;
import com.yxtech.smartlife.dto.FileUploadResponse;
import com.yxtech.smartlife.service.LocalFileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileUploadController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = TestWebApplication.class)
class FileUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocalFileStorageService localFileStorageService;

    @Test
    void uploadImageShouldReturnUploadedUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "house.png", "image/png", "img".getBytes());
        when(localFileStorageService.storeImage(any(), eq("http://localhost")))
                .thenReturn(FileUploadResponse.builder()
                        .fileName("house.png")
                        .url("http://localhost/uploads/house.png")
                        .build());

        mockMvc.perform(multipart("/api/files/images").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileName").value("house.png"))
                .andExpect(jsonPath("$.data.url").value("http://localhost/uploads/house.png"));
    }
}

package com.yxtech.smartlife.controller;

import com.yxtech.smartlife.TestBackendApplication;
import com.yxtech.smartlife.auth.AdminAuthInterceptor;
import com.yxtech.smartlife.auth.CurrentAdminArgumentResolver;
import com.yxtech.smartlife.config.AdminWebMvcConfigurer;
import com.yxtech.smartlife.dto.AdminLoginResponse;
import com.yxtech.smartlife.entity.Admin;
import com.yxtech.smartlife.service.AdminAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = TestBackendApplication.class)
@Import({AdminWebMvcConfigurer.class, AdminAuthInterceptor.class, CurrentAdminArgumentResolver.class})
class AdminAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminAuthService adminAuthService;

    @Test
    void loginShouldReturnAdminToken() throws Exception {
        when(adminAuthService.login("admin", "admin123")).thenReturn(AdminLoginResponse.builder()
                .adminId(1L)
                .displayName("系统管理员")
                .adminToken("token-123")
                .expiresAt(java.time.LocalDateTime.of(2026, 3, 12, 12, 0))
                .build());

        mockMvc.perform(post("/api/admin/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"admin",
                                  "password":"admin123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.adminId").value(1))
                .andExpect(jsonPath("$.data.displayName").value("系统管理员"))
                .andExpect(jsonPath("$.data.adminToken").value("token-123"))
                .andExpect(jsonPath("$.data.expiresAt").value("2026-03-12T12:00:00"));
    }

    @Test
    void logoutShouldReturnSuccess() throws Exception {
        when(adminAuthService.requireAdmin("token-123")).thenReturn(buildAdmin());

        mockMvc.perform(post("/api/admin/logout").header("X-Admin-Token", "token-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private Admin buildAdmin() {
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setDisplayName("系统管理员");
        return admin;
    }
}

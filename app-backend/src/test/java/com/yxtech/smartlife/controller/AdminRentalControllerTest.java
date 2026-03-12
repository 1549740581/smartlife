package com.yxtech.smartlife.controller;

import com.yxtech.smartlife.entity.Admin;
import com.yxtech.smartlife.entity.RentalInfo;
import com.yxtech.smartlife.service.AdminAuthService;
import com.yxtech.smartlife.service.RentalService;
import com.yxtech.smartlife.TestBackendApplication;
import com.yxtech.smartlife.auth.AdminAuthInterceptor;
import com.yxtech.smartlife.auth.CurrentAdminArgumentResolver;
import com.yxtech.smartlife.config.AdminWebMvcConfigurer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminRentalController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = TestBackendApplication.class)
@Import({AdminWebMvcConfigurer.class, AdminAuthInterceptor.class, CurrentAdminArgumentResolver.class})
class AdminRentalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminAuthService adminAuthService;

    @MockBean
    private RentalService rentalService;

    @Test
    void getPendingRentalsShouldRequireAdminTokenAndReturnList() throws Exception {
        when(adminAuthService.requireAdmin("token")).thenReturn(buildAdmin());
        when(rentalService.findPendingRentals()).thenReturn(List.of(buildRental(RentalInfo.RentalStatus.PENDING)));

        mockMvc.perform(get("/api/admin/rentals/pending").header("X-Admin-Token", "token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    void reviewRentalShouldReturnApprovedRental() throws Exception {
        when(adminAuthService.requireAdmin("token")).thenReturn(buildAdmin());
        when(rentalService.reviewRental(anyLong(), anyLong(), anyBoolean(), anyString()))
                .thenReturn(buildRental(RentalInfo.RentalStatus.APPROVED));

        mockMvc.perform(post("/api/admin/rentals/1/review")
                        .header("X-Admin-Token", "token")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "action":"APPROVE",
                                  "approved":true,
                                  "reason":""
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    void getRentalDetailShouldReturnRental() throws Exception {
        when(adminAuthService.requireAdmin("token")).thenReturn(buildAdmin());
        when(rentalService.findRentalById(1L)).thenReturn(buildRental(RentalInfo.RentalStatus.PENDING));

        mockMvc.perform(get("/api/admin/rentals/1").header("X-Admin-Token", "token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void offlineRentalShouldReturnOfflineRental() throws Exception {
        when(adminAuthService.requireAdmin("token")).thenReturn(buildAdmin());
        when(rentalService.offlineRental(anyLong(), anyLong(), anyString()))
                .thenReturn(buildRental(RentalInfo.RentalStatus.OFFLINE));

        mockMvc.perform(post("/api/admin/rentals/1/offline")
                        .header("X-Admin-Token", "token")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "action":"OFFLINE",
                                  "approved":false,
                                  "reason":"违规信息"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("OFFLINE"));
    }

    private Admin buildAdmin() {
        Admin admin = new Admin();
        admin.setId(2L);
        admin.setUsername("admin");
        admin.setDisplayName("系统管理员");
        return admin;
    }

    private RentalInfo buildRental(RentalInfo.RentalStatus status) {
        RentalInfo rentalInfo = new RentalInfo();
        rentalInfo.setId(1L);
        rentalInfo.setPublisherUserId(1L);
        rentalInfo.setRentalType(RentalInfo.RentalType.HOUSE);
        rentalInfo.setTitle("两居室");
        rentalInfo.setDescription("近地铁");
        rentalInfo.setPrice(BigDecimal.valueOf(3200));
        rentalInfo.setContactName("张三");
        rentalInfo.setContactPhone("13800000000");
        rentalInfo.setImageUrls("[\"https://img/a.jpg\"]");
        rentalInfo.setStatus(status);
        return rentalInfo;
    }
}

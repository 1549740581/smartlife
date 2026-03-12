package com.yxtech.smartlife.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yxtech.smartlife.TestWebApplication;
import com.yxtech.smartlife.entity.RentalInfo;
import com.yxtech.smartlife.service.RentalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RentalController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = TestWebApplication.class)
class RentalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RentalService rentalService;

    @Test
    void createRentalShouldReturnPendingRental() throws Exception {
        RentalInfo rentalInfo = buildRental();
        when(rentalService.createRental(any())).thenReturn(rentalInfo);

        mockMvc.perform(post("/api/rentals")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "publisherUserId":1,
                                  "rentalType":"HOUSE",
                                  "title":"两居室",
                                  "description":"近地铁",
                                  "price":3200,
                                  "contactName":"张三",
                                  "contactPhone":"13800000000",
                                  "communityName":"智慧小区",
                                  "imageUrls":["https://img/a.jpg"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.title").value("两居室"));
    }

    @Test
    void getPublicRentalsShouldReturnApprovedList() throws Exception {
        RentalInfo rentalInfo = buildRental();
        rentalInfo.setStatus(RentalInfo.RentalStatus.APPROVED);
        when(rentalService.findPublicRentals()).thenReturn(List.of(rentalInfo));

        mockMvc.perform(get("/api/rentals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("APPROVED"))
                .andExpect(jsonPath("$.data[0].rentalType").value("HOUSE"));
    }

    @Test
    void getPublicRentalDetailShouldReturnApprovedRental() throws Exception {
        RentalInfo rentalInfo = buildRental();
        rentalInfo.setStatus(RentalInfo.RentalStatus.APPROVED);
        when(rentalService.findPublicRentalById(1L)).thenReturn(rentalInfo);

        mockMvc.perform(get("/api/rentals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    void getUserRentalDetailShouldReturnOwnedRental() throws Exception {
        RentalInfo rentalInfo = buildRental();
        rentalInfo.setPublisherUserId(8L);
        when(rentalService.findUserRentalById(8L, 1L)).thenReturn(rentalInfo);

        mockMvc.perform(get("/api/rentals/user/8/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.publisherUserId").value(8));
    }

    private RentalInfo buildRental() {
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
        rentalInfo.setStatus(RentalInfo.RentalStatus.PENDING);
        return rentalInfo;
    }
}

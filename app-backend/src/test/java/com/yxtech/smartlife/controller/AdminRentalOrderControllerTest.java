package com.yxtech.smartlife.controller;

import com.yxtech.smartlife.TestBackendApplication;
import com.yxtech.smartlife.auth.AdminAuthInterceptor;
import com.yxtech.smartlife.auth.CurrentAdminArgumentResolver;
import com.yxtech.smartlife.config.AdminWebMvcConfigurer;
import com.yxtech.smartlife.entity.Admin;
import com.yxtech.smartlife.entity.RentalInfo;
import com.yxtech.smartlife.entity.RentalOrder;
import com.yxtech.smartlife.entity.User;
import com.yxtech.smartlife.service.AdminAuthService;
import com.yxtech.smartlife.service.RentalTradeService;
import com.yxtech.smartlife.service.model.RentalOrderAggregate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminRentalOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = TestBackendApplication.class)
@Import({AdminWebMvcConfigurer.class, AdminAuthInterceptor.class, CurrentAdminArgumentResolver.class})
class AdminRentalOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminAuthService adminAuthService;

    @MockBean
    private RentalTradeService rentalTradeService;

    @Test
    void getOrdersShouldReturnOrderList() throws Exception {
        when(adminAuthService.requireAdmin("token")).thenReturn(buildAdmin());
        when(rentalTradeService.listAdminOrders()).thenReturn(List.of(buildAggregate()));

        mockMvc.perform(get("/api/admin/orders").header("X-Admin-Token", "token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].rentalTitle").value("精装两居"))
                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
    }

    @Test
    void cancelOrderShouldReturnCanceledOrder() throws Exception {
        when(adminAuthService.requireAdmin("token")).thenReturn(buildAdmin());
        RentalOrder canceled = buildAggregate().order();
        canceled.setStatus(RentalOrder.OrderStatus.CANCELED);
        when(rentalTradeService.adminCancelOrder(anyLong(), anyLong(), anyString())).thenReturn(canceled);
        when(rentalTradeService.listAdminOrders()).thenReturn(List.of(new RentalOrderAggregate(
                canceled,
                buildAggregate().rentalInfo(),
                buildAggregate().landlord(),
                buildAggregate().tenant()
        )));

        mockMvc.perform(post("/api/admin/orders/100/cancel")
                        .header("X-Admin-Token", "token")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "reason":"管理员取消订单"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELED"));
    }

    private Admin buildAdmin() {
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setDisplayName("系统管理员");
        return admin;
    }

    private RentalOrderAggregate buildAggregate() {
        RentalOrder order = new RentalOrder();
        order.setId(100L);
        order.setConversationId(1L);
        order.setRentalInfoId(10L);
        order.setLandlordUserId(1L);
        order.setTenantUserId(2L);
        order.setStartDate(LocalDate.of(2026, 3, 20));
        order.setEndDate(LocalDate.of(2026, 4, 20));
        order.setStatus(RentalOrder.OrderStatus.ACTIVE);

        RentalInfo rentalInfo = new RentalInfo();
        rentalInfo.setId(10L);
        rentalInfo.setPublisherUserId(1L);
        rentalInfo.setRentalType(RentalInfo.RentalType.HOUSE);
        rentalInfo.setTitle("精装两居");
        rentalInfo.setPrice(BigDecimal.valueOf(4200));
        rentalInfo.setStatus(RentalInfo.RentalStatus.RENTED);

        User landlord = new User();
        landlord.setId(1L);
        landlord.setNickname("房东陈姐");

        User tenant = new User();
        tenant.setId(2L);
        tenant.setNickname("租客小王");

        return new RentalOrderAggregate(order, rentalInfo, landlord, tenant);
    }
}

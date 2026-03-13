package com.yxtech.smartlife.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yxtech.smartlife.TestWebApplication;
import com.yxtech.smartlife.entity.RentalConversation;
import com.yxtech.smartlife.entity.RentalInfo;
import com.yxtech.smartlife.entity.RentalMessage;
import com.yxtech.smartlife.entity.RentalOrder;
import com.yxtech.smartlife.entity.User;
import com.yxtech.smartlife.service.RentalTradeService;
import com.yxtech.smartlife.service.model.RentalConversationAggregate;
import com.yxtech.smartlife.service.model.RentalConversationSummaryAggregate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RentalTradeController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = TestWebApplication.class)
class RentalTradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RentalTradeService rentalTradeService;

    @Test
    void openConversationShouldReturnConversationId() throws Exception {
        RentalConversation conversation = buildConversation();
        when(rentalTradeService.openConversation(10L, 2L)).thenReturn(conversation);

        mockMvc.perform(post("/api/rentals/10/conversation")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void getConversationDetailShouldReturnOrdersAndMessages() throws Exception {
        RentalConversationAggregate aggregate = buildConversationAggregate();
        when(rentalTradeService.getConversationDetail(1L, 2L)).thenReturn(aggregate);

        mockMvc.perform(get("/api/rental-conversations/1").param("userId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.currentUserRole").value("TENANT"))
                .andExpect(jsonPath("$.data.orders[0].status").value("PENDING_CONFIRMATION"))
                .andExpect(jsonPath("$.data.messages[0].messageType").value("TEXT"));
    }

    @Test
    void getConversationListShouldReturnSummary() throws Exception {
        RentalConversationSummaryAggregate aggregate = new RentalConversationSummaryAggregate(
                buildConversation(),
                buildRental(),
                buildLandlord(),
                buildTenant(),
                buildTextMessage(),
                buildOrder()
        );
        when(rentalTradeService.listUserConversations(anyLong())).thenReturn(List.of(aggregate));

        mockMvc.perform(get("/api/rental-conversations").param("userId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].rentalTitle").value("精装两居"))
                .andExpect(jsonPath("$.data[0].counterpartNickname").value("房东陈姐"));
    }

    private RentalConversationAggregate buildConversationAggregate() {
        return new RentalConversationAggregate(
                buildConversation(),
                buildRental(),
                buildLandlord(),
                buildTenant(),
                List.of(buildTextMessage()),
                List.of(buildOrder())
        );
    }

    private RentalConversation buildConversation() {
        RentalConversation conversation = new RentalConversation();
        conversation.setId(1L);
        conversation.setRentalInfoId(10L);
        conversation.setLandlordUserId(1L);
        conversation.setTenantUserId(2L);
        return conversation;
    }

    private RentalInfo buildRental() {
        RentalInfo rentalInfo = new RentalInfo();
        rentalInfo.setId(10L);
        rentalInfo.setPublisherUserId(1L);
        rentalInfo.setRentalType(RentalInfo.RentalType.HOUSE);
        rentalInfo.setTitle("精装两居");
        rentalInfo.setDescription("近地铁");
        rentalInfo.setPrice(BigDecimal.valueOf(4200));
        rentalInfo.setCity("杭州");
        rentalInfo.setDistrict("滨江区");
        rentalInfo.setStreet("长河街道");
        rentalInfo.setCommunityName("卓悦华庭");
        rentalInfo.setStatus(RentalInfo.RentalStatus.APPROVED);
        return rentalInfo;
    }

    private User buildLandlord() {
        User user = new User();
        user.setId(1L);
        user.setNickname("房东陈姐");
        return user;
    }

    private User buildTenant() {
        User user = new User();
        user.setId(2L);
        user.setNickname("租客小王");
        return user;
    }

    private RentalOrder buildOrder() {
        RentalOrder order = new RentalOrder();
        order.setId(100L);
        order.setConversationId(1L);
        order.setRentalInfoId(10L);
        order.setLandlordUserId(1L);
        order.setTenantUserId(2L);
        order.setStartDate(LocalDate.of(2026, 3, 20));
        order.setEndDate(LocalDate.of(2026, 4, 20));
        order.setStatus(RentalOrder.OrderStatus.PENDING_CONFIRMATION);
        return order;
    }

    private RentalMessage buildTextMessage() {
        RentalMessage message = new RentalMessage();
        message.setId(200L);
        message.setConversationId(1L);
        message.setRentalInfoId(10L);
        message.setSenderUserId(2L);
        message.setReceiverUserId(1L);
        message.setMessageType(RentalMessage.MessageType.TEXT);
        message.setContent("我想租这个房子");
        return message;
    }
}

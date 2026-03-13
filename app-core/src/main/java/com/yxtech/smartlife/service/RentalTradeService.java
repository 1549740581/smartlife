package com.yxtech.smartlife.service;

import com.yxtech.smartlife.entity.RentalConversation;
import com.yxtech.smartlife.entity.RentalMessage;
import com.yxtech.smartlife.entity.RentalOrder;
import com.yxtech.smartlife.service.model.RentalConversationAggregate;
import com.yxtech.smartlife.service.model.RentalConversationSummaryAggregate;
import com.yxtech.smartlife.service.model.RentalOrderAggregate;

import java.time.LocalDate;
import java.util.List;

public interface RentalTradeService {

    RentalConversation openConversation(Long rentalInfoId, Long tenantUserId);

    List<RentalConversationSummaryAggregate> listUserConversations(Long userId);

    RentalConversationAggregate getConversationDetail(Long conversationId, Long userId);

    RentalMessage sendTextMessage(Long conversationId, Long senderUserId, String content);

    RentalOrder createOrder(Long conversationId, Long tenantUserId, LocalDate startDate, LocalDate endDate);

    RentalOrder acceptOrder(Long orderId, Long landlordUserId);

    RentalOrder requestCancel(Long orderId, Long userId, String reason);

    RentalOrder confirmCancel(Long orderId, Long userId);

    RentalOrder renewOrder(Long orderId, Long userId, LocalDate startDate, LocalDate endDate);

    List<RentalOrderAggregate> listAdminOrders();

    RentalOrder adminCancelOrder(Long orderId, Long adminId, String reason);

    void sendExpirationReminders();

    void completeExpiredOrders();

    long countUserUnreadMessages(Long userId);

    long countConversationUnreadMessages(Long conversationId, Long userId);

    void markConversationAsRead(Long conversationId, Long userId);
}

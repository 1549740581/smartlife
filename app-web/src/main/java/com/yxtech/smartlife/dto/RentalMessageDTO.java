package com.yxtech.smartlife.dto;

import com.yxtech.smartlife.entity.RentalMessage;
import com.yxtech.smartlife.entity.RentalOrder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class RentalMessageDTO {

    private Long id;
    private Long conversationId;
    private Long rentalInfoId;
    private Long orderId;
    private Long senderUserId;
    private String senderNickname;
    private Long receiverUserId;
    private String receiverNickname;
    private RentalMessage.MessageType messageType;
    private String content;
    private String metadataJson;
    private LocalDateTime createdAt;
    private RentalOrderDTO order;

    public static RentalMessageDTO fromEntity(
            RentalMessage message,
            Map<Long, String> nicknameMap,
            Map<Long, RentalOrder> orderMap,
            String landlordNickname,
            String tenantNickname
    ) {
        RentalMessageDTO dto = new RentalMessageDTO();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversationId());
        dto.setRentalInfoId(message.getRentalInfoId());
        dto.setOrderId(message.getOrderId());
        dto.setSenderUserId(message.getSenderUserId());
        dto.setSenderNickname(resolveNickname(message.getSenderUserId(), nicknameMap));
        dto.setReceiverUserId(message.getReceiverUserId());
        dto.setReceiverNickname(resolveNickname(message.getReceiverUserId(), nicknameMap));
        dto.setMessageType(message.getMessageType());
        dto.setContent(message.getContent());
        dto.setMetadataJson(message.getMetadataJson());
        dto.setCreatedAt(message.getCreatedAt());
        if (message.getOrderId() != null && orderMap.containsKey(message.getOrderId())) {
            dto.setOrder(RentalOrderDTO.fromEntity(orderMap.get(message.getOrderId()), landlordNickname, tenantNickname));
        }
        return dto;
    }

    private static String resolveNickname(Long userId, Map<Long, String> nicknameMap) {
        if (userId == null) {
            return "系统";
        }
        return nicknameMap.getOrDefault(userId, "用户");
    }
}

package com.yxtech.smartlife.dto;

import com.yxtech.smartlife.entity.RentalOrder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RentalOrderDTO {

    private Long id;
    private Long conversationId;
    private Long rentalInfoId;
    private Long landlordUserId;
    private Long tenantUserId;
    private String landlordNickname;
    private String tenantNickname;
    private LocalDate startDate;
    private LocalDate endDate;
    private RentalOrder.OrderStatus status;
    private Long cancelRequestedBy;
    private String cancelReason;
    private LocalDateTime cancelRequestedAt;
    private Boolean landlordCancelConfirmed;
    private Boolean tenantCancelConfirmed;
    private Long renewalFromOrderId;
    private LocalDateTime reminderSentAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RentalOrderDTO fromEntity(RentalOrder order, String landlordNickname, String tenantNickname) {
        RentalOrderDTO dto = new RentalOrderDTO();
        dto.setId(order.getId());
        dto.setConversationId(order.getConversationId());
        dto.setRentalInfoId(order.getRentalInfoId());
        dto.setLandlordUserId(order.getLandlordUserId());
        dto.setTenantUserId(order.getTenantUserId());
        dto.setLandlordNickname(landlordNickname);
        dto.setTenantNickname(tenantNickname);
        dto.setStartDate(order.getStartDate());
        dto.setEndDate(order.getEndDate());
        dto.setStatus(order.getStatus());
        dto.setCancelRequestedBy(order.getCancelRequestedBy());
        dto.setCancelReason(order.getCancelReason());
        dto.setCancelRequestedAt(order.getCancelRequestedAt());
        dto.setLandlordCancelConfirmed(order.getLandlordCancelConfirmed());
        dto.setTenantCancelConfirmed(order.getTenantCancelConfirmed());
        dto.setRenewalFromOrderId(order.getRenewalFromOrderId());
        dto.setReminderSentAt(order.getReminderSentAt());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        return dto;
    }
}

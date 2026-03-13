package com.yxtech.smartlife.dto;

import com.yxtech.smartlife.entity.RentalOrder;
import com.yxtech.smartlife.service.model.RentalOrderAggregate;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AdminRentalOrderDTO {

    private Long id;
    private Long conversationId;
    private Long rentalInfoId;
    private String rentalTitle;
    private String rentalType;
    private String rentalStatus;
    private Long landlordUserId;
    private String landlordNickname;
    private Long tenantUserId;
    private String tenantNickname;
    private LocalDate startDate;
    private LocalDate endDate;
    private RentalOrder.OrderStatus status;
    private String cancelReason;
    private Long cancelRequestedBy;
    private LocalDateTime cancelRequestedAt;
    private Long renewalFromOrderId;
    private LocalDateTime reminderSentAt;
    private LocalDateTime createdAt;

    public static AdminRentalOrderDTO fromAggregate(RentalOrderAggregate aggregate) {
        AdminRentalOrderDTO dto = new AdminRentalOrderDTO();
        dto.setId(aggregate.order().getId());
        dto.setConversationId(aggregate.order().getConversationId());
        dto.setRentalInfoId(aggregate.order().getRentalInfoId());
        dto.setRentalTitle(aggregate.rentalInfo().getTitle());
        dto.setRentalType(aggregate.rentalInfo().getRentalType().name());
        dto.setRentalStatus(aggregate.rentalInfo().getStatus().name());
        dto.setLandlordUserId(aggregate.order().getLandlordUserId());
        dto.setLandlordNickname(aggregate.landlord().getNickname());
        dto.setTenantUserId(aggregate.order().getTenantUserId());
        dto.setTenantNickname(aggregate.tenant().getNickname());
        dto.setStartDate(aggregate.order().getStartDate());
        dto.setEndDate(aggregate.order().getEndDate());
        dto.setStatus(aggregate.order().getStatus());
        dto.setCancelReason(aggregate.order().getCancelReason());
        dto.setCancelRequestedBy(aggregate.order().getCancelRequestedBy());
        dto.setCancelRequestedAt(aggregate.order().getCancelRequestedAt());
        dto.setRenewalFromOrderId(aggregate.order().getRenewalFromOrderId());
        dto.setReminderSentAt(aggregate.order().getReminderSentAt());
        dto.setCreatedAt(aggregate.order().getCreatedAt());
        return dto;
    }
}

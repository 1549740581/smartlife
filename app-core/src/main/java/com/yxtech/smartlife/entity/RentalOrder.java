package com.yxtech.smartlife.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "rental_order")
public class RentalOrder extends BaseEntity {

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Column(name = "rental_info_id", nullable = false)
    private Long rentalInfoId;

    @Column(name = "landlord_user_id", nullable = false)
    private Long landlordUserId;

    @Column(name = "tenant_user_id", nullable = false)
    private Long tenantUserId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private OrderStatus status = OrderStatus.PENDING_CONFIRMATION;

    @Column(name = "cancel_requested_by")
    private Long cancelRequestedBy;

    @Column(name = "cancel_reason", length = 512)
    private String cancelReason;

    @Column(name = "cancel_requested_at")
    private LocalDateTime cancelRequestedAt;

    @Column(name = "landlord_cancel_confirmed", nullable = false)
    private Boolean landlordCancelConfirmed = false;

    @Column(name = "tenant_cancel_confirmed", nullable = false)
    private Boolean tenantCancelConfirmed = false;

    @Column(name = "renewal_from_order_id")
    private Long renewalFromOrderId;

    @Column(name = "reminder_sent_at")
    private LocalDateTime reminderSentAt;

    public enum OrderStatus {
        PENDING_CONFIRMATION,
        ACTIVE,
        CANCEL_PENDING,
        CANCELED,
        COMPLETED
    }
}

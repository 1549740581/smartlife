package com.yxtech.smartlife.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "rental_conversation")
public class RentalConversation extends BaseEntity {

    @Column(name = "rental_info_id", nullable = false)
    private Long rentalInfoId;

    @Column(name = "landlord_user_id", nullable = false)
    private Long landlordUserId;

    @Column(name = "tenant_user_id", nullable = false)
    private Long tenantUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ConversationStatus status = ConversationStatus.OPEN;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    public enum ConversationStatus {
        OPEN,
        CLOSED
    }
}

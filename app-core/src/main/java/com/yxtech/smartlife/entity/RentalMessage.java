package com.yxtech.smartlife.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rental_message")
public class RentalMessage extends BaseEntity {

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Column(name = "rental_info_id", nullable = false)
    private Long rentalInfoId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "sender_user_id")
    private Long senderUserId;

    @Column(name = "receiver_user_id")
    private Long receiverUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 32)
    private MessageType messageType;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Column(name = "metadata_json", length = 4000)
    private String metadataJson;

    @Column(name = "read_at")
    private java.time.LocalDateTime readAt;

    public enum MessageType {
        TEXT,
        ORDER_CARD,
        SYSTEM
    }
}

package com.yxtech.smartlife.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "rental_info")
public class RentalInfo extends BaseEntity {

    @Column(name = "publisher_user_id", nullable = false)
    private Long publisherUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rental_type", nullable = false, length = 32)
    private RentalType rentalType;

    @Column(name = "title", nullable = false, length = 128)
    private String title;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "contact_name", nullable = false, length = 64)
    private String contactName;

    @Column(name = "contact_phone", nullable = false, length = 32)
    private String contactPhone;

    @Column(name = "community_name", length = 128)
    private String communityName;

    @Column(name = "image_urls", length = 4000)
    private String imageUrls;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private RentalStatus status = RentalStatus.PENDING;

    @Column(name = "reject_reason", length = 512)
    private String rejectReason;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    public enum RentalType {
        HOUSE,
        PARKING,
        ITEM
    }

    public enum RentalStatus {
        PENDING,
        APPROVED,
        REJECTED,
        OFFLINE
    }
}

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
@Table(name = "complaint")
public class Complaint extends BaseEntity {

    @Column(name = "complainant_user_id", nullable = false)
    private Long complainantUserId;

    @Column(name = "rental_info_id", nullable = false)
    private Long rentalInfoId;

    @Column(name = "target_user_id", nullable = false)
    private Long targetUserId;

    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @Column(name = "evidence_urls", length = 4000)
    private String evidenceUrls;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ComplaintStatus status = ComplaintStatus.PENDING;

    @Column(name = "processed_by")
    private Long processedBy;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "process_remark", length = 512)
    private String processRemark;

    public enum ComplaintStatus {
        PENDING,
        ACCEPTED,
        REJECTED
    }
}

package com.yxtech.smartlife.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "review_record")
public class ReviewRecord extends BaseEntity {

    @Column(name = "rental_info_id", nullable = false)
    private Long rentalInfoId;

    @Column(name = "action", nullable = false, length = 32)
    private String action;

    @Column(name = "from_status", nullable = false, length = 32)
    private String fromStatus;

    @Column(name = "to_status", nullable = false, length = 32)
    private String toStatus;

    @Column(name = "reason", length = 512)
    private String reason;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;
}

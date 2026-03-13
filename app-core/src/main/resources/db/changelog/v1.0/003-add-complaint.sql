--liquibase formatted sql

--changeset smart-life:003-add-complaint-schema logicalFilePath:db/changelog/v1.0/003-add-complaint.sql
ALTER TABLE users ADD COLUMN warning_count INT NOT NULL DEFAULT 0 AFTER status;

CREATE TABLE IF NOT EXISTS complaint (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    complainant_user_id BIGINT NOT NULL COMMENT '投诉人用户ID',
    rental_info_id BIGINT NOT NULL COMMENT '被投诉的租赁信息ID',
    target_user_id BIGINT NOT NULL COMMENT '被投诉的房东用户ID',
    reason VARCHAR(1000) NOT NULL COMMENT '投诉理由',
    evidence_urls VARCHAR(4000) NULL COMMENT '证据文件URL（图片/视频），JSON数组格式',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '投诉状态：PENDING/ACCEPTED/REJECTED',
    processed_by BIGINT NULL COMMENT '处理人管理员ID',
    processed_at DATETIME NULL COMMENT '处理时间',
    process_remark VARCHAR(512) NULL COMMENT '处理备注',
    deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_complaint_complainant (complainant_user_id),
    INDEX idx_complaint_target (target_user_id),
    INDEX idx_complaint_rental (rental_info_id),
    INDEX idx_complaint_status (status)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

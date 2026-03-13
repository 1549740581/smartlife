--liquibase formatted sql

--changeset smart-life:003-add-rental-trade logicalFilePath:db/changelog/v1.0/001-add-rental-trade.sql
ALTER TABLE rental_info
    ADD COLUMN rent_start_date DATE NULL AFTER community_name,
    ADD COLUMN rent_end_date DATE NULL AFTER rent_start_date;

CREATE TABLE IF NOT EXISTS rental_conversation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rental_info_id BIGINT NOT NULL,
    landlord_user_id BIGINT NOT NULL,
    tenant_user_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    last_message_at DATETIME NULL,
    deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_rental_conversation UNIQUE (rental_info_id, landlord_user_id, tenant_user_id)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS rental_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    rental_info_id BIGINT NOT NULL,
    landlord_user_id BIGINT NOT NULL,
    tenant_user_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    cancel_requested_by BIGINT NULL,
    cancel_reason VARCHAR(512) NULL,
    cancel_requested_at DATETIME NULL,
    landlord_cancel_confirmed BIT NOT NULL DEFAULT 0,
    tenant_cancel_confirmed BIT NOT NULL DEFAULT 0,
    renewal_from_order_id BIGINT NULL,
    reminder_sent_at DATETIME NULL,
    deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS rental_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    rental_info_id BIGINT NOT NULL,
    order_id BIGINT NULL,
    sender_user_id BIGINT NULL,
    receiver_user_id BIGINT NULL,
    message_type VARCHAR(32) NOT NULL,
    content VARCHAR(1000) NOT NULL,
    metadata_json VARCHAR(4000) NULL,
    deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

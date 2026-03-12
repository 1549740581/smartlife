--liquibase formatted sql

--changeset smart-life:001-create-users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(255) NULL,
    email VARCHAR(128) NULL,
    phone VARCHAR(32) NULL,
    nickname VARCHAR(128) NULL,
    open_id VARCHAR(128) NULL,
    avatar_url VARCHAR(512) NULL,
    status VARCHAR(32) NOT NULL,
    deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_open_id UNIQUE (open_id)
);

--changeset smart-life:002-create-admins
CREATE TABLE IF NOT EXISTS admins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_admins_username UNIQUE (username)
);

--changeset smart-life:003-create-rental-info
CREATE TABLE IF NOT EXISTS rental_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    publisher_user_id BIGINT NOT NULL,
    rental_type VARCHAR(32) NOT NULL,
    title VARCHAR(128) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    contact_name VARCHAR(64) NOT NULL,
    contact_phone VARCHAR(32) NOT NULL,
    community_name VARCHAR(128) NULL,
    image_urls VARCHAR(4000) NULL,
    status VARCHAR(32) NOT NULL,
    reject_reason VARCHAR(512) NULL,
    reviewed_by BIGINT NULL,
    reviewed_at DATETIME NULL,
    deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

--changeset smart-life:004-create-review-record
CREATE TABLE IF NOT EXISTS review_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rental_info_id BIGINT NOT NULL,
    action VARCHAR(32) NOT NULL,
    from_status VARCHAR(32) NOT NULL,
    to_status VARCHAR(32) NOT NULL,
    reason VARCHAR(512) NULL,
    operator_id BIGINT NOT NULL,
    deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

--liquibase formatted sql
--changeset smartlife:005-add-user-favorite

CREATE TABLE user_favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    rental_info_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_rental (user_id, rental_info_id),
    INDEX idx_user_id (user_id),
    INDEX idx_rental_info_id (rental_info_id)
);

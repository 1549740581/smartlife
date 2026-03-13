--liquibase formatted sql
--changeset smartlife:004-add-message-read

ALTER TABLE rental_message ADD COLUMN read_at DATETIME NULL COMMENT '消息已读时间';

CREATE INDEX idx_rental_message_receiver_read ON rental_message(receiver_user_id, read_at);

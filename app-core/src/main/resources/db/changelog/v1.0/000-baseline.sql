--liquibase formatted sql

--changeset smart-life:001-baseline-schema logicalFilePath:db/changelog/v1.0/000-baseline.sql
ALTER DATABASE CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

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
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

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
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS rental_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    publisher_user_id BIGINT NOT NULL,
    rental_type VARCHAR(32) NOT NULL,
    title VARCHAR(128) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    contact_name VARCHAR(64) NOT NULL,
    contact_phone VARCHAR(32) NOT NULL,
    city VARCHAR(64) NOT NULL,
    district VARCHAR(64) NULL,
    street VARCHAR(128) NULL,
    community_name VARCHAR(128) NULL,
    image_urls VARCHAR(4000) NULL,
    status VARCHAR(32) NOT NULL,
    reject_reason VARCHAR(512) NULL,
    reviewed_by BIGINT NULL,
    reviewed_at DATETIME NULL,
    deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

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
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS address_option (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    city VARCHAR(64) NOT NULL,
    district VARCHAR(64) NOT NULL,
    street VARCHAR(128) NOT NULL,
    community_name VARCHAR(128) NOT NULL,
    deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_address_option UNIQUE (city, district, street, community_name)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci;

--changeset smart-life:002-baseline-seed logicalFilePath:db/changelog/v1.0/000-baseline.sql
INSERT INTO admins (id, username, password_hash, display_name, status, deleted)
SELECT
    1,
    'admin',
    '$2a$10$6oxQptAthanqUISEytEqkOzC/ykx1KG.8g923UVclm3wQRUBdMJry',
    '系统管理员',
    'ACTIVE',
    0
WHERE NOT EXISTS (
    SELECT 1 FROM admins WHERE id = 1 OR username = 'admin'
);

INSERT INTO users (id, username, password, email, phone, nickname, open_id, avatar_url, status, deleted)
SELECT
    10001,
    'reviewer_10001',
    NULL,
    'reviewer10001@smartlife.local',
    '13800001001',
    '审核员小杨',
    'wx-reviewer-10001',
    NULL,
    'ACTIVE',
    0
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE id = 10001 OR open_id = 'wx-reviewer-10001' OR username = 'reviewer_10001'
);

INSERT INTO users (id, username, password, email, phone, nickname, open_id, avatar_url, status, deleted)
SELECT
    10002,
    'reviewer_10002',
    NULL,
    'reviewer10002@smartlife.local',
    '13800001002',
    '审核员小李',
    'wx-reviewer-10002',
    NULL,
    'ACTIVE',
    0
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE id = 10002 OR open_id = 'wx-reviewer-10002' OR username = 'reviewer_10002'
);

INSERT INTO users (id, username, password, email, phone, nickname, open_id, avatar_url, status, deleted)
SELECT
    10003,
    'publisher_10003',
    NULL,
    'publisher10003@smartlife.local',
    '13800001003',
    '房东陈姐',
    'wx-user-10003',
    NULL,
    'ACTIVE',
    0
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE id = 10003 OR open_id = 'wx-user-10003' OR username = 'publisher_10003'
);

INSERT INTO users (id, username, password, email, phone, nickname, open_id, avatar_url, status, deleted)
SELECT
    10004,
    'publisher_10004',
    NULL,
    'publisher10004@smartlife.local',
    '13800001004',
    '车位王哥',
    'wx-user-10004',
    NULL,
    'ACTIVE',
    0
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE id = 10004 OR open_id = 'wx-user-10004' OR username = 'publisher_10004'
);

INSERT INTO address_option (id, city, district, street, community_name, deleted)
SELECT
    30001,
    '杭州' COLLATE utf8mb4_general_ci,
    '滨江区' COLLATE utf8mb4_general_ci,
    '长河街道' COLLATE utf8mb4_general_ci,
    '卓悦华庭' COLLATE utf8mb4_general_ci,
    0
WHERE NOT EXISTS (
    SELECT 1
    FROM address_option
    WHERE city = '杭州' COLLATE utf8mb4_general_ci
      AND district = '滨江区' COLLATE utf8mb4_general_ci
      AND street = '长河街道' COLLATE utf8mb4_general_ci
      AND community_name = '卓悦华庭' COLLATE utf8mb4_general_ci
      AND deleted = 0
);

INSERT INTO rental_info (
    id, publisher_user_id, rental_type, title, description, price,
    contact_name, contact_phone, city, district, street, community_name,
    image_urls, status, reject_reason, reviewed_by, reviewed_at, deleted, created_at, updated_at
)
SELECT
    20001,
    10003,
    'HOUSE',
    '地铁口精装两居室',
    '近地铁，拎包入住，适合情侣或小家庭。',
    4200.00,
    '房东陈姐',
    '13800001003',
    '杭州',
    '滨江区',
    '长河街道',
    '卓悦华庭',
    '["https://img.smartlife.local/demo-house-1.jpg"]',
    'APPROVED',
    NULL,
    1,
    NOW(),
    0,
    NOW(),
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM rental_info WHERE id = 20001);

INSERT INTO rental_info (
    id, publisher_user_id, rental_type, title, description, price,
    contact_name, contact_phone, city, district, street, community_name,
    image_urls, status, reject_reason, reviewed_by, reviewed_at, deleted, created_at, updated_at
)
SELECT
    20002,
    10004,
    'PARKING',
    '地库固定车位转租',
    '24小时可进出，靠近电梯厅，新能源车辆可停。',
    480.00,
    '车位王哥',
    '13800001004',
    '杭州',
    '滨江区',
    '长河街道',
    '卓悦华庭',
    '["https://img.smartlife.local/demo-parking-1.jpg"]',
    'APPROVED',
    NULL,
    1,
    NOW(),
    0,
    NOW(),
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM rental_info WHERE id = 20002);

INSERT INTO rental_info (
    id, publisher_user_id, rental_type, title, description, price,
    contact_name, contact_phone, city, district, street, community_name,
    image_urls, status, reject_reason, reviewed_by, reviewed_at, deleted, created_at, updated_at
)
SELECT
    20003,
    10003,
    'ITEM',
    '九成新人体工学椅',
    '家里闲置，功能正常，支持同城自提。',
    260.00,
    '房东陈姐',
    '13800001003',
    '杭州',
    '滨江区',
    '长河街道',
    '卓悦华庭',
    '["https://img.smartlife.local/demo-item-1.jpg"]',
    'APPROVED',
    NULL,
    1,
    NOW(),
    0,
    NOW(),
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM rental_info WHERE id = 20003);

INSERT INTO rental_info (
    id, publisher_user_id, rental_type, title, description, price,
    contact_name, contact_phone, city, district, street, community_name,
    image_urls, status, reject_reason, reviewed_by, reviewed_at, deleted, created_at, updated_at
)
SELECT
    20004,
    10001,
    'HOUSE',
    '南向次卧短租',
    '家具齐全，月付，已提交等待审核。',
    1800.00,
    '审核员小杨',
    '13800001001',
    '杭州',
    '滨江区',
    '长河街道',
    '卓悦华庭',
    '[]',
    'PENDING',
    NULL,
    NULL,
    NULL,
    0,
    NOW(),
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM rental_info WHERE id = 20004);

INSERT INTO rental_info (
    id, publisher_user_id, rental_type, title, description, price,
    contact_name, contact_phone, city, district, street, community_name,
    image_urls, status, reject_reason, reviewed_by, reviewed_at, deleted, created_at, updated_at
)
SELECT
    20005,
    10002,
    'ITEM',
    '闲置婴儿推车',
    '图片较少，等审核员补充。',
    120.00,
    '审核员小李',
    '13800001002',
    '杭州',
    '滨江区',
    '长河街道',
    '卓悦华庭',
    '[]',
    'PENDING',
    NULL,
    NULL,
    NULL,
    0,
    NOW(),
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM rental_info WHERE id = 20005);

INSERT INTO rental_info (
    id, publisher_user_id, rental_type, title, description, price,
    contact_name, contact_phone, city, district, street, community_name,
    image_urls, status, reject_reason, reviewed_by, reviewed_at, deleted, created_at, updated_at
)
SELECT
    20006,
    10003,
    'PARKING',
    '临街车位日租',
    '已被驳回，原因示例用于个人中心展示。',
    35.00,
    '房东陈姐',
    '13800001003',
    '杭州',
    '滨江区',
    '长河街道',
    '卓悦华庭',
    '[]',
    'REJECTED',
    '描述不够完整，请补充停车时段和位置照片。',
    1,
    NOW(),
    0,
    NOW(),
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM rental_info WHERE id = 20006);

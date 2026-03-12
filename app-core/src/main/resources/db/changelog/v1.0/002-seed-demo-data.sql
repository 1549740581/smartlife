--liquibase formatted sql

--changeset smart-life:005-seed-demo-users
INSERT INTO users (id, username, password, email, phone, nickname, open_id, avatar_url, status, deleted)
SELECT 10001, 'reviewer_10001', NULL, 'reviewer10001@smartlife.local', '13800001001', '审核员小杨', 'wx-reviewer-10001', NULL, 'ACTIVE', 0
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE id = 10001 OR open_id = 'wx-reviewer-10001' OR username = 'reviewer_10001'
);

INSERT INTO users (id, username, password, email, phone, nickname, open_id, avatar_url, status, deleted)
SELECT 10002, 'reviewer_10002', NULL, 'reviewer10002@smartlife.local', '13800001002', '审核员小李', 'wx-reviewer-10002', NULL, 'ACTIVE', 0
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE id = 10002 OR open_id = 'wx-reviewer-10002' OR username = 'reviewer_10002'
);

INSERT INTO users (id, username, password, email, phone, nickname, open_id, avatar_url, status, deleted)
SELECT 10003, 'publisher_10003', NULL, 'publisher10003@smartlife.local', '13800001003', '房东陈姐', 'wx-user-10003', NULL, 'ACTIVE', 0
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE id = 10003 OR open_id = 'wx-user-10003' OR username = 'publisher_10003'
);

INSERT INTO users (id, username, password, email, phone, nickname, open_id, avatar_url, status, deleted)
SELECT 10004, 'publisher_10004', NULL, 'publisher10004@smartlife.local', '13800001004', '车位王哥', 'wx-user-10004', NULL, 'ACTIVE', 0
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE id = 10004 OR open_id = 'wx-user-10004' OR username = 'publisher_10004'
);

--changeset smart-life:006-seed-demo-rentals
INSERT INTO rental_info (
    id, publisher_user_id, rental_type, title, description, price,
    contact_name, contact_phone, community_name, image_urls, status,
    reject_reason, reviewed_by, reviewed_at, deleted, created_at, updated_at
)
SELECT
    20001, 10003, 'HOUSE', '地铁口精装两居室',
    '近地铁，拎包入住，适合情侣或小家庭。', 4200.00,
    '房东陈姐', '13800001003', '阳光花园',
    '["https://img.smartlife.local/demo-house-1.jpg"]', 'APPROVED',
    NULL, 1, NOW(), 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM rental_info WHERE id = 20001);

INSERT INTO rental_info (
    id, publisher_user_id, rental_type, title, description, price,
    contact_name, contact_phone, community_name, image_urls, status,
    reject_reason, reviewed_by, reviewed_at, deleted, created_at, updated_at
)
SELECT
    20002, 10004, 'PARKING', '地库固定车位转租',
    '24小时可进出，靠近电梯厅，新能源车辆可停。', 480.00,
    '车位王哥', '13800001004', '未来城',
    '["https://img.smartlife.local/demo-parking-1.jpg"]', 'APPROVED',
    NULL, 1, NOW(), 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM rental_info WHERE id = 20002);

INSERT INTO rental_info (
    id, publisher_user_id, rental_type, title, description, price,
    contact_name, contact_phone, community_name, image_urls, status,
    reject_reason, reviewed_by, reviewed_at, deleted, created_at, updated_at
)
SELECT
    20003, 10003, 'ITEM', '九成新人体工学椅',
    '家里闲置，功能正常，支持同城自提。', 260.00,
    '房东陈姐', '13800001003', '阳光花园',
    '["https://img.smartlife.local/demo-item-1.jpg"]', 'APPROVED',
    NULL, 1, NOW(), 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM rental_info WHERE id = 20003);

INSERT INTO rental_info (
    id, publisher_user_id, rental_type, title, description, price,
    contact_name, contact_phone, community_name, image_urls, status,
    reject_reason, reviewed_by, reviewed_at, deleted, created_at, updated_at
)
SELECT
    20004, 10001, 'HOUSE', '南向次卧短租',
    '家具齐全，月付，已提交等待审核。', 1800.00,
    '审核员小杨', '13800001001', '书香苑',
    '[]', 'PENDING',
    NULL, NULL, NULL, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM rental_info WHERE id = 20004);

INSERT INTO rental_info (
    id, publisher_user_id, rental_type, title, description, price,
    contact_name, contact_phone, community_name, image_urls, status,
    reject_reason, reviewed_by, reviewed_at, deleted, created_at, updated_at
)
SELECT
    20005, 10002, 'ITEM', '闲置婴儿推车',
    '图片较少，等审核员补充。', 120.00,
    '审核员小李', '13800001002', '云栖里',
    '[]', 'PENDING',
    NULL, NULL, NULL, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM rental_info WHERE id = 20005);

INSERT INTO rental_info (
    id, publisher_user_id, rental_type, title, description, price,
    contact_name, contact_phone, community_name, image_urls, status,
    reject_reason, reviewed_by, reviewed_at, deleted, created_at, updated_at
)
SELECT
    20006, 10003, 'PARKING', '临街车位日租',
    '已被驳回，原因示例用于个人中心展示。', 35.00,
    '房东陈姐', '13800001003', '阳光花园',
    '[]', 'REJECTED',
    '描述不够完整，请补充停车时段和位置照片。', 1, NOW(), 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM rental_info WHERE id = 20006);

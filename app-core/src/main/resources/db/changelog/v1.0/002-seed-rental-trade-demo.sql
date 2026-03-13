--liquibase formatted sql

--changeset smart-life:004-seed-rental-trade-demo logicalFilePath:db/changelog/v1.0/002-seed-rental-trade-demo.sql
UPDATE rental_info
SET status = 'APPROVED',
    rent_start_date = NULL,
    rent_end_date = NULL,
    updated_at = NOW()
WHERE id = 20001
  AND deleted = 0;

UPDATE rental_info
SET status = 'RENTED',
    rent_start_date = '2026-03-01',
    rent_end_date = '2026-03-27',
    updated_at = NOW()
WHERE id = 20002
  AND deleted = 0;

INSERT INTO rental_conversation (
    id, rental_info_id, landlord_user_id, tenant_user_id, status, last_message_at, deleted, created_at, updated_at
)
SELECT
    40001,
    20001,
    10003,
    10004,
    'OPEN',
    '2026-03-10 09:10:00',
    0,
    '2026-03-10 08:30:00',
    '2026-03-10 09:10:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM rental_conversation
    WHERE rental_info_id = 20001
      AND landlord_user_id = 10003
      AND tenant_user_id = 10004
      AND deleted = 0
);

INSERT INTO rental_order (
    id, conversation_id, rental_info_id, landlord_user_id, tenant_user_id,
    start_date, end_date, status, cancel_requested_by, cancel_reason, cancel_requested_at,
    landlord_cancel_confirmed, tenant_cancel_confirmed, renewal_from_order_id, reminder_sent_at,
    deleted, created_at, updated_at
)
SELECT
    50001,
    c.id,
    20001,
    10003,
    10004,
    '2026-04-01',
    '2026-06-30',
    'PENDING_CONFIRMATION',
    NULL,
    NULL,
    NULL,
    0,
    0,
    NULL,
    NULL,
    0,
    '2026-03-10 09:10:00',
    '2026-03-10 09:10:00'
FROM rental_conversation c
WHERE c.rental_info_id = 20001
  AND c.landlord_user_id = 10003
  AND c.tenant_user_id = 10004
  AND c.deleted = 0
  AND NOT EXISTS (
      SELECT 1
      FROM rental_order
      WHERE rental_info_id = 20001
        AND landlord_user_id = 10003
        AND tenant_user_id = 10004
        AND start_date = '2026-04-01'
        AND end_date = '2026-06-30'
        AND deleted = 0
  );

INSERT INTO rental_message (
    id, conversation_id, rental_info_id, order_id, sender_user_id, receiver_user_id,
    message_type, content, metadata_json, deleted, created_at, updated_at
)
SELECT
    60001,
    c.id,
    20001,
    NULL,
    10004,
    10003,
    'TEXT',
    '你好，我想了解下这套房子的采光和停车情况。',
    NULL,
    0,
    '2026-03-10 08:31:00',
    '2026-03-10 08:31:00'
FROM rental_conversation c
WHERE c.rental_info_id = 20001
  AND c.landlord_user_id = 10003
  AND c.tenant_user_id = 10004
  AND c.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM rental_message WHERE id = 60001);

INSERT INTO rental_message (
    id, conversation_id, rental_info_id, order_id, sender_user_id, receiver_user_id,
    message_type, content, metadata_json, deleted, created_at, updated_at
)
SELECT
    60002,
    c.id,
    20001,
    NULL,
    10003,
    10004,
    'TEXT',
    '房子朝南，楼下可以临时停车，你可以先发一个租期给我。',
    NULL,
    0,
    '2026-03-10 08:42:00',
    '2026-03-10 08:42:00'
FROM rental_conversation c
WHERE c.rental_info_id = 20001
  AND c.landlord_user_id = 10003
  AND c.tenant_user_id = 10004
  AND c.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM rental_message WHERE id = 60002);

INSERT INTO rental_message (
    id, conversation_id, rental_info_id, order_id, sender_user_id, receiver_user_id,
    message_type, content, metadata_json, deleted, created_at, updated_at
)
SELECT
    60003,
    c.id,
    20001,
    o.id,
    10004,
    10003,
    'ORDER_CARD',
    '租期申请：2026-04-01 至 2026-06-30',
    '{"startDate":"2026-04-01","endDate":"2026-06-30","status":"PENDING_CONFIRMATION","renewalFromOrderId":null}',
    0,
    '2026-03-10 09:10:00',
    '2026-03-10 09:10:00'
FROM rental_conversation c
JOIN rental_order o
  ON o.conversation_id = c.id
 AND o.rental_info_id = 20001
 AND o.landlord_user_id = 10003
 AND o.tenant_user_id = 10004
 AND o.start_date = '2026-04-01'
 AND o.end_date = '2026-06-30'
 AND o.deleted = 0
WHERE c.rental_info_id = 20001
  AND c.landlord_user_id = 10003
  AND c.tenant_user_id = 10004
  AND c.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM rental_message WHERE id = 60003);

INSERT INTO rental_conversation (
    id, rental_info_id, landlord_user_id, tenant_user_id, status, last_message_at, deleted, created_at, updated_at
)
SELECT
    40002,
    20002,
    10004,
    10003,
    'OPEN',
    '2026-03-12 09:00:00',
    0,
    '2026-02-27 19:30:00',
    '2026-03-12 09:00:00'
WHERE NOT EXISTS (
    SELECT 1
    FROM rental_conversation
    WHERE rental_info_id = 20002
      AND landlord_user_id = 10004
      AND tenant_user_id = 10003
      AND deleted = 0
);

INSERT INTO rental_order (
    id, conversation_id, rental_info_id, landlord_user_id, tenant_user_id,
    start_date, end_date, status, cancel_requested_by, cancel_reason, cancel_requested_at,
    landlord_cancel_confirmed, tenant_cancel_confirmed, renewal_from_order_id, reminder_sent_at,
    deleted, created_at, updated_at
)
SELECT
    50002,
    c.id,
    20002,
    10004,
    10003,
    '2026-03-01',
    '2026-03-27',
    'ACTIVE',
    NULL,
    NULL,
    NULL,
    0,
    0,
    NULL,
    '2026-03-12 09:00:00',
    0,
    '2026-02-27 20:10:00',
    '2026-03-12 09:00:00'
FROM rental_conversation c
WHERE c.rental_info_id = 20002
  AND c.landlord_user_id = 10004
  AND c.tenant_user_id = 10003
  AND c.deleted = 0
  AND NOT EXISTS (
      SELECT 1
      FROM rental_order
      WHERE rental_info_id = 20002
        AND landlord_user_id = 10004
        AND tenant_user_id = 10003
        AND start_date = '2026-03-01'
        AND end_date = '2026-03-27'
        AND deleted = 0
  );

INSERT INTO rental_message (
    id, conversation_id, rental_info_id, order_id, sender_user_id, receiver_user_id,
    message_type, content, metadata_json, deleted, created_at, updated_at
)
SELECT
    60004,
    c.id,
    20002,
    NULL,
    10003,
    10004,
    'TEXT',
    '车位晚上和周末都能正常进出吗？我想整月租。',
    NULL,
    0,
    '2026-02-27 19:31:00',
    '2026-02-27 19:31:00'
FROM rental_conversation c
WHERE c.rental_info_id = 20002
  AND c.landlord_user_id = 10004
  AND c.tenant_user_id = 10003
  AND c.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM rental_message WHERE id = 60004);

INSERT INTO rental_message (
    id, conversation_id, rental_info_id, order_id, sender_user_id, receiver_user_id,
    message_type, content, metadata_json, deleted, created_at, updated_at
)
SELECT
    60005,
    c.id,
    20002,
    o.id,
    10003,
    10004,
    'ORDER_CARD',
    '租期申请：2026-03-01 至 2026-03-27',
    '{"startDate":"2026-03-01","endDate":"2026-03-27","status":"PENDING_CONFIRMATION","renewalFromOrderId":null}',
    0,
    '2026-02-27 20:10:00',
    '2026-02-27 20:10:00'
FROM rental_conversation c
JOIN rental_order o
  ON o.conversation_id = c.id
 AND o.rental_info_id = 20002
 AND o.landlord_user_id = 10004
 AND o.tenant_user_id = 10003
 AND o.start_date = '2026-03-01'
 AND o.end_date = '2026-03-27'
 AND o.deleted = 0
WHERE c.rental_info_id = 20002
  AND c.landlord_user_id = 10004
  AND c.tenant_user_id = 10003
  AND c.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM rental_message WHERE id = 60005);

INSERT INTO rental_message (
    id, conversation_id, rental_info_id, order_id, sender_user_id, receiver_user_id,
    message_type, content, metadata_json, deleted, created_at, updated_at
)
SELECT
    60006,
    c.id,
    20002,
    o.id,
    NULL,
    NULL,
    'SYSTEM',
    '房东已确认租期，订单生效。',
    NULL,
    0,
    '2026-02-27 20:20:00',
    '2026-02-27 20:20:00'
FROM rental_conversation c
JOIN rental_order o
  ON o.conversation_id = c.id
 AND o.rental_info_id = 20002
 AND o.landlord_user_id = 10004
 AND o.tenant_user_id = 10003
 AND o.start_date = '2026-03-01'
 AND o.end_date = '2026-03-27'
 AND o.deleted = 0
WHERE c.rental_info_id = 20002
  AND c.landlord_user_id = 10004
  AND c.tenant_user_id = 10003
  AND c.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM rental_message WHERE id = 60006);

INSERT INTO rental_message (
    id, conversation_id, rental_info_id, order_id, sender_user_id, receiver_user_id,
    message_type, content, metadata_json, deleted, created_at, updated_at
)
SELECT
    60007,
    c.id,
    20002,
    o.id,
    NULL,
    NULL,
    'SYSTEM',
    '当前租期将在 15 天后到期，请双方及时确认是否续约。',
    NULL,
    0,
    '2026-03-12 09:00:00',
    '2026-03-12 09:00:00'
FROM rental_conversation c
JOIN rental_order o
  ON o.conversation_id = c.id
 AND o.rental_info_id = 20002
 AND o.landlord_user_id = 10004
 AND o.tenant_user_id = 10003
 AND o.start_date = '2026-03-01'
 AND o.end_date = '2026-03-27'
 AND o.deleted = 0
WHERE c.rental_info_id = 20002
  AND c.landlord_user_id = 10004
  AND c.tenant_user_id = 10003
  AND c.deleted = 0
  AND NOT EXISTS (SELECT 1 FROM rental_message WHERE id = 60007);

UPDATE rental_conversation c
SET c.last_message_at = (
    SELECT MAX(m.created_at)
    FROM rental_message m
    WHERE m.conversation_id = c.id
      AND m.deleted = 0
)
WHERE c.deleted = 0
  AND (
      (c.rental_info_id = 20001 AND c.landlord_user_id = 10003 AND c.tenant_user_id = 10004)
      OR (c.rental_info_id = 20002 AND c.landlord_user_id = 10004 AND c.tenant_user_id = 10003)
  );

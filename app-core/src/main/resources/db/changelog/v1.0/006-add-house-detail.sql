--liquibase formatted sql

--changeset smart-life:006-add-house-detail logicalFilePath:db/changelog/v1.0/006-add-house-detail.sql
CREATE TABLE IF NOT EXISTS house_detail (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    rental_info_id BIGINT UNSIGNED NOT NULL COMMENT '关联租赁信息ID',
    floor INT NOT NULL COMMENT '楼层(0~40)',
    bedroom_count INT NOT NULL COMMENT '卧室数量',
    living_room_count INT NOT NULL COMMENT '客厅数量',
    kitchen_count INT NOT NULL COMMENT '厨房数量',
    bathroom_count INT NOT NULL COMMENT '卫生间数量',
    orientation VARCHAR(16) NOT NULL COMMENT '朝向(EAST/SOUTH/WEST/NORTH/SOUTHEAST/SOUTHWEST/NORTHEAST/NORTHWEST)',
    has_balcony TINYINT(1) NOT NULL COMMENT '是否有阳台(0-无,1-有)',
    appliances JSON NOT NULL COMMENT '家电家具(JSON数组)',
    has_elevator TINYINT(1) NOT NULL COMMENT '是否有电梯(0-无,1-有)',
    property_fee DECIMAL(10,2) NOT NULL COMMENT '物业费(元/月)',
    water_fee DECIMAL(10,2) NOT NULL COMMENT '水费(元/吨)',
    electricity_fee DECIMAL(10,2) NOT NULL COMMENT '电费(元/度)',
    extra_info TEXT NULL COMMENT '其他信息',
    version BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标识(0-未删除,1-已删除)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT uk_house_detail_rental_info_id UNIQUE (rental_info_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC COMMENT = '房屋详情表';

CREATE INDEX idx_house_detail_rental_info_id ON house_detail(rental_info_id);

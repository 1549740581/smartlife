# 数据模型

本文件描述当前版本已落地的数据对象，用于指导实体设计、数据库建模和接口字段命名。

## 1. User

用途：存储普通住户用户信息。

当前字段：

- `id`
- `username`
- `password`
- `email`
- `phone`
- `nickname`
- `openId`
- `avatarUrl`
- `status`
- `deleted`
- `createdAt`
- `updatedAt`

约束：

- `openId` 唯一
- `phone` 可为空，但若填写需满足格式校验

## 2. Admin

用途：存储管理端账户信息。

当前字段：

- `id`
- `username`
- `passwordHash`
- `displayName`
- `status`
- `deleted`
- `createdAt`
- `updatedAt`

约束：

- `username` 唯一
- 不存储明文密码

## 3. RentalInfo

用途：统一承载房屋、车位和闲置物品信息。

当前字段：

- `id`
- `publisherUserId`
- `rentalType`
- `title`
- `description`
- `price`
- `contactName`
- `contactPhone`
- `city`
- `district`
- `street`
- `communityName`
- `rentStartDate`
- `rentEndDate`
- `imageUrls`
- `status`
- `rejectReason`
- `reviewedBy`
- `reviewedAt`
- `deleted`
- `createdAt`
- `updatedAt`

约束：

- `rentalType` 仅允许 `HOUSE`、`PARKING`、`ITEM`
- `status` 仅允许 `PENDING`、`APPROVED`、`REJECTED`、`OFFLINE`、`RENTED`
- `rejectReason` 仅在拒绝时必填
- `city` 当前固定为 `杭州`
- 发布时 `city + district + street + communityName` 必须命中 `address_option`

## 4. ReviewRecord

用途：保留审核动作历史，避免仅依赖 `RentalInfo` 当前状态字段。

当前字段：

- `id`
- `rentalInfoId`
- `action`
- `fromStatus`
- `toStatus`
- `reason`
- `operatorId`
- `deleted`
- `createdAt`
- `updatedAt`

## 5. AddressOption

用途：维护小程序发布和搜索使用的可选地址。

当前字段：

- `id`
- `city`
- `district`
- `street`
- `communityName`
- `deleted`
- `createdAt`
- `updatedAt`

约束：

- 唯一键：`city + district + street + communityName`
- 当前以扁平叶子表存储，接口层组装为地址树
- 当前默认仅初始化 `杭州 / 滨江区 / 长河街道 / 卓悦华庭`

## 6. RentalConversation

用途：承载“房源/车位 + 房东 + 租客”维度的唯一沟通会话。

当前字段：

- `id`
- `rentalInfoId`
- `landlordUserId`
- `tenantUserId`
- `status`
- `lastMessageAt`
- `deleted`
- `createdAt`
- `updatedAt`

约束：

- 唯一键：`rentalInfoId + landlordUserId + tenantUserId`
- 当前状态枚举：`OPEN`
- 会话列表按 `lastMessageAt` 倒序展示

## 7. RentalOrder

用途：承载租客和房东确认的租期订单。

当前字段：

- `id`
- `conversationId`
- `rentalInfoId`
- `landlordUserId`
- `tenantUserId`
- `startDate`
- `endDate`
- `status`
- `cancelRequestedBy`
- `cancelReason`
- `cancelRequestedAt`
- `landlordCancelConfirmed`
- `tenantCancelConfirmed`
- `renewalFromOrderId`
- `reminderSentAt`
- `deleted`
- `createdAt`
- `updatedAt`

约束：

- 同一 `rentalInfoId` 在交叉时间段内只能有一个未失效订单
- 冲突校验针对 `PENDING_CONFIRMATION`、`ACTIVE`、`CANCEL_PENDING`
- 续约通过新订单表达，不覆盖历史订单

## 8. RentalMessage

用途：承载站内沟通消息和系统提醒。

当前字段：

- `id`
- `conversationId`
- `rentalInfoId`
- `orderId`
- `senderUserId`
- `receiverUserId`
- `messageType`
- `content`
- `metadataJson`
- `deleted`
- `createdAt`
- `updatedAt`

约束：

- `messageType` 当前支持 `TEXT`、`ORDER_CARD`、`SYSTEM`
- `ORDER_CARD` 消息必须关联订单
- `SYSTEM` 消息由服务端写入，用于确认、取消和到期提醒

## 9. 状态流转

允许的租赁状态流转：

- `PENDING -> APPROVED`
- `PENDING -> REJECTED`
- `APPROVED -> OFFLINE`
- `APPROVED -> RENTED`
- `RENTED -> APPROVED`

允许的订单状态流转：

- `PENDING_CONFIRMATION -> ACTIVE`
- `PENDING_CONFIRMATION -> CANCELED`
- `ACTIVE -> CANCEL_PENDING`
- `CANCEL_PENDING -> CANCELED`
- `ACTIVE -> COMPLETED`

## 10. 建模注意事项

- 图片字段若暂不接对象存储，可先保存为 JSON 数组字符串
- 所有实体建议继承统一基类以复用主键、时间字段和逻辑删除字段
- 枚举值要同时在数据库、后端代码和接口文档中保持一致

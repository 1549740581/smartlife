# 数据模型

本文件描述首期版本建议的数据对象，用于指导实体设计、数据库建模和接口字段命名。

## 1. User

用途：存储普通住户用户信息。

建议字段：

- `id`
- `openid`
- `unionId`
- `nickname`
- `avatarUrl`
- `phone`
- `status`
- `createdAt`
- `updatedAt`

约束：

- `openid` 唯一
- `phone` 可为空，但若填写需满足格式校验

## 2. Admin

用途：存储管理端账户信息。

建议字段：

- `id`
- `username`
- `passwordHash`
- `displayName`
- `status`
- `lastLoginAt`
- `createdAt`
- `updatedAt`

约束：

- `username` 唯一
- 不存储明文密码

## 3. RentalInfo

用途：统一承载房屋和车位出租信息。

建议字段：

- `id`
- `publisherUserId`
- `rentalType`
- `title`
- `description`
- `price`
- `contactName`
- `contactPhone`
- `images`
- `status`
- `rejectReason`
- `reviewedBy`
- `reviewedAt`
- `createdAt`
- `updatedAt`

可选扩展字段：

- `communityName`
- `buildingNo`
- `roomNo`
- `parkingNo`
- `area`
- `tags`

约束：

- `rentalType` 仅允许 `HOUSE` 或 `PARKING`
- `status` 仅允许 `PENDING`、`APPROVED`、`REJECTED`、`OFFLINE`
- `rejectReason` 仅在拒绝时必填

## 4. ReviewRecord

用途：保留审核动作历史，避免仅依赖 `RentalInfo` 当前状态字段。

建议字段：

- `id`
- `rentalInfoId`
- `action`
- `fromStatus`
- `toStatus`
- `reason`
- `operatorId`
- `createdAt`

## 5. 状态流转

允许的租赁状态流转：

- `PENDING -> APPROVED`
- `PENDING -> REJECTED`
- `APPROVED -> OFFLINE`
- `REJECTED -> PENDING`（用户重新提交后可考虑支持）

## 6. 建模注意事项

- 图片字段若暂不接对象存储，可先保存为 JSON 数组字符串
- 所有实体建议继承统一基类以复用主键、时间字段和逻辑删除字段
- 枚举值要同时在数据库、后端代码和接口文档中保持一致

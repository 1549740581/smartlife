# API 规范草案

本文件描述当前代码已实现并用于小程序联调的接口集合，字段示例以当前 MVP 为准。

## 1. 通用规则

### Base Path

- 用户侧接口：`/api`
- 管理侧接口：`/api/admin`

### 响应结构

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1710000000000
}
```

### 错误结构

```json
{
  "code": 400,
  "message": "title must not be blank",
  "data": null
}
```

## 2. 用户侧接口

### 登录

- `POST /api/wechat/login`

请求示例：

```json
{
  "code": "wx-user-10003",
  "nickname": "房东陈姐"
}
```

说明：

- 小程序前端默认允许访客不登录直接浏览公开列表
- 当前登录为模拟微信登录，`code` 直接作为 `openId`
- 开发环境下允许输入任意 `code` 完成登录联调
- 若 `code` 首次出现，后端会自动创建对应用户

### 发布租赁信息

- `POST /api/rentals`

请求示例（房屋类型）：

```json
{
  "publisherUserId": 10003,
  "rentalType": "HOUSE",
  "title": "两居室出租",
  "description": "近地铁，精装修",
  "price": 4200,
  "contactName": "张三",
  "contactPhone": "13800000000",
  "city": "杭州",
  "district": "滨江区",
  "street": "长河街道",
  "communityName": "卓悦华庭",
  "imageUrls": [
    "https://example.com/image-1.jpg"
  ],
  "houseDetail": {
    "floor": 12,
    "bedroomCount": 2,
    "livingRoomCount": 1,
    "kitchenCount": 1,
    "bathroomCount": 1,
    "orientation": "SOUTH",
    "hasBalcony": true,
    "appliances": ["REFRIGERATOR", "TV", "AIR_CONDITIONER"],
    "hasElevator": true,
    "propertyFee": 2.5,
    "waterFee": 5.0,
    "electricityFee": 0.6,
    "extraInfo": "可养小型宠物"
  }
}
```

说明：

- `rentalType` 当前支持 `HOUSE`、`PARKING`、`ITEM`
- 发布成功后状态默认为 `PENDING`
- 小程序发布页会在调用接口前进行标题、描述、价格、联系人、联系电话的前端校验
- 发布地址必须命中地址表中存在的地址组合
- 当前 `city` 仅支持 `杭州`
- `houseDetail` 仅在 `rentalType=HOUSE` 时必填，车位和闲置物品不需要

房屋详情字段说明：

- `floor`：楼层，0~40，必填
- `bedroomCount`：几室，≥0，必填
- `livingRoomCount`：几厅，≥0，必填
- `kitchenCount`：几厨，≥0，必填
- `bathroomCount`：几卫，≥0，必填
- `orientation`：朝向，枚举值 `EAST`/`SOUTH`/`WEST`/`NORTH`/`SOUTHEAST`/`SOUTHWEST`/`NORTHEAST`/`NORTHWEST`，必填
- `hasBalcony`：有无阳台，必填
- `appliances`：家电家具，数组，可选值 `REFRIGERATOR`/`TV`/`AIR_CONDITIONER`/`WASHING_MACHINE`/`WARDROBE`/`NONE`，选 `NONE` 时不可选其他，必填
- `hasElevator`：有无电梯，必填
- `propertyFee`：物业费（元/月），≥0，必填
- `waterFee`：水费（元/吨），必填
- `electricityFee`：电费（元/度），必填
- `extraInfo`：其他信息，非必填

### 地址树查询

- `GET /api/addresses/tree`

说明：

- 地址数据来自 MySQL `address_option` 表
- 当前仅初始化一条杭州地址

### 上传图片

- `POST /api/files/images`
- `Content-Type: multipart/form-data`
- 表单字段：`file`

### 查询公开列表

- `GET /api/rentals`
- `GET /api/rentals/type/{type}`
- `GET /api/rentals/{id}`

说明：

- 仅返回 `APPROVED` 数据
- 访客与登录用户查询结果一致
- `type` 允许值为 `HOUSE`、`PARKING`、`ITEM`
- 支持查询参数：`keyword`、`type`、`city`、`district`、`street`、`communityName`

### 查询我的发布

- `GET /api/rentals/user/{userId}`
- `GET /api/rentals/user/{userId}/{id}`

说明：

- 返回用户自己的全部状态数据，包括 `PENDING`、`APPROVED`、`REJECTED`、`OFFLINE`、`RENTED`

### 打开租赁沟通

- `POST /api/rentals/{rentalId}/conversation`

请求示例：

```json
{
  "userId": 10004
}
```

说明：

- 仅房屋和车位支持发起沟通
- 房东不能给自己的信息发起沟通
- 仅 `APPROVED` 信息可新开沟通；已出租信息仅允许已存在会话继续进入

### 查询我的沟通列表

- `GET /api/rental-conversations?userId=10004`

返回要点：

- 返回当前用户作为房东或租客参与的全部会话
- 包含 `latestOrder`、`lastMessagePreview`、`rentStartDate`、`rentEndDate`
- 小程序“个人中心 -> 租赁沟通”使用该接口

### 查询沟通详情

- `GET /api/rental-conversations/{conversationId}?userId=10004`

返回要点：

- 返回会话基本信息、当前用户角色、对应房源信息
- `messages` 同时承载文本消息、租期卡片消息和系统提醒
- `orders` 按创建时间倒序返回

### 发送文字消息

- `POST /api/rental-conversations/{conversationId}/messages`

请求示例：

```json
{
  "userId": 10004,
  "content": "我想先租三个月，可以吗？"
}
```

### 发送租期卡片

- `POST /api/rental-conversations/{conversationId}/orders`

请求示例：

```json
{
  "userId": 10004,
  "startDate": "2026-04-01",
  "endDate": "2026-06-30"
}
```

说明：

- 仅租客可以创建租期卡片
- 仅房屋和车位支持下单
- 交叉时间段内不能重复创建 `PENDING_CONFIRMATION` / `ACTIVE` / `CANCEL_PENDING` 订单
- 创建后订单状态为 `PENDING_CONFIRMATION`

### 房东确认订单

- `POST /api/rental-orders/{id}/accept`

请求示例：

```json
{
  "userId": 10003
}
```

说明：

- 仅房东可确认
- 确认后订单变为 `ACTIVE`
- 对应房源或车位状态切换为 `RENTED`

### 发起取消申请

- `POST /api/rental-orders/{id}/cancel/request`

请求示例：

```json
{
  "userId": 10004,
  "reason": "计划有变，想提前结束租期"
}
```

说明：

- `PENDING_CONFIRMATION` 订单取消后直接变为 `CANCELED`
- `ACTIVE` 订单取消后先进入 `CANCEL_PENDING`
- `reason` 当前为可选字段，前端可透传给对方和管理员查看

### 确认取消申请

- `POST /api/rental-orders/{id}/cancel/confirm`

请求示例：

```json
{
  "userId": 10003
}
```

说明：

- 双方都确认后，订单变为 `CANCELED`
- 对应房源恢复为 `APPROVED`

### 续约

- `POST /api/rental-orders/{id}/renew`

请求示例：

```json
{
  "userId": 10004,
  "startDate": "2026-03-28",
  "endDate": "2026-05-31"
}
```

说明：

- 仅 `ACTIVE` 或 `COMPLETED` 订单支持续约
- 续约起始日期必须晚于原订单结束日期
- 续约通过新订单表达，不覆盖原订单

## 3. 管理侧接口

### 管理员登录

- `POST /api/admin/login`

请求示例：

```json
{
  "username": "admin",
  "password": "admin123"
}
```

说明：

- 小程序 UI 层只对指定审核员用户展示“后台审核”入口
- 后端管理接口以 `X-Admin-Token` 作为鉴权依据
- 会话保存在 Redis 中，服务重启后未过期 token 仍可用

### 管理员退出

- `POST /api/admin/logout`

### 获取待审核列表

- `GET /api/admin/rentals/pending`

### 审核租赁信息

- `POST /api/admin/rentals/{id}/review`

### 强制下架

- `POST /api/admin/rentals/{id}/offline`

### 获取出租订单列表

- `GET /api/admin/orders`

返回要点：

- 返回全部未删除订单
- 包含房源标题、房源状态、房东昵称、租客昵称、提醒时间、取消原因等字段

### 管理员取消订单

- `POST /api/admin/orders/{id}/cancel`

请求示例：

```json
{
  "reason": "管理员取消订单"
}
```

说明：

- 通过 `X-Admin-Token` 鉴权
- 仅未取消、未完成的订单允许管理员直接取消
- 取消后订单状态变为 `CANCELED`，对应房源重新公开

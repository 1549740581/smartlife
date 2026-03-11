# API 规范草案

本文件定义首期接口规范方向。正式实现时，应基于此文件收敛为更具体的 OpenAPI 定义。

## 1. 通用规则

### Base Path

- 用户侧接口：`/api`
- 管理侧接口：`/api/admin`

### 响应结构

```json
{
  "code": "OK",
  "message": "success",
  "data": {}
}
```

### 错误结构

```json
{
  "code": "VALIDATION_ERROR",
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
  "code": "wx-login-code"
}
```

### 发布租赁信息

- `POST /api/rentals`

请求示例：

```json
{
  "rentalType": "HOUSE",
  "title": "两居室出租",
  "description": "近地铁，精装修",
  "price": 4200,
  "contactName": "张三",
  "contactPhone": "13800000000",
  "images": [
    "https://example.com/image-1.jpg"
  ]
}
```

### 查询公开列表

- `GET /api/rentals`
- `GET /api/rentals/type/{type}`

说明：

- 仅返回 `APPROVED` 数据

### 查询我的发布

- `GET /api/rentals/user/{userId}`

## 3. 管理侧接口

### 管理员登录

- `POST /api/admin/login`

### 获取待审核列表

- `GET /api/admin/rentals/pending`

### 获取全部租赁信息

- `GET /api/admin/rentals`

### 审核信息

- `POST /api/admin/rentals/{id}/review`

请求示例：

```json
{
  "action": "APPROVE",
  "reason": ""
}
```

拒绝示例：

```json
{
  "action": "REJECT",
  "reason": "联系方式不完整"
}
```

### 强制下架

- `POST /api/admin/rentals/{id}/offline`

## 4. 约束

- 所有写接口必须做参数校验
- 所有状态流转必须校验当前状态是否合法
- 所有管理端接口默认需要鉴权
- 错误码应集中维护，避免字符串散落

# API 规范草案

本文件定义当前首期接口规范方向。正式实现时，应基于此文件继续收敛为更具体的 OpenAPI 定义。

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

响应示例：

```json
{
  "code": "OK",
  "message": "success",
  "data": {
    "userId": 10003,
    "openId": "wx-user-10003",
    "nickname": "房东陈姐"
  }
}
```

说明：

- 小程序前端默认允许访客不登录直接浏览公开列表
- 当前登录为模拟微信登录，`code` 直接作为 `openId`
- 开发环境下允许输入任意 `code` 完成登录联调
- 若 `code` 首次出现，后端会自动创建对应用户

### 发布租赁信息

- `POST /api/rentals`

请求示例：

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
  ]
}
```

说明：

- `rentalType` 当前支持 `HOUSE`、`PARKING`、`ITEM`
- 发布成功后状态默认为 `PENDING`
- 小程序发布页会在调用接口前进行标题、描述、价格、联系人、联系电话的前端校验
- 发布地址必须命中地址表中存在的地址组合
- 当前 `city` 仅支持 `杭州`
- 提交成功后前端回到个人中心查看发布状态

### 地址树查询

- `GET /api/addresses/tree`

响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "label": "杭州",
      "value": "杭州",
      "children": [
        {
          "label": "滨江区",
          "value": "滨江区",
          "children": [
            {
              "label": "长河街道",
              "value": "长河街道",
              "children": [
                {
                  "label": "卓悦华庭",
                  "value": "卓悦华庭",
                  "children": []
                }
              ]
            }
          ]
        }
      ]
    }
  ],
  "timestamp": 1710000000000
}
```

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
- 小程序公开列表通过底部 `生活广场` 入口承载，支持关键词、类型和地址筛选

### 查询我的发布

- `GET /api/rentals/user/{userId}`
- `GET /api/rentals/user/{userId}/{id}`

说明：

- 返回用户自己的全部状态数据，包括 `PENDING`、`APPROVED`、`REJECTED`、`OFFLINE`
- 小程序 `个人中心` 使用该接口展示“我的发布”列表

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

响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "adminId": 1,
    "displayName": "系统管理员",
    "adminToken": "token-123",
    "expiresAt": "2026-03-12 14:00:00"
  }
}
```

说明：

- 小程序 UI 层只对指定审核员用户展示“后台审核”入口
- 后端管理接口仍以 `X-Admin-Token` 作为鉴权依据
- 当前审核入口集成在小程序 `个人中心` 中，不单独提供独立后台前端
- 会话保存在 Redis 中，服务重启后未过期 token 仍可用

### 管理员退出

- `POST /api/admin/logout`

### 获取待审核列表

- `GET /api/admin/rentals/pending`

### 获取全部租赁信息

- `GET /api/admin/rentals`

### 获取租赁信息详情

- `GET /api/admin/rentals/{id}`

### 审核信息

- `POST /api/admin/rentals/{id}/review`

请求示例：

```json
{
  "action": "APPROVE",
  "approved": true,
  "reason": ""
}
```

拒绝示例：

```json
{
  "action": "REJECT",
  "approved": false,
  "reason": "联系方式不完整"
}
```

### 强制下架

- `POST /api/admin/rentals/{id}/offline`

## 4. 约束

- 所有写接口必须做参数校验
- 所有状态流转必须校验当前状态是否合法
- 所有管理端接口默认需要鉴权
- 审核入口的前端展示规则由小程序本地用户角色控制
- 小程序固定使用底部双 Tab：`生活广场`、`个人中心`
- 错误码应集中维护，避免字符串散落

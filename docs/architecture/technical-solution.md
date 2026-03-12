# 技术方案

## 1. 总体方案

采用单体多模块架构，按“核心领域 + 用户侧 API + 管理侧 API + 启动模块 + 小程序前端”组织：

- `app-core`: 领域模型、仓储、服务、数据库迁移
- `app-web`: 用户侧 API
- `app-backend`: 管理侧 API
- `app-starter`: Spring Boot 启动入口
- `wechat-mini-app`: 原生微信小程序客户端

## 2. 鉴权策略

### 住户用户

- 首期采用“模拟微信登录”
- 前端调用 `/api/wechat/login`
- 后端使用 `code` 作为模拟 `openId`
- 若用户不存在则自动注册
- 小程序默认以访客身份进入，不强制先登录
- 发布操作在前端先完成基础表单校验，再调用后端接口

### 管理员

- 首期采用简单令牌方案
- 登录成功后返回 `adminToken`
- 响应中同时返回 `expiresAt`
- 管理端请求通过 `X-Admin-Token` 头传递令牌
- 令牌由 Redis 会话服务维护，并带滑动过期时间
- Redis 开启持久化，保证 Java 服务重启后管理员未过期会话仍可继续使用
- 支持主动退出登录
- 管理侧鉴权统一由 MVC 拦截器完成，控制器通过请求上下文读取当前管理员

说明：

- 该方案适合 MVP 阶段，后续可演进为 JWT 或 Spring Security 正式认证

## 3. 数据建模方案

### User

- 同时保留传统字段和小程序字段，兼容现有用户接口与微信登录
- 核心字段：`username`、`password`、`email`、`phone`、`nickname`、`openId`、`avatarUrl`

### Admin

- 使用用户名 + BCrypt 密码哈希
- 支持 `ACTIVE` / `DISABLED`

### RentalInfo

- 用单表承载房屋与车位
- 当前也承载闲置物品
- 通过 `rentalType` 区分类型
- 图片以 JSON 字符串落库，避免首期引入对象存储

### AddressOption

- 地址数据维护在 MySQL `address_option` 表
- 当前采用叶子表存储 `city / district / street / community_name`
- 用户侧通过 `/api/addresses/tree` 获取树形结构
- 当前城市能力限制为杭州，后续再扩展多城市

### ReviewRecord

- 记录每次审核和下架动作
- 保留前后状态、原因和操作人

## 4. 接口方案

### 小程序导航与页面组织

- 小程序底部固定两个一级入口：`生活广场`、`个人中心`
- `生活广场` 承载公开列表和类型筛选
- `生活广场` 当前支持关键词、类型、地址多条件筛选
- `个人中心` 承载用户信息、我要发布、我的发布、账号切换和审核入口
- `后台审核` 为审核员条件展示页面入口，不直接出现在公共导航中

### 用户侧

- `/api/wechat/login`
- `/api/users/*`
- `/api/addresses/tree`
- `/api/rentals`
- `/api/rentals/type/{type}`
- `/api/rentals/user/{userId}`

### 管理侧

- `/api/admin/login`
- `/api/admin/logout`
- `/api/admin/rentals/pending`
- `/api/admin/rentals`
- `/api/admin/rentals/{id}/review`
- `/api/admin/rentals/{id}/offline`

## 5. 状态流转

### 发布流程

- 创建租赁信息时状态固定为 `PENDING`

### 审核流程

- `PENDING -> APPROVED`
- `PENDING -> REJECTED`
- `APPROVED -> OFFLINE`

### 约束

- 审核拒绝必须填写原因
- 不能对非 `PENDING` 数据再次执行审核
- 不能对非 `APPROVED` 数据执行下架

## 6. 测试方案

### 单元测试

- `RentalService`
- `AdminAuthService`
- `AdminRentalService`

### Web 层测试

- 用户发布接口
- 公开列表接口
- 管理员审核接口

### 前端验证

- 首期以前端页面联调验证为主，不单独引入小程序测试框架
- 当前已形成“访客浏览 -> 模拟登录 -> 发布 -> 审核 -> 浏览”的本地联调闭环
- 发布页承担基础输入校验和提交态控制，后端继续作为最终校验边界

### 集成测试实现

- 后端涉及 MySQL / Redis 的测试统一通过 Testcontainers 启动容器
- Liquibase baseline 在集成测试启动阶段自动执行

## 7. 数据库迁移策略

- 当前已收敛为单份 Liquibase baseline：`db/changelog/v1.0/000-baseline.sql`
- 新库初始化直接执行 baseline，避免长期维护一串仅用于历史演进的初始化 SQL
- 后续新增结构变更时，在 baseline 之后继续追加新的增量脚本

## 8. 目录选择说明

当前仓库已有 `wechat-mini-app/` 目录，因此本次实现默认使用该目录作为小程序客户端目录，暂不额外创建 `miniprogram/`，避免重复维护。

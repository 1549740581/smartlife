# 系统设计

## 1. 设计目标

在保持实现简单的前提下，建立清晰的模块边界，支持后端、小程序和管理端并行开发。

## 2. 仓库模块映射

### `app-core`

职责：

- 领域模型
- 仓储接口
- 业务服务
- 公共配置
- 数据库迁移资源

### `app-web`

职责：

- 面向用户侧和开放接口的 REST API
- 参数校验
- 统一异常处理
- 接口文档暴露

### `app-backend`

职责：

- 面向管理端的后台接口
- 审核、下架、管理查询

说明：

- 当前已实现管理员登录、退出、待审核查询、全部列表、详情、审核和下架接口

### `app-starter`

职责：

- 应用启动装配
- Spring Boot 主启动类
- 环境级配置聚合

### `wechat-mini-app`

职责：

- 微信小程序用户端

## 3. 逻辑分层

当前采用以下分层：

1. Controller / API
2. Service
3. Entity / DTO
4. Repository
5. Config / Auth / Infrastructure

约束：

- Controller 不直接操作数据库
- Repository 不承载业务编排
- Service 层负责状态流转和业务规则

## 4. 关键业务对象

- User
- Admin
- RentalInfo
- ReviewRecord
- AddressOption
- RentalConversation
- RentalMessage
- RentalOrder
- Complaint

## 5. 关键流程

### 发布流程

用户提交租赁信息后进入 `PENDING`，等待管理员审核。发布时地址必须从地址表中选择，当前城市固定为杭州。

### 审核流程

管理员对 `PENDING` 内容执行 `APPROVED` 或 `REJECTED` 操作，必要时对已发布内容执行 `OFFLINE`。

## 6. 外部依赖

- MySQL：核心业务数据
- Redis：管理员会话持久化
- 微信小程序平台：登录与前端运行环境
- Swagger / OpenAPI：接口文档
- Testcontainers：MySQL / Redis 集成测试

## 7. 当前未决设计

- 真实微信登录替换模拟登录的接入方案
- 图片上传到本地、数据库还是对象存储
- 管理端是否需要独立前端项目
- 多城市地址维护和后台配置方式

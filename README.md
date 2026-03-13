# Smart Life - 小区生活助手

基于 Spring Boot 3.x + Java 17 的小区生活助手项目，包含微信小程序和管理后台接口。

## 文档入口

建议阅读顺序如下：

1. `AGENTS.md`
2. `docs/README.md`
3. `docs/product/prd.md`
4. `docs/architecture/system-design.md`
5. `docs/engineering/coding-standards.md`
6. `docs/tasks/backlog.md`

约束：

- 以后每次代码改动都必须同步更新 `docs/product/prd.md` 和相关正式文档
- 文档必须与当前可运行代码保持一致，不能滞后

## 项目结构

```text
smart-life/
├── app-core/           # 核心领域层与共享能力
├── app-web/            # 用户侧 REST API 模块
├── app-backend/        # 管理侧后端模块
├── app-starter/        # 应用启动装配模块
├── wechat-mini-app/    # 微信小程序目录
├── compose.yaml        # 本地 MySQL / Redis 编排
├── .env.example        # 本地环境变量模板
├── docs/               # 正式文档体系
├── requirement/        # 历史需求材料
└── pom.xml             # 根项目配置
```

## 技术栈

### 后端

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- MySQL 8
- Redis
- Liquibase
- Maven

### 前端

- 微信小程序原生开发

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- 微信开发者工具

### 1. 启动本地依赖

推荐直接使用 Docker：

```bash
cp .env.example .env
docker compose up -d
```

说明：

- 会启动 MySQL 8 和 Redis 7
- MySQL 默认字符集为 `utf8mb4`，校验规则为 `utf8mb4_general_ci`
- 数据库初始化按 `000-baseline.sql -> 001-add-rental-trade.sql -> 002-seed-rental-trade-demo.sql` 顺序执行

### 2. 启动后端服务

```bash
DB_USERNAME=root DB_PASSWORD=root MYSQL_DATABASE=smart_life \
mvn -pl app-starter -am clean spring-boot:run -Dspring-boot.run.profiles=local
```

说明：

- Spring Boot 启动时会先执行 Liquibase，再初始化业务 Bean
- 2026-03-12 已使用 `127.0.0.1:3306 / smart_life / root / root` 完成一次真实启动验证
- 2026-03-12 已完成 Redis 持久会话真实验证：管理员登录后 token 写入 Redis，服务重启后同一 token 仍可访问管理接口

### 3. 启动微信小程序

1. 打开微信开发者工具
2. 导入 `wechat-mini-app` 目录
3. 如有需要，修改 `wechat-mini-app/utils/config.js` 中的 `baseUrl`
4. 配置测试 AppID
5. 点击编译运行

## 开发环境模拟登录

小程序当前支持“访客默认进入 + 模拟登录”的联调方式：

- 访客：无需登录即可浏览已审核通过的信息
- 底部一级菜单：`生活广场`、`个人中心`
- 审核员示例 code：`wx-reviewer-10001`、`wx-reviewer-10002`
- 普通用户示例 code：`wx-user-10003`、`wx-user-10004`

登录方式：

1. 打开小程序后，默认以访客身份进入 `生活广场`
2. 切换到底部 `个人中心`
3. 点击 `去登录`
4. 输入任意 `code`，后端会直接将该 `code` 作为 `openId`
5. 也可以直接点击演示账号按钮自动填充示例 `code` 和昵称
6. 登录成功后返回 `生活广场`

说明：

- 开发环境不依赖真实微信登录态
- `POST /api/wechat/login` 当前为模拟登录接口
- 如果输入的 `code` 首次出现，后端会自动创建对应用户

## 审核入口与管理员账号

审核能力集成在小程序个人中心的“后台审核”入口中，只有指定审核员用户会显示该入口。

默认管理员账号：

- 用户名：`admin`
- 密码：`admin123`

## 本地联调指南

### 基础发布审核链路

1. 启动 MySQL、Redis 和 Java 服务
2. 在微信开发者工具编译小程序
3. 以访客身份进入 `生活广场`，确认能看到审核通过的数据
4. 切到 `个人中心`，点击 `去登录`
5. 使用普通用户示例 `wx-user-10003` 或 `wx-user-10004` 登录
6. 在 `个人中心` 点击 `我要发布`
7. 提交一条新的房屋、车位或闲置物品信息
8. 回到 `个人中心`，确认新记录状态为 `待审核`
9. 切换到审核员示例 `wx-reviewer-10001` 或 `wx-reviewer-10002`
10. 在 `个人中心` 进入 `后台审核`
11. 使用管理员账号 `admin / admin123` 登录审核工作台
12. 对刚提交的信息执行 `审核通过` 或 `审核拒绝`

推荐检查项：

- 访客只能看到 `APPROVED` 信息
- 普通用户看不到 `后台审核` 入口
- 审核员能看到 `后台审核` 入口
- 审核拒绝必须填写原因
- 审核通过后，信息能出现在 `生活广场`
- 审核拒绝后，发布者能在 `个人中心` 看到驳回原因

### 租赁沟通与订单链路

当前默认演示数据已经补齐：

- `wx-user-10003`：房屋发布者，同时是车位租客
- `wx-user-10004`：车位发布者，同时是房屋租客
- 房屋 `20001`：存在一条 `PENDING_CONFIRMATION` 的租期卡片
- 车位 `20002`：存在一条 `ACTIVE` 的生效订单，并带一条到期前 15 天提醒系统消息

建议联调步骤：

1. 使用 `wx-user-10003` 登录，进入 `个人中心 -> 租赁沟通`
2. 查看车位会话，确认能看到生效订单、已出租状态和到期提醒
3. 切换到 `wx-user-10004`，查看房屋会话中的待确认租期卡片
4. 以房东身份确认订单后，返回详情页验证房源状态切换为 `已出租`
5. 在任一会话中发起取消申请，再用另一方确认，验证房源重新公开
6. 使用审核员账号进入 `后台审核 -> 订单管理`，验证管理员取消订单入口

## 访问地址

- 后端 API：http://localhost:8080
- Swagger UI：http://localhost:8080/swagger-ui.html
- 健康检查：http://localhost:8080/api/health

## 功能特性

### 小程序功能

- 访客浏览已审核通过的信息
- 双 Tab 导航：`生活广场 / 个人中心`
- 模拟登录
- 房屋、车位、闲置物品发布
- 地址表驱动的级联地址选择
- 个人中心展示我的发布、我要发布、租赁沟通、账号切换和审核入口
- 公开列表支持关键词、类型、地址筛选
- 详情页支持“我想租”和拨打电话
- 站内租赁沟通支持文本消息和租期卡片
- 订单流转支持房东确认、双方取消、续约和系统提醒

### 管理侧功能

- 审核入口按用户 ID 控制显示
- 管理员登录
- Redis 持久化管理员会话
- 信息审核与强制下架
- 审核拒绝原因录入
- 订单管理与管理员直接取消订单

## 主要接口

### 用户侧

- `POST /api/wechat/login`
- `GET /api/addresses/tree`
- `POST /api/rentals`
- `GET /api/rentals`
- `GET /api/rentals/{id}`
- `GET /api/rentals/user/{userId}`
- `POST /api/rentals/{id}/conversation`
- `GET /api/rental-conversations`
- `GET /api/rental-conversations/{conversationId}`
- `POST /api/rental-conversations/{conversationId}/messages`
- `POST /api/rental-conversations/{conversationId}/orders`
- `POST /api/rental-orders/{id}/accept`
- `POST /api/rental-orders/{id}/cancel/request`
- `POST /api/rental-orders/{id}/cancel/confirm`
- `POST /api/rental-orders/{id}/renew`
- `POST /api/complaints`
- `POST /api/complaints/user`
- `POST /api/complaints/detail`
- `POST /api/messages/unread-count`
- `POST /api/rental-conversations/list`
- `POST /api/rental-conversations/mark-read`

### 管理侧

- `POST /api/admin/login`
- `POST /api/admin/logout`
- `GET /api/admin/rentals/pending`
- `POST /api/admin/rentals/{id}/review`
- `POST /api/admin/rentals/{id}/offline`
- `GET /api/admin/orders`
- `POST /api/admin/orders/{id}/cancel`
- `POST /api/admin/complaints/pending`
- `POST /api/admin/complaints/list`
- `POST /api/admin/complaints/detail`
- `POST /api/admin/complaints/process`
- `POST /api/admin/complaints/unlock`

## 配置说明

### 环境变量

- `DB_USERNAME`
- `DB_PASSWORD`
- `MYSQL_DATABASE`
- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_PASSWORD`

管理员会话：

- 管理员令牌默认使用 Redis 持久化存储
- 默认 TTL 为 120 分钟，本地 profile 为 240 分钟
- 每次访问管理接口会刷新过期时间

### 本地文件上传

- 上传目录默认是仓库根目录下的 `uploads/`
- 访问路径默认映射为 `http://localhost:8080/uploads/{fileName}`

## 常见问题

### Q: 小程序无法连接后端？

A: 检查 `wechat-mini-app/utils/config.js` 中的 `baseUrl` 是否正确，并确认后端服务已启动。

### Q: 为什么普通用户看不到“后台审核”？

A: 该入口只对配置的审核员用户 ID 展示，普通用户和访客不会显示。

### Q: 为什么公开列表看不到某条已出租信息？

A: 公开列表只展示 `APPROVED` 数据；房东确认订单后，对应房源会切换为 `RENTED` 并从公开广场移除。

### Q: 管理后台登录失败？

A: 确认后端已成功启动且数据库迁移已完成，默认账号为 `admin/admin123`。

## 许可证

MIT License

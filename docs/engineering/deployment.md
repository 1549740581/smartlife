# 部署说明

## 1. 环境划分

建议至少保留以下环境：

- `local`: 本地开发
- `test`: 联调或测试环境
- `prod`: 生产环境

## 2. 后端运行前置

- JDK 17
- MySQL 8
- Redis
- Liquibase 迁移脚本

## 3. 配置管理

- 数据库和 Redis 连接信息通过环境配置注入
- 密码和密钥不得写入代码仓库
- 环境差异通过配置文件或环境变量区分

## 4. 部署原则
- 所有后端服务统一在app-starter模块启动，通过开关控制部分或者全部模块启动
- 先执行数据库迁移，再启动应用
- 启动后检查健康接口和关键接口
- 发布过程保留回滚方案

## 5. 待补充项

- `compose.yaml` 已提供本地 MySQL / Redis 编排
- CI/CD 流程
- 日志采集和监控方案
- 小程序发布流程

## 6. 本地运行方案

### 启动依赖服务

```bash
cp .env.example .env
docker compose up -d
```

### 启动后端

```bash
DB_USERNAME=root DB_PASSWORD=root MYSQL_DATABASE=smart_life \
mvn -pl app-starter -am clean spring-boot:run -Dspring-boot.run.profiles=local
```

### 验证

- 健康检查：`http://localhost:8080/api/health`
- Swagger：`http://localhost:8080/swagger-ui.html`
- 图片上传后可通过 `http://localhost:8080/uploads/{fileName}` 访问
- 2026-03-12 已基于 `127.0.0.1:3306/smart_life` 完成真实启动验证，`/api/health` 返回 `200`
- 2026-03-12 已完成 Redis 会话真实验证：
  - `POST /api/admin/login` 返回 `adminToken` 与 `expiresAt`
  - Redis 中存在 `smart-life:admin:session:{token}`，value 为管理员 ID
  - Java 服务重启后，同一 token 访问 `GET /api/admin/rentals/pending` 仍返回 `200`
  - 调用 `POST /api/admin/logout` 后，Redis key 被删除，再访问管理接口返回 `401`

### 默认管理员

- 用户名：`admin`
- 密码：`admin123`

说明：

- 默认管理员由 `AdminAuthService` 在系统首次启动时自动初始化
- 数据库结构由 Liquibase 在启动阶段自动创建
- 管理员会话持久化在 Redis，Redis 不可用时管理端登录与鉴权会失败
- `AdminAuthService` 显式依赖数据库初始化，确保 Spring 启动时先执行 Liquibase 再访问业务表
- 本地上传文件默认落到仓库根目录 `uploads/`，该目录已加入 `.gitignore`
- 如果修改过 Liquibase 资源文件，建议使用带 `clean` 的启动命令，避免旧构建产物残留

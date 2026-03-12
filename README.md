# Smart Life - 小区生活助手

基于Spring Boot 3.x + Java 17的小区生活助手项目，包含微信小程序和管理后台。

## 文档入口

为了支持后续尽量依靠 AI 持续开发，本仓库已新增正式文档体系。建议阅读顺序如下：

1. `AGENTS.md` - AI 协作规则和文档优先级
2. `docs/README.md` - 文档导航
3. `docs/product/prd.md` - 产品需求主文档
4. `docs/architecture/system-design.md` - 系统设计
5. `docs/engineering/coding-standards.md` - 开发规范
6. `docs/tasks/backlog.md` - 任务池

说明：

- `requirement/` 目录保留为历史输入材料
- 后续新增需求、架构调整和技术决策，优先更新 `docs/` 下正式文档

## 项目结构

``` 
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
├── init-admin.sql      # 初始化管理员数据
└── pom.xml             # 根项目配置
```

## 技术栈

### 后端技术
- **Java 17** - 编程语言
- **Spring Boot 3.2.0** - 应用框架
- **Spring Data JPA** - 数据访问层
- **MySQL 8.0** - 关系型数据库
- **Redis** - 缓存数据库
- **Spring Security** - 安全框架
- **SpringDoc OpenAPI** - API文档
- **Lombok** - 代码简化工具
- **Maven** - 项目构建工具

### 前端技术
- **微信小程序** - 用户端应用
- **HTML/CSS/JavaScript** - 管理后台
- **原生小程序开发** - 无第三方框架依赖

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- 微信开发者工具（用于小程序开发）

### 1. 启动本地依赖

推荐直接使用 Docker：

```bash
cp .env.example .env
docker compose up -d
```

会启动：

- MySQL 8
- Redis 7

说明：

- 数据库结构会在应用启动时由 Liquibase 自动创建
- 当前使用单份 Liquibase baseline：`app-core/src/main/resources/db/changelog/v1.0/000-baseline.sql`
- 默认数据库账号见 `.env.example`
- MySQL 默认字符集为 `utf8mb4`，校验规则为 `utf8mb4_general_ci`

## 文档维护约束

- 以后每次代码改动都必须同步更新 `docs/product/prd.md` 和相关正式文档
- 文档必须与当前可运行代码保持一致，不能在代码变更后滞后

### 2. 启动后端服务

```bash
DB_USERNAME=root DB_PASSWORD=root MYSQL_DATABASE=smart_life \
mvn -pl app-starter -am clean spring-boot:run -Dspring-boot.run.profiles=local
```

说明：

- 如果刚调整过 Liquibase 迁移文件，建议保留 `clean`，避免旧的 `target/classes` 资源残留影响启动结果
- Spring Boot 启动过程中会先执行 Liquibase，再初始化依赖数据库表的启动 Bean
- 2026-03-12 已使用 `127.0.0.1:3306 / smart_life / root / root` 完成一次真实启动验证
- 2026-03-12 已完成 Redis 持久会话真实验证：管理员登录后 token 写入 Redis，Java 服务重启后同一 token 仍可访问管理接口，退出登录后 token 立即失效

### 3. 启动微信小程序

1. 打开微信开发者工具
2. 导入项目，选择 `wechat-mini-app` 目录
3. 如有需要，修改 `wechat-mini-app/utils/config.js` 中的 `baseUrl`
4. 配置小程序的AppID（测试可使用测试号）
5. 点击编译运行

### 4. 开发环境模拟登录

小程序当前支持“访客默认进入 + 模拟登录”的联调方式：

- 访客：无需登录即可浏览已审核通过的信息
- 底部一级菜单：`生活广场`、`个人中心`
- 审核员示例 code：`wx-reviewer-10001`、`wx-reviewer-10002`
- 普通用户示例 code：`wx-user-10003`、`wx-user-10004`

点击演示账号按钮时，小程序会自动填充对应的示例昵称，方便快速联调。

开发环境下的模拟登录方式如下：

1. 打开小程序后，默认以访客身份进入 `生活广场`
2. 切换到底部 `个人中心`
3. 点击 `去登录`
4. 在登录页输入任意 `code`，后端会直接将该 `code` 作为 `openId`
5. 也可以直接点击演示账号按钮，自动填充示例 `code` 和昵称
6. 登录成功后会返回 `生活广场`，此时可进入 `个人中心` 使用 `我要发布`

说明：

- 开发环境不依赖真实微信登录态
- `POST /api/wechat/login` 当前为模拟登录接口
- 如果输入的 `code` 首次出现，后端会自动创建对应用户
- 如果使用演示账号，能直接联调普通用户发布流程和审核员审核流程

### 5. 审核入口与管理员账号

审核能力集成在小程序个人中心的“后台审核”入口中，只有指定审核员用户会显示该入口。

后台审核接口默认管理员账号：

- 用户名：`admin`
- 密码：`admin123`

### 6. 本地联调指南

建议按下面顺序联调，能最快验证主流程是否正常：

1. 启动 MySQL、Redis 和 Java 服务
2. 在微信开发者工具编译小程序
3. 先以访客身份进入 `生活广场`，确认能看到审核通过的数据
4. 切到 `个人中心`，点击 `去登录`
5. 使用普通用户示例 `wx-user-10003` 或 `wx-user-10004` 登录
6. 在 `个人中心` 点击 `我要发布`，提交一条新的房屋、车位或闲置物品信息
7. 提交成功后回到 `个人中心`，确认新记录状态为 `待审核`
8. 切换到审核员示例 `wx-reviewer-10001` 或 `wx-reviewer-10002`
9. 在 `个人中心` 进入 `后台审核`
10. 使用管理员账号 `admin / admin123` 登录审核工作台
11. 对刚提交的信息执行 `审核通过` 或 `审核拒绝`
12. 回到普通用户账号，在 `个人中心` 查看审核结果；如果审核通过，再到 `生活广场` 验证公开展示

推荐联调检查项：

- 访客只能看到 `APPROVED` 信息
- 普通用户看不到 `后台审核` 入口
- 审核员能看到 `后台审核` 入口
- 发布页前端校验能拦截空标题、空描述、非法价格和非法联系电话
- 审核通过后，信息能出现在 `生活广场`
- 审核拒绝后，发布者能在 `个人中心` 看到驳回原因

### 访问地址

- **后端API**：http://localhost:8080
- **API文档**：http://localhost:8080/swagger-ui.html
- **健康检查**：http://localhost:8080/api/health
- **微信小程序**：在微信开发者工具中运行

## 功能特性

### 小程序功能
- **访客浏览**：用户进入小程序默认以访客身份浏览公开信息
- **双 Tab 导航**：底部固定提供“生活广场 / 个人中心”两个一级入口
- **模拟登录**：通过 code 完成演示账号登录
- **房屋出租**：发布房屋出租信息，支持图片上传
- **车位出租**：发布车位出租信息，支持图片上传
- **闲置物品发布**：支持发布闲置物品信息
- **个人中心**：提供“我要发布”、状态查看、账号切换和审核入口
- **信息浏览**：查看所有审核通过的房屋、车位和闲置物品
- **详情页**：查看完整描述、图片和联系方式
- **联系功能**：一键拨打房东/车主电话
- **审核状态**：显示发布信息的审核状态
- **表单校验**：发布页对标题、描述、价格、联系人和联系电话做前端校验

### 管理侧功能
- **审核入口控制**：仅指定审核员用户在个人中心看到“后台审核”
- **管理员登录**：安全的管理员认证
- **Redis 会话**：管理员令牌持久化在 Redis，并带过期时间
- **重启续会**：Java 服务重启后，未过期管理员会话仍然有效
- **信息审核**：审核用户发布的租赁信息
- **强制下架**：管理员可强制下架任何信息
- **状态管理**：查看所有信息的状态和详情
- **拒绝原因**：审核拒绝时可填写拒绝原因

## API接口

### 用户相关接口
- `POST /api/users` - 创建用户
- `GET /api/users/{id}` - 根据ID获取用户
- `GET /api/users` - 获取所有用户
- `PUT /api/users/{id}` - 更新用户信息
- `DELETE /api/users/{id}` - 删除用户

### 微信小程序接口
- `POST /api/wechat/login` - 微信小程序登录
- `POST /api/files/images` - 上传图片

### 租赁信息接口
- `POST /api/rentals` - 发布租赁信息
- `GET /api/rentals` - 获取所有可租信息
- `GET /api/rentals/{id}` - 获取公开详情
- `GET /api/rentals/type/{type}` - 根据类型获取（HOUSE/PARKING/ITEM）
- `GET /api/rentals/user/{userId}` - 获取用户发布的信息
- `GET /api/rentals/user/{userId}/{id}` - 获取我的发布详情

### 管理侧接口
- `POST /api/admin/login` - 管理员登录
- `GET /api/admin/rentals/pending` - 获取待审核信息
- `GET /api/admin/rentals` - 获取所有信息
- `GET /api/admin/rentals/{id}` - 获取管理侧详情
- `POST /api/admin/rentals/{id}/review` - 审核信息
- `POST /api/admin/rentals/{id}/offline` - 强制下架

## 数据库表结构

### 用户表 (users)
- 基础用户信息
- 微信相关字段（openid, unionid, avatarUrl）
- 支持微信小程序自动注册

### 租赁信息表 (rental_info)
- 统一的房屋、车位和闲置物品信息表
- 通过 `rental_type` 字段区分类型（HOUSE/PARKING/ITEM）
- 包含审核状态和审核相关字段
- 支持图片和视频存储（JSON格式）

### 管理员表 (admins)
- 管理员账号信息
- 用于管理侧登录和权限控制

## 审核流程

1. **访客浏览**：未登录用户默认浏览 `APPROVED` 信息
2. **用户发布**：用户在个人中心点击“我要发布”，发布页进行前端校验后提交，状态变为 `PENDING`
3. **管理员审核**：审核员用户进入后台审核页查看待审核信息
4. **审核结果**：
   - 通过：状态变为 `APPROVED`，信息在小程序中显示
   - 拒绝：状态变为 `REJECTED`，可填写拒绝原因
5. **强制下架**：管理员可随时将信息状态改为 `OFFLINE`

## 开发指南

### 后端开发
1. 实体类继承 `BaseEntity` 获得基础字段
2. 使用JPA注解进行数据库映射
3. Repository接口继承 `JpaRepository`
4. 服务层使用 `@Service` 注解
5. 控制器使用 `@RestController` 和Swagger注解

### 小程序开发
1. 页面文件包含 `.wxml`、`.js`、`.wxss`、`.json`
2. 使用 `wx.request` 调用后端API
3. 通过 `app.js` 管理全局状态和用户信息
4. 通过 `reviewerUserIds` 控制审核入口展示
5. 使用微信官方组件和API

### 管理审核能力开发
1. 当前管理审核能力集成在微信小程序中
2. 通过管理员接口和 `X-Admin-Token` 调用后端能力
3. 如后续拆分独立前端，再补独立开发规范

## 配置说明

### 环境变量

可以通过环境变量覆盖配置：
- `DB_USERNAME` - 数据库用户名
- `DB_PASSWORD` - 数据库密码
- `REDIS_HOST` - Redis主机地址
- `REDIS_PORT` - Redis端口
- `REDIS_PASSWORD` - Redis密码

管理员会话：
- 管理员令牌默认使用 Redis 持久化存储
- 默认 TTL 为 120 分钟，本地 profile 为 240 分钟
- 每次访问管理接口会刷新过期时间

本地文件上传：
- 上传目录默认是仓库根目录下的 `uploads/`
- 访问路径默认映射为 `http://localhost:8080/uploads/{fileName}`

### 微信小程序配置

1. 在微信公众平台注册小程序账号
2. 获取小程序的AppID和AppSecret
3. 配置服务器域名（后端API地址）
4. 在 `wechat-mini-app/utils/config.js` 中修改 `baseUrl`

### 日志配置

日志文件位置：`logs/smart-life.log`

## 常见问题

### Q: 小程序无法连接后端？
A: 检查 `wechat-mini-app/utils/config.js` 中的 `baseUrl` 是否正确，确保后端服务已启动

### Q: 为什么普通用户看不到“后台审核”？
A: 该入口只对 `wechat-mini-app/utils/config.js` 中配置的审核员用户 ID 展示，普通用户和访客不会显示

### Q: 发布的信息不显示？
A: 新发布的信息需要管理员审核通过后才会在小程序中显示；访客和普通用户公开列表只看到 `APPROVED` 数据

### Q: 管理后台登录失败？
A: 默认管理员会在应用首次启动时自动初始化，确认后端已成功启动且数据库迁移已完成，默认账号为 `admin/admin123`

### Q: 图片上传失败？
A: 确认后端已启动且 `POST /api/files/images` 可用，同时检查 `uploads/` 目录是否可写；当前版本已支持本地图片上传

## 部署

### 生产环境部署

1. **后端部署**：
   - 打包：`mvn clean package`
   - 运行：`java -jar app-starter/target/app-starter-1.0.0.jar --spring.profiles.active=prod`

2. **小程序部署**：
   - 在微信开发者工具中点击"上传"
   - 在微信公众平台提交审核
   - 审核通过后发布

3. **管理审核能力**：
   - 当前管理审核能力集成在微信小程序中
   - 如后续拆分独立前端，再补独立部署说明

## 技术支持

如有问题，请提交Issue或联系开发团队。

## 许可证

MIT License

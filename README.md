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
├── wechat-mini-app/        # 微信小程序目录
├── wechat-mini-app/    # 预留或待清理的小程序相关目录
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

### 1. 数据库配置

1. 创建MySQL数据库：
```sql
CREATE DATABASE smart_life CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改配置文件中的数据库连接信息：
```yaml
# app-core/src/main/resources/application-core.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smart_life?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
```

3. 执行初始化SQL脚本：
```bash
mysql -u your_username -p smart_life < init-admin.sql
```

### 2. Redis配置

确保Redis服务正在运行，默认配置：
- 主机：localhost
- 端口：6379
- 无密码

### 3. 启动后端服务

1. 克隆项目到本地
2. 进入项目根目录
3. 编译项目：
```bash
mvn clean compile
```

4. 运行项目：
```bash
cd app-web
mvn spring-boot:run
```

或者直接运行主类：`com.yxtech.smartlife.SmartLifeApplication`

### 4. 启动微信小程序

1. 打开微信开发者工具
2. 导入项目，选择 `wechat-mini-app` 目录
3. 修改 `wechat-mini-app/app.js` 中的 `baseUrl` 为你的后端地址
4. 配置小程序的AppID（测试可使用测试号）
5. 点击编译运行

### 5. 启动管理后台

1. 直接在浏览器中打开 `admin-web/index.html`
2. 或者通过Web服务器访问（推荐）：
```bash
# 使用Python简单服务器
cd admin-web
python -m http.server 8081
# 访问 http://localhost:8081
```

3. 使用默认管理员账号登录：
   - 用户名：`admin`
   - 密码：`admin123`

### 访问地址

- **后端API**：http://localhost:8080
- **API文档**：http://localhost:8080/swagger-ui.html
- **健康检查**：http://localhost:8080/api/health
- **管理后台**：http://localhost:8081（如使用Web服务器）
- **微信小程序**：在微信开发者工具中运行

## 功能特性

### 小程序功能
- **自动登录**：用户进入小程序自动注册/登录
- **房屋出租**：发布房屋出租信息，支持图片上传
- **车位出租**：发布车位出租信息，支持图片上传
- **信息浏览**：查看所有可租房屋和车位信息
- **联系功能**：一键拨打房东/车主电话
- **审核状态**：显示发布信息的审核状态

### 管理后台功能
- **管理员登录**：安全的管理员认证
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

### 租赁信息接口
- `POST /api/rentals` - 发布租赁信息
- `GET /api/rentals` - 获取所有可租信息
- `GET /api/rentals/type/{type}` - 根据类型获取（HOUSE/PARKING）
- `GET /api/rentals/user/{userId}` - 获取用户发布的信息

### 管理后台接口
- `POST /api/admin/login` - 管理员登录
- `GET /api/admin/rentals/pending` - 获取待审核信息
- `GET /api/admin/rentals` - 获取所有信息
- `POST /api/admin/rentals/{id}/review` - 审核信息
- `POST /api/admin/rentals/{id}/offline` - 强制下架

## 数据库表结构

### 用户表 (users)
- 基础用户信息
- 微信相关字段（openid, unionid, avatarUrl）
- 支持微信小程序自动注册

### 租赁信息表 (rental_info)
- 统一的房屋和车位租赁信息表
- 通过 `rental_type` 字段区分类型（HOUSE/PARKING）
- 包含审核状态和审核相关字段
- 支持图片和视频存储（JSON格式）

### 管理员表 (admins)
- 管理员账号信息
- 用于管理后台登录和权限控制

## 审核流程

1. **用户发布**：用户在小程序中发布租赁信息，状态为 `PENDING`（待审核）
2. **管理员审核**：管理员在后台查看待审核信息
3. **审核结果**：
   - 通过：状态变为 `APPROVED`，信息在小程序中显示
   - 拒绝：状态变为 `REJECTED`，可填写拒绝原因
4. **强制下架**：管理员可随时将信息状态改为 `OFFLINE`

## 开发指南

### 后端开发
1. 实体类继承 `BaseEntity` 获得基础字段
2. 使用JPA注解进行数据库映射
3. dao接口继承 `Jpadao`
4. 服务层使用 `@Service` 注解
5. 控制器使用 `@RestController` 和Swagger注解

### 小程序开发
1. 页面文件包含 `.wxml`、`.js`、`.wxss`、`.json`
2. 使用 `wx.request` 调用后端API
3. 通过 `app.js` 管理全局状态和用户信息
4. 使用微信官方组件和API

### 管理后台开发
1. 使用原生HTML/CSS/JavaScript
2. 通过 `fetch` API调用后端接口
3. 简单的单页面应用架构

## 配置说明

### 环境变量

可以通过环境变量覆盖配置：
- `DB_USERNAME` - 数据库用户名
- `DB_PASSWORD` - 数据库密码
- `REDIS_HOST` - Redis主机地址
- `REDIS_PORT` - Redis端口
- `REDIS_PASSWORD` - Redis密码

### 微信小程序配置

1. 在微信公众平台注册小程序账号
2. 获取小程序的AppID和AppSecret
3. 配置服务器域名（后端API地址）
4. 在 `wechat-mini-app/app.js` 中修改 `baseUrl`

### 日志配置

日志文件位置：`logs/smart-life.log`

## 常见问题

### Q: 小程序无法连接后端？
A: 检查 `wechat-mini-app/app.js` 中的 `baseUrl` 是否正确，确保后端服务已启动

### Q: 管理后台登录失败？
A: 确保已执行 `init-admin.sql` 脚本，默认账号为 admin/admin123

### Q: 发布的信息不显示？
A: 新发布的信息需要管理员审核通过后才会在小程序中显示

### Q: 图片上传失败？
A: 当前版本使用模拟图片URL，生产环境需要集成真实的图片上传服务

## 部署

### 生产环境部署

1. **后端部署**：
   - 打包：`mvn clean package`
   - 运行：`java -jar app-web/target/app-web-1.0.0.jar --spring.profiles.active=prod`

2. **小程序部署**：
   - 在微信开发者工具中点击"上传"
   - 在微信公众平台提交审核
   - 审核通过后发布

3. **管理后台部署**：
   - 将 `admin-web` 目录部署到Web服务器
   - 配置Nginx或Apache代理

## 技术支持

如有问题，请提交Issue或联系开发团队。

## 许可证

MIT License

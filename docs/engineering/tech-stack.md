# 技术栈说明

## 1. 当前基线

根据仓库现有 `pom.xml` 和目录结构，首期开发基线如下：

### 后端

- Java 17
- Spring Boot 3.2.0
- Spring Web
- Spring Security
- Spring Data JPA
- MySQL 8.0
- Redis
- Liquibase
- Maven

### 文档

- SpringDoc OpenAPI
- Markdown 文档体系

### 测试

- JUnit 5
- Testcontainers
- Mockito

### 前端

- 微信小程序原生开发
- JavaScript
- WXML / WXSS

## 2. 依赖策略

- 优先使用 Spring Boot 官方生态，不重复引入功能重叠框架。
- 如无明确必要，不引入额外分布式中间件。
- ORM 默认使用 JPA；若局部场景需要其他持久化方式，需先说明理由。

## 3. 版本策略

- Java 主版本固定为 17，升级前需评估所有模块兼容性。
- Spring Boot 采用 3.2.x 小版本升级策略。
- 数据库版本以 MySQL 8 为目标环境。

## 4. 允许的扩展方向

以下扩展在有明确需求时可考虑：

- 对象存储：用于图片上传
- 独立后台前端：如管理复杂度上升
- 缓存策略：用于列表和热点详情

## 5. 暂不建议引入

- 微服务拆分
- 消息队列
- 复杂工作流引擎
- 多套前端框架并存

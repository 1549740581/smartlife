# ADR 0001: 首期技术栈选型

## 状态

Accepted

## 背景

当前仓库已具备 Maven 多模块结构和 Spring Boot 依赖骨架，需要明确首期允许采用的主技术栈，避免实现过程中频繁摇摆。

## 决策

首期采用以下技术栈：

- Java 17
- Spring Boot 3.2.x
- Maven 多模块工程
- Spring Web
- Spring Security
- Spring Data JPA
- MySQL 8.0
- Redis
- SpringDoc OpenAPI
- Liquibase
- 微信小程序原生开发
- React
- TypeScript

测试技术默认采用：

- JUnit 5
- Testcontainers
- Kotest / MockK（仅在测试层使用）

## 原因

- 与当前仓库 `pom.xml` 已声明的依赖一致
- 学习和维护成本较低
- 足够支撑首期中小规模业务

## 结果

- 后续如引入新 ORM、消息队列或前端框架，应先补 ADR
- 优先在现有骨架内完善能力，而不是频繁更换框架

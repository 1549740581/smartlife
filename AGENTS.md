# AGENTS.md

本文件定义 AI 在本仓库中的默认工作方式。目标是让需求、架构、技术栈和开发规范有稳定入口，减少实现过程中的偏差。

## 1. 文档读取顺序

开始任何开发前，按以下顺序获取上下文：

1. `README.md`
2. `docs/README.md`
3. `docs/product/prd.md`
4. `docs/architecture/system-design.md`
5. `docs/engineering/tech-stack.md`
6. `docs/engineering/coding-standards.md`
7. `docs/tasks/backlog.md`

如果多个文档冲突，优先级如下：

1. `docs/product/prd.md`
2. `docs/architecture/adr/`
3. `docs/engineering/`
4. `README.md`
5. `requirement/`

`requirement/` 目录视为历史输入材料，不作为最终规范来源。

## 2. 当前项目定位

- 项目名称：Smart Life / 小区生活助手
- 当前形态：多模块 Java 后端骨架 + 微信小程序骨架
- 主要业务：小区内房屋/车位租赁信息发布、审核、浏览和联系

## 3. AI 开发规则

- 不擅自引入未在 `docs/engineering/tech-stack.md` 中声明的核心框架。
- 新增重要技术决策前，先补对应 ADR。
- 需求不明确时，先按 `docs/product/prd.md` 的范围实现，不扩写新功能。
- 改代码时优先遵循已有模块边界：`app-core`、`app-web`、`app-starter`、`app-backend`。
- 每次实现功能时，同时补齐必要的测试和文档。
- 不删除现有用户文件，除非文档或任务明确要求。

## 4. 代码组织约束

### 后端

- `app-core` 放领域实体、仓储、服务、共享配置和公共能力。
- `app-web` 放对外 REST API。
- `app-backend` 预留为管理端后端接口或后台相关能力。
- `app-starter` 作为应用启动装配模块。

### 前端

- `wechat-mini-app/` 作为微信小程序主目录。
- `wechat-mini-app/` 若后续继续使用，需先在 ADR 或任务文档中明确其角色，避免和 `wechat-mini-app/` 重复。

## 5. 实现前检查

开始编码前，应确认：

- 目标任务是否已写入 `docs/tasks/backlog.md` 或对应迭代文档
- 功能范围是否在 `docs/product/prd.md` 内
- 接口和数据结构是否已在 `docs/architecture/` 中定义或允许补充

## 6. 实现后检查

完成后应同步更新：

- 相关任务状态
- 新增的接口或数据结构文档
- 新增的重要设计决策 ADR
- README 的启动说明（如果启动方式发生变化）

## 7. 输出偏好

- 先给结论，再给改动点，再给验证结果。
- 说明问题时尽量引用具体文件。
- 如果仓库现状不足以支撑某项实现，要明确指出缺口，而不是假设完成。

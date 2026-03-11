# ADR 0002: 仓库模块边界

## 状态

Accepted

## 背景

仓库已经存在 `app-core`、`app-web`、`app-starter`、`app-backend` 等模块，但边界尚未通过正式文档固化。

## 决策

采用以下模块职责划分：

- `app-core`: 领域模型、业务服务、仓储、共享配置
- `app-web`: 用户侧 REST API
- `app-backend`: 管理侧 REST API以及后台前端
- `app-starter`: 应用启动和装配
- `wechat-mini-app`: 微信小程序客户端

## 原因

- 与现有目录结构一致
- 便于按业务端口拆分接口
- 避免将管理端逻辑和用户端接口混在一起

## 结果

- 业务逻辑优先沉淀到 `app-core`
- 控制层模块不承载复杂领域逻辑
- 新增模块前需说明现有模块为何无法承载

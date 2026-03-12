# 编码规范

## 1. 通用规则

- 优先保持实现直接、可读、可测试。
- 命名要贴近业务，不使用含糊缩写。
- 同类问题使用统一模式，不在仓库内混用多种风格。
- 当前后端默认调用链路为 `Controller -> Service -> Repository`
- 每次改动如果影响需求、交互、接口、数据库、测试方式或运行方式，必须同步更新 `docs/product/prd.md` 和相关正式文档，不允许代码领先文档。

## 2. 包与目录规范

### 后端建议包结构

- `config`
- `common`
- `entity`
- `repository`
- `service`
- `controller`
- `dto`
- `exception`
- `auth`
- `mysql`

## 3. 后端分层规范

- `controller`: 只负责收参、调服务、出参
- `service`: 负责业务编排、校验、状态流转
- `repository`: 负责数据访问
- `entity`: 负责领域数据承载
- `auth`: 管理员鉴权、上下文和拦截器
- `mysql`: MySQL 数据源相关配置

禁止事项：

- Controller 直接写业务规则
- Service 直接拼接 HTTP 响应
- 跨层直接绕过 Service 操作 Repository

## 4. 接口规范

- 路径使用名词复数形式
- 请求和响应 DTO 分离
- 所有接口统一响应体
- 校验失败返回明确错误码和消息
- 当前统一响应字段为 `code`、`message`、`data`、`timestamp`

## 5. 数据库规范

- 表名采用 `snake_case`
- 当前项目表统一使用 `id`、`created_at`、`updated_at`、`deleted`
- 表数据删除只做逻辑删除，当前统一使用 `deleted` 标识
- 状态字段采用枚举值，不使用魔法数字
- 结构变更通过 Liquibase 管理
- 新库初始化优先维护 Liquibase baseline，避免长期堆积大量一次性历史 SQL

## 6. 异常与日志

- 统一异常处理入口
- 业务异常和系统异常分开建模
- 关键状态流转必须记录日志
- 日志中避免输出敏感信息和明文密码

## 7. 测试规范

- 新增业务逻辑优先补 Service 层测试
- 修复缺陷时必须补对应回归测试
- 涉及数据库行为的测试优先使用集成测试验证
- 涉及 MySQL / Redis 的集成测试默认通过 Testcontainers 启动依赖容器

## 8. 小程序规范

- 页面逻辑、请求调用、通用工具分开组织
- 接口地址统一配置，不在页面内硬编码
- 状态文案与后端枚举要有映射表，不直接散落在页面代码中

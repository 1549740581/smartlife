# 文档导航

本目录用于承载 AI 协作开发所需的正式文档。建议后续所有新需求、架构调整、技术决策和开发规范都收敛到这里，而不是继续散落在聊天记录或临时文件中。

## 目录说明

- `product/`: 产品定位、PRD、用户故事、路线图
- `architecture/`: 系统设计、数据模型、接口规范、架构决策记录
- `engineering/`: 技术栈、编码规范、测试策略、协作流程、部署说明
- `tasks/`: 需求拆解、任务池、迭代计划

## 阅读顺序

1. `product/project-overview.md`
2. `product/prd.md`
3. `architecture/system-design.md`
4. `engineering/tech-stack.md`
5. `engineering/coding-standards.md`
6. `tasks/backlog.md`

## 维护原则

- 一个结论只保留一个主入口，避免多个文件重复维护。
- 抽象描述必须尽量转成可实现、可验证的规则。
- 需求变更先改文档，再改代码。
- 重要技术决策必须写入 `architecture/adr/`。

# Smart Life 管理后台

PC端管理后台前端项目，基于 React + TypeScript + Ant Design 构建。

## 技术栈

- **框架**: React 18
- **构建工具**: Vite 5
- **UI 组件库**: Ant Design 5
- **状态管理**: Zustand
- **路由**: React Router 6
- **图表**: ECharts + echarts-for-react
- **HTTP 请求**: Axios
- **语言**: TypeScript

## 目录结构

```
src/
├── main.tsx              # 应用入口
├── App.tsx               # 路由配置
├── index.css             # 全局样式
├── vite-env.d.ts         # Vite 类型声明
├── stores/
│   └── auth.ts           # 认证状态管理（Zustand）
├── utils/
│   └── request.ts        # Axios 封装，统一请求拦截
├── layouts/
│   └── MainLayout.tsx    # 主布局（侧边栏 + 头部 + 内容区）
└── pages/
    ├── Login.tsx         # 微信扫码登录
    ├── Dashboard.tsx     # 数据面板
    ├── rentals/          # 信息管理
    │   ├── RentalList.tsx
    │   └── RentalDetail.tsx
    ├── complaints/       # 投诉管理
    │   ├── ComplaintList.tsx
    │   └── ComplaintDetail.tsx
    ├── users/            # 用户管理
    │   ├── UserList.tsx
    │   └── UserDetail.tsx
    └── addresses/        # 地址管理
        └── AddressList.tsx
```

## 功能模块

| 模块 | 路由 | 功能说明 |
| --- | --- | --- |
| 登录 | `/login` | 微信扫码登录，轮询检查登录状态 |
| 数据面板 | `/dashboard` | 核心指标卡片、趋势图、分布饼图 |
| 信息管理 | `/rentals` | 列表搜索、按类型/状态筛选、审核、下架 |
| 信息详情 | `/rentals/:id` | 详情查看、审核操作、审核记录 |
| 投诉管理 | `/complaints` | 投诉列表、按状态筛选 |
| 投诉详情 | `/complaints/:id` | 详情查看、接受/驳回处理 |
| 用户管理 | `/users` | 用户列表、搜索、锁定/解锁 |
| 用户详情 | `/users/:id` | 用户信息、发布记录 |
| 地址管理 | `/addresses` | 地址增删改查 |

## 开发

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

开发服务器默认运行在 `http://localhost:3000`。

API 请求会自动代理到后端 `http://localhost:8080`。

### 构建生产版本

```bash
npm run build
```

构建产物输出到 `../resources/static/admin` 目录，可直接由 Spring Boot 静态资源服务。

## API 接口

前端调用的所有 API 都以 `/api/admin-web` 为前缀。

### 认证相关

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/api/admin-web/qrcode` | GET | 获取微信登录二维码 |
| `/api/admin-web/qrcode/status` | GET | 轮询登录状态 |

### 数据面板

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/api/admin-web/dashboard/overview` | GET | 获取概览数据 |
| `/api/admin-web/dashboard/trends` | GET | 获取趋势数据 |
| `/api/admin-web/dashboard/distributions` | GET | 获取分布数据 |

### 信息管理

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/api/admin-web/rentals` | GET | 获取信息列表 |
| `/api/admin-web/rentals/:id` | GET | 获取信息详情 |
| `/api/admin-web/rentals/:id/review` | POST | 审核信息 |
| `/api/admin-web/rentals/:id/offline` | POST | 下架信息 |

### 投诉管理

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/api/admin-web/complaints` | GET | 获取投诉列表 |
| `/api/admin-web/complaints/:id` | GET | 获取投诉详情 |
| `/api/admin-web/complaints/:id/process` | POST | 处理投诉 |

### 用户管理

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/api/admin-web/users` | GET | 获取用户列表 |
| `/api/admin-web/users/:id` | GET | 获取用户详情 |
| `/api/admin-web/users/:id/rentals` | GET | 获取用户发布的信息 |
| `/api/admin-web/users/:id/lock` | POST | 锁定用户 |
| `/api/admin-web/users/:id/unlock` | POST | 解锁用户 |

### 地址管理

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/api/admin-web/addresses` | GET | 获取地址列表 |
| `/api/admin-web/addresses` | POST | 新增地址 |
| `/api/admin-web/addresses/:id` | PUT | 修改地址 |
| `/api/admin-web/addresses/:id` | DELETE | 删除地址 |

## 后端 API 实现

后端 API 控制器位于 `app-backend/src/main/java/com/yxtech/smartlife/controller/adminweb/` 目录：

| 控制器 | 路径前缀 | 功能 |
| --- | --- | --- |
| `AdminWebAuthController` | `/api/admin-web` | 微信扫码登录、登出 |
| `AdminWebDashboardController` | `/api/admin-web/dashboard` | 数据面板统计 |
| `AdminWebRentalController` | `/api/admin-web/rentals` | 信息管理 |
| `AdminWebComplaintController` | `/api/admin-web/complaints` | 投诉管理 |
| `AdminWebUserController` | `/api/admin-web/users` | 用户管理 |
| `AdminWebAddressController` | `/api/admin-web/addresses` | 地址管理 |

### 认证配置

- 拦截器配置在 `AdminWebMvcConfigurer.java`
- `/api/admin-web/qrcode` 和 `/api/admin-web/qrcode/status` 不需要认证
- 其他接口需要在请求头中携带 `Authorization: Bearer {token}`

## 注意事项

1. **认证**: 所有非登录页面都需要认证，认证信息存储在 localStorage 中
2. **错误处理**: Axios 拦截器统一处理 401 错误，自动跳转登录页
3. **微信扫码登录**: 目前使用模拟二维码，实际部署需要接入微信开放平台
4. **生产部署**: 构建后通过 Spring Boot 提供静态资源服务，访问路径为 `/admin`

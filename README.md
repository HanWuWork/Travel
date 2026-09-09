# AI 旅游助手

基于 Vue 3 + Spring Boot 的全栈 AI 旅游规划应用。用户输入目的地、预算、天数，AI 自动生成详细行程规划（含每日景点、预算分配），并提供智能对话助手随时答疑。

## 功能特性

- **AI 行程规划** — 输入目的地/预算/天数，AI 生成逐日行程方案与预算明细
- **AI 对话助手** — 支持流式(SSE)与非流式两种模式，实时旅游问答
- **用户认证** — 注册/登录/登出，BCrypt 密码加密，Token 拦截器鉴权
- **收藏管理** — 景点/行程收藏与取消，收藏状态查询
- **订单管理** — 创建/查看/删除订单
- **浏览历史** — 记录用户查看过的行程
- **移动端适配** — 基于 Vant 4 组件库，开屏动画 + 底部 Tab 导航

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3、Vite、Vant 4、Vue Router |
| 后端 | Spring Boot 4.1、Spring Web MVC、Spring Data JPA |
| 数据库 | MySQL 8.0（生产）、H2（本地开发可选） |
| AI  | SiliconFlow API（OpenAI 兼容）、DeepSeek-V3 模型 |
| 鉴权 | BCrypt 密码加密 + Token 拦截器（非 Spring Security 全家桶） |
| 构建 | Maven（后端）、npm（前端） |

## 项目结构

```
Travel/
├── travel/                      # 前端 Vue 3 项目
│   ├── src/
│   │   ├── api/                 # API 请求封装（auth, chat, plan, order, favorite）
│   │   ├── components/          # 通用组件（JourneyCard, QuickEntryCard, ScenicSpotCard）
│   │   ├── views/               # 页面（Home, Chat, Profile, Login, Register, PlanResult, ...）
│   │   ├── router/              # 路由配置 + 登录守卫
│   │   ├── utils/               # 工具函数（auth token 管理）
│   │   └── App.vue              # 根组件 + 开屏动画 + Tab 导航
│   ├── vite.config.js           # Vite 配置（含 /api 代理到后端 1200 端口）
│   └── package.json
│
├── travel-server/               # 后端 Spring Boot 项目
│   ├── src/main/java/com/example/travelserver/
│   │   ├── controller/          # 接口层
│   │   │   ├── AuthController        — 认证（注册/登录/登出/用户信息）
│   │   │   ├── TravelController      — 行程规划
│   │   │   ├── OrderController       — 订单管理
│   │   │   ├── FavoriteController    — 收藏管理
│   │   │   └── ai/AiChatController   — AI 对话（流式 + 非流式）
│   │   ├── service/             # 业务层
│   │   │   ├── user/             — AuthService, FavoriteService, OrderService
│   │   │   ├── travel/           — TravelPlanService（AI 行程生成）
│   │   │   └── ai/               — AiChatService, AiAssistant（SiliconFlow + 规则引擎双实现）
│   │   ├── entity/              # JPA 实体（User, TravelOrder, Favorite）
│   │   ├── repository/          # JPA Repository
│   │   ├── dto/                 # 请求 DTO（Login, Register, Order, Favorite, Chat, TravelPlan）
│   │   ├── vo/                  # 响应 VO（Result 统一响应, UserVO, OrderVO, TravelPlanVO, ...）
│   │   ├── common/              # 公共组件（BusinessException, GlobalExceptionHandler, UserContext）
│   │   └── config/             # 配置（AuthInterceptor, WebConfig, TravelAiProperties）
│   └── src/main/resources/
│       ├── application.yml       # 主配置（数据库 + AI 配置）
│       └── application-local.yml # 本地私有配置（已 gitignore，存放 API Key）
│
└── .gitignore
```

## 快速开始

### 环境要求

- **Node.js** >= 18
- **Java** >= 17
- **Maven** >= 3.8（或使用项目自带 mvnw）
- **MySQL** 8.0+（可选，无 MySQL 时可用 H2 内存库）

### 1. 克隆项目

```bash
git clone <repo-url>
cd Travel
```

### 2. 启动后端

```bash
cd travel-server

# 方式一：使用 Maven Wrapper
./mvnw spring-boot:run

# 方式二：使用系统 Maven
mvn spring-boot:run
```

后端启动后监听 **1200** 端口。

### 3. 启动前端

```bash
cd travel
npm install
npm run dev
```

前端开发服务器启动后，`/api` 请求会自动代理到后端 `http://127.0.0.1:1200`。

### 4. 数据库配置

**使用 MySQL（默认）：**

确保本地 MySQL 8.0 运行在 `localhost:3306`，创建数据库：

```sql
CREATE DATABASE travel CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

默认账号 `root`、密码 `root`，如需修改请编辑 `travel-server/src/main/resources/application.yml`。

**使用 H2 内存库（无需安装 MySQL）：**

在 `application-local.yml`（已 gitignore，需自行创建）中覆盖数据源：

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:travel;DB_CLOSE_DELAY=-1;MODE=MySQL
    username: sa
    password:
    driver-class-name: org.h2.Driver
```

### 5. 配置 AI API Key

在 `application-local.yml` 中配置 SiliconFlow API Key：

```yaml
travel:
  ai:
    siliconflow:
      api-key: your-api-key-here
```

或通过环境变量注入（推荐，避免密钥进入文件）：

```bash
export SILICONFLOW_API_KEY=your-api-key-here
```

> 如未配置 API Key，AI 功能将自动回退到内置规则引擎（`provider: mock`），可离线体验基本功能。

## API 接口

所有接口统一前缀 `/api`，响应格式 `{ "code": 200, "message": "...", "data": ... }`。

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/api/auth/register` | 注册 | 否 |
| POST | `/api/auth/login` | 登录 | 否 |
| POST | `/api/auth/logout` | 登出 | 是 |
| GET | `/api/user/info` | 获取当前用户信息 | 是 |
| POST | `/api/travel/plan` | 生成行程规划（destination, budget, days） | 否 |
| POST | `/api/ai/chat` | AI 对话（非流式） | 否 |
| POST | `/api/ai/chat/stream` | AI 对话（SSE 流式） | 否 |
| POST | `/api/favorite/add` | 添加收藏 | 是 |
| GET | `/api/favorite/list` | 收藏列表 | 是 |
| DELETE | `/api/favorite/{id}` | 取消收藏 | 是 |
| GET | `/api/favorite/check` | 检查是否已收藏 | 是 |
| POST | `/api/order/create` | 创建订单 | 是 |
| GET | `/api/order/list` | 订单列表 | 是 |
| DELETE | `/api/order/{id}` | 删除订单 | 是 |

## 配置说明

### AI 提供方切换

在 `application.yml` 中切换 `travel.ai.provider`：

| 值 | 说明 |
|----|------|
| `siliconflow` | 调用 SiliconFlow 真实大模型（需 API Key） |
| `mock` | 内置规则引擎，无需 Key，离线可用 |

### 安全说明

- `application-local.yml` 已在 `.gitignore` 中排除，**切勿提交到版本库**
- API Key 推荐通过环境变量 `SILICONFLOW_API_KEY` 注入，避免明文写入文件
- 用户密码使用 BCrypt 加密存储，不使用明文

## 开发说明

- 前端代理：Vite 开发服务器将 `/api` 代理到 `http://127.0.0.1:1200`，生产环境需配置 Nginx 反向代理
- JPA DDL：`ddl-auto: update`，实体变更会自动同步表结构
- 路由守卫：需登录的页面（orders, favorites, history, coupons, settings, help, about）未登录时自动跳转登录页

## License

MIT

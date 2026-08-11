# CLAUDE.md

> EnStudApp (英语学习助手) — 给 AI Coding Agent 的项目指令

## 项目概述

EnStudApp 是一款基于 Spring Cloud 微服务架构的免费英语学习应用，采用前后端分离架构。
后端 7 个微服务 + API 网关，前端 React + TypeScript + Ant Design Mobile。

**功能模块：** 单词学习、AI英语日常聊天、英语写作批改、英语翻译、英语阅读、论坛交流

**技术栈：**

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot + Spring Cloud Alibaba | 3.2.4 / 2023.0.1.2 |
| Java | JDK | 17 |
| ORM | MyBatis-Plus | 3.5.5 |
| 注册/配置中心 | Nacos | 2.2.3 |
| API 网关 | Spring Cloud Gateway | — |
| 数据库 | MySQL | 8.0 |
| 缓存 | Redis + Redisson | 7.x / 3.27.0 |
| 消息队列 | RocketMQ | 5.1.4 |
| API 文档 | Knife4j (OpenAPI 3) | 4.4.0 |
| 认证 | JWT (jjwt) | 0.12.5 |
| 前端框架 | React + TypeScript | Vite 8 / React 19 / TS 6 |
| UI 组件库 | Ant Design (antd) | 6.x |
| 容器编排 | Docker Compose | — |
| CI/CD | GitHub Actions | — |

---

## 仓库结构

```
EnStudApp/
├── CLAUDE.md                          # AI Coding Agent 项目指令
├── README.md                          # 项目说明
├── 产品设计文档.md                     # 产品设计文档
├── pom.xml                            # Maven 父工程（统一版本管理）
├── docker-compose.yml                 # 容器编排（开发环境）
├── docker-compose.prod.yml            # 容器编排（生产环境覆盖配置）
├── .env.example                       # 环境变量模板
├── .gitignore                         # Git 忽略规则
├── build-frontend.bat                 # Windows 前端构建脚本
├── deploy-frontend.sh                 # Linux 前端部署脚本
├── run-migrations.sh                  # 数据库迁移脚本
│
├── enstud-common/                     # 公共模块（实体、DTO、工具、Feign 客户端）
│   └── src/main/java/com/enstud/common/
│       ├── entity/                    # 全部数据库实体
│       ├── dto/                       # 跨服务共享 DTO
│       ├── feign/                     # OpenFeign 客户端接口
│       ├── constant/                  # 常量定义
│       ├── enums/                     # 枚举定义
│       ├── exception/                 # 异常类
│       ├── util/                      # 工具类
│       ├── config/                    # 公共配置类
│       ├── Result.java                # 统一响应 Result<T>
│       ├── PageResult.java            # 游标分页 PageResult<T>
│       ├── BusinessException.java     # 业务异常
│       ├── GlobalExceptionHandler.java# 全局异常处理
│       ├── SecurityContext.java       # 用户上下文
│       ├── JwtUtil.java               # JWT 工具类
│       └── MybatisPlusConfig.java     # MyBatis Plus 配置
│
├── enstud-gateway/                    # API 网关（Spring Cloud Gateway）
│   └── filter/JwtAuthFilter.java      # JWT 认证过滤器
│
├── enstud-user-service/               # 用户服务（注册、登录、个人信息、积分等级、学习目标、关注）
├── enstud-word-service/               # 单词服务（词库、学习记录、记忆算法、错词本）
├── enstud-chat-service/               # AI对话服务（聊天、语法纠正、对话历史、话题推荐）
├── enstud-writing-service/            # 写作服务（作文提交、智能批改、范文）
├── enstud-translate-service/          # 翻译服务（文本/语音/图片翻译）
├── enstud-forum-service/              # 论坛服务（帖子、回复、点赞、搜索、标签）
├── enstud-read-service/               # 阅读服务（英文文章、阅读查词联动）
│
├── enstud-frontend/                   # 前端（Vite + React + TypeScript + Ant Design）
│   └── src/
│       ├── api/                       # API 请求封装（按模块拆分：user/word/chat/forum/reading 等）
│       ├── pages/                     # 页面组件（HomePage, WordPage, ChatPage, WritingPage, TranslatePage, ReadPage, ForumPage, DashboardPage 等）
│       ├── components/                # 通用组件（MainLayout 等）
│       ├── types/                     # TypeScript 类型定义
│       ├── utils/                     # 工具函数
│       └── assets/                    # 静态资源
│
├── client/                            # 前端旧目录（已废弃，新代码请使用 enstud-frontend）
│
├── nginx/                             # Nginx 配置（反向代理 + 前端静态资源）
│   ├── nginx.conf                     # 开发环境配置
│   ├── nginx.prod.conf                # 生产环境配置
│   └── Dockerfile
│
├── sql/                               # 数据库脚本
│   ├── init/                          # 初始化 SQL（首次建表）
│   ├── migrations/                    # 增量迁移脚本（V1__xxx.sql 命名）
│   ├── v1.1.0-memory-level.sql        # 版本升级脚本
│   └── v1.2.0-read-articles.sql
│
├── scripts/                           # 运维脚本
│   ├── deploy.sh                      # 生产部署脚本
│   ├── release.sh                     # 发布脚本
│   ├── rollback.sh                    # 回滚脚本
│   ├── health-check.sh                # 健康检查
│   ├── seed_words.py / seed_words.sql # 词库种子数据
│   ├── e2e_test.sh / e2e_*.sh         # E2E 测试脚本
│   └── e2e_test_py.py                 # Python E2E 测试
│
├── docs/                              # 详细文档
│   ├── guides/                        # 开发指南
│   │   ├── coding-standards.md        # 编码规范
│   │   ├── environment-setup.md       # 环境配置
│   │   ├── api-verification.md        # API 验证规范
│   │   ├── git-workflow.md            # Git 工作流
│   │   ├── frontend-guide.md          # 前端开发指南
│   │   └── ai-one-person-company.md   # AI 一人公司开发指南
│   ├── design-docs/                   # 设计文档
│   │   ├── database-schema.sql        # 完整数据库 Schema
│   │   ├── api-design.md              # API 设计文档
│   │   ├── database-changelog.md      # 数据库变更记录
│   │   └── feature-enhancements-v2.md # 功能增强设计（v2）
│   └── deployment/                    # 部署相关
│       ├── RELEASE.md                 # 发布说明
│       ├── cicd.md                    # CI/CD 流程
│       ├── deployment.md              # 部署指南
│       └── nginx.md                   # Nginx 配置说明
│
├── nacos-config/                      # Nacos 配置文件（导入用，目前为空）
├── e2e/                               # E2E 测试输出目录（gitignore）
├── logs/                              # 日志输出目录（gitignore）
```

---

## 环境配置

- 环境变量文件：项目根目录 `.env`（不提交 Git）
- 环境变量模板：`.env.example`

**端口规划：**

| 服务 | 端口 | 说明 |
|------|------|------|
| Nginx | 80 | 前端统一入口 |
| Gateway | 8080 | API 网关 |
| user-service | 8081 | 用户服务 |
| word-service | 8082 | 单词服务 |
| chat-service | 8083 | AI对话服务 |
| writing-service | 8084 | 写作服务 |
| translate-service | 8085 | 翻译服务 |
| forum-service | 8086 | 论坛服务 |
| read-service | 8087 | 阅读服务 |
| Nacos | 8848 | 注册/配置中心 |
| MySQL | 13306 | 数据库（Docker） |
| Redis | 6379 | 缓存 |

---

## 快速命令

### 构建

```bash
mvn clean install -DskipTests                          # 编译所有模块
mvn clean compile -pl enstud-user-service              # 编译单个模块
```

### 启动（本地开发）

```bash
# 1. 启动基础设施
docker-compose up -d mysql redis nacos

# 2. 启动单个微服务
cd enstud-user-service && mvn spring-boot:run

# 3. 启动前端
cd enstud-frontend && npm install && npm run dev
```

### 启动（Docker 一键）

```bash
cp .env.example .env    # 首次：复制环境变量
docker-compose up -d    # 启动所有服务
docker-compose ps       # 查看状态
docker-compose logs -f <service>  # 查看日志
docker-compose down     # 停止所有
```

### 生产部署

```bash
# 使用 prod 覆盖配置（拉取预构建镜像，不本地构建）
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# 或使用发布脚本
./scripts/release.sh
./scripts/deploy.sh
```

### 测试

```bash
mvn test                # 后端单元测试
cd enstud-frontend && npm run lint  # 前端 lint
```

---

## 编码规范（硬性规则）

### 必须遵守

1. **依赖注入**：使用 `@RequiredArgsConstructor` + `private final`，禁止 `@Autowired` 字段注入
2. **统一响应**：所有 API 返回 `Result<T>`，成功码 `0`
3. **分页**：使用 `PageResult<T>` 游标分页，禁止 Offset 分页
4. **DTO 校验**：入参使用 Java Record + Jakarta Validation（`@Valid`）
5. **异常处理**：业务异常使用 `BusinessException`，全局由 `GlobalExceptionHandler` 拦截
6. **用户上下文**：获取当前用户使用 `SecurityContext.getCurrentUserId()`
7. **日志**：使用 `@Slf4j` + 参数化日志，禁止字符串拼接
8. **API 文档**：Controller 必须添加 `@Tag` / `@Operation` / `@Schema` 注解
9. **事务**：`@Transactional(rollbackFor = Exception.class)` 声明在 Service 层

### 禁止项

- ❌ Controller 中直接 try-catch（交给全局异常处理）
- ❌ Controller 直接注入 Mapper（必须经过 Service 层）
- ❌ 直接返回实体类（必须使用 DTO）
- ❌ 硬编码密码、密钥、数据库连接串
- ❌ 在 SQL 中使用字符串拼接（使用 MyBatis Plus 参数绑定）
- ❌ 前端使用 `any` 类型

> 详细编码规范：[docs/guides/coding-standards.md](docs/guides/coding-standards.md)

---

## 分层架构

每个微服务遵循 Controller → Service → Mapper 三层架构，禁止跨层调用：

```
Controller (API 入口)
    ↓ 只能调用
Service (业务逻辑)
    ↓ 只能调用
Mapper (数据访问)
```

- 实体类统一放在 `enstud-common/entity/`，各服务共享
- 跨服务调用使用 `enstud-common/feign/` 中的 OpenFeign 客户端
- Gateway 使用 WebFlux（非 Servlet），注意不要引入 Servlet 依赖到 Gateway

---

## 验证规范

改完代码不算完成，必须验证接口可用。

**基本验证流程：**
1. 编译通过：`mvn clean compile`
2. 服务启动正常：检查 actuator/health
3. curl 调接口验证返回值

---

## 文档导航

| 需要了解 | 去哪里 |
|---------|--------|
| 编码规范 | [docs/guides/coding-standards.md](docs/guides/coding-standards.md) |
| 环境配置 | [docs/guides/environment-setup.md](docs/guides/environment-setup.md) |
| API 验证 | [docs/guides/api-verification.md](docs/guides/api-verification.md) |
| Git 工作流 | [docs/guides/git-workflow.md](docs/guides/git-workflow.md) |
| 前端开发 | [docs/guides/frontend-guide.md](docs/guides/frontend-guide.md) |
| AI 一人公司开发 | [docs/guides/ai-one-person-company.md](docs/guides/ai-one-person-company.md) |
| 完整数据库 Schema | [docs/design-docs/database-schema.sql](docs/design-docs/database-schema.sql) |
| API 设计文档 | [docs/design-docs/api-design.md](docs/design-docs/api-design.md) |
| 数据库变更记录 | [docs/design-docs/database-changelog.md](docs/design-docs/database-changelog.md) |
| 功能增强设计（v2） | [docs/design-docs/feature-enhancements-v2.md](docs/design-docs/feature-enhancements-v2.md) |
| 发布说明 | [docs/deployment/RELEASE.md](docs/deployment/RELEASE.md) |
| CI/CD 流程 | [docs/deployment/cicd.md](docs/deployment/cicd.md) |
| 部署指南 | [docs/deployment/deployment.md](docs/deployment/deployment.md) |
| Nginx 配置 | [docs/deployment/nginx.md](docs/deployment/nginx.md) |

# 环境配置指南

> EnStudApp 开发环境搭建完整流程

## 前置要求

### 必装软件

| 软件 | 版本要求 | 用途 |
|------|---------|------|
| JDK | 17+ | 后端编译运行 |
| Maven | 3.8+ | 依赖管理 & 构建 |
| Node.js | 18+ | 前端开发 |
| Docker & Docker Compose | 最新版 | 运行基础设施 |
| Git | 最新版 | 版本控制 |

### 验证安装

```bash
java -version          # 应显示 17.x
mvn -version           # Maven 版本
node -v                # v18+
npm -v                 # npm 版本
docker --version       # Docker 版本
docker-compose --version  # Compose 版本
```

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/EnStudApp/EnStudApp.git
cd EnStudApp
```

### 2. 配置环境变量

```bash
cp .env.example .env
```

编辑 `.env` 文件，根据本地环境调整端口（通常使用默认值即可）：

```bash
# 数据库配置
MYSQL_ROOT_PASSWORD=root
MYSQL_PORT=13306

# Redis
REDIS_PORT=6379

# Nacos
NACOS_PORT=8848
NACOS_GRPC_PORT=9848

# API 网关
GATEWAY_PORT=8080

# 微服务端口（通常保持默认）
USER_SERVICE_PORT=8081
WORD_SERVICE_PORT=8082
CHAT_SERVICE_PORT=8083
WRITING_SERVICE_PORT=8084
TRANSLATE_SERVICE_PORT=8085
FORUM_SERVICE_PORT=8086
READ_SERVICE_PORT=8087

# JWT 密钥（开发环境可用默认值，生产环境必须修改）
JWT_SECRET=your-jwt-secret-key-change-in-production

# AI 服务配置（可选，根据实际使用的 AI 提供商配置）
OPENAI_API_KEY=
OPENAI_BASE_URL=https://api.openai.com/v1
DEEPL_API_KEY=
```

### 3. 启动基础设施

```bash
# 启动 MySQL、Redis、Nacos
docker-compose up -d mysql redis nacos

# 验证服务状态
docker-compose ps
```

等待 30 秒后，访问 Nacos 控制台：http://localhost:8848/nacos

- 用户名：`nacos`
- 密码：`nacos`

### 4. 导入 Nacos 配置

1. 登录 Nacos 控制台
2. 进入「配置管理」→「配置列表」
3. 点击「导入配置」，选择 `nacos-config/` 目录下的配置文件
4. 确认所有配置已导入

### 5. 数据库初始化

```bash
# 运行数据库迁移脚本
bash scripts/run-migrations.sh

# （可选）导入单词数据
python scripts/seed_words.py
# 或
mysql -h127.0.0.1 -P13306 -uroot -proot enstud < scripts/seed_words.sql
```

### 6. 编译后端

```bash
# 编译所有模块（跳过测试）
mvn clean install -DskipTests

# 或编译单个模块
mvn clean compile -pl enstud-user-service
```

### 7. 启动微服务

**方式一：逐个启动（开发推荐）**

```bash
# 终端 1：启动用户服务
cd enstud-user-service
mvn spring-boot:run

# 终端 2：启动单词服务
cd enstud-word-service
mvn spring-boot:run

# 依次启动其他服务...
```

**方式二：一键启动所有服务**

```bash
docker-compose up -d
```

### 8. 启动前端

```bash
cd enstud-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

访问：http://localhost:5173

## 验证环境

### 检查后端服务

```bash
# 检查用户服务健康状态
curl http://localhost:8081/actuator/health

# 检查网关
curl http://localhost:8080/actuator/health
```

应返回：
```json
{"status":"UP"}
```

### 检查前端

浏览器访问 http://localhost:5173，应看到登录页面。

### 测试 API

```bash
# 注册用户
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123","email":"test@example.com"}'

# 登录获取 JWT Token
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123"}'
```

## 常见问题

### 1. Docker 启动失败

**问题：** `Cannot connect to the Docker daemon`

**解决：**
```bash
# 启动 Docker Desktop（Windows/Mac）
# 或启动 Docker 服务（Linux）
sudo systemctl start docker
```

### 2. 端口被占用

**问题：** `Port 8080 is already in use`

**解决：** 修改 `.env` 中的端口配置，或关闭占用端口的程序：

```bash
# Windows 查看端口占用
netstat -ano | findstr :8080

# Linux/Mac 查看端口占用
lsof -i :8080
```

### 3. Maven 编译失败

**问题：** `Could not resolve dependencies`

**解决：**
```bash
# 清理本地缓存
mvn clean

# 强制更新依赖
mvn clean install -U -DskipTests
```

### 4. Nacos 连接失败

**问题：** `Unable to connect to Nacos`

**解决：**
1. 检查 Nacos 容器是否运行：`docker-compose ps nacos`
2. 等待 Nacos 完全启动（约 30 秒）
3. 检查 `.env` 中的 Nacos 配置是否正确

### 5. 前端 npm install 失败

**问题：** `npm ERR! network timeout`

**解决：**
```bash
# 使用国内镜像
npm config set registry https://registry.npmmirror.com

# 或清除缓存重试
npm cache clean --force
npm install
```

### 6. 数据库连接失败

**问题：** `Communications link failure`

**解决：**
1. 检查 MySQL 容器：`docker-compose ps mysql`
2. 查看日志：`docker-compose logs mysql`
3. 确认 `.env` 中的数据库配置正确

## 开发环境端口总览

| 服务 | 端口 | 访问地址 |
|------|------|---------|
| 前端 | 5173 | http://localhost:5173 |
| API 网关 | 8080 | http://localhost:8080 |
| 用户服务 | 8081 | http://localhost:8081 |
| 单词服务 | 8082 | http://localhost:8082 |
| 聊天服务 | 8083 | http://localhost:8083 |
| 写作服务 | 8084 | http://localhost:8084 |
| 翻译服务 | 8085 | http://localhost:8085 |
| 论坛服务 | 8086 | http://localhost:8086 |
| 阅读服务 | 8087 | http://localhost:8087 |
| Nacos | 8848 | http://localhost:8848/nacos |
| MySQL | 13306 | 127.0.0.1:13306 |
| Redis | 6379 | 127.0.0.1:6379 |

## 下一步

- 阅读 [编码规范](./coding-standards.md)
- 了解 [API 验证流程](./api-verification.md)
- 查看 [Git 工作流](./git-workflow.md)

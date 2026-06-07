# EnStudApp - 英语学习助手

一款免费的英语学习应用，集单词学习、AI英语聊天、写作批改、翻译和社区交流于一体。

## 技术栈

- **后端**：Spring Boot 3.x + Spring Cloud Alibaba + MyBatis-Plus
- **前端**：React + TypeScript + Ant Design Mobile + Vite
- **数据库**：MySQL 8.0 + Redis + Elasticsearch
- **基础设施**：Docker Compose + Nginx + GitHub Actions

## 快速开始

```bash
# 1. 克隆项目
git clone https://github.com/your-org/EnStudApp.git
cd EnStudApp

# 2. 配置环境变量
cp .env.example .env

# 3. 启动基础设施
docker-compose up -d mysql redis nacos

# 4. 启动后端服务
mvn clean install -DskipTests
cd enstud-user-service && mvn spring-boot:run

# 5. 启动前端
cd client && npm install && npm run dev
```

## 项目结构

```
EnStudApp/
├── enstud-common/           # 公共模块
├── enstud-gateway/          # API 网关
├── enstud-user-service/     # 用户服务
├── enstud-word-service/     # 单词学习服务
├── enstud-chat-service/     # AI对话服务
├── enstud-writing-service/  # 写作服务
├── enstud-translate-service/# 翻译服务
├── enstud-forum-service/    # 论坛服务
├── client/                  # 前端项目
├── sql/                     # 数据库脚本
├── docs/                    # 项目文档
└── nginx/                   # Nginx 配置
```

## 开发规范

开发前请务必阅读 [开发规范文档](docs/guides/coding-standards.md)，所有开发任务必须遵守规范。

## License

MIT

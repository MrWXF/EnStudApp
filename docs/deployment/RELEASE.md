# EnStudApp 投产发布流程

> 完整的版本管理、构建、发布、部署、回滚流程

---

## 版本号管理

遵循 [语义化版本](https://semver.org/lang/zh-CN/)：`MAJOR.MINOR.PATCH`

| 类型 | 示例 | 触发条件 |
|------|------|---------|
| MAJOR | v2.0.0 | 不兼容的 API 变更 |
| MINOR | v1.2.0 | 向下兼容的功能新增 |
| PATCH | v1.2.1 | 向下兼容的 Bug 修复 |

---

## 快速发布（一键）

```bash
./scripts/release.sh
```

脚本自动完成：
1. 检查 git 状态（无未提交变更，在 main 分支）
2. 运行 `mvn clean verify` + 前端 lint/test
3. 提示输入版本号
4. 更新 CHANGELOG.md
5. `git tag` + `git push`（触发 CD 流水线）

---

## 发布流程（手动 7 步）

### 1. 开发 → PR

```bash
git checkout develop
git pull origin develop
git checkout -b feature/my-feature

git add .
git commit -m "feat(word): 实现xxx功能"
git push origin feature/my-feature
```

### 2. 创建 PR → develop

在 GitHub 创建 Pull Request：`feature/my-feature` → `develop`

CI 自动执行：
- 后端编译 + Checkstyle + 测试 + JaCoCo 覆盖率
- 前端 tsc 检查 + ESLint + Vitest 测试
- 依赖漏洞扫描（OWASP）

**PR 合并条件：** CI 全部通过 + 至少 1 人 Code Review 通过

### 3. 合并到 main

```bash
git checkout main
git pull origin main
git merge develop
git push origin main
```

`main` 分支 CI 额外执行：**集成冒烟测试**（启动 MySQL + Redis + user-service，验证注册/登录 API）

### 4. 更新 CHANGELOG

编辑 `CHANGELOG.md`，在 `[Unreleased]` 下添加本次变更。

### 5. 打 Tag → 触发 CD

```bash
git checkout main
git pull origin main

git tag -a v1.3.0 -m "feat: 阅读查词联动, perf: 首页加载优化"
git push origin v1.3.0
```

**Tag 推送后 CD 流水线自动执行：**

```
┌─────────────────────────────────────────────────┐
│ 1. Build — mvn package + npm build               │
├─────────────────────────────────────────────────┤
│ 2. Docker — 并行构建 9 个镜像                    │
│    → 推送到 ghcr.io (ghcr.io/<org>/<svc>:v1.3.0) │
├─────────────────────────────────────────────────┤
│ 3. Deploy — SSH 到服务器                         │
│    ├── DB 备份 (mysqldump) + 保留 7 个             │
│    ├── DB 迁移 (sql/migrations/*.sql 增量执行)     │
│    ├── docker compose pull (拉取镜像)              │
│    ├── docker compose up -d --no-build (滚动重启)  │
│    ├── 健康检查 gateway:8080 / nginx:80            │
│    ├── 失败自动回滚到上一版本                       │
│    ├── 写入 .version 文件记录当前版本               │
│    └── docker image prune (清理 24h 外旧镜像)      │
├─────────────────────────────────────────────────┤
│ 4. Webhook 通知 (需配置 DEPLOY_WEBHOOK_URL)       │
└─────────────────────────────────────────────────┘
```

### 6. 验证发布

```bash
# 核心 API 验证
curl -sf http://your-server/api/user/register \
  -H "Content-Type: application/json" \
  -d '{"username":"check","password":"Check@1234","email":"check@test.com"}'

# 健康检查
curl http://your-server:8080/actuator/health

# Prometheus 指标
curl http://your-server:8080/actuator/prometheus

# 检查容器状态
ssh deploy@your-server 'docker compose ps'
```

---

## 手动部署

```bash
# 在服务器上
cd /opt/enstud

# 方式 1：使用部署脚本（推荐）
./scripts/deploy.sh v1.3.0          # 完整流程：备份 → 迁移 → 部署 → 健康检查
./scripts/deploy.sh v1.3.0 --dry-run # 预览模式

# 方式 2：直接 docker compose
export IMAGE_TAG=v1.3.0
docker compose -f docker-compose.yml -f docker-compose.prod.yml pull
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --remove-orphans --no-build

# 查看状态
docker compose ps
docker compose logs -f --tail=50 gateway
```

---

## 数据库迁移

迁移脚本存放于 `sql/migrations/`，采用 Flyway 兼容命名：`V{序号}__{描述}.sql`

| 脚本 | 说明 |
|------|------|
| `V1__create_chat_tables.sql` | AI 对话相关表 |
| `V2__create_writing_table.sql` | 写作批改相关表 |
| `V3__add_streak_fields.sql` | 连续学习天数字段 |
| `V4__add_user_goal_tables.sql` | 用户学习目标表 |
| `V5__add_forum_tag_tables.sql` | 论坛标签表 |
| `V6__add_user_follow_table.sql` | 用户关注表 |
| `V7__add_wrong_count_field.sql` | 错词本字段 |
| `V8__add_user_follow_counts.sql` | 用户关注计数字段 |

**新增迁移：**
1. 在 `sql/migrations/` 下创建 `V4__描述.sql`
2. 脚本必须幂等（`CREATE TABLE IF NOT EXISTS` / `INSERT IGNORE`）
3. 部署时自动按序号执行

---

## 回滚流程

```bash
# 自动回滚：CD 健康检查失败时自动回滚到上一版本

# 手动回滚方式 1：使用脚本
./scripts/rollback.sh v1.2.9             # 需确认
./scripts/rollback.sh v1.2.9 --force     # 跳过确认 (CI/CD)

# 手动回滚方式 2：直接指定版本
IMAGE_TAG=v1.2.9 docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --remove-orphans --no-build

# 查看可用版本
docker image ls | grep enstudapp/user-service
```

---

## HTTPS 配置

1. 获取证书（推荐 Let's Encrypt）：
```bash
certbot certonly --webroot -w /var/www/certbot -d your-domain.com
```

2. 将证书放到 `./certs/` 目录：
```
certs/
├── fullchain.pem
└── privkey.pem
```

3. 编辑 `nginx/nginx.prod.conf`，替换 `${DOMAIN}` 为实际域名。

4. 部署时自动加载 HTTPS 配置（通过 `nginx/nginx.prod.conf` 挂载）。

---

## CI/CD 流水线全景

```
PR (feature → develop)
│
├── Backend Build & Test
│   ├── mvn compile (Checkstyle validate 阶段)
│   ├── mvn verify (JUnit + JaCoCo coverage check)
│   ├── Upload test reports
│   ├── Upload coverage reports (JaCoCo)
│   └── Upload Checkstyle reports
│
├── Frontend Build & Lint
│   ├── npm ci + tsc --noEmit
│   ├── npm run build
│   ├── npm run lint (失败阻断构建)
│   └── npm test (Vitest + MSW)
│
└── Vulnerability Scan
    └── mvn dependency-check:check (CVSS ≥ 7 fail, continue-on-error)

Merge to main — 额外执行：
│
└── Integration Test (API Smoke)
    ├── Start MySQL + Redis
    ├── Start user-service
    └── curl 验证 register + login

Git Tag v* → CD:
│
├── Build All Artifacts (JAR + dist)
├── Docker Build & Push × 9 (parallel)
│   user-service / word-service / chat-service / writing-service
│   translate-service / forum-service / read-service / gateway / nginx
│   → ghcr.io/<org>/<svc>:<version> + :latest
├── Deploy to Production (SSH)
│   ├── DB 备份 (mysqldump → backups/, 保留 7 天)
│   ├── DB 迁移 (sql/migrations/)
│   ├── docker compose pull + up -d --no-build
│   ├── 健康检查 gateway:8080/actuator/health + nginx:80/
│   ├── 失败自动回滚 → PREV_VERSION
│   └── .version 记录 + 清理旧镜像
└── Webhook 通知 (DEPLOY_WEBHOOK_URL)
```

---

## 环境变量与密钥

| 变量 | 用途 | 配置位置 |
|------|------|---------|
| `JWT_SECRET` | JWT 签名密钥 | `.env`（生产） |
| `MYSQL_ROOT_PASSWORD` | 数据库密码 | `.env`（生产） |
| `DEEPSEEK_API_KEY` | AI 对话 API Key | `.env`（生产） |
| `GHCR_REPO` | GitHub Container Registry 组织名 | `.env`（生产，默认 enstudapp） |
| `IMAGE_TAG` | 部署版本号 | 部署时环境变量（默认 latest） |
| `SSL_CERTS_DIR` | SSL 证书目录 | `.env`（生产，默认 ./certs） |
| `NVD_API_KEY` | OWASP 漏洞数据库 API | GitHub Secrets |
| `DEPLOY_HOST` | 生产服务器 IP | GitHub Secrets → Environment: production |
| `DEPLOY_USER` | 服务器 SSH 用户 | GitHub Secrets → Environment: production |
| `DEPLOY_SSH_KEY` | 服务器 SSH 私钥 | GitHub Secrets → Environment: production |
| `DEPLOY_WEBHOOK_URL` | 部署通知 Webhook | GitHub Secrets（可选） |

### GitHub 配置清单

1. **Settings → Secrets and variables → Actions**：
   - `NVD_API_KEY` — NVD API Key（可选，提速漏洞扫描）

2. **Settings → Environments → New Environment (`production`)**：
   - 添加 Protection Rules（如需要审批）
   - 添加 Environment Secrets：
     - `DEPLOY_HOST` — 服务器 IP
     - `DEPLOY_USER` — SSH 用户名
     - `DEPLOY_SSH_KEY` — SSH 私钥
     - `DEPLOY_WEBHOOK_URL` — 通知 Webhook URL（可选）

3. **服务器准备**：
```bash
# 创建部署目录
mkdir -p /opt/enstud
cd /opt/enstud

# 放好 docker-compose.yml, docker-compose.prod.yml, .env

# 登录 GitHub Container Registry
echo "$GITHUB_TOKEN" | docker login ghcr.io -u <username> --password-stdin

# 确保基础设施网络存在
docker network create enstud-network 2>/dev/null || true
```

---

## 检查清单

每次发布前：

- [ ] `mvn clean verify` 本地通过
- [ ] `npm run lint && npm test` 前端通过
- [ ] CHANGELOG.md 已更新
- [ ] Git Tag 格式正确（`vMAJOR.MINOR.PATCH`）
- [ ] 生产环境 `.env` 文件密钥已配置
- [ ] 服务器 Docker 已登录 `ghcr.io`
- [ ] 数据库迁移脚本已添加至 `sql/migrations/`（如有）
- [ ] HTTPS 证书已放置（生产首次配置）

每次发布后：

- [ ] `curl <server>/actuator/health` 返回正常
- [ ] 前端页面正常访问
- [ ] 核心 API 正常响应
- [ ] `docker compose ps` 所有服务 healthy
- [ ] 备份文件已生成 (`backups/`)

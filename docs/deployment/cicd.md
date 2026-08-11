# EnStudApp CI/CD 流水线

> 基于 GitHub Actions 的持续集成与持续部署流水线，覆盖代码提交、构建、测试、镜像发布、生产部署全生命周期。

---

## 一、流水线全景

```
┌──────────────────────────────────────────────────────────────┐
│  PR (feature/* → develop)                                     │
│  ├── Backend CI: compile + Checkstyle + 单元测试 + 覆盖率       │
│  ├── Frontend CI: tsc + ESLint + Vitest                       │
│  └── Security Scan: OWASP dependency-check                    │
├──────────────────────────────────────────────────────────────┤
│  Merge → main                                                 │
│  └── 额外执行集成冒烟测试（启动 MySQL/Redis/user-service）       │
├──────────────────────────────────────────────────────────────┤
│  Git Tag (v*) → CD                                            │
│  ├── Build Artifacts（JAR + dist）                            │
│  ├── Docker Build × 9（并行，推送 ghcr.io）                     │
│  ├── Deploy to Production（SSH：备份 → 迁移 → 拉镜像 → 重启）   │
│  ├── 健康检查 + 失败自动回滚                                    │
│  └── Webhook 通知（可选）                                      │
└──────────────────────────────────────────────────────────────┘
```

---

## 二、触发条件

| 事件 | 触发范围 | 行为 |
|------|---------|------|
| `pull_request` (→ `develop`) | 后端/前端/CI 配置变更 | 完整 CI |
| `push` (→ `main`) | 合并到主分支 | CI + 集成冒烟测试 |
| `push` (tags `v*`) | 发布标签 | CI + CD 全流程 |
| `workflow_dispatch` | 手动触发 | 按输入参数执行 |

---

## 三、CI 流水线

### 3.1 后端 CI

```yaml
steps:
  - mvn clean compile       # 触发 Checkstyle validate 阶段
  - mvn verify              # JUnit 单元测试 + JaCoCo 覆盖率检查
  - Upload test reports     # 测试报告归档
  - Upload coverage reports # JaCoCo 覆盖率报告
  - Upload Checkstyle reports
```

**失败条件：**
- 编译失败
- Checkstyle 违规
- 单元测试失败
- JaCoCo 覆盖率低于阈值

### 3.2 前端 CI

```yaml
steps:
  - npm ci                  # 锁定依赖安装
  - tsc --noEmit            # TypeScript 类型检查
  - npm run build           # Vite 构建
  - npm run lint            # ESLint（失败直接阻断）
  - npm test                # Vitest + MSW Mock
```

### 3.3 漏洞扫描

```yaml
steps:
  - mvn dependency-check:check
    # CVSS ≥ 7 视为高危，默认 continue-on-error
    # 可配置 NVD_API_KEY 提升查询速率
```

### 3.4 集成冒烟测试（仅 main 分支）

```yaml
services:
  mysql:8.0       # 端口 13306
  redis:7         # 端口 6379

steps:
  - mvn -pl enstud-user-service spring-boot:run &
  - 等待 /actuator/health 返回 UP
  - curl POST /api/user/register
  - curl POST /api/user/login
```

---

## 四、CD 流水线

### 4.1 构建产物

```
Build All Artifacts
  ├── mvn clean package -DskipTests   # 生成各模块 JAR
  └── npm run build (client/)         # 生成前端 dist/
```

### 4.2 镜像构建与推送（并行 × 9）

| 镜像 | 来源 | Tag |
|------|------|-----|
| `ghcr.io/<org>/user-service` | `enstud-user-service/Dockerfile` | `<version>` + `latest` |
| `ghcr.io/<org>/word-service` | `enstud-word-service/Dockerfile` | 同上 |
| `ghcr.io/<org>/chat-service` | `enstud-chat-service/Dockerfile` | 同上 |
| `ghcr.io/<org>/writing-service` | `enstud-writing-service/Dockerfile` | 同上 |
| `ghcr.io/<org>/translate-service` | `enstud-translate-service/Dockerfile` | 同上 |
| `ghcr.io/<org>/forum-service` | `enstud-forum-service/Dockerfile` | 同上 |
| `ghcr.io/<org>/read-service` | `enstud-read-service/Dockerfile` | 同上 |
| `ghcr.io/<org>/gateway` | `enstud-gateway/Dockerfile` | 同上 |
| `ghcr.io/<org>/nginx` | `nginx/Dockerfile` | 同上 |

> `<org>` 通过环境变量 `GHCR_REPO` 配置，默认 `enstudapp`。

**GHCR 认证：** 使用内置 `GITHUB_TOKEN`，无需额外 Secret。

### 4.3 部署到生产（SSH）

```bash
# 部署脚本 scripts/deploy.sh <version>
./scripts/deploy.sh v1.3.0
```

脚本内部步骤：

1. **数据库备份** — `mysqldump` → `backups/` 目录，保留最近 7 份
2. **数据库迁移** — 按序号执行 `sql/migrations/V*__*.sql`（需幂等）
3. **拉取新镜像** — `docker compose pull`
4. **滚动重启** — `docker compose up -d --remove-orphans --no-build`
5. **健康检查** — 轮询 `gateway:8080/actuator/health` 和 `nginx:80/`
6. **失败回滚** — 检查失败时自动回滚到 `PREV_VERSION`
7. **版本记录** — 写入 `.version` 文件
8. **清理旧镜像** — `docker image prune`（保留 24h 内镜像）

### 4.4 Webhook 通知

部署成功/失败后，向 `DEPLOY_WEBHOOK_URL` 发送 POST 通知（可选配置）。

---

## 五、环境分离

| 环境 | 触发 | 镜像来源 | 配置 |
|------|------|---------|------|
| **dev** | 本地 `docker compose up` | 本地构建 (`build:`) | `docker-compose.yml` |
| **staging** | 手动 / develop 分支 | ghcr.io `:staging` | 独立服务器 |
| **production** | `v*` tag | ghcr.io `:<version>` | `docker-compose.yml` + `docker-compose.prod.yml` |

### 环境差异

| 配置项 | dev | production |
|--------|-----|-----------|
| 镜像构建 | 本地 `docker build` | 拉取 ghcr.io |
| 重启策略 | `unless-stopped` | `always` |
| 资源限制 | 无 | CPU / 内存 limits |
| 日志驱动 | 默认 | `json-file`，50MB × 5 轮转 |
| Nginx 配置 | `nginx.conf` (HTTP) | `nginx.prod.conf` (HTTPS) |
| SSL 证书 | 不挂载 | `./certs/` 只读挂载 |

---

## 六、密钥与环境变量

### GitHub Secrets 配置

**Repository Secrets（全局）：**

| Secret | 用途 |
|--------|------|
| `NVD_API_KEY` | NVD 漏洞数据库 API Key（可选，提速扫描） |

**Environment Secrets（`production` 环境）：**

| Secret | 用途 |
|--------|------|
| `DEPLOY_HOST` | 生产服务器 IP / 域名 |
| `DEPLOY_USER` | SSH 用户名 |
| `DEPLOY_SSH_KEY` | SSH 私钥（Ed25519 推荐） |
| `DEPLOY_WEBHOOK_URL` | 部署通知 Webhook（可选） |

### 服务器 .env 文件

生产服务器必须放置 `.env` 文件（不提交 Git），包含：

```bash
# 基础设施密码
MYSQL_ROOT_PASSWORD=<strong-password>

# JWT 密钥（必须更换，不得沿用默认值）
JWT_SECRET=<random-64-chars>

# AI 服务
DEEPSEEK_API_KEY=sk-xxx
DEEPSEEK_BASE_URL=https://api.deepseek.com/v1

# 镜像仓库
GHCR_REPO=enstudapp
IMAGE_TAG=v1.3.0

# SSL 证书目录
SSL_CERTS_DIR=/opt/enstud/certs

# 端口（可选，默认值即可）
NGINX_PORT=80
GATEWAY_PORT=8080
# ... 其他端口参考 .env.example
```

---

## 七、回滚策略

### 7.1 自动回滚

CD 部署后健康检查失败时，自动回滚到上一版本：

```
Deploy NEW_VERSION
  → Health check fail
  → IMAGE_TAG=PREV_VERSION docker compose up -d --no-build
  → Re-check health
  → 通知 Webhook（REVERT 状态）
```

### 7.2 手动回滚

```bash
# 方式 1：使用脚本
./scripts/rollback.sh v1.2.9             # 需确认
./scripts/rollback.sh v1.2.9 --force     # 跳过确认（CI 调用）

# 方式 2：直接指定版本
export IMAGE_TAG=v1.2.9
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --remove-orphans --no-build
```

### 7.3 数据库回滚

迁移脚本必须**幂等**且**向后兼容**（仅 `ADD COLUMN IF NOT EXISTS`、`CREATE TABLE IF NOT EXISTS`），通常无需反向迁移。如需反向变更，需手工编写并执行。

---

## 八、GitHub 配置清单

首次配置 CI/CD 时逐项检查：

- [ ] **Settings → Actions → General**：Workflow permissions 设置为 `Read and write`
- [ ] **Settings → Secrets → Actions**：添加 `NVD_API_KEY`（可选）
- [ ] **Settings → Environments → New Environment**：创建 `production`
  - [ ] 添加 `DEPLOY_HOST` / `DEPLOY_USER` / `DEPLOY_SSH_KEY`
  - [ ] （可选）开启 Protection Rules，指定审批人
- [ ] **Packages**：确认 `Packages` 权限允许 Actions 发布
- [ ] **服务器**：`docker login ghcr.io` 已完成
- [ ] **服务器**：`docker network create enstud-network` 已创建
- [ ] **服务器**：`/opt/enstud/` 目录已放置 `docker-compose*.yml` + `.env`

---

## 九、常见问题

**Q：CI 一直 pending？**
A：检查 `.github/workflows/` 下 workflow 文件语法，确认分支名匹配 trigger rules。

**Q：镜像推送 403？**
A：确认 `GITHUB_TOKEN` 具有 `packages:write` 权限；私有仓库需在 Packages 页面手动授权 Actions 访问。

**Q：部署 SSH 失败？**
A：检查 `DEPLOY_SSH_KEY` 是否为完整私钥（含 `BEGIN OPENSSH PRIVATE KEY` 头尾）；确认 `DEPLOY_HOST` 防火墙放行 22 端口；确认 `DEPLOY_USER` 在 `authorized_keys` 中。

**Q：CD 健康检查失败但服务已启动？**
A：网关依赖所有下游服务 healthy，任一服务启动缓慢会导致整体失败。查看 `docker compose logs <service>` 定位启动慢的服务，适当增加健康检查间隔。

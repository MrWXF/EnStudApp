# EnStudApp 部署指南

> 从零开始部署 EnStudApp 的完整指南，涵盖开发环境与生产环境。

---

## 一、前提条件

### 1.1 服务器要求

| 环境 | CPU | 内存 | 磁盘 | 系统 |
|------|-----|------|------|------|
| **开发** | 2 核 | 4 GB | 20 GB | Linux / macOS / WSL2 |
| **生产** | 4 核+ | 8 GB+ | 50 GB+ SSD | Ubuntu 22.04 / Debian 12 |

### 1.2 软件依赖

| 软件 | 最低版本 | 说明 |
|------|---------|------|
| Docker | 20.10+ | 容器运行时 |
| Docker Compose | v2.20+ | 服务编排（`docker compose` 子命令） |
| Git | 2.30+ | 代码拉取 |
| curl | — | 健康检查 |

### 1.3 基础设施（独立运行）

以下基础设施**不在** `docker-compose.yml` 编排范围内，需提前独立部署：

| 组件 | 默认端口 | 说明 |
|------|---------|------|
| MySQL 8.0 | 13306 | 数据库 |
| Redis 7.x | 6379 | 缓存 |
| Nacos 2.2.3 | 8848 (gRPC 9848) | 注册/配置中心 |

启动基础设施：

```bash
docker-compose up -d mysql redis nacos
```

---

## 二、首次部署（开发环境）

### 2.1 克隆代码

```bash
git clone https://github.com/<your-org>/EnStudApp.git
cd EnStudApp
```

### 2.2 配置环境变量

```bash
cp .env.example .env
# 编辑 .env，按需修改端口和密钥
```

开发环境可直接使用默认值；**生产环境必须更换** `JWT_SECRET` 和 `MYSQL_ROOT_PASSWORD`。

### 2.3 数据库初始化

```bash
# 1. 执行初始化 Schema
docker exec -i enstud-mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" enstud_app < sql/init/01-schema.sql

# 2. 执行增量迁移（首次部署需全量执行）
for f in sql/migrations/V*.sql; do
  docker exec -i enstud-mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" enstud_app < "$f"
done

# 或使用一键脚本
bash scripts/run-migrations.sh
```

### 2.4 导入词库种子数据（可选）

```bash
# SQL 方式（快速）
docker exec -i enstud-mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" enstud_app < scripts/seed_words.sql

# Python 方式（灵活，支持自定义）
python3 scripts/seed_words.py
```

### 2.5 构建并启动服务

```bash
# 方式 1：本地构建（开发，首次较慢）
docker compose up -d --build

# 方式 2：仅启动已构建镜像
docker compose build
docker compose up -d
```

### 2.6 验证部署

```bash
# 查看所有容器状态
docker compose ps

# 期望输出：所有服务状态为 healthy 或 running

# 网关健康检查
curl http://localhost:8080/actuator/health
# 期望：{"status":"UP"}

# Nginx 入口
curl -I http://localhost/
# 期望：HTTP/1.1 200 OK

# 注册接口验证
curl -sf http://localhost/api/user/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@1234","email":"admin@test.com"}'
```

---

## 三、生产环境部署

### 3.1 服务器准备

```bash
# 创建部署目录
sudo mkdir -p /opt/enstud
sudo chown $USER:$USER /opt/enstud
cd /opt/enstud

# 创建网络（微服务通信）
docker network create enstud-network 2>/dev/null || true

# 拉取必要代码或配置文件
git clone https://github.com/<your-org>/EnStudApp.git .
```

### 3.2 配置生产环境变量

```bash
cp .env.example .env
```

**必须修改的变量：**

```bash
# .env
MYSQL_ROOT_PASSWORD=<随机强密码，至少 16 位>
JWT_SECRET=<随机 64 字符字符串>

# AI 服务密钥（按实际使用配置）
DEEPSEEK_API_KEY=sk-xxx
DEEPSEEK_BASE_URL=https://api.deepseek.com/v1

# 镜像仓库
GHCR_REPO=enstudapp
IMAGE_TAG=v1.0.0   # 当前部署版本

# SSL 证书目录
SSL_CERTS_DIR=/opt/enstud/certs
```

生成随机密钥：

```bash
# JWT_SECRET
openssl rand -base64 48

# MYSQL_ROOT_PASSWORD
openssl rand -base64 24
```

### 3.3 配置 HTTPS（推荐）

```bash
# 创建证书目录
mkdir -p /opt/enstud/certs

# 方式 1：Let's Encrypt（推荐）
sudo apt install certbot
sudo certbot certonly --standalone -d your-domain.com
sudo cp /etc/letsencrypt/live/your-domain.com/fullchain.pem /opt/enstud/certs/
sudo cp /etc/letsencrypt/live/your-domain.com/privkey.pem /opt/enstud/certs/

# 方式 2：已有证书，直接拷贝到 /opt/enstud/certs/

# 修改 nginx/nginx.prod.conf 中的 ${DOMAIN} 为实际域名
```

### 3.4 登录 GHCR 并拉取镜像

```bash
# 登录 GitHub Container Registry
echo "<YOUR_GITHUB_TOKEN>" | docker login ghcr.io -u <username> --password-stdin

# 拉取所有镜像
export IMAGE_TAG=v1.0.0
docker compose -f docker-compose.yml -f docker-compose.prod.yml pull
```

### 3.5 启动生产服务

```bash
# 使用生产 overlay（资源限制 + HTTPS + 日志轮转）
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --no-build

# 查看启动状态
docker compose ps
```

### 3.6 验证生产部署

```bash
# 健康检查
curl https://your-domain.com/actuator/health
curl https://your-domain.com/

# 查看关键服务日志
docker compose logs -f --tail=50 gateway
docker compose logs -f --tail=50 nginx
```

---

## 四、生产加固

### 4.1 资源限制

生产 overlay (`docker-compose.prod.yml`) 已配置资源限制：

| 服务 | CPU Limit | Memory Limit | CPU Reservation | Memory Reservation |
|------|-----------|--------------|-----------------|-------------------|
| 各微服务 | 1.0 | 512M | 0.5 | 256M |
| gateway | 1.0 | 512M | 0.5 | 256M |
| nginx | 0.5 | 256M | 0.25 | 128M |

### 4.2 日志轮转

生产环境所有服务使用 `json-file` 驱动，单文件 50MB，保留 5 份：

```yaml
logging:
  driver: json-file
  options:
    max-size: "50m"
    max-file: "5"
```

### 4.3 重启策略

生产环境所有服务设置为 `restart: always`，崩溃后自动重启。

### 4.4 安全加固

- **JWT 密钥**：必须使用强随机字符串，不得沿用 `.env.example` 默认值
- **MySQL 密码**：同上
- **SSH 密钥**：CD 部署使用 Ed25519 密钥，禁用密码登录
- **防火墙**：仅开放 80 / 443 / 22 端口，其余内部网络通信
- **Nginx 安全头**：生产配置已启用 HSTS / X-Content-Type-Options / X-Frame-Options
- **Actuator 端点**：生产环境仅允许 `127.0.0.1` 和 `172.20.0.0/16` 内网访问

---

## 五、数据库管理

### 5.1 目录结构

```
sql/
├── init/               # 初始化 Schema（首次部署使用）
│   └── 01-schema.sql
├── migrations/         # 增量迁移（Flyway 兼容命名）
│   ├── V1__create_chat_tables.sql
│   ├── V2__create_writing_table.sql
│   ├── V3__add_streak_fields.sql
│   ├── ...
│   └── V8__add_user_follow_counts.sql
└── run-migrations.sh   # 一键迁移脚本
```

### 5.2 执行迁移

```bash
# 一键迁移（推荐）
bash scripts/run-migrations.sh

# 手动迁移单文件
docker exec -i enstud-mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" enstud_app \
  < sql/migrations/V8__add_user_follow_counts.sql
```

### 5.3 迁移脚本编写规范

- 文件名：`V{序号}__{描述}.sql`（双下划线）
- 必须**幂等**：使用 `CREATE TABLE IF NOT EXISTS` / `ALTER TABLE ... ADD COLUMN IF NOT EXISTS` / `INSERT IGNORE`
- 部署时按序号自动排序执行
- 不得包含 `DROP TABLE` 等破坏性语句（除非经过评审）

### 5.4 备份

```bash
# 手动备份
docker exec enstud-mysql mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" \
  --single-transaction --routines --triggers enstud_app \
  | gzip > backups/enstud_$(date +%Y%m%d_%H%M%S).sql.gz

# CD 脚本自动备份：保留最近 7 份，存放于 backups/ 目录
```

**建议备份策略：**

| 类型 | 频率 | 保留 |
|------|------|------|
| 全量备份 | 每日 1 次 | 7 天 |
| 部署前备份 | 每次部署前 | 7 天 |
| binlog | 持续 | 3 天 |

---

## 六、监控与日志

### 6.1 健康检查

```bash
# 网关健康（聚合所有下游）
curl http://localhost:8080/actuator/health

# 各服务独立健康
curl http://localhost:8081/actuator/health  # user-service
curl http://localhost:8082/actuator/health  # word-service
# ... 其余服务类推

# 一键检查脚本
bash scripts/health-check.sh
```

### 6.2 Prometheus 指标

```bash
curl http://localhost:8080/actuator/prometheus
```

可对接 Grafana 进行可视化。

### 6.3 查看日志

```bash
# 查看所有服务日志
docker compose logs -f

# 查看单个服务
docker compose logs -f --tail=100 gateway
docker compose logs -f --tail=100 user-service

# 查看 Nginx 访问日志
docker exec enstud-nginx tail -f /var/log/nginx/access.log
```

### 6.4 性能脚本

```bash
# E2E 接口测试
bash scripts/e2e_test.sh

# Python 版本（更详细）
python3 scripts/e2e_test.py
```

---

## 七、日常运维

### 7.1 版本升级

```bash
cd /opt/enstud

# 方式 1：部署脚本（推荐，含备份 + 迁移 + 健康检查）
./scripts/deploy.sh v1.4.0
./scripts/deploy.sh v1.4.0 --dry-run   # 预览模式

# 方式 2：手动升级
export IMAGE_TAG=v1.4.0
docker compose -f docker-compose.yml -f docker-compose.prod.yml pull
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --remove-orphans --no-build
```

### 7.2 回滚

```bash
# 使用脚本（推荐）
./scripts/rollback.sh v1.3.0
./scripts/rollback.sh v1.3.0 --force   # 跳过确认

# 手动回滚
IMAGE_TAG=v1.3.0 docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --remove-orphans --no-build
```

### 7.3 清理旧镜像

```bash
# 清理 24h 外的悬空镜像
docker image prune -f --filter "until=24h"
```

### 7.4 重启单个服务

```bash
docker compose restart user-service
docker compose logs -f --tail=50 user-service
```

---

## 八、故障排查

| 现象 | 排查方向 |
|------|---------|
| 网关 503 | 下游服务未启动或未注册到 Nacos，查看 `docker compose ps` |
| Nginx 502 | Gateway 未就绪，检查 gateway 健康检查 |
| 服务启动慢 | JVM 初始化 + Nacos 注册，通常 1-3 分钟，增加健康检查超时 |
| MySQL 连接失败 | 确认 `enstud-network` 网络存在，MySQL 容器名为 `enstud-mysql` |
| 注册接口 401 | JWT_SECRET 与 gateway 不一致，检查 .env |
| 镜像拉取 401 | `docker login ghcr.io` 凭据过期，重新登录 |

---

## 九、部署检查清单

### 首次部署

- [ ] Docker + Docker Compose 已安装
- [ ] `enstud-network` Docker 网络已创建
- [ ] `.env` 文件已配置（生产密钥已更换）
- [ ] 基础设施（MySQL/Redis/Nacos）已启动
- [ ] 数据库 Schema + 迁移已执行
- [ ] （可选）词库种子数据已导入
- [ ] HTTPS 证书已配置（生产）
- [ ] `docker compose ps` 所有服务 healthy
- [ ] 核心 API 验证通过
- [ ] 前端页面正常加载

### 每次发布

- [ ] 数据库迁移脚本已添加到 `sql/migrations/`
- [ ] 备份已完成
- [ ] 镜像已拉取到服务器
- [ ] 部署脚本已执行
- [ ] 健康检查通过
- [ ] 旧镜像已清理

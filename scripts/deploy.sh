#!/bin/bash
# ============================================================
# EnStudApp 生产部署脚本
#
# 用法：
#   ./scripts/deploy.sh v1.2.3              # 部署指定版本
#   ./scripts/deploy.sh latest              # 部署最新版本
#   ./scripts/deploy.sh v1.2.3 --dry-run    # 预览模式（不实际部署）
#
# 前置条件：
#   - 服务器上已安装 Docker + Docker Compose
#   - 已 docker login ghcr.io（或配置 GitHub Token）
#   - 基础设施（MySQL/Redis/Nacos）已运行在 enstud-network
# ============================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_DIR"

VERSION="${1:-latest}"
DRY_RUN=0

# 解析参数
for arg in "$@"; do
    case "$arg" in
        --dry-run) DRY_RUN=1 ;;
    esac
done

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log()  { echo -e "${GREEN}[INFO]${NC}  $*"; }
warn() { echo -e "${YELLOW}[WARN]${NC}  $*"; }
err()  { echo -e "${RED}[ERROR]${NC} $*"; }

# ============================================================
# 0. 预检查
# ============================================================
log "检查环境..."

if [ "$DRY_RUN" -eq 1 ]; then
    warn "=== 预览模式：不会实际执行部署 ==="
fi

if ! command -v docker &>/dev/null; then
    err "Docker 未安装"
    exit 1
fi

if ! docker compose version &>/dev/null; then
    err "Docker Compose 插件未安装（需要 docker compose 命令）"
    exit 1
fi

# 记录当前版本（部署前的版本，失败时用它回滚）
PREV_VERSION=""
if [ -f .version ]; then
    PREV_VERSION=$(cat .version)
    log "当前版本: ${PREV_VERSION}"
else
    warn "未找到 .version 文件，无法自动回滚"
fi

# ============================================================
# 1. 数据库备份
# ============================================================
log "备份数据库..."

BACKUP_DIR="backups"
BACKUP_FILE="${BACKUP_DIR}/enstud_pre_deploy_$(date +%Y%m%d_%H%M%S).sql"

if [ "$DRY_RUN" -eq 0 ]; then
    mkdir -p "$BACKUP_DIR"
    if docker ps --format '{{.Names}}' | grep -q enstud-mysql; then
        MYSQL_PASS="${MYSQL_ROOT_PASSWORD:-root}"
        docker exec enstud-mysql mysqldump -uroot -p"${MYSQL_PASS}" \
            --single-transaction --routines --triggers enstud_app > "$BACKUP_FILE" 2>/dev/null
        log "  备份完成: ${BACKUP_FILE} ($(du -h "$BACKUP_FILE" | cut -f1))"
        # 保留最近 7 个备份
        ls -t ${BACKUP_DIR}/enstud_pre_deploy_*.sql 2>/dev/null | tail -n +8 | xargs rm -f
    else
        warn "  未找到 enstud-mysql 容器，跳过备份"
    fi
fi

# ============================================================
# 2. 数据库迁移
# ============================================================
log "检查数据库迁移..."

MIGRATIONS_DIR="sql/migrations"
if [ -d "$MIGRATIONS_DIR" ] && [ "$(ls -A "$MIGRATIONS_DIR"/*.sql 2>/dev/null)" ]; then
    if [ "$DRY_RUN" -eq 0 ]; then
        for f in $(ls "$MIGRATIONS_DIR"/*.sql 2>/dev/null | sort); do
            name=$(basename "$f")
            if docker ps --format '{{.Names}}' | grep -q enstud-mysql; then
                docker exec -i enstud-mysql mysql -uroot -p"${MYSQL_ROOT_PASSWORD:-root}" enstud_app < "$f" 2>/dev/null && \
                    log "  ✓ ${name}" || log "  ⚠ ${name} (可能已执行过)"
            fi
        done
    else
        warn "[DRY-RUN] 将执行 ${MIGRATIONS_DIR} 中的迁移脚本"
    fi
else
    warn "  无待执行的迁移脚本"
fi

# ============================================================
# 3. 拉取镜像
# ============================================================
log "拉取镜像 (版本: ${VERSION})..."

SERVICES=(user-service word-service chat-service writing-service translate-service forum-service gateway nginx)
GHCR_REPO="${GHCR_REPO:-enstudapp}"

for svc in "${SERVICES[@]}"; do
    image="ghcr.io/${GHCR_REPO}/${svc}:${VERSION}"
    log "  拉取 ${image} ..."
    if [ "$DRY_RUN" -eq 0 ]; then
        docker pull "$image"
    fi
done

log "所有镜像拉取完成"

# ============================================================
# 4. 滚动重启
# ============================================================
log "更新服务..."

COMPOSE_CMD="docker compose -f docker-compose.yml -f docker-compose.prod.yml"
export IMAGE_TAG="$VERSION"

if [ "$DRY_RUN" -eq 0 ]; then
    $COMPOSE_CMD up -d --remove-orphans --no-build
else
    warn "[DRY-RUN] ${COMPOSE_CMD} up -d --remove-orphans --no-build"
fi

# ============================================================
# 5. 健康检查
# ============================================================
log "等待服务就绪..."

HEALTH_CHECK() {
    local url=$1
    local name=$2
    local max_attempts="${3:-30}"
    local attempt=1

    while [ "$attempt" -le "$max_attempts" ]; do
        if curl -sf "$url" > /dev/null 2>&1; then
            log "  ✓ ${name} 就绪"
            return 0
        fi
        sleep 2
        attempt=$((attempt + 1))
    done
    err "  ✗ ${name} 启动超时"
    return 1
}

FAILURES=0

if [ "$DRY_RUN" -eq 0 ]; then
    HEALTH_CHECK "http://localhost:8080/actuator/health" "gateway" 30 || ((FAILURES++))
    HEALTH_CHECK "http://localhost:80/" "nginx" 10 || ((FAILURES++))
fi

# ============================================================
# 6. 失败回滚
# ============================================================
if [ "$FAILURES" -gt 0 ]; then
    err "部署失败 — ${FAILURES} 个服务未就绪"
    if [ -n "$PREV_VERSION" ]; then
        warn "自动回滚到上一版本: ${PREV_VERSION} ..."
        IMAGE_TAG="$PREV_VERSION" $COMPOSE_CMD up -d --remove-orphans --no-build
        warn "已回滚到: ${PREV_VERSION}"
    fi
    err "请检查日志: docker compose logs"
    exit 1
fi

# ============================================================
# 7. 标记版本 + 清理
# ============================================================
if [ "$DRY_RUN" -eq 0 ]; then
    echo "$VERSION" > .version
    if [ -n "$PREV_VERSION" ] && [ "$PREV_VERSION" != "$VERSION" ]; then
        echo "$PREV_VERSION" > .version.prev
    fi

    log "清理旧镜像（保留 24h 内）..."
    docker image prune -af --filter "until=24h" > /dev/null 2>&1

    log "========================================"
    log "  部署成功 — 版本: ${VERSION}"
    log "  访问: http://localhost:80"
    log "  API:  http://localhost:80/api/"
    log "  备份: ${BACKUP_FILE}"
    log "========================================"
fi

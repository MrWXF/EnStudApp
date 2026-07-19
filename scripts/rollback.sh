#!/bin/bash
# ============================================================
# EnStudApp 回滚脚本
#
# 用法：
#   ./scripts/rollback.sh v1.2.1              # 回滚到指定版本（需确认）
#   ./scripts/rollback.sh v1.2.1 --force      # 强制执行（CI/CD 用）
#   ./scripts/rollback.sh                     # 回滚到上一版本（从 .version.prev 读取）
#
# 原理：
#   Docker 镜像保留了历史版本标签，直接切换到旧版本即可
# ============================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_DIR"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log()  { echo -e "${GREEN}[INFO]${NC}  $*"; }
warn() { echo -e "${YELLOW}[WARN]${NC}  $*"; }
err()  { echo -e "${RED}[ERROR]${NC} $*"; }

VERSION="${1:-}"
FORCE=0

# 解析参数
for arg in "$@"; do
    case "$arg" in
        --force|-f) FORCE=1 ;;
    esac
done

# 未指定版本：尝试从 .version.prev 读取
if [ -z "$VERSION" ]; then
    if [ -f .version.prev ]; then
        VERSION=$(cat .version.prev)
        log "使用上一版本: ${VERSION}"
    else
        err "未找到 .version.prev，请手动指定版本号"
        echo ""
        echo "用法: $0 <版本号>"
        echo ""
        echo "示例:"
        echo "  docker image ls | grep enstudapp/user-service"
        echo "  ./scripts/rollback.sh v1.2.1"
        exit 1
    fi
fi

# 非强制模式：确认
if [ "$FORCE" -eq 0 ]; then
    log "确认回滚到版本: ${VERSION}"
    echo -n "输入 'yes' 确认: "
    read -r confirm
    if [ "$confirm" != "yes" ]; then
        warn "已取消回滚"
        exit 0
    fi
else
    log "强制回滚到版本: ${VERSION}（跳过确认）"
fi

COMPOSE_CMD="docker compose -f docker-compose.yml -f docker-compose.prod.yml"
export IMAGE_TAG="$VERSION"

# 记录当前版本到 .version.prev
if [ -f .version ]; then
    cp .version .version.prev
fi

log "切换到版本 ${VERSION} ..."
$COMPOSE_CMD up -d --remove-orphans --no-build

log "等待健康检查..."
sleep 15

FAILED=0

# Gateway
for i in $(seq 1 20); do
    if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
        log "✓ gateway 就绪"
        break
    fi
    if [ "$i" -eq 20 ]; then
        err "✗ gateway 未就绪"
        FAILED=1
    fi
    sleep 3
done

# Nginx
for i in $(seq 1 10); do
    if curl -sf http://localhost:80/ > /dev/null 2>&1; then
        log "✓ nginx 就绪"
        break
    fi
    if [ "$i" -eq 10 ]; then
        err "✗ nginx 未就绪"
        FAILED=1
    fi
    sleep 3
done

if [ "$FAILED" -eq 0 ]; then
    echo "$VERSION" > .version
    log "回滚成功 — 版本: ${VERSION}"
else
    err "回滚后部分服务未就绪，请检查日志: docker compose logs"
    exit 1
fi

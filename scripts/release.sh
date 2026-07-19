#!/bin/bash
# ============================================================
# EnStudApp 一键发布脚本
#
# 用法：
#   ./scripts/release.sh               # 交互式发布（推荐）
#   ./scripts/release.sh --dry-run     # 预览模式
#
# 自动完成：
#   1. 检查 git 状态（无未提交变更）
#   2. 检查是否在 main 分支
#   3. 运行 mvn clean verify（确保测试通过）
#   4. 提示输入版本号
#   5. 更新 CHANGELOG.md 中的 [Unreleased] 为指定版本
#   6. git tag + push（触发 CD）
# ============================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_DIR"

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

log()  { echo -e "${GREEN}[INFO]${NC}  $*"; }
warn() { echo -e "${YELLOW}[WARN]${NC}  $*"; }
err()  { echo -e "${RED}[ERROR]${NC} $*"; }

DRY_RUN=0
for arg in "$@"; do
    case "$arg" in
        --dry-run) DRY_RUN=1 ;;
    esac
done

if [ "$DRY_RUN" -eq 1 ]; then
    warn "=== 预览模式：不会执行任何实际操作 ==="
fi

# ==================== 1. 检查 git 状态 ====================
log "1/5 检查 Git 状态..."

if [ -n "$(git status --porcelain)" ]; then
    err "工作区有未提交的变更，请先提交或暂存"
    git status --short
    exit 1
fi

BRANCH=$(git branch --show-current)
if [ "$BRANCH" != "main" ]; then
    warn "当前分支: ${BRANCH}（不是 main）"
    echo -n "是否在 ${BRANCH} 分支上发布? [y/N] "
    read -r answer
    if [ "$answer" != "y" ] && [ "$answer" != "Y" ]; then
        log "已取消"
        exit 0
    fi
fi

# 确保是最新的
log "拉取最新代码..."
if [ "$DRY_RUN" -eq 0 ]; then
    git pull origin "$BRANCH"
fi

# ==================== 2. 运行测试 ====================
echo ""
log "2/5 运行测试..."

echo -n "是否运行完整测试? (mvn clean verify + npm test) [Y/n] "
read -r run_tests
if [ "$run_tests" != "n" ] && [ "$run_tests" != "N" ]; then
    if [ "$DRY_RUN" -eq 0 ]; then
        log "运行后端测试..."
        mvn clean verify -q || { err "后端测试失败"; exit 1; }

        log "运行前端测试..."
        cd enstud-frontend && npm ci --silent && npm run lint && npm test || { err "前端测试失败"; exit 1; }
        cd "$PROJECT_DIR"
    else
        warn "[DRY-RUN] mvn clean verify && npm test"
    fi
    log "全部测试通过 ✓"
else
    warn "跳过测试"
fi

# ==================== 3. 确定版本号 ====================
echo ""
log "3/5 确定版本号..."

# 从 git tag 获取最新版本
LATEST_TAG=$(git tag -l 'v*' --sort=-v:refname | head -1)
if [ -z "$LATEST_TAG" ]; then
    LATEST_TAG="v0.0.0"
fi
log "最新 tag: ${LATEST_TAG}"

echo -n "输入新版本号 (如 v1.3.0): "
read -r VERSION

if [ -z "$VERSION" ]; then
    err "版本号不能为空"
    exit 1
fi

# 校验格式
if ! echo "$VERSION" | grep -qE '^v[0-9]+\.[0-9]+\.[0-9]+$'; then
    err "版本号格式错误，应为 vMAJOR.MINOR.PATCH (如 v1.3.0)"
    exit 1
fi

# ==================== 4. 更新 CHANGELOG ====================
echo ""
log "4/5 更新 CHANGELOG..."

echo -n "本次发布有哪些变更? (简短描述): "
read -r changes_desc

if [ "$DRY_RUN" -eq 0 ]; then
    TODAY=$(date +%Y-%m-%d)
    # 在 [Unreleased] 和 ## [之间插入新版本
    if grep -q "## \[Unreleased\]" CHANGELOG.md; then
        sed -i.bak "/## \[Unreleased\]/a\\
\\
## [${VERSION#v}] — ${TODAY}\\
\\
- ${changes_desc}" CHANGELOG.md
        rm -f CHANGELOG.md.bak
        log "CHANGELOG.md 已更新"
    fi
else
    warn "[DRY-RUN] 更新 CHANGELOG.md: ${VERSION} — ${changes_desc}"
fi

# ==================== 5. Git Tag + Push ====================
echo ""
log "5/5 创建 Git Tag..."

TAG_MSG="release: ${VERSION} — ${changes_desc}"

log "Tag: ${VERSION}"
log "Message: ${TAG_MSG}"

if [ "$DRY_RUN" -eq 0 ]; then
    # 提交 CHANGELOG 更新
    git add CHANGELOG.md
    git commit -m "docs: update changelog for ${VERSION}" 2>/dev/null || true

    # 创建 tag
    git tag -a "$VERSION" -m "$TAG_MSG"

    echo ""
    echo -n "确认推送 tag 到远程仓库? [y/N] "
    read -r push_confirm
    if [ "$push_confirm" = "y" ] || [ "$push_confirm" = "Y" ]; then
        git push origin "$BRANCH"
        git push origin "$VERSION"
        log "Tag 已推送 — CD 流水线将自动触发"
        echo ""
        log "监控部署进度:"
        log "  https://github.com/${GITHUB_REPOSITORY:-your-org/enstudapp}/actions"
    else
        warn "已取消推送，tag 仅存在于本地"
        warn "手动推送: git push origin ${VERSION}"
    fi
else
    warn "[DRY-RUN] git tag -a ${VERSION} -m \"${TAG_MSG}\""
    warn "[DRY-RUN] git push origin ${VERSION}"
fi

echo ""
log "========================================"
log "  Release ${VERSION} 完成"
log "========================================"

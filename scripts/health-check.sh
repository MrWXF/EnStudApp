#!/bin/bash
# EnStudApp 健康检查脚本
# 检查 7 个微服务 + 1 个网关的 HTTP 状态
# 用法: ./scripts/health-check.sh [--quiet]

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost}"
SERVICES=(
  "API网关:8080/actuator/health"
  "用户服务:8081/actuator/health"
  "单词服务:8082/actuator/health"
  "AI对话服务:8083/actuator/health"
  "写作服务:8084/actuator/health"
  "翻译服务:8085/actuator/health"
  "论坛服务:8086/actuator/health"
  "阅读服务:8087/actuator/health"
)

QUIET=false
[[ "${1:-}" == "--quiet" ]] && QUIET=true

ALL_PASSED=true
PASS_COUNT=0
FAIL_COUNT=0

echo "========================================"
echo "  EnStudApp 服务健康检查"
echo "  $(date '+%Y-%m-%d %H:%M:%S')"
echo "========================================"

for entry in "${SERVICES[@]}"; do
  name="${entry%%:*}"
  port="${entry#*:}"
  path="${port#*/}"
  port="${port%%/*}"
  url="${BASE_URL}:${port}/${path}"

  if $QUIET; then
    if curl -sf -o /dev/null --max-time 5 "$url" 2>/dev/null; then
      ((PASS_COUNT++))
    else
      ((FAIL_COUNT++))
      ALL_PASSED=false
    fi
  else
    printf "  [%-16s] %s ... " "$name" "$url"
    if curl -sf -o /dev/null --max-time 5 "$url" 2>/dev/null; then
      echo "✅ UP"
      ((PASS_COUNT++))
    else
      echo "❌ DOWN"
      ((FAIL_COUNT++))
      ALL_PASSED=false
    fi
  fi
done

echo "========================================"
if $ALL_PASSED && [ "$FAIL_COUNT" -eq 0 ]; then
  echo "  结果: ✅ 全部通过 (${PASS_COUNT}/${PASS_COUNT} 服务正常运行)"
  exit 0
else
  echo "  结果: ❌ ${FAIL_COUNT} 个服务异常 (通过 ${PASS_COUNT}/${#SERVICES[@]})"
  # 如果有任何服务挂了，检查它们是否真的没启动，或 actuator 路径不同
  echo ""
  echo "  提示: 如果全部显示 DOWN，可能是因为:"
  echo "  1. 服务未启动 → 运行 docker-compose up -d"
  echo "  2. actuator 未暴露/mapped → 检查配置中 management.endpoints.web.base-path"
  echo "  3. 端口映射不同 → 调整 BASE_URL 变量"
  exit 1
fi

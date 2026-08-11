#!/bin/bash
# 启动可观测性服务脚本
# 用法: ./start-observability.sh

set -e

echo "🚀 开始启动可观测性服务..."
echo ""

# 镜像列表
IMAGES=(
    "docker.m.daocloud.io/prom/prometheus:v2.51.0|prom/prometheus:v2.51.0"
    "docker.m.daocloud.io/grafana/grafana:10.4.2|grafana/grafana:10.4.2"
    "docker.m.daocloud.io/grafana/loki:3.0.0|grafana/loki:3.0.0"
    "docker.m.daocloud.io/grafana/promtail:3.0.0|grafana/promtail:3.0.0"
)

# 检查并拉取镜像
for image_pair in "${IMAGES[@]}"; do
    IFS='|' read -r source target <<< "$image_pair"

    echo "📦 检查镜像: $target"
    if ! docker image inspect "$target" > /dev/null 2>&1; then
        echo "   下载中: $source"
        docker pull "$source"
        docker tag "$source" "$target"
        echo "   ✅ 下载完成"
    else
        echo "   ✅ 已存在"
    fi
    echo ""
done

# 启动服务
echo "🔧 启动服务..."
docker compose up -d prometheus grafana loki promtail

echo ""
echo "✅ 可观测性服务启动完成！"
echo ""
echo "📊 访问地址："
echo "   - Prometheus: http://localhost:9090"
echo "   - Grafana:    http://localhost:3000 (admin/admin)"
echo "   - Loki:       http://localhost:3100"
echo ""

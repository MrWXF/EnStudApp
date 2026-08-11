#!/bin/bash
# 可观测性服务镜像下载脚本
# 使用多个镜像源确保下载成功

set -e

echo "=========================================="
echo "  可观测性服务镜像下载工具"
echo "=========================================="
echo ""

# 镜像源列表
MIRRORS=(
    "docker.m.daocloud.io"
    "docker.1ms.run"
    "dockerproxy.com"
    "dockerhub.icu"
)

# 需要下载的镜像
IMAGES=(
    "prom/prometheus:v2.51.0"
    "grafana/grafana:10.4.2"
    "grafana/loki:3.0.0"
    "grafana/promtail:3.0.0"
)

# 尝试从不同镜像源下载
pull_with_mirror() {
    local image=$1
    local success=false

    for mirror in "${MIRRORS[@]}"; do
        echo ""
        echo "尝试镜像源: $mirror"
        echo "下载镜像: $image"

        if timeout 300 docker pull "${mirror}/${image}" 2>&1; then
            echo "✅ 下载成功: ${mirror}/${image}"
            docker tag "${mirror}/${image}" "${image}"
            echo "✅ 标记完成: ${image}"
            success=true
            break
        else
            echo "❌ 下载失败: ${mirror}/${image}"
        fi
    done

    if [ "$success" = false ]; then
        echo ""
        echo "=========================================="
        echo "❌ 所有镜像源都失败了"
        echo "=========================================="
        echo ""
        echo "建议："
        echo "1. 检查网络连接"
        echo "2. 使用 VPN 或代理"
        echo "3. 稍后重试"
        echo "4. 手动下载镜像文件"
        echo ""
        return 1
    fi

    return 0
}

# 主流程
failed_images=()

for image in "${IMAGES[@]}"; do
    echo ""
    echo "=========================================="
    echo "处理镜像: $image"
    echo "=========================================="

    if docker image inspect "$image" > /dev/null 2>&1; then
        echo "✅ 镜像已存在: $image"
        continue
    fi

    if ! pull_with_mirror "$image"; then
        failed_images+=("$image")
    fi

    echo ""
    sleep 2
done

# 总结
echo ""
echo "=========================================="
echo "  下载总结"
echo "=========================================="
echo ""

if [ ${#failed_images[@]} -eq 0 ]; then
    echo "✅ 所有镜像下载成功！"
    echo ""
    echo "现在可以启动服务："
    echo "  docker compose up -d prometheus grafana loki promtail"
else
    echo "❌ 以下镜像下载失败："
    for image in "${failed_images[@]}"; do
        echo "   - $image"
    done
    echo ""
    echo "建议操作："
    echo "1. 检查网络连接"
    echo "2. 使用 VPN 或代理"
    echo "3. 稍后重新运行此脚本"
fi

echo ""

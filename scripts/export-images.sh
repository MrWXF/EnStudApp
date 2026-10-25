#!/bin/bash
# 导出生产镜像到 tar 文件，便于上传到云服务器

set -e

OUTPUT_DIR="./docker-images"
mkdir -p $OUTPUT_DIR

echo "=== 导出生产镜像 ==="

# 导出应用镜像
echo "导出应用镜像..."
docker save -o $OUTPUT_DIR/enstudapp-images.tar \
    enstudapp-gateway:latest \
    enstudapp-user-service:latest \
    enstudapp-content-service:latest \
    enstudapp-ai-service:latest \
    enstudapp-nginx:latest

# 导出基础镜像
echo "导出基础镜像..."
docker save -o $OUTPUT_DIR/enstudapp-infra.tar \
    mysql:8.0 \
    redis:7-alpine \
    nacos/nacos-server:v2.2.3 \
    prom/prometheus:v2.51.0 \
    grafana/grafana:10.4.2

echo ""
echo "=== 导出完成 ==="
ls -lh $OUTPUT_DIR/
echo ""
echo "上传到服务器:"
echo "  scp -r $OUTPUT_DIR user@your-server:/opt/enstudapp/"
echo ""
echo "服务器上导入:"
echo "  cd /opt/enstudapp && docker load -i docker-images/enstudapp-images.tar"
echo "  cd /opt/enstudapp && docker load -i docker-images/enstudapp-infra.tar"

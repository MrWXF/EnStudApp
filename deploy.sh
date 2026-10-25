#!/bin/bash
# EnStudApp Docker 部署脚本

set -e

echo "🚀 开始部署 EnStudApp..."

# 检查 Docker 是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker 未运行，请先启动 Docker"
    exit 1
fi

# 检查 Docker Compose
if ! docker-compose --version > /dev/null 2>&1; then
    echo "❌ Docker Compose 未安装"
    exit 1
fi

# 创建环境变量文件
if [ ! -f .env ]; then
    echo "📝 创建环境变量文件..."
    cat > .env << EOF
# 数据库配置
MYSQL_ROOT_PASSWORD=root
MYSQL_PORT=13306

# Redis 配置
REDIS_PORT=6379

# Nacos 配置
NACOS_PORT=8848
NACOS_GRPC_PORT=9848

# 服务端口
GATEWAY_PORT=8080
USER_SERVICE_PORT=8081
CONTENT_SERVICE_PORT=8082
AI_SERVICE_PORT=8083

# Nginx
NGINX_PORT=80

# JWT 密钥
JWT_SECRET=your-super-secret-jwt-key-here-change-in-production

# AI 服务配置
DEEPSEEK_API_KEY=
DEEPSEEK_BASE_URL=https://api.deepseek.com/v1

# 监控配置
PROMETHEUS_PORT=9090
GRAFANA_PORT=3000
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=admin
EOF
    echo "✅ 环境变量文件已创建"
fi

# 构建并启动服务
echo "🔨 构建 Docker 镜像..."
docker-compose -f docker-compose.prod.yml build

echo "🚀 启动服务..."
docker-compose -f docker-compose.prod.yml up -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 60

# 检查服务健康状态
echo "🔍 检查服务健康状态..."

services=("gateway:8080" "user-service:8081" "content-service:8082" "ai-service:8083")
for service in "${services[@]}"; do
    IFS=':' read -r name port <<< "$service"
    if curl -f http://localhost:$port/actuator/health > /dev/null 2>&1; then
        echo "✅ $name 服务健康"
    else
        echo "❌ $name 服务未就绪"
    fi
done

echo ""
echo "🎉 部署完成！"
echo ""
echo "📋 访问信息:"
echo "  前端应用: http://localhost"
echo "  API 网关: http://localhost:8080"
echo "  Nacos 控制台: http://localhost:8848/nacos (nacos/nacos)"
echo "  Grafana 监控: http://localhost:3000 (admin/admin)"
echo "  Prometheus: http://localhost:9090"
echo ""
echo "📊 查看日志: docker-compose -f docker-compose.prod.yml logs -f [service-name]"
echo "🛑 停止服务: docker-compose -f docker-compose.prod.yml down"
echo "🔄 重启服务: docker-compose -f docker-compose.prod.yml restart [service-name]"

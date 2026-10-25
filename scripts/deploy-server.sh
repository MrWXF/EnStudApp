#!/bin/bash
# EnStudApp 云服务器一键部署脚本
# 用法: ./deploy-server.sh [服务器IP]

set -e

SERVER_IP=${1:-}
APP_DIR="/opt/enstudapp"

echo "=== EnStudApp 云服务器部署脚本 ==="

# 1. 安装 Docker（如未安装）
if ! command -v docker &> /dev/null; then
    echo "[1/6] 安装 Docker..."
    curl -fsSL https://get.docker.com | sh
    sudo usermod -aG docker $USER
    echo "Docker 安装完成，请重新登录后重跑此脚本"
    exit 0
else
    echo "[1/6] Docker 已安装: $(docker --version)"
fi

# 2. 创建应用目录
echo "[2/6] 创建应用目录..."
sudo mkdir -p $APP_DIR
sudo chown $USER:$USER $APP_DIR
cd $APP_DIR

# 3. 复制部署文件（从本地或 git 拉取）
echo "[3/6] 准备部署文件..."
if [ ! -f docker-compose.prod.yml ]; then
    echo "请将以下文件复制到 $APP_DIR/:"
    echo "  - docker-compose.prod.yml"
    echo "  - Dockerfile.multi"
    echo "  - .env"
    echo "  - nginx/nginx.conf"
    echo "  - nginx/nginx.prod.conf（如使用 HTTPS）"
    echo "  - enstud-frontend/dist/（前端构建产物）"
    echo "  - sql/init/（数据库初始化脚本）"
    exit 1
fi

# 4. 配置环境变量
if [ ! -f .env ]; then
    echo "[4/6] 创建 .env 文件..."
    cat > .env << 'EOF'
# 数据库
MYSQL_ROOT_PASSWORD=请修改为你的强密码
MYSQL_PORT=3306

# Redis
REDIS_PORT=6379

# Nacos
NACOS_PORT=8848

# 服务端口
GATEWAY_PORT=8080
USER_SERVICE_PORT=8081
CONTENT_SERVICE_PORT=8082
AI_SERVICE_PORT=8083
NGINX_PORT=80

# JWT 密钥（必须修改！）
JWT_SECRET=请生成一个64位随机字符串

# AI 服务（根据实际配置）
DEEPSEEK_API_KEY=
DEEPSEEK_BASE_URL=https://api.deepseek.com/v1

# 监控（可选）
PROMETHEUS_PORT=9090
GRAFANA_PORT=3000
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=admin
EOF
    echo "请编辑 .env 文件配置数据库密码和 JWT 密钥！"
    nano .env
fi

# 5. 构建并启动服务
echo "[5/6] 构建生产镜像（首次约需 10-20 分钟）..."
docker compose -f docker-compose.prod.yml build

echo "[6/6] 启动服务..."
docker compose -f docker-compose.prod.yml up -d

# 6. 等待服务健康检查
echo "等待服务启动..."
sleep 30

echo ""
echo "=== 部署状态 ==="
docker compose -f docker-compose.prod.yml ps

echo ""
echo "=== 健康检查 ==="
echo -n "Nginx: "; curl -s -o /dev/null -w "%{http_code}" http://localhost:80
echo ""
echo -n "Gateway: "; curl -s http://localhost:8080/actuator/health | grep -o '"status":"[^"]*"' | head -1
echo ""
echo -n "User Service: "; curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/actuator/health
echo ""
echo -n "Content Service: "; curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/actuator/health
echo ""
echo -n "AI Service: "; curl -s -o /dev/null -w "%{http_code}" http://localhost:8083/actuator/health
echo ""

echo ""
echo "=== 部署完成 ==="
echo "前端访问: http://$SERVER_IP 或 http://localhost"
echo "API 网关: http://$SERVER_IP:8080"
echo "Nacos 控制台: http://$SERVER_IP:8848/nacos (默认账号 nacos/nacos)"
echo "Grafana 监控: http://$SERVER_IP:3000 (默认账号 admin/admin)"
echo ""
echo "常用命令:"
echo "  查看日志: docker compose -f docker-compose.prod.yml logs -f [服务名]"
echo "  重启服务: docker compose -f docker-compose.prod.yml restart"
echo "  停止服务: docker compose -f docker-compose.prod.yml down"

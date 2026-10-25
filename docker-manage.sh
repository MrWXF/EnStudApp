#!/bin/bash
# EnStudApp Docker 管理脚本

case "$1" in
    "start")
        echo "🚀 启动 EnStudApp 服务..."
        docker-compose -f docker-compose.prod.yml up -d
        echo "✅ 服务已启动"
        ;;
    "stop")
        echo "🛑 停止 EnStudApp 服务..."
        docker-compose -f docker-compose.prod.yml down
        echo "✅ 服务已停止"
        ;;
    "restart")
        echo "🔄 重启 EnStudApp 服务..."
        docker-compose -f docker-compose.prod.yml restart
        echo "✅ 服务已重启"
        ;;
    "status")
        echo "📊 服务状态:"
        docker-compose -f docker-compose.prod.yml ps
        ;;
    "logs")
        if [ -z "$2" ]; then
            echo "📋 查看所有服务日志:"
            docker-compose -f docker-compose.prod.yml logs -f
        else
            echo "📋 查看 $2 服务日志:"
            docker-compose -f docker-compose.prod.yml logs -f "$2"
        fi
        ;;
    "build")
        echo "🔨 重新构建镜像..."
        docker-compose -f docker-compose.prod.yml build
        echo "✅ 镜像构建完成"
        ;;
    "clean")
        echo "🧹 清理 Docker 资源..."
        docker-compose -f docker-compose.prod.yml down -v
        docker system prune -f
        echo "✅ 清理完成"
        ;;
    "health")
        echo "🔍 检查服务健康状态..."
        services=("gateway:8080" "user-service:8081" "content-service:8082" "ai-service:8083")
        for service in "${services[@]}"; do
            IFS=':' read -r name port <<< "$service"
            if curl -f http://localhost:$port/actuator/health > /dev/null 2>&1; then
                echo "✅ $name: 健康"
            else
                echo "❌ $name: 异常"
            fi
        done
        ;;
    "info")
        echo "📋 EnStudApp Docker 信息:"
        echo ""
        echo "🐳 Docker 版本:"
        docker --version
        echo ""
        echo "📦 Docker Compose 版本:"
        docker-compose --version
        echo ""
        echo "📊 容器资源使用:"
        docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}"
        echo ""
        echo "🌐 服务端口:"
        echo "  Gateway: 8080"
        echo "  User Service: 8081"
        echo "  Content Service: 8082"
        echo "  AI Service: 8083"
        echo "  MySQL: 13306"
        echo "  Redis: 6379"
        echo "  Nacos: 8848"
        echo "  Nginx: 80"
        echo "  Prometheus: 9090"
        echo "  Grafana: 3000"
        ;;
    *)
        echo "EnStudApp Docker 管理脚本"
        echo ""
        echo "用法: $0 [命令] [选项]"
        echo ""
        echo "命令:"
        echo "  start     启动所有服务"
        echo "  stop      停止所有服务"
        echo "  restart   重启所有服务"
        echo "  status    查看服务状态"
        echo "  logs      查看服务日志 (logs [service-name])"
        echo "  build     重新构建镜像"
        echo "  clean     清理 Docker 资源"
        echo "  health    检查服务健康状态"
        echo "  info      显示 Docker 信息"
        echo ""
        echo "示例:"
        echo "  $0 start           # 启动所有服务"
        echo "  $0 logs gateway    # 查看网关日志"
        echo "  $0 health          # 检查服务健康"
        ;;
esac

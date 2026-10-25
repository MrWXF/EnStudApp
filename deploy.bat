@echo off
echo 🚀 开始部署 EnStudApp...

REM 检查 Docker 是否运行
docker info >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker 未运行，请先启动 Docker
    exit /b 1
)

REM 创建环境变量文件
if not exist .env (
    echo 📝 创建环境变量文件...
    (
        echo # 数据库配置
        echo MYSQL_ROOT_PASSWORD=root
        echo MYSQL_PORT=13306
        echo.
        echo # Redis 配置
        echo REDIS_PORT=6379
        echo.
        echo # Nacos 配置
        echo NACOS_PORT=8848
        echo NACOS_GRPC_PORT=9848
        echo.
        echo # 服务端口
        echo GATEWAY_PORT=8080
        echo USER_SERVICE_PORT=8081
        echo CONTENT_SERVICE_PORT=8082
        echo AI_SERVICE_PORT=8083
        echo.
        echo # Nginx
        echo NGINX_PORT=80
        echo.
        echo # JWT 密钥
        echo JWT_SECRET=your-super-secret-jwt-key-here-change-in-production
        echo.
        echo # AI 服务配置
        echo DEEPSEEK_API_KEY=
        echo DEEPSEEK_BASE_URL=https://api.deepseek.com/v1
        echo.
        echo # 监控配置
        echo PROMETHEUS_PORT=9090
        echo GRAFANA_PORT=3000
        echo GRAFANA_ADMIN_USER=admin
        echo GRAFANA_ADMIN_PASSWORD=admin
    ) > .env
    echo ✅ 环境变量文件已创建
)

echo 🔨 构建 Docker 镜像...
docker-compose -f docker-compose.prod.yml build

echo 🚀 启动服务...
docker-compose -f docker-compose.prod.yml up -d

echo ⏳ 等待服务启动...
timeout /t 60 /nobreak >nul

echo 🔍 检查服务健康状态...
curl -f http://localhost:8080/actuator/health >nul 2>&1
if not errorlevel 1 (
    echo ✅ Gateway 服务健康
) else (
    echo ❌ Gateway 服务未就绪
)

curl -f http://localhost:8081/actuator/health >nul 2>&1
if not errorlevel 1 (
    echo ✅ User Service 服务健康
) else (
    echo ❌ User Service 服务未就绪
)

curl -f http://localhost:8082/actuator/health >nul 2>&1
if not errorlevel 1 (
    echo ✅ Content Service 服务健康
) else (
    echo ❌ Content Service 服务未就绪
)

curl -f http://localhost:8083/actuator/health >nul 2>&1
if not errorlevel 1 (
    echo ✅ AI Service 服务健康
) else (
    echo ❌ AI Service 服务未就绪
)

echo.
echo 🎉 部署完成！
echo.
echo 📋 访问信息:
echo   前端应用: http://localhost
echo   API 网关: http://localhost:8080
echo   Nacos 控制台: http://localhost:8848/nacos (nacos/nacos)
echo   Grafana 监控: http://localhost:3000 (admin/admin)
echo   Prometheus: http://localhost:9090
echo.
echo 📊 查看日志: docker-compose -f docker-compose.prod.yml logs -f [service-name]
echo 🛑 停止服务: docker-compose -f docker-compose.prod.yml down
echo 🔄 重启服务: docker-compose -f docker-compose.prod.yml restart [service-name]

pause

@echo off
chcp 65001 >nul
echo === 构建前端并更新 Nginx ===
echo.

cd /d D:\project\github\EnStudApp\client

echo [1/4] 安装依赖...
call npm install
if errorlevel 1 (
    echo 安装失败
    pause
    exit /b 1
)

echo [2/4] 构建前端...
call npm run build
if errorlevel 1 (
    echo 构建失败
    pause
    exit /b 1
)

echo [3/4] 重建 Nginx 镜像...
cd /d D:\project\github\EnStudApp
docker-compose build nginx
if errorlevel 1 (
    echo 重建失败
    pause
    exit /b 1
)

echo [4/4] 重启 Nginx...
docker-compose up -d nginx
if errorlevel 1 (
    echo 重启失败
    pause
    exit /b 1
)

echo.
echo === 完成！ ===
echo 访问: http://localhost
pause

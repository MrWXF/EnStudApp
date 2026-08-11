@echo off
chcp 65001 >nul
echo === 恢复所有 EnStud 容器 ===
echo.

docker unpause enstud-mysql
docker unpause enstud-redis
docker unpause enstud-nacos
docker unpause enstud-user-service
docker unpause enstud-word-service
docker unpause enstud-chat-service
docker unpause enstud-writing-service
docker unpause enstud-translate-service
docker unpause enstud-forum-service
docker unpause enstud-gateway
docker unpause enstud-nginx

echo.
echo === 容器状态 ===
docker ps --filter "name=enstud-" --format "table {{.Names}}\t{{.Status}}"

echo.
echo === 完成！==="
echo 访问: http://localhost
pause

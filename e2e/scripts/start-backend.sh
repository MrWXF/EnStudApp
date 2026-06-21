#!/bin/bash
# E2E 后端启动脚本
# 以 test profile 启动各微服务（H2 数据库），无需 Nacos

set -e

BASE_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
echo "Starting backend services from $BASE_DIR"

# 使用 H2 test profile 启动服务，跳过 Nacos
export SPRING_PROFILES_ACTIVE=test

# 启动 user-service
echo "Starting user-service (8081)..."
cd "$BASE_DIR/enstud-user-service"
mvn spring-boot:run -q > /tmp/user-service.log 2>&1 &
USER_PID=$!
echo "user-service PID: $USER_PID"

# 等待 user-service 就绪
for i in $(seq 1 30); do
  if curl -sf http://localhost:8081/actuator/health > /dev/null 2>&1; then
    echo "user-service is UP"
    break
  fi
  sleep 2
done

echo "Backend startup complete."
echo "PIDs: user=$USER_PID"
wait

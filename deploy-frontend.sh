#!/bin/bash
# 前端部署脚本

echo "=== 开始前端部署 ==="

# 1. 进入前端目录
cd D:/project/github/EnStudApp/client

# 2. 安装依赖
echo "正在安装依赖..."
npm install

if [ $? -ne 0 ]; then
  echo "❌ npm install 失败"
  exit 1
fi

echo "✓ 依赖安装完成"

# 3. 构建前端
echo "正在构建前端..."
npm run build

if [ $? -ne 0 ]; then
  echo "❌ npm run build 失败"
  exit 1
fi

echo "✓ 前端构建完成"

# 4. 回到项目根目录
cd ..

# 5. 重新构建 Nginx 镜像
echo "正在重建 Nginx 镜像..."
docker-compose build nginx

if [ $? -ne 0 ]; then
  echo "❌ docker-compose build 失败"
  exit 1
fi

echo "✓ Nginx 镜像重建完成"

# 6. 重启 Nginx
echo "正在重启 Nginx..."
docker-compose up -d nginx

if [ $? -ne 0 ]; then
  echo "❌ docker-compose up 失败"
  exit 1
fi

echo "✓ Nginx 重启完成"

echo ""
echo "=== 部署完成！==="
echo "访问: http://localhost"

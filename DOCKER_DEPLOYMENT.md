# EnStudApp Docker 部署报告

## 部署时间
'$(Get-Date -Format "yyyy-MM-dd HH:mm:ss")'

## Docker 架构

### 镜像结构
- **多阶段构建**: 使用 Maven 构建 + JRE 运行
- **基础镜像**: eclipse-temurin:17-jre-alpine (轻量级)
- **构建镜像**: maven:3.9-eclipse-temurin-17

### 服务镜像
1. **user-service**: 用户核心服务 (8081)
2. **content-service**: 内容服务 (8082) 
3. **ai-service**: AI 服务 (8083)
4. **gateway**: API 网关 (8080)

### 基础设施镜像
- **MySQL 8.0**: 数据库 (13306)
- **Redis 7-alpine**: 缓存 (6379)
- **Nacos 2.2.3**: 注册中心 (8848)
- **Nginx**: 前端代理 (80)
- **Prometheus**: 监控 (9090)
- **Grafana**: 可视化 (3000)

## 部署文件

### 1. Dockerfile.multi
- 多阶段构建文件
- 支持 4 个服务目标：user-service, content-service, ai-service, gateway
- 优化镜像大小，只包含运行时依赖

### 2. docker-compose.prod.yml
- 生产环境配置
- 包含健康检查
- 服务依赖管理
- 网络隔离

### 3. deploy.sh / deploy.bat
- 一键部署脚本
- 自动环境检查
- 服务健康验证

### 4. docker-manage.sh
- Docker 管理工具
- 支持启动/停止/重启/日志/健康检查

## 部署命令

### 快速部署
```bash
# Linux/Mac
./deploy.sh

# Windows
deploy.bat
```

### 手动部署
```bash
# 构建镜像
docker-compose -f docker-compose.prod.yml build

# 启动服务
docker-compose -f docker-compose.prod.yml up -d

# 查看状态
docker-compose -f docker-compose.prod.yml ps

# 查看日志
docker-compose -f docker-compose.prod.yml logs -f [service-name]
```

## 服务管理

### 管理命令
```bash
# 启动所有服务
docker-compose -f docker-compose.prod.yml up -d

# 停止所有服务
docker-compose -f docker-compose.prod.yml down

# 重启服务
docker-compose -f docker-compose.prod.yml restart [service-name]

# 查看日志
docker-compose -f docker-compose.prod.yml logs -f [service-name]

# 清理资源
docker-compose -f docker-compose.prod.yml down -v
docker system prune -f
```

## 访问信息

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端应用 | http://localhost | Nginx 入口 |
| API 网关 | http://localhost:8080 | Spring Cloud Gateway |
| 用户服务 | http://localhost:8081 | User Service |
| 内容服务 | http://localhost:8082 | Content Service |
| AI 服务 | http://localhost:8083 | AI Service |
| Nacos 控制台 | http://localhost:8848/nacos | nacos/nacos |
| Grafana 监控 | http://localhost:3000 | admin/admin |
| Prometheus | http://localhost:9090 | 监控指标 |

## 优势

1. **资源优化**: 3 个核心服务 vs 7 个微服务
2. **易于管理**: 统一的 Docker 部署方案
3. **生产就绪**: 健康检查、自动重启、日志管理
4. **监控完善**: Prometheus + Grafana 监控
5. **一键部署**: 自动化部署脚本

## 注意事项

1. 确保 Docker 和 Docker Compose 已安装
2. 首次构建需要下载依赖，时间较长
3. 生产环境请修改默认密码和密钥
4. 定期备份数据库数据

## 故障排除

### 常见问题
1. **端口冲突**: 修改 .env 文件中的端口配置
2. **内存不足**: 增加 Docker 内存限制
3. **网络问题**: 检查 Docker 网络配置

### 日志查看
```bash
# 查看特定服务日志
docker-compose -f docker-compose.prod.yml logs -f user-service

# 查看所有服务日志
docker-compose -f docker-compose.prod.yml logs -f
```

## 总结

Docker 化部署完成！系统现在可以：
- 一键部署和启动
- 自动健康检查和重启
- 完整的监控和日志
- 资源优化和隔离

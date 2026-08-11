# EnStudApp Nginx 配置指南

> Nginx 作为统一入口，承担前端静态资源服务与后端 API 反向代理职责。本文档基于项目实际配置文件 `nginx/nginx.conf`（开发）和 `nginx/nginx.prod.conf`（生产）。

---

## 一、架构概览

```
                     ┌──────────────────┐
   客户端 ───────▶   │   Nginx (80/443) │
                     └──────────────────┘
                            │
           ┌────────────────┼────────────────┐
           │                │                │
     ┌─────▼─────┐   ┌─────▼──────┐   ┌─────▼──────┐
     │ 前端静态   │   │ /api/*     │   │ 兼容路径    │
     │ 资源 (/)   │   │ 代理       │   │ 代理        │
     └───────────┘   └─────┬──────┘   └─────┬──────┘
                           │                │
                     ┌─────▼──────────────────▼─────┐
                     │   Gateway (enstud-gateway:8080)│
                     └──────────────────────────────┘
```

---

## 二、配置文件说明

| 文件 | 用途 | 使用环境 |
|------|------|---------|
| `nginx/nginx.conf` | 基础 HTTP 配置 | 开发环境 |
| `nginx/nginx.prod.conf` | HTTPS + 安全加固 | 生产环境 |
| `nginx/Dockerfile` | Nginx 镜像构建 | 通用 |

---

## 三、开发环境配置（nginx.conf）

### 3.1 完整配置

```nginx
worker_processes auto;

events {
    worker_connections 1024;
}

http {
    include       mime.types;
    default_type  application/octet-stream;
    sendfile      on;
    keepalive_timeout 65;

    # 上游网关集群
    upstream gateway {
        server enstud-gateway:8080;
    }

    server {
        listen 80;
        server_name localhost;

        # 前端静态资源
        location / {
            root   /usr/share/nginx/html;
            index  index.html;
            try_files $uri $uri/ /index.html;
        }

        # API 请求转发到网关
        location /api/ {
            proxy_pass http://gateway/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # 直接服务路径也转发（兼容不通过 /api 前缀的请求）
        location ~ ^/(user|word|chat|writing|translate|forum)/ {
            proxy_pass http://gateway;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }
}
```

### 3.2 关键配置解读

**`worker_processes auto;`**
自动按 CPU 核数设置 worker 进程数，充分利用多核。

**`upstream gateway`**
定义上游网关集群，容器内使用服务名 `enstud-gateway`，Docker DNS 自动解析。如需负载均衡可添加多个 server。

**`location /`**
前端静态资源服务：
- `root /usr/share/nginx/html` — React 构建产物（`client/dist` 拷贝至此）
- `try_files $uri $uri/ /index.html` — SPA 路由兜底，所有前端路由都返回 `index.html`

**`location /api/`**
API 代理：
- `proxy_pass http://gateway/;`（注意末尾 `/`）— 去掉 `/api` 前缀后转发
- 例：`/api/user/register` → `http://enstud-gateway:8080/user/register`

**`location ~ ^/(user|word|chat|writing|translate|forum)/`**
兼容路径，处理直接通过服务名访问的请求：
- `/user/profile` → `http://enstud-gateway:8080/user/profile`
- `/word/learn` → `http://enstud-gateway:8080/word/learn`
- 覆盖 6 个对外服务：user / word / chat / writing / translate / forum

---

## 四、生产环境配置（nginx.prod.conf）

### 4.1 与开发配置的差异

| 特性 | 开发 (nginx.conf) | 生产 (nginx.prod.conf) |
|------|------------------|----------------------|
| 协议 | HTTP :80 | HTTPS :443 + HTTP→HTTPS 301 |
| Gzip 压缩 | 未启用 | 启用（1KB 以上） |
| 安全响应头 | 无 | HSTS / X-Content-Type-Options / X-Frame-Options |
| Actuator 端点 | 公开 | 仅内网 IP 允许 |
| SSL 证书 | 无 | `/etc/nginx/certs/` 挂载 |
| 变量替换 | 无 | `${DOMAIN}` / `${SSL_CERT_PATH}` / `${SSL_KEY_PATH}` |

### 4.2 HTTP → HTTPS 重定向

```nginx
server {
    listen 80;
    server_name ${DOMAIN:-localhost};
    return 301 https://$server_name$request_uri;
}
```

所有 HTTP 请求 301 永久重定向到 HTTPS。`${DOMAIN}` 在部署时由 Nginx 环境变量或挂载前替换。

### 4.3 SSL/TLS 配置

```nginx
listen 443 ssl http2;

ssl_certificate     ${SSL_CERT_PATH:-/etc/nginx/certs/fullchain.pem};
ssl_certificate_key ${SSL_KEY_PATH:-/etc/nginx/certs/privkey.pem};

# 仅允许 TLS 1.2 / 1.3
ssl_protocols TLSv1.2 TLSv1.3;

# 强密码套件
ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:...;
ssl_prefer_server_ciphers off;

# Session 缓存（提升 TLS 握手性能）
ssl_session_cache shared:SSL:10m;
ssl_session_timeout 1d;
```

### 4.4 安全响应头

```nginx
add_header Strict-Transport-Security "max-age=63072000" always;  # HSTS 2 年
add_header X-Content-Type-Options nosniff;                       # 禁止 MIME 嗅探
add_header X-Frame-Options DENY;                                 # 禁止 iframe 嵌套
```

### 4.5 Gzip 压缩

```nginx
gzip on;
gzip_min_length 1024;
gzip_types text/plain text/css application/json application/javascript
           text/xml application/xml text/javascript image/svg+xml;
```

压缩 1KB 以上的文本类资源，显著减少传输体积。

### 4.6 Actuator 内网访问

```nginx
location /actuator/ {
    allow 127.0.0.1;
    allow 172.20.0.0/16;   # Docker 内网段
    deny all;
    proxy_pass http://gateway/actuator/;
    proxy_set_header Host $host;
}
```

生产环境健康检查和 Prometheus 指标端点仅允许内网访问，避免暴露给公网。

---

## 五、HTTPS 证书配置

### 5.1 获取 Let's Encrypt 证书

```bash
# 安装 certbot
sudo apt install certbot

# 方式 1：standalone（需先停 Nginx）
sudo certbot certonly --standalone -d your-domain.com

# 方式 2：webroot（Nginx 运行中）
sudo certbot certonly --webroot -w /var/www/certbot -d your-domain.com

# 证书位置
/etc/letsencrypt/live/your-domain.com/
  ├── fullchain.pem
  └── privkey.pem
```

### 5.2 部署证书

```bash
# 创建证书目录
mkdir -p /opt/enstud/certs

# 拷贝证书
sudo cp /etc/letsencrypt/live/your-domain.com/fullchain.pem /opt/enstud/certs/
sudo cp /etc/letsencrypt/live/your-domain.com/privkey.pem /opt/enstud/certs/

# 权限设置
sudo chown -R $USER:$USER /opt/enstud/certs
chmod 600 /opt/enstud/certs/privkey.pem
```

### 5.3 自动续期

```bash
# 添加 crontab（每日检查，30 天内过期则续期）
0 3 * * * certbot renew --quiet --deploy-hook "docker exec enstud-nginx nginx -s reload"
```

### 5.4 docker-compose.prod.yml 挂载

```yaml
nginx:
  volumes:
    - ./nginx/nginx.prod.conf:/etc/nginx/nginx.conf:ro
    - ${SSL_CERTS_DIR:-./certs}:/etc/nginx/certs:ro
    - certbot-webroot:/var/www/certbot:ro
  ports:
    - "80:80"
    - "443:443"
```

---

## 六、性能优化

### 6.1 已启用

- **Gzip 压缩** — 文本资源减少 60-80% 体积
- **sendfile on** — 零拷贝传输静态文件
- **keepalive_timeout 65** — 长连接复用，减少握手开销
- **HTTP/2** — 生产环境 443 端口默认启用 HTTP/2
- **SSL Session Cache** — 10MB 共享缓存，TLS 握手提速

### 6.2 可选优化

如需进一步优化，可考虑添加以下配置到 `nginx.prod.conf`：

```nginx
# 打开文件缓存（减少磁盘 IO）
open_file_cache max=10000 inactive=60s;
open_file_cache_valid 60s;
open_file_cache_min_uses 2;

# 代理缓冲区
proxy_buffer_size 16k;
proxy_buffers 4 32k;
proxy_busy_buffers_size 64k;

# 客户端请求体限制（上传文件等）
client_max_body_size 20m;

# 代理超时
proxy_connect_timeout 60s;
proxy_send_timeout 60s;
proxy_read_timeout 60s;
```

### 6.3 静态资源缓存

前端构建产物已带 content hash，可在 `location /` 中添加：

```nginx
location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff2)$ {
    expires 1y;
    add_header Cache-Control "public, immutable";
}
```

---

## 七、构建与部署

### 7.1 Dockerfile

```dockerfile
FROM nginx:alpine
COPY nginx/nginx.conf /etc/nginx/nginx.conf
COPY client/dist /usr/share/nginx/html
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD curl -sf http://localhost:80/ || exit 1
EXPOSE 80
```

镜像基于 `nginx:alpine`，体积小（约 40MB），包含前端构建产物。

### 7.2 生产环境替换配置

生产环境通过 `docker-compose.prod.yml` 挂载覆盖：

```yaml
volumes:
  - ./nginx/nginx.prod.conf:/etc/nginx/nginx.conf:ro
```

修改配置后无需重建镜像，重启容器即可：

```bash
docker compose restart nginx
```

### 7.3 配置校验

重载前务必检查语法：

```bash
docker exec enstud-nginx nginx -t
```

期望输出：

```
nginx: the configuration file /etc/nginx/nginx.conf syntax is ok
nginx: configuration file /etc/nginx/nginx.conf test is successful
```

### 7.4 热重载

```bash
docker exec enstud-nginx nginx -s reload
```

---

## 八、故障排查

| 现象 | 排查 |
|------|------|
| 502 Bad Gateway | Gateway 未启动或未就绪。`docker compose ps gateway` 检查状态 |
| 504 Gateway Timeout | 后端服务处理慢或挂起。`docker compose logs <service>` 查日志 |
| 前端页面 404 | 构建产物未拷贝到 `/usr/share/nginx/html`。重建镜像 |
| SPA 路由 404 | 检查 `try_files $uri $uri/ /index.html` 是否存在 |
| HTTPS 证书错误 | 证书路径是否正确；域名是否匹配；证书是否过期 |
| SSL 握手失败 | 客户端 TLS 版本过低（需 1.2+）；检查 `ssl_ciphers` |
| `/api/xxx` 返回 404 | Gateway 路由未注册；检查对应微服务是否在 Nacos 注册 |
| Actuator 403 | 公网访问被拒绝（预期行为）。从内网 IP 访问 |
| `upstream gateway` 解析失败 | Docker DNS 问题。确认 `enstud-network` 网络存在，gateway 容器已启动 |

### 实时日志

```bash
# Nginx 访问日志
docker exec enstud-nginx tail -f /var/log/nginx/access.log

# Nginx 错误日志
docker exec enstud-nginx tail -f /var/log/nginx/error.log
```

### 调试代理路径

```bash
# 测试 /api/ 代理
curl -v https://your-domain.com/api/user/register

# 测试服务路径代理
curl -v https://your-domain.com/user/profile

# 查看响应头（确认经过 Nginx）
curl -I https://your-domain.com/
```

---

## 九、相关文档

- [部署指南](deployment.md) — 完整部署流程
- [CI/CD 流水线](cicd.md) — 自动化构建与部署
- [投产发布流程](RELEASE.md) — 版本发布与回滚

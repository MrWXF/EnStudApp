# EnStudApp E2E 测试报告

**测试时间：** 2026-06-15 23:56 UTC
**测试环境：** WSL (Docker容器)
**测试方式：** Python3 脚本从 WSL 调用容器内微服务（通过 Gateway 172.20.0.6:8080）

## 测试结果总览

| 指标 | 数值 |
|------|------|
| ✅ 通过 | **16 / 16** |
| ❌ 失败 | 0 |
| 通过率 | **100%** |

## 详细测试项

| # | 测试项 | 方法 | 路径 | HTTP状态 | 结果 |
|---|--------|------|------|----------|------|
| 1 | 用户注册 | POST | `/user/register` | 200 | ✅ |
| 2 | 用户登录 | POST | `/user/login` | 200 | ✅ |
| 3 | 获取用户信息 | GET | `/user/profile` | 200 | ✅ |
| 4 | 论坛帖子列表(公开) | GET | `/forum/posts?limit=3` | 200 | ✅ |
| 5 | 创建论坛帖子 | POST | `/forum/posts` | 200 | ✅ |
| 6 | 生词本列表 | GET | `/word/wordbooks` | 200 | ✅ |
| 7 | 开始学习 | POST | `/word/study?wordbookId=1&limit=5` | 200 | ✅ |
| 8 | 提交写作 | POST | `/writing/submit` | 200 | ✅ |
| 9 | 翻译文本 | POST | `/translate/text` | 200 | ✅ |
| 10 | 创建聊天会话 | POST | `/chat/sessions` | 200 | ✅ |
| 11 | 聊天会话列表 | GET | `/chat/sessions` | 200 | ✅ |
| 12 | 热门文章列表 | GET | `/read/hot?limit=3` | 200 | ✅ |
| 13 | 书签列表 | GET | `/read/bookmarks` | 200 | ✅ |
| 14 | 阅读来源列表 | GET | `/read/sources` | 200 | ✅ |
| 15 | 同步文章 | POST | `/read/sync` | 200 | ✅ |
| 16 | 论坛分类列表 | GET | `/forum/categories` | 200 | ✅ |

## 服务健康状态

| 服务 | 端口 | 状态 |
|------|------|------|
| Gateway | 8080 | ✅ 运行中 |
| user-service | 8081 | ✅ 运行中 |
| word-service | 8082 | ✅ 运行中 |
| chat-service | 8083 | ✅ 运行中 |
| writing-service | 8084 | ✅ 运行中 |
| translate-service | 8085 | ✅ 运行中 |
| forum-service | 8086 | ✅ 运行中 |
| read-service | 8087 | ✅ 运行中 |
| MySQL | 3306 | ✅ 运行中 |
| Redis | 6379 | ✅ 运行中 |
| Nacos | 8848 | ✅ 运行中 |

## 修复的问题记录

### 1. SecurityContextInterceptor 缺少 preHandle
- **根因：** `SecurityContextInterceptor.preHandle()` 为空方法，导致从网关转发 X-User-Id 请求头后，下游服务无法获取用户上下文
- **修复：** 添加 preHandle 逻辑读取请求头中的 X-User-Id 并设置 SecurityContext
- **影响范围：** user/profile, word/wordbooks, writing/submit 等所有需要认证的接口

### 2. 数据库字段缺失
- **`enstud_writing` 表不存在** — 执行迁移脚本 `20260524_create_writing_table.sql`
- **`enstud_chat_session`、`enstud_chat_message` 表不存在** — 执行迁移脚本 `20260524_create_chat_tables.sql`
- **`review_interval` 列在 `enstud_user_word_record` 表缺失** — ALTER TABLE 添加
- **`sort_order` 列在 `enstud_wordbook` 表缺失** — ALTER TABLE 添加
- **根因：** 建表脚本在服务发布时未执行

### 3. IP 配置错误
- **问题：** application.yml 中 Redis 默认 IP 写为 `172.20.0.3`（实际是 MySQL 的 IP）
- **Redis 正确 IP：** `172.20.0.4`
- **解决：** 启动时传递 `REDIS_HOST=172.20.0.4` 环境变量
- **衍生问题：** translate/forum/read 服务无 `spring.data.redis` 配置，Redisson 自动配置默认连接 `localhost:6379`，需启动时传 `--spring.data.redis.host=172.20.0.4`

### 4. E2E 脚本路径/方法错误
- 8 处 API 路径与 Controller 实际映射不匹配（均在修复的 e2e_test_py.py 中修正）

### 5. MySQL 用户服务启动参数缺失
- user-service 初次重启时未传递 `--spring.datasource.url`，导致使用默认 IP 172.20.0.2
- 已在启动命令中补充完整的 jdbc 连接参数

## 遗留问题

| 问题 | 严重性 | 说明 |
|------|--------|------|
| Redisson 排除不彻底 | ⚠️ 低 | `@SpringBootApplication(exclude=RedissonAutoConfigurationV2.class)` 只排除了 V2 自动配置，但 `spring.factories` 中的 `RedissonAutoConfiguration` 仍会加载。需启动时传递 `--spring.data.redis.host=172.20.0.4` 作为规避 |
| CLADUE.md 文档同步 | ℹ️ 信息 | CLAUDE.md 中 `spring.data.redis.host` 默认值仍写为 `172.20.0.3`，需更新为 `172.20.0.4` |

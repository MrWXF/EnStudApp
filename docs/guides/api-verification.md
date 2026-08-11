# API 验证规范

> 代码修改后必须验证 API 可用性，确保功能正常

## 验证流程

**每次修改代码后，必须按以下顺序验证：**

1. **编译检查** - 确保代码语法正确
2. **启动服务** - 确保服务能正常启动
3. **接口测试** - 验证 API 功能符合预期

## 1. 编译检查

```bash
# 编译所有模块
mvn clean compile

# 编译单个模块
mvn clean compile -pl enstud-user-service

# 完整构建（跳过测试）
mvn clean install -DskipTests
```

**预期结果：** 编译成功，无错误输出

```
[INFO] BUILD SUCCESS
[INFO] Total time:  xx s
```

## 2. 启动服务并检查健康状态

```bash
# 启动服务
cd enstud-user-service
mvn spring-boot:run

# 或使用 Docker
docker-compose up -d user-service
```

**检查健康状态：**

```bash
curl http://localhost:8081/actuator/health
```

**预期响应：**

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "redis": {
      "status": "UP"
    }
  }
}
```

## 3. 接口测试

### 获取 JWT Token

大多数 API 需要认证，先获取 Token：

```bash
# 注册用户（如果还没有账号）
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123456",
    "email": "test@example.com"
  }'

# 登录获取 Token
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123456"
  }'
```

**预期响应：**

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "username": "testuser"
  }
}
```

保存 Token 用于后续请求：

```bash
TOKEN="eyJhbGciOiJIUzI1NiJ9..."
```

### 常见 API 测试示例

#### 用户服务

```bash
# 获取用户信息
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer $TOKEN"

# 更新用户信息
curl -X PUT http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nickname": "测试用户",
    "avatar": "https://example.com/avatar.jpg"
  }'
```

#### 单词服务

```bash
# 获取单词列表
curl -X GET "http://localhost:8080/api/word/list?bookId=1&limit=10" \
  -H "Authorization: Bearer $TOKEN"

# 提交学习记录
curl -X POST http://localhost:8080/api/word/learn \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "wordId": 1,
    "result": "CORRECT"
  }'

# 获取错词本
curl -X GET http://localhost:8080/api/word/wrong-words \
  -H "Authorization: Bearer $TOKEN"
```

#### 论坛服务

```bash
# 获取帖子列表
curl -X GET "http://localhost:8080/api/forum/posts?limit=10" \
  -H "Authorization: Bearer $TOKEN"

# 发布帖子
curl -X POST http://localhost:8080/api/forum/posts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "测试帖子",
    "content": "这是测试内容",
    "tags": ["学习", "讨论"]
  }'

# 点赞帖子
curl -X POST http://localhost:8080/api/forum/posts/1/like \
  -H "Authorization: Bearer $TOKEN"
```

#### 聊天服务

```bash
# 发送消息
curl -X POST http://localhost:8080/api/chat/send \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Hello, how are you?",
    "topic": "daily"
  }'

# 获取聊天记录
curl -X GET "http://localhost:8080/api/chat/history?limit=20" \
  -H "Authorization: Bearer $TOKEN"
```

## 统一响应格式

所有 API 返回 `Result<T>` 格式：

### 成功响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    // 具体数据
  }
}
```

### 失败响应

```json
{
  "code": 40001,
  "message": "用户名或密码错误",
  "data": null
}
```

### 分页响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      // 数据列表
    ],
    "hasMore": true,
    "nextCursor": "eyJpZCI6MTAwfQ=="
  }
}
```

## 错误码说明

| 错误码范围 | 说明 |
|-----------|------|
| 0 | 成功 |
| 40000-40099 | 参数错误 |
| 40100-40199 | 认证失败 |
| 40300-40399 | 权限不足 |
| 40400-40499 | 资源不存在 |
| 50000-50099 | 服务器内部错误 |

## 使用 Knife4j 查看 API 文档

启动服务后，访问 Swagger 文档：

```bash
# 用户服务
http://localhost:8081/doc.html

# 单词服务
http://localhost:8082/doc.html

# 其他服务类似...
```

在 Knife4j 界面可以：
- 查看所有 API 接口
- 在线调试接口
- 查看请求/响应示例

## 自动化测试脚本

项目提供了 E2E 测试脚本：

```bash
# 运行 E2E 测试
bash scripts/e2e_test.sh

# 或使用 Python 版本
python scripts/e2e_test.py
```

## 验证检查清单

完成代码修改后，对照以下清单：

- [ ] `mvn clean compile` 编译成功
- [ ] 服务启动无报错
- [ ] `/actuator/health` 返回 `UP`
- [ ] 新增/修改的 API 返回正确格式
- [ ] 错误场景返回合适的错误码和消息
- [ ] 需要认证的接口已添加认证
- [ ] 参数校验正常工作
- [ ] 数据库操作正确（增删改查）
- [ ] 日志输出正常（使用 `@Slf4j`）
- [ ] API 文档注解完整（`@Tag` / `@Operation` / `@Schema`）

## 常见问题

### Q: 返回 401 Unauthorized

**原因：** Token 缺失或过期

**解决：** 重新登录获取 Token

### Q: 返回 403 Forbidden

**原因：** 权限不足

**解决：** 检查用户角色和权限配置

### Q: 返回 404 Not Found

**原因：** 接口路径错误

**解决：** 检查 Controller 的 `@RequestMapping` 路径

### Q: 返回 500 Internal Server Error

**原因：** 服务器内部错误

**解决：** 查看服务日志定位问题：

```bash
docker-compose logs -f user-service
```

## 相关文档

- [编码规范](./coding-standards.md)
- [环境配置](./environment-setup.md)
- [API 设计文档](../design-docs/api-design.md)

# EnStudApp API 设计文档

> 本文档描述 EnStudApp 全部 RESTful API 的设计规范与接口列表。
> 所有接口通过 API 网关统一路由，前端仅需对接 Nginx（端口 80）即可。

---

## 目录

1. [总体约定](#1-总体约定)
2. [认证与授权](#2-认证与授权)
3. [统一响应格式](#3-统一响应格式)
4. [分页机制](#4-分页机制)
5. [错误码规范](#5-错误码规范)
6. [各服务接口列表](#6-各服务接口列表)
   - [用户服务 user-service（8081）](#61-用户服务-user-service8081)
   - [单词服务 word-service（8082）](#62-单词服务-word-service8082)
   - [AI 对话服务 chat-service（8083）](#63-ai-对话服务-chat-service8083)
   - [写作服务 writing-service（8084）](#64-写作服务-writing-service8084)
   - [翻译服务 translate-service（8085）](#65-翻译服务-translate-service8085)
   - [论坛服务 forum-service（8086）](#66-论坛服务-forum-service8086)
   - [阅读服务 read-service（8087）](#67-阅读服务-read-service8087)

---

## 1. 总体约定

### 基础 URL

所有 API 请求通过 Nginx 反向代理转发至 API 网关：

```
http://<host>:80/api/<service-prefix>/<endpoint>
```

| 路径前缀 | 路由目标 | 服务端口 |
|----------|----------|----------|
| `/api/user/**` | user-service | 8081 |
| `/api/word/**` | word-service | 8082 |
| `/api/chat/**` | chat-service | 8083 |
| `/api/writing/**` | writing-service | 8084 |
| `/api/translate/**` | translate-service | 8085 |
| `/api/forum/**` | forum-service | 8086 |
| `/api/read/**` | read-service | 8087 |

网关地址：`http://<host>:8080`（开发时可直接调用）

### Content-Type

- 请求体：`application/json`
- 响应体：`application/json;charset=UTF-8`

### HTTP 方法

| 方法 | 用途 |
|------|------|
| `GET` | 查询（幂等） |
| `POST` | 创建或触发动作 |
| `PUT` | 更新（全量替换） |
| `DELETE` | 删除（逻辑删除） |

---

## 2. 认证与授权

### JWT Bearer Token

除注册、登录及白名单接口外，所有接口均需携带 JWT Token：

```
Authorization: Bearer <token>
```

### 认证流程

1. 客户端调用 `POST /api/user/register` 或 `POST /api/user/login` 获取 Token
2. 登录成功返回 `accessToken`（短期）和 `refreshToken`（长期）
3. 后续请求在 Header 中携带 `Authorization: Bearer <accessToken>`
4. Token 过期后使用 `refreshToken` 刷新，或重新登录

### Gateway 鉴权

- API 网关 `JwtAuthFilter` 拦截所有请求，校验 Token 有效性
- 校验通过后将用户信息写入请求头传递给下游服务：
  - `X-User-Id`：当前用户 ID
  - `X-Username`：当前用户名
- 校验失败返回 HTTP 401：`{"code":401,"msg":"Token无效或已过期","data":null}`

### 白名单路径（无需鉴权）

- `POST /user/register`
- `POST /user/login`
- 各服务 Health Check：`/actuator/**`
- Knife4j 文档：`/doc.html`, `/swagger-resources/**`, `/v3/api-docs/**`

---

## 3. 统一响应格式

所有接口统一返回 `Result<T>` 结构：

```json
{
  "code": 0,
  "msg": "success",
  "data": { ... }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | int | 状态码。`0` = 成功，非 `0` = 失败（详见[错误码规范](#5-错误码规范)） |
| `msg` | String | 提示信息。成功时为 `"success"` |
| `data` | T | 响应数据，类型随接口而定。失败时为 `null` |

### 成功示例

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "id": 1,
    "username": "john",
    "nickname": "John"
  }
}
```

### 失败示例

```json
{
  "code": 1004,
  "msg": "密码错误",
  "data": null
}
```

---

## 4. 分页机制

项目采用 **游标分页（Cursor-based Pagination）**，使用 `PageResult<T>` 封装分页响应，避免 Offset 深翻页性能问题。

### 请求参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `cursor` | String | 否 | 下一页游标（首次请求不传或传 `null`） |
| `limit` | int | 否 | 每页条数，默认 `20` |

### 响应结构 `PageResult<T>`

```json
{
  "records": [ ... ],
  "cursor": "eyJpZCI6MTAwLCJjcmVhdGVkQXQiOiIyMDI2LTA3LTAxIn0=",
  "hasMore": true
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `records` | List\<T\> | 当前页数据列表 |
| `cursor` | String | 下一页游标，为 `null` 时表示没有更多数据 |
| `hasMore` | Boolean | 是否还有更多数据 |

### 翻页示例

```
# 第 1 页
GET /api/forum/posts?limit=20

# 第 2 页（使用上次返回的 cursor）
GET /api/forum/posts?cursor=eyJpZCI6MTAwfQ==&limit=20
```

---

## 5. 错误码规范

错误码格式：**模块前缀（千位）+ 具体错误（个十百位）**。

### 通用错误码

| 错误码 | 说明 | HTTP Status |
|--------|------|-------------|
| `0` | 成功 | 200 |
| `400` | 请求参数错误 | 400 |
| `401` | 未登录或登录已过期 | 401 |
| `403` | 无权限访问 | 403 |
| `404` | 资源不存在 | 404 |
| `500` | 服务器内部错误 | 500 |

### 用户模块（1xxx）

| 错误码 | 说明 |
|--------|------|
| `1001` | 用户不存在 |
| `1002` | 用户名已存在 |
| `1003` | 邮箱已被注册 |
| `1004` | 密码错误 |

### 单词模块（2xxx）

| 错误码 | 说明 |
|--------|------|
| `2001` | 单词不存在 |
| `2002` | 词库不存在 |
| `2003` | 记忆等级必须在 0-5 之间 |
| `2004` | 尚未学习该单词，请先复习后再调整记忆等级 |

### AI 对话模块（3xxx）

| 错误码 | 说明 |
|--------|------|
| `3001` | 对话不存在 |

### 写作模块（4xxx）

| 错误码 | 说明 |
|--------|------|
| `4001` | 作文不存在 |
| `4002` | 批改结果不存在 |

### AI 通用模块（5xxx）

| 错误码 | 说明 |
|--------|------|
| `5001` | AI 返回内容为空，请稍后重试 |
| `5002` | AI 服务调用异常，请稍后重试 |

### 论坛模块（6xxx）

| 错误码 | 说明 |
|--------|------|
| `6001` | 帖子不存在 |
| `6002` | 板块不存在 |
| `6003` | 内容包含敏感词，请修改后重新提交 |
| `6004` | 内容长度超过限制 |

---

## 6. 各服务接口列表

> 以下路径为服务内部路由。前端访问时加上 `/api` 前缀通过 Nginx 转发。

---

### 6.1 用户服务 user-service（8081）

#### 用户管理

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| `POST` | `/user/register` | 用户注册 | 否 |
| `POST` | `/user/login` | 用户登录 | 否 |
| `GET` | `/user/profile` | 获取当前用户信息 | 是 |
| `GET` | `/user/stats` | 获取学习仪表盘统计数据 | 是 |
| `GET` | `/user/users/{userId}/name` | 内部接口：获取用户名 | 否（内部） |

#### 学习目标

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| `GET` | `/user/goal` | 获取用户学习目标 | 是 |
| `PUT` | `/user/goal` | 更新用户学习目标 | 是 |
| `GET` | `/user/goal/status` | 获取今日学习进度 | 是 |
| `POST` | `/user/goal/checkin` | 打卡（获得积分奖励） | 是 |

#### 今日学习之旅

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| `GET` | `/user/daily-journey` | 获取今日任务流状态（阅读/复习/对话/打卡进度聚合） | 是 |

响应字段：`date`（日期）、`streakDays`（连续天数）、`checkedIn`（是否已打卡）、`dailyWordGoal`（每日单词目标）、`todayLearnedWords`（今日学词数）、`dueReviewCount`（到期待复习数）、`todayReadArticles`（今日阅读篇数）、`todayChats`（今日对话数）。

#### 用户关注

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| `POST` | `/user/follow/{targetUserId}` | 关注用户 | 是 |
| `DELETE` | `/user/follow/{targetUserId}` | 取消关注 | 是 |
| `GET` | `/user/following` | 获取我关注的用户列表 | 是 |
| `GET` | `/user/followers` | 获取我的粉丝列表 | 是 |
| `GET` | `/user/{userId}/follow-status` | 查询是否已关注某用户 | 是 |

---

### 6.2 单词服务 word-service（8082）

#### 词库与单词

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| `GET` | `/word/wordbooks` | 获取词库列表 | 是 |
| `GET` | `/word/wordbooks/{wordbookId}/words` | 获取词库下的单词 | 是 |
| `POST` | `/word/study` | 开始学习 / 获取待学习单词 | 是 |
| `POST` | `/word/review` | 提交复习结果（SM-2 算法） | 是 |

#### 记忆等级

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| `GET` | `/word/memory-level/distribution` | 获取记忆等级分布统计 | 是 |
| `GET` | `/word/memory-level/words` | 按记忆等级筛选单词 | 是 |
| `PUT` | `/word/memory-level/adjust` | 手动调整单词记忆等级 | 是 |

#### 阅读联动

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| `POST` | `/word/reading/add` | 从阅读中新增单词/加入生词本 | 是 |

#### 错词本

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| `GET` | `/word/wrong-words` | 获取错词列表 | 是 |
| `POST` | `/word/wrong-words/review-batch` | 批量复习错词 | 是 |
| `PUT` | `/word/wrong-words/{recordId}/reset` | 重置错词状态（标记为已掌握） | 是 |

#### 词汇量测试

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| `GET` | `/word/vocab-test/words` | 获取测试单词（6 频段 × 5 词 + 2 个防乱猜伪词，共 32 词乱序） | 是 |
| `POST` | `/word/vocab-test/submit` | 提交作答并估算词汇量（`knowledge`: 2=认识, 1=模糊, 0=不认识） | 是 |
| `GET` | `/word/vocab-test/latest` | 获取最近一次测试结果（用于分享卡片） | 是 |

估算算法：各频段认知比例（认识=1，模糊=0.5）× 频段容量增量求和；伪词答"认识"每个扣 800。

#### 学习推荐

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| `GET` | `/word/recommendations/words` | 推荐单词（基于记忆等级分布） | 是 |
| `GET` | `/word/recommendations/topics` | 推荐学习主题（基于已学词库分布） | 是 |

---

### 6.3 AI 对话服务 chat-service（8083）

#### 会话管理

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| `POST` | `/chat/sessions` | 开始新会话 | 是 |
| `GET` | `/chat/sessions` | 获取会话列表 | 是 |
| `DELETE` | `/chat/sessions/{sessionId}` | 删除会话 | 是 |

#### 消息

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| `POST` | `/chat/sessions/{sessionId}/messages` | 发送消息（含语法纠正） | 是 |
| `GET` | `/chat/sessions/{sessionId}/messages` | 获取会话消息历史 | 是 |

#### 对话推荐

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| `GET` | `/chat/recommendations/topics` | 推荐对话主题（预置场景 + AI 生成） | 是 |

**对话场景枚举（scenario）：**

| 值 | 说明 |
|----|------|
| `FREE` | 自由对话 |
| `DAILY` | 日常口语 |
| `BUSINESS` | 商务英语 |
| `TRAVEL` | 旅行英语 |

---

### 6.4 写作服务 writing-service（8084）

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| `POST` | `/writing/submit` | 提交作文并获取 AI 批改 | 是 |
| `GET` | `/writing/{writingId}/correction` | 获取批改结果 | 是 |
| `GET` | `/writing/history` | 获取写作历史 | 是 |
| `GET` | `/writing/models` | 获取范文 | 是 |
| `DELETE` | `/writing/{writingId}` | 删除作文 | 是 |

**题目类型枚举（topicType）：**

| 值 | 说明 |
|----|------|
| `ESSAY` | 议论文/散文 |
| `LETTER` | 书信 |
| `SUMMARY` | 摘要 |

---

### 6.5 翻译服务 translate-service（8085）

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| `POST` | `/translate/text` | 文本翻译（英汉互译） | 是 |

> 注：语音翻译（`/translate/voice`）和图片翻译（`/translate/image`）为规划中的功能，尚未实现。

---

### 6.6 论坛服务 forum-service（8086）

#### 板块与帖子

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| `GET` | `/forum/categories` | 获取板块列表 | 否 |
| `GET` | `/forum/posts` | 获取帖子列表（支持按板块筛选） | 否 |
| `GET` | `/forum/posts/{postId}` | 获取帖子详情 | 否 |
| `POST` | `/forum/posts` | 发布帖子 | 是 |
| `DELETE` | `/forum/posts/{postId}` | 删除帖子 | 是 |

#### 回复与点赞

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| `POST` | `/forum/posts/{postId}/reply` | 回复帖子（支持楼中楼） | 是 |
| `POST` | `/forum/like` | 点赞/取消点赞（toggle） | 是 |

#### 标签

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| `GET` | `/forum/tags` | 获取热门标签 | 否 |
| `GET` | `/forum/tags/{tagId}/posts` | 获取标签下的帖子列表 | 否 |

**点赞目标类型（targetType）：**

| 值 | 说明 |
|----|------|
| `POST` | 帖子 |
| `REPLY` | 回复 |

---

### 6.7 阅读服务 read-service（8087）

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| `GET` | `/read/hot` | 获取热门文章列表 | 是 |
| `GET` | `/read/{id}` | 查看文章详情 | 是 |
| `GET` | `/read/{id}/translate` | 获取文章中文翻译（懒加载） | 是 |
| `POST` | `/read/{id}/bookmark` | 收藏/取消收藏（toggle） | 是 |
| `GET` | `/read/bookmarks` | 获取收藏列表 | 是 |
| `GET` | `/read/sources` | 获取文章来源列表 | 是 |
| `POST` | `/read/sync` | 手动触发文章同步 | 是 |
| `POST` | `/read/word-lookup` | 划词查词：翻译选中文本并加入生词本 | 是 |

**文章来源枚举（source）：**

| 值 | 说明 |
|----|------|
| `HN` | Hacker News |
| `GitHub` | GitHub Trending |
| `Medium` | Medium |
| `InfoQ` | InfoQ |
| `TechCrunch` | TechCrunch |
| `all` | 全部来源（默认） |

---

## 附录：接口快速索引

| # | 服务 | 接口数量 |
|---|------|----------|
| 1 | user-service | 12 |
| 2 | word-service | 13 |
| 3 | chat-service | 6 |
| 4 | writing-service | 5 |
| 5 | translate-service | 1 |
| 6 | forum-service | 8 |
| 7 | read-service | 8 |
| **合计** | | **53** |

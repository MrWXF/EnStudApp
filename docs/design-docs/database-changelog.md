# EnStudApp 数据库变更记录

> 本文件按版本倒序记录所有数据库 Schema 变更。
> 迁移脚本位于 `sql/init/`（初始化）和 `sql/migrations/`（增量迁移），版本化脚本位于 `sql/`。

---

## 变更汇总

| 版本 | 日期 | 变更描述 | 影响表 |
|------|------|----------|--------|
| v1.2.0 | 2026-06-08 | 新增热门英文文章阅读功能 | `enstud_article`, `enstud_article_read` |
| v1.1.0 | 2026-06-08 | 新增单词记忆程度等级系统 | `enstud_user_word_record` |
| V8 | 2026-05-xx | 用户表新增关注数/粉丝数冗余字段 | `enstud_user` |
| V7 | 2026-05-xx | 用户单词学习记录表新增答错次数字段 | `enstud_user_word_record` |
| V6 | 2026-05-xx | 新增用户关注表 | `enstud_user_follow` |
| V5 | 2026-05-xx | 新增论坛标签表 + 帖子-标签关联表 | `enstud_forum_tag`, `enstud_forum_post_tag` |
| V4 | 2026-05-xx | 新增用户学习目标表 + 每日学习记录表 | `enstud_user_goal`, `enstud_daily_study_record` |
| V3 | 2026-04-xx | 用户档案表新增连续打卡天数字段 | `enstud_user_profile` |
| V2 | 2026-04-xx | 新增写作批改表 | `enstud_writing` |
| V1 | 2026-04-xx | 新增 AI 对话会话表 + 消息表 | `enstud_chat_session`, `enstud_chat_message` |
| init | 2026-03-xx | 初始建表：用户、单词、论坛 | 9 张表 |

---

## 详细变更记录

---

### v1.2.0 — 新增热门英文文章阅读功能

**日期：** 2026-06-08
**脚本：** `sql/v1.2.0-read-articles.sql`
**关联服务：** read-service（8087）

**变更内容：**

1. **新建 `enstud_article`（热门英文文章表）**
   - 存储从 Hacker News、GitHub Trending、Medium、InfoQ、TechCrunch 等来源抓取的英文技术文章
   - 包含英文原文 + 中文翻译双语内容
   - 支持热度分排序（带时间衰减）
   - 支持按来源筛选和 URL+来源去重
   - 主要索引：`idx_score(score DESC)`, `idx_published(published_at DESC)`, `idx_url_source(url, source)`

2. **新建 `enstud_article_read`（用户阅读记录表）**
   - 记录用户阅读次数、是否收藏
   - 联合唯一约束 `(user_id, article_id)` 保证一用户一文章仅一条记录
   - 索引：`idx_user_bookmarks(user_id, is_bookmarked)` 用于快速查询收藏列表

**迁移方式：** 直接执行 `CREATE TABLE IF NOT EXISTS`

---

### v1.1.0 — 新增单词记忆程度等级系统

**日期：** 2026-06-08
**脚本：** `sql/v1.1.0-memory-level.sql`
**关联服务：** word-service（8082）

**变更内容：**

1. **`enstud_user_word_record` 新增 `memory_level` 字段**
   - 类型：`TINYINT DEFAULT 0`
   - 位置：在 `status` 字段之后
   - 枚举值：`0=未学习 1=模糊 2=有印象 3=基本掌握 4=熟练 5=精通`
   - 用于比 `mastery_level`（0-100 数值）更直观的等级划分

2. **历史数据初始化**
   - 基于已有的 `status` 和 `mastery_level` 字段自动计算初始 `memory_level`：
     - `MASTERED` 且 `mastery_level >= 90` → 等级 5（精通）
     - `mastery_level >= 61` → 等级 4（熟练）
     - `mastery_level >= 41` → 等级 3（基本掌握）
     - `mastery_level >= 21` → 等级 2（有印象）
     - 有复习记录 → 等级 1（模糊）
     - 其他 → 等级 0（未学习）

3. **新增索引 `idx_user_memory_level(user_id, memory_level)`**
   - 优化按记忆等级筛选单词的查询性能

**迁移方式：** `ALTER TABLE` + `UPDATE`（带数据迁移）

---

### V8 — 用户表新增关注数/粉丝数冗余字段

**日期：** 2026-05 月
**脚本：** `sql/migrations/V8__add_user_follow_counts.sql`
**关联服务：** user-service（8081）

**变更内容：**

1. **`enstud_user` 新增 `following_count` 字段**
   - 类型：`INT NOT NULL DEFAULT 0`
   - 位置：在 `points` 字段之后
   - 说明：关注数（冗余字段，定期与 `enstud_user_follow` 表同步）

2. **`enstud_user` 新增 `follower_count` 字段**
   - 类型：`INT NOT NULL DEFAULT 0`
   - 位置：在 `following_count` 字段之后
   - 说明：粉丝数（冗余字段，定期与 `enstud_user_follow` 表同步）

**设计说明：** 采用冗余计数避免查询关注/粉丝数时实时 COUNT，提升列表页性能。计数通过定时任务或异步事件同步。

**迁移方式：** 动态检查字段是否存在，不存在则 `ALTER TABLE ADD COLUMN`

---

### V7 — 用户单词学习记录表新增答错次数字段

**日期：** 2026-05 月
**脚本：** `sql/migrations/V7__add_wrong_count_field.sql`
**关联服务：** word-service（8082）

**变更内容：**

1. **`enstud_user_word_record` 新增 `wrong_count` 字段**
   - 类型：`INT NOT NULL DEFAULT 0`
   - 位置：在 `quality` 字段之后
   - 说明：累计答错次数，用于错词本功能筛选

**迁移方式：** 动态检查字段是否存在，不存在则 `ALTER TABLE ADD COLUMN`

---

### V6 — 新增用户关注表

**日期：** 2026-05 月
**脚本：** `sql/migrations/V6__add_user_follow_table.sql`
**关联服务：** user-service（8081）

**变更内容：**

1. **新建 `enstud_user_follow`（用户关注表）**
   - `follower_id`：关注者 ID
   - `following_id`：被关注者 ID
   - 联合唯一约束 `uk_follow(follower_id, following_id)` 防止重复关注
   - 索引：`idx_follower(follower_id)`, `idx_following(following_id)` 用于双向查询

**迁移方式：** 直接执行 `CREATE TABLE IF NOT EXISTS`

---

### V5 — 新增论坛标签表 + 帖子-标签关联表

**日期：** 2026-05 月
**脚本：** `sql/migrations/V5__add_forum_tag_tables.sql`
**关联服务：** forum-service（8086）

**变更内容：**

1. **新建 `enstud_forum_tag`（标签表）**
   - 标签名称唯一约束 `uk_name(name)`
   - `post_count` 冗余字段记录关联帖子数
   - 索引：`idx_post_count(post_count)` 用于热门标签排序

2. **新建 `enstud_forum_post_tag`（帖子-标签关联表）**
   - 多对多关系表，联合主键 `(post_id, tag_id)`
   - 索引：`idx_tag(tag_id)` 用于按标签查询帖子

**迁移方式：** 直接执行 `CREATE TABLE IF NOT EXISTS`

---

### V4 — 新增用户学习目标表 + 每日学习记录表

**日期：** 2026-05 月
**脚本：** `sql/migrations/V4__add_user_goal_tables.sql`
**关联服务：** user-service（8081）

**变更内容：**

1. **新建 `enstud_user_goal`（用户学习目标表）**
   - `user_id` 为主键，一用户一目标
   - `daily_word_goal`：每日单词目标，默认 20
   - `daily_minute_goal`：每日学习时长目标（分钟），默认 30

2. **新建 `enstud_daily_study_record`（每日学习记录表）**
   - 记录每日学习单词数、学习时长、是否完成目标
   - 联合唯一约束 `uk_user_date(user_id, study_date)` 保证一天仅一条记录
   - 索引：`idx_user_date(user_id, study_date)` 用于查询学习日历

**迁移方式：** 直接执行 `CREATE TABLE IF NOT EXISTS`

---

### V3 — 用户档案表新增连续打卡天数字段

**日期：** 2026-04 月
**脚本：** `sql/migrations/V3__add_streak_fields.sql`
**关联服务：** user-service（8081）

**变更内容：**

1. **`enstud_user_profile` 新增 `current_streak` 字段**
   - 类型：`INT NOT NULL DEFAULT 0`
   - 说明：当前连续打卡天数

2. **`enstud_user_profile` 新增 `longest_streak` 字段**
   - 类型：`INT NOT NULL DEFAULT 0`
   - 说明：最长连续打卡天数

3. **`enstud_user_profile` 新增 `last_study_date` 字段**
   - 类型：`DATE DEFAULT NULL`
   - 说明：最后学习日期，用于判断是否断签

**注意：** 初始建表脚本 `01_create_user_tables.sql` 已包含这三个字段。此迁移脚本仅用于旧版数据库升级，采用动态检查方式避免重复添加。

**迁移方式：** 动态检查字段是否存在，不存在则 `ALTER TABLE ADD COLUMN`

---

### V2 — 新增写作批改表

**日期：** 2026-04 月
**脚本：** `sql/migrations/V2__create_writing_table.sql`
**关联服务：** writing-service（8084）

**变更内容：**

1. **新建 `enstud_writing`（写作批改表）**
   - `title`：作文标题
   - `content`：作文内容（LONGTEXT）
   - `word_count`：字数
   - `topic_type`：题目类型（ESSAY/LETTER/SUMMARY）
   - `score`：AI 评分（0-100）
   - `correction`：批改结果（MEDIUMTEXT，JSON 格式）
   - 索引：`idx_user(user_id)` 用于查询写作历史

**迁移方式：** 直接执行 `CREATE TABLE IF NOT EXISTS`

---

### V1 — 新增 AI 对话会话表 + 消息表

**日期：** 2026-04 月
**脚本：** `sql/migrations/V1__create_chat_tables.sql`
**关联服务：** chat-service（8083）

**变更内容：**

1. **新建 `enstud_chat_session`（AI 对话会话表）**
   - `title`：会话标题（由 AI 自动生成或用户手动设置）
   - `scenario`：对话场景（FREE/DAILY/BUSINESS/TRAVEL）
   - `message_count`：消息计数
   - 索引：`idx_user(user_id)` 用于查询用户的会话列表

2. **新建 `enstud_chat_message`（AI 对话消息表）**
   - `session_id`：所属会话 ID
   - `role`：消息角色（USER/AI）
   - `content`：消息内容（TEXT）
   - `grammar_issues`：语法问题（JSON 数组，对 USER 消息的语法纠正结果）
   - 索引：`idx_session(session_id)` 用于查询会话内消息历史

**迁移方式：** 直接执行 `CREATE TABLE IF NOT EXISTS`

---

### init — 初始建表

**日期：** 2026-03 月
**脚本：** `sql/init/01_create_user_tables.sql`, `02_create_word_tables.sql`, `03_create_forum_tables.sql`

**变更内容：**

#### 用户模块（`01_create_user_tables.sql`）

| 表名 | 说明 |
|------|------|
| `enstud_user` | 用户表（用户名、邮箱、密码哈希、等级、积分等） |
| `enstud_user_profile` | 用户学习档案表（学习统计、词汇量等，与 user 表 1:1） |
| `enstud_user_token` | 登录 Token 表（支持 Token 黑名单/主动失效） |

#### 单词模块（`02_create_word_tables.sql`）

| 表名 | 说明 |
|------|------|
| `enstud_wordbook` | 词库表（CET4/CET6/IELTS/TOEFL 等分类） |
| `enstud_word` | 单词表（含音标、释义、例句、难度等级） |
| `enstud_user_word_record` | 用户单词学习记录表（SM-2 间隔重复算法数据） |

#### 论坛模块（`03_create_forum_tables.sql`）

| 表名 | 说明 |
|------|------|
| `enstud_forum_category` | 论坛板块表 |
| `enstud_forum_post` | 帖子表（含全文索引） |
| `enstud_forum_reply` | 回复表（支持楼中楼嵌套回复） |
| `enstud_forum_like` | 点赞记录表（多态：帖子/回复） |

**初始数据：** 插入 5 个默认论坛板块 — 学习经验、学习资源、问答求助、每日打卡、英语角

---

## 当前表清单（v1.2.0）

| # | 表名 | 所属服务 | 说明 |
|---|------|----------|------|
| 1 | `enstud_user` | user-service | 用户表 |
| 2 | `enstud_user_profile` | user-service | 用户学习档案表 |
| 3 | `enstud_user_token` | user-service | 登录 Token 表 |
| 4 | `enstud_user_goal` | user-service | 用户学习目标表 |
| 5 | `enstud_user_follow` | user-service | 用户关注表 |
| 6 | `enstud_daily_study_record` | user-service | 每日学习记录表 |
| 7 | `enstud_wordbook` | word-service | 词库表 |
| 8 | `enstud_word` | word-service | 单词表 |
| 9 | `enstud_user_word_record` | word-service | 用户单词学习记录表 |
| 10 | `enstud_chat_session` | chat-service | AI 对话会话表 |
| 11 | `enstud_chat_message` | chat-service | AI 对话消息表 |
| 12 | `enstud_writing` | writing-service | 写作批改表 |
| 13 | `enstud_forum_category` | forum-service | 论坛板块表 |
| 14 | `enstud_forum_post` | forum-service | 帖子表 |
| 15 | `enstud_forum_reply` | forum-service | 回复表 |
| 16 | `enstud_forum_like` | forum-service | 点赞记录表 |
| 17 | `enstud_forum_tag` | forum-service | 标签表 |
| 18 | `enstud_forum_post_tag` | forum-service | 帖子-标签关联表 |
| 19 | `enstud_article` | read-service | 热门英文文章表 |
| 20 | `enstud_article_read` | read-service | 用户阅读记录表 |

**共计 20 张表。**

完整建表 SQL 请参考：[database-schema.sql](./database-schema.sql)

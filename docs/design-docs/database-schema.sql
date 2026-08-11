# EnStudApp 数据库设计文档

> 本文档为 EnStudApp 完整数据库 Schema 参考，包含所有表的定义与说明。
> 数据库：MySQL 8.0，字符集：utf8mb4，排序规则：utf8mb4_unicode_ci
> 所有表名统一使用 `enstud_` 前缀。

---

## 目录

1. [用户服务（user-service）](#1-用户服务user-service)
   - `enstud_user` 用户表
   - `enstud_user_profile` 用户学习档案表
   - `enstud_user_token` 登录 Token 表
   - `enstud_user_goal` 用户学习目标表
   - `enstud_user_follow` 用户关注表
   - `enstud_daily_study_record` 每日学习记录表
2. [单词服务（word-service）](#2-单词服务word-service)
   - `enstud_wordbook` 词库表
   - `enstud_word` 单词表
   - `enstud_user_word_record` 用户单词学习记录表
3. [AI 对话服务（chat-service）](#3-ai-对话服务chat-service)
   - `enstud_chat_session` AI 对话会话表
   - `enstud_chat_message` AI 对话消息表
4. [写作服务（writing-service）](#4-写作服务writing-service)
   - `enstud_writing` 写作批改表
5. [论坛服务（forum-service）](#5-论坛服务forum-service)
   - `enstud_forum_category` 论坛板块表
   - `enstud_forum_post` 帖子表
   - `enstud_forum_reply` 回复表
   - `enstud_forum_like` 点赞记录表
   - `enstud_forum_tag` 标签表
   - `enstud_forum_post_tag` 帖子-标签关联表
6. [阅读服务（read-service）](#6-阅读服务read-service)
   - `enstud_article` 热门英文文章表
   - `enstud_article_read` 用户阅读记录表

---

## 1. 用户服务（user-service）

### enstud_user — 用户表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 用户 ID |
| `username` | VARCHAR(50) | NOT NULL, UNIQUE | 用户名 |
| `email` | VARCHAR(100) | NOT NULL, UNIQUE | 邮箱 |
| `password_hash` | VARCHAR(255) | NOT NULL | 密码哈希（BCrypt） |
| `avatar_url` | VARCHAR(500) | DEFAULT NULL | 头像 URL |
| `nickname` | VARCHAR(50) | DEFAULT NULL | 昵称 |
| `level` | INT | NOT NULL, DEFAULT 1 | 用户等级 |
| `points` | INT | NOT NULL, DEFAULT 0 | 积分 |
| `following_count` | INT | NOT NULL, DEFAULT 0 | 关注数（冗余，定期同步） |
| `follower_count` | INT | NOT NULL, DEFAULT 0 | 粉丝数（冗余，定期同步） |
| `last_login_at` | DATETIME | DEFAULT NULL | 最后登录时间 |
| `is_deleted` | TINYINT | NOT NULL, DEFAULT 0 | 逻辑删除 0-正常 1-删除 |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| `updated_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引：** `uk_username(username)`, `uk_email(email)`

---

### enstud_user_profile — 用户学习档案表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `user_id` | BIGINT | PK, FK → enstud_user(id) | 用户 ID |
| `total_words_learned` | INT | NOT NULL, DEFAULT 0 | 累计学习单词数 |
| `vocabulary_size` | INT | NOT NULL, DEFAULT 0 | 词汇量估算 |
| `writing_count` | INT | NOT NULL, DEFAULT 0 | 写作次数 |
| `chat_count` | INT | NOT NULL, DEFAULT 0 | 对话次数 |
| `current_streak` | INT | NOT NULL, DEFAULT 0 | 当前连续打卡天数 |
| `longest_streak` | INT | NOT NULL, DEFAULT 0 | 最长连续打卡天数 |
| `last_study_date` | DATE | DEFAULT NULL | 最后学习日期 |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| `updated_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

---

### enstud_user_token — 登录 Token 表

> 用于 Token 黑名单 / 主动失效场景。

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | ID |
| `user_id` | BIGINT | NOT NULL | 用户 ID |
| `token` | VARCHAR(500) | NOT NULL | Token |
| `token_type` | VARCHAR(20) | NOT NULL, DEFAULT 'ACCESS' | Token 类型 ACCESS/REFRESH |
| `expires_at` | DATETIME | NOT NULL | 过期时间 |
| `is_revoked` | TINYINT | NOT NULL, DEFAULT 0 | 是否已吊销 0-否 1-是 |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引：** `idx_user_id(user_id)`, `idx_token(token(100))`

---

### enstud_user_goal — 用户学习目标表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `user_id` | BIGINT | PK | 用户 ID |
| `daily_word_goal` | INT | NOT NULL, DEFAULT 20 | 每日单词目标 |
| `daily_minute_goal` | INT | NOT NULL, DEFAULT 30 | 每日学习时长目标（分钟） |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| `updated_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

---

### enstud_user_follow — 用户关注表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | ID |
| `follower_id` | BIGINT | NOT NULL | 关注者 ID |
| `following_id` | BIGINT | NOT NULL | 被关注者 ID |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引：** `uk_follow(follower_id, following_id)`, `idx_follower(follower_id)`, `idx_following(following_id)`

---

### enstud_daily_study_record — 每日学习记录表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 记录 ID |
| `user_id` | BIGINT | NOT NULL | 用户 ID |
| `study_date` | DATE | NOT NULL | 学习日期 |
| `words_learned` | INT | NOT NULL, DEFAULT 0 | 当日学习单词数 |
| `minutes_studied` | INT | NOT NULL, DEFAULT 0 | 当日学习时长（分钟） |
| `goal_completed` | TINYINT | NOT NULL, DEFAULT 0 | 是否完成目标 0-否 1-是 |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| `updated_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引：** `uk_user_date(user_id, study_date)`, `idx_user_date(user_id, study_date)`

---

## 2. 单词服务（word-service）

### enstud_wordbook — 词库表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 词库 ID |
| `name` | VARCHAR(100) | NOT NULL | 词库名称 |
| `description` | VARCHAR(500) | DEFAULT NULL | 词库描述 |
| `cover_url` | VARCHAR(500) | DEFAULT NULL | 封面图 |
| `word_count` | INT | NOT NULL, DEFAULT 0 | 单词数量 |
| `difficulty` | INT | NOT NULL, DEFAULT 1 | 难度等级 1-5 |
| `category` | VARCHAR(50) | NOT NULL | 分类 CET4/CET6/IELTS/TOEFL/BUSINESS/CUSTOM |
| `is_official` | TINYINT | NOT NULL, DEFAULT 1 | 是否官方词库 0-否 1-是 |
| `creator_id` | BIGINT | DEFAULT NULL | 创建者 ID（自定义词库） |
| `is_deleted` | TINYINT | NOT NULL, DEFAULT 0 | 逻辑删除 |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| `updated_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引：** `idx_category(category)`, `idx_creator(creator_id)`

---

### enstud_word — 单词表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 单词 ID |
| `word` | VARCHAR(100) | NOT NULL | 单词 |
| `phonetic_uk` | VARCHAR(100) | DEFAULT NULL | 英式音标 |
| `phonetic_us` | VARCHAR(100) | DEFAULT NULL | 美式音标 |
| `pronunciation_url` | VARCHAR(500) | DEFAULT NULL | 发音音频 URL |
| `definition_cn` | TEXT | DEFAULT NULL | 中文释义 |
| `definition_en` | TEXT | DEFAULT NULL | 英文释义 |
| `example_sentence` | TEXT | DEFAULT NULL | 例句 |
| `example_cn` | TEXT | DEFAULT NULL | 例句中文翻译 |
| `part_of_speech` | VARCHAR(30) | DEFAULT NULL | 词性 n/v/adj/adv |
| `difficulty_level` | INT | NOT NULL, DEFAULT 1 | 难度等级 1-5 |
| `wordbook_id` | BIGINT | DEFAULT NULL | 所属词库 ID |
| `is_deleted` | TINYINT | NOT NULL, DEFAULT 0 | 逻辑删除 |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| `updated_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引：** `idx_word(word)`, `idx_wordbook(wordbook_id)`

---

### enstud_user_word_record — 用户单词学习记录表

> 基于 SM-2 间隔重复记忆算法，记录每个用户的单词学习进度。

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 记录 ID |
| `user_id` | BIGINT | NOT NULL | 用户 ID |
| `word_id` | BIGINT | NOT NULL | 单词 ID |
| `ease_factor` | DOUBLE | NOT NULL, DEFAULT 2.5 | 简易度系数（SM-2） |
| `interval` | INT | NOT NULL, DEFAULT 0 | 间隔天数（SM-2） |
| `repetitions` | INT | NOT NULL, DEFAULT 0 | 复习次数（SM-2） |
| `quality` | INT | NOT NULL, DEFAULT 0 | 最近一次答题质量 0-5 |
| `wrong_count` | INT | NOT NULL, DEFAULT 0 | 答错次数 |
| `mastery_level` | INT | NOT NULL, DEFAULT 0 | 掌握程度 0-100 |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'LEARNING' | 状态 LEARNING/REVIEWING/MASTERED |
| `memory_level` | TINYINT | DEFAULT 0 | 记忆等级 0=未学习 1=模糊 2=有印象 3=基本掌握 4=熟练 5=精通 |
| `next_review_time` | DATETIME | DEFAULT NULL | 下次复习时间 |
| `last_review_time` | DATETIME | DEFAULT NULL | 最近复习时间 |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| `updated_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引：** `uk_user_word(user_id, word_id)`, `idx_next_review(user_id, next_review_time)`, `idx_user_memory_level(user_id, memory_level)`

---

## 3. AI 对话服务（chat-service）

### enstud_chat_session — AI 对话会话表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 会话 ID |
| `user_id` | BIGINT | NOT NULL | 用户 ID |
| `title` | VARCHAR(100) | DEFAULT NULL | 会话标题 |
| `scenario` | VARCHAR(50) | DEFAULT 'FREE' | 对话场景 FREE/DAILY/BUSINESS/TRAVEL |
| `message_count` | INT | NOT NULL, DEFAULT 0 | 消息数 |
| `is_deleted` | TINYINT | NOT NULL, DEFAULT 0 | 逻辑删除 |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| `updated_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引：** `idx_user(user_id)`

---

### enstud_chat_message — AI 对话消息表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 消息 ID |
| `session_id` | BIGINT | NOT NULL | 会话 ID |
| `role` | VARCHAR(20) | NOT NULL | 角色 USER/AI |
| `content` | TEXT | NOT NULL | 消息内容 |
| `grammar_issues` | TEXT | DEFAULT NULL | 语法问题（JSON 数组） |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引：** `idx_session(session_id)`

---

## 4. 写作服务（writing-service）

### enstud_writing — 写作批改表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 作文 ID |
| `user_id` | BIGINT | NOT NULL | 用户 ID |
| `title` | VARCHAR(200) | NOT NULL | 作文标题 |
| `content` | LONGTEXT | NOT NULL | 作文内容 |
| `word_count` | INT | NOT NULL, DEFAULT 0 | 字数 |
| `topic_type` | VARCHAR(50) | DEFAULT NULL | 题目类型 ESSAY/LETTER/SUMMARY |
| `score` | INT | DEFAULT NULL | 评分 0-100 |
| `correction` | MEDIUMTEXT | DEFAULT NULL | 批改结果（JSON） |
| `is_deleted` | TINYINT | NOT NULL, DEFAULT 0 | 逻辑删除 |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| `updated_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引：** `idx_user(user_id)`

---

## 5. 论坛服务（forum-service）

### enstud_forum_category — 论坛板块表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 板块 ID |
| `name` | VARCHAR(50) | NOT NULL | 板块名称 |
| `description` | VARCHAR(200) | DEFAULT NULL | 板块描述 |
| `icon` | VARCHAR(100) | DEFAULT NULL | 图标 |
| `sort_order` | INT | NOT NULL, DEFAULT 0 | 排序 |
| `is_deleted` | TINYINT | NOT NULL, DEFAULT 0 | 逻辑删除 |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| `updated_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**默认板块：** 学习经验、学习资源、问答求助、每日打卡、英语角

---

### enstud_forum_post — 帖子表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 帖子 ID |
| `title` | VARCHAR(200) | NOT NULL | 标题 |
| `content` | LONGTEXT | NOT NULL | 内容 |
| `summary` | VARCHAR(500) | DEFAULT NULL | 摘要 |
| `author_id` | BIGINT | NOT NULL | 作者 ID |
| `category_id` | BIGINT | NOT NULL | 板块 ID |
| `tags` | VARCHAR(500) | DEFAULT NULL | 标签（逗号分隔，旧字段） |
| `view_count` | INT | NOT NULL, DEFAULT 0 | 浏览量 |
| `like_count` | INT | NOT NULL, DEFAULT 0 | 点赞数 |
| `reply_count` | INT | NOT NULL, DEFAULT 0 | 回复数 |
| `collect_count` | INT | NOT NULL, DEFAULT 0 | 收藏数 |
| `is_pinned` | TINYINT | NOT NULL, DEFAULT 0 | 是否置顶 |
| `is_essence` | TINYINT | NOT NULL, DEFAULT 0 | 是否精华 |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'PUBLISHED' | 状态 PUBLISHED/DRAFT/HIDDEN |
| `is_deleted` | TINYINT | NOT NULL, DEFAULT 0 | 逻辑删除 |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| `updated_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引：** `idx_author(author_id)`, `idx_category(category_id)`, `idx_created(created_at)`, `ft_title_content(title, content)` 全文索引

---

### enstud_forum_reply — 回复表

> 支持楼中楼嵌套回复（通过 `parent_id`）。

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 回复 ID |
| `post_id` | BIGINT | NOT NULL | 帖子 ID |
| `parent_id` | BIGINT | DEFAULT NULL | 父回复 ID（楼中楼） |
| `content` | TEXT | NOT NULL | 回复内容 |
| `author_id` | BIGINT | NOT NULL | 作者 ID |
| `like_count` | INT | NOT NULL, DEFAULT 0 | 点赞数 |
| `is_deleted` | TINYINT | NOT NULL, DEFAULT 0 | 逻辑删除 |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| `updated_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引：** `idx_post(post_id)`, `idx_author(author_id)`

---

### enstud_forum_like — 点赞记录表

> 多态关联，同时支持帖子点赞和回复点赞。

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | ID |
| `user_id` | BIGINT | NOT NULL | 用户 ID |
| `target_type` | VARCHAR(20) | NOT NULL | 目标类型 POST/REPLY |
| `target_id` | BIGINT | NOT NULL | 目标 ID |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引：** `uk_user_target(user_id, target_type, target_id)` 唯一约束

---

### enstud_forum_tag — 标签表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 标签 ID |
| `name` | VARCHAR(50) | NOT NULL | 标签名称 |
| `post_count` | INT | NOT NULL, DEFAULT 0 | 关联帖子数 |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引：** `uk_name(name)`, `idx_post_count(post_count)`

---

### enstud_forum_post_tag — 帖子-标签关联表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `post_id` | BIGINT | PK (联合) | 帖子 ID |
| `tag_id` | BIGINT | PK (联合) | 标签 ID |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**索引：** `idx_tag(tag_id)`

---

## 6. 阅读服务（read-service）

### enstud_article — 热门英文文章表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 主键 |
| `title` | VARCHAR(500) | NOT NULL | 英文标题 |
| `title_cn` | VARCHAR(500) | DEFAULT NULL | 中文翻译标题 |
| `url` | VARCHAR(1000) | NOT NULL | 原文链接 |
| `source` | VARCHAR(50) | NOT NULL | 文章来源 HN/GitHub/Medium/InfoQ/TechCrunch |
| `summary` | TEXT | DEFAULT NULL | 英文摘要 |
| `summary_cn` | TEXT | DEFAULT NULL | 中文翻译摘要 |
| `content` | LONGTEXT | DEFAULT NULL | 英文原文 |
| `content_cn` | LONGTEXT | DEFAULT NULL | 中文翻译全文 |
| `source_score` | INT | DEFAULT 0 | 原始热度分 |
| `score` | INT | DEFAULT 0 | 计算后热度分（带时间衰减） |
| `published_at` | DATETIME | DEFAULT NULL | 原文发布时间 |
| `fetched_at` | DATETIME | DEFAULT NULL | 抓取时间 |
| `cover_url` | VARCHAR(1000) | DEFAULT NULL | 封面图 URL |
| `source_icon` | VARCHAR(500) | DEFAULT NULL | 来源图标 URL |
| `author` | VARCHAR(200) | DEFAULT NULL | 作者 |
| `is_deleted` | TINYINT | DEFAULT 0 | 逻辑删除 |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| `updated_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引：** `idx_source(source)`, `idx_score(score DESC)`, `idx_published(published_at DESC)`, `idx_url_source(url(255), source)` 去重索引

---

### enstud_article_read — 用户阅读记录表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| `id` | BIGINT | PK, AUTO_INCREMENT | 主键 |
| `user_id` | BIGINT | NOT NULL | 用户 ID |
| `article_id` | BIGINT | NOT NULL | 文章 ID |
| `read_count` | INT | DEFAULT 1 | 阅读次数 |
| `is_bookmarked` | TINYINT(1) | DEFAULT 0 | 是否收藏 |
| `created_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| `updated_at` | DATETIME | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引：** `uk_user_article(user_id, article_id)`, `idx_user_bookmarks(user_id, is_bookmarked)`

---

## 表关系概览

```
enstud_user (1) ──→ (1) enstud_user_profile        用户学习档案
enstud_user (1) ──→ (N) enstud_user_token          登录 Token
enstud_user (1) ──→ (0..1) enstud_user_goal        学习目标
enstud_user (1) ──→ (N) enstud_user_follow         关注关系
enstud_user (1) ──→ (N) enstud_daily_study_record  每日学习记录

enstud_wordbook (1) ──→ (N) enstud_word             词库-单词
enstud_user (1) ──→ (N) enstud_user_word_record    用户-单词学习记录
enstud_word (1) ──→ (N) enstud_user_word_record    单词-学习记录

enstud_chat_session (1) ──→ (N) enstud_chat_message 会话-消息

enstud_forum_category (1) ──→ (N) enstud_forum_post  板块-帖子
enstud_forum_post (1) ──→ (N) enstud_forum_reply     帖子-回复
enstud_forum_post (1) ──→ (N) enstud_forum_like      帖子-点赞（多态）
enstud_forum_reply (1) ──→ (N) enstud_forum_like     回复-点赞（多态）
enstud_forum_post (M) ──↔ (M) enstud_forum_tag      帖子-标签（通过 post_tag 关联）

enstud_article (1) ──→ (N) enstud_article_read       文章-阅读记录
```

---

## 完整建表 SQL

> 以下为合并后的完整建表 SQL，适用于从零初始化数据库。
> 已整合 init 脚本与所有迁移脚本（V1-V8 + v1.1.0 + v1.2.0）。

```sql
-- ============================================
-- EnStudApp 完整数据库 Schema
-- 版本: v1.2.0 (合并全部初始化 + 迁移脚本)
-- 数据库: MySQL 8.0
-- 字符集: utf8mb4 / utf8mb4_unicode_ci
-- ============================================

-- ==========================================
-- 第一部分：用户服务（user-service）
-- ==========================================

-- 用户表
CREATE TABLE IF NOT EXISTS `enstud_user` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
    `username`        VARCHAR(50)  NOT NULL                 COMMENT '用户名',
    `email`           VARCHAR(100) NOT NULL                 COMMENT '邮箱',
    `password_hash`   VARCHAR(255) NOT NULL                 COMMENT '密码哈希（BCrypt）',
    `avatar_url`      VARCHAR(500) DEFAULT NULL             COMMENT '头像URL',
    `nickname`        VARCHAR(50)  DEFAULT NULL             COMMENT '昵称',
    `level`           INT          NOT NULL DEFAULT 1       COMMENT '用户等级',
    `points`          INT          NOT NULL DEFAULT 0       COMMENT '积分',
    `following_count` INT          NOT NULL DEFAULT 0       COMMENT '关注数',
    `follower_count`  INT          NOT NULL DEFAULT 0       COMMENT '粉丝数',
    `last_login_at`   DATETIME     DEFAULT NULL             COMMENT '最后登录时间',
    `is_deleted`      TINYINT      NOT NULL DEFAULT 0       COMMENT '逻辑删除 0-正常 1-删除',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 用户学习档案表
CREATE TABLE IF NOT EXISTS `enstud_user_profile` (
    `user_id`             BIGINT  NOT NULL                 COMMENT '用户ID',
    `total_words_learned` INT     NOT NULL DEFAULT 0       COMMENT '累计学习单词数',
    `vocabulary_size`     INT     NOT NULL DEFAULT 0       COMMENT '词汇量估算',
    `writing_count`       INT     NOT NULL DEFAULT 0       COMMENT '写作次数',
    `chat_count`          INT     NOT NULL DEFAULT 0       COMMENT '对话次数',
    `current_streak`      INT     NOT NULL DEFAULT 0       COMMENT '当前连续打卡天数',
    `longest_streak`      INT     NOT NULL DEFAULT 0       COMMENT '最长连续打卡天数',
    `last_study_date`     DATE    DEFAULT NULL             COMMENT '最后学习日期',
    `created_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`user_id`),
    CONSTRAINT `fk_profile_user` FOREIGN KEY (`user_id`) REFERENCES `enstud_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户学习档案表';

-- 登录Token表（用于Token黑名单/主动失效）
CREATE TABLE IF NOT EXISTS `enstud_user_token` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    `user_id`     BIGINT       NOT NULL                 COMMENT '用户ID',
    `token`       VARCHAR(500) NOT NULL                 COMMENT 'Token',
    `token_type`  VARCHAR(20)  NOT NULL DEFAULT 'ACCESS' COMMENT 'Token类型 ACCESS/REFRESH',
    `expires_at`  DATETIME     NOT NULL                 COMMENT '过期时间',
    `is_revoked`  TINYINT      NOT NULL DEFAULT 0       COMMENT '是否已吊销 0-否 1-是',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_token` (`token`(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录Token表';

-- 用户学习目标表
CREATE TABLE IF NOT EXISTS `enstud_user_goal` (
    `user_id`           BIGINT   NOT NULL                 COMMENT '用户ID',
    `daily_word_goal`   INT      NOT NULL DEFAULT 20      COMMENT '每日单词目标',
    `daily_minute_goal` INT      NOT NULL DEFAULT 30      COMMENT '每日学习时长目标（分钟）',
    `created_at`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户学习目标表';

-- 用户关注表
CREATE TABLE IF NOT EXISTS `enstud_user_follow` (
    `id`           BIGINT   NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    `follower_id`  BIGINT   NOT NULL                 COMMENT '关注者ID',
    `following_id` BIGINT   NOT NULL                 COMMENT '被关注者ID',
    `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follow` (`follower_id`, `following_id`),
    KEY `idx_follower` (`follower_id`),
    KEY `idx_following` (`following_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注表';

-- 每日学习记录表
CREATE TABLE IF NOT EXISTS `enstud_daily_study_record` (
    `id`              BIGINT   NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
    `user_id`         BIGINT   NOT NULL                 COMMENT '用户ID',
    `study_date`      DATE     NOT NULL                 COMMENT '学习日期',
    `words_learned`   INT      NOT NULL DEFAULT 0       COMMENT '当日学习单词数',
    `minutes_studied` INT      NOT NULL DEFAULT 0       COMMENT '当日学习时长（分钟）',
    `goal_completed`  TINYINT  NOT NULL DEFAULT 0       COMMENT '是否完成目标 0-否 1-是',
    `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `study_date`),
    KEY `idx_user_date` (`user_id`, `study_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日学习记录表';

-- ==========================================
-- 第二部分：单词服务（word-service）
-- ==========================================

-- 词库表
CREATE TABLE IF NOT EXISTS `enstud_wordbook` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '词库ID',
    `name`        VARCHAR(100) NOT NULL                 COMMENT '词库名称',
    `description` VARCHAR(500) DEFAULT NULL             COMMENT '词库描述',
    `cover_url`   VARCHAR(500) DEFAULT NULL             COMMENT '封面图',
    `word_count`  INT          NOT NULL DEFAULT 0       COMMENT '单词数量',
    `difficulty`  INT          NOT NULL DEFAULT 1       COMMENT '难度等级 1-5',
    `category`    VARCHAR(50)  NOT NULL                 COMMENT '分类 CET4/CET6/IELTS/TOEFL/BUSINESS/CUSTOM',
    `is_official` TINYINT      NOT NULL DEFAULT 1       COMMENT '是否官方词库 0-否 1-是',
    `creator_id`  BIGINT       DEFAULT NULL             COMMENT '创建者ID（自定义词库）',
    `is_deleted`  TINYINT      NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_creator` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='词库表';

-- 单词表
CREATE TABLE IF NOT EXISTS `enstud_word` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '单词ID',
    `word`              VARCHAR(100) NOT NULL                 COMMENT '单词',
    `phonetic_uk`       VARCHAR(100) DEFAULT NULL             COMMENT '英式音标',
    `phonetic_us`       VARCHAR(100) DEFAULT NULL             COMMENT '美式音标',
    `pronunciation_url` VARCHAR(500) DEFAULT NULL             COMMENT '发音音频URL',
    `definition_cn`     TEXT         DEFAULT NULL             COMMENT '中文释义',
    `definition_en`     TEXT         DEFAULT NULL             COMMENT '英文释义',
    `example_sentence`  TEXT         DEFAULT NULL             COMMENT '例句',
    `example_cn`        TEXT         DEFAULT NULL             COMMENT '例句中文翻译',
    `part_of_speech`    VARCHAR(30)  DEFAULT NULL             COMMENT '词性 n/v/adj/adv',
    `difficulty_level`  INT          NOT NULL DEFAULT 1       COMMENT '难度等级 1-5',
    `wordbook_id`       BIGINT       DEFAULT NULL             COMMENT '所属词库ID',
    `is_deleted`        TINYINT      NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_word` (`word`),
    KEY `idx_wordbook` (`wordbook_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='单词表';

-- 用户单词学习记录表（SM-2 记忆算法数据）
CREATE TABLE IF NOT EXISTS `enstud_user_word_record` (
    `id`               BIGINT      NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
    `user_id`          BIGINT      NOT NULL                 COMMENT '用户ID',
    `word_id`          BIGINT      NOT NULL                 COMMENT '单词ID',
    `ease_factor`      DOUBLE      NOT NULL DEFAULT 2.5    COMMENT '简易度系数（SM-2）',
    `interval`         INT         NOT NULL DEFAULT 0       COMMENT '间隔天数（SM-2）',
    `repetitions`      INT         NOT NULL DEFAULT 0       COMMENT '复习次数（SM-2）',
    `quality`          INT         NOT NULL DEFAULT 0       COMMENT '最近一次答题质量 0-5',
    `wrong_count`      INT         NOT NULL DEFAULT 0       COMMENT '答错次数',
    `mastery_level`    INT         NOT NULL DEFAULT 0       COMMENT '掌握程度 0-100',
    `status`           VARCHAR(20) NOT NULL DEFAULT 'LEARNING' COMMENT '状态 LEARNING/REVIEWING/MASTERED',
    `memory_level`     TINYINT     DEFAULT 0                COMMENT '记忆等级 0=未学习 1=模糊 2=有印象 3=基本掌握 4=熟练 5=精通',
    `next_review_time` DATETIME    DEFAULT NULL             COMMENT '下次复习时间',
    `last_review_time` DATETIME    DEFAULT NULL             COMMENT '最近复习时间',
    `created_at`       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_word` (`user_id`, `word_id`),
    KEY `idx_next_review` (`user_id`, `next_review_time`),
    KEY `idx_user_memory_level` (`user_id`, `memory_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户单词学习记录表';

-- ==========================================
-- 第三部分：AI 对话服务（chat-service）
-- ==========================================

-- AI对话会话表
CREATE TABLE IF NOT EXISTS `enstud_chat_session` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    `user_id`       BIGINT       NOT NULL                COMMENT '用户ID',
    `title`         VARCHAR(100) DEFAULT NULL            COMMENT '会话标题',
    `scenario`      VARCHAR(50)  DEFAULT 'FREE'          COMMENT '对话场景 FREE/DAILY/BUSINESS/TRAVEL',
    `message_count` INT          NOT NULL DEFAULT 0      COMMENT '消息数',
    `is_deleted`    TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话会话表';

-- AI对话消息表
CREATE TABLE IF NOT EXISTS `enstud_chat_message` (
    `id`             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `session_id`     BIGINT      NOT NULL                COMMENT '会话ID',
    `role`           VARCHAR(20) NOT NULL                COMMENT '角色 USER/AI',
    `content`        TEXT        NOT NULL                COMMENT '消息内容',
    `grammar_issues` TEXT        DEFAULT NULL             COMMENT '语法问题（JSON数组）',
    `created_at`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话消息表';

-- ==========================================
-- 第四部分：写作服务（writing-service）
-- ==========================================

-- 写作批改表
CREATE TABLE IF NOT EXISTS `enstud_writing` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '作文ID',
    `user_id`    BIGINT       NOT NULL                COMMENT '用户ID',
    `title`      VARCHAR(200) NOT NULL                COMMENT '作文标题',
    `content`    LONGTEXT     NOT NULL                COMMENT '作文内容',
    `word_count` INT          NOT NULL DEFAULT 0      COMMENT '字数',
    `topic_type` VARCHAR(50)  DEFAULT NULL            COMMENT '题目类型 ESSAY/LETTER/SUMMARY',
    `score`      INT          DEFAULT NULL            COMMENT '评分 0-100',
    `correction` MEDIUMTEXT   DEFAULT NULL            COMMENT '批改结果（JSON）',
    `is_deleted` TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='写作批改表';

-- ==========================================
-- 第五部分：论坛服务（forum-service）
-- ==========================================

-- 论坛板块表
CREATE TABLE IF NOT EXISTS `enstud_forum_category` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '板块ID',
    `name`        VARCHAR(50)  NOT NULL                 COMMENT '板块名称',
    `description` VARCHAR(200) DEFAULT NULL             COMMENT '板块描述',
    `icon`        VARCHAR(100) DEFAULT NULL             COMMENT '图标',
    `sort_order`  INT          NOT NULL DEFAULT 0       COMMENT '排序',
    `is_deleted`  TINYINT      NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛板块表';

-- 帖子表
CREATE TABLE IF NOT EXISTS `enstud_forum_post` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '帖子ID',
    `title`         VARCHAR(200) NOT NULL                 COMMENT '标题',
    `content`       LONGTEXT     NOT NULL                 COMMENT '内容',
    `summary`       VARCHAR(500) DEFAULT NULL             COMMENT '摘要',
    `author_id`     BIGINT       NOT NULL                 COMMENT '作者ID',
    `category_id`   BIGINT       NOT NULL                 COMMENT '板块ID',
    `tags`          VARCHAR(500) DEFAULT NULL             COMMENT '标签（逗号分隔）',
    `view_count`    INT          NOT NULL DEFAULT 0       COMMENT '浏览量',
    `like_count`    INT          NOT NULL DEFAULT 0       COMMENT '点赞数',
    `reply_count`   INT          NOT NULL DEFAULT 0       COMMENT '回复数',
    `collect_count` INT          NOT NULL DEFAULT 0       COMMENT '收藏数',
    `is_pinned`     TINYINT      NOT NULL DEFAULT 0       COMMENT '是否置顶',
    `is_essence`    TINYINT      NOT NULL DEFAULT 0       COMMENT '是否精华',
    `status`        VARCHAR(20)  NOT NULL DEFAULT 'PUBLISHED' COMMENT '状态 PUBLISHED/DRAFT/HIDDEN',
    `is_deleted`    TINYINT      NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_author` (`author_id`),
    KEY `idx_category` (`category_id`),
    KEY `idx_created` (`created_at`),
    FULLTEXT KEY `ft_title_content` (`title`, `content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';

-- 回复表
CREATE TABLE IF NOT EXISTS `enstud_forum_reply` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT  COMMENT '回复ID',
    `post_id`     BIGINT   NOT NULL                 COMMENT '帖子ID',
    `parent_id`   BIGINT   DEFAULT NULL             COMMENT '父回复ID（楼中楼）',
    `content`     TEXT     NOT NULL                 COMMENT '回复内容',
    `author_id`   BIGINT   NOT NULL                 COMMENT '作者ID',
    `like_count`  INT      NOT NULL DEFAULT 0       COMMENT '点赞数',
    `is_deleted`  TINYINT  NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_post` (`post_id`),
    KEY `idx_author` (`author_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回复表';

-- 点赞记录表
CREATE TABLE IF NOT EXISTS `enstud_forum_like` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    `user_id`     BIGINT      NOT NULL                 COMMENT '用户ID',
    `target_type` VARCHAR(20) NOT NULL                 COMMENT '目标类型 POST/REPLY',
    `target_id`   BIGINT      NOT NULL                 COMMENT '目标ID',
    `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞记录表';

-- 标签表
CREATE TABLE IF NOT EXISTS `enstud_forum_tag` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT  COMMENT '标签ID',
    `name`       VARCHAR(50) NOT NULL                 COMMENT '标签名称',
    `post_count` INT         NOT NULL DEFAULT 0       COMMENT '关联帖子数',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_post_count` (`post_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- 帖子-标签关联表（多对多）
CREATE TABLE IF NOT EXISTS `enstud_forum_post_tag` (
    `post_id`    BIGINT   NOT NULL                 COMMENT '帖子ID',
    `tag_id`     BIGINT   NOT NULL                 COMMENT '标签ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`post_id`, `tag_id`),
    KEY `idx_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子-标签关联表';

-- ==========================================
-- 第六部分：阅读服务（read-service）
-- ==========================================

-- 热门英文文章表
CREATE TABLE IF NOT EXISTS `enstud_article` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`       VARCHAR(500)  NOT NULL                COMMENT '英文标题',
    `title_cn`    VARCHAR(500)  DEFAULT NULL            COMMENT '中文翻译标题',
    `url`         VARCHAR(1000) NOT NULL                COMMENT '原文链接',
    `source`      VARCHAR(50)   NOT NULL                COMMENT '文章来源 (HN/GitHub/Medium/InfoQ/TechCrunch)',
    `summary`     TEXT          DEFAULT NULL            COMMENT '英文摘要',
    `summary_cn`  TEXT          DEFAULT NULL            COMMENT '中文翻译摘要',
    `content`     LONGTEXT      DEFAULT NULL            COMMENT '英文原文',
    `content_cn`  LONGTEXT      DEFAULT NULL            COMMENT '中文翻译全文',
    `source_score` INT          DEFAULT 0               COMMENT '原始热度分',
    `score`       INT           DEFAULT 0               COMMENT '计算后热度分（带时间衰减）',
    `published_at` DATETIME     DEFAULT NULL            COMMENT '原文发布时间',
    `fetched_at`  DATETIME      DEFAULT NULL            COMMENT '抓取时间',
    `cover_url`   VARCHAR(1000) DEFAULT NULL            COMMENT '封面图 URL',
    `source_icon` VARCHAR(500)  DEFAULT NULL            COMMENT '来源图标 URL',
    `author`      VARCHAR(200)  DEFAULT NULL            COMMENT '作者',
    `is_deleted`  TINYINT       DEFAULT 0               COMMENT '逻辑删除',
    `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_source` (`source`),
    KEY `idx_score` (`score` DESC),
    KEY `idx_published` (`published_at` DESC),
    KEY `idx_url_source` (`url`(255), `source`) COMMENT '去重索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热门英文文章';

-- 用户阅读记录表
CREATE TABLE IF NOT EXISTS `enstud_article_read` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`       BIGINT       NOT NULL                COMMENT '用户 ID',
    `article_id`    BIGINT       NOT NULL                COMMENT '文章 ID',
    `read_count`    INT          DEFAULT 1               COMMENT '阅读次数',
    `is_bookmarked` TINYINT(1)   DEFAULT 0               COMMENT '是否收藏',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
    KEY `idx_user_bookmarks` (`user_id`, `is_bookmarked`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户阅读记录';

-- ==========================================
-- 第七部分：初始数据
-- ==========================================

-- 插入默认论坛板块
INSERT INTO `enstud_forum_category` (`name`, `description`, `sort_order`) VALUES
('学习经验', '分享英语学习方法与心得', 1),
('学习资源', '分享学习资料、工具、课程推荐', 2),
('问答求助', '学习中遇到的问题来这里交流', 3),
('每日打卡', '记录每天的学习进度，互相监督', 4),
('英语角', '自由讨论，提升英语表达能力', 5);
```

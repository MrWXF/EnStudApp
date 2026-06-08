-- EnStudApp 数据库迁移脚本
-- 版本: v1.2.0
-- 描述: 新增热门英文文章阅读功能
-- 日期: 2026-06-08

-- 1. 热门英文文章表
CREATE TABLE IF NOT EXISTS `enstud_article` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title` VARCHAR(500) NOT NULL COMMENT '英文标题',
    `title_cn` VARCHAR(500) DEFAULT NULL COMMENT '中文翻译标题',
    `url` VARCHAR(1000) NOT NULL COMMENT '原文链接',
    `source` VARCHAR(50) NOT NULL COMMENT '文章来源 (HN/GitHub/Medium/InfoQ/TechCrunch)',
    `summary` TEXT DEFAULT NULL COMMENT '英文摘要',
    `summary_cn` TEXT DEFAULT NULL COMMENT '中文翻译摘要',
    `content` LONGTEXT DEFAULT NULL COMMENT '英文原文',
    `content_cn` LONGTEXT DEFAULT NULL COMMENT '中文翻译全文',
    `source_score` INT DEFAULT 0 COMMENT '原始热度分',
    `score` INT DEFAULT 0 COMMENT '计算后热度分（带时间衰减）',
    `published_at` DATETIME DEFAULT NULL COMMENT '原文发布时间',
    `fetched_at` DATETIME DEFAULT NULL COMMENT '抓取时间',
    `cover_url` VARCHAR(1000) DEFAULT NULL COMMENT '封面图 URL',
    `source_icon` VARCHAR(500) DEFAULT NULL COMMENT '来源图标 URL',
    `author` VARCHAR(200) DEFAULT NULL COMMENT '作者',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_source` (`source`),
    KEY `idx_score` (`score` DESC),
    KEY `idx_published` (`published_at` DESC),
    KEY `idx_url_source` (`url`(255), `source`) COMMENT '去重索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热门英文文章';

-- 2. 用户阅读记录表
CREATE TABLE IF NOT EXISTS `enstud_article_read` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户 ID',
    `article_id` BIGINT NOT NULL COMMENT '文章 ID',
    `read_count` INT DEFAULT 1 COMMENT '阅读次数',
    `is_bookmarked` TINYINT(1) DEFAULT 0 COMMENT '是否收藏',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
    KEY `idx_user_bookmarks` (`user_id`, `is_bookmarked`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户阅读记录';

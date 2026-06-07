-- ============================================
-- EnStudApp 论坛相关表
-- ============================================

-- 板块表
CREATE TABLE IF NOT EXISTS `enstud_forum_category` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '板块ID',
    `name`          VARCHAR(50)     NOT NULL                 COMMENT '板块名称',
    `description`   VARCHAR(200)    DEFAULT NULL             COMMENT '板块描述',
    `icon`          VARCHAR(100)    DEFAULT NULL             COMMENT '图标',
    `sort_order`    INT             NOT NULL DEFAULT 0       COMMENT '排序',
    `is_deleted`    TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论坛板块表';

-- 帖子表
CREATE TABLE IF NOT EXISTS `enstud_forum_post` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '帖子ID',
    `title`         VARCHAR(200)    NOT NULL                 COMMENT '标题',
    `content`       LONGTEXT        NOT NULL                 COMMENT '内容',
    `summary`       VARCHAR(500)    DEFAULT NULL             COMMENT '摘要',
    `author_id`     BIGINT          NOT NULL                 COMMENT '作者ID',
    `category_id`   BIGINT          NOT NULL                 COMMENT '板块ID',
    `tags`          VARCHAR(500)    DEFAULT NULL             COMMENT '标签（逗号分隔）',
    `view_count`    INT             NOT NULL DEFAULT 0       COMMENT '浏览量',
    `like_count`    INT             NOT NULL DEFAULT 0       COMMENT '点赞数',
    `reply_count`   INT             NOT NULL DEFAULT 0       COMMENT '回复数',
    `collect_count` INT             NOT NULL DEFAULT 0       COMMENT '收藏数',
    `is_pinned`     TINYINT         NOT NULL DEFAULT 0       COMMENT '是否置顶',
    `is_essence`    TINYINT         NOT NULL DEFAULT 0       COMMENT '是否精华',
    `status`        VARCHAR(20)     NOT NULL DEFAULT 'PUBLISHED' COMMENT '状态 PUBLISHED/DRAFT/HIDDEN',
    `is_deleted`    TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_author` (`author_id`),
    KEY `idx_category` (`category_id`),
    KEY `idx_created` (`created_at`),
    FULLTEXT KEY `ft_title_content` (`title`, `content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';

-- 回复表
CREATE TABLE IF NOT EXISTS `enstud_forum_reply` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '回复ID',
    `post_id`       BIGINT          NOT NULL                 COMMENT '帖子ID',
    `parent_id`     BIGINT          DEFAULT NULL             COMMENT '父回复ID（楼中楼）',
    `content`       TEXT            NOT NULL                 COMMENT '回复内容',
    `author_id`     BIGINT          NOT NULL                 COMMENT '作者ID',
    `like_count`    INT             NOT NULL DEFAULT 0       COMMENT '点赞数',
    `is_deleted`    TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_post` (`post_id`),
    KEY `idx_author` (`author_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回复表';

-- 点赞记录表
CREATE TABLE IF NOT EXISTS `enstud_forum_like` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    `user_id`       BIGINT          NOT NULL                 COMMENT '用户ID',
    `target_type`   VARCHAR(20)     NOT NULL                 COMMENT '目标类型 POST/REPLY',
    `target_id`     BIGINT          NOT NULL                 COMMENT '目标ID',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞记录表';

-- 插入默认板块
INSERT INTO `enstud_forum_category` (`name`, `description`, `sort_order`) VALUES
('学习经验', '分享英语学习方法与心得', 1),
('学习资源', '分享学习资料、工具、课程推荐', 2),
('问答求助', '学习中遇到的问题来这里交流', 3),
('每日打卡', '记录每天的学习进度，互相监督', 4),
('英语角', '自由讨论，提升英语表达能力', 5);

-- ============================================
-- V5: 论坛标签表 + 帖子-标签关联表
-- ============================================

-- 标签表
CREATE TABLE IF NOT EXISTS `enstud_forum_tag` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '标签ID',
    `name`          VARCHAR(50)  NOT NULL                 COMMENT '标签名称',
    `post_count`    INT          NOT NULL DEFAULT 0       COMMENT '关联帖子数',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_post_count` (`post_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- 帖子-标签关联表（多对多）
CREATE TABLE IF NOT EXISTS `enstud_forum_post_tag` (
    `post_id`       BIGINT    NOT NULL                 COMMENT '帖子ID',
    `tag_id`        BIGINT    NOT NULL                 COMMENT '标签ID',
    `created_at`    DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`post_id`, `tag_id`),
    KEY `idx_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子-标签关联表';

-- ============================================
-- V6: 用户关注表
-- ============================================

-- 用户关注表
CREATE TABLE IF NOT EXISTS `enstud_user_follow` (
    `id`            BIGINT    NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    `follower_id`   BIGINT    NOT NULL                 COMMENT '关注者ID',
    `following_id`  BIGINT    NOT NULL                 COMMENT '被关注者ID',
    `created_at`    DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follow` (`follower_id`, `following_id`),
    KEY `idx_follower` (`follower_id`),
    KEY `idx_following` (`following_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注表';

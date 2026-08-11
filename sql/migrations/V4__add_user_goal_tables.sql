-- ============================================
-- V4: 用户学习目标表 + 每日学习记录表
-- ============================================

-- 用户学习目标表
CREATE TABLE IF NOT EXISTS `enstud_user_goal` (
    `user_id`             BIGINT NOT NULL                 COMMENT '用户ID',
    `daily_word_goal`     INT NOT NULL DEFAULT 20         COMMENT '每日单词目标',
    `daily_minute_goal`   INT NOT NULL DEFAULT 30         COMMENT '每日学习时长目标（分钟）',
    `created_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户学习目标表';

-- 每日学习记录表
CREATE TABLE IF NOT EXISTS `enstud_daily_study_record` (
    `id`              BIGINT    NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
    `user_id`         BIGINT    NOT NULL                 COMMENT '用户ID',
    `study_date`      DATE      NOT NULL                 COMMENT '学习日期',
    `words_learned`   INT       NOT NULL DEFAULT 0       COMMENT '当日学习单词数',
    `minutes_studied` INT       NOT NULL DEFAULT 0       COMMENT '当日学习时长（分钟）',
    `goal_completed`  TINYINT   NOT NULL DEFAULT 0       COMMENT '是否完成目标 0-否 1-是',
    `created_at`      DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `study_date`),
    KEY `idx_user_date` (`user_id`, `study_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日学习记录表';

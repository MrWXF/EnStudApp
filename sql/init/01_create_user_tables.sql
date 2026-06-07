-- ============================================
-- EnStudApp 用户相关表
-- ============================================

-- 用户表
CREATE TABLE IF NOT EXISTS `enstud_user` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
    `username`      VARCHAR(50)     NOT NULL                 COMMENT '用户名',
    `email`         VARCHAR(100)    NOT NULL                 COMMENT '邮箱',
    `password_hash` VARCHAR(255)    NOT NULL                 COMMENT '密码哈希（BCrypt）',
    `avatar_url`    VARCHAR(500)    DEFAULT NULL             COMMENT '头像URL',
    `nickname`      VARCHAR(50)     DEFAULT NULL             COMMENT '昵称',
    `level`         INT             NOT NULL DEFAULT 1       COMMENT '用户等级',
    `points`        INT             NOT NULL DEFAULT 0       COMMENT '积分',
    `last_login_at` DATETIME        DEFAULT NULL             COMMENT '最后登录时间',
    `is_deleted`    TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除 0-正常 1-删除',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 用户学习档案表
CREATE TABLE IF NOT EXISTS `enstud_user_profile` (
    `user_id`               BIGINT  NOT NULL                 COMMENT '用户ID',
    `total_words_learned`   INT     NOT NULL DEFAULT 0       COMMENT '累计学习单词数',
    `vocabulary_size`       INT     NOT NULL DEFAULT 0       COMMENT '词汇量估算',
    `writing_count`         INT     NOT NULL DEFAULT 0       COMMENT '写作次数',
    `chat_count`            INT     NOT NULL DEFAULT 0       COMMENT '对话次数',
    `current_streak`        INT     NOT NULL DEFAULT 0       COMMENT '当前连续打卡天数',
    `longest_streak`        INT     NOT NULL DEFAULT 0       COMMENT '最长连续打卡天数',
    `last_study_date`       DATE    DEFAULT NULL             COMMENT '最后学习日期',
    `created_at`            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`user_id`),
    CONSTRAINT `fk_profile_user` FOREIGN KEY (`user_id`) REFERENCES `enstud_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户学习档案表';

-- 登录Token表（用于Token黑名单/主动失效）
CREATE TABLE IF NOT EXISTS `enstud_user_token` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    `user_id`       BIGINT          NOT NULL                 COMMENT '用户ID',
    `token`         VARCHAR(500)    NOT NULL                 COMMENT 'Token',
    `token_type`    VARCHAR(20)     NOT NULL DEFAULT 'ACCESS' COMMENT 'Token类型 ACCESS/REFRESH',
    `expires_at`    DATETIME        NOT NULL                 COMMENT '过期时间',
    `is_revoked`    TINYINT         NOT NULL DEFAULT 0       COMMENT '是否已吊销 0-否 1-是',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_token` (`token`(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录Token表';

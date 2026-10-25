-- ============================================
-- V10: 词汇量测试记录表（分享卡片 + 词汇量测试）
-- ============================================

CREATE TABLE IF NOT EXISTS `enstud_vocab_test_record` (
    `id`                BIGINT      NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    `user_id`           BIGINT      NOT NULL                 COMMENT '用户ID',
    `estimated_size`    INT         NOT NULL                 COMMENT '估算词汇量',
    `level_label`       VARCHAR(32) NOT NULL                 COMMENT '水平标签',
    `band_stats`        VARCHAR(1024) NULL                  COMMENT '各频段得分明细 JSON',
    `created_at`        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '测试时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='词汇量测试记录表';

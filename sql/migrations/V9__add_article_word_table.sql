-- ============================================
-- V9: 文章-单词关联表（学习闭环：阅读中匹配用户正在学习的单词）
-- ============================================

CREATE TABLE IF NOT EXISTS `enstud_article_word` (
    `id`                BIGINT    NOT NULL AUTO_INCREMENT  COMMENT 'ID',
    `article_id`        BIGINT    NOT NULL                 COMMENT '文章ID',
    `word_id`           BIGINT    NOT NULL                 COMMENT '单词ID',
    `word_text`         VARCHAR(100) NOT NULL              COMMENT '单词文本（冗余，便于查询）',
    `occurrence_count`  INT       NOT NULL DEFAULT 1       COMMENT '在文章中出现的次数',
    `created_at`        DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_word` (`article_id`, `word_id`),
    KEY `idx_word_id` (`word_id`),
    KEY `idx_word_text` (`word_text`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章-单词关联表';

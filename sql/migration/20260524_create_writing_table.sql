-- 写作批改相关表

CREATE TABLE IF NOT EXISTS `enstud_writing` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '作文ID',
    `user_id`      BIGINT       NOT NULL                COMMENT '用户ID',
    `title`        VARCHAR(200) NOT NULL                COMMENT '作文标题',
    `content`      LONGTEXT     NOT NULL                COMMENT '作文内容',
    `word_count`   INT          NOT NULL DEFAULT 0      COMMENT '字数',
    `topic_type`   VARCHAR(50)  DEFAULT NULL            COMMENT '题目类型 ESSAY/LETTER/SUMMARY',
    `score`        INT          DEFAULT NULL            COMMENT '评分 0-100',
    `correction`   MEDIUMTEXT   DEFAULT NULL            COMMENT '批改结果（JSON）',
    `is_deleted`   TINYINT      NOT NULL DEFAULT 0,
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='写作批改表';

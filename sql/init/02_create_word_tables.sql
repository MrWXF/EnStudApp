-- ============================================
-- EnStudApp 单词学习相关表
-- ============================================

-- 词库表
CREATE TABLE IF NOT EXISTS `enstud_wordbook` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '词库ID',
    `name`          VARCHAR(100)    NOT NULL                 COMMENT '词库名称',
    `description`   VARCHAR(500)    DEFAULT NULL             COMMENT '词库描述',
    `cover_url`     VARCHAR(500)    DEFAULT NULL             COMMENT '封面图',
    `word_count`    INT             NOT NULL DEFAULT 0       COMMENT '单词数量',
    `difficulty`    INT             NOT NULL DEFAULT 1       COMMENT '难度等级 1-5',
    `category`      VARCHAR(50)     NOT NULL                 COMMENT '分类 CET4/CET6/IELTS/TOEFL/BUSINESS/CUSTOM',
    `is_official`   TINYINT         NOT NULL DEFAULT 1       COMMENT '是否官方词库 0-否 1-是',
    `creator_id`    BIGINT          DEFAULT NULL             COMMENT '创建者ID（自定义词库）',
    `is_deleted`    TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_creator` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='词库表';

-- 单词表
CREATE TABLE IF NOT EXISTS `enstud_word` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '单词ID',
    `word`              VARCHAR(100)    NOT NULL                 COMMENT '单词',
    `phonetic_uk`       VARCHAR(100)    DEFAULT NULL             COMMENT '英式音标',
    `phonetic_us`       VARCHAR(100)    DEFAULT NULL             COMMENT '美式音标',
    `pronunciation_url` VARCHAR(500)    DEFAULT NULL             COMMENT '发音音频URL',
    `definition_cn`     TEXT            DEFAULT NULL             COMMENT '中文释义',
    `definition_en`     TEXT            DEFAULT NULL             COMMENT '英文释义',
    `example_sentence`  TEXT            DEFAULT NULL             COMMENT '例句',
    `example_cn`        TEXT            DEFAULT NULL             COMMENT '例句中文翻译',
    `part_of_speech`    VARCHAR(30)     DEFAULT NULL             COMMENT '词性 n/v/adj/adv',
    `difficulty_level`  INT             NOT NULL DEFAULT 1       COMMENT '难度等级 1-5',
    `wordbook_id`       BIGINT          DEFAULT NULL             COMMENT '所属词库ID',
    `is_deleted`        TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_word` (`word`),
    KEY `idx_wordbook` (`wordbook_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='单词表';

-- 用户单词学习记录表（SM-2 记忆算法数据）
CREATE TABLE IF NOT EXISTS `enstud_user_word_record` (
    `id`                BIGINT      NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
    `user_id`           BIGINT      NOT NULL                 COMMENT '用户ID',
    `word_id`           BIGINT      NOT NULL                 COMMENT '单词ID',
    `ease_factor`       DOUBLE      NOT NULL DEFAULT 2.5    COMMENT '简易度系数（SM-2）',
    `interval`          INT         NOT NULL DEFAULT 0       COMMENT '间隔天数（SM-2）',
    `repetitions`       INT         NOT NULL DEFAULT 0       COMMENT '复习次数（SM-2）',
    `quality`           INT         NOT NULL DEFAULT 0       COMMENT '最近一次答题质量 0-5',
    `mastery_level`     INT         NOT NULL DEFAULT 0       COMMENT '掌握程度 0-100',
    `status`            VARCHAR(20) NOT NULL DEFAULT 'LEARNING' COMMENT '状态 LEARNING/REVIEWING/MASTERED',
    `next_review_time`  DATETIME    DEFAULT NULL             COMMENT '下次复习时间',
    `last_review_time`  DATETIME    DEFAULT NULL             COMMENT '最近复习时间',
    `created_at`        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_word` (`user_id`, `word_id`),
    KEY `idx_next_review` (`user_id`, `next_review_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户单词学习记录表';

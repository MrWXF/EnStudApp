-- AI 对话相关表

CREATE TABLE IF NOT EXISTS `enstud_chat_session` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    `user_id`     BIGINT   NOT NULL                COMMENT '用户ID',
    `title`       VARCHAR(100) DEFAULT NULL        COMMENT '会话标题',
    `scenario`    VARCHAR(50) DEFAULT 'FREE'       COMMENT '对话场景 FREE/DAILY/BUSINESS/TRAVEL',
    `message_count` INT    NOT NULL DEFAULT 0      COMMENT '消息数',
    `is_deleted`  TINYINT  NOT NULL DEFAULT 0      COMMENT '逻辑删除',
    `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话会话表';

CREATE TABLE IF NOT EXISTS `enstud_chat_message` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `session_id`  BIGINT       NOT NULL                COMMENT '会话ID',
    `role`        VARCHAR(20)  NOT NULL                COMMENT '角色 USER/AI',
    `content`     TEXT         NOT NULL                COMMENT '消息内容',
    `grammar_issues` TEXT     DEFAULT NULL             COMMENT '语法问题（JSON数组）',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话消息表';

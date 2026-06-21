-- user-service 集成测试 H2 Schema
-- H2 MODE=MySQL 兼容语法

CREATE TABLE IF NOT EXISTS enstud_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL,
    email         VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    avatar_url    VARCHAR(500) DEFAULT NULL,
    nickname      VARCHAR(50)  DEFAULT NULL,
    level         INT          NOT NULL DEFAULT 1,
    points        INT          NOT NULL DEFAULT 0,
    last_login_at TIMESTAMP    DEFAULT NULL,
    is_deleted    TINYINT      NOT NULL DEFAULT 0,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE (username),
    UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS enstud_user_profile (
    user_id              BIGINT    NOT NULL,
    total_words_learned  INT       NOT NULL DEFAULT 0,
    vocabulary_size      INT       NOT NULL DEFAULT 0,
    writing_count        INT       NOT NULL DEFAULT 0,
    chat_count           INT       NOT NULL DEFAULT 0,
    current_streak       INT       NOT NULL DEFAULT 0,
    longest_streak       INT       NOT NULL DEFAULT 0,
    last_study_date      DATE      DEFAULT NULL,
    created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id)
);

CREATE TABLE IF NOT EXISTS enstud_user_token (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    token       VARCHAR(500) NOT NULL,
    token_type  VARCHAR(20)  NOT NULL DEFAULT 'ACCESS',
    expires_at  TIMESTAMP    NOT NULL,
    is_revoked  TINYINT      NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

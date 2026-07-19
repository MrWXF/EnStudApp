CREATE TABLE IF NOT EXISTS enstud_user (
    id            BIGINT       AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL,
    email         VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    avatar_url    VARCHAR(500),
    nickname      VARCHAR(50),
    level         INT          DEFAULT 1,
    points        INT          DEFAULT 0,
    last_login_at TIMESTAMP    NULL,
    is_deleted    TINYINT      DEFAULT 0,
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE (username),
    UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS enstud_user_profile (
    user_id              BIGINT    NOT NULL,
    total_words_learned  INT       DEFAULT 0,
    vocabulary_size      INT       DEFAULT 0,
    writing_count        INT       DEFAULT 0,
    chat_count           INT       DEFAULT 0,
    current_streak       INT       DEFAULT 0,
    longest_streak       INT       DEFAULT 0,
    last_study_date      DATE,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id)
);

CREATE TABLE IF NOT EXISTS enstud_user_token (
    id          BIGINT       AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    token       VARCHAR(500) NOT NULL,
    token_type  VARCHAR(20)  DEFAULT 'ACCESS',
    expires_at  TIMESTAMP    NOT NULL,
    is_revoked  TINYINT      DEFAULT 0,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

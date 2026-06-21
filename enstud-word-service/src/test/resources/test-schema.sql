-- word-service H2 test schema

CREATE TABLE IF NOT EXISTS enstud_wordbook (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    name          VARCHAR(100) NOT NULL,
    description   VARCHAR(500) DEFAULT NULL,
    cover_url     VARCHAR(500) DEFAULT NULL,
    word_count    INT          NOT NULL DEFAULT 0,
    difficulty    INT          NOT NULL DEFAULT 1,
    category      VARCHAR(50)  NOT NULL,
    sort_order    INT          DEFAULT 0,
    is_official   TINYINT      NOT NULL DEFAULT 1,
    creator_id    BIGINT       DEFAULT NULL,
    is_deleted    TINYINT      NOT NULL DEFAULT 0,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS enstud_word (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    word              VARCHAR(100) NOT NULL,
    phonetic_uk       VARCHAR(100) DEFAULT NULL,
    phonetic_us       VARCHAR(100) DEFAULT NULL,
    pronunciation_url VARCHAR(500) DEFAULT NULL,
    definition_cn     TEXT         DEFAULT NULL,
    definition_en     TEXT         DEFAULT NULL,
    example_sentence  TEXT         DEFAULT NULL,
    example_cn        TEXT         DEFAULT NULL,
    part_of_speech    VARCHAR(30)  DEFAULT NULL,
    difficulty_level  INT          NOT NULL DEFAULT 1,
    wordbook_id       BIGINT       DEFAULT NULL,
    is_deleted        TINYINT      NOT NULL DEFAULT 0,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS enstud_user_word_record (
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    user_id          BIGINT      NOT NULL,
    word_id          BIGINT      NOT NULL,
    ease_factor      DOUBLE      NOT NULL DEFAULT 2.5,
    review_interval  INT         NOT NULL DEFAULT 0,
    repetitions      INT         NOT NULL DEFAULT 0,
    quality          INT         NOT NULL DEFAULT 0,
    mastery_level    INT         NOT NULL DEFAULT 0,
    status           VARCHAR(20) NOT NULL DEFAULT 'LEARNING',
    memory_level     INT         NOT NULL DEFAULT 0,
    next_review_time TIMESTAMP   DEFAULT NULL,
    last_review_time TIMESTAMP   DEFAULT NULL,
    created_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE (user_id, word_id)
);

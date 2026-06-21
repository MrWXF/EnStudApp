CREATE TABLE IF NOT EXISTS enstud_article (
    id BIGINT NOT NULL AUTO_INCREMENT, title VARCHAR(500) NOT NULL,
    title_cn VARCHAR(500), url VARCHAR(1000) NOT NULL, source VARCHAR(50) NOT NULL,
    summary TEXT, summary_cn TEXT, content CLOB, content_cn CLOB,
    source_score INT DEFAULT 0, score INT DEFAULT 0,
    published_at TIMESTAMP, fetched_at TIMESTAMP, cover_url VARCHAR(1000),
    source_icon VARCHAR(500), author VARCHAR(200),
    is_deleted TINYINT DEFAULT 0, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS enstud_article_read (
    id BIGINT NOT NULL AUTO_INCREMENT, user_id BIGINT NOT NULL, article_id BIGINT NOT NULL,
    read_count INT DEFAULT 1, is_bookmarked TINYINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id), UNIQUE (user_id, article_id)
);

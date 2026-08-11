-- ============================================
-- V8: 在用户表新增关注数/粉丝数字段（冗余，定期同步）
-- ============================================

-- 检查并添加 following_count 字段
SET @db_name = DATABASE();
SET @has_following_count = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'enstud_user'
      AND COLUMN_NAME = 'following_count');

SET @sql_add_following_count = IF(@has_following_count = 0,
    'ALTER TABLE enstud_user ADD COLUMN `following_count` INT NOT NULL DEFAULT 0 COMMENT ''关注数'' AFTER `points`',
    'SELECT 1');

PREPARE stmt FROM @sql_add_following_count;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 follower_count 字段
SET @has_follower_count = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'enstud_user'
      AND COLUMN_NAME = 'follower_count');

SET @sql_add_follower_count = IF(@has_follower_count = 0,
    'ALTER TABLE enstud_user ADD COLUMN `follower_count` INT NOT NULL DEFAULT 0 COMMENT ''粉丝数'' AFTER `following_count`',
    'SELECT 1');

PREPARE stmt FROM @sql_add_follower_count;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

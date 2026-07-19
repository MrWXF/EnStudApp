-- ============================================
-- V2: 添加连续学习天数字段（若不存在）
-- 注意：enstud_user_profile 表在 V1 创建时已包含
--   current_streak, longest_streak, last_study_date 字段。
-- 此脚本用于确保这些字段存在，适用于旧版数据库升级。
-- ============================================

-- 检查并添加 current_streak 字段
SET @db_name = DATABASE();
SET @has_current_streak = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'enstud_user_profile'
      AND COLUMN_NAME = 'current_streak');

SET @sql_add_current_streak = IF(@has_current_streak = 0,
    'ALTER TABLE enstud_user_profile ADD COLUMN `current_streak` INT NOT NULL DEFAULT 0 COMMENT ''当前连续打卡天数'' AFTER `chat_count`',
    'SELECT 1');

PREPARE stmt FROM @sql_add_current_streak;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 longest_streak 字段
SET @has_longest_streak = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'enstud_user_profile'
      AND COLUMN_NAME = 'longest_streak');

SET @sql_add_longest_streak = IF(@has_longest_streak = 0,
    'ALTER TABLE enstud_user_profile ADD COLUMN `longest_streak` INT NOT NULL DEFAULT 0 COMMENT ''最长连续打卡天数'' AFTER `current_streak`',
    'SELECT 1');

PREPARE stmt FROM @sql_add_longest_streak;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 last_study_date 字段
SET @has_last_study_date = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'enstud_user_profile'
      AND COLUMN_NAME = 'last_study_date');

SET @sql_add_last_study_date = IF(@has_last_study_date = 0,
    'ALTER TABLE enstud_user_profile ADD COLUMN `last_study_date` DATE DEFAULT NULL COMMENT ''最后学习日期'' AFTER `longest_streak`',
    'SELECT 1');

PREPARE stmt FROM @sql_add_last_study_date;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

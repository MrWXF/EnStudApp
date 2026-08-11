-- ============================================
-- V7: 在用户单词学习记录表新增答错次数字段
-- ============================================

-- 检查并添加 wrong_count 字段
SET @db_name = DATABASE();
SET @has_wrong_count = (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'enstud_user_word_record'
      AND COLUMN_NAME = 'wrong_count');

SET @sql_add_wrong_count = IF(@has_wrong_count = 0,
    'ALTER TABLE enstud_user_word_record ADD COLUMN `wrong_count` INT NOT NULL DEFAULT 0 COMMENT ''答错次数'' AFTER `quality`',
    'SELECT 1');

PREPARE stmt FROM @sql_add_wrong_count;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

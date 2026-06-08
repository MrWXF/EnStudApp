-- EnStudApp 数据库迁移脚本
-- 版本: v1.1.0
-- 描述: 新增单词记忆程度等级系统
-- 日期: 2026-06-08

-- 1. user_word_record 表新增 memory_level 字段
ALTER TABLE `enstud_user_word_record`
    ADD COLUMN `memory_level` TINYINT DEFAULT 0 COMMENT '记忆等级 0=未学习 1=模糊 2=有印象 3=基本掌握 4=熟练 5=精通' AFTER `status`;

-- 2. 为现有记录初始化 memory_level（基于已有的 masteryLevel 和 status）
UPDATE `enstud_user_word_record`
SET `memory_level` = CASE
    WHEN `status` = 'MASTERED' AND `mastery_level` >= 90 THEN 5
    WHEN `mastery_level` >= 61 THEN 4
    WHEN `mastery_level` >= 41 THEN 3
    WHEN `mastery_level` >= 21 THEN 2
    WHEN `repetitions` > 0 OR `mastery_level` > 0 THEN 1
    ELSE 0
END;

-- 3. 添加索引以优化按记忆等级查询
ALTER TABLE `enstud_user_word_record`
    ADD INDEX `idx_user_memory_level` (`user_id`, `memory_level`);

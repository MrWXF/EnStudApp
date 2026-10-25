-- V10: 为 enstud_wordbook 表添加 sort_order 字段
-- 问题：代码中查询词库列表时使用了 sort_order 字段，但表中缺失该字段
-- 修复：添加 sort_order 字段用于词库排序

ALTER TABLE enstud_app.enstud_wordbook
ADD COLUMN IF NOT EXISTS sort_order INT NOT NULL DEFAULT 0 AFTER category;

-- 添加索引优化查询性能
CREATE INDEX idx_sort_order ON enstud_app.enstud_wordbook(sort_order);

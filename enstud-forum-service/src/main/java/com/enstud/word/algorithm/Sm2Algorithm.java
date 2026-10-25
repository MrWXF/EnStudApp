package com.enstud.word.algorithm;

import com.enstud.common.enums.MemoryLevel;
import com.enstud.word.entity.UserWordRecord;

/**
 * SM-2 记忆算法实现
 * 根据用户答题质量动态调整复习间隔和难度系数
 * 并计算直观的记忆等级（0-5 级）
 */
public class Sm2Algorithm {

    /**
     * 计算下次复习时间和更新记忆参数
     *
     * @param record 当前学习记录
     * @param quality 本次答题质量 0-5（0=完全忘记，5=完美回忆）
     */
    public static void update(UserWordRecord record, int quality) {
        if (quality < 0 || quality > 5) {
            throw new IllegalArgumentException("答题质量必须在 0-5 之间");
        }
        record.setQuality(quality);

        if (quality >= 3) {
            // 回答正确
            switch (record.getRepetitions()) {
                case 0 -> record.setReviewInterval(1);
                case 1 -> record.setReviewInterval(6);
                default -> record.setReviewInterval((int) Math.round(record.getReviewInterval() * record.getEaseFactor()));
            }
            record.setRepetitions(record.getRepetitions() + 1);
        } else {
            // 回答错误：重置为重新学习
            record.setRepetitions(0);
            record.setReviewInterval(1);
        }

        // 更新简易度系数（SM-2 公式）
        double newEf = record.getEaseFactor() + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        record.setEaseFactor(Math.max(1.3, newEf));

        // 掌握程度计算（基于 repetition 和 ease_factor）
        int mastery = (int) Math.min(100, (record.getRepetitions() * 10 + (record.getEaseFactor() - 1.3) * 50));
        record.setMasteryLevel(mastery);

        // 更新状态
        if (mastery >= 90) {
            record.setStatus("MASTERED");
        } else if (record.getRepetitions() > 0) {
            record.setStatus("REVIEWING");
        } else {
            record.setStatus("LEARNING");
        }

        // 计算记忆等级（新增）
        MemoryLevel ml = MemoryLevel.calcFromMastery(mastery, record.getRepetitions(), record.getStatus(), quality);
        record.setMemoryLevel(ml.level);

        // 设置下次复习时间（当前时间 + interval 天）
        record.setNextReviewTime(
                java.time.LocalDateTime.now().plusDays(record.getReviewInterval())
        );
        record.setLastReviewTime(java.time.LocalDateTime.now());
    }
}

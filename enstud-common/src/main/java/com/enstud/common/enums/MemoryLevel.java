package com.enstud.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 单词记忆程度等级
 * 根据 SM-2 算法的 masteryLevel 映射为直观的 6 级记忆等级
 */
@Schema(description = "记忆程度等级")
public enum MemoryLevel {

    NOT_LEARNED(0, "未学习", "🆕"),
    FUZZY(1, "模糊", "😵‍💫"),
    FAMILIAR(2, "有印象", "🤔"),
    BASIC(3, "基本掌握", "👍"),
    PROFICIENT(4, "熟练", "💪"),
    MASTERED(5, "精通", "🏆");

    public final int level;
    public final String label;
    public final String emoji;

    MemoryLevel(int level, String label, String emoji) {
        this.level = level;
        this.label = label;
        this.emoji = emoji;
    }

    public static MemoryLevel fromLevel(int level) {
        for (MemoryLevel ml : values()) {
            if (ml.level == level) return ml;
        }
        return NOT_LEARNED;
    }

    /**
     * 根据 masteryLevel 和记忆状态计算记忆等级
     */
    public static MemoryLevel calcFromMastery(int mastery, int repetitions, String status, int quality) {
        // 刚答错或质量很差时，等级不应该虚高
        if (repetitions == 0 && quality > 0 && quality < 3) return FUZZY;
        if (repetitions == 0 && quality == 0) return NOT_LEARNED;

        if ("MASTERED".equals(status) && mastery >= 90) return MASTERED;
        if (mastery >= 61) return PROFICIENT;
        if (mastery >= 41) return BASIC;
        if (mastery >= 21) return FAMILIAR;
        if (repetitions > 0 || mastery > 0) return FUZZY;
        return NOT_LEARNED;
    }

    /**
     * 简化版本，用于不传 quality 的场合
     */
    public static MemoryLevel calcFromMastery(int mastery, int repetitions, String status) {
        return calcFromMastery(mastery, repetitions, status, -1);
    }
}

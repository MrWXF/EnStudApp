package com.enstud.word.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 学习统计
 */
@Data
@AllArgsConstructor
public class WordStatsDTO {
    private int totalLearned;
    private int mastered;
    private int learning;
    private int dueToday;
}

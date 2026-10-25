package com.enstud.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * 今日学习之旅：每日任务流（阅读 → 复习 → 对话 → 打卡）进度聚合
 */
@Schema(description = "今日学习之旅任务状态")
public record DailyJourneyDTO(
        @Schema(description = "日期", example = "2026-10-25")
        LocalDate date,
        @Schema(description = "连续学习天数")
        int streakDays,
        @Schema(description = "今日是否已打卡")
        boolean checkedIn,
        @Schema(description = "每日单词目标")
        int dailyWordGoal,
        @Schema(description = "今日已学单词数")
        long todayLearnedWords,
        @Schema(description = "当前到期待复习单词数")
        long dueReviewCount,
        @Schema(description = "今日已阅读文章数")
        long todayReadArticles,
        @Schema(description = "今日 AI 对话数")
        long todayChats
) {}

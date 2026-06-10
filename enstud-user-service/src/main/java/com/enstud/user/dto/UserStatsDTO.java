package com.enstud.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 学习仪表盘统计
 */
@Schema(description = "学习仪表盘统计")
public record UserStatsDTO(
        @Schema(description = "今日学习单词数") long todayLearnedWords,
        @Schema(description = "累计学习单词数") long totalLearnedWords,
        @Schema(description = "累计阅读文章数") long totalReadArticles,
        @Schema(description = "累计作文数") long totalWritings,
        @Schema(description = "平均写作得分") double avgWritingScore,
        @Schema(description = "累计对话数") long totalChats,
        @Schema(description = "累计帖子数") long totalPosts,
        @Schema(description = "记忆等级分布") MemoryLevelDistribution memoryLevelDistribution
) {
    @Schema(description = "记忆等级分布")
    public record MemoryLevelDistribution(
            @Schema(description = "未学习") long notLearned,
            @Schema(description = "模糊") long fuzzy,
            @Schema(description = "有印象") long familiar,
            @Schema(description = "基本掌握") long basic,
            @Schema(description = "熟练") long proficient,
            @Schema(description = "精通") long mastered
    ) {}
}

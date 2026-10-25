package com.enstud.word.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 正在学习中的单词（用于阅读高亮、Chat 融入提示等学习闭环场景）
 */
@Schema(description = "正在学习的单词")
public record LearningWordDTO(
        @Schema(description = "单词 ID", example = "1")
        Long wordId,

        @Schema(description = "单词", example = "abandon")
        String word,

        @Schema(description = "中文释义", example = "v. 放弃；抛弃")
        String definitionCn,

        @Schema(description = "美式音标", example = "/əˈbændən/")
        String phoneticUs,

        @Schema(description = "掌握程度 0-100", example = "10")
        Integer masteryLevel,

        @Schema(description = "学习状态：LEARNING / REVIEWING", example = "LEARNING")
        String status
) {}

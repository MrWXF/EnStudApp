package com.enstud.word.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 词汇量测试题目单词
 */
@Schema(description = "词汇量测试题目单词")
public record VocabTestWordDTO(
        @Schema(description = "单词", example = "abundant")
        String word
) {}

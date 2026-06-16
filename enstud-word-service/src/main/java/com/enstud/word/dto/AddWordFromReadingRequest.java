package com.enstud.word.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 阅读中新增单词的请求
 */
@Schema(description = "阅读中新增单词请求")
public record AddWordFromReadingRequest(
        @NotBlank(message = "单词不能为空")
        @Size(max = 100)
        @Schema(description = "待学习的单词", example = "abandon")
        String word,

        @Schema(description = "中文释义", example = "放弃，抛弃")
        String definitionCn,

        @Schema(description = "英文释义", example = "to leave someone or something forever")
        String definitionEn,

        @Size(max = 200)
        @Schema(description = "从原文中摘录的上下文句子", example = "They had to abandon the sinking ship.")
        String contextSentence,

        @Schema(description = "词性，如 v/n/adj", example = "v")
        String partOfSpeech
) {}

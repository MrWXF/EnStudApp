package com.enstud.word.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 手动调整记忆等级请求
 */
@Schema(description = "调整记忆等级请求")
public record AdjustMemoryLevelRequest(

    @Schema(description = "单词 ID")
    Long wordId,

    @Schema(description = "目标记忆等级 (0-5)，0=未学习 1=模糊 2=有印象 3=基本掌握 4=熟练 5=精通")
    Integer targetLevel
) {}

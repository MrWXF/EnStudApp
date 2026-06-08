package com.enstud.word.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 记忆等级分布统计
 */
@Schema(description = "记忆等级分布统计")
public record MemoryLevelDistributionDTO(

    @Schema(description = "未学习数量")
    long notLearned,

    @Schema(description = "模糊数量")
    long fuzzy,

    @Schema(description = "有印象数量")
    long familiar,

    @Schema(description = "基本掌握数量")
    long basic,

    @Schema(description = "熟练数量")
    long proficient,

    @Schema(description = "精通数量")
    long mastered
) {}

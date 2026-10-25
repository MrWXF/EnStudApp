package com.enstud.word.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 词汇量测试结果
 */
@Schema(description = "词汇量测试结果")
public record VocabTestResultDTO(
        @Schema(description = "估算词汇量", example = "5400")
        long estimatedSize,

        @Schema(description = "水平标签", example = "四级水平")
        String levelLabel,

        @Schema(description = "水平描述")
        String levelDescription,

        @Schema(description = "各频段得分明细")
        List<BandResult> bands,

        @Schema(description = "测试时间")
        LocalDateTime testedAt
) {
    /**
     * 单个频段得分
     */
    @Schema(description = "频段得分")
    public record BandResult(
            @Schema(description = "频段序号 1-6", example = "3")
            int band,

            @Schema(description = "频段名称", example = "四级")
            String label,

            @Schema(description = "认识数", example = "3")
            int known,

            @Schema(description = "模糊数", example = "1")
            int vague,

            @Schema(description = "不认识数", example = "1")
            int unknown,

            @Schema(description = "得分比例 0-1", example = "0.7")
            double ratio
    ) {}
}

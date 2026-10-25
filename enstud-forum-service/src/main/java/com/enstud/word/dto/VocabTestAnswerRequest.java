package com.enstud.word.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 词汇量测试提交请求
 */
@Schema(description = "词汇量测试提交请求")
public record VocabTestAnswerRequest(
        @Schema(description = "作答列表")
        @NotEmpty(message = "作答不能为空")
        @Size(max = 100, message = "作答数量超出限制")
        @Valid
        List<Answer> answers
) {
    /**
     * 单个单词作答
     *
     * @param knowledge 认知程度：2=认识，1=模糊，0=不认识
     */
    @Schema(description = "单个单词作答")
    public record Answer(
            @Schema(description = "单词", example = "abundant")
            @NotBlank(message = "单词不能为空")
            String word,

            @Schema(description = "认知程度：2=认识，1=模糊，0=不认识", example = "2")
            int knowledge
    ) {}
}

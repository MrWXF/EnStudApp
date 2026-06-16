package com.enstud.read.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "划词查词请求")
public class WordLookupRequest {

    @NotBlank(message = "选中文本不能为空")
    @Schema(description = "用户选中的英文文本（可能是单词、短语或短句）", example = "abandon")
    private String selectedText;

    @NotNull(message = "文章ID不能为空")
    @Schema(description = "当前阅读的文章ID", example = "42")
    private Long articleId;

    @Schema(description = "选中文本在原文中的上下文（前一句+选中文本+后一句）", example = "They had to abandon the sinking ship.")
    private String contextSentence;

    public String getSelectedText() { return selectedText; }
    public void setSelectedText(String selectedText) { this.selectedText = selectedText; }
    public Long getArticleId() { return articleId; }
    public void setArticleId(Long articleId) { this.articleId = articleId; }
    public String getContextSentence() { return contextSentence; }
    public void setContextSentence(String contextSentence) { this.contextSentence = contextSentence; }
}

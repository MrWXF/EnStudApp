package com.enstud.read.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 文章详情 DTO（含全文）
 */
@Schema(description = "文章详情")
public record ArticleDetailDTO(
        @Schema(description = "文章ID") Long id,
        @Schema(description = "英文标题") String title,
        @Schema(description = "中文翻译标题") String titleCn,
        @Schema(description = "原文链接") String url,
        @Schema(description = "来源") String source,
        @Schema(description = "来源图标") String sourceIcon,
        @Schema(description = "作者") String author,
        @Schema(description = "封面图") String coverUrl,
        @Schema(description = "英文全文") String content,
        @Schema(description = "中文翻译全文（空时表示未翻译）") String contentCn,
        @Schema(description = "英文摘要") String summary,
        @Schema(description = "中文翻译摘要") String summaryCn,
        @Schema(description = "热度分") Integer score,
        @Schema(description = "原始热度分") Integer sourceScore,
        @Schema(description = "发布时间") LocalDateTime publishedAt
) {}

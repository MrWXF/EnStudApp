package com.enstud.read.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 热门文章列表 DTO
 */
@Schema(description = "热门文章")
public record ArticleDTO(
        @Schema(description = "文章ID") Long id,
        @Schema(description = "英文标题") String title,
        @Schema(description = "中文翻译标题") String titleCn,
        @Schema(description = "原文链接") String url,
        @Schema(description = "来源") String source,
        @Schema(description = "来源图标") String sourceIcon,
        @Schema(description = "英文摘要") String summary,
        @Schema(description = "中文翻译摘要") String summaryCn,
        @Schema(description = "封面图") String coverUrl,
        @Schema(description = "作者") String author,
        @Schema(description = "热度分") Integer score,
        @Schema(description = "发布时间") LocalDateTime publishedAt,
        @Schema(description = "是否已收藏") Boolean bookmarked
) {}

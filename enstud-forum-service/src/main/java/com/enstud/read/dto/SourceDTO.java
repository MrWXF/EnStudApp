package com.enstud.read.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 文章来源列表 DTO
 */
@Schema(description = "文章来源")
public record SourceDTO(
        @Schema(description = "来源标识") String id,
        @Schema(description = "来源名称") String name,
        @Schema(description = "图标URL") String icon,
        @Schema(description = "活跃文章数") Integer activeCount
) {}

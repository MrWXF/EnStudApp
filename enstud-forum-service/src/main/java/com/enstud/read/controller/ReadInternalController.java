package com.enstud.read.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enstud.common.Result;
import com.enstud.read.entity.ArticleReadRecord;
import com.enstud.read.mapper.ArticleReadRecordMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 内部接口：阅读统计（供 user-service 聚合调用，无需鉴权）
 */
@Tag(name = "阅读服务内部接口")
@RestController
@RequestMapping("/read/internal")
@RequiredArgsConstructor
public class ReadInternalController {

    private final ArticleReadRecordMapper recordMapper;

    @Operation(summary = "内部接口：获取用户阅读统计")
    @GetMapping("/stats/{userId}")
    public Result<ReadInternalStatsResponse> getStats(
            @PathVariable Long userId,
            @Parameter(description = "统计日期，默认当天")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate today) {

        if (today == null) {
            today = LocalDate.now();
        }

        long totalReadArticles = recordMapper.selectCount(
                new LambdaQueryWrapper<ArticleReadRecord>()
                        .eq(ArticleReadRecord::getUserId, userId)
        );

        // 今日阅读文章数：updatedAt >= 今日零点（新读或重读均计入）
        long todayReadArticles = recordMapper.selectCount(
                new LambdaQueryWrapper<ArticleReadRecord>()
                        .eq(ArticleReadRecord::getUserId, userId)
                        .ge(ArticleReadRecord::getUpdatedAt, today.atStartOfDay())
        );

        return Result.success(new ReadInternalStatsResponse(totalReadArticles, todayReadArticles));
    }

    /**
     * 阅读统计响应体
     */
    public record ReadInternalStatsResponse(long totalReadArticles, long todayReadArticles) {}
}

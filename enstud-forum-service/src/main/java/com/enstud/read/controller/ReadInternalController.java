package com.enstud.read.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enstud.common.Result;
import com.enstud.read.entity.ArticleReadRecord;
import com.enstud.read.mapper.ArticleReadRecordMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    public Result<ReadInternalStatsResponse> getStats(@PathVariable Long userId) {
        long totalReadArticles = recordMapper.selectCount(
                new LambdaQueryWrapper<ArticleReadRecord>()
                        .eq(ArticleReadRecord::getUserId, userId)
        );
        return Result.success(new ReadInternalStatsResponse(totalReadArticles));
    }

    /**
     * 阅读统计响应体
     */
    public record ReadInternalStatsResponse(long totalReadArticles) {}
}

package com.enstud.writing.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enstud.common.Result;
import com.enstud.writing.entity.Writing;
import com.enstud.writing.mapper.WritingMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 内部接口：写作统计（供 user-service 聚合调用，无需鉴权）
 */
@Tag(name = "写作服务内部接口")
@RestController
@RequestMapping("/writing/internal")
@RequiredArgsConstructor
public class WritingInternalController {

    private final WritingMapper writingMapper;

    @Operation(summary = "内部接口：获取用户写作统计")
    @GetMapping("/stats/{userId}")
    public Result<WritingInternalStatsResponse> getStats(@PathVariable Long userId) {
        List<Writing> writings = writingMapper.selectList(
                new LambdaQueryWrapper<Writing>()
                        .eq(Writing::getUserId, userId)
        );
        long totalWritings = writings.size();
        double avgScore = writings.stream()
                .filter(w -> w.getScore() != null)
                .mapToInt(Writing::getScore)
                .average()
                .orElse(0.0);
        return Result.success(new WritingInternalStatsResponse(totalWritings, avgScore));
    }

    /**
     * 写作统计响应体
     */
    public record WritingInternalStatsResponse(
            long totalWritings,
            double avgScore
    ) {}
}

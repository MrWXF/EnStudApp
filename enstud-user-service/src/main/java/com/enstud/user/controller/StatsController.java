package com.enstud.user.controller;

import com.enstud.common.AuthRequired;
import com.enstud.common.Result;
import com.enstud.common.SecurityContext;
import com.enstud.user.client.StatsClient;
import com.enstud.user.dto.UserStatsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学习仪表盘统计控制器
 */
@Tag(name = "学习统计", description = "学习仪表盘数据聚合")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@AuthRequired
public class StatsController {

    private final StatsClient statsClient;

    @Operation(summary = "获取学习仪表盘统计数据")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/stats")
    public Result<UserStatsDTO> getStats() {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        return Result.success(statsClient.getUserStats(userId));
    }
}

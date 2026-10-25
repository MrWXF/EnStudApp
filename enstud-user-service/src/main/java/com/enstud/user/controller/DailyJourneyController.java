package com.enstud.user.controller;

import com.enstud.common.AuthRequired;
import com.enstud.common.Result;
import com.enstud.common.SecurityContext;
import com.enstud.user.dto.DailyJourneyDTO;
import com.enstud.user.service.DailyJourneyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "今日学习之旅", description = "每日任务流：阅读、复习、对话、打卡进度聚合")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@AuthRequired
public class DailyJourneyController {

    private final DailyJourneyService dailyJourneyService;

    @Operation(summary = "获取今日学习之旅任务状态", description = "返回今日四项任务（阅读/复习/对话/打卡）的进度数据")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/daily-journey")
    public Result<DailyJourneyDTO> getDailyJourney() {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        return Result.success(dailyJourneyService.getDailyJourney(userId));
    }
}

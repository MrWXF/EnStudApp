package com.enstud.user.controller;

import com.enstud.common.AuthRequired;
import com.enstud.common.Result;
import com.enstud.common.SecurityContext;
import com.enstud.user.dto.*;
import com.enstud.user.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "学习目标", description = "每日学习目标、打卡、进度查询")
@RestController
@RequestMapping("/user/goal")
@RequiredArgsConstructor
@AuthRequired
public class GoalController {

    private final GoalService goalService;

    @Operation(summary = "获取用户学习目标")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping
    public Result<GoalDTO> getGoal() {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        return Result.success(goalService.getGoal(userId));
    }

    @Operation(summary = "更新用户学习目标")
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping
    public Result<Void> updateGoal(@Valid @RequestBody UpdateGoalRequest request) {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        goalService.updateGoal(userId, request);
        return Result.success();
    }

    @Operation(summary = "获取今日学习进度")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/status")
    public Result<DailyStudyStatusDTO> getDailyStatus() {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        return Result.success(goalService.getDailyStatus(userId));
    }

    @Operation(summary = "打卡（获得积分奖励）")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/checkin")
    public Result<CheckinResponseDTO> checkin() {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        return Result.success(goalService.checkin(userId));
    }
}

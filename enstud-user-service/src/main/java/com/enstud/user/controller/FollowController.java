package com.enstud.user.controller;

import com.enstud.common.AuthRequired;
import com.enstud.common.Result;
import com.enstud.common.SecurityContext;
import com.enstud.user.dto.FollowDTO;
import com.enstud.user.dto.FollowStatusDTO;
import com.enstud.user.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户关注", description = "关注、取消关注、关注列表、粉丝列表")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@AuthRequired
public class FollowController {

    private final FollowService followService;

    @Operation(summary = "关注用户")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/follow/{targetUserId}")
    public Result<Void> follow(@PathVariable Long targetUserId) {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        followService.follow(userId, targetUserId);
        return Result.success();
    }

    @Operation(summary = "取消关注")
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/follow/{targetUserId}")
    public Result<Void> unfollow(@PathVariable Long targetUserId) {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        followService.unfollow(userId, targetUserId);
        return Result.success();
    }

    @Operation(summary = "获取我关注的用户列表")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/following")
    public Result<List<FollowDTO>> getFollowing(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        return Result.success(followService.getFollowing(userId, cursor, limit));
    }

    @Operation(summary = "获取我的粉丝列表")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/followers")
    public Result<List<FollowDTO>> getFollowers(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        return Result.success(followService.getFollowers(userId, cursor, limit));
    }

    @Operation(summary = "查询是否已关注某用户")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{userId}/follow-status")
    public Result<FollowStatusDTO> getFollowStatus(@PathVariable Long userId) {
        Long currentUserId = SecurityContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.fail(401, "未登录");
        }
        return Result.success(followService.getFollowStatus(currentUserId, userId));
    }
}

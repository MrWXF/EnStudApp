package com.enstud.user.controller;

import com.enstud.common.Result;
import com.enstud.common.SecurityContext;
import com.enstud.user.dto.LoginRequest;
import com.enstud.user.dto.LoginResultDTO;
import com.enstud.user.dto.RegisterRequest;
import com.enstud.user.dto.UserProfileDTO;
import com.enstud.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户服务控制器
 */
@Tag(name = "用户管理", description = "用户注册、登录、个人信息接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResultDTO> login(@Valid @RequestBody LoginRequest request) {
        LoginResultDTO result = userService.login(request);
        return Result.success(result);
    }

    @Operation(summary = "获取当前用户信息")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/profile")
    public Result<UserProfileDTO> getProfile() {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) {
            // 如果 SecurityContext 为空，从请求头读取（由 Gateway 设置）
            return Result.fail(401, "未登录");
        }
        UserProfileDTO profile = userService.getProfile(userId);
        return Result.success(profile);
    }

    /**
     * 内部接口：根据 userId 获取用户名（供其他微服务调用，无需鉴权）
     */
    @Operation(summary = "内部接口：获取用户名")
    @GetMapping("/users/{userId}/name")
    public Result<String> getUserName(@PathVariable Long userId) {
        return Result.success(userService.getUserName(userId));
    }
}

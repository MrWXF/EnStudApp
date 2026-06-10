package com.enstud.forum.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enstud.common.Result;
import com.enstud.forum.entity.ForumPost;
import com.enstud.forum.mapper.ForumPostMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 内部接口：论坛统计（供 user-service 聚合调用，无需鉴权）
 */
@Tag(name = "论坛服务内部接口")
@RestController
@RequestMapping("/forum/internal")
@RequiredArgsConstructor
public class ForumInternalController {

    private final ForumPostMapper forumPostMapper;

    @Operation(summary = "内部接口：获取用户论坛统计")
    @GetMapping("/stats/{userId}")
    public Result<ForumInternalStatsResponse> getStats(@PathVariable Long userId) {
        long totalPosts = forumPostMapper.selectCount(
                new LambdaQueryWrapper<ForumPost>()
                        .eq(ForumPost::getAuthorId, userId)
        );
        return Result.success(new ForumInternalStatsResponse(totalPosts));
    }

    /**
     * 论坛统计响应体
     */
    public record ForumInternalStatsResponse(long totalPosts) {}
}

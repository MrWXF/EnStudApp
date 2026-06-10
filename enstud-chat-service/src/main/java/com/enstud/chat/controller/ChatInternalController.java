package com.enstud.chat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enstud.chat.entity.ChatSession;
import com.enstud.chat.mapper.ChatSessionMapper;
import com.enstud.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 内部接口：对话统计（供 user-service 聚合调用，无需鉴权）
 */
@Tag(name = "对话服务内部接口")
@RestController
@RequestMapping("/chat/internal")
@RequiredArgsConstructor
public class ChatInternalController {

    private final ChatSessionMapper chatSessionMapper;

    @Operation(summary = "内部接口：获取用户对话统计")
    @GetMapping("/stats/{userId}")
    public Result<ChatInternalStatsResponse> getStats(@PathVariable Long userId) {
        long totalChats = chatSessionMapper.selectCount(
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getUserId, userId)
        );
        return Result.success(new ChatInternalStatsResponse(totalChats));
    }

    /**
     * 对话统计响应体
     */
    public record ChatInternalStatsResponse(long totalChats) {}
}

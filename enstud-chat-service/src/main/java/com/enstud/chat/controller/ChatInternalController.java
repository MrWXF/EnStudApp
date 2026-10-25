package com.enstud.chat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enstud.chat.entity.ChatSession;
import com.enstud.chat.mapper.ChatSessionMapper;
import com.enstud.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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
    public Result<ChatInternalStatsResponse> getStats(
            @PathVariable Long userId,
            @Parameter(description = "统计日期，默认当天")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate today) {

        if (today == null) {
            today = LocalDate.now();
        }

        long totalChats = chatSessionMapper.selectCount(
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getUserId, userId)
        );

        // 今日对话数：会话 updatedAt >= 今日零点（发消息会更新会话，新会话/旧会话续聊均计入）
        long todayChats = chatSessionMapper.selectCount(
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getUserId, userId)
                        .ge(ChatSession::getUpdatedAt, today.atStartOfDay())
        );

        return Result.success(new ChatInternalStatsResponse(totalChats, todayChats));
    }

    /**
     * 对话统计响应体
     */
    public record ChatInternalStatsResponse(long totalChats, long todayChats) {}
}

package com.enstud.chat.controller;

import com.enstud.chat.dto.MessageDTO;
import com.enstud.chat.dto.SendMessageResponse;
import com.enstud.chat.dto.SessionDTO;
import com.enstud.chat.service.ChatService;
import com.enstud.common.Result;
import com.enstud.common.SecurityContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "AI 对话", description = "AI 英语日常聊天")
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "开始新会话")
    @PostMapping("/sessions")
    public Result<SessionDTO> startSession(@RequestParam(defaultValue = "FREE") String scenario) {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) return Result.fail(401, "请先登录");
        return Result.success(chatService.startSession(userId, scenario));
    }

    @Operation(summary = "发送消息")
    @PostMapping("/sessions/{sessionId}/messages")
    public Result<SendMessageResponse> sendMessage(
            @PathVariable Long sessionId,
            @RequestParam String content) {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) return Result.fail(401, "请先登录");
        return Result.success(chatService.sendMessage(userId, sessionId, content));
    }

    @Operation(summary = "获取会话列表")
    @GetMapping("/sessions")
    public Result<List<SessionDTO>> getSessions() {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) return Result.fail(401, "请先登录");
        return Result.success(chatService.getSessions(userId));
    }

    @Operation(summary = "获取会话消息历史")
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<MessageDTO>> getMessages(@PathVariable Long sessionId) {
        return Result.success(chatService.getMessages(sessionId));
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> deleteSession(@PathVariable Long sessionId) {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) return Result.fail(401, "请先登录");
        chatService.deleteSession(userId, sessionId);
        return Result.success();
    }
}

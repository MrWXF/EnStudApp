package com.enstud.chat.controller;

import com.enstud.chat.dto.ChatTopicDTO;
import com.enstud.chat.service.ChatRecommendationService;
import com.enstud.common.AuthRequired;
import com.enstud.common.Result;
import com.enstud.common.SecurityContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "对话推荐", description = "基于用户水平的对话主题推荐")
@RestController
@RequestMapping("/chat/recommendations")
@RequiredArgsConstructor
@AuthRequired
public class RecommendationController {

    private final ChatRecommendationService chatRecommendationService;

    @Operation(summary = "推荐对话主题（混合方案：预置场景 + AI 生成）")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/topics")
    public Result<List<ChatTopicDTO>> recommendTopics(
            @RequestParam(defaultValue = "5") int limit) {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        return Result.success(chatRecommendationService.recommendTopics(userId, limit));
    }
}

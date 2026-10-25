package com.enstud.word.controller;

import com.enstud.common.AuthRequired;
import com.enstud.common.Result;
import com.enstud.common.SecurityContext;
import com.enstud.word.dto.TopicRecommendationDTO;
import com.enstud.word.dto.WordRecommendationDTO;
import com.enstud.word.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "学习推荐", description = "基于用户水平的单词和主题推荐")
@RestController
@RequestMapping("/word/recommendations")
@RequiredArgsConstructor
@AuthRequired
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Operation(summary = "推荐单词（基于记忆等级分布）")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/words")
    public Result<List<WordRecommendationDTO>> recommendWords(
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        return Result.success(recommendationService.recommendWords(userId, limit));
    }

    @Operation(summary = "推荐学习主题（基于已学词库分布）")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/topics")
    public Result<List<TopicRecommendationDTO>> recommendTopics(
            @RequestParam(defaultValue = "5") int limit) {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        return Result.success(recommendationService.recommendTopics(userId, limit));
    }
}

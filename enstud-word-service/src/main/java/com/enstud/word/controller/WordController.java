package com.enstud.word.controller;

import com.enstud.common.Result;
import com.enstud.common.SecurityContext;
import com.enstud.word.dto.AdjustMemoryLevelRequest;
import com.enstud.word.dto.MemoryLevelDistributionDTO;
import com.enstud.word.dto.WordCardDTO;
import com.enstud.word.dto.WordbookDTO;
import com.enstud.word.service.WordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "单词学习", description = "词库管理、单词学习、记忆复习、记忆等级")
@RestController
@RequestMapping("/word")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;

    @Operation(summary = "获取词库列表")
    @GetMapping("/wordbooks")
    public Result<List<WordbookDTO>> getWordbooks() {
        return Result.success(wordService.getWordbooks());
    }

    @Operation(summary = "获取词库下的单词")
    @GetMapping("/wordbooks/{wordbookId}/words")
    public Result<List<WordCardDTO>> getWords(
            @PathVariable Long wordbookId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        Long userId = SecurityContext.getCurrentUserId();
        return Result.success(wordService.getWordsByWordbook(wordbookId, userId, cursor, limit));
    }

    @Operation(summary = "开始学习 / 获取待学习单词")
    @PostMapping("/study")
    public Result<List<WordCardDTO>> startStudy(
            @RequestParam Long wordbookId,
            @RequestParam(defaultValue = "10") int limit) {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) return Result.fail(401, "请先登录");
        return Result.success(wordService.getWordsForStudy(userId, wordbookId, limit));
    }

    @Operation(summary = "提交复习结果")
    @PostMapping("/review")
    public Result<Void> review(
            @RequestParam Long wordId,
            @RequestParam int quality) {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) return Result.fail(401, "请先登录");
        wordService.submitReview(userId, wordId, quality);
        return Result.success();
    }

    @Operation(summary = "获取记忆等级分布统计")
    @GetMapping("/memory-level/distribution")
    public Result<MemoryLevelDistributionDTO> getMemoryLevelDistribution() {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) return Result.fail(401, "请先登录");
        return Result.success(wordService.getMemoryLevelDistribution(userId));
    }

    @Operation(summary = "按记忆等级筛选单词")
    @GetMapping("/memory-level/words")
    public Result<List<WordCardDTO>> getWordsByMemoryLevel(
            @RequestParam(defaultValue = "0") Integer memoryLevel,
            @RequestParam(required = false) Long wordbookId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) return Result.fail(401, "请先登录");
        return Result.success(wordService.getWordsByMemoryLevel(userId, wordbookId, memoryLevel, cursor, limit));
    }

    @Operation(summary = "手动调整单词记忆等级")
    @PutMapping("/memory-level/adjust")
    public Result<Void> adjustMemoryLevel(@Valid @RequestBody AdjustMemoryLevelRequest request) {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) return Result.fail(401, "请先登录");
        wordService.adjustMemoryLevel(userId, request.wordId(), request.targetLevel());
        return Result.success();
    }
}

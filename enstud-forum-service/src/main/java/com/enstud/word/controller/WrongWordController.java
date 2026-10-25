package com.enstud.word.controller;

import com.enstud.common.AuthRequired;
import com.enstud.common.Result;
import com.enstud.common.SecurityContext;
import com.enstud.word.dto.ReviewBatchRequest;
import com.enstud.word.dto.WrongWordDTO;
import com.enstud.word.service.WrongWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "错词本", description = "错词列表、批量复习、重置状态")
@RestController
@RequestMapping("/word/wrong-words")
@RequiredArgsConstructor
@AuthRequired
public class WrongWordController {

    private final WrongWordService wrongWordService;

    @Operation(summary = "获取错词列表")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping
    public Result<List<WrongWordDTO>> getWrongWords(
            @RequestParam(required = false, defaultValue = "all") String timeFilter,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        return Result.success(wrongWordService.getWrongWords(userId, timeFilter, cursor, limit));
    }

    @Operation(summary = "批量复习错词")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/review-batch")
    public Result<Void> reviewBatch(@Valid @RequestBody ReviewBatchRequest request) {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        wrongWordService.reviewBatch(userId, request.wordIds());
        return Result.success();
    }

    @Operation(summary = "重置错词状态（标记为已掌握）")
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/{recordId}/reset")
    public Result<Void> resetWrongWord(@PathVariable Long recordId) {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        wrongWordService.resetWrongWord(userId, recordId);
        return Result.success();
    }
}

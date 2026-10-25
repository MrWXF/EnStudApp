package com.enstud.writing.controller;

import com.enstud.common.Result;
import com.enstud.common.SecurityContext;
import com.enstud.writing.dto.*;
import com.enstud.writing.service.WritingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "写作练习", description = "英语写作与智能批改")
@RestController
@RequestMapping("/writing")
@RequiredArgsConstructor
public class WritingController {

    private final WritingService writingService;

    @Operation(summary = "提交作文并获取批改")
    @PostMapping("/submit")
    public Result<CorrectionDTO> submit(@Valid @RequestBody SubmitWritingRequest request) {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) return Result.fail(401, "请先登录");
        return Result.success(writingService.submitAndCorrect(userId, request));
    }

    @Operation(summary = "获取批改结果")
    @GetMapping("/{writingId}/correction")
    public Result<CorrectionDTO> getCorrection(@PathVariable Long writingId) {
        return Result.success(writingService.getCorrection(writingId));
    }

    @Operation(summary = "获取写作历史")
    @GetMapping("/history")
    public Result<List<WritingDTO>> getHistory() {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) return Result.fail(401, "请先登录");
        return Result.success(writingService.getHistory(userId));
    }

    @Operation(summary = "获取范文")
    @GetMapping("/models")
    public Result<List<ModelEssayDTO>> getModelEssays(
            @RequestParam(defaultValue = "ESSAY") String topicType) {
        return Result.success(writingService.getModelEssays(topicType));
    }

    @Operation(summary = "删除作文")
    @DeleteMapping("/{writingId}")
    public Result<Void> delete(@PathVariable Long writingId) {
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) return Result.fail(401, "请先登录");
        writingService.deleteWriting(userId, writingId);
        return Result.success();
    }
}

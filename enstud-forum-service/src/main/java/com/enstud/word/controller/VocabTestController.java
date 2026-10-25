package com.enstud.word.controller;

import com.enstud.common.AuthRequired;
import com.enstud.common.Result;
import com.enstud.common.SecurityContext;
import com.enstud.word.dto.VocabTestAnswerRequest;
import com.enstud.word.dto.VocabTestResultDTO;
import com.enstud.word.dto.VocabTestWordDTO;
import com.enstud.word.service.VocabTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "词汇量测试", description = "按频段采样估算词汇量，支持分享卡片")
@RestController
@RequestMapping("/word/vocab-test")
@RequiredArgsConstructor
@AuthRequired
public class VocabTestController {

    private final VocabTestService vocabTestService;

    @Operation(summary = "获取测试单词", description = "6 个频段各 5 词 + 2 个防乱猜伪词，共 32 词乱序返回")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/words")
    public Result<List<VocabTestWordDTO>> getTestWords() {
        return Result.success(vocabTestService.getTestWords());
    }

    @Operation(summary = "提交作答并估算词汇量", description = "knowledge: 2=认识, 1=模糊, 0=不认识")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/submit")
    public Result<VocabTestResultDTO> submit(@Valid @RequestBody VocabTestAnswerRequest request) {
        Long userId = SecurityContext.getCurrentUserId();
        return Result.success(vocabTestService.submit(userId, request));
    }

    @Operation(summary = "获取最近一次测试结果")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/latest")
    public Result<VocabTestResultDTO> getLatest() {
        Long userId = SecurityContext.getCurrentUserId();
        return Result.success(vocabTestService.getLatest(userId));
    }
}

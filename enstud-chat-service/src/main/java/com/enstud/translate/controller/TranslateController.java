package com.enstud.translate.controller;

import com.enstud.common.Result;
import com.enstud.translate.dto.TranslateRequest;
import com.enstud.translate.dto.TranslateResponse;
import com.enstud.translate.service.TranslateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "翻译服务", description = "英汉互译服务")
@RestController
@RequestMapping("/translate")
public class TranslateController {

    private final TranslateService translateService;

    public TranslateController(TranslateService translateService) {
        this.translateService = translateService;
    }

    @Operation(summary = "文本翻译")
    @PostMapping("/text")
    public Result<TranslateResponse> translate(@Valid @RequestBody TranslateRequest request) {
        return Result.success(translateService.translate(request));
    }
}

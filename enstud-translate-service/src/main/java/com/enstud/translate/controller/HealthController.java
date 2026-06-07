package com.enstud.translate.controller;

import com.enstud.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "翻译服务", description = "英汉互译服务")
@RestController
@RequestMapping("/translate")
public class HealthController {
    @Operation(summary = "服务健康检查")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("翻译服务运行正常");
    }
}

package com.enstud.chat.controller;

import com.enstud.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI 对话", description = "AI 英语日常聊天服务")
@RestController
@RequestMapping("/chat")
public class HealthController {

    @Operation(summary = "服务健康检查")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("AI对话服务运行正常");
    }
}

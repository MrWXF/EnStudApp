package com.enstud.writing.controller;

import com.enstud.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "写作练习", description = "英语写作与智能批改服务")
@RestController
@RequestMapping("/writing")
public class HealthController {
    @Operation(summary = "服务健康检查")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("写作服务运行正常");
    }
}

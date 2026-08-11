package com.enstud.user.controller;

import com.enstud.common.Result;
import com.enstud.common.ai.AiUsageTracker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 内部接口：AI 用量查询（供前端 Dashboard 或网关调用，无需鉴权）
 */
@Tag(name = "AI 用量内部接口")
@RestController
@RequestMapping("/user/internal")
public class UserInternalController {

    private final AiUsageTracker aiUsageTracker;

    public UserInternalController(@Autowired(required = false) AiUsageTracker aiUsageTracker) {
        this.aiUsageTracker = aiUsageTracker;
    }

    @Operation(summary = "内部接口：查询用户今日 AI 用量")
    @GetMapping("/ai-usage/{userId}")
    public Result<AiUsageResponse> getAiUsage(@PathVariable Long userId) {
        if (aiUsageTracker == null) {
            return Result.success(new AiUsageResponse(Map.of(
                    "chat", new UsageDetail(0, 0),
                    "writing", new UsageDetail(0, 0),
                    "translate", new UsageDetail(0, 0)
            )));
        }
        Map<String, UsageDetail> details = Map.of(
                "chat", new UsageDetail(
                        aiUsageTracker.getUsedToday(userId, "chat"),
                        aiUsageTracker.getLimit("chat")
                ),
                "writing", new UsageDetail(
                        aiUsageTracker.getUsedToday(userId, "writing"),
                        aiUsageTracker.getLimit("writing")
                ),
                "translate", new UsageDetail(
                        aiUsageTracker.getUsedToday(userId, "translate"),
                        aiUsageTracker.getLimit("translate")
                )
        );
        return Result.success(new AiUsageResponse(details));
    }

    public record AiUsageResponse(Map<String, UsageDetail> features) {}

    public record UsageDetail(long usedToday, int dailyLimit) {}
}

package com.enstud.common.feign;

import com.enstud.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 对话服务 Feign 客户端
 */
@FeignClient(name = "enstud-ai-service", contextId = "chatServiceClient")
public interface ChatServiceClient {

    @GetMapping("/chat/internal/stats/{userId}")
    Result<Map<String, Object>> getStats(@PathVariable("userId") Long userId);
}

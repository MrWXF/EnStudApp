package com.enstud.common.feign;

import com.enstud.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 阅读服务 Feign 客户端
 */
@FeignClient(name = "enstud-content-service", contextId = "readServiceClient")
public interface ReadServiceClient {

    @GetMapping("/read/internal/stats/{userId}")
    Result<Map<String, Object>> getStats(@PathVariable("userId") Long userId);
}

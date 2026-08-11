package com.enstud.common.feign;

import com.enstud.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 用户服务 Feign 客户端
 */
@FeignClient(name = "enstud-user-service", contextId = "userServiceClient")
public interface UserServiceClient {

    @GetMapping("/user/internal/profile/{userId}")
    Result<Map<String, Object>> getProfile(@PathVariable("userId") Long userId);
}

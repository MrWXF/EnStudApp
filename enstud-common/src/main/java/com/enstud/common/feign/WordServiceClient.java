package com.enstud.common.feign;

import com.enstud.common.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 单词服务 Feign 客户端 — 跨服务获取单词学习数据
 */
@FeignClient(name = "enstud-content-service", contextId = "wordServiceClient")
public interface WordServiceClient {

    /**
     * 获取用户正在学习的单词列表
     */
    @GetMapping("/word/internal/learning-words/{userId}")
    Result<List<Map<String, Object>>> getLearningWords(@PathVariable("userId") Long userId,
                                                        @RequestParam(value = "limit", defaultValue = "20") int limit);

    /**
     * 获取用户今日待复习单词
     */
    @GetMapping("/word/internal/review-words/{userId}")
    Result<List<Map<String, Object>>> getReviewWords(@PathVariable("userId") Long userId);

    /**
     * 获取用户单词学习统计
     */
    @GetMapping("/word/internal/stats/{userId}")
    Result<Map<String, Object>> getStats(@PathVariable("userId") Long userId);
}

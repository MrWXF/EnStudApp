package com.enstud.forum.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户服务客户端
 * <p>通过 HTTP 调用 user-service 获取用户信息，结果缓存在本地 Map 中。</p>
 */
@Slf4j
@Component
public class UserClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${user-service.base-url:http://localhost:8081}")
    private String userServiceBaseUrl;

    /** 用户名缓存：userId -> username/nickname */
    private final Map<Long, String> nameCache = new ConcurrentHashMap<>();

    /**
     * 根据 userId 获取用户昵称/用户名
     *
     * @param userId 用户ID
     * @return 用户昵称，若用户名则返回 username；获取失败返回 null
     */
    public String getUserName(Long userId) {
        if (userId == null) return null;
        return nameCache.computeIfAbsent(userId, this::fetchUserName);
    }

    /**
     * 清除指定用户缓存
     */
    public void evictCache(Long userId) {
        nameCache.remove(userId);
    }

    /**
     * 从 user-service 获取用户名
     */
    private String fetchUserName(Long userId) {
        try {
            String url = userServiceBaseUrl + "/user/users/" + userId + "/name";
            var response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("data")) {
                return (String) response.get("data");
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败, userId={}, error={}", userId, e.getMessage());
        }
        return null;
    }
}

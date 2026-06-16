package com.enstud.forum.client;

import com.enstud.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 用户服务客户端：通过 RestTemplate 调用 user-service 获取用户信息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserClient {

    private final RestTemplate restTemplate;

    @Value("${enstud.services.user-url:http://localhost:8081}")
    private String userServiceUrl;

    /**
     * 根据用户 ID 获取用户名
     */
    public String getUserName(Long userId) {
        try {
            String url = userServiceUrl + "/user/users/" + userId + "/name";
            ParameterizedTypeReference<Result<String>> typeRef =
                    new ParameterizedTypeReference<>() {};
            Result<String> result = restTemplate.exchange(
                    url, HttpMethod.GET, null, typeRef
            ).getBody();
            if (result != null && result.getCode() == 0 && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.warn("获取用户名失败: userId={}", userId, e);
        }
        return "用户" + userId;
    }
}

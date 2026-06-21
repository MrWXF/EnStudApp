package com.enstud.common.test;

import com.enstud.common.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * 集成测试基类
 * <p>
 * 提供 MockMvc、JWT 生成、auth header 请求辅助方法。
 * 所有服务的集成测试应继承此类（直接或间接）。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    /** 测试用 JWT 密钥，与 application-test.yml 中 jwt.secret 一致 */
    protected static final String TEST_JWT_SECRET = "test-secret-for-integration-tests-only";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    // ==================== Auth 辅助方法 ====================

    /** 生成测试用 JWT Access Token */
    protected String generateAccessToken(Long userId, String username) {
        return JwtUtil.generateAccessToken(userId, username, TEST_JWT_SECRET);
    }

    // ==================== HTTP 请求辅助方法 ====================

    /** GET + X-User-Id/X-Username 模拟 Gateway 转发 */
    protected ResultActions getWithAuth(String url, Long userId, String username) throws Exception {
        return mockMvc.perform(get(url)
                .header("X-User-Id", userId.toString())
                .header("X-Username", username));
    }

    /** POST + X-User-Id/X-Username + JSON body */
    protected ResultActions postWithAuth(String url, Object body, Long userId, String username) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", userId.toString())
                .header("X-Username", username)
                .content(toJson(body)));
    }

    /** PUT + X-User-Id/X-Username + JSON body */
    protected ResultActions putWithAuth(String url, Object body, Long userId, String username) throws Exception {
        return mockMvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", userId.toString())
                .header("X-Username", username)
                .content(toJson(body)));
    }

    /** DELETE + X-User-Id/X-Username */
    protected ResultActions deleteWithAuth(String url, Long userId, String username) throws Exception {
        return mockMvc.perform(delete(url)
                .header("X-User-Id", userId.toString())
                .header("X-Username", username));
    }

    /** POST without auth headers */
    protected ResultActions postWithoutAuth(String url, Object body) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(body)));
    }

    /** GET without auth headers */
    protected ResultActions getWithoutAuth(String url) throws Exception {
        return mockMvc.perform(get(url));
    }

    /** DELETE without auth headers */
    protected ResultActions deleteWithoutAuth(String url) throws Exception {
        return mockMvc.perform(delete(url));
    }

    // ==================== 内部工具 ====================

    private String toJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}

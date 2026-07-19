package com.enstud.gateway;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Gateway JWT 鉴权集成测试
 * <p>
 * 启动真实 Gateway + JwtAuthFilter，Nacos/Redis 已禁用。
 * 路由规则仍然生效，但因无下游服务，鉴权通过后路由转发会 503。
 * <p>
 * 核心验证：白名单放行 ≠ 401，无 Token 被拦截 = 401。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@DisplayName("Gateway — JWT 鉴权过滤器")
class GatewayAuthTest extends GatewayAuthHelper {

    @Autowired
    private WebTestClient webClient;

    // ==================== 白名单路径 — 鉴权通过（路由可能 503 但不会被 401 拒绝） ====================

    @Test
    @DisplayName("白名单 /user/register → 鉴权放行，状态码 ≠ 401")
    void shouldAllowWhitelistedRegister() {
        webClient.get().uri("/user/register") // 路由存在，只是 LB 解析不到服务
                .exchange()
                .expectStatus().is5xxServerError(); // 503 — 鉴权过了但无后端
    }

    @Test
    @DisplayName("白名单 /forum/posts → 鉴权放行。")
    void shouldAllowWhitelistedForumPosts() {
        webClient.get().uri("/forum/posts")
                .exchange()
                .expectStatus().is5xxServerError(); // 503 不是 401
    }

    @Test
    @DisplayName("OPTIONS CORS 预检 → JWT 过滤器放行")
    void shouldAllowOptionsPreflight() {
        webClient.options().uri("/word/study")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .exchange()
                .expectStatus().value(Matchers.not(HttpStatus.UNAUTHORIZED.value()));
    }

    // ==================== 无 Token 场景 ====================

    @Test
    @DisplayName("无 Token 访问受保护路径 → 401")
    void shouldRejectWithoutToken() {
        webClient.get().uri("/user/profile")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.code").isEqualTo(401)
                .jsonPath("$.msg").value(Matchers.containsString("认证"));
    }

    @Test
    @DisplayName("无 Token 访问 /chat/sessions → 401")
    void shouldRejectChatWithoutToken() {
        webClient.get().uri("/chat/sessions")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("无 Token 访问 /word/study → 401")
    void shouldRejectWordWithoutToken() {
        webClient.get().uri("/word/study")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==================== 无效 Token 场景 ====================

    @Test
    @DisplayName("无效 Token → 401 Token 无效或已过期")
    void shouldRejectInvalidToken() {
        webClient.get().uri("/user/profile")
                .header("Authorization", "Bearer invalid.token.value")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.code").isEqualTo(401)
                .jsonPath("$.msg").value(Matchers.containsString("Token"));
    }

    @Test
    @DisplayName("空 Bearer Token → 401")
    void shouldRejectEmptyBearer() {
        webClient.get().uri("/user/profile")
                .header("Authorization", "Bearer ")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("错误密钥 Token → 401")
    void shouldRejectWrongSecretToken() {
        String token = generateToken(42L, "tester", "wrong-secret-key-xxxxxxxxxxxxxxxxxxxxxxxxx");
        webClient.get().uri("/user/profile")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    // ==================== 有效 Token 场景 ====================

    @Test
    @DisplayName("有效 Token → 鉴权通过，路由转发到后端（503 无后端，非 401）")
    void shouldPassWithValidToken() {
        String token = generateToken(1L, "user1");
        webClient.get().uri("/user/profile")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().is5xxServerError(); // 503 — 鉴权过了不是 401
    }

    @Test
    @DisplayName("有效 Token 访问 /word/wordbooks → 路由转发")
    void shouldPassWordWithValidToken() {
        String token = generateToken(1L, "user1");
        webClient.get().uri("/word/wordbooks")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().is5xxServerError(); // 503 不是 401
    }

    @Test
    @DisplayName("有效 Token 非 Bearer 方式 → 当做无 Token")
    void shouldRejectNonBearerAuthHeader() {
        String token = generateToken(1L, "user1");
        webClient.get().uri("/user/profile")
                .header("Authorization", token) // 缺少 Bearer 前缀
                .exchange()
                .expectStatus().isUnauthorized();
    }
}

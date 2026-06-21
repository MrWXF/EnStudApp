package com.enstud.user;

import com.enstud.common.test.DbIntegrationTest;
import com.enstud.user.client.StatsClient;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * user-service 学习仪表盘统计 集成测试
 * <p>
 * StatsClient 被 Mock 以避免对其他微服务的 HTTP 调用
 */
@DisplayName("用户服务 — 学习统计")
class StatsIntegrationTest extends DbIntegrationTest {

    @MockBean
    private StatsClient statsClient;

    private static final String TEST_USERNAME = "statsuser";
    private static final String TEST_PASSWORD = "Test@1234";
    private static final String TEST_EMAIL = "stats@example.com";

    private Long testUserId;

    @BeforeEach
    void setUp() throws Exception {
        // 使用原生 SQL 插入测试用户（绕过 BCrypt）
        jdbcTemplate.update(
                "INSERT INTO enstud_user (username, email, password_hash, nickname) VALUES (?, ?, ?, ?)",
                TEST_USERNAME, TEST_EMAIL,
                "$2a$10$dXJ3SW6G7P50lLzM2pzM7eO0rQvZ3Gf0D3qE5q8v8x8h8j8k8l8m",
                "StatsUser");

        testUserId = jdbcTemplate.queryForObject(
                "SELECT id FROM enstud_user WHERE username = ?", Long.class, TEST_USERNAME);

        // Mock StatsClient 返回预设统计
        Map<String, Object> mockStats = new HashMap<>();
        mockStats.put("todayLearned", 5L);
        mockStats.put("totalLearned", 120L);
        mockStats.put("totalWritings", 3L);
        mockStats.put("totalChats", 15L);
        mockStats.put("totalPosts", 2L);

        when(statsClient.getUserStats(anyLong()))
                .thenReturn(com.enstud.common.Result.success(mockStats));
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM enstud_user_profile");
        jdbcTemplate.update("DELETE FROM enstud_user WHERE username = ?", TEST_USERNAME);
    }

    @Test
    @DisplayName("获取仪表盘统计（有 X-User-Id header）→ 返回聚合数据")
    void shouldGetStatsWithAuth() throws Exception {
        getWithAuth("/user/stats", testUserId, TEST_USERNAME)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.todayLearned").value(5))
                .andExpect(jsonPath("$.data.totalLearned").value(120));
    }

    @Test
    @DisplayName("获取仪表盘统计（无 X-User-Id header）→ 401（@AuthRequired）")
    void shouldRejectStatsWithoutAuth() throws Exception {
        getWithoutAuth("/user/stats")
                .andExpect(status().isUnauthorized());
    }
}

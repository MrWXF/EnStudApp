package com.enstud.user;

import com.enstud.common.test.DbIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * user-service 用户注册/登录/个人信息 集成测试
 */
@DisplayName("用户服务 — 注册/登录/个人信息")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserIntegrationTest extends DbIntegrationTest {

    private static final String TEST_USERNAME = "newuser";
    private static final String TEST_PASSWORD = "Test@1234";
    private static final String TEST_EMAIL = "newuser@example.com";

    // ==================== 注册 ====================

    @Test
    @Order(1)
    @DisplayName("正常注册新用户 → code=0, DB 存在")
    void shouldRegisterNewUser() throws Exception {
        Map<String, String> body = Map.of(
                "username", TEST_USERNAME,
                "password", TEST_PASSWORD,
                "email", TEST_EMAIL,
                "nickname", "NewUser"
        );

        postWithoutAuth("/user/register", body)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 验证 DB 中确实插入了用户
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM enstud_user WHERE username = ?", Integer.class, TEST_USERNAME);
        Assertions.assertEquals(1, count);
    }

    @Test
    @Order(2)
    @DisplayName("重复用户名注册 → code=1002")
    void shouldRejectDuplicateUsername() throws Exception {
        Map<String, String> body = Map.of(
                "username", TEST_USERNAME,  // same as test #1
                "password", TEST_PASSWORD,
                "email", "other@example.com",
                "nickname", "DupUser"
        );

        postWithoutAuth("/user/register", body)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1002))
                .andExpect(jsonPath("$.msg", containsString("用户名")));
    }

    @Test
    @Order(3)
    @DisplayName("重复邮箱注册 → code=1003")
    void shouldRejectDuplicateEmail() throws Exception {
        Map<String, String> body = Map.of(
                "username", "anotheruser",
                "password", TEST_PASSWORD,
                "email", TEST_EMAIL,  // same as test #1
                "nickname", "DupEmail"
        );

        postWithoutAuth("/user/register", body)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1003))
                .andExpect(jsonPath("$.msg", containsString("邮箱")));
    }

    @Test
    @Order(4)
    @DisplayName("无效邮箱格式 → 400")
    void shouldRejectInvalidEmail() throws Exception {
        Map<String, String> body = Map.of(
                "username", "emailuser",
                "password", TEST_PASSWORD,
                "email", "notanemail"
        );

        postWithoutAuth("/user/register", body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @Order(5)
    @DisplayName("短密码（< 6 位）→ 400")
    void shouldRejectShortPassword() throws Exception {
        Map<String, String> body = Map.of(
                "username", "shortpw",
                "password", "123",
                "email", "short@example.com"
        );

        postWithoutAuth("/user/register", body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    // ==================== 登录 ====================

    @Test
    @Order(6)
    @DisplayName("正确凭据登录 → 返回 accessToken + refreshToken")
    void shouldLoginWithCorrectCredentials() throws Exception {
        Map<String, String> body = Map.of(
                "username", TEST_USERNAME,
                "password", TEST_PASSWORD
        );

        postWithoutAuth("/user/login", body)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accessToken", not(emptyString())))
                .andExpect(jsonPath("$.data.refreshToken", not(emptyString())))
                .andExpect(jsonPath("$.data.username").value(TEST_USERNAME));
    }

    @Test
    @Order(7)
    @DisplayName("错误密码登录 → code=1004")
    void shouldRejectWrongPassword() throws Exception {
        Map<String, String> body = Map.of(
                "username", TEST_USERNAME,
                "password", "WrongPassword"
        );

        postWithoutAuth("/user/login", body)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1004));
    }

    @Test
    @Order(8)
    @DisplayName("不存在用户登录 → code=1001")
    void shouldRejectNonExistentUser() throws Exception {
        Map<String, String> body = Map.of(
                "username", "nonexistent_user_999",
                "password", TEST_PASSWORD
        );

        postWithoutAuth("/user/login", body)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    // ==================== 个人信息 ====================

    @Test
    @Order(9)
    @DisplayName("获取个人信息（有 X-User-Id header）→ 返回正确数据")
    void shouldGetProfileWithAuth() throws Exception {
        // 注册用户后通过 DB 查 ID 获取 profile
        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM enstud_user WHERE username = ?", Long.class, TEST_USERNAME);

        getWithAuth("/user/profile", userId, TEST_USERNAME)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value(TEST_USERNAME))
                .andExpect(jsonPath("$.data.email").value(TEST_EMAIL));
    }

    @Test
    @Order(10)
    @DisplayName("获取个人信息（无 X-User-Id header）→ code=401")
    void shouldRejectProfileWithoutAuth() throws Exception {
        getWithoutAuth("/user/profile")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    // ==================== 内部接口 ====================

    @Test
    @Order(11)
    @DisplayName("内部接口：获取用户名")
    void shouldGetUserName() throws Exception {
        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM enstud_user WHERE username = ?", Long.class, TEST_USERNAME);

        getWithoutAuth("/user/users/" + userId + "/name")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data", not(emptyString())));
    }
}

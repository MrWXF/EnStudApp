package com.enstud.common;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT 工具类单元测试
 */
@DisplayName("JWT — Token 生成/解析/校验")
class JwtUtilTest {

    private static final String SECRET = "test-unit-secret-key-for-junit-12345678";
    private static final Long USER_ID = 42L;
    private static final String USERNAME = "tester";

    // ==================== Token 生成 ====================

    @Test
    @DisplayName("生成 Access Token — 包含 userId sub + username claim")
    void shouldGenerateAccessToken() {
        String token = JwtUtil.generateAccessToken(USER_ID, USERNAME, SECRET);
        assertNotNull(token);
        assertTrue(token.length() > 50);

        Claims claims = JwtUtil.parseToken(token, SECRET);
        assertEquals("42", claims.getSubject());
        assertEquals("tester", claims.get("username", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    @DisplayName("生成 Refresh Token — 7 天过期")
    void shouldGenerateRefreshTokenWithLongExpiry() {
        String token = JwtUtil.generateRefreshToken(USER_ID, SECRET);
        Claims claims = JwtUtil.parseToken(token, SECRET);
        assertEquals("42", claims.getSubject());
        // 7 天 ≈ 604800000 ms
        long diff = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertTrue(diff > 600_000_000, "expect >10 min: " + diff + "ms");
    }

    // ==================== Token 解析 ====================

    @Test
    @DisplayName("parseToken — 正常解析返回 Claims")
    void shouldParseToken() {
        String token = JwtUtil.generateAccessToken(USER_ID, USERNAME, SECRET);
        Claims claims = JwtUtil.parseToken(token, SECRET);
        assertEquals("42", claims.getSubject());
    }

    @Test
    @DisplayName("parseToken — 不同密钥抛出异常")
    void shouldRejectTokenWithWrongSecret() {
        String token = JwtUtil.generateAccessToken(USER_ID, USERNAME, SECRET);
        assertThrows(Exception.class, () ->
                JwtUtil.parseToken(token, "wrong-secret-key-different-12345678"));
    }

    @Test
    @DisplayName("parseToken — 篡改 Token 抛出异常")
    void shouldRejectTamperedToken() {
        String token = JwtUtil.generateAccessToken(USER_ID, USERNAME, SECRET);
        String tampered = token.substring(0, token.length() - 5) + "abcde";
        assertThrows(Exception.class, () ->
                JwtUtil.parseToken(tampered, SECRET));
    }

    @Test
    @DisplayName("parseToken — 空字符串抛出异常")
    void shouldRejectEmptyToken() {
        assertThrows(Exception.class, () ->
                JwtUtil.parseToken("", SECRET));
    }

    @Test
    @DisplayName("parseToken — null 抛出异常")
    void shouldRejectNullToken() {
        assertThrows(Exception.class, () ->
                JwtUtil.parseToken(null, SECRET));
    }

    // ==================== getUserIdFromToken ====================

    @Test
    @DisplayName("getUserIdFromToken — 正常返回 userId")
    void shouldGetUserIdFromToken() {
        String token = JwtUtil.generateAccessToken(USER_ID, USERNAME, SECRET);
        Long uid = JwtUtil.getUserIdFromToken(token, SECRET);
        assertEquals(42L, uid);
    }

    @Test
    @DisplayName("getUserIdFromToken — 无效 Token 返回 null")
    void shouldReturnNullForInvalidToken() {
        assertNull(JwtUtil.getUserIdFromToken("invalid.token.here", SECRET));
    }

    // ==================== validateToken ====================

    @Test
    @DisplayName("validateToken — 有效 Token 返回 true")
    void shouldValidateToken() {
        String token = JwtUtil.generateAccessToken(USER_ID, USERNAME, SECRET);
        assertTrue(JwtUtil.validateToken(token, SECRET));
    }

    @Test
    @DisplayName("validateToken — 无效 Token 返回 false")
    void shouldNotValidateInvalidToken() {
        assertFalse(JwtUtil.validateToken("garbage-token-value", SECRET));
    }

    @Test
    @DisplayName("validateToken — null Token 返回 false")
    void shouldNotValidateNullToken() {
        assertFalse(JwtUtil.validateToken(null, SECRET));
    }
}

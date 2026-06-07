package com.enstud.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类 — Token 生成和校验
 */
@Slf4j
public class JwtUtil {

    /** 默认密钥（生产环境必须通过配置覆盖） */
    private static final String DEFAULT_SECRET = "enstud-app-jwt-secret-key-2024-production-must-change";
    private static final long ACCESS_TOKEN_EXPIRE = 1000 * 60 * 60 * 2;  // 2 小时
    private static final long REFRESH_TOKEN_EXPIRE = 1000 * 60 * 60 * 24 * 7;  // 7 天

    private static SecretKey getKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /** 生成 Access Token */
    public static String generateAccessToken(Long userId, String username, String secret) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE))
                .signWith(getKey(secret))
                .compact();
    }

    /** 生成 Refresh Token */
    public static String generateRefreshToken(Long userId, String secret) {
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE))
                .signWith(getKey(secret))
                .compact();
    }

    /** 解析 Token */
    public static Claims parseToken(String token, String secret) {
        return Jwts.parser()
                .verifyWith(getKey(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 从 Token 中提取用户 ID */
    public static Long getUserIdFromToken(String token, String secret) {
        try {
            Claims claims = parseToken(token, secret);
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            log.warn("解析 Token 中的 userId 失败: {}", e.getMessage());
            return null;
        }
    }

    /** 校验 Token 是否有效 */
    public static boolean validateToken(String token, String secret) {
        try {
            parseToken(token, secret);
            return true;
        } catch (Exception e) {
            log.warn("Token 校验失败: {}", e.getMessage());
            return false;
        }
    }
}

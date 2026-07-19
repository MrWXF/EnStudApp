package com.enstud.gateway;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Gateway 测试辅助类
 */
public abstract class GatewayAuthHelper {

    private static final String TEST_SECRET = "test-gateway-secret-key-for-integration-tests";

    protected static String generateToken(Long userId, String username) {
        return generateToken(userId, username, TEST_SECRET);
    }

    protected static String generateToken(Long userId, String username, String secret) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(key)
                .compact();
    }
}

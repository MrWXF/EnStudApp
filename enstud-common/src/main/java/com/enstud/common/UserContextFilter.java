package com.enstud.common;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 用户上下文过滤器
 * 优先从 Gateway 传递的 X-User-Id 读取，
 * 如果不存在则从 Authorization Bearer JWT 中解析
 */
@Slf4j
@Component
@Order(1)
public class UserContextFilter implements Filter {

    private static final String JWT_SECRET = "enstud-app-jwt-secret-key-2024-production-must-change";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String userIdHeader = httpRequest.getHeader("X-User-Id");
        String usernameHeader = httpRequest.getHeader("X-Username");

        // Gateway 没传 header 时，从 Authorization Bearer Token 解析
        if (userIdHeader == null) {
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    Long userId = JwtUtil.getUserIdFromToken(token, JWT_SECRET);
                    if (userId != null) {
                        userIdHeader = userId.toString();
                        var claims = JwtUtil.parseToken(token, JWT_SECRET);
                        usernameHeader = (String) claims.get("username");
                    }
                } catch (Exception e) {
                    log.debug("JWT 解析失败: {}", e.getMessage());
                }
            }
        }

        if (userIdHeader != null) {
            try {
                SecurityContext.setCurrentUserId(Long.parseLong(userIdHeader));
                SecurityContext.setCurrentUsername(usernameHeader);
            } catch (NumberFormatException e) {
                log.warn("无效的 UserId: {}", userIdHeader);
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            SecurityContext.clear();
        }
    }
}

package com.enstud.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 从 Gateway 设置的请求头解析用户信息并设置 SecurityContext，
 * 以及在请求结束后清理 SecurityContext，防止 ThreadLocal 内存泄漏。
 *
 * Gateway 的 JwtAuthFilter 会在转发请求前设置以下请求头：
 *   - X-User-Id: 用户ID
 *   - X-Username: 用户名
 */
@Slf4j
public class SecurityContextInterceptor implements HandlerInterceptor {

    /** 从 Gateway 转发的请求头中读取用户 ID */
    private static final String HEADER_USER_ID = "X-User-Id";
    /** 从 Gateway 转发的请求头中读取用户名 */
    private static final String HEADER_USERNAME = "X-Username";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        // 从请求头读取 Gateway 设置的用户信息
        String userIdStr = request.getHeader(HEADER_USER_ID);
        String username = request.getHeader(HEADER_USERNAME);

        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                Long userId = Long.parseLong(userIdStr);
                SecurityContext.setCurrentUserId(userId);
                if (username != null && !username.isEmpty()) {
                    SecurityContext.setCurrentUsername(username);
                }
                log.trace("SecurityContext set: userId={}, username={}", userId, username);
            } catch (NumberFormatException e) {
                log.warn("Invalid X-User-Id header: {}", userIdStr);
            }
        } else {
            log.trace("No X-User-Id header, SecurityContext remains empty");
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        SecurityContext.clear();
    }
}

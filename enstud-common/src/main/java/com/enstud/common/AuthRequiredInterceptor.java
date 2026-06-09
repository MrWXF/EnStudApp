package com.enstud.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.Set;

/**
 * 用户认证拦截器
 * <p>
 * 检查已声明 @AuthRequired 的接口，SecurityContext 中是否有 userId。
 * 若未登录则直接返回 401，避免 Controller 中重复写 {@code if (userId == null)}。
 * <p>
 * 使用方式：在需要登录的 Controller 类或方法上标注 {@code @AuthRequired}
 * <pre>{@code
 * @RestController
 * @RequestMapping("/word")
 * @AuthRequired  // 整个 Controller 需要登录
 * public class WordController { ... }
 *
 * // 或只在特定方法上标注
 * @GetMapping("/public/stats")
 * public Result<?> getPublicStats() { ... }  // 无需登录
 * }</pre>
 */
@Slf4j
public class AuthRequiredInterceptor implements HandlerInterceptor {

    /** 白名单路径前缀（不需要登录的接口） */
    private static final Set<String> WHITE_LIST = Set.of(
            "/user/login", "/user/register", "/health", "/actuator",
            "/v3/api-docs", "/swagger-ui", "/doc.html", "/webjars"
    );

    /** 默认允许通过的 HTTP 方法 */
    private static final List<String> SAFE_METHODS = List.of("OPTIONS");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // OPTIONS 预检请求放行
        if (SAFE_METHODS.contains(request.getMethod().toUpperCase())) {
            return true;
        }

        // 路径白名单放行
        String path = request.getRequestURI();
        for (String prefix : WHITE_LIST) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }

        // 只拦截 HandlerMethod（跳过静态资源等）
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }

        // 检查方法或类是否有 @AuthRequired 注解
        AuthRequired authRequired = hm.getMethodAnnotation(AuthRequired.class);
        if (authRequired == null) {
            authRequired = hm.getBeanType().getAnnotation(AuthRequired.class);
        }

        // 未标注 @AuthRequired 的方法不需要登录
        if (authRequired == null) {
            return true;
        }

        // 检查用户是否已登录
        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) {
            log.debug("未登录请求被拦截: {} {} (handler={})",
                    request.getMethod(), path, hm.getMethod().getName());

            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":401,\"msg\":\"请先登录\",\"data\":null}");
            return false;
        }

        return true;
    }
}

package com.enstud.common;

/**
 * 当前登录用户上下文（ThreadLocal 实现）
 * 由 Gateway 的 JWT 过滤器设置，Service 层通过此类获取当前用户
 *
 * <pre>{@code
 * Long userId = SecurityContext.getCurrentUserId();
 * }</pre>
 */
public class SecurityContext {
    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME_HOLDER = new ThreadLocal<>();

    /** 设置当前用户 ID */
    public static void setCurrentUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    /** 获取当前用户 ID */
    public static Long getCurrentUserId() {
        return USER_ID_HOLDER.get();
    }

    /** 设置当前用户名 */
    public static void setCurrentUsername(String username) {
        USERNAME_HOLDER.set(username);
    }

    /** 获取当前用户名 */
    public static String getCurrentUsername() {
        return USERNAME_HOLDER.get();
    }

    /** 清理上下文（请求结束后调用，防止内存泄漏） */
    public static void clear() {
        USER_ID_HOLDER.remove();
        USERNAME_HOLDER.remove();
    }
}

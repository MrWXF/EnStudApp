package com.enstud.common;

import java.lang.annotation.*;

/**
 * 标记需要用户登录认证的 Controller 类或方法
 * <p>
 * 配合 {@link AuthRequiredInterceptor} 使用，自动拦截未登录请求并返回 401。
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 整个 Controller 都需要登录
 * @RestController
 * @AuthRequired
 * public class WordController { ... }
 *
 * // 或只在特定方法上标注
 * @GetMapping("/profile")
 * @AuthRequired
 * public Result<UserProfileDTO> getProfile() { ... }
 * }</pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthRequired {
}

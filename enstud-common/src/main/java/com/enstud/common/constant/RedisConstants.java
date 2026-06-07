package com.enstud.common.constant;

/**
 * Redis Key 常量
 */
public final class RedisConstants {

    private RedisConstants() {}

    /** 用户 Token 前缀 */
    public static final String TOKEN_PREFIX = "enstud:token:";

    /** 用户信息缓存 */
    public static final String USER_INFO_PREFIX = "enstud:user:info:";

    /** 验证码 */
    public static final String VERIFY_CODE_PREFIX = "enstud:verify:code:";

    /** Token 过期时间（秒） */
    public static final long TOKEN_EXPIRE_SECONDS = 7200L;

    /** 验证码过期时间（秒） */
    public static final long VERIFY_CODE_EXPIRE_SECONDS = 300L;

    /** 构造 Token Key */
    public static String tokenKey(Long userId) {
        return TOKEN_PREFIX + userId;
    }

    /** 构造用户信息 Key */
    public static String userInfoKey(Long userId) {
        return USER_INFO_PREFIX + userId;
    }
}

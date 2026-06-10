package com.enstud.common.constant;

/**
 * 业务错误码枚举（1000~9999）
 * <p>
 * 格式：模块前缀(千位) + 具体错误(个十百位)
 * <pre>
 *   1xxx — 用户模块
 *   2xxx — 单词模块
 *   3xxx — AI对话模块
 *   4xxx — 写作模块
 *   5xxx — 翻译/通用AI模块
 *   6xxx — 论坛模块
 * </pre>
 */
public enum ErrorCode {

    // ===== 通用 =====
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // ===== 1xxx — 用户模块 =====
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_EXISTS(1002, "用户名已存在"),
    EMAIL_REGISTERED(1003, "邮箱已被注册"),
    PASSWORD_ERROR(1004, "密码错误"),

    // ===== 2xxx — 单词模块 =====
    WORD_NOT_FOUND(2001, "单词不存在"),
    WORD_BOOK_NOT_FOUND(2002, "词库不存在"),
    INVALID_MEMORY_LEVEL(2003, "记忆等级必须在 0-5 之间"),
    WORD_NOT_LEARNED_YET(2004, "尚未学习该单词，请先复习后再调整记忆等级"),

    // ===== 3xxx — AI对话模块 =====
    CONVERSATION_NOT_FOUND(3001, "对话不存在"),

    // ===== 4xxx — 写作模块 =====
    WRITING_NOT_FOUND(4001, "作文不存在"),
    CORRECTION_NOT_FOUND(4002, "批改结果不存在"),

    // ===== 5xxx — AI通用 =====
    AI_RESPONSE_EMPTY(5001, "AI 返回内容为空，请稍后重试"),
    AI_SERVICE_ERROR(5002, "AI 服务调用异常，请稍后重试"),

    // ===== 6xxx — 论坛模块 =====
    POST_NOT_FOUND(6001, "帖子不存在"),
    CATEGORY_NOT_FOUND(6002, "板块不存在");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

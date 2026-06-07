package com.enstud.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务错误码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // 通用错误码
    SUCCESS(0, "操作成功"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或 Token 已过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源冲突"),
    SERVER_ERROR(500, "服务器内部错误"),

    // 用户模块 1xxx
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_EXISTS(1002, "用户名已存在"),
    EMAIL_EXISTS(1003, "邮箱已被注册"),
    PASSWORD_ERROR(1004, "密码错误"),
    USER_DISABLED(1005, "账号已被禁用"),

    // 单词模块 2xxx
    WORD_NOT_FOUND(2001, "单词不存在"),
    WORDBOOK_NOT_FOUND(2002, "词库不存在"),
    ALREADY_MASTERED(2003, "该单词已掌握"),

    // AI对话模块 3xxx
    CHAT_SESSION_NOT_FOUND(3001, "对话不存在"),
    AI_SERVICE_ERROR(3002, "AI 服务异常"),

    // 写作模块 4xxx
    WRITING_NOT_FOUND(4001, "作文不存在"),
    CORRECTION_FAILED(4002, "批改服务异常"),

    // 翻译模块 5xxx
    TRANSLATE_FAILED(5001, "翻译服务异常"),

    // 论坛模块 6xxx
    POST_NOT_FOUND(6001, "帖子不存在"),
    REPLY_NOT_FOUND(6002, "回复不存在"),
    CONTENT_SENSITIVE(6003, "内容包含敏感词"),
    ;

    private final int code;
    private final String message;
}

package com.enstud.common;

import lombok.Getter;

/**
 * 业务异常（全局异常处理器统一拦截）
 * <pre>{@code
 * throw new BusinessException(404, "用户不存在");
 * throw new BusinessException("密码错误");
 * }</pre>
 */
@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
}

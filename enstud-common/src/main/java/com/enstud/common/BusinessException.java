package com.enstud.common;

import com.enstud.common.constant.ErrorCode;
import lombok.Getter;

/**
 * 业务异常（全局异常处理器统一拦截）
 * <pre>{@code
 * throw new BusinessException(ErrorCode.USER_NOT_FOUND);
 * throw new BusinessException(ErrorCode.USER_NOT_FOUND, "自定义描述");
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

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.code = errorCode.getCode();
    }

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
}

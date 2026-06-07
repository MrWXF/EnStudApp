package com.enstud.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应封装
 * <pre>{@code
 * // 成功返回
 * return Result.success(data);
 * // 失败返回
 * return Result.fail(400, "参数错误");
 * }</pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    /** 0=成功，非0=错误码 */
    private int code;
    /** 提示信息 */
    private String msg;
    /** 响应数据 */
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(0, "success", data);
    }

    public static <T> Result<T> success() {
        return new Result<>(0, "success", null);
    }

    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static <T> Result<T> fail(String msg) {
        return new Result<>(500, msg, null);
    }
}

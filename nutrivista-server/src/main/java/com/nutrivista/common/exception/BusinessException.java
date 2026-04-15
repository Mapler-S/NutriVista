package com.nutrivista.common.exception;

import lombok.Getter;

/**
 * 业务异常 — 用于可预期的业务错误（如数据不存在、参数非法等）
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

    // ===== 常用静态工厂方法 =====

    public static BusinessException notFound(String resource) {
        return new BusinessException(404, resource + " 不存在");
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(400, message);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(409, message);
    }
}

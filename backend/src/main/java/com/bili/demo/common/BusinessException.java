package com.bili.demo.common;

import lombok.Getter;

/**
 * 自定义业务异常
 * 用于在 Service 层抛出业务错误(如登录失败、用户已存在等)
 * 由 GlobalExceptionHandler 统一捕获并返回友好提示
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}

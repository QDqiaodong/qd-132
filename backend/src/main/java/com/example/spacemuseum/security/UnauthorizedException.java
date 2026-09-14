package com.example.spacemuseum.security;

/**
 * 未认证异常：缺少或携带了无效的身份信息。
 * 由 GlobalExceptionHandler 映射为 HTTP 401。
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}

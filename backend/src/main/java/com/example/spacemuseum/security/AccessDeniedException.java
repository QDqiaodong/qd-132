package com.example.spacemuseum.security;

/**
 * 越权访问异常：已识别身份，但无权访问目标资源或执行目标操作。
 * 由 GlobalExceptionHandler 映射为 HTTP 403。
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}

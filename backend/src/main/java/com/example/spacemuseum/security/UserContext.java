package com.example.spacemuseum.security;

/**
 * 基于 ThreadLocal 的当前请求身份上下文，由 AuthInterceptor 在请求进入时写入、
 * 请求结束时清理，业务代码通过 SecurityUtils 读取。
 */
public final class UserContext {

    private static final ThreadLocal<CurrentUser> CURRENT = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(CurrentUser user) {
        CURRENT.set(user);
    }

    public static CurrentUser get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}

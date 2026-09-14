package com.example.spacemuseum.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 身份解析拦截器：从请求头解析当前用户身份并写入 UserContext。
 * 缺少身份或身份非法时抛出 UnauthorizedException（401），
 * 请求结束后清理 ThreadLocal，防止线程复用导致身份串号。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String HEADER_ROLE = "X-User-Role";
    public static final String HEADER_GROUP_ID = "X-Group-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // CORS 预检请求直接放行
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String roleHeader = request.getHeader(HEADER_ROLE);
        if (roleHeader == null || roleHeader.isBlank()) {
            throw new UnauthorizedException("未登录或身份已失效");
        }

        Role role;
        try {
            role = Role.valueOf(roleHeader.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("无效的用户角色");
        }

        Long groupId = null;
        if (role == Role.TEACHER) {
            String groupHeader = request.getHeader(HEADER_GROUP_ID);
            if (groupHeader == null || groupHeader.isBlank()) {
                throw new UnauthorizedException("带队老师身份缺少所属研学团");
            }
            try {
                groupId = Long.parseLong(groupHeader.trim());
            } catch (NumberFormatException e) {
                throw new UnauthorizedException("无效的研学团标识");
            }
        }

        UserContext.set(new CurrentUser(role, groupId));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}

package com.example.spacemuseum.security;

/**
 * 鉴权工具：所有"按角色收窄可见范围"的判断都收敛在这里，
 * 保证列表、详情、写操作使用同一套规则。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /** 获取当前请求身份；未通过认证拦截器时视为未登录。 */
    public static CurrentUser currentUser() {
        CurrentUser user = UserContext.get();
        if (user == null) {
            throw new UnauthorizedException("未登录或身份已失效");
        }
        return user;
    }

    /** 仅馆务人员可执行（如新增、改派、取消配对等写操作）。 */
    public static void requireStaff() {
        if (!currentUser().isStaff()) {
            throw new AccessDeniedException("仅馆务人员可执行该操作");
        }
    }

    /**
     * 访问某个研学团的配对数据前校验：
     * 馆务可访问全部；带队老师仅可访问本团，访问他团一律按越权拦截。
     */
    public static void requireGroupAccess(Long studyGroupId) {
        CurrentUser user = currentUser();
        if (user.isStaff()) {
            return;
        }
        if (studyGroupId == null || !studyGroupId.equals(user.groupId())) {
            throw new AccessDeniedException("无权访问其他研学团的配对信息");
        }
    }
}

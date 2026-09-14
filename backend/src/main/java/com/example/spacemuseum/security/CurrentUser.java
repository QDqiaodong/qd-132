package com.example.spacemuseum.security;

/**
 * 当前请求的用户身份。
 * groupId 仅对带队老师有意义，表示其带领的研学团 id；馆务人员为 null。
 */
public record CurrentUser(Role role, Long groupId) {

    public boolean isStaff() {
        return role == Role.STAFF;
    }

    public boolean isTeacher() {
        return role == Role.TEACHER;
    }
}

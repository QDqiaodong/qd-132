package com.example.spacemuseum.security;

/**
 * 系统角色：
 * STAFF   馆务人员，可查看全部研学团配对并维护台账；
 * TEACHER 带队老师，仅可查看本团配对，不能改写配对。
 */
public enum Role {
    STAFF,
    TEACHER
}

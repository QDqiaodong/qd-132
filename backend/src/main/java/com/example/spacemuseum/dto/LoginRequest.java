package com.example.spacemuseum.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    /** 角色：STAFF（馆务）或 TEACHER（带队老师） */
    @NotBlank(message = "角色不能为空")
    private String role;

    /** 带队老师登录时必填，用于绑定其带领的研学团 */
    private String groupCode;
}

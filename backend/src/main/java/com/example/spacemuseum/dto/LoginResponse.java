package com.example.spacemuseum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String role;

    /** 带队老师所属研学团 id，馆务为 null */
    private Long groupId;

    private String groupName;

    private String groupCode;
}

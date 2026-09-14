package com.example.spacemuseum.controller;

import com.example.spacemuseum.dto.LoginRequest;
import com.example.spacemuseum.dto.LoginResponse;
import com.example.spacemuseum.entity.StudyGroup;
import com.example.spacemuseum.repository.StudyGroupRepository;
import com.example.spacemuseum.security.Role;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 身份登录：馆务直接登录；带队老师凭本团团号登录并绑定所属研学团，
 * 后续请求携带该身份，由服务端按角色收窄可见范围。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Role role;
        try {
            role = Role.valueOf(request.getRole().trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new RuntimeException("无效的角色");
        }

        if (role == Role.STAFF) {
            return ResponseEntity.ok(new LoginResponse(Role.STAFF.name(), null, null, null));
        }

        String groupCode = request.getGroupCode();
        if (groupCode == null || groupCode.isBlank()) {
            throw new RuntimeException("带队老师登录必须填写本团团号");
        }
        StudyGroup group = studyGroupRepository.findByGroupCode(groupCode.trim())
                .orElseThrow(() -> new RuntimeException("团号不存在，请核对后重试"));

        return ResponseEntity.ok(new LoginResponse(
                Role.TEACHER.name(), group.getId(), group.getGroupName(), group.getGroupCode()));
    }
}

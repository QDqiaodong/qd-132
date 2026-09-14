package com.example.spacemuseum.controller;

import com.example.spacemuseum.dto.RoutePlanDTO;
import com.example.spacemuseum.dto.RoutePlanRequest;
import com.example.spacemuseum.dto.RouteStopDTO;
import com.example.spacemuseum.security.CurrentUser;
import com.example.spacemuseum.security.SecurityUtils;
import com.example.spacemuseum.service.RouteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 体验动线接口。可见范围按角色收窄：
 * 馆务（STAFF）可查看全部研学团动线并编排、放行、标记完成；
 * 带队老师（TEACHER）仅可查看本团动线，访问他团一律 403，且不能改动动线。
 */
@RestController
@RequestMapping("/api/routes")
public class RouteController {

    @Autowired
    private RouteService routeService;

    @GetMapping
    public ResponseEntity<List<RoutePlanDTO>> getAllRoutes() {
        CurrentUser user = SecurityUtils.currentUser();
        if (user.isStaff()) {
            return ResponseEntity.ok(routeService.getAllRoutes());
        }
        // 带队老师刷新列表时仍只拿到本团，保证可见范围与角色权限一致
        return ResponseEntity.ok(List.of(routeService.getGroupRoute(user.groupId())));
    }

    @GetMapping("/group/{studyGroupId}")
    public ResponseEntity<RoutePlanDTO> getGroupRoute(@PathVariable Long studyGroupId) {
        SecurityUtils.requireGroupAccess(studyGroupId);
        return ResponseEntity.ok(routeService.getGroupRoute(studyGroupId));
    }

    @PutMapping("/group/{studyGroupId}")
    public ResponseEntity<RoutePlanDTO> saveRoute(@PathVariable Long studyGroupId,
                                                  @Valid @RequestBody RoutePlanRequest request) {
        SecurityUtils.requireStaff();
        return ResponseEntity.ok(routeService.saveRoute(studyGroupId, request));
    }

    @PostMapping("/stops/{stopId}/release")
    public ResponseEntity<RouteStopDTO> releaseStop(@PathVariable Long stopId) {
        SecurityUtils.requireStaff();
        return ResponseEntity.ok(routeService.releaseStop(stopId));
    }

    @PostMapping("/stops/{stopId}/complete")
    public ResponseEntity<RouteStopDTO> completeStop(@PathVariable Long stopId) {
        SecurityUtils.requireStaff();
        return ResponseEntity.ok(routeService.completeStop(stopId));
    }

    @DeleteMapping("/group/{studyGroupId}")
    public ResponseEntity<Map<String, String>> deleteRoute(@PathVariable Long studyGroupId) {
        SecurityUtils.requireStaff();
        routeService.deleteRoute(studyGroupId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "动线已删除");
        return ResponseEntity.ok(response);
    }
}

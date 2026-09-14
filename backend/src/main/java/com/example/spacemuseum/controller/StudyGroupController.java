package com.example.spacemuseum.controller;

import com.example.spacemuseum.dto.DailyAttendanceDTO;
import com.example.spacemuseum.dto.StudyGroupDTO;
import com.example.spacemuseum.entity.StudyGroup;
import com.example.spacemuseum.security.CurrentUser;
import com.example.spacemuseum.security.SecurityUtils;
import com.example.spacemuseum.service.StudyGroupService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 研学团接口。馆务可见并维护全部研学团；
 * 带队老师仅可见本团，访问他团按越权拦截（403），且不能增删改。
 */
@RestController
@RequestMapping("/api/study-groups")
public class StudyGroupController {

    @Autowired
    private StudyGroupService studyGroupService;

    @GetMapping
    public ResponseEntity<List<StudyGroup>> getAllStudyGroups() {
        CurrentUser user = SecurityUtils.currentUser();
        if (user.isStaff()) {
            return ResponseEntity.ok(studyGroupService.getAllStudyGroups());
        }
        return ResponseEntity.ok(studyGroupService.getStudyGroupById(user.groupId())
                .map(List::of)
                .orElse(List.of()));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<StudyGroup>> getStudyGroupsByStatus(@PathVariable Integer status) {
        CurrentUser user = SecurityUtils.currentUser();
        if (user.isStaff()) {
            return ResponseEntity.ok(studyGroupService.getStudyGroupsByStatus(status));
        }
        return ResponseEntity.ok(studyGroupService.getStudyGroupById(user.groupId())
                .filter(g -> g.getStatus().equals(status))
                .map(List::of)
                .orElse(List.of()));
    }

    @GetMapping("/date/{visitDate}")
    public ResponseEntity<List<StudyGroup>> getStudyGroupsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate) {
        CurrentUser user = SecurityUtils.currentUser();
        if (user.isStaff()) {
            return ResponseEntity.ok(studyGroupService.getStudyGroupsByDate(visitDate));
        }
        return ResponseEntity.ok(studyGroupService.getStudyGroupById(user.groupId())
                .filter(g -> g.getVisitDate().equals(visitDate))
                .map(List::of)
                .orElse(List.of()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudyGroup> getStudyGroupById(@PathVariable Long id) {
        SecurityUtils.requireGroupAccess(id);
        return studyGroupService.getStudyGroupById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 查询某一参观日的到馆人数（当天已预约各团学生人数之和，实时按各团当前人数汇总）。
     * 到馆人数涉及全馆多个研学团，仅馆务可查。
     */
    @GetMapping("/attendance/date/{visitDate}")
    public ResponseEntity<DailyAttendanceDTO> getDailyAttendance(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate) {
        SecurityUtils.requireStaff();
        return ResponseEntity.ok(studyGroupService.getDailyAttendance(visitDate));
    }

    @PostMapping
    public ResponseEntity<StudyGroup> createStudyGroup(@Valid @RequestBody StudyGroupDTO dto) {
        SecurityUtils.requireStaff();
        return ResponseEntity.ok(studyGroupService.createStudyGroup(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudyGroup> updateStudyGroup(@PathVariable Long id, @Valid @RequestBody StudyGroupDTO dto) {
        SecurityUtils.requireStaff();
        return ResponseEntity.ok(studyGroupService.updateStudyGroup(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudyGroup(@PathVariable Long id) {
        SecurityUtils.requireStaff();
        studyGroupService.deleteStudyGroup(id);
        return ResponseEntity.noContent().build();
    }
}

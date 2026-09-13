package com.example.spacemuseum.controller;

import com.example.spacemuseum.dto.StudyGroupDTO;
import com.example.spacemuseum.entity.StudyGroup;
import com.example.spacemuseum.service.StudyGroupService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/study-groups")
public class StudyGroupController {

    @Autowired
    private StudyGroupService studyGroupService;

    @GetMapping
    public ResponseEntity<List<StudyGroup>> getAllStudyGroups() {
        return ResponseEntity.ok(studyGroupService.getAllStudyGroups());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<StudyGroup>> getStudyGroupsByStatus(@PathVariable Integer status) {
        return ResponseEntity.ok(studyGroupService.getStudyGroupsByStatus(status));
    }

    @GetMapping("/date/{visitDate}")
    public ResponseEntity<List<StudyGroup>> getStudyGroupsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate) {
        return ResponseEntity.ok(studyGroupService.getStudyGroupsByDate(visitDate));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudyGroup> getStudyGroupById(@PathVariable Long id) {
        return studyGroupService.getStudyGroupById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StudyGroup> createStudyGroup(@Valid @RequestBody StudyGroupDTO dto) {
        return ResponseEntity.ok(studyGroupService.createStudyGroup(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudyGroup> updateStudyGroup(@PathVariable Long id, @Valid @RequestBody StudyGroupDTO dto) {
        return ResponseEntity.ok(studyGroupService.updateStudyGroup(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudyGroup(@PathVariable Long id) {
        studyGroupService.deleteStudyGroup(id);
        return ResponseEntity.noContent().build();
    }
}
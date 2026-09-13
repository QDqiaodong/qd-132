package com.example.spacemuseum.controller;

import com.example.spacemuseum.dto.AllocationDTO;
import com.example.spacemuseum.dto.AllocationResultDTO;
import com.example.spacemuseum.dto.GroupAllocationDTO;
import com.example.spacemuseum.entity.Allocation;
import com.example.spacemuseum.service.AllocationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/allocations")
public class AllocationController {

    @Autowired
    private AllocationService allocationService;

    @GetMapping
    public ResponseEntity<List<Allocation>> getAllAllocations() {
        return ResponseEntity.ok(allocationService.getAllAllocations());
    }

    @GetMapping("/group/{studyGroupId}")
    public ResponseEntity<List<Allocation>> getAllocationsByGroup(@PathVariable Long studyGroupId) {
        return ResponseEntity.ok(allocationService.getAllocationsByGroup(studyGroupId));
    }

    @GetMapping("/group/{studyGroupId}/detail")
    public ResponseEntity<GroupAllocationDTO> getGroupAllocationDetail(@PathVariable Long studyGroupId) {
        return ResponseEntity.ok(allocationService.getGroupAllocationDetail(studyGroupId));
    }

    @GetMapping("/all-groups")
    public ResponseEntity<List<GroupAllocationDTO>> getAllGroupAllocations() {
        return ResponseEntity.ok(allocationService.getAllGroupAllocations());
    }

    @GetMapping("/date/{visitDate}")
    public ResponseEntity<List<Allocation>> getAllocationsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate) {
        return ResponseEntity.ok(allocationService.getAllocationsByDate(visitDate));
    }

    @PostMapping("/auto/{studyGroupId}")
    public ResponseEntity<List<AllocationResultDTO>> autoAllocate(@PathVariable Long studyGroupId) {
        return ResponseEntity.ok(allocationService.autoAllocate(studyGroupId));
    }

    @PostMapping
    public ResponseEntity<Allocation> manualAllocate(@Valid @RequestBody AllocationDTO dto) {
        return ResponseEntity.ok(allocationService.manualAllocate(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Allocation> updateAllocation(@PathVariable Long id, @Valid @RequestBody AllocationDTO dto) {
        return ResponseEntity.ok(allocationService.updateAllocation(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> cancelAllocation(@PathVariable Long id) {
        allocationService.cancelAllocation(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "分配已取消");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/group/{studyGroupId}")
    public ResponseEntity<Map<String, String>> cancelGroupAllocations(@PathVariable Long studyGroupId) {
        allocationService.cancelExistingAllocations(studyGroupId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "该研学团所有分配已取消");
        return ResponseEntity.ok(response);
    }
}
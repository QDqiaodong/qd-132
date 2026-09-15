package com.example.spacemuseum.controller;

import com.example.spacemuseum.dto.AllocationDTO;
import com.example.spacemuseum.dto.AllocationResultDTO;
import com.example.spacemuseum.dto.DeviceOccupancyDTO;
import com.example.spacemuseum.dto.ExperimentOrderDTO;
import com.example.spacemuseum.dto.GroupAllocationDTO;
import com.example.spacemuseum.entity.Allocation;
import com.example.spacemuseum.security.CurrentUser;
import com.example.spacemuseum.security.SecurityUtils;
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

/**
 * 配对台账接口。可见范围按角色收窄：
 * 馆务（STAFF）可查看全部研学团配对并维护台账；
 * 带队老师（TEACHER）仅可查看本团配对，访问他团一律 403，且不能改写配对。
 */
@RestController
@RequestMapping("/api/allocations")
public class AllocationController {

    @Autowired
    private AllocationService allocationService;

    @GetMapping
    public ResponseEntity<List<Allocation>> getAllAllocations() {
        CurrentUser user = SecurityUtils.currentUser();
        if (user.isStaff()) {
            return ResponseEntity.ok(allocationService.getAllAllocations());
        }
        return ResponseEntity.ok(allocationService.getAllocationsByGroup(user.groupId()));
    }

    @GetMapping("/group/{studyGroupId}")
    public ResponseEntity<List<Allocation>> getAllocationsByGroup(@PathVariable Long studyGroupId) {
        SecurityUtils.requireGroupAccess(studyGroupId);
        return ResponseEntity.ok(allocationService.getAllocationsByGroup(studyGroupId));
    }

    @GetMapping("/group/{studyGroupId}/detail")
    public ResponseEntity<GroupAllocationDTO> getGroupAllocationDetail(@PathVariable Long studyGroupId) {
        SecurityUtils.requireGroupAccess(studyGroupId);
        return ResponseEntity.ok(allocationService.getGroupAllocationDetail(studyGroupId));
    }

    /**
     * 实验顺序条：进馆前带队老师领取的本团凭条。
     * 按角色收窄可见范围：馆务可选任意研学团，带队老师只能开本团；
     * 内容实时取自当前有效占用并按时段先后排列，占用取消后重新打开即不含被取消的行。
     */
    @GetMapping("/group/{studyGroupId}/experiment-order")
    public ResponseEntity<ExperimentOrderDTO> getExperimentOrder(@PathVariable Long studyGroupId) {
        SecurityUtils.requireGroupAccess(studyGroupId);
        return ResponseEntity.ok(allocationService.getExperimentOrder(studyGroupId));
    }

    @GetMapping("/all-groups")
    public ResponseEntity<List<GroupAllocationDTO>> getAllGroupAllocations() {
        CurrentUser user = SecurityUtils.currentUser();
        if (user.isStaff()) {
            return ResponseEntity.ok(allocationService.getAllGroupAllocations());
        }
        // 带队老师刷新列表时仍只拿到本团，保证可见范围与角色权限一致
        return ResponseEntity.ok(List.of(allocationService.getGroupAllocationDetail(user.groupId())));
    }

    @GetMapping("/date/{visitDate}")
    public ResponseEntity<List<Allocation>> getAllocationsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate) {
        CurrentUser user = SecurityUtils.currentUser();
        if (user.isStaff()) {
            return ResponseEntity.ok(allocationService.getAllocationsByDate(visitDate));
        }
        return ResponseEntity.ok(allocationService.getAllocationsByDateAndGroup(visitDate, user.groupId()));
    }

    /**
     * 占用一览：仅馆务可查。按参观日返回每台设备的已排人数、当日容量与剩余容量，
     * 数字由当前有效占用实时汇总，占用变动后刷新即与台账一致。
     */
    @GetMapping("/occupancy/{visitDate}")
    public ResponseEntity<List<DeviceOccupancyDTO>> getDeviceOccupancy(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate visitDate) {
        SecurityUtils.requireStaff();
        return ResponseEntity.ok(allocationService.getDeviceOccupancy(visitDate));
    }

    @PostMapping("/auto/{studyGroupId}")
    public ResponseEntity<List<AllocationResultDTO>> autoAllocate(@PathVariable Long studyGroupId) {
        SecurityUtils.requireStaff();
        return ResponseEntity.ok(allocationService.autoAllocate(studyGroupId));
    }

    @PostMapping
    public ResponseEntity<Allocation> manualAllocate(@Valid @RequestBody AllocationDTO dto) {
        SecurityUtils.requireStaff();
        return ResponseEntity.ok(allocationService.manualAllocate(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Allocation> updateAllocation(@PathVariable Long id, @Valid @RequestBody AllocationDTO dto) {
        SecurityUtils.requireStaff();
        return ResponseEntity.ok(allocationService.updateAllocation(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> cancelAllocation(@PathVariable Long id) {
        SecurityUtils.requireStaff();
        allocationService.cancelAllocation(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "分配已取消");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/group/{studyGroupId}")
    public ResponseEntity<Map<String, String>> cancelGroupAllocations(@PathVariable Long studyGroupId) {
        SecurityUtils.requireStaff();
        allocationService.cancelExistingAllocations(studyGroupId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "该研学团所有分配已取消");
        return ResponseEntity.ok(response);
    }
}

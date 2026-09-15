package com.example.spacemuseum.controller;

import com.example.spacemuseum.dto.CreateMaintenanceOrderRequest;
import com.example.spacemuseum.dto.DeviceMaintenanceDTO;
import com.example.spacemuseum.dto.MaintenanceOrderDTO;
import com.example.spacemuseum.security.SecurityUtils;
import com.example.spacemuseum.service.MaintenanceOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 互动实验设备保养台接口。
 * 登录用户都可查看设备保养状态与工单；开单、开始保养、完成保养仅馆务可操作。
 */
@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    @Autowired
    private MaintenanceOrderService maintenanceService;

    /** 设备档案（看得出每台设备是否正在保养） */
    @GetMapping("/devices")
    public ResponseEntity<List<DeviceMaintenanceDTO>> listDevices() {
        return ResponseEntity.ok(maintenanceService.listDevicesWithMaintenance());
    }

    /** 工单列表：未完成在前，已完成在后 */
    @GetMapping("/orders")
    public ResponseEntity<List<MaintenanceOrderDTO>> listOrders() {
        return ResponseEntity.ok(maintenanceService.listOrders());
    }

    /** 开保养工单：选设备、写保养说明、计划完成日；同一设备有未完成工单时返回 409 */
    @PostMapping("/orders")
    public ResponseEntity<MaintenanceOrderDTO> createOrder(@Valid @RequestBody CreateMaintenanceOrderRequest request) {
        SecurityUtils.requireStaff();
        return ResponseEntity.status(HttpStatus.CREATED).body(maintenanceService.createOrder(request));
    }

    /** 待保养 → 保养中 */
    @PostMapping("/orders/{id}/start")
    public ResponseEntity<MaintenanceOrderDTO> startOrder(@PathVariable Long id) {
        SecurityUtils.requireStaff();
        return ResponseEntity.ok(maintenanceService.start(id));
    }

    /** 保养中 → 已完成（不能跳过中间态、不能从已完成退回） */
    @PostMapping("/orders/{id}/complete")
    public ResponseEntity<MaintenanceOrderDTO> completeOrder(@PathVariable Long id) {
        SecurityUtils.requireStaff();
        return ResponseEntity.ok(maintenanceService.complete(id));
    }
}

package com.example.spacemuseum.controller;

import com.example.spacemuseum.dto.DeviceInventoryDTO;
import com.example.spacemuseum.security.SecurityUtils;
import com.example.spacemuseum.service.DeviceInventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 设备盘点台账接口。台账为馆务功能：仅馆务人员可查看与登记实到件数。
 */
@RestController
@RequestMapping("/api/device-inventory")
public class DeviceInventoryController {

    @Autowired
    private DeviceInventoryService deviceInventoryService;

    @GetMapping
    public ResponseEntity<List<DeviceInventoryDTO>> getAllInventory() {
        SecurityUtils.requireStaff();
        return ResponseEntity.ok(deviceInventoryService.getAllInventory());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceInventoryDTO> updateActualParts(
            @PathVariable Long id, @Valid @RequestBody DeviceInventoryDTO dto) {
        SecurityUtils.requireStaff();
        return ResponseEntity.ok(deviceInventoryService.updateActualParts(id, dto));
    }
}

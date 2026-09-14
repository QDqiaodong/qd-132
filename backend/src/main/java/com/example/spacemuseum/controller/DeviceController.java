package com.example.spacemuseum.controller;

import com.example.spacemuseum.dto.DeviceDTO;
import com.example.spacemuseum.dto.TimeSlotDTO;
import com.example.spacemuseum.entity.Device;
import com.example.spacemuseum.entity.TimeSlot;
import com.example.spacemuseum.security.SecurityUtils;
import com.example.spacemuseum.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @GetMapping
    public ResponseEntity<List<Device>> getAllDevices() {
        return ResponseEntity.ok(deviceService.getAllDevices());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Device>> getActiveDevices() {
        return ResponseEntity.ok(deviceService.getActiveDevices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable Long id) {
        return deviceService.getDeviceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/suitable/{age}")
    public ResponseEntity<List<Device>> getSuitableDevices(@PathVariable Integer age) {
        return ResponseEntity.ok(deviceService.getSuitableDevices(age));
    }

    @PostMapping
    public ResponseEntity<Device> createDevice(@Valid @RequestBody DeviceDTO dto) {
        SecurityUtils.requireStaff();
        return ResponseEntity.ok(deviceService.createDevice(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Device> updateDevice(@PathVariable Long id, @Valid @RequestBody DeviceDTO dto) {
        SecurityUtils.requireStaff();
        return ResponseEntity.ok(deviceService.updateDevice(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        SecurityUtils.requireStaff();
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/slots")
    public ResponseEntity<List<TimeSlot>> getDeviceTimeSlots(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.getTimeSlotsByDevice(id));
    }

    @PostMapping("/{id}/slots")
    public ResponseEntity<Map<String, String>> addTimeSlot(@PathVariable Long id, @Valid @RequestBody TimeSlotDTO dto) {
        SecurityUtils.requireStaff();
        deviceService.addTimeSlot(id, dto);
        Map<String, String> response = new HashMap<>();
        response.put("message", "时段添加成功");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/slots/{slotId}")
    public ResponseEntity<Map<String, String>> updateTimeSlot(@PathVariable Long slotId, @Valid @RequestBody TimeSlotDTO dto) {
        SecurityUtils.requireStaff();
        deviceService.updateTimeSlot(slotId, dto);
        Map<String, String> response = new HashMap<>();
        response.put("message", "时段更新成功");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/slots/{slotId}")
    public ResponseEntity<Map<String, String>> deleteTimeSlot(@PathVariable Long slotId) {
        SecurityUtils.requireStaff();
        deviceService.deleteTimeSlot(slotId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "时段删除成功");
        return ResponseEntity.ok(response);
    }
}
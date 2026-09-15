package com.example.spacemuseum.service;

import com.example.spacemuseum.dto.DeviceInventoryDTO;
import com.example.spacemuseum.entity.Device;
import com.example.spacemuseum.entity.DeviceInventory;
import com.example.spacemuseum.repository.DeviceInventoryRepository;
import com.example.spacemuseum.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设备盘点台账：每台互动实验设备一行，登记应出库配件件数与实到件数。
 * 两数对不上就记一笔差异（mismatch=true、differenceCount=实到-应出），行上标成盘点不符；
 * 对得上差异清零、去掉标记。改动实到件数（或应出件数）后一律按新数字重算。
 */
@Service
public class DeviceInventoryService {

    @Autowired
    private DeviceInventoryRepository inventoryRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Transactional
    public List<DeviceInventoryDTO> getAllInventory() {
        // 台账覆盖全部设备：新设备还没有盘点行时先补一行（应出/实到初始为 0/空，不带差异标记）
        List<Device> missing = inventoryRepository.findDevicesWithoutInventory();
        for (Device device : missing) {
            DeviceInventory row = new DeviceInventory();
            row.setDevice(device);
            row.setExpectedParts(0);
            row.setActualParts(null);
            row.setMismatch(false);
            row.setDifferenceCount(null);
            inventoryRepository.save(row);
        }
        return inventoryRepository.findAllWithDevice().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DeviceInventoryDTO updateActualParts(Long id, DeviceInventoryDTO dto) {
        DeviceInventory row = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("盘点记录不存在"));

        row.setExpectedParts(dto.getExpectedParts());
        row.setActualParts(dto.getActualParts());
        row.setRemark(dto.getRemark());
        recalculate(row);

        DeviceInventory saved = inventoryRepository.save(row);
        return toDTO(saved);
    }

    /**
     * 按当前应出/实到重算差异：
     * 已盘点且两数不等才记差异并打上盘点不符标记；两数相等则差异清零、不带标记；
     * 尚未盘点（实到为空）也不记差异。
     */
    private void recalculate(DeviceInventory row) {
        Integer expected = row.getExpectedParts();
        Integer actual = row.getActualParts();
        if (actual != null && expected != null && !actual.equals(expected)) {
            row.setMismatch(true);
            row.setDifferenceCount(actual - expected);
            row.setInventoryTime(LocalDateTime.now());
        } else {
            row.setMismatch(false);
            row.setDifferenceCount(actual == null ? null : 0);
            if (actual != null) {
                row.setInventoryTime(LocalDateTime.now());
            }
        }
    }

    private DeviceInventoryDTO toDTO(DeviceInventory row) {
        DeviceInventoryDTO dto = new DeviceInventoryDTO();
        dto.setId(row.getId());
        dto.setDeviceId(row.getDevice().getId());
        dto.setDeviceCode(row.getDevice().getDeviceCode());
        dto.setDeviceName(row.getDevice().getDeviceName());
        dto.setExpectedParts(row.getExpectedParts());
        dto.setActualParts(row.getActualParts());
        dto.setMismatch(row.getMismatch());
        dto.setDifferenceCount(row.getDifferenceCount());
        dto.setInventoryTime(row.getInventoryTime());
        dto.setRemark(row.getRemark());
        return dto;
    }
}

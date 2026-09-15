package com.example.spacemuseum.service;

import com.example.spacemuseum.dto.CreateMaintenanceOrderRequest;
import com.example.spacemuseum.dto.DeviceMaintenanceDTO;
import com.example.spacemuseum.dto.MaintenanceOrderDTO;
import com.example.spacemuseum.entity.Device;
import com.example.spacemuseum.entity.MaintenanceOrder;
import com.example.spacemuseum.entity.MaintenanceStatus;
import com.example.spacemuseum.exception.ConflictException;
import com.example.spacemuseum.repository.DeviceRepository;
import com.example.spacemuseum.repository.MaintenanceOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 保养工单业务：
 * 1. 同一台设备只要还有未完成工单（待保养/保养中）就不能再开新单。
 * 2. 两人几乎同时为同一台设备开单时，靠“设备行悲观锁 + 唯一索引 uk_maintenance_open_per_device”
 *    双保险，只会留下一张工单（后者收到 409）。
 * 3. 工单状态只能 待保养→保养中→已完成，不能跳过中间态，也不能从已完成退回。
 * 4. 设备档案接口带出“是否正在保养”，前端设备列表可直接辨认。
 */
@Service
public class MaintenanceOrderService {

    /** 未完成工单判别列取值；完成后置为 null（MySQL 唯一索引对 NULL 不去重） */
    private static final int ACTIVE = 1;

    @Autowired
    private MaintenanceOrderRepository orderRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    /**
     * 开单先锁设备行再判重、插入：
     * 并发请求在同一把设备行锁上排队，第二个请求拿到锁后做判重检查必然失败；
     * 即便绕过检查（如多实例部署），数据库唯一索引 uk_maintenance_open_per_device 仍会拒绝第二张工单。
     */
    @Transactional
    public MaintenanceOrderDTO createOrder(CreateMaintenanceOrderRequest request) {
        Device device = deviceRepository.findByIdForUpdate(request.getDeviceId())
                .orElseThrow(() -> new IllegalArgumentException("设备不存在"));

        if (request.getPlannedDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("计划完成日不能早于今天");
        }

        orderRepository.findFirstByDeviceIdAndActiveFlag(device.getId(), ACTIVE)
                .ifPresent(existing -> {
                    throw new ConflictException(
                            "OPEN_ORDER_EXISTS",
                            "该设备已有未完成工单（" + existing.getOrderNo() + "），不能重复开单");
                });

        MaintenanceOrder order = new MaintenanceOrder();
        order.setDevice(device);
        order.setDescription(request.getDescription().trim());
        order.setPlannedDate(request.getPlannedDate());
        order.setStatus(MaintenanceStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setActiveFlag(ACTIVE);

        try {
            // 先 flush 拿到自增 id，再据此生成对外工单号（WX+日期+当日序号），保证并发下也唯一
            MaintenanceOrder saved = orderRepository.saveAndFlush(order);
            saved.setOrderNo(generateOrderNo(saved.getId()));
            return toDTO(orderRepository.save(saved));
        } catch (DataIntegrityViolationException e) {
            // 并发兜底：唯一索引冲突说明已有另一张未完成工单落库
            throw new ConflictException("OPEN_ORDER_EXISTS", "该设备已有未完成工单，不能重复开单");
        }
    }

    /** 待保养 → 保养中 */
    @Transactional
    public MaintenanceOrderDTO start(Long id) {
        MaintenanceOrder order = requireOrder(id);
        switch (order.getStatus()) {
            case COMPLETED -> throw new ConflictException("ORDER_ALREADY_COMPLETED", "工单已完成，不能退回");
            case IN_PROGRESS -> throw new ConflictException("ORDER_ALREADY_STARTED", "工单已在保养中");
            case PENDING -> {
                order.setStatus(MaintenanceStatus.IN_PROGRESS);
                order.setStartedAt(LocalDateTime.now());
            }
        }
        return toDTO(orderRepository.save(order));
    }

    /** 保养中 → 已完成（不允许从待保养直接完成，不能跳过中间态） */
    @Transactional
    public MaintenanceOrderDTO complete(Long id) {
        MaintenanceOrder order = requireOrder(id);
        switch (order.getStatus()) {
            case COMPLETED -> throw new ConflictException("ORDER_ALREADY_COMPLETED", "工单已完成，不能退回");
            case PENDING -> throw new ConflictException("CANNOT_SKIP_IN_PROGRESS",
                    "工单需先“开始保养”进入保养中，不能跳过中间态直接完成");
            case IN_PROGRESS -> {
                order.setStatus(MaintenanceStatus.COMPLETED);
                order.setCompletedAt(LocalDateTime.now());
                // 放开“一台一张未完成工单”的唯一约束：active_flag 置空，之后该设备可再开新单
                order.setActiveFlag(null);
            }
        }
        return toDTO(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<MaintenanceOrderDTO> listOrders() {
        return orderRepository.findAllOrdered().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 设备档案 + 保养状态：一次查出全部设备和全部未完成工单后在内存中关联，
     * 每台设备都能看出是否正在保养（待保养/保养中）以及对应工单号。
     */
    @Transactional(readOnly = true)
    public List<DeviceMaintenanceDTO> listDevicesWithMaintenance() {
        Map<Long, MaintenanceOrder> openByDevice = orderRepository
                .findAllOrdered().stream()
                .filter(o -> o.getActiveFlag() != null && o.getActiveFlag() == ACTIVE)
                .collect(Collectors.toMap(o -> o.getDevice().getId(), o -> o, (a, b) -> a));

        return deviceRepository.findAll().stream()
                .map(device -> {
                    DeviceMaintenanceDTO dto = new DeviceMaintenanceDTO();
                    dto.setId(device.getId());
                    dto.setDeviceCode(device.getDeviceCode());
                    dto.setDeviceName(device.getDeviceName());
                    dto.setDescription(device.getDescription());
                    dto.setExperienceDuration(device.getExperienceDuration());
                    dto.setMinAge(device.getMinAge());
                    dto.setMaxAge(device.getMaxAge());
                    dto.setCapacity(device.getCapacity());
                    dto.setStatus(device.getStatus());

                    MaintenanceOrder open = openByDevice.get(device.getId());
                    dto.setUnderMaintenance(open != null);
                    if (open != null) {
                        dto.setOpenOrderNo(open.getOrderNo());
                        dto.setOpenOrderStatus(open.getStatus().name());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private MaintenanceOrder requireOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("工单不存在"));
    }

    private static final DateTimeFormatter ORDER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 工单号 = WX + 8 位日期 + 3 位自增序号，如 WX20260915001 */
    private String generateOrderNo(Long id) {
        return String.format("WX%s%03d", LocalDate.now().format(ORDER_NO_FMT), id);
    }

    private MaintenanceOrderDTO toDTO(MaintenanceOrder order) {
        MaintenanceOrderDTO dto = new MaintenanceOrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setDeviceId(order.getDevice().getId());
        dto.setDeviceCode(order.getDevice().getDeviceCode());
        dto.setDeviceName(order.getDevice().getDeviceName());
        dto.setDescription(order.getDescription());
        dto.setPlannedDate(order.getPlannedDate());
        dto.setStatus(order.getStatus());
        dto.setActive(order.getActiveFlag() != null && order.getActiveFlag() == ACTIVE);
        dto.setCreatedAt(order.getCreatedAt());
        dto.setStartedAt(order.getStartedAt());
        dto.setCompletedAt(order.getCompletedAt());
        return dto;
    }
}

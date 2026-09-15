package com.example.spacemuseum.dto;

import com.example.spacemuseum.entity.MaintenanceStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 保养工单视图对象。
 */
@Data
public class MaintenanceOrderDTO {

    private Long id;

    private String orderNo;

    private Long deviceId;

    private String deviceCode;

    private String deviceName;

    private String description;

    private LocalDate plannedDate;

    private MaintenanceStatus status;

    /** 是否仍未完成（待保养/保养中） */
    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;
}

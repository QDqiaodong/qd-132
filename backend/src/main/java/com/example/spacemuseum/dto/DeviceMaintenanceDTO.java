package com.example.spacemuseum.dto;

import lombok.Data;

/**
 * 设备档案上的保养状态视图：在设备信息之外附带“是否正在保养”。
 */
@Data
public class DeviceMaintenanceDTO {

    private Long id;

    private String deviceCode;

    private String deviceName;

    private String description;

    private Integer experienceDuration;

    private Integer minAge;

    private Integer maxAge;

    private Integer capacity;

    private Integer status;

    /** 是否有未完成工单（待保养/保养中） */
    private Boolean underMaintenance;

    /** 当前未完成工单号，没有则为 null */
    private String openOrderNo;

    /** 当前未完成工单状态：PENDING / IN_PROGRESS / null */
    private String openOrderStatus;
}

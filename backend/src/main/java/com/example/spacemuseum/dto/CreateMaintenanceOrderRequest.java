package com.example.spacemuseum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 开保养工单请求：选择设备、保养说明、计划完成日。
 */
@Data
public class CreateMaintenanceOrderRequest {

    @NotNull(message = "必须选择设备")
    private Long deviceId;

    @NotBlank(message = "保养说明不能为空")
    @Size(max = 500, message = "保养说明不能超过500字")
    private String description;

    @NotNull(message = "计划完成日不能为空")
    private LocalDate plannedDate;
}

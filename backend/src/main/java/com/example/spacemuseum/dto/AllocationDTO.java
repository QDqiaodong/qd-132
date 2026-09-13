package com.example.spacemuseum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllocationDTO {

    private Long id;

    @NotNull(message = "研学团ID不能为空")
    private Long studyGroupId;

    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @NotNull(message = "时段ID不能为空")
    private Long timeSlotId;

    @NotNull(message = "学生人数不能为空")
    private Integer studentCount;

    private LocalDateTime allocationTime;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private String batchNumber;
}
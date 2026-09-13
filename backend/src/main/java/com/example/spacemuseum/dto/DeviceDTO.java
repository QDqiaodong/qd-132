package com.example.spacemuseum.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceDTO {

    private Long id;

    @NotBlank(message = "设备编号不能为空")
    @Size(max = 50, message = "设备编号长度不能超过50")
    private String deviceCode;

    @NotBlank(message = "设备名称不能为空")
    @Size(max = 100, message = "设备名称长度不能超过100")
    private String deviceName;

    @Size(max = 500, message = "设备描述长度不能超过500")
    private String description;

    @NotNull(message = "体验时长不能为空")
    @Min(value = 5, message = "体验时长不能小于5分钟")
    @Max(value = 120, message = "体验时长不能大于120分钟")
    private Integer experienceDuration;

    @NotNull(message = "最小年龄不能为空")
    @Min(value = 3, message = "最小年龄不能小于3岁")
    @Max(value = 18, message = "最小年龄不能大于18岁")
    private Integer minAge;

    @NotNull(message = "最大年龄不能为空")
    @Min(value = 3, message = "最大年龄不能小于3岁")
    @Max(value = 18, message = "最大年龄不能大于18岁")
    private Integer maxAge;

    @NotNull(message = "容量不能为空")
    @Min(value = 1, message = "容量不能小于1")
    @Max(value = 100, message = "容量不能大于100")
    private Integer capacity;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private List<TimeSlotDTO> timeSlots;
}
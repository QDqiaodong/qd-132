package com.example.spacemuseum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotDTO {

    private Long id;

    private Long deviceId;

    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;

    @NotNull(message = "星期不能为空")
    private Integer dayOfWeek;

    private Boolean available = true;

    @NotNull(message = "排序号不能为空")
    private Integer sortOrder;
}
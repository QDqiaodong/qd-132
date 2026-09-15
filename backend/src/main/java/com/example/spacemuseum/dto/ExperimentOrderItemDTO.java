package com.example.spacemuseum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 实验顺序条：带队老师进馆前领取的本团实验顺序凭条。
 * 一行 = 一个已配上的有效占用，按时段开始时间先后排列；
 * 占用取消（status 置 0）后该行不再出现在顺序条中。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExperimentOrderItemDTO {

    /** 顺序号（从 1 开始，按时间先后） */
    private Integer sequence;

    private Long allocationId;

    private String deviceCode;

    private String deviceName;

    private LocalTime startTime;

    private LocalTime endTime;

    /** 该时段进入该设备实验的人数 */
    private Integer studentCount;
}

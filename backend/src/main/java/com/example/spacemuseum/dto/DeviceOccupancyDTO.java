package com.example.spacemuseum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 某参观日单台设备的占用一览：已排人数按当前有效占用实时汇总，
 * 当日容量 = 设备单时段容量 × 当日可用时段数，剩余容量 = 当日容量 - 已排人数。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceOccupancyDTO {

    private Long deviceId;
    private String deviceCode;
    private String deviceName;
    /** 参观日当天的可用时段数 */
    private Integer slotCount;
    /** 该参观日已排进这台设备的学生总数 */
    private Integer allocatedStudents;
    /** 当日总容量：单时段容量 × 当日时段数 */
    private Integer totalCapacity;
    /** 剩余容量：当日容量 - 已排人数 */
    private Integer remainingCapacity;
}

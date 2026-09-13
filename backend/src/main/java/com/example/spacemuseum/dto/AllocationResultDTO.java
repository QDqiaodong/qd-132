package com.example.spacemuseum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllocationResultDTO {

    private Long allocationId;
    private Long deviceId;
    private String deviceCode;
    private String deviceName;
    private Long timeSlotId;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer studentCount;
    private String batchNumber;
}
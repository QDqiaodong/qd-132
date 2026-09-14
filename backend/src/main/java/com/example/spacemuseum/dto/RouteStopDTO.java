package com.example.spacemuseum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteStopDTO {

    private Long stopId;
    private Long deviceId;
    private String deviceCode;
    private String deviceName;
    private Integer stopOrder;
    private Boolean released;
    private Boolean completed;
    private LocalDateTime releasedTime;
    private LocalDateTime completedTime;
}

package com.example.spacemuseum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutePlanDTO {

    /** 未编排动线时为 null */
    private Long routeId;
    private Long groupId;
    private String groupCode;
    private String groupName;
    private String schoolName;
    private LocalDate visitDate;
    private String routeName;
    private List<RouteStopDTO> stops = new ArrayList<>();
}

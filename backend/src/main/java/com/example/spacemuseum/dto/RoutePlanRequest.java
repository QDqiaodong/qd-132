package com.example.spacemuseum.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 编排动线请求：deviceIds 按站序排列，下标即游览顺序。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutePlanRequest {

    private String routeName;

    @NotEmpty(message = "动线至少包含一个站点")
    private List<Long> deviceIds;
}

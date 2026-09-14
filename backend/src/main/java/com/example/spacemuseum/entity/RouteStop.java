package com.example.spacemuseum.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 动线站点：一个研学团动线上的一台互动实验设备。
 * released 表示是否已放行进入该站；completed 表示该站体验已完成。
 * 只有按站序完成上一站后，下一站才允许放行。
 */
@Entity
@Table(name = "route_stop")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_plan_id", nullable = false)
    private RoutePlan routePlan;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    /** 站序：从 1 开始递增，决定放行先后 */
    @Column(name = "stop_order", nullable = false)
    private Integer stopOrder;

    /** 是否已放行进入该站 */
    @Column(name = "released", nullable = false)
    private Boolean released;

    /** 该站体验是否已完成 */
    @Column(name = "completed", nullable = false)
    private Boolean completed;

    @Column(name = "released_time")
    private LocalDateTime releasedTime;

    @Column(name = "completed_time")
    private LocalDateTime completedTime;
}

package com.example.spacemuseum.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 互动实验设备保养工单。
 * 同一台设备只要还存在“未完成”（待保养/保养中）工单，就不能再开新单。
 */
@Entity
@Table(
        name = "maintenance_order",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_maintenance_open_per_device",
                columnNames = {"device_id", "active_flag"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 工单号，便于人工辨认，如 WX20260915001；先插入拿到自增 id 后再回填 */
    @Column(name = "order_no", unique = true, length = 32, insertable = false, updatable = true)
    private String orderNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    /** 保养说明 */
    @Column(name = "description", nullable = false, length = 500)
    private String description;

    /** 计划完成日 */
    @Column(name = "planned_date", nullable = false)
    private LocalDate plannedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MaintenanceStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * 未完成唯一约束用的判别列：未完成工单恒为 1，完成后置为 null。
     * MySQL 唯一索引对多个 NULL 不去重，因此每台设备至多有一行 active_flag=1，
     * 两人同时为同一台设备开工单时，数据库层面只允许一张插入成功，另一张直接冲突。
     */
    @Column(name = "active_flag")
    private Integer activeFlag;
}

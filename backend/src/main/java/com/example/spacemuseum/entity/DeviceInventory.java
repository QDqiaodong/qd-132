package com.example.spacemuseum.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 互动实验设备盘点台账行：每台设备一行，
 * 登记应出库配件件数与实到件数，两数对不上时记一笔差异（mismatch + differenceCount）。
 */
@Entity
@Table(name = "device_inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", unique = true, nullable = false)
    private Device device;

    /** 应出库配件件数 */
    @Column(name = "expected_parts", nullable = false)
    private Integer expectedParts;

    /** 实到件数；尚未盘点时为空 */
    @Column(name = "actual_parts")
    private Integer actualParts;

    /** 盘点不符标记：实到与应出不一致即为 true，一致或未盘点为 false */
    @Column(name = "mismatch_flag", nullable = false)
    private Boolean mismatch = false;

    /** 差异件数：实到 - 应出；未盘点时为空 */
    @Column(name = "difference_count")
    private Integer differenceCount;

    /** 最近一次盘点时间 */
    @Column(name = "inventory_time")
    private LocalDateTime inventoryTime;

    @Column(name = "remark", length = 500)
    private String remark;
}

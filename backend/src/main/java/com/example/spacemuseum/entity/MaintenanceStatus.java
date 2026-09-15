package com.example.spacemuseum.entity;

/**
 * 保养工单状态，只能沿固定方向逐步前进：
 * 待保养(PENDING) → 保养中(IN_PROGRESS) → 已完成(COMPLETED)。
 * 不允许跳过中间态，也不允许从已完成退回。
 */
public enum MaintenanceStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}

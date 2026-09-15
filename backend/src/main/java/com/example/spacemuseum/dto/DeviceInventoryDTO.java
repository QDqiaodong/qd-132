package com.example.spacemuseum.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInventoryDTO {

    private Long id;

    private Long deviceId;

    private String deviceCode;

    private String deviceName;

    /** 应出库配件件数 */
    @NotNull(message = "应出库配件件数不能为空")
    @Min(value = 0, message = "应出库配件件数不能为负数")
    private Integer expectedParts;

    /** 实到件数；尚未盘点时为空 */
    @Min(value = 0, message = "实到件数不能为负数")
    private Integer actualParts;

    /** 盘点不符标记，由服务端按应出与实到重算，不信任前端传值 */
    private Boolean mismatch;

    /** 差异件数：实到 - 应出 */
    private Integer differenceCount;

    private LocalDateTime inventoryTime;

    private String remark;
}

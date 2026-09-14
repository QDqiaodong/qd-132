package com.example.spacemuseum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 某一参观日的到馆人数汇总。
 * 到馆人数不取缓存值，每次请求都按当天各研学团的当前人数现算，
 * 因此某一团人数修改后，再次查询即为新人数加总的结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyAttendanceDTO {

    /** 参观日期 */
    private LocalDate visitDate;

    /** 当天有效（已预约）研学团数量 */
    private Integer groupCount;

    /** 当天到馆人数：各有效研学团学生人数之和 */
    private Integer totalStudents;
}
